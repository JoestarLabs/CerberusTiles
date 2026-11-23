#!/usr/bin/env python3
"""
Generate GitHub Actions Step Summary and optional Shields badge artifacts from Kover XML reports.
Standard library only: requires no external dependencies.
"""

import argparse
import json
import os
import sys
import xml.etree.ElementTree as ET
from xml.sax.saxutils import escape


def get_color(pct: float) -> str:
    if pct >= 80.0:
        return "brightgreen"
    elif pct >= 60.0:
        return "green"
    elif pct >= 40.0:
        return "yellowgreen"
    elif pct >= 20.0:
        return "yellow"
    else:
        return "red"


def get_status_emoji(pct: float) -> str:
    if pct >= 80.0:
        return "🟢"
    elif pct >= 50.0:
        return "🟡"
    else:
        return "🔴"


def generate_svg_badge(label: str, message: str, color_hex: str) -> str:
    label_width = len(label) * 7 + 10
    value_width = len(message) * 7 + 10
    total_width = label_width + value_width
    safe_label = escape(label)
    safe_message = escape(message)

    return f"""<svg xmlns="http://www.w3.org/2000/svg" width="{total_width}" height="20" role="img" aria-label="{safe_label}: {safe_message}">
  <linearGradient id="s" x2="0" y2="100%">
    <stop offset="0" stop-color="#bbb" stop-opacity=".1"/>
    <stop offset="1" stop-opacity=".1"/>
  </linearGradient>
  <clipPath id="r">
    <rect width="{total_width}" height="20" rx="3" fill="#fff"/>
  </clipPath>
  <g clip-path="url(#r)">
    <rect width="{label_width}" height="20" fill="#555"/>
    <rect x="{label_width}" width="{value_width}" height="20" fill="{color_hex}"/>
    <rect width="{total_width}" height="20" fill="url(#s)"/>
  </g>
  <g fill="#fff" text-anchor="middle" font-family="Verdana,Geneva,DejaVu Sans,sans-serif" text-rendering="geometricPrecision" font-size="110">
    <text aria-hidden="true" x="{label_width * 5}" y="150" fill="#010101" fill-opacity=".3" transform="scale(.1)">{safe_label}</text>
    <text x="{label_width * 5}" y="140" transform="scale(.1)">{safe_label}</text>
    <text aria-hidden="true" x="{(label_width + value_width / 2) * 10}" y="150" fill="#010101" fill-opacity=".3" transform="scale(.1)">{safe_message}</text>
    <text x="{(label_width + value_width / 2) * 10}" y="140" transform="scale(.1)">{safe_message}</text>
  </g>
</svg>"""


def parse_counters(element: ET.Element) -> dict:
    counters = {}
    for counter in element.findall("counter"):
        counter_type = counter.get("type")
        if not counter_type:
            continue
        missed = int(counter.get("missed", 0))
        covered = int(counter.get("covered", 0))
        total = missed + covered
        pct = (covered / total * 100.0) if total > 0 else 0.0
        counters[counter_type] = {
            "covered": covered,
            "missed": missed,
            "total": total,
            "pct": pct,
        }
    return counters


def categorize_package(pkg_name: str) -> str:
    if pkg_name.endswith(".ui.theme") or ".ui.theme." in pkg_name:
        return "Compose Theme"
    elif pkg_name.endswith(".ui") or ".ui." in pkg_name:
        return "Compose UI"
    elif pkg_name.endswith(".activities") or ".activities." in pkg_name:
        return "Android Activities"
    elif pkg_name.endswith(".quicksettingstiles") or ".quicksettingstiles." in pkg_name:
        return "Tile Services"
    elif pkg_name.endswith(".smartpixels") or ".smartpixels." in pkg_name:
        return "Smart Pixels"
    elif pkg_name.endswith(".models") or ".models." in pkg_name:
        return "Data Models"
    elif pkg_name.endswith(".data") or ".data." in pkg_name:
        return "Data & Docs"
    elif pkg_name.endswith(".util") or ".util." in pkg_name:
        return "Utilities & ViewModel"
    else:
        return "Core & Shortcuts"


def main():
    parser = argparse.ArgumentParser(description="Parse Kover XML and emit step summary & badge artifacts.")
    parser.add_argument(
        "--xml",
        default="app/build/reports/kover/report.xml",
        help="Path to Kover report.xml",
    )
    parser.add_argument(
        "--json-out",
        default=None,
        help="Path to write Shields.io endpoint JSON",
    )
    parser.add_argument(
        "--svg-out",
        default=None,
        help="Path to write SVG badge",
    )
    args = parser.parse_args()

    if not os.path.exists(args.xml):
        msg = f"### ⚠️ Kover XML report not found at `{args.xml}`\n"
        step_summary_file = os.environ.get("GITHUB_STEP_SUMMARY")
        if step_summary_file:
            with open(step_summary_file, "a", encoding="utf-8") as f:
                f.write(msg)
        else:
            print(msg)
        sys.exit(0)

    try:
        tree = ET.parse(args.xml)
        root = tree.getroot()
    except (ET.ParseError, OSError) as e:
        print(f"Error parsing {args.xml}: {e}", file=sys.stderr)
        sys.exit(1)

    counters = parse_counters(root)
    empty_counter = {"pct": 0.0, "covered": 0, "total": 0}
    line_cov = counters.get("LINE", empty_counter)
    branch_cov = counters.get("BRANCH", empty_counter)
    inst_cov = counters.get("INSTRUCTION", empty_counter)
    method_cov = counters.get("METHOD", empty_counter)
    class_cov = counters.get("CLASS", empty_counter)

    line_pct = line_cov["pct"]
    color_name = get_color(line_pct)
    color_hex_map = {
        "brightgreen": "#4c1",
        "green": "#97ca00",
        "yellowgreen": "#a4a61d",
        "yellow": "#dfb317",
        "red": "#e05d44",
    }
    color_hex = color_hex_map.get(color_name, "#e05d44")

    if args.json_out:
        os.makedirs(os.path.dirname(os.path.abspath(args.json_out)), exist_ok=True)
        shield_data = {
            "schemaVersion": 1,
            "label": "coverage",
            "message": f"{line_pct:.1f}%",
            "color": color_name,
        }
        with open(args.json_out, "w", encoding="utf-8") as f:
            json.dump(shield_data, f, indent=2)

    if args.svg_out:
        os.makedirs(os.path.dirname(os.path.abspath(args.svg_out)), exist_ok=True)
        svg_content = generate_svg_badge("coverage", f"{line_pct:.1f}%", color_hex)
        with open(args.svg_out, "w", encoding="utf-8") as f:
            f.write(svg_content)

    packages = []
    logic_covered, logic_total = 0, 0
    non_ui_covered, non_ui_total = 0, 0
    ui_total = 0

    for pkg in root.findall("package"):
        name = pkg.get("name", "").replace("/", ".")
        cat = categorize_package(name)
        pkg_counters = parse_counters(pkg)
        if "LINE" in pkg_counters:
            cov = pkg_counters["LINE"]["covered"]
            tot = pkg_counters["LINE"]["total"]
            pct = pkg_counters["LINE"]["pct"]
            packages.append((name, cat, pct, cov, tot))

            if "Compose" in cat:
                ui_total += tot
            else:
                non_ui_covered += cov
                non_ui_total += tot
            if cat in ("Tile Services", "Smart Pixels", "Data Models", "Data & Docs", "Core & Shortcuts", "Utilities & ViewModel"):
                logic_covered += cov
                logic_total += tot

    packages.sort(key=lambda x: x[2], reverse=True)

    logic_pct = (logic_covered / logic_total * 100.0) if logic_total > 0 else 0.0
    non_ui_pct = (non_ui_covered / non_ui_total * 100.0) if non_ui_total > 0 else 0.0
    total_lines = line_cov["total"]
    ui_share_pct = round(ui_total / total_lines * 100) if total_lines > 0 else 0

    summary_lines = [
        "## 📊 Kover Code Coverage Summary\n",
        "| Scope | Line Coverage | Covered / Total Lines | Status |",
        "| :--- | :---: | :---: | :---: |",
        f"| 🧠 **Core Logic & Services** | **{logic_pct:.1f}%** | {logic_covered:,} / {logic_total:,} | {get_status_emoji(logic_pct)} |",
        f"| ⚙️ **All Non-UI Code** *(Logic + Activities)* | **{non_ui_pct:.1f}%** | {non_ui_covered:,} / {non_ui_total:,} | {get_status_emoji(non_ui_pct)} |",
        f"| 🌐 **Total Codebase** *(Includes Compose UI)* | **{line_pct:.1f}%** | {line_cov['covered']:,} / {line_cov['total']:,} | {get_status_emoji(line_pct)} |",
        f"\n> ℹ️ *Compose UI & Theme composables account for {ui_total:,} lines (~{ui_share_pct}% of the codebase) and are tested via instrumented tests.*\n",
        "<details><summary><b>Detailed Metrics by Counter Type</b></summary>\n",
        "| Metric | Covered | Total | Coverage % | Status |",
        "| :--- | :---: | :---: | :---: | :---: |",
        f"| **Lines** | {line_cov['covered']:,} | {line_cov['total']:,} | **{line_pct:.1f}%** | {get_status_emoji(line_pct)} |",
        f"| **Branches** | {branch_cov['covered']:,} | {branch_cov['total']:,} | **{branch_cov['pct']:.1f}%** | {get_status_emoji(branch_cov['pct'])} |",
        f"| **Instructions** | {inst_cov['covered']:,} | {inst_cov['total']:,} | **{inst_cov['pct']:.1f}%** | {get_status_emoji(inst_cov['pct'])} |",
        f"| **Methods** | {method_cov['covered']:,} | {method_cov['total']:,} | **{method_cov['pct']:.1f}%** | {get_status_emoji(method_cov['pct'])} |",
        f"| **Classes** | {class_cov['covered']:,} | {class_cov['total']:,} | **{class_cov['pct']:.1f}%** | {get_status_emoji(class_cov['pct'])} |",
        "\n</details>\n",
        "<details open><summary><b>📦 Package Breakdown</b></summary>\n",
        "| Package | Layer / Category | Line Coverage | Covered / Total |",
        "| :--- | :--- | :---: | :---: |",
    ]

    for name, cat, pct, cov, tot in packages:
        summary_lines.append(f"| `{name}` | *{cat}* | **{pct:.1f}%** | {cov:,} / {tot:,} |")

    summary_lines.append("\n</details>\n")
    markdown_output = "\n".join(summary_lines)

    step_summary_file = os.environ.get("GITHUB_STEP_SUMMARY")
    if step_summary_file:
        with open(step_summary_file, "a", encoding="utf-8") as f:
            f.write(markdown_output)
    else:
        print(markdown_output)


if __name__ == "__main__":
    main()
