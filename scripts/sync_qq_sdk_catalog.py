"""Snapshot QQ's published API routes and generate the Java endpoint catalogue.

Network access is explicit (--refresh); normal builds use the committed snapshot.
The deployed VuePress manifest, rather than the older GitHub docs tree, is the
source of truth for the current public API and event inventory.
"""
import argparse
import concurrent.futures
import hashlib
import html
import json
import re
from datetime import datetime, timezone, timedelta
from pathlib import Path
from urllib.request import urlopen

ROOT = Path(__file__).resolve().parents[1]
SPEC = ROOT / "spec/official-api.json"
JAVA = ROOT / "src/main/java/top/sshh/qqbot/sdk"
BASE = "https://bot.q.qq.com/wiki"


def fetch(url):
    with urlopen(url, timeout=30) as response:
        return response.read().decode("utf-8")


def plain(value):
    value = re.sub(r"</?(?:p|h[1-6]|pre|tr|td|th|br|div|li|table)\b[^>]*>", " ", value)
    return html.unescape(re.sub(r"<[^>]*>", "", value)).strip().lstrip("# ")


def parse_page(route):
    source = fetch(BASE + route)
    main = source.split('<main class="page">', 1)[-1].split("</main>", 1)[0]
    rows = {plain(k): plain(v) for k, v in re.findall(
        r"<tr><td>(.*?)</td>\s*<td>(.*?)</td>.*?</tr>", main, re.S)}
    path, method = rows.get("HTTP URL"), rows.get("HTTP Method")
    heading = re.search(r"<h1[^>]*>(.*?)</h1>", main, re.S)
    title = plain(heading[1]) if heading else route
    text = plain(main)
    pairs = [(method, path)] if path and method else []
    # The deployed site still documents channel APIs outside autogen/. Include
    # their HTTP declarations too, but exclude concrete example IDs/URLs.
    if "/autogen/" not in route and "/dev-prepare/" not in route:
        pairs += re.findall(r"\b(GET|POST|PUT|PATCH|DELETE)\s+(/[A-Za-z0-9_{}@./-]+)", text)
    endpoints = []
    for verb, path in set(pairs):
        if verb not in {"GET", "POST", "PUT", "PATCH", "DELETE"}:
            continue
        if re.search(r"/(?:\d+|x{3,}|abc|123abc)(?:/|$)", path):
            continue
        if path.startswith("http"):
            from urllib.parse import urlsplit
            path = urlsplit(path).path
        endpoints.append({"method": verb, "path": path, "title": title,
                          "documentation": BASE + route, "restricted": "内邀" in text,
                          "sha256": hashlib.sha256(main.encode()).hexdigest()})
    names = {plain(h) for h in re.findall(r"<h[234][^>]*>(.*?)</h[234]>", main, re.S)}
    names |= set(re.findall(r'"t"\s*:\s*"([A-Z][A-Z_]+)"', text))
    names |= set(re.findall(r"-\s+([A-Z][A-Z_]+)\s+//", text))
    events = [{"name": n, "documentation": BASE + route} for n in names
              if re.fullmatch(r"[A-Z]+(?:_[A-Z]+)+", n) and n != "GATEWAY_EVENT_NAME"]
    return endpoints, events


def refresh():
    home = fetch(BASE + "/develop/api-v2/")
    app = re.search(r'https://[^" ]+/assets/js/app\.[^" ]+\.js', home)[0]
    manifest = fetch(app)
    routes = sorted(set(re.findall(r'["\'](/develop/api-v2/[^"\']+\.html)["\']', manifest)))
    event_routes = sorted(set(re.findall(r'["\'](/develop/api-v2/autogen/event/[^"\']+\.html)["\']', manifest)))
    if not routes or not event_routes:
        raise ValueError("Official manifest has no API/event inventory")
    with concurrent.futures.ThreadPoolExecutor(max_workers=8) as pool:
        pages = list(pool.map(parse_page, routes))
    endpoints = {}; events = {}
    for ep, ev in pages:
        for item in ep:
            key = (item["method"], item["path"])
            if key not in endpoints or "/autogen/" in item["documentation"]:
                endpoints[key] = item
        for item in ev:
            events[item["name"]] = item
    for p in event_routes:
        name = p.rsplit("/", 1)[-1][:-5].upper()
        events[name] = {"name": name, "documentation": BASE + p}
    spec = {"source": app, "snapshotDate": datetime.now(timezone(timedelta(hours=8))).date().isoformat(), "scannedPages": len(routes),
            "endpoints": [endpoints[k] for k in sorted(endpoints)],
            "events": [events[k] for k in sorted(events)]}
    SPEC.parent.mkdir(parents=True, exist_ok=True)
    SPEC.write_text(json.dumps(spec, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def identifier(endpoint):
    return endpoint["method"] + "_" + re.sub(r"[^A-Za-z0-9]+", "_", endpoint["path"]).strip("_").upper()


def render(spec):
    constants = []
    methods = []
    for ep in spec["endpoints"]:
        key = identifier(ep)
        constants.append(f'    {key}("{ep["method"]}", "{ep["path"]}", "{ep["documentation"]}", {str(ep["restricted"]).lower()})')
        name = key.lower().split("_")
        method = name[0] + "".join(p.title() for p in name[1:])
        params = re.findall(r"\{([^}]+)\}", ep["path"])
        java_params = ["String " + p for p in params] + ["ApiRequest request"]
        path_map = "Map.of(" + ", ".join('"' + p + '", ' + p for p in params) + ")"
        methods.append(f'''    /** {ep["title"]}. @see <a href="{ep["documentation"]}">Official documentation</a> */
    public ApiResponse {method}({", ".join(java_params)}) {{
        return call(ApiEndpoint.{key}, {path_map}, request);
    }}''')
    enum = '''package top.sshh.qqbot.sdk;

/** Generated by scripts/sync_qq_sdk_catalog.py; do not edit. */
public enum ApiEndpoint {
''' + ",\n".join(constants) + ''';

    private final String method;
    private final String path;
    private final String documentation;
    private final boolean restricted;

    ApiEndpoint(String method, String path, String documentation, boolean restricted) {
        this.method = method; this.path = path; this.documentation = documentation; this.restricted = restricted;
    }
    public String method() { return method; }
    public String path() { return path; }
    public String documentation() { return documentation; }
    public boolean restricted() { return restricted; }
}
'''
    client = '''package top.sshh.qqbot.sdk;

import java.util.Map;

/** All REST operations in the committed official documentation snapshot. */
public final class QQOpenApi {
    private final ApiClient client;
    public QQOpenApi(ApiClient client) { this.client = java.util.Objects.requireNonNull(client); }
    public ApiResponse call(ApiEndpoint endpoint, Map<String, String> path, ApiRequest request) {
        return client.execute(endpoint, path, request);
    }

''' + "\n\n".join(methods) + "\n}\n"
    event = '''package top.sshh.qqbot.sdk;

/** Published event names; unknown future event names remain available through BotEvent. */
public enum EventType {
    ''' + ",\n    ".join(e["name"] for e in spec["events"]) + ";\n}\n"
    return {"ApiEndpoint.java": enum, "QQOpenApi.java": client, "EventType.java": event}


def coverage(spec):
    lines = ["# QQ Java SDK 接口覆盖清单", "",
             f'快照日期：{spec["snapshotDate"]}；扫描 {spec["scannedPages"]} 个官方文档页面，'
             f'收录 {len(spec["endpoints"])} 个 REST 接口和 {len(spec["events"])} 种事件。', "",
             "由 `scripts/sync_qq_sdk_catalog.py` 生成；端点、来源与页面摘要见 `spec/official-api.json`。", "",
             "所有 REST 接口均有独立 Java 方法，公共字段通过 ApiRequest 的 JSON Map、query、multipart 或原始字节传入。",
             "标记内邀的接口已封装，实际访问权限由 QQ 平台决定。", "",
             "| 方法 | 路径 | Java 方法 | 官方文档 |", "|---|---|---|---|"]
    for e in spec["endpoints"]:
        words = identifier(e).lower().split("_")
        name = words[0] + "".join(w.title() for w in words[1:])
        lines.append(f'| {e["method"]} | `{e["path"]}` | `{name}` | [{e["title"]}]({e["documentation"]}) |')
    lines += ["", "## 事件", "", "全部事件通过 `onEvent` 或 `on(EventType, ...)` 订阅，未知新事件同样保留原始数据。", "",
              "| 事件 | 官方文档 |", "|---|---|"]
    for e in spec["events"]:
        lines.append(f'| `{e["name"]}` | [文档]({e["documentation"]}) |')
    return "\n".join(lines) + "\n"


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--refresh", action="store_true")
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    if args.refresh:
        refresh()
    spec = json.loads(SPEC.read_text(encoding="utf-8"))
    JAVA.mkdir(parents=True, exist_ok=True)
    for name, content in render(spec).items():
        path = JAVA / name
        if args.check:
            if not path.exists() or path.read_text(encoding="utf-8") != content:
                raise SystemExit("Generated catalogue differs: " + str(path))
        else:
            path.write_text(content, encoding="utf-8")
    coverage_path = SPEC.parent.parent / "API_COVERAGE.md"
    if args.check:
        if not coverage_path.exists() or coverage_path.read_text(encoding="utf-8") != coverage(spec):
            raise SystemExit("Generated coverage document differs")
    else:
        coverage_path.write_text(coverage(spec), encoding="utf-8")
    print(f'{len(spec["endpoints"])} REST endpoints, {len(spec["events"])} events; catalogue verified')


if __name__ == "__main__":
    main()
