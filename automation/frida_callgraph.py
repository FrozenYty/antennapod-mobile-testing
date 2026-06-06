#!/usr/bin/env python3
"""
Dynamic Call Graph Builder using Frida Runtime Tracing for AntennaPod.

Unlike callgraph.py (static DEX analysis via Androguard), this script uses
Frida's dynamic instrumentation to trace actual method call edges at runtime
as the user interacts with the app. This yields execution-driven call graphs
that reflect real user flows, not just possible code paths.

Workflow:
  1. Launch AntennaPod on a connected Android device
  2. Spawn frida CLI with frida_hooks_cli.js to hook AntennaPod methods
  3. Interact with the app via ADB while tracing, parse the frida -o log file
  4. Aggregate weighted edges and generate HTML report + charts

Requirements:
    pip install -r automation/requirements.txt
    # frida-server must be running on the Android device (see README)

Usage:
    # Default: spawn the app, trace for 30s
    python automation/frida_callgraph.py

    # Custom trace duration & package
    python automation/frida_callgraph.py --duration 60 --package de.danoeh.antennapod.debug

    # Attach to already-running app
    python automation/frida_callgraph.py --attach

    # Specify custom ADB device serial
    python automation/frida_callgraph.py --device 127.0.0.1:7555

Author: Tianyu Yao
"""

import argparse
import json
import os
import subprocess
import sys
import time
import webbrowser
from collections import defaultdict
from datetime import datetime
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parent.parent
HOOK_SCRIPT = Path(__file__).resolve().parent / "frida_hooks_cli.js"
DEFAULT_OUTPUT = PROJECT_ROOT / "test-docs" / "callgraphs"

# ── matplotlib setup ────────────────────────────────────────────────────

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt


# ── ADB helpers ─────────────────────────────────────────────────────────

def adb(args: list[str], device: Optional[str] = None) -> str:
    """Run an ADB command and return stdout."""
    cmd = ["adb"]
    if device:
        cmd.extend(["-s", device])
    cmd.extend(args)
    result = subprocess.run(cmd, capture_output=True, text=True)
    if result.returncode != 0 and result.stderr:
        print(f"  [adb warn] {' '.join(cmd)}: {result.stderr.strip()}")
    return result.stdout.strip()


def get_device_serial() -> str:
    """Auto-detect connected Android device. Error if none or multiple."""
    out = adb(["devices"])
    lines = [l for l in out.splitlines() if l and "List of devices" not in l]
    devices = [l.split()[0] for l in lines if "offline" not in l]
    if not devices:
        sys.exit("ERROR: No Android device connected. Run `adb devices`.")
    if len(devices) > 1:
        print(f"  Multiple devices: {devices}")
        print(f"  Using first: {devices[0]}")
        print(f"  (Use --device to specify)")
    return devices[0]


def install_and_launch(apk_path: str, package: str, device: str) -> bool:
    """Install (if needed) and launch the APK on the device."""
    print(f"\n── ADB: Device setup ──")
    print(f"  Device: {device}")
    print(f"  Package: {package}")

    # Check if already installed
    installed = adb(["shell", "pm", "list", "packages", package], device)
    if package not in installed:
        print(f"  Installing APK: {apk_path}")
        result = subprocess.run(
            ["adb", "-s", device, "install", "-r", apk_path],
            capture_output=True, text=True,
        )
        if "Success" not in result.stdout:
            sys.exit(f"ERROR: APK install failed:\n{result.stdout}\n{result.stderr}")
        print("  Install: OK")
    else:
        print("  App already installed")

    # Stop the app first (clean state)
    adb(["shell", "am", "force-stop", package], device)
    time.sleep(1)

    # Launch using am start (keeps app running, unlike monkey)
    print(f"  Launching {package}...")
    out = adb([
        "shell", "am", "start", "-n",
        f"{package}/de.danoeh.antennapod.activity.MainActivity",
    ], device)
    time.sleep(4)
    print("  Launch: OK")
    return True


# ── Frida trace engine ──────────────────────────────────────────────────

class FridaCallGraphTracer:
    """Manages the Frida session, collects edges, and builds the call graph."""

    def __init__(self, package: str, device_serial: str, duration: int,
                 output_dir: Path, class_filter: str = "de.danoeh.antennapod"):
        self.package = package
        self.device_serial = device_serial
        self.duration = duration
        self.output_dir = output_dir
        self.class_filter = class_filter

        self.edges: dict[tuple[str, str], int] = defaultdict(int)
        self.total_edges = 0
        self.classes_hooked = 0
        self.methods_hooked = 0
        self.start_time: float = 0.0

    # ── Run trace session ───────────────────────────────────────────

    def run(self) -> bool:
        """Run frida CLI subprocess with -q -t, parse stdout JSON lines."""
        script_path = str(HOOK_SCRIPT)
        if not os.path.exists(script_path):
            sys.exit(f"ERROR: Hook script not found: {script_path}")

        print(f"\n── Frida CLI: Dynamic Call Graph Tracing ──")
        print(f"  Hook script: {os.path.relpath(script_path, PROJECT_ROOT)}")
        print(f"  Trace duration: {self.duration}s")
        print(f"  Class filter: {self.class_filter}.*")

        timeout_sec = self.duration + 8  # extra for app startup + bridge

        # Use frida's -o flag to write output to a temp file (avoids pipe buffering)
        log_file = self.output_dir / "frida_trace_output.txt"
        cmd = [
            "frida", "-U",
            "-f", self.package,
            "--runtime=v8",
            "-l", script_path,
            "-q",                        # quiet mode (no REPL)
            "-t", str(timeout_sec),      # keep running N seconds then exit
            "-o", str(log_file),         # write console output to file
        ]
        print(f"  Spawning: {' '.join(cmd)}")
        print(f"  Log file: {os.path.relpath(str(log_file), PROJECT_ROOT)}")

        self.start_time = time.time()
        print(f"\n  Tracing... (interacting via ADB)")
        print(f"  {'─' * 40}")

        try:
            proc = subprocess.Popen(
                cmd,
                stdin=subprocess.DEVNULL,
                stdout=subprocess.PIPE,
                stderr=subprocess.STDOUT,
                text=True,
                encoding="utf-8",
                errors="replace",
            )
        except FileNotFoundError:
            sys.exit("ERROR: 'frida' CLI not found. Install: pip install frida-tools")

        # Interact with the app while tracing (simulate user input)
        _interact_with_app(self.device_serial, self.duration)

        # Wait for frida to exit
        proc.wait()

        # Read the log file written by frida -o
        if log_file.exists():
            raw = log_file.read_text(encoding="utf-8", errors="replace")
            for line in raw.splitlines():
                line = line.strip()
                if line:
                    self._parse_cli_line(line)
            # Keep for debugging, remove old ones
            # log_file.unlink()
        else:
            print(f"  WARNING: Log file not found: {log_file}")
            # Try reading from stdout pipe as fallback
            stdout_data = proc.stdout.read() if proc.stdout else ""
            for line in stdout_data.splitlines():
                line = line.strip()
                if line:
                    self._parse_cli_line(line)

        print(f"\n  {'─' * 40}")
        print(f"  Parsing log file...")

        # Stop the app
        adb(["shell", "am", "force-stop", self.package], self.device_serial)

        elapsed = time.time() - self.start_time
        print(f"\n  Tracing complete: {elapsed:.1f}s")
        print(f"  Classes hooked: {self.classes_hooked}")
        print(f"  Methods hooked: {self.methods_hooked}")
        print(f"  Unique edges:   {len(self.edges):,}")
        print(f"  Total calls:    {self.total_edges:,}")

        return len(self.edges) > 0

    def _parse_cli_line(self, line: str) -> None:
        """Parse a JSON line from frida -o log file."""
        if not line or len(line) < 2:
            return
        if line.startswith("[") and "]->" in line:
            return
        if line.startswith("____") or line.startswith("|") or line.startswith("Spawn"):
            return
        try:
            data = json.loads(line)
        except json.JSONDecodeError:
            return
        msg_type = data.get("type", "")
        if msg_type == "init":
            print(f"  [frida] {data.get('msg', '')}")
        elif msg_type == "debug":
            print(f"  [debug] {data.get('msg', '')}")
        elif msg_type == "error":
            print(f"  [error] {data.get('msg', '')}")
        elif msg_type == "status":
            self.classes_hooked = data.get("classes", self.classes_hooked)
            self.methods_hooked = data.get("hooks", self.methods_hooked)
            nh = data.get("newHooks", 0)
            tc = data.get("totalClasses", 0)
            print(f"  [status] {self.classes_hooked} classes hooked, "
                  f"{self.methods_hooked} methods, +{nh} new "
                  f"(scanned {tc} total classes)")
        elif msg_type == "edges":
            for edge in data.get("payload", []):
                caller = edge.get("caller", "unknown")
                callee = edge.get("callee", "unknown")
                if caller and callee:
                    self.edges[(caller, callee)] += 1
                    self.total_edges += 1

    # ── Analysis ────────────────────────────────────────────────────

    def get_call_graph(self) -> dict[str, Any]:
        """Build a structured call graph summary from collected edges."""
        caller_weights: dict[str, int] = defaultdict(int)
        callee_weights: dict[str, int] = defaultdict(int)
        pkg_edges: dict[tuple[str, str], int] = defaultdict(int)

        for (caller, callee), weight in self.edges.items():
            caller_weights[caller] += weight
            callee_weights[callee] += weight

            # Package-level aggregation
            src_pkg = _extract_top_pkg(caller, self.class_filter)
            tgt_pkg = _extract_top_pkg(callee, self.class_filter)
            if src_pkg and tgt_pkg and src_pkg != tgt_pkg:
                pkg_edges[(src_pkg, tgt_pkg)] += weight

        top_callers = sorted(caller_weights.items(), key=lambda x: -x[1])[:20]
        top_callees = sorted(callee_weights.items(), key=lambda x: -x[1])[:20]

        return {
            "edges": dict(self.edges),
            "unique_edges": len(self.edges),
            "total_calls": self.total_edges,
            "classes_hooked": self.classes_hooked,
            "methods_hooked": self.methods_hooked,
            "top_callers": top_callers,
            "top_callees": top_callees,
            "pkg_edges": dict(pkg_edges),
            "package": self.package,
            "class_filter": self.class_filter,
        }

    def export_json(self, data: dict) -> Path:
        """Save call graph data as JSON."""
        out = self.output_dir / "frida-callgraph.json"
        serializable = {
            "meta": {
                "package": data["package"],
                "class_filter": data["class_filter"],
                "unique_edges": data["unique_edges"],
                "total_calls": data["total_calls"],
                "classes_hooked": data["classes_hooked"],
                "methods_hooked": data["methods_hooked"],
                "generated": datetime.now().isoformat(),
            },
            "top_callers": [{"class": c, "calls": n} for c, n in data["top_callers"]],
            "top_callees": [{"class": c, "calls": n} for c, n in data["top_callees"]],
            "edges": [
                {"caller": c, "callee": t, "weight": w}
                for (c, t), w in sorted(
                    data["edges"].items(), key=lambda x: -x[1]
                )[:500]
            ],
        }
        with open(out, "w", encoding="utf-8") as f:
            json.dump(serializable, f, indent=2, ensure_ascii=False)
        print(f"  JSON exported: {os.path.relpath(out, PROJECT_ROOT)} "
              f"({out.stat().st_size // 1024} KB)")
        return out


# ── Visualization ────────────────────────────────────────────────────────

def _extract_top_pkg(full_method: str, base_pkg: str) -> str:
    """Extract top-2-level package from a 'pkg.Class.method' string."""
    # full_method looks like "de.danoeh.antennapod.activity.MainActivity.onCreate"
    parts = full_method.split(".")
    # Find base_pkg components
    base_parts = base_pkg.split(".")
    try:
        idx = 0
        for i, p in enumerate(parts):
            if idx < len(base_parts) and p == base_parts[idx]:
                idx += 1
                if idx == len(base_parts):
                    # Return next level
                    if i + 1 < len(parts):
                        return base_pkg + "." + parts[i + 1]
                    return base_pkg
        return ".".join(parts[:3]) if len(parts) >= 3 else base_pkg
    except Exception:
        return base_pkg


def _shorten_class(full_name: str) -> str:
    """Shorten a fully qualified class name for chart labels."""
    parts = full_name.split(".")
    if len(parts) <= 2:
        return full_name
    short = ".".join(p[0] for p in parts[:-2]) + "." + ".".join(parts[-2:])
    return short


def _pkg_short(pkg: str) -> str:
    parts = pkg.split(".")
    if len(parts) <= 3:
        return pkg
    return parts[0][0] + "." + ".".join(parts[2:])


def draw_frida_callers_callees(data: dict, output_dir: Path) -> None:
    """Bar chart of top callers (outgoing) and callees (incoming) from Frida trace."""
    top_callers = data["top_callers"][:15]
    top_callees = data["top_callees"][:15]

    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(16, 8))
    n1, n2 = len(top_callers), len(top_callees)

    # ── Left: Top Callers ──
    names1 = [_shorten_class(n) for n, _ in top_callers]
    colors1 = ["#9673A6" if i == 0 else "#BDBDBD" for i in range(n1)]
    ax1.barh(range(n1), [v for _, v in top_callers], color=colors1,
             edgecolor="black", linewidth=0.5)
    ax1.set_yticks(range(n1))
    ax1.set_yticklabels(names1, fontsize=7)
    ax1.set_xlabel("Outgoing calls (runtime)")
    ax1.set_title("Top 15 Callers (Frida Trace)")
    ax1.invert_yaxis()
    ax1.spines[["top", "right"]].set_visible(False)
    for i, (_, v) in enumerate(top_callers):
        ax1.text(v + max(v * 0.01, 1), i, f"{v:,}", va="center", fontsize=6)

    # ── Right: Top Callees ──
    names2 = [_shorten_class(n) for n, _ in top_callees]
    colors2 = ["#9673A6" if i == 0 else "#BDBDBD" for i in range(n2)]
    ax2.barh(range(n2), [v for _, v in top_callees], color=colors2,
             edgecolor="black", linewidth=0.5)
    ax2.set_yticks(range(n2))
    ax2.set_yticklabels(names2, fontsize=7)
    ax2.set_xlabel("Incoming calls (runtime)")
    ax2.set_title("Top 15 Callees (Frida Trace)")
    ax2.invert_yaxis()
    ax2.spines[["top", "right"]].set_visible(False)
    for i, (_, v) in enumerate(top_callees):
        ax2.text(v + max(v * 0.01, 1), i, f"{v:,}", va="center", fontsize=6)

    fig.suptitle(
        f"Dynamic Call Graph — Frida Runtime Trace\n"
        f"Package: {data['package']} | "
        f"{data['unique_edges']:,} unique edges, {data['total_calls']:,} total calls",
        fontsize=12, fontweight="bold",
    )
    plt.tight_layout()

    for fmt in ["pdf", "png"]:
        out = output_dir / f"frida-callgraph-stats.{fmt}"
        plt.savefig(out, dpi=600 if fmt == "png" else None, bbox_inches="tight")
    plt.close()
    print(f"  Stats chart saved: frida-callgraph-stats.pdf/png")


def draw_frida_package_heatmap(data: dict, output_dir: Path) -> None:
    """Heatmap of package-to-package call density from Frida trace data."""
    pkg_edges_data = data["pkg_edges"]
    if not pkg_edges_data:
        print("  (No cross-package edges — skipping heatmap)")
        return

    packages_set: set[str] = set()
    for src, tgt in pkg_edges_data:
        packages_set.add(src)
        packages_set.add(tgt)

    pkg_weight: dict[str, int] = defaultdict(int)
    for (src, tgt), w in pkg_edges_data.items():
        pkg_weight[src] += w
        pkg_weight[tgt] += w

    top_pkgs = sorted(pkg_weight, key=lambda x: -pkg_weight[x])[:16]
    top_pkgs.sort()

    n = len(top_pkgs)
    if n < 2:
        print("  (Not enough packages for heatmap)")
        return

    matrix = [[0] * n for _ in range(n)]
    for (src, tgt), w in pkg_edges_data.items():
        if src in top_pkgs and tgt in top_pkgs:
            i, j = top_pkgs.index(src), top_pkgs.index(tgt)
            matrix[i][j] = w

    short_names = [_pkg_short(p) for p in top_pkgs]

    fig, ax = plt.subplots(figsize=(14, 12))
    im = ax.imshow(matrix, cmap="YlOrRd", aspect="auto", vmin=0)

    ax.set_xticks(range(n))
    ax.set_xticklabels(short_names, rotation=45, ha="right", fontsize=7)
    ax.set_yticks(range(n))
    ax.set_yticklabels(short_names, fontsize=7)
    ax.set_xlabel("Callee (called by)", fontsize=10)
    ax.set_ylabel("Caller (calls →)", fontsize=10)

    # Annotate cells with significant values
    for i in range(n):
        for j in range(n):
            if matrix[i][j] > 0:
                matrix_max = max(max(row) for row in matrix) if any(any(row) for row in matrix) else 1
                color = "white" if matrix[i][j] > max(3, matrix_max * 0.6) else "black"
                ax.text(j, i, str(matrix[i][j]), ha="center", va="center",
                        fontsize=5, color=color, fontweight="bold")

    plt.colorbar(im, ax=ax, shrink=0.8, label="Call count (runtime)")
    ax.spines[["top", "right"]].set_visible(False)
    plt.title(
        f"Package Interaction Heatmap — Frida Runtime Trace\n"
        f"{data['package']} | Top {n} packages | "
        f"{sum(sum(row) for row in matrix)} cross-package calls",
        fontsize=12, fontweight="bold",
    )
    plt.tight_layout()

    for fmt in ["pdf", "png"]:
        out = output_dir / f"frida-callgraph-heatmap.{fmt}"
        plt.savefig(out, dpi=600 if fmt == "png" else None, bbox_inches="tight")
    plt.close()
    print(f"  Heatmap saved: frida-callgraph-heatmap.pdf/png")


def draw_frida_top_edges_chart(data: dict, output_dir: Path, top_n: int = 20) -> None:
    """Horizontal bar chart of the top individual caller→callee edges."""
    edges = data["edges"]
    if not edges:
        return

    top_edges = sorted(edges.items(), key=lambda x: -x[1])[:top_n]
    if not top_edges:
        return

    labels = [
        f"{_shorten_class(caller).split('.')[-1]} → {_shorten_class(callee).split('.')[-1]}"
        for (caller, callee), _ in top_edges
    ]
    values = [w for _, w in top_edges]
    n = len(labels)

    fig, ax = plt.subplots(figsize=(12, 8))
    colors = ["#9673A6" if i < 3 else ("#6C8EBF" if i < 6 else "#BDBDBD")
              for i in range(n)]
    ax.barh(range(n), values, color=colors, edgecolor="black", linewidth=0.5)
    ax.set_yticks(range(n))
    ax.set_yticklabels(labels, fontsize=7, family="monospace")
    ax.set_xlabel("Call count")
    ax.invert_yaxis()
    ax.spines[["top", "right"]].set_visible(False)
    for i, v in enumerate(values):
        ax.text(v + max(v * 0.01, 1), i, f"{v:,}", va="center", fontsize=6)

    ax.set_title(
        f"Top {top_n} Call Edges — Frida Runtime Trace\n"
        f"{data['package']} | {data['unique_edges']:,} unique edges",
        fontsize=12, fontweight="bold",
    )
    plt.tight_layout()

    for fmt in ["pdf", "png"]:
        out = output_dir / f"frida-callgraph-edges.{fmt}"
        plt.savefig(out, dpi=600 if fmt == "png" else None, bbox_inches="tight")
    plt.close()
    print(f"  Edges chart saved: frida-callgraph-edges.pdf/png")


# ── Interactive HTML Report ──────────────────────────────────────────────

# Sub-package color palette for network graph nodes
_PKG_COLORS = [
    "#9673A6", "#6C8EBF", "#82B366", "#D79B00", "#B85450",
    "#6C8EBF", "#82B366", "#D79B00", "#B85450", "#9673A6",
    "#4DA6A6", "#E87D72", "#7B9E5A", "#C49B6C", "#5B8DB8",
    "#A67C52", "#6B5B8C", "#4E8C6E", "#B56576", "#8B7D6B",
]


def _extract_subpkg(full_class: str, base_pkg: str) -> str:
    """Extract the first sub-package beneath base_pkg for color grouping."""
    # full_class like "de.danoeh.antennapod.activity.MainActivity.onCreate"
    base_parts = base_pkg.split(".")
    parts = full_class.split(".")
    try:
        idx = 0
        for i, p in enumerate(parts):
            if idx < len(base_parts) and p == base_parts[idx]:
                idx += 1
                if idx == len(base_parts) and i + 1 < len(parts):
                    return parts[i + 1]
        return "other"
    except Exception:
        return "other"


def _format_class_label(full_class: str) -> str:
    """Short display name for a full class+method string."""
    parts = full_class.split(".")
    if len(parts) <= 2:
        return full_class
    return parts[-2] + "." + parts[-1]


def generate_html_report(data: dict, output_dir: Path) -> Path:
    """Generate a self-contained interactive HTML report with vis-network and Chart.js.

    Returns the path to the generated HTML file.
    """
    edges = data["edges"]
    top_callers = data["top_callers"][:15]
    top_callees = data["top_callees"][:15]
    class_filter = data["class_filter"]

    # ── Build network data ────────────────────────────────────────────

    # METHOD-level graph: each unique full method (class.method) is a node.
    method_degree: dict[str, int] = defaultdict(int)
    for (caller, callee), weight in edges.items():
        method_degree[caller] += weight
        method_degree[callee] += weight

    # Take top 60 methods for readability
    top_methods = sorted(method_degree.items(), key=lambda x: -x[1])[:60]
    top_method_set = {m for m, _ in top_methods}

    node_list = []
    node_index: dict[str, int] = {}
    subpkg_set: set[str] = set()
    max_degree = max(method_degree.values()) if method_degree else 1

    for i, (method_full, degree) in enumerate(top_methods):
        subpkg = _extract_subpkg(method_full, class_filter)
        subpkg_set.add(subpkg)
        node_index[method_full] = i
        short_label = _format_class_label(method_full)
        node_list.append({
            "id": i,
            "label": short_label,
            "title": f"<b>{method_full}</b><br>Total calls: {degree:,}",
            "value": max(5, int(15 + (degree / max_degree) * 55)),
            "group": subpkg,
            "degree": degree,
        })

    edge_list = []
    max_weight = max(edges.values()) if edges else 1
    for (caller, callee), weight in edges.items():
        if caller in node_index and callee in node_index:
            edge_list.append({
                "from": node_index[caller],
                "to": node_index[callee],
                "value": weight,
                "title": f"{_format_class_label(caller)} → {_format_class_label(callee)}<br>Calls: {weight:,}",
                "width": max(1, int(1 + (weight / max_weight) * 8)),
            })

    subpkg_list = sorted(subpkg_set)
    pkg_color_map = {sp: _PKG_COLORS[i % len(_PKG_COLORS)]
                     for i, sp in enumerate(subpkg_list)}
    for node in node_list:
        node["color"] = pkg_color_map.get(node["group"], "#BDBDBD")

    # ── Chart data ────────────────────────────────────────────────────
    caller_labels = [_format_class_label(n) for n, _ in top_callers]
    caller_values = [v for _, v in top_callers]
    callee_labels = [_format_class_label(n) for n, _ in top_callees]
    callee_values = [v for _, v in top_callees]

    # Top edges for table
    top_edges_for_table = sorted(edges.items(), key=lambda x: -x[1])[:200]
    edge_rows = [
        {"caller": c, "callee": t, "weight": w}
        for (c, t), w in top_edges_for_table
    ]

    # ── Build HTML ────────────────────────────────────────────────────
    html_data = {
        "nodes": node_list,
        "edges": edge_list,
        "subpkgGroups": sorted(subpkg_list),
        "pkgColorMap": pkg_color_map,
        "callerLabels": caller_labels,
        "callerValues": caller_values,
        "calleeLabels": callee_labels,
        "calleeValues": callee_values,
        "edgeTableRows": edge_rows,
        "meta": {
            "package": data["package"],
            "classFilter": class_filter,
            "uniqueEdges": data["unique_edges"],
            "totalCalls": data["total_calls"],
            "classesHooked": data["classes_hooked"],
            "methodsHooked": data["methods_hooked"],
            "networkNodes": len(node_list),
            "networkEdges": len(edge_list),
            "generated": datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
        },
    }

    html = _build_html_page(html_data)

    out = output_dir / "frida-callgraph.html"
    with open(out, "w", encoding="utf-8") as f:
        f.write(html)
    rel = os.path.relpath(str(out), PROJECT_ROOT)
    size_kb = out.stat().st_size // 1024
    print(f"  HTML report saved: {rel} ({size_kb} KB)")

    return out


def _build_html_page(d: dict) -> str:
    """Render the complete HTML page with embedded JSON data."""
    json_data = json.dumps(d, ensure_ascii=False)

    return f"""<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Frida Call Graph — {d['meta']['package']}</title>
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/vis/4.21.0/vis.min.css">
<script src="https://cdnjs.cloudflare.com/ajax/libs/vis/4.21.0/vis.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/Chart.js/4.4.0/chart.umd.min.js"></script>
<style>
* {{ margin: 0; padding: 0; box-sizing: border-box; }}
body {{ font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background: #f5f5f5; color: #333; }}
.header {{ background: linear-gradient(135deg, #9673A6 0%, #6C8EBF 100%); color: white; padding: 24px 32px; }}
.header h1 {{ font-size: 1.5em; margin-bottom: 4px; }}
.header p {{ opacity: 0.85; font-size: 0.9em; }}
.stats {{ display: flex; flex-wrap: wrap; gap: 12px; padding: 20px 32px; background: white; border-bottom: 1px solid #e0e0e0; }}
.stat-card {{ flex: 1; min-width: 120px; text-align: center; padding: 12px 16px; background: #f9f9f9; border-radius: 8px; border: 1px solid #eee; }}
.stat-card .value {{ font-size: 1.4em; font-weight: 700; color: #9673A6; }}
.stat-card .label {{ font-size: 0.75em; color: #888; margin-top: 2px; text-transform: uppercase; letter-spacing: 0.5px; }}
.tabs {{ display: flex; background: white; padding: 0 32px; border-bottom: 1px solid #e0e0e0; }}
.tab {{ padding: 12px 20px; cursor: pointer; font-size: 0.9em; color: #888; border-bottom: 2px solid transparent; transition: all 0.2s; }}
.tab:hover {{ color: #9673A6; }}
.tab.active {{ color: #9673A6; border-bottom-color: #9673A6; font-weight: 600; }}
.panel {{ display: none; }}
.panel.active {{ display: block; }}
.content {{ max-width: 1400px; margin: 0 auto; padding: 24px 32px; }}
#network {{ width: 100%; height: 650px; border: 1px solid #e0e0e0; border-radius: 8px; background: white; }}
#stats-charts {{ display: grid; grid-template-columns: 1fr 1fr; gap: 24px; }}
#stats-charts canvas {{ background: white; border: 1px solid #e0e0e0; border-radius: 8px; min-height: 380px; padding: 12px; }}
#stats-charts > div {{ min-height: 400px; }}
#search-box {{ margin-bottom: 12px; padding: 8px 14px; width: 100%; max-width: 400px; border: 1px solid #ddd; border-radius: 6px; font-size: 0.9em; }}
#search-box:focus {{ outline: none; border-color: #9673A6; box-shadow: 0 0 0 2px rgba(150,115,166,0.15); }}
.edge-table {{ width: 100%; border-collapse: collapse; font-size: 0.85em; background: white; border-radius: 8px; overflow: hidden; border: 1px solid #e0e0e0; }}
.edge-table th {{ background: #9673A6; color: white; padding: 10px 14px; text-align: left; font-weight: 600; cursor: pointer; user-select: none; }}
.edge-table th:hover {{ background: #866096; }}
.edge-table td {{ padding: 8px 14px; border-bottom: 1px solid #f0f0f0; font-family: 'Courier New', monospace; font-size: 0.82em; }}
.edge-table tr:hover td {{ background: #f9f5fb; }}
.edge-table .weight {{ text-align: right; font-weight: 600; color: #9673A6; }}
.table-wrap {{ max-height: 600px; overflow-y: auto; border-radius: 8px; }}
.legend {{ display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 12px; }}
.legend-item {{ display: flex; align-items: center; gap: 4px; font-size: 0.75em; padding: 3px 8px; background: white; border-radius: 4px; border: 1px solid #eee; }}
.legend-swatch {{ width: 10px; height: 10px; border-radius: 50%; }}
.toolbar {{ display: flex; gap: 8px; align-items: center; margin-bottom: 12px; flex-wrap: wrap; }}
.btn {{ padding: 6px 14px; border: 1px solid #ddd; border-radius: 6px; background: white; cursor: pointer; font-size: 0.82em; transition: all 0.15s; }}
.btn:hover {{ border-color: #9673A6; color: #9673A6; }}
.footer {{ text-align: center; padding: 24px; color: #aaa; font-size: 0.8em; }}
</style>
</head>
<body>

<div class="header">
  <h1>🔬 Frida Dynamic Call Graph</h1>
  <p>{d['meta']['package']} · Generated {d['meta']['generated']}</p>
</div>

<div class="stats">
  <div class="stat-card"><div class="value">{d['meta']['totalCalls']:,}</div><div class="label">Total Calls</div></div>
  <div class="stat-card"><div class="value">{d['meta']['uniqueEdges']:,}</div><div class="label">Unique Edges</div></div>
  <div class="stat-card"><div class="value">{d['meta']['classesHooked']}</div><div class="label">Classes Hooked</div></div>
  <div class="stat-card"><div class="value">{d['meta']['methodsHooked']}</div><div class="label">Methods Hooked</div></div>
  <div class="stat-card"><div class="value">{d['meta']['networkNodes']}</div><div class="label">Graph Nodes</div></div>
  <div class="stat-card"><div class="value">{d['meta']['networkEdges']}</div><div class="label">Graph Edges</div></div>
</div>

<div class="tabs">
  <div class="tab active" onclick="switchTab('network')">🕸 Network Graph</div>
  <div class="tab" onclick="switchTab('charts')">📊 Stats Charts</div>
  <div class="tab" onclick="switchTab('table')">📋 Edge Table</div>
</div>

<div class="content">

<!-- Network Panel -->
<div id="panel-network" class="panel active">
  <div class="toolbar">
    <input id="search-box" type="text" placeholder="🔍 Search class (e.g. MainActivity)..." oninput="searchNode()">
    <button class="btn" onclick="network.fit()">Fit View</button>
    <button class="btn" onclick="network.stabilize()">Re-stabilize</button>
    <button class="btn" onclick="togglePhysics()">⏯ Physics</button>
  </div>
  <div class="legend" id="legend"></div>
  <div id="network"></div>
</div>

<!-- Charts Panel -->
<div id="panel-charts" class="panel">
  <div id="stats-charts">
    <div><canvas id="chart-callers"></canvas></div>
    <div><canvas id="chart-callees"></canvas></div>
  </div>
</div>

<!-- Table Panel -->
<div id="panel-table" class="panel">
  <input id="table-search" type="text" placeholder="🔍 Filter edges..." oninput="filterTable()" style="margin-bottom:12px;padding:8px 14px;width:100%;max-width:400px;border:1px solid #ddd;border-radius:6px;font-size:0.9em;">
  <div class="table-wrap">
    <table class="edge-table">
      <thead>
        <tr>
          <th onclick="sortTable(0)">#</th>
          <th onclick="sortTable(1)">Caller</th>
          <th onclick="sortTable(2)">Callee</th>
          <th onclick="sortTable(3)">Weight ▼</th>
        </tr>
      </thead>
      <tbody id="edge-tbody"></tbody>
    </table>
  </div>
</div>

</div>

<div class="footer">Frida Dynamic Call Graph · AntennaPod Mobile Testing · <a href="https://github.com/FrozenYty/antennapod-mobile-testing">GitHub</a></div>

<script>
const DATA = {json_data};

// ── Tabs ──────────────────────────────────────────────────────────
function switchTab(name) {{
  document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
  document.querySelectorAll('.panel').forEach(p => p.classList.remove('active'));
  document.querySelector(`.tab:nth-child(${{name === 'network' ? 1 : name === 'charts' ? 2 : 3}})`).classList.add('active');
  document.getElementById('panel-' + name).classList.add('active');
  if (name === 'network') setTimeout(() => network.fit(), 100);
  if (name === 'charts') setTimeout(initCharts, 50);  // deferred: canvas must be visible
}}

// ── Legend ────────────────────────────────────────────────────────
(function buildLegend() {{
  const groups = DATA.subpkgGroups;
  const map = DATA.pkgColorMap;
  const container = document.getElementById('legend');
  groups.forEach(g => {{
    const item = document.createElement('span');
    item.className = 'legend-item';
    item.innerHTML = `<span class="legend-swatch" style="background:${{map[g]}}"></span>${{g}}`;
    container.appendChild(item);
  }});
}})();

// ── Network Graph ─────────────────────────────────────────────────
const nodes = new vis.DataSet(DATA.nodes);
const edges = new vis.DataSet(DATA.edges);
const networkEl = document.getElementById('network');

const network = new vis.Network(networkEl, {{ nodes, edges }}, {{
  physics: {{ solver: 'barnesHut', barnesHut: {{ gravitationalConstant: -3000, centralGravity: 0.3, springLength: 120 }} }},
  edges: {{ arrows: 'to', smooth: {{ type: 'continuous' }}, color: {{ opacity: 0.4 }} }},
  interaction: {{ hover: true, tooltipDelay: 150, zoomView: true, dragView: true }},
  layout: {{ improvedLayout: true }},
}});

let physicsOn = true;
function togglePhysics() {{
  physicsOn = !physicsOn;
  network.setOptions({{ physics: physicsOn }});
  if (physicsOn) network.stabilize();
}}

function searchNode() {{
  const q = document.getElementById('search-box').value.toLowerCase();
  if (!q) {{ nodes.forEach(n => nodes.update({{ id: n.id, opacity: 1 }})); edges.forEach(e => edges.update({{ id: e.id, opacity: 1 }})); return; }}
  const matches = new Set();
  nodes.forEach(n => {{ if (n.label.toLowerCase().includes(q)) matches.add(n.id); }});
  nodes.forEach(n => nodes.update({{ id: n.id, opacity: matches.has(n.id) || matches.size === 0 ? 1 : 0.15 }}));
  edges.forEach(e => {{
    const visible = matches.has(e.from) || matches.has(e.to);
    edges.update({{ id: e.id, opacity: visible ? 1 : 0.05 }});
  }});
}}

// ── Charts (lazy-init on first tab switch) ─────────────────────────
let chartsInited = false;
function initCharts() {{
  if (chartsInited) return;
  chartsInited = true;
  const purple = '#9673A6', gray = '#BDBDBD', blue = '#6C8EBF';
  const font = {{ family: '-apple-system, BlinkMacSystemFont, sans-serif', size: 11 }};

  new Chart(document.getElementById('chart-callers'), {{
    type: 'bar', indexAxis: 'y',
    data: {{
      labels: DATA.callerLabels,
      datasets: [{{
        data: DATA.callerValues,
        backgroundColor: DATA.callerValues.map((_, i) => i === 0 ? purple : (i < 3 ? blue : gray)),
        borderColor: '#333', borderWidth: 0.5,
      }}]
    }},
    options: {{
      responsive: true, maintainAspectRatio: false,
      plugins: {{ title: {{ display: true, text: 'Top 15 Callers (Outgoing)', font: {{ size: 14, weight: 'bold' }} }}, legend: {{ display: false }} }},
      scales: {{ x: {{ title: {{ display: true, text: 'Outgoing calls' }} }}, y: {{ ticks: {{ font }} }} }},
    }}
  }});

  new Chart(document.getElementById('chart-callees'), {{
    type: 'bar', indexAxis: 'y',
    data: {{
      labels: DATA.calleeLabels,
      datasets: [{{
        data: DATA.calleeValues,
        backgroundColor: DATA.calleeValues.map((_, i) => i === 0 ? purple : (i < 3 ? blue : gray)),
        borderColor: '#333', borderWidth: 0.5,
      }}]
    }},
    options: {{
      responsive: true, maintainAspectRatio: false,
      plugins: {{ title: {{ display: true, text: 'Top 15 Callees (Incoming)', font: {{ size: 14, weight: 'bold' }} }}, legend: {{ display: false }} }},
      scales: {{ x: {{ title: {{ display: true, text: 'Incoming calls' }} }}, y: {{ ticks: {{ font }} }} }},
    }}
  }});
}}

// ── Edge Table ────────────────────────────────────────────────────
let sortDir = [1, 1, 1, -1]; // sort direction per column, weight desc by default
function sortTable(col) {{
  sortDir[col] *= -1;
  const dir = sortDir[col];
  DATA.edgeTableRows.sort((a, b) => {{
    const keys = ['', 'caller', 'callee', 'weight'];
    const va = col === 3 ? a[keys[col]] : a[keys[col]].toLowerCase();
    const vb = col === 3 ? b[keys[col]] : b[keys[col]].toLowerCase();
    return va > vb ? dir : va < vb ? -dir : 0;
  }});
  renderTable();
}}

function filterTable() {{
  renderTable();
}}

function renderTable() {{
  const q = (document.getElementById('table-search')?.value || '').toLowerCase();
  let rows = DATA.edgeTableRows;
  if (q) rows = rows.filter(r => r.caller.toLowerCase().includes(q) || r.callee.toLowerCase().includes(q));
  document.getElementById('edge-tbody').innerHTML = rows.map((r, i) =>
    `<tr><td>${{i + 1}}</td><td>${{r.caller}}</td><td>${{r.callee}}</td><td class="weight">${{r.weight.toLocaleString()}}</td></tr>`
  ).join('');
}}

renderTable();

// Fit network after load
window.addEventListener('load', () => setTimeout(() => network.fit({{ animation: true }}), 500));
</script>
</body>
</html>"""

def _interact_with_app(device_serial: str, duration: int) -> None:
    """Simulate user interaction via ADB while Frida is tracing."""
    nav_items = [
        (108, 1850), (324, 1850), (540, 1850), (756, 1850),
    ]
    start = time.time()
    time.sleep(5)  # wait for app load + hooks setup
    i = 0
    while time.time() - start < duration:
        x, y = nav_items[i % len(nav_items)]
        adb(["shell", "input", "tap", str(x), str(y)], device_serial)
        time.sleep(0.6)
        i += 1


DEFAULT_APK = str(
    PROJECT_ROOT / "app-under-test" / "antennapod" / "app" / "build"
    / "outputs" / "apk" / "play" / "debug" / "app-play-debug.apk"
)
DEFAULT_PACKAGE = "de.danoeh.antennapod.debug"


def main():
    parser = argparse.ArgumentParser(
        description="Frida Dynamic Call Graph Builder for AntennaPod",
    )
    parser.add_argument(
        "--apk", default=DEFAULT_APK,
        help="Path to debug APK (default: app/build/outputs/apk/play/debug/)",
    )
    parser.add_argument(
        "--package", default=DEFAULT_PACKAGE,
        help="Android package name (default: de.danoeh.antennapod.debug)",
    )
    parser.add_argument(
        "--device", default=None,
        help="ADB device serial (default: auto-detect)",
    )
    parser.add_argument(
        "--duration", type=int, default=30,
        help="Trace duration in seconds (default: 30)",
    )
    parser.add_argument(
        "--attach", action="store_true",
        help="Attach to already-running app instead of spawning",
    )
    parser.add_argument(
        "--output", default=str(DEFAULT_OUTPUT),
        help="Output directory for graph images and JSON",
    )
    parser.add_argument(
        "--class-filter", default="de.danoeh.antennapod",
        help="Class prefix filter for hooking (default: de.danoeh.antennapod)",
    )
    parser.add_argument(
        "--no-vis", action="store_true",
        help="Skip visualization, only export JSON",
    )
    parser.add_argument(
        "--no-html", action="store_true",
        help="Skip interactive HTML report",
    )
    parser.add_argument(
        "--no-browser", action="store_true",
        help="Don't auto-open the HTML report in browser",
    )
    args = parser.parse_args()

    output_dir = Path(args.output)
    output_dir.mkdir(parents=True, exist_ok=True)

    device_serial = args.device or get_device_serial()

    rel_apk = os.path.relpath(args.apk, PROJECT_ROOT)
    rel_out = os.path.relpath(str(output_dir), PROJECT_ROOT)

    print(f"Frida Call Graph Builder — {datetime.now().strftime('%Y-%m-%d %H:%M')}")
    print(f"  APK: {rel_apk}")
    print(f"  Output: {rel_out}")

    # ── Device setup ──
    if not args.attach:
        # Ensure APK is installed and app is stopped (Frida will spawn it fresh)
        installed = adb(["shell", "pm", "list", "packages", args.package], device_serial)
        if args.package not in installed:
            print(f"  Installing APK: {args.apk}")
            result = subprocess.run(
                ["adb", "-s", device_serial, "install", "-r", args.apk],
                capture_output=True, text=True,
            )
            if "Success" not in result.stdout:
                sys.exit(f"ERROR: APK install failed:\n{result.stdout}\n{result.stderr}")
            print("  Install: OK")
        # Stop any running instance so we spawn clean
        adb(["shell", "am", "force-stop", args.package], device_serial)
        time.sleep(1)
        print(f"  App ready for spawn: {args.package}")
    else:
        print(f"\n── Attaching to running app: {args.package} ──")

    # ── Trace ──
    tracer = FridaCallGraphTracer(
        package=args.package,
        device_serial=device_serial,
        duration=args.duration,
        output_dir=output_dir,
        class_filter=args.class_filter,
    )

    success = tracer.run()
    if not success:
        print("\n  NOTE: No call edges collected (no user interaction during trace).")
        print("  Hooks are active — interact with the app and re-run to capture edges.")
        if tracer.classes_hooked == 0:
            print("  WARNING: No classes hooked. Check:")
            print("    - Is the app running?")
            print("    - Does class filter match? " + args.class_filter)
            sys.exit(1)

    # ── Build call graph ──
    print(f"\n── Call Graph Analysis ──")
    data = tracer.get_call_graph()

    print(f"  Top callers (outgoing):")
    for name, count in data["top_callers"][:5]:
        print(f"    {_shorten_class(name):60s} {count:>6,} calls")
    print(f"\n  Top callees (incoming):")
    for name, count in data["top_callees"][:5]:
        print(f"    {_shorten_class(name):60s} {count:>6,} calls")

    # ── Export ──
    tracer.export_json(data)

    # ── Visualize ──
    if not args.no_vis:
        print(f"\n── Visualization ──")
        draw_frida_callers_callees(data, output_dir)
        draw_frida_package_heatmap(data, output_dir)
        draw_frida_top_edges_chart(data, output_dir)

    # ── Interactive HTML Report ──
    html_path = None
    if not args.no_html:
        print(f"\n── HTML Report ──")
        html_path = generate_html_report(data, output_dir)

    # ── Summary ──
    print(f"\n── Done ──")
    for f in sorted(output_dir.glob("frida-*")):
        if f.is_file():
            print(f"  {f.name} ({f.stat().st_size // 1024} KB)")

    print(f"\n  Compare with static call graph (Androguard):")
    print(f"    python automation/callgraph.py")

    # ── Open in browser ──
    if html_path and not args.no_browser:
        # file:// protocol can't load CDN scripts — serve via local HTTP
        import threading
        port = 8765
        server_dir = str(html_path.parent)
        os.chdir(server_dir)
        httpd = None
        try:
            from http.server import HTTPServer, SimpleHTTPRequestHandler
            httpd = HTTPServer(("127.0.0.1", port), SimpleHTTPRequestHandler)
            t = threading.Thread(target=httpd.serve_forever, daemon=True)
            t.start()
            url = f"http://127.0.0.1:{port}/{html_path.name}"
        except Exception:
            url = html_path.resolve().as_uri()
        print(f"\n  Opening browser: {url}")
        print(f"  (Local server on port {port} — close terminal to stop)")
        webbrowser.open(url)


if __name__ == "__main__":
    main()
