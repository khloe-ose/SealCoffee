# Changelog

## Final implementation — 2026-07-25

### Added

- Room/SQLite entities, relations, indexed DAOs, database singleton, first-run seeding, repository, and transactional checkout.
- Secure customer registration and shared role-based authentication using salted PBKDF2-HMAC-SHA256 password derivation.
- Private session persistence with authenticated route restoration and role guards.
- Database-backed customer menu, exact category filtering, quick preview, product customisation, cart, checkout, active orders, history, order details, profile, and logout.
- Database-backed employee dashboard counts, order queues, full order details, manual status controls, menu management, customer-history search, and logout.
- Five approved touch gestures: menu long press, cart swipe left/right, and employee-order swipe left/right.
- Unit tests for security, registration validation, pricing, status rules, and list conversion.
- Room repository instrumentation tests and end-to-end Activity smoke tests.
- Empty, unavailable, disabled, loading, confirmation, and error states throughout the main workflows.

### Changed

- Replaced mutable `DummyData` runtime state with one shared observable Room source of truth.
- Replaced permissive and email-domain-based login with stored credential and role verification.
- Replaced floating-point persisted prices with integer centavos and central pricing rules.
- Replaced snapshot Intent lists with lifecycle-aware `Flow` collection from SQLite.
- Consolidated order state strings under canonical roles, categories, and order-status definitions.
- Refined the navy/cream XML/View interface while retaining the project's branding, images, Inter fonts, Activity, and RecyclerView approach.
- Moved menu image resources to density-neutral drawables so the database's image-key mapping is stable.

### Fixed

- Registration now validates each input, enforces unique normalised email, hashes the password, and persists the new account.
- Cart configurations, quantities, edits, removals, and Undo now survive navigation and process restart.
- Checkout now creates an order and item snapshots and clears only the current customer's cart atomically.
- Employee status and menu changes now persist and refresh every affected customer and employee screen.
- Customer profile and order queries no longer expose seeded Mika data to every account.
- Logout now clears the session and authenticated task stack.
- Unavailable or archived products cannot be newly ordered; affected cart rows are identified and block checkout safely.
- Completed orders cannot silently progress beyond the terminal state.
- Prototype-only layouts, legacy models, example tests, redundant navigation artifacts, and dead runtime data were removed.
- Android lint correctness/accessibility issues found during final implementation were resolved; remaining findings are informational.

### Tested

- `./gradlew clean`
- `./gradlew testDebugUnitTest` — 9 tests passed.
- `./gradlew assembleDebug`
- `./gradlew lintDebug` — zero errors.
- `./gradlew connectedDebugAndroidTest` — 7 tests passed on Pixel 7 AVD, Android 17 / API 37.
- Completed the 18-step manual customer/employee persistence and gesture walkthrough documented in `TESTING_REPORT.md`.

### Documentation

- Added the root setup and architecture `README.md`.
- Added implementation, requirements traceability, database schema, testing, demonstration, changelog, and final completion reports.
