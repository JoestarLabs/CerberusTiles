#!/usr/bin/env python3
"""Unit tests for scripts/coverage_summary.py."""

import json
import os
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path

SCRIPT_PATH = Path(__file__).resolve().parent / "coverage_summary.py"

SAMPLE_KOVER_XML = """<?xml version="1.0" ?>
<report name="Intellij Coverage Report">
  <package name="com/bl4ckswordsman/cerberustiles">
    <counter type="INSTRUCTION" missed="10" covered="90"/>
    <counter type="BRANCH" missed="2" covered="8"/>
    <counter type="LINE" missed="5" covered="45"/>
    <counter type="METHOD" missed="1" covered="9"/>
    <counter type="CLASS" missed="0" covered="3"/>
  </package>
  <package name="com/bl4ckswordsman/cerberustiles/ui">
    <counter type="LINE" missed="80" covered="20"/>
  </package>
  <package name="com/bl4ckswordsman/cerberustiles/ui/theme">
    <counter type="LINE" missed="10" covered="0"/>
  </package>
  <package name="com/bl4ckswordsman/cerberustiles/activities">
    <counter type="LINE" missed="5" covered="15"/>
  </package>
  <counter type="INSTRUCTION" missed="10" covered="90"/>
  <counter type="BRANCH" missed="2" covered="8"/>
  <counter type="LINE" missed="100" covered="80"/>
  <counter type="METHOD" missed="1" covered="9"/>
  <counter type="CLASS" missed="0" covered="3"/>
</report>
"""


class CoverageSummaryTest(unittest.TestCase):
    def test_missing_xml_exits_zero(self):
        with tempfile.TemporaryDirectory() as tmp:
            missing = os.path.join(tmp, "missing.xml")
            env = os.environ.copy()
            env.pop("GITHUB_STEP_SUMMARY", None)
            proc = subprocess.run(
                [sys.executable, str(SCRIPT_PATH), "--xml", missing],
                env=env,
                capture_output=True,
                text=True,
                check=False,
            )
            self.assertEqual(proc.returncode, 0)
            self.assertIn("not found", proc.stdout)

    def test_generates_json_svg_and_dynamic_summary(self):
        with tempfile.TemporaryDirectory() as tmp:
            xml_path = os.path.join(tmp, "report.xml")
            json_path = os.path.join(tmp, "badge.json")
            svg_path = os.path.join(tmp, "badge.svg")
            summary_path = os.path.join(tmp, "summary.md")

            with open(xml_path, "w", encoding="utf-8") as f:
                f.write(SAMPLE_KOVER_XML)

            env = os.environ.copy()
            env["GITHUB_STEP_SUMMARY"] = summary_path

            proc = subprocess.run(
                [
                    sys.executable,
                    str(SCRIPT_PATH),
                    "--xml",
                    xml_path,
                    "--json-out",
                    json_path,
                    "--svg-out",
                    svg_path,
                ],
                env=env,
                capture_output=True,
                text=True,
                check=False,
            )
            self.assertEqual(proc.returncode, 0, proc.stderr)

            with open(json_path, encoding="utf-8") as f:
                badge = json.load(f)
            self.assertEqual(badge["schemaVersion"], 1)
            self.assertEqual(badge["label"], "coverage")
            self.assertEqual(badge["message"], "44.4%")
            self.assertEqual(badge["color"], "yellowgreen")

            with open(svg_path, encoding="utf-8") as f:
                svg = f.read()
            self.assertIn("<svg", svg)
            self.assertIn("44.4%", svg)

            with open(summary_path, encoding="utf-8") as f:
                summary = f.read()
            self.assertIn("110 lines (~61% of the codebase)", summary)
            self.assertIn("90.0%", summary)  # Core logic: 45/50
            self.assertIn("85.7%", summary)  # Non-UI: 60/70


if __name__ == "__main__":
    unittest.main()
