"""Verify the actual release jars and compile both standalone integration examples."""
import os
import shutil
import struct
import subprocess
import xml.etree.ElementTree as ET
from pathlib import Path
from zipfile import ZipFile

ROOT = Path(__file__).resolve().parents[1]
NS = {"m": "http://maven.apache.org/POM/4.0.0"}


def verify(root=ROOT):
    pom = ET.parse(root / "pom.xml").getroot()
    version = pom.findtext("m:version", namespaces=NS)
    artifact = pom.findtext("m:artifactId", namespaces=NS)
    base = root / "target" / f"{artifact}-{version}"
    with ZipFile(str(base) + ".jar") as jar:
        classes = [name for name in jar.namelist() if name.endswith(".class")]
        assert classes, "Binary jar has no classes"
        for name in classes:
            assert name.startswith("top/sshh/qqbot/sdk/"), f"Unexpected runtime class: {name}"
            magic, minor, major = struct.unpack(">IHH", jar.read(name)[:8])
            assert magic == 0xCAFEBABE and major == 61, f"Not Java 17 bytecode: {name}"
        for name in ("official-api.json", "sdk-references.json"):
            assert jar.read("META-INF/qqbot/" + name) == (root / "spec" / name).read_bytes()
        published = ET.fromstring(jar.read(f"META-INF/maven/com.github.BoBoJiangs/{artifact}/pom.xml"))
        assert published.find("m:parent", NS) is None, "Published POM depends on an external parent"
    with ZipFile(str(base) + "-sources.jar") as jar:
        assert "top/sshh/qqbot/sdk/QQBot.java" in jar.namelist()
    with ZipFile(str(base) + "-javadoc.jar") as jar:
        assert "top/sshh/qqbot/sdk/QQBot.html" in jar.namelist()
    classpath = os.pathsep.join([str(base) + ".jar", (root / "target/classpath.txt").read_text().strip()])
    java_home = os.environ.get("JAVA_HOME")
    suffix = ".exe" if os.name == "nt" else ""
    javac = str(Path(java_home) / "bin" / ("javac" + suffix)) if java_home else shutil.which("javac")
    assert javac, "javac is required to verify examples"
    subprocess.run([javac, "--release", "17", "-encoding", "UTF-8", "-cp", classpath,
                    "-d", str(root / "target/examples"),
                    str(root / "examples/StandaloneBot.java"), str(root / "examples/WebhookServer.java")], check=True)
    print(f"Verified {version}: Java 17 binary, embedded specs, sources, Javadoc, and both examples")


if __name__ == "__main__":
    verify()
