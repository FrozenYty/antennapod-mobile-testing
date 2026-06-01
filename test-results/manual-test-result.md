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
| **Tester** | Yuanbing Wang |
| **Date** | 2026-06-01 |
| **Device** | To be filled after execution |
| **Build** | app-under-test/antennapod (playDebug) |

### TC-030: Long Playback Stability

| TC-ID | Step | Pass/Fail | Notes |
|-------|------|-----------|-------|
| TC-030 | 1. Navigate to a downloaded episode | | |
| TC-030 | 2. Tap "Play" to start playback | | |
| TC-030 | 3. Let playback run for 5+ minutes | | |
| TC-030 | 4. Open full player via mini-player tap | | |
| TC-030 | 5. Seek forward to ~50% | | |
| TC-030 | 6. Seek backward via skip-back button | | |
| TC-030 | 7. Press Home (background the app) | | |
| TC-030 | 8. Pause via notification controls | | |
| TC-030 | 9. Resume via notification controls | | |
| TC-030 | 10. Return to app via notification tap | | |
| TC-030 | 11. Change playback speed to 1.5x | | |
| TC-030 | 12. Change playback speed back to 1.0x | | |
| TC-030 | 13. Plug in wired headphones | | |
| TC-030 | 14. Unplug headphones | | |
| TC-030 | 15. Connect Bluetooth audio device | | |
| TC-030 | 16. Disconnect Bluetooth device | | |
| TC-030 | 17. Set sleep timer | | |
| TC-030 | 18. Simulate incoming phone call | | |
| TC-030 | 19. Lock screen during playback | | |
| TC-030 | 20. Unlock and verify position after 10+ min | | |

## Summary

| Total | Passed | Partial | Failed |
|-------|--------|---------|--------|
| 20 | | | |

**Pass Rate: —**

## Notes

- To be filled after manual test execution.
- Use a downloaded episode (not streaming) for consistent results.
- For Bluetooth tests (steps 15-16), ensure a Bluetooth audio device is paired.
- For call simulation (step 18), use `adb shell am start -a android.intent.action.CALL -d tel:1234567890` or MuMu's phone call simulation.
- Monitor logcat: `adb logcat -d | grep -E "PlaybackService|MediaPlayer|AudioTrack" | tail -30`
- Take screenshots of critical moments and save with TC-030 prefix.

---

## Test Session Info - Member Four (Sprint 4)

| Field | Detail |
|-------|--------|
| **Tester** | Member Four |
| **Date** | 2026-05-31 |
| **Device** | To be filled after execution |
| **Build** | app-under-test/antennapod (playDebug) |

### TC-040: Accessibility & Edge Cases

| TC-ID | Step | Pass/Fail | Notes |
|-------|------|-----------|-------|
| TC-040 | 1. Launch app with default text size | | |
| TC-040 | 2. Navigate across bottom navigation items | | |
| TC-040 | 3. Enable screen reader and focus bottom navigation | | |
| TC-040 | 4. Open Settings | | |
| TC-040 | 5. Open User interface settings | | |
| TC-040 | 6. Toggle Full black theme | | |
| TC-040 | 7. Return to Settings and open Downloads | | |
| TC-040 | 8. Open Proxy configuration dialog | | |
| TC-040 | 9. Rotate to landscape on Settings screen | | |
| TC-040 | 10. Rotate back to portrait | | |
| TC-040 | 11. Increase system font size to maximum practical value | | |
| TC-040 | 12. Reopen Settings with large font | | |
| TC-040 | 13. Open User interface settings with large font | | |
| TC-040 | 14. Navigate to Downloads with large font | | |
| TC-040 | 15. Open More menu from bottom navigation | | |
| TC-040 | 16. Press Back from nested settings screen | | |
| TC-040 | 17. Press Back from main settings screen | | |
| TC-040 | 18. Test with network disabled | | |
| TC-040 | 19. Re-enable network and relaunch app | | |
| TC-040 | 20. Review visual contrast in light and dark themes | | |

## Summary

| Total | Passed | Partial | Failed |
|-------|--------|---------|--------|
| 20 | | | |

**Pass Rate: -**

## Notes

- To be filled after manual test execution.
- Record accessibility services used, font/display size settings, and Android version.
- Save screenshots for accessibility issues or key evidence with a TC-040 prefix.
