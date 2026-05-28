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
