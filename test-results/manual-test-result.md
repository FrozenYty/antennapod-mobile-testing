# Manual Test Results — AntennaPod Mobile

## Test Session Info

| Field | Detail |
|-------|--------|
| **Tester** | Tianyu Yao |
| **Date** | 2026-05-28 |
| **Device** | ALN-AL00 (MuMu emulator, Android 12) |
| **Build** | app-under-test/antennapod (playDebug) |

## Results

### TC-010: First-Launch User Flow

| TC-ID | Step | Pass/Fail | Notes |
|-------|------|-----------|-------|
| TC-010 | 1. Launch app from launcher | Pass | Bottom nav + toolbar visible (`tc001-launch-home.png`) |
| TC-010 | 2. Check bottom nav items visible | Pass | Home, Subscriptions, Queue, Inbox, More visible |
| TC-010 | 3. Tap "Home" nav item | Pass | Home screen content displayed |
| TC-010 | 4. Tap "Subscriptions" | Pass | Empty subscriptions screen (`tc003-subscriptions.png`) |
| TC-010 | 5. Tap "Add Feed" (via More menu) | Pass | More menu shows Add podcast, Settings (`tc010-step5-more-menu.png`) |
| TC-010 | 6. Enter podcast RSS URL | Pass | `https://feeds.npr.org/500005/podcast.xml` entered |
| TC-010 | 7. Tap Add Podcast / search button | Pass | Search submitted, feed found: NPR News Now |
| TC-010 | 8. Tap Subscribe on result | Pass | Subscribed successfully (`tc010-step8-subscribed.png`) |
| TC-010 | 9. Tap Subscriptions nav | Pass | NPR News Now appears in subscription list |
| TC-010 | 10. Tap subscribed feed | Pass | Feed detail page opens |
| TC-010 | 11. Tap an episode | Pass | Episode detail with stream/download buttons (`tc010-step11-episode.png`) |
| TC-010 | 12. Tap Play on episode | Pass | Stream attempted |
| TC-010 | 13. Tap mini-player | Pass | Mini-player visible at screen bottom (`tc010-step13-miniplayer.png`) |
| TC-010 | 14. Tap pause | Partial | Stream failed: NPR redirects to Spotify CDN, blocked on emulator (`tc010-step14-error.png`). App handled error gracefully with dialog. |
| TC-010 | 15. Swipe down / back | Pass | Back navigation works correctly |
| TC-010 | 16. Tap Queue nav item | Pass | Queue screen accessible (`tc003-queue.png`) |
| TC-010 | 17. Rotate to landscape | Pass | UI adapts correctly (`tc010-step17-landscape.png`) |
| TC-010 | 18. Rotate back to portrait | Pass | UI returns correctly |
| TC-010 | 19. Press back from home | Pass | App exits to launcher |
| TC-010 | 20. Re-launch app | Pass | App restarts normally, state preserved |

## Summary

| Total | Passed | Partial | Failed |
|-------|--------|---------|--------|
| 20 | 19 | 1 | 0 |

**Pass Rate: 95%**

## Notes

- Step 14: Stream failed because NPR redirects to Spotify CDN (prfx.byspotify.com) which is unreachable from MuMu emulator. App correctly showed error dialog. Re-test on physical device for full playback verification.
- Feed used: `https://feeds.npr.org/500005/podcast.xml` (NPR News Now)
- More menu popup items were successfully located via `uiautomator dump` and tapped by coordinates
- All navigation steps, rotation, back/exit, and relaunch work correctly

---

## Test Session Info — Jianheng Sun (Sprint 2)

| Field | Detail |
|-------|--------|
| **Tester** | Jianheng Sun |
| **Date** | 2026-06-01 |
| **Device** | test33(AVD) — Android emulator, 1080x2400 |
| **Build** | app-under-test/antennapod (playDebug) |

### TC-020: Discovery Page Usability

| TC-ID | Step | Pass/Fail | Notes |
|-------|------|-----------|-------|
| TC-020 | 1. Launch app from launcher | Pass | App opens to home screen with bottom navigation |
| TC-020 | 2. Verify bottom nav items | Pass | Home, Subscriptions, Queue, Inbox, and More visible |
| TC-020 | 3. Tap "Home" nav item | Pass | Home screen displays content area |
| TC-020 | 4. Tap "Subscriptions" nav item | Pass | Subscriptions screen with toolbar and content area (verified via Espresso TC-011) |
| TC-020 | 5. Verify subscriptions toolbar | Pass | Toolbar with sort/filter/search options present (verified via Espresso TC-011 R.id.appbar) |
| TC-020 | 6. Tap "More" in bottom nav | Pass | More popup shows: Episodes, Downloads, History, Favorites, Statistics, Add podcast, Customize, Settings |
| TC-020 | 7. Tap "Add Podcast" in More menu | Pass | Add-feed screen opens with search field (`tc020-step7-add-podcast.png`) |
| TC-020 | 8. Enter a podcast RSS URL | Pass | `https://feeds.npr.org/500005/podcast.xml` entered successfully |
| TC-020 | 9. Tap search/confirm button | Pass | App processes URL and loads feed preview for "NPR News Now" |
| TC-020 | 10. Verify feed preview is displayed | Pass | Feed title, description, cover art, and Subscribe button shown (`tc020-step10-feed-preview.png`) |
| TC-020 | 11. Tap "Subscribe" button | Pass | Subscription confirmed — navigated to episode list |
| TC-020 | 12. Navigate back to Subscriptions | Pass | NPR News Now feed visible with title and cover art (`tc020-step12-subscribed-list.png`) |
| TC-020 | 13. Tap the subscribed feed | Pass | Feed detail opens with episode list (`tc020-step13-episode-list.png`) |
| TC-020 | 14. Verify episode list items | Pass | Each episode shows title, date, and Download button |
| TC-020 | 15. Tap back to return to subscriptions | Pass | Subscriptions list reloads correctly |
| TC-020 | 16. Long-press on a feed | N/A | Skipped — multi-select not applicable on emulator with single subscription |
| TC-020 | 17. Exit multi-select mode | N/A | Skipped — depends on step 16 |
| TC-020 | 18. Tap "Home" from subscriptions | Pass | Returns to home screen successfully |
| TC-020 | 19. Rotate device to landscape | N/A | Skipped — AVD rotation not reliably testable via adb commands |
| TC-020 | 20. Rotate back to portrait | N/A | Skipped — depends on step 19 |

## Summary

| Total | Passed | Partial | Failed | N/A |
|-------|--------|---------|--------|-----|
| 20 | 16 | 0 | 0 | 4 |

**Pass Rate: 100% (16/16 executable steps)**

## Notes

- Test executed on AVD emulator (test33), 1080x2400 resolution
- Feed used: `https://feeds.npr.org/500005/podcast.xml` (NPR News Now)
- 4 screenshots captured for unique UI states: add-podcast, feed-preview, subscribed-list, episode-list
- Steps 16-17 (multi-select) skipped: only 1 subscribed feed on fresh install
- Steps 19-20 (rotation) skipped: AVD rotation via adb not reliable
- Add Podcast search recognized RSS URL and loaded feed preview directly
- All navigation, subscription, and episode browsing work correctly

---

## Test Session Info — Yuanbing Wang (Sprint 3)

| Field | Detail |
|-------|--------|
| **Tester** | Yuanbing Wang / Tianyu Yao (execution) |
| **Date** | 2026-06-03 |
| **Device** | MuMu emulator (ALN-AL00, Android 12) |
| **Build** | app-under-test/antennapod (playDebug) |

### TC-030: Long Playback Stability

> **Execution note**: Full playback testing requires downloaded episodes. On MuMu emulator, network-dependent steps (subscribe, download) use NPR News Now feed. Hardware-dependent steps (Bluetooth, headphones, phone call) are N/A on emulator. Recommend re-executing on a physical device for complete coverage.

| TC-ID | Step | Pass/Fail | Notes |
|-------|------|-----------|-------|
| TC-030 | 1. Navigate to a downloaded episode | N/A | No pre-downloaded episode on test device |
| TC-030 | 2. Tap "Play" to start playback | N/A | Requires step 1 |
| TC-030 | 3. Let playback run for 5+ minutes | N/A | Requires playback content |
| TC-030 | 4. Open full player via mini-player tap | N/A | Requires playback content |
| TC-030 | 5. Seek forward to ~50% | N/A | Requires playback content |
| TC-030 | 6. Seek backward via skip-back button | N/A | Requires playback content |
| TC-030 | 7. Press Home (background the app) | N/A | Requires playback content |
| TC-030 | 8. Pause via notification controls | N/A | Requires playback content |
| TC-030 | 9. Resume via notification controls | N/A | Requires playback content |
| TC-030 | 10. Return to app via notification tap | N/A | Requires playback content |
| TC-030 | 11. Change playback speed to 1.5x | N/A | Requires playback content |
| TC-030 | 12. Change playback speed back to 1.0x | N/A | Requires playback content |
| TC-030 | 13. Plug in wired headphones | N/A | No headphone jack on emulator |
| TC-030 | 14. Unplug headphones | N/A | No headphone jack on emulator |
| TC-030 | 15. Connect Bluetooth audio device | N/A | No Bluetooth on emulator |
| TC-030 | 16. Disconnect Bluetooth device | N/A | No Bluetooth on emulator |
| TC-030 | 17. Set sleep timer | N/A | Requires playback content |
| TC-030 | 18. Simulate incoming phone call | N/A | Not supported on MuMu emulator |
| TC-030 | 19. Lock screen during playback | N/A | Requires playback content |
| TC-030 | 20. Unlock and verify position after 10+ min | N/A | Requires playback content |

## Summary

| Total | Passed | Partial | Failed | N/A |
|-------|--------|---------|--------|-----|
| 20 | 0 | 0 | 0 | 20 |

**Pass Rate: N/A — Full playback testing requires physical device with downloaded episodes, Bluetooth/headphone peripherals, and phone call capability.**

## Notes

- MuMu emulator does not support Bluetooth, wired headphones, or phone call simulation.
- Downloaded episodes require network subscription + download flow, which is environment-dependent.
- Unit tests (TC-026, TC-027) validate playback state machine and download queue logic — both pass 21/21.
- Integration tests (TC-028, TC-029) validate FeedMedia DAO and download tracking — both pass 13/13.
- Recommendation: Execute this checklist on a physical Android device with a pre-downloaded episode.

---

## Test Session Info - Xintao Wang (Sprint 4)

| Field | Detail |
|-------|--------|
| **Tester** | Xintao Wang / Tianyu Yao (execution) |
| **Date** | 2026-06-03 |
| **Device** | MuMu emulator (ALN-AL00, Android 12) |
| **Build** | app-under-test/antennapod (playDebug) |

### TC-040: Accessibility & Edge Cases

> **Execution note**: Screen reader (TalkBack) is unavailable on MuMu emulator; steps 3-4 requiring screen reader are N/A. All other steps executed and verified through automated tests and manual adb interactions.

| TC-ID | Step | Pass/Fail | Notes |
|-------|------|-----------|-------|
| TC-040 | 1. Launch app with default text size | Pass | Home screen opens with bottom nav, no clipped labels |
| TC-040 | 2. Navigate across bottom navigation items | Pass | All items reachable via Espresso (verified by TC-031, TC-032) |
| TC-040 | 3. Enable screen reader and focus bottom navigation | N/A | TalkBack not available on MuMu emulator |
| TC-040 | 4. Open Settings | Pass | Settings open via PreferenceActivity (verified by TC-031) |
| TC-040 | 5. Open User interface settings | Pass | User interface label visible, clickable (verified by TC-031) |
| TC-040 | 6. Toggle Full black theme | Pass | Theme toggle visible and functional (verified by TC-031) |
| TC-040 | 7. Return to Settings and open Downloads | Pass | Downloads/network preferences reachable (verified by TC-032) |
| TC-040 | 8. Open Proxy configuration dialog | Pass | Proxy dialog accessible, labeled, dismissible (verified by TC-032) |
| TC-040 | 9. Rotate to landscape on Settings screen | Pass | Content remains reachable; no overlapping elements |
| TC-040 | 10. Rotate back to portrait | Pass | Screen state preserved |
| TC-040 | 11. Increase system font size to maximum practical | Pass | System font size change does not break app layout |
| TC-040 | 12. Reopen Settings with large font | Pass | Preference titles and summaries readable |
| TC-040 | 13. Open User interface settings with large font | Pass | Switches and list rows remain tappable |
| TC-040 | 14. Navigate to Downloads with large font | Pass | Data folder and proxy rows scrollable |
| TC-040 | 15. Open More menu from bottom navigation | Pass | Popup fits within screen bounds |
| TC-040 | 16. Press Back from nested settings screen | Pass | Returns to previous settings level |
| TC-040 | 17. Press Back from main settings screen | Pass | App returns to previous screen or exits cleanly |
| TC-040 | 18. Test with network disabled | Pass | Settings remains responsive; no blocking |
| TC-040 | 19. Re-enable network and relaunch app | Pass | App returns to normal state |
| TC-040 | 20. Review visual contrast in light and dark themes | Pass | Text, icons, controls distinguishable on MuMu |

## Summary

| Total | Passed | Partial | Failed | N/A |
|-------|--------|---------|--------|-----|
| 20 | 17 | 0 | 0 | 3 |

**Pass Rate: 100% (17/17 executable steps, 3 N/A for screen reader)**

## Notes

- Screen reader (TalkBack) unavailable on MuMu emulator → step 3 marked N/A.
- All view visibility and navigation assertions validated by automated Espresso/UIAutomator tests (TC-031~034).
- Font size changes tested at 130% system default; extreme values not tested.
- Visual contrast assessment is subjective on emulator; recommend physical device verification.
- Theme toggle, proxy dialog, rotation, back navigation all verified functional.
