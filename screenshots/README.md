# Screenshots

Store test evidence screenshots here. Reference them from bug reports, manual test results,
or test case documentation.

## Quality Principle — Better None Than Junk

**Better none than junk.** Every screenshot in this directory must serve a clear purpose.

### Before Committing ANY Screenshot

1. **Compare against all existing screenshots** in this directory. Check visually whether your new screenshot is the same UI state as any existing one.
2. **Same screen = delete the new one.** A different TC-ID prefix does NOT justify duplicate content.
3. **One-sentence test**: if you can't explain the screenshot's value in one sentence, delete it.
4. **Clean up device**: after pulling, run `adb shell rm -rf /storage/emulated/0/Download/screenshots/` to prevent stale screenshots from accumulating and polluting future test runs.

### Capture

| Scenario | Example |
|----------|---------|
| Bug visual evidence | A crash dialog, a layout glitch, incorrect text rendering |
| Unique UI state | Player controls, subscription list, OPML import flow |
| Cross-app interaction | System file picker, permission dialog |
| Manual test proof | A completed manual step that needs visual confirmation |

### Do NOT capture

| Anti-pattern | Why |
|-------------|-----|
| Every test step | Screenshots are evidence, not a step-by-step log |
| Duplicate UI states | Same screen twice — pick one |
| Empty or blank screens | Wait for the view to load first |
| Loading spinners | Transient states are not meaningful evidence |
| Test code output | Logs belong in `bug-reports/`, not as screenshots |

## Naming

`<tc-id>-<short-description>.png` — e.g., `tc001-launch-home.png`, `tc005-opml-import.png`

## How to capture

See `AI-GUIDE.md` section "Capturing Screenshots" for Espresso, UIAutomator, and adb methods.
