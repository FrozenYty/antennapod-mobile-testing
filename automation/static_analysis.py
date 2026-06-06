#!/usr/bin/env python3
"""
Static Analysis for AntennaPod APK using Androguard.

Author: Tianyu Yao

Performs manifest-based security and structure analysis without
running the app. Check: permissions, exported components, security flags,
and DEX structure.

Usage:
    python automation/static_analysis.py [--json] [--apk <path>]

Output:
    - Console summary (default)
    - JSON report (with --json)
"""

import argparse
import json
import os
import sys
from datetime import datetime
from typing import Any

try:
    from androguard.core.apk import APK
except ImportError:
    print("ERROR: androguard not installed. Run: pip install androguard")
    sys.exit(1)

# Suppress androguard's verbose loguru output
from loguru import logger
logger.remove()
logger.add(lambda _: None)

# ── Risk definitions ────────────────────────────────────────────────

# Permissions expected and legitimate for a podcast/media app
PODCAST_EXPECTED_PERMISSIONS: set[str] = {
    "android.permission.INTERNET",
    "android.permission.ACCESS_NETWORK_STATE",
    "android.permission.ACCESS_WIFI_STATE",
    "android.permission.CHANGE_WIFI_STATE",
    "android.permission.WAKE_LOCK",
    "android.permission.VIBRATE",
    "android.permission.POST_NOTIFICATIONS",
    "android.permission.FOREGROUND_SERVICE",
    "android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK",
    "android.permission.FOREGROUND_SERVICE_DATA_SYNC",
    "android.permission.RECEIVE_BOOT_COMPLETED",
    "android.permission.BLUETOOTH",
    "android.permission.BLUETOOTH_ADMIN",
    "android.permission.BLUETOOTH_CONNECT",
    "android.permission.MODIFY_AUDIO_SETTINGS",
    "android.permission.SCHEDULE_EXACT_ALARM",
    "android.permission.USE_EXACT_ALARM",
    "android.permission.ACCESS_NOTIFICATION_POLICY",
    "android.permission.READ_MEDIA_AUDIO",
}

DANGEROUS_PERMISSIONS: dict[str, str] = {
    "android.permission.READ_EXTERNAL_STORAGE": "Read external storage",
    "android.permission.WRITE_EXTERNAL_STORAGE": "Write external storage",
    "android.permission.READ_CONTACTS": "Read contacts",
    "android.permission.READ_PHONE_STATE": "Read phone state / IMEI",
    "android.permission.ACCESS_FINE_LOCATION": "Precise GPS location",
    "android.permission.ACCESS_COARSE_LOCATION": "Approximate location",
    "android.permission.CAMERA": "Camera access",
    "android.permission.RECORD_AUDIO": "Microphone recording",
    "android.permission.READ_SMS": "Read SMS messages",
    "android.permission.SEND_SMS": "Send SMS messages",
    "android.permission.CALL_PHONE": "Initiate phone calls",
    "android.permission.READ_CALENDAR": "Read calendar events",
    "android.permission.BODY_SENSORS": "Body sensor data",
    "android.permission.ACTIVITY_RECOGNITION": "Physical activity tracking",
    "android.permission.READ_MEDIA_AUDIO": "Read audio files",
    "android.permission.READ_MEDIA_VIDEO": "Read video files",
    "android.permission.READ_MEDIA_IMAGES": "Read image files",
    "android.permission.POST_NOTIFICATIONS": "Post notifications (Android 13+)",
    "android.permission.FOREGROUND_SERVICE": "Run foreground service",
    "android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK": "Media playback foreground service",
    "android.permission.FOREGROUND_SERVICE_DATA_SYNC": "Data sync foreground service",
    "android.permission.RECEIVE_BOOT_COMPLETED": "Launch on device boot",
    "android.permission.WAKE_LOCK": "Keep device awake",
    "android.permission.REQUEST_INSTALL_PACKAGES": "Install packages",
    "android.permission.SYSTEM_ALERT_WINDOW": "Overlay over other apps",
    "android.permission.INTERNET": "Full network access",
    "android.permission.ACCESS_NETWORK_STATE": "View network state",
    "android.permission.ACCESS_WIFI_STATE": "View Wi-Fi state",
    "android.permission.CHANGE_WIFI_STATE": "Change Wi-Fi state",
    "android.permission.BLUETOOTH": "Bluetooth access",
    "android.permission.BLUETOOTH_ADMIN": "Bluetooth administration",
    "android.permission.BLUETOOTH_CONNECT": "Bluetooth connect (Android 12+)",
    "android.permission.VIBRATE": "Vibrate device",
    "android.permission.SCHEDULE_EXACT_ALARM": "Schedule exact alarms",
    "android.permission.USE_EXACT_ALARM": "Use exact alarms",
    "android.permission.ACCESS_NOTIFICATION_POLICY": "Do Not Disturb access",
    "android.permission.MODIFY_AUDIO_SETTINGS": "Modify audio settings",
}

# Permissions that warrant scrutiny in a podcast app context.
# Podcast apps legitimately need: INTERNET, NETWORK_STATE, WAKE_LOCK,
# FOREGROUND_SERVICE*, BLUETOOTH*, POST_NOTIFICATIONS, VIBRATE,
# RECEIVE_BOOT_COMPLETED, MODIFY_AUDIO_SETTINGS.
HIGH_RISK_PERMISSIONS: set[str] = {
    "android.permission.READ_CONTACTS",
    "android.permission.READ_PHONE_STATE",
    "android.permission.ACCESS_FINE_LOCATION",
    "android.permission.ACCESS_COARSE_LOCATION",
    "android.permission.CAMERA",
    "android.permission.RECORD_AUDIO",
    "android.permission.READ_SMS",
    "android.permission.SEND_SMS",
    "android.permission.CALL_PHONE",
    "android.permission.READ_CALENDAR",
    "android.permission.BODY_SENSORS",
    "android.permission.REQUEST_INSTALL_PACKAGES",
    "android.permission.SYSTEM_ALERT_WINDOW",
    "android.permission.ACTIVITY_RECOGNITION",
    "android.permission.READ_EXTERNAL_STORAGE",
    "android.permission.WRITE_EXTERNAL_STORAGE",
    "android.permission.MANAGE_EXTERNAL_STORAGE",
}


# ── Analysis engine ──────────────────────────────────────────────────

def _maybe(fn, *args) -> Any:
    """Call fn with args, return None on any error."""
    try:
        return fn(*args)
    except Exception:
        return None


def analyze(apk_path: str) -> dict[str, Any]:
    """Run all checks and return a structured result dict."""
    if not os.path.exists(apk_path):
        raise FileNotFoundError(f"APK not found: {apk_path}")

    apk = APK(apk_path)
    report: dict[str, Any] = {
        "meta": {
            "tool": "Androguard static analysis",
            "apk": apk_path,
            "timestamp": datetime.now().isoformat(),
        },
        "app_info": {},
        "permissions": {"declared": [], "dangerous": [], "high_risk": []},
        "components": [],
        "security_flags": [],
        "signature": {},
        "findings": {"pass": 0, "warn": 0, "fail": 0, "items": []},
    }

    # ── App info ──
    report["app_info"] = {
        "package": apk.get_package(),
        "version_name": apk.get_androidversion_name(),
        "version_code": apk.get_androidversion_code(),
        "min_sdk": apk.get_min_sdk_version(),
        "target_sdk": apk.get_target_sdk_version(),
        "max_sdk": apk.get_max_sdk_version(),
    }

    # ── Permissions ──
    declared = sorted(set(
        apk.get_permissions() + apk.get_declared_permissions()
    ))
    report["permissions"]["declared"] = declared
    report["permissions"]["count"] = len(declared)

    dangerous = [p for p in declared if p in DANGEROUS_PERMISSIONS]
    high_risk = [p for p in dangerous if p in HIGH_RISK_PERMISSIONS]
    report["permissions"]["dangerous"] = [
        {"permission": p, "label": DANGEROUS_PERMISSIONS[p]}
        for p in dangerous
    ]
    report["permissions"]["high_risk"] = high_risk

    for p in dangerous:
        if p in HIGH_RISK_PERMISSIONS:
            level = "fail"
        elif p in PODCAST_EXPECTED_PERMISSIONS:
            level = "pass"
        else:
            level = "warn"
        label = DANGEROUS_PERMISSIONS.get(p, p)
        _add_finding(report, level, f"Dangerous permission: {label}",
                     detail=f"Permission: {p}")

    # ── Security flags ──
    is_debuggable = apk.get_attribute_value(
        "application", "android:debuggable"
    )
    allow_backup = _maybe(
        apk.get_attribute_value, "application", "android:allowBackup"
    )
    network_config = _maybe(
        apk.get_attribute_value, "application", "android:networkSecurityConfig"
    )

    build_type = "debug" if "debug" in apk_path.lower() else "unknown"
    report["security_flags"] = [
        {"flag": "debuggable", "value": str(is_debuggable)},
        {"flag": "allowBackup", "value": str(allow_backup)},
    ]
    if network_config:
        report["security_flags"].append(
            {"flag": "networkSecurityConfig", "value": str(network_config)}
        )

    if is_debuggable == "true" and build_type == "debug":
        _add_finding(report, "pass", "Debuggable=true (expected for debug build)")
    elif is_debuggable == "true":
        _add_finding(report, "fail", "Debuggable=true in non-debug build")
    else:
        _add_finding(report, "pass", "Debuggable=false (release mode)")

    # ── Component analysis ──
    component_sources: dict[str, list[str]] = {
        "activity": apk.get_activities(),
        "service": apk.get_services(),
        "receiver": apk.get_receivers(),
        "provider": apk.get_providers(),
    }

    for comp_type, names in component_sources.items():
        for name in names:
            intent_filters = _maybe(
                apk.get_intent_filters, comp_type, name
            )
            filter_actions: list[str] = []
            if intent_filters:
                filter_actions = intent_filters.get("action", [])

            short_name = name.split(".")[-1] if "." in name else name

            report["components"].append({
                "type": comp_type,
                "name": name,
                "intent_filters": filter_actions,
            })

            # Flag components with intent filters that may be exported
            if filter_actions:
                _add_finding(
                    report, "pass",
                    f"{comp_type.capitalize()} with intent-filter: {short_name}",
                    detail=f"Full: {name} | Actions: {', '.join(filter_actions)}",
                )

    # Count by type
    type_counts: dict[str, int] = {}
    for c in report["components"]:
        type_counts[c["type"]] = type_counts.get(c["type"], 0) + 1
    report["component_summary"] = type_counts

    # ── Signature ──
    try:
        report["signature"] = {
            "signed_v1": apk.is_signed_v1(),
            "signed_v2": apk.is_signed_v2(),
            "signed_v3": apk.is_signed_v3(),
        }
    except Exception as e:
        report["signature"] = {"error": str(e)}

    return report


def _add_finding(report: dict, level: str, msg: str, *, detail: str = ""):
    """Append a finding to the report."""
    if level == "pass":
        report["findings"]["pass"] += 1
    elif level == "fail":
        report["findings"]["fail"] += 1
    else:
        report["findings"]["warn"] += 1
    report["findings"]["items"].append({
        "level": level, "message": msg, "detail": detail,
    })


# ── Output ───────────────────────────────────────────────────────────

def print_console(report: dict) -> None:
    """Pretty-print analysis results to the terminal."""
    info = report["app_info"]
    perm = report["permissions"]
    findings = report["findings"]

    sep = "=" * 60
    print(f"\n{sep}")
    print(f"  AntennaPod Static Analysis — Androguard")
    print(f"  {report['meta']['timestamp']}")
    print(f"{sep}")

    print(f"\n  Package : {info['package']}")
    print(f"  Version : {info['version_name']} ({info['version_code']})")
    print(f"  SDK     : min={info['min_sdk']}  target={info['target_sdk']}")

    print(f"\n  Permissions declared : {perm['count']}")
    if perm["dangerous"]:
        print(f"  Dangerous permissions : {len(perm['dangerous'])}")
        for p in perm["dangerous"]:
            tag = "!!" if p["permission"] in HIGH_RISK_PERMISSIONS else "  "
            print(f"    {tag} {p['label']}")
    else:
        print("  Dangerous permissions : none")

    print(f"\n  Components : {len(report['components'])}")
    if "component_summary" in report:
        for ctype, count in report["component_summary"].items():
            print(f"    {ctype}s: {count}")
    filters_count = len([
        c for c in report["components"] if c["intent_filters"]
    ])
    print(f"    with intent-filters: {filters_count}")

    print(f"\n  Security flags:")
    for f in report["security_flags"]:
        print(f"    {f['flag']:30s} = {f['value']}")

    print(f"\n  Findings: {findings['pass']} pass  "
          f"{findings['warn']} warn  {findings['fail']} fail")

    if findings["items"]:
        for item in findings["items"]:
            icon = {"pass": "[PASS]", "warn": "[WARN]", "fail": "[FAIL]"}[item["level"]]
            print(f"    {icon} {item['message']}")

    print(f"\n{sep}")
    if findings["fail"] > 0:
        print("  RESULT: FAIL  ({f} failures)".format(f=findings["fail"]))
    elif findings["warn"] > 0:
        print(f"  RESULT: PASS with {findings['warn']} warning(s)")
    else:
        print("  RESULT: ALL CLEAN")
    print(f"{sep}\n")
    sys.stdout.flush()


# ── CLI ──────────────────────────────────────────────────────────────

DEFAULT_APK = os.path.join(
    os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
    "app-under-test", "antennapod", "app", "build", "outputs",
    "apk", "play", "debug", "app-play-debug.apk",
)


def main():
    parser = argparse.ArgumentParser(description="Static analysis for AntennaPod APK")
    parser.add_argument("--apk", default=DEFAULT_APK, help="Path to APK file")
    parser.add_argument("--json", action="store_true", help="Output JSON to stdout")
    args = parser.parse_args()

    try:
        report = analyze(args.apk)
    except FileNotFoundError as e:
        print(f"ERROR: {e}", file=sys.stderr)
        print("Build the APK first: cd app-under-test/antennapod && "
              "./gradlew :app:assemblePlayDebug", file=sys.stderr)
        sys.exit(1)

    if args.json:
        print(json.dumps(report, indent=2, default=str))
    else:
        print_console(report)

    # Exit with non-zero if fails
    if report["findings"]["fail"] > 0:
        sys.exit(1)


if __name__ == "__main__":
    main()
