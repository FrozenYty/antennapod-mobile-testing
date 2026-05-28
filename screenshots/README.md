# Screenshots

Store test evidence screenshots here. Reference them from bug reports, manual test results,
or test case documentation.

## Quality Principle — Better None Than Junk

**Better none than junk.** Every screenshot in this directory must serve a clear purpose.
Review each screenshot before committing — if you can't explain why it's valuable in one
sentence, delete it.

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
