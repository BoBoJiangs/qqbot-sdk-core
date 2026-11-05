"""Check that a checkout is independently buildable and documented correctly."""
import re
import unittest
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
NS = {"m": "http://maven.apache.org/POM/4.0.0"}


class DistributionTest(unittest.TestCase):
    def test_published_pom_resolves_without_the_bot_parent(self):
        pom = ET.parse(ROOT / "pom.xml").getroot()
        self.assertIsNone(pom.find("m:parent", NS))
        self.assertEqual("com.github.BoBoJiangs", pom.findtext("m:groupId", namespaces=NS))
        self.assertEqual("17", pom.findtext("m:properties/m:maven.compiler.release", namespaces=NS))
        runtime = set()
        properties = {node.tag.split("}")[-1]: node.text for node in pom.find("m:properties", NS)}
        for dep in pom.findall("m:dependencies/m:dependency", NS):
            version = dep.findtext("m:version", namespaces=NS)
            self.assertTrue(version, "Dependency version is not self-contained")
            if version.startswith("${"):
                self.assertIn(version[2:-1], properties)
            if dep.findtext("m:scope", default="compile", namespaces=NS) != "test":
                runtime.add(dep.findtext("m:artifactId", namespaces=NS))
        self.assertEqual({"fastjson2", "slf4j-api"}, runtime)

    def test_jitpack_builds_with_java_17_and_runs_tests(self):
        config = (ROOT / "jitpack.yml").read_text(encoding="utf-8")
        self.assertIn("openjdk17", config)
        self.assertRegex(config, r"\./mvnw .*clean install")
        self.assertNotIn("skipTests", config)
        self.assertNotIn("maven.test.skip", config)

    def test_maven_wrapper_bootstraps_a_known_version_on_both_platforms(self):
        properties = (ROOT / ".mvn/wrapper/maven-wrapper.properties").read_text(encoding="utf-8")
        self.assertIn("distributionUrl=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.11/apache-maven-3.9.11-bin.zip", properties)
        self.assertTrue((ROOT / "mvnw").is_file())
        self.assertTrue((ROOT / "mvnw.cmd").is_file())
        self.assertIn("./mvnw -B", (ROOT / ".github/workflows/verify.yml").read_text(encoding="utf-8"))

    def test_readme_coordinates_match_published_pom(self):
        pom = ET.parse(ROOT / "pom.xml").getroot()
        readme = (ROOT / "README.md").read_text(encoding="utf-8")
        for tag in ("groupId", "artifactId", "version"):
            value = pom.findtext("m:" + tag, namespaces=NS)
            self.assertIn(f"<{tag}>{value}</{tag}>", readme)
        self.assertIn("<url>https://jitpack.io</url>", readme)

    def test_relative_markdown_links_stay_in_the_standalone_repository(self):
        for doc in ROOT.glob("*.md"):
            for link in re.findall(r"\]\(([^)]+)\)", doc.read_text(encoding="utf-8")):
                if "://" in link or link.startswith("#"):
                    continue
                self.assertTrue((doc.parent / link.split("#", 1)[0]).is_file(), f"{doc.name}: {link}")
