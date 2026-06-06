#!/usr/bin/env python3
"""
Call Graph & Static Analysis Visualization for AntennaPod APK.

Author: Tianyu Yao

Generates:
  1. Package-level method call graph (PNG)
  2. Top-level package interaction network (PNG)
  3. Method cross-reference statistics

Based on Androguard's Analysis API (Lecture L11 techniques):
  - DEX → Analysis → create_xref() → call graph extraction
  - CG(Call Graph) / CFG(Control Flow Graph) principles

Usage:
    python automation/callgraph.py [--apk <path>] [--output <dir>]
"""

import argparse
import os
import sys
from collections import defaultdict
from datetime import datetime
from pathlib import Path
from typing import Any

PROJECT_ROOT = Path(__file__).resolve().parent.parent

# Suppress androguard logging
from loguru import logger as _loguru_logger
_loguru_logger.remove()
_loguru_logger.add(lambda _: None)

try:
    from androguard.misc import AnalyzeAPK
except ImportError:
    print("ERROR: androguard not installed. Run: pip install androguard")
    sys.exit(1)

import matplotlib
matplotlib.use("Agg")  # Non-interactive backend
import matplotlib.pyplot as plt
import networkx as nx


# ── Core analysis ─────────────────────────────────────────────────────

def analyze_call_graph(apk_path: str) -> dict[str, Any]:
    """Extract method call relationships from APK using androguard."""
    if not os.path.exists(apk_path):
        raise FileNotFoundError(f"APK not found: {apk_path}")

    print(f"  Analyzing APK with Androguard...")
    a, d_list, dx = AnalyzeAPK(apk_path)

    print(f"  Package: {a.get_package()}")
    print(f"  DEX files: {len(d_list)}")
    all_classes = dx.get_classes()
    print(f"  Total classes: {len(all_classes)}")

    # Build call graph using androguard's built-in method
    from androguard.core.bytecode import FormatClassToJava
    entry_points = list(map(
        FormatClassToJava,
        a.get_activities() + a.get_services()
        + a.get_receivers() + a.get_providers(),
    ))

    cg = dx.get_call_graph(no_isolated=True, entry_points=entry_points)
    print(f"  Call graph nodes: {cg.number_of_nodes()}")
    print(f"  Call graph edges: {cg.number_of_edges()}")

    # Process call graph edges into class-level and package-level
    edge_weights: dict[tuple[str, str], int] = defaultdict(int)
    pkg_edges: dict[tuple[str, str], int] = defaultdict(int)
    caller_counts: dict[str, int] = defaultdict(int)
    callee_counts: dict[str, int] = defaultdict(int)
    total_methods = cg.number_of_nodes()

    for src_node, dst_node in cg.edges():
        try:
            src_class = _shorten_class(src_node.get_class_name())
            dst_class = _shorten_class(dst_node.get_class_name())
        except Exception:
            continue

        if src_class == dst_class:
            continue

        edge_weights[(src_class, dst_class)] += 1
        caller_counts[src_class] += 1
        callee_counts[dst_class] += 1

        # Package edges
        src_pkg = _extract_package(src_node.get_class_name())
        tgt_pkg = _extract_package(dst_node.get_class_name())
        if src_pkg and tgt_pkg and src_pkg != tgt_pkg:
            pkg_edges[(src_pkg, tgt_pkg)] += 1

    total_edges = cg.number_of_edges()
    print(f"  Class-level edges (inter-class): {len(edge_weights)}")
    print(f"  Package-level edges: {len(pkg_edges)}")

    top_callers = sorted(caller_counts.items(), key=lambda x: -x[1])[:20]
    top_callees = sorted(callee_counts.items(), key=lambda x: -x[1])[:20]

    return {
        "edge_weights": dict(edge_weights),
        "pkg_edges": dict(pkg_edges),
        "total_methods": total_methods,
        "total_xrefs": total_edges,
        "total_classes": len(all_classes),
        "call_graph_nodes": cg.number_of_nodes(),
        "call_graph_edges": cg.number_of_edges(),
        "top_callers": top_callers,
        "top_callees": top_callees,
        "package": a.get_package(),
        "version": a.get_androidversion_name(),
    }


def _shorten_class(full_name: str) -> str:
    """Shorten a fully qualified class name for readability."""
    parts = full_name.replace("/", ".").split(".")
    if len(parts) <= 2:
        return full_name.replace("/", ".")
    # Keep last 2 segments + first letter of preceding segments
    short = ".".join(p[0] for p in parts[:-2]) + "." + ".".join(parts[-2:])
    return short


def _extract_package(class_name: str) -> str:
    """Extract top-level package prefix."""
    parts = class_name.replace("/", ".").split(".")
    if "antennapod" in parts:
        idx = parts.index("antennapod")
        if idx + 1 < len(parts):
            return ".".join(parts[:idx + 2])
        return ".".join(parts[:idx + 1])
    return parts[0] if parts else "unknown"


# ── Visualization ─────────────────────────────────────────────────────

def draw_package_heatmap(data: dict, output_dir: Path):
    """Heatmap matrix of package-to-package call density."""
    pkg_edges = data["pkg_edges"]
    if not pkg_edges:
        return

    # Collect unique packages
    packages_set: set[str] = set()
    for src, tgt in pkg_edges:
        packages_set.add(src)
        packages_set.add(tgt)

    # Select top N packages by total weight
    pkg_weight: dict[str, int] = defaultdict(int)
    for (src, tgt), w in pkg_edges.items():
        pkg_weight[src] += w
        pkg_weight[tgt] += w
    top_pkgs = sorted(pkg_weight, key=lambda x: -pkg_weight[x])[:20]
    top_pkgs.sort()

    # Build matrix
    n = len(top_pkgs)
    matrix = [[0] * n for _ in range(n)]
    for (src, tgt), w in pkg_edges.items():
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
                color = "white" if matrix[i][j] > max(5, matrix_max * 0.6) else "black"
                ax.text(j, i, str(matrix[i][j]), ha="center", va="center",
                        fontsize=5, color=color, fontweight="bold")

    plt.colorbar(im, ax=ax, shrink=0.8, label="Call count")
    ax.spines[["top", "right"]].set_visible(False)
    plt.title(f"Package Interaction Heatmap — AntennaPod v{data['version']}\n"
              f"Top {n} packages | {sum(sum(row) for row in matrix)} total cross-package calls",
              fontsize=12, fontweight="bold")
    plt.tight_layout()

    for fmt in ["pdf", "png"]:
        out = output_dir / f"callgraph-heatmap.{fmt}"
        plt.savefig(out, dpi=600 if fmt == "png" else None, bbox_inches="tight")
    plt.close()
    print(f"  Heatmap saved: callgraph-heatmap.pdf/png")


def draw_class_chord(data: dict, output_dir: Path, top_n: int = 20):
    """Arc diagram of top class-to-class call relationships."""
    edge_weights = data["edge_weights"]
    if not edge_weights:
        return

    # Select top classes
    node_weight: dict[str, int] = defaultdict(int)
    for (src, tgt), w in edge_weights.items():
        node_weight[src] += w
        node_weight[tgt] += w

    top_classes = [n for n, _ in sorted(node_weight.items(),
                                         key=lambda x: -x[1])[:top_n]]
    short_map = {c: _shorten_class(c) for c in top_classes}

    # Build adjacency matrix for top classes
    n = len(top_classes)
    idx = {c: i for i, c in enumerate(top_classes)}
    matrix = [[0] * n for _ in range(n)]
    for (src, tgt), w in edge_weights.items():
        if src in idx and tgt in idx:
            matrix[idx[src]][idx[tgt]] += w

    names = [short_map.get(c, c) for c in top_classes]

    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(20, 10),
                                    gridspec_kw={'width_ratios': [3, 1]})

    # Left: heatmap
    im = ax1.imshow(matrix, cmap="Blues", aspect="auto")
    ax1.set_xticks(range(n))
    ax1.set_xticklabels(names, rotation=45, ha="right", fontsize=6)
    ax1.set_yticks(range(n))
    ax1.set_yticklabels(names, fontsize=6)
    ax1.set_title(f"Top {top_n} Class Call Matrix")
    for i in range(n):
        for j in range(n):
            if matrix[i][j] >= 10:
                ax1.text(j, i, str(matrix[i][j]), ha="center", va="center",
                         fontsize=4)

    # Right: total call volume per class (bar)
    totals = [sum(row) + sum(matrix[k][i] for k in range(n)) for i, row in enumerate(matrix)]
    sorted_idx = sorted(range(n), key=lambda i: totals[i])
    sorted_names = [names[i] for i in sorted_idx]
    sorted_totals = [totals[i] for i in sorted_idx]

    # Highlight top-3 with emphasis color, rest gray
    bar_colors = ["#9673A6" if i >= n - 3 else "#BDBDBD" for i in range(n)]
    ax2.barh(range(n), sorted_totals, color=bar_colors, edgecolor="black", linewidth=0.5)
    ax2.set_yticks(range(n))
    ax2.set_yticklabels(sorted_names, fontsize=6)
    ax2.set_xlabel("Total call volume")
    ax2.set_title("Ranked by\ncall volume")
    ax2.invert_yaxis()
    ax2.spines[["top", "right"]].set_visible(False)

    fig.suptitle(f"Class-Level Call Relationships — AntennaPod v{data['version']}\n"
                 f"{n} classes, {sum(sum(row) for row in matrix)} inter-class calls",
                 fontsize=13, fontweight="bold")
    plt.tight_layout()

    for fmt in ["pdf", "png"]:
        out = output_dir / f"callgraph-classes.{fmt}"
        plt.savefig(out, dpi=600 if fmt == "png" else None, bbox_inches="tight")
    plt.close()
    print(f"  Class matrix saved: callgraph-classes.pdf/png")

def draw_top_callers_chart(data: dict, output_dir: Path):
    """Bar chart of top caller/callee classes."""
    top_callers = data["top_callers"][:15]
    top_callees = data["top_callees"][:15]

    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(16, 8))
    n1, n2 = len(top_callers), len(top_callees)

    names1 = [_shorten_class(n) for n, _ in top_callers]
    colors1 = ["#9673A6" if i == 0 else "#BDBDBD" for i in range(n1)]
    ax1.barh(range(n1), [v for _, v in top_callers], color=colors1, edgecolor="black",
             linewidth=0.5)
    ax1.set_yticks(range(n1))
    ax1.set_yticklabels(names1, fontsize=7)
    ax1.set_xlabel("Outgoing calls")
    ax1.set_title("Top 15 Callers")
    ax1.invert_yaxis()
    ax1.spines[["top", "right"]].set_visible(False)
    for i, (_, v) in enumerate(top_callers):
        ax1.text(v + max(v * 0.01, 10), i, f"{v:,}", va="center", fontsize=6)

    names2 = [_shorten_class(n) for n, _ in top_callees]
    colors2 = ["#9673A6" if i == 0 else "#BDBDBD" for i in range(n2)]
    ax2.barh(range(n2), [v for _, v in top_callees], color=colors2, edgecolor="black",
             linewidth=0.5)
    ax2.set_yticks(range(n2))
    ax2.set_yticklabels(names2, fontsize=7)
    ax2.set_xlabel("Incoming calls")
    ax2.set_title("Top 15 Callees")
    ax2.invert_yaxis()
    ax2.spines[["top", "right"]].set_visible(False)
    for i, (_, v) in enumerate(top_callees):
        ax2.text(v + max(v * 0.01, 10), i, f"{v:,}", va="center", fontsize=6)

    fig.suptitle(f"Call Graph Statistics — {data['package']} v{data['version']}",
                 fontsize=13, fontweight="bold")
    plt.tight_layout()
    for fmt in ["pdf", "png"]:
        out = output_dir / f"callgraph-stats.{fmt}"
        plt.savefig(out, dpi=600 if fmt == "png" else None, bbox_inches="tight")
    plt.close()
    print(f"  Stats chart saved: callgraph-stats.pdf/png")


def _pkg_short(pkg: str) -> str:
    parts = pkg.split(".")
    if len(parts) <= 3:
        return pkg
    return parts[0][0] + "." + ".".join(parts[2:])


def export_call_graph_gml(output_dir: Path, apk_path: str):
    """Export call graph as GML for Gephi (PPT-style professional visualization)."""
    print("\nExporting call graph GML for Gephi...")
    from androguard.core.bytecode import FormatClassToJava
    from androguard.misc import AnalyzeAPK as _AnalyzeAPK
    import networkx as nx

    a, d_list, dx = _AnalyzeAPK(apk_path)
    entry_points = list(map(
        FormatClassToJava,
        a.get_activities() + a.get_services()
        + a.get_receivers() + a.get_providers(),
    ))

    cg = dx.get_call_graph(
        classname=r".*antennapod.*",
        no_isolated=True,
        entry_points=entry_points,
    )

    out = output_dir / "callgraph-antennapod.gml"
    nx.write_gml(cg, str(out), stringizer=str)
    rel_gml = os.path.relpath(str(out), PROJECT_ROOT)
    print(f"  GML exported: {rel_gml} ({cg.number_of_nodes():,} nodes, "
          f"{cg.number_of_edges():,} edges)")
    print(f"  Open in Gephi: File → Open → {out.name}")
    print(f"  → Use 'Force Atlas 2' layout, then 'Preview' for PPT-quality")


def generate_cfg_samples(apk_path: str, output_dir: Path):
    """Generate sample CFG images for key methods (PPT-style per-method CFG)."""
    print("\nGenerating sample CFG images...")
    from androguard.misc import AnalyzeAPK as _AnalyzeAPK

    a, d_list, dx = _AnalyzeAPK(apk_path)

    cfg_dir = output_dir / "cfg"
    cfg_dir.mkdir(parents=True, exist_ok=True)

    # Select key methods from AntennaPod app code only
    key_patterns = {
        r"onCreate$": 3,
        r"onStartCommand$": 2,
        r"setDownloaded": 2,
        r"getFeed$": 2,
    }

    count = 0
    for pattern, limit in key_patterns.items():
        pat_count = 0
        for meth in dx.find_methods(
            classname=r".*antennapod.*",
            methodname=pattern
        ):
            if pat_count >= limit:
                break
            try:
                mx = meth.get_method()
                if not mx:
                    continue

                class_name = meth.get_class_name().split("/")[-1].replace(";", "")
                fname = f"{class_name}_{meth.name}".replace("<", "init>")

                import graphviz as gv
                dot = gv.Digraph(name=fname, format="png",
                                 engine="dot")
                dot.attr(rankdir="TB", fontsize="12")
                dot.attr("node", shape="box", style="rounded,filled",
                         fillcolor="#D4E6F1", fontname="Courier New",
                         fontsize="9")
                dot.attr("edge", fontsize="7", color="#555555")

                # Get instructions and build sequential CFG
                insns = list(mx.get_instructions())
                if len(insns) == 0:
                    continue
                if len(insns) > 80:  # Skip huge methods
                    continue

                # Simple sequential CFG with branch detection
                branch_ops = {0x28, 0x29, 0x2a, 0x2b, 0x2c, 0x2d,
                              0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38,
                              0x0e, 0x0f, 0x2e, 0x2f, 0x30, 0x31}
                bbs = []
                current = []
                for ins in insns:
                    current.append(ins)
                    try:
                        if ins.get_op_value() in branch_ops:
                            bbs.append(list(current))
                            current = []
                    except Exception:
                        pass
                if current:
                    bbs.append(current)

                # Skip trivial methods (< 4 BBs = uninteresting)
                if len(bbs) < 4:
                    continue

                # Add BB nodes
                for i, bb in enumerate(bbs):
                    lines = []
                    for ins in bb[:12]:
                        lines.append(ins.get_name()[:70])
                    label = f"BB{i}\\n" + "\\n".join(lines)
                    if len(bb) > 12:
                        label += f"\\n... ({len(bb)} total)"
                    dot.node(f"bb{i}", label=label)

                # Add edges: sequential flow + branch arcs
                for i in range(len(bbs) - 1):
                    last_ins = bbs[i][-1] if bbs[i] else None
                    jumped = False
                    if last_ins:
                        try:
                            op = last_ins.get_op_value()
                            if op in (0x28, 0x29, 0x2a, 0x2b, 0x2c, 0x2d):
                                dot.edge(f"bb{i}", f"bb{i+1}",
                                         label="T", color="#2E86C1", fontsize="7")
                                target = last_ins.get_off() + last_ins.get_ref_off()
                                for j, bb in enumerate(bbs):
                                    if j != i + 1 and bb and bb[0].get_off() == target:
                                        dot.edge(f"bb{i}", f"bb{j}",
                                                 label="F", color="#E74C3C",
                                                 fontsize="7")
                                        jumped = True
                            elif op in (0x2e, 0x2f, 0x30, 0x31):
                                target = last_ins.get_off() + last_ins.get_ref_off()
                                dot.edge(f"bb{i}", f"bb{i+1}",
                                         label="fallthrough", color="#999999",
                                         style="dotted", fontsize="6")
                                for j, bb in enumerate(bbs):
                                    if j != i + 1 and bb and bb[0].get_off() == target:
                                        dot.edge(f"bb{i}", f"bb{j}",
                                                 label="goto", color="#E74C3C",
                                                 style="dashed", fontsize="7")
                                        jumped = True
                        except Exception:
                            pass
                    if not jumped:
                        dot.edge(f"bb{i}", f"bb{i+1}")

                out_path = cfg_dir / fname
                dot.render(str(out_path), cleanup=True)
                count += 1
                pat_count += 1
            except Exception:
                continue

    # Also highlight a few as "exemplar"
    pngs = sorted(cfg_dir.glob("*.png"))
    print(f"  CFG samples generated: {count} images in {cfg_dir}")
    if pngs:
        print(f"  Exemplars: {', '.join(p.name for p in pngs[:4])} ...")


def draw_dex_composition(apk_path: str, output_dir: Path):
    """DEX file size treemap & method count distribution."""
    from androguard.misc import AnalyzeAPK as _AnalyzeAPK
    a, d_list, dx = _AnalyzeAPK(apk_path)

    dex_data = []
    for i, d in enumerate(d_list):
        dex_data.append({
            "idx": i,
            "classes": len(d.get_classes()),
            "methods": len(d.get_methods()),
            "strings": len(d.get_strings()),
        })

    # Top DEX by method count (filter those with >1000 methods)
    dex_significant = [d for d in dex_data if d["methods"] > 1000]
    sorted_dex = sorted(dex_significant, key=lambda x: -x["methods"])

    # Unified color mapping: same DEX id → same color in both charts
    dex_palette = ["#9673A6", "#6C8EBF", "#82B366", "#D79B00", "#B85450"]
    dex_color = {d["idx"]: dex_palette[i % len(dex_palette)]
                 for i, d in enumerate(sorted_dex)}

    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(14, 6))

    # Left: horizontal bar — all major DEX by method count
    names = [f"DEX #{d['idx']}" for d in sorted_dex]
    counts = [d["methods"] for d in sorted_dex]
    bar_colors = [dex_color[d["idx"]] for d in sorted_dex]
    ax1.barh(range(len(names)), counts, color=bar_colors, edgecolor="black", linewidth=0.5)
    ax1.set_yticks(range(len(names)))
    ax1.set_yticklabels(names, fontsize=8)
    ax1.set_xlabel("Method count")
    ax1.set_title(f"Major DEX Files ({len(sorted_dex)} with >1000 methods)")
    ax1.invert_yaxis()
    ax1.spines[["top", "right"]].set_visible(False)
    for i, v in enumerate(counts):
        ax1.text(v + 500, i, f"{v:,}", va="center", fontsize=7)

    # Right: donut — top 3 + Others, same colors from unified map
    top3 = sorted_dex[:3]
    other_small = sum(d["classes"] for d in sorted_dex[3:])
    sizes = [d["classes"] for d in top3] + [other_small]
    labels = [f"DEX #{d['idx']}" for d in top3] + [f"Others\n({len(sorted_dex)-3} DEX)"]
    donut_colors = [dex_color[d["idx"]] for d in top3] + ["#BDBDBD"]

    wedges, texts, autotexts = ax2.pie(
        sizes, labels=labels, colors=donut_colors, startangle=90,
        autopct="%1.0f%%", pctdistance=0.82, labeldistance=1.12,
        wedgeprops=dict(width=0.4, edgecolor="white", linewidth=1.5))
    for at in autotexts:
        at.set_color("black"); at.set_fontsize(8)
    ax2.text(0, 0, f"{sum(sizes):,}\nclasses", ha="center", va="center",
             fontsize=12, fontweight="bold")
    ax2.set_aspect("equal")
    ax2.set_title(f"Class Distribution ({len(sorted_dex)} Major DEX)")

    fig.suptitle(f"DEX Composition — AntennaPod v{a.get_androidversion_name()}",
                 fontsize=13, fontweight="bold")
    plt.tight_layout()
    for fmt in ["pdf", "png"]:
        out = output_dir / f"dex-composition.{fmt}"
        plt.savefig(out, dpi=600 if fmt == "png" else None, bbox_inches="tight")
    plt.close()
    print(f"  DEX composition saved: dex-composition.pdf/png")


def draw_test_coverage(output_dir: Path):
    """Donut chart of test method distribution across 40 TCs."""
    methods = ["Espresso", "UIAutomator", "Unit Test",
               "Integration", "Manual", "Performance"]
    counts = [13, 7, 8, 6, 4, 2]
    palette = ["#9673A6", "#6C8EBF", "#82B366",
               "#D79B00", "#B85450", "#BDBDBD"]

    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(14, 6))

    # Left: donut
    wedges, texts, autotexts = ax1.pie(
        counts, labels=methods, colors=palette, startangle=90,
        autopct="%1.0f%%", pctdistance=0.78, labeldistance=1.08,
        wedgeprops=dict(width=0.4, edgecolor="white", linewidth=1.5))
    for at in autotexts:
        at.set_color("black"); at.set_fontsize(9)
    ax1.text(0, 0, f"{sum(counts)}\nTCs", ha="center", va="center",
             fontsize=14, fontweight="bold")
    ax1.set_aspect("equal")
    ax1.set_title("Test Method Distribution")

    # Right: stacked bar by sprint
    sprints = ["Sprint 1\n(Core)", "Sprint 2\n(Subscription)", "Sprint 3\n(Playback)", "Sprint 4\n(Settings)"]
    sprint_data = {
        "Espresso":    [5, 3, 3, 2],
        "UIAutomator": [1, 2, 2, 2],
        "Unit Test":   [2, 2, 2, 2],
        "Integration": [1, 1, 2, 2],
        "Manual":      [1, 1, 1, 1],
        "Performance": [0, 1, 0, 1],
    }

    x = range(len(sprints))
    bottom = [0] * len(sprints)
    for method, color in zip(methods, palette):
        values = sprint_data[method]
        ax2.bar(x, values, bottom=bottom, label=method, color=color,
                edgecolor="black", linewidth=0.5)
        bottom = [b + v for b, v in zip(bottom, values)]

    ax2.set_xticks(x)
    ax2.set_xticklabels(sprints, fontsize=9)
    ax2.set_ylabel("Test Cases")
    ax2.set_title("Test Coverage by Sprint")
    ax2.legend(frameon=False, loc="upper left", ncol=2, fontsize=8)
    ax2.spines[["top", "right"]].set_visible(False)
    ax2.set_ylim(0, 12)

    fig.suptitle("Test Coverage Analysis — 40 TCs Across 4 Sprints",
                 fontsize=13, fontweight="bold")
    plt.tight_layout()
    for fmt in ["pdf", "png"]:
        out = output_dir / f"test-coverage.{fmt}"
        plt.savefig(out, dpi=600 if fmt == "png" else None, bbox_inches="tight")
    plt.close()
    print(f"  Test coverage saved: test-coverage.pdf/png")


def draw_component_exposure(apk_path: str, output_dir: Path):
    """Bar chart of Android components and their exposure status."""
    from androguard.misc import AnalyzeAPK as _AnalyzeAPK
    a, d_list, dx = _AnalyzeAPK(apk_path)

    comps = {
        "Activity": a.get_activities(),
        "Service": a.get_services(),
        "Receiver": a.get_receivers(),
        "Provider": a.get_providers(),
    }

    data = []
    for ctype, names in comps.items():
        exported = 0
        for name in names:
            try:
                filters = a.get_intent_filters(ctype.lower(), name)
                if filters:
                    exported += 1
            except Exception:
                pass
        data.append((ctype, len(names), exported))

    fig, ax = plt.subplots(figsize=(8, 5))
    types = [d[0] for d in data]
    total = [d[1] for d in data]
    exposed = [d[2] for d in data]
    internal = [t - e for t, e in zip(total, exposed)]

    x = range(len(types))
    ax.bar(x, internal, color="#BDBDBD", edgecolor="black", linewidth=0.5,
           label="Internal (no intent-filter)")
    ax.bar(x, exposed, bottom=internal, color="#9673A6", edgecolor="black",
           linewidth=0.5, label="Exposed (has intent-filter)")
    ax.set_xticks(x)
    ax.set_xticklabels(types, fontsize=11)
    ax.set_ylabel("Count")
    ax.set_title(f"Android Component Exposure — {a.get_package()}\n"
                 f"{sum(total)} total components | v{a.get_androidversion_name()}",
                 fontsize=12, fontweight="bold")
    ax.legend(frameon=False, loc="upper left", fontsize=10)
    ax.spines[["top", "right"]].set_visible(False)
    ax.set_ylim(0, max(total) * 1.25)

    # Annotate
    for i, (t, e) in enumerate(zip(total, exposed)):
        ax.text(i, t + 0.5, str(t), ha="center", fontsize=10, fontweight="bold")

    plt.tight_layout()
    for fmt in ["pdf", "png"]:
        out = output_dir / f"component-exposure.{fmt}"
        plt.savefig(out, dpi=600 if fmt == "png" else None, bbox_inches="tight")
    plt.close()
    print(f"  Component exposure saved: component-exposure.pdf/png")


# ── CLI ───────────────────────────────────────────────────────────────

DEFAULT_APK = str(
    PROJECT_ROOT / "app-under-test" / "antennapod" / "app" / "build"
    / "outputs" / "apk" / "play" / "debug" / "app-play-debug.apk"
)

DEFAULT_OUTPUT = str(PROJECT_ROOT / "test-docs" / "callgraphs")


def main():
    parser = argparse.ArgumentParser(
        description="Generate call graph visualizations for Android APK"
    )
    parser.add_argument("--apk", default=DEFAULT_APK, help="Path to APK")
    parser.add_argument("--output", default=DEFAULT_OUTPUT,
                        help="Output directory for graph images")
    parser.add_argument("--top-n", type=int, default=60,
                        help="Max classes in method call graph")
    args = parser.parse_args()

    output_dir = Path(args.output)
    output_dir.mkdir(parents=True, exist_ok=True)

    rel_apk = str(Path(os.path.relpath(args.apk, PROJECT_ROOT)))
    rel_out = str(Path(os.path.relpath(str(output_dir), PROJECT_ROOT)))

    print(f"Call Graph Analysis — {datetime.now().strftime('%Y-%m-%d %H:%M')}")
    print(f"  APK: {rel_apk}")
    print(f"  Output: {rel_out}")

    data = analyze_call_graph(args.apk)

    # Clean old blurry network images
    for old_img in ["callgraph-methods.png", "callgraph-package.png"]:
        p = output_dir / old_img
        if p.exists():
            p.unlink()

    print(f"\nGenerating visualizations...")
    draw_top_callers_chart(data, output_dir)
    draw_package_heatmap(data, output_dir)
    draw_class_chord(data, output_dir, top_n=max(15, args.top_n // 2))
    export_call_graph_gml(output_dir, args.apk)
    generate_cfg_samples(args.apk, output_dir)
    draw_dex_composition(args.apk, output_dir)
    draw_test_coverage(output_dir)
    draw_component_exposure(args.apk, output_dir)

    # Print summary stats
    print(f"\n=== Summary ===")
    print(f"  Call graph: {data['call_graph_nodes']} nodes, "
          f"{data['call_graph_edges']} edges")
    print(f"  Classes: {data['total_classes']}")

    print(f"\n  Top callers:")
    for name, count in data["top_callers"][:5]:
        print(f"    {_shorten_class(name)} ({count} calls)")

    print(f"\n  Top callees:")
    for name, count in data["top_callees"][:5]:
        print(f"    {_shorten_class(name)} ({count} incoming)")

    print(f"\n  Images saved to: {rel_out}")
    for f in sorted(output_dir.glob("*.png")):
        print(f"    {f.name} ({f.stat().st_size // 1024} KB)")

    # Clean up androguard intermediate files
    for junk in ["androguard.log", "androguard.db", "callgraph.gml"]:
        p = PROJECT_ROOT / junk
        if p.exists():
            p.unlink()
            print(f"  Cleaned: {junk}")


if __name__ == "__main__":
    main()
