# SealCoffee Testing Report

## Final status

All mandatory final Gradle checks passed on 2026-07-25. The complete JVM suite passed 9/9, the connected Room/UI suite passed 7/7, the debug APK assembled, and Android lint completed with zero errors.

## Environment

| Component | Verified value |
|---|---|
| Host | macOS 26.5.2, Apple Silicon |
| JDK | JetBrains Runtime/OpenJDK 17.0.14 |
| Gradle wrapper | 9.4.1 |
| Android Gradle Plugin | 9.2.1 |
| compile SDK | 36.1 |
| target/min SDK | 36 / 24 |
| Connected target | Pixel_7 AVD |
| Reported device model | `sdk_gphone16k_arm64` |
| Android release/API | Android 17 / API 37 |
| App ID | `com.mobdeve.s15.group4.sealcoffee` |

The emulator was booted headlessly from the existing Pixel_7 AVD. The connected Gradle runner reported it as `Pixel_7(AVD) - 17`.

## Mandatory final commands

These commands were run individually from the repository root after the implementation and test fixes were complete:

| Command | Actual final result |
|---|---|
| `./gradlew clean` | **BUILD SUCCESSFUL** in 665 ms; 1 task executed |
| `./gradlew testDebugUnitTest` | **BUILD SUCCESSFUL** in 4 s; 9 tests, 0 failures/errors/skips |
| `./gradlew assembleDebug` | **BUILD SUCCESSFUL** in 2 s; debug APK produced |
| `./gradlew lintDebug` | **BUILD SUCCESSFUL** in 12 s; 0 errors |
| `./gradlew connectedDebugAndroidTest` | **BUILD SUCCESSFUL** in 12 s; 7 tests, 0 failures/errors/skips |

Final artifacts/reports:

- APK: `app/build/outputs/apk/debug/app-debug.apk` (approximately 12 MB)
- JVM XML: `app/build/test-results/testDebugUnitTest/`
- JVM HTML: `app/build/reports/tests/testDebugUnitTest/`
- Connected XML: `app/build/outputs/androidTest-results/connected/debug/`
- Connected HTML: `app/build/reports/androidTests/connected/debug/`
- Lint text/HTML: `app/build/reports/lint-results-debug.txt` and `.html`

## Automated test inventory

### JVM tests: 9

| Test class | Cases | Coverage |
|---|---:|---|
| `PasswordHasherTest` | 1 | Random salts, correct password verification, wrong password rejection |
| `RegistrationValidatorTest` | 2 | Valid registration/email normalisation; every invalid field returns a field-specific error |
| `PricingCalculatorTest` | 3 | Size/add-on/quantity totals; non-coffee add-on rules; food canonicalisation and summed order total |
| `OrderStatusTest` | 2 | Entire preparation progression; completed/delayed stop behavior; stored label/name parsing |
| `StringListCodecTest` | 1 | Deterministic escaped ingredient/add-on round trip |

Result from XML: 9 tests, 0 failures, 0 errors, 0 skipped.

### Connected instrumentation tests: 7

`RepositoryInstrumentedTest` uses an in-memory Room database:

1. Registration accepts one account, rejects case-insensitive duplicate email, authenticates the right password, and rejects the wrong password.
2. Cart add/merge/update/remove persists and remains isolated between two customers.
3. Checkout creates Pending order/snapshots, calculates ₱420.00 correctly, clears only one customer's cart, and isolates order queries.
4. Menu create/availability/archive changes DAO queries as expected.

`UiSmokeTest` uses the installed app and real Activities:

5. Login renders email, password, and submit controls while signed out.
6. A seeded customer opens the Room-backed menu and changes the Coffee filter.
7. A customer opens Product Details, adds to the database cart, places an order through the confirmation dialog, reaches Orders, then an employee opens that order and applies Preparing; the final database status is asserted.

Result from connected XML: 7 tests, 0 failures, 0 errors, 0 skipped, 7.648 seconds of test time.

## Iterative command history and fixes

The following meaningful verification commands were also run during implementation:

| Command/check | Result and resolution |
|---|---|
| Initial `./gradlew assembleDebug` | Passed before refactoring, establishing a working prototype baseline. |
| `./gradlew compileDebugKotlin` | Passed after the Room/domain/customer/employee integration. |
| Initial `./gradlew testDebugUnitTest assembleDebug` | One pricing assertion failed because the test expected ₱470.00 while the documented rules correctly calculate ₱480.00. The incorrect test expectation was fixed; production pricing was unchanged. |
| `./gradlew compileDebugAndroidTestKotlin` | Passed after adding in-memory Room tests. |
| First `./gradlew connectedDebugAndroidTest` | Passed 6 tests before the full Activity checkout/status smoke test was added. |
| Repeated `./gradlew lintDebug` | Always completed; app warnings were reduced by moving bitmaps to `drawable-nodpi`, deleting unused resources, externalising strings, adding content descriptions, using KTX, and configuring backups/themes. |
| `./gradlew lintDebug testDebugUnitTest assembleDebug` | Passed after lint cleanup. |
| `./gradlew testDebugUnitTest assembleDebug installDebug` | Passed and installed the manual-walkthrough build. |
| `./gradlew compileDebugAndroidTestKotlin testDebugUnitTest assembleDebug` | First run found a test-only wrong resource name (`R.string.orders`); corrected to `orders_navbar_name`, then passed. |
| First expanded `./gradlew connectedDebugAndroidTest` | Failed two UI tests: the first inherited the manual session and the new smoke test tried `scrollTo` during emulator animations. Added a `@Before` session reset, disabled test animations, and used explicit ScrollView swipes. |
| Second expanded `./gradlew connectedDebugAndroidTest` | One UI assertion ambiguously matched product title and Snackbar. Changed it to the exact formatted Snackbar message. |
| Third and final-precheck connected runs | Passed all 7 tests. |
| Restricted metadata probe `./gradlew --version \| sed ...` | Not a build check; the restricted shell could not create a Gradle wrapper `.lck` outside the workspace. The wrapper version was confirmed from `gradle-wrapper.properties`, and every real Gradle task above ran successfully. |

No build or test failure was ignored; each reproducible project/test failure was fixed and rerun.

## Lint

Final result: **BUILD SUCCESSFUL**, zero errors.

Remaining informational warnings:

| Lint category | Count | Assessment |
|---|---:|---|
| `UseCompoundDrawables` | 4 | Customer bottom-nav icon/text pairs intentionally remain separate to support selected/unselected icon and colour changes. |
| `Overdraw` | 2 | The included bottom navigation and preview dialog intentionally paint surfaces distinct from their host window. |
| `GradleDependency` | 3 | Newer versions exist; verified stable compatible versions are intentionally pinned. |
| `NewerVersionAvailable` | 2 | Coroutines update notices only. |
| `AndroidGradlePluginVersion` | 2 | Wrapper/AGP update notices only. |
| `OldTargetApi` | 1 | API 37 exists in the 2026 SDK; the project intentionally retains tested target SDK 36 while being executed on API 37. |

Resolved findings included hard-coded layout text, missing image descriptions, unused resources, densityless bitmap placement, `SharedPreferences` KTX usage, unnecessary overdraw, unsafe backup defaults, and adapter full-refresh calls.

## Manual emulator walkthrough

All requested steps were executed against the installed debug build on the Pixel_7 AVD. The test customer created during the walkthrough was:

`codex.tester@example.com` / `Strong123!`

| # | Test | Expected | Actual |
|---:|---|---|---|
| 1 | Fresh install/seed | Demo users, menu, and history are in SQLite | Passed; login/menu and `SC-DEMO-0001` available |
| 2 | Register customer | Valid account persists and auto-signs in | Passed; Codex Tester appeared in menu/profile |
| 3 | Logout and login again | Stored credentials work | Passed |
| 4 | Filter and long-press | Coffee filter; separate preview/haptic | Passed; Signature Latte preview showed image/name/description |
| 5 | Add multiple configurations | Distinct configurations persist | Passed; large + extra shot + oat quantity 2 and regular configuration were listed |
| 6 | Change quantity | Persistent quantity/total change | Passed |
| 7 | Swipe right edit | Product editor opens; changes persist | Passed; row became Large + Caramel, quantity 2, ₱410.00 |
| 8 | Swipe left delete and Undo | Row removes and can be restored | Passed; removal Snackbar displayed and Undo restored row |
| 9 | Place order | Transaction/confirmation/cart clear | Passed; created `SC-260725-134716-49AE`, ₱410.00 |
| 10 | Customer active status | New order is Pending | Passed |
| 11 | Employee login | Stored employee routes to dashboard | Passed with Carlo credentials |
| 12 | Dashboard/incoming | Counts and new order reflect database | Passed; Total 2, Active 1, Completed 1, Delayed 0; incoming row showed Codex/order/amount |
| 13 | Details/manual update | Full details; Preparing persists | Passed |
| 14 | Employee swipes | Left Delayed; recover manually; right advances to Completed | Passed through Delayed → Preparing → Ready for Pickup → Completed |
| 15 | Customer status/history | Active clears; order appears Completed in own Past | Passed; history showed number, time, `2× Signature Latte`, ₱410.00 |
| 16 | Menu management | Add/edit/disable/archive and customer response | Passed; added Codex Cocoa, renamed Codex Cocoa Plus, marked unavailable, customer saw disabled `Unavailable`, archived, then customer list no longer showed it |
| 17 | Employee customer search | Name/email search isolates matching records | Passed; searching `Codex` showed only the Codex order |
| 18 | Restart/session persistence | Process restart returns to valid role and keeps changes | Passed; force-stop/relaunch restored employee dashboard and counts; database state remained |

Additional navigation/security result: after employee logout, pressing Back showed the Pixel launcher rather than an authenticated Activity.

## Crash and edge review

- Empty active and incoming queues were displayed without crashes.
- An unavailable product opened safely and its Add button was disabled.
- Customer order detail access is user-ID checked.
- Missing product/order IDs take safe Toast/finish paths.
- Completed order swipe/manual controls are blocked.
- Checkout rejects empty and newly unavailable carts inside the transaction.
- No production references to `DummyData`, `allowMainThreadQueries`, `TODO`, or `FIXME` remain.

## Limitations of testing

- The connected suite ran on one Pixel 7 API 37 AVD, not the entire API 24–37/device-size matrix.
- No remote/multi-device synchronization was tested because the approved app is deliberately local-only.
- Real payment, camera, cloud image storage, and push notifications are outside the approved scope.
