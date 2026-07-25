# SealCoffee

SealCoffee is an offline Android coffee-ordering application for the MOBDEVE S15 Group 4 final project. A customer can create an account, customise products, keep a persistent cart, place an order, and track it through completion. An employee uses the same installed application and local database to manage orders, products, availability, and customer purchase history.

The final implementation keeps the prototype's Kotlin, XML/View, Activity, RecyclerView, navy-and-cream branding, Inter font resources, bundled product art, and application ID:

`com.mobdeve.s15.group4.sealcoffee`

## Implemented features

### Customer

- Field-validated registration with a date picker, normalised unique email, and securely hashed password
- Credential-based login and restart-safe customer session
- SQLite-backed menu with All, Coffee, Non-Coffee, Snacks, and Desserts filters
- Visible product availability and intentional empty states
- Long-press product preview with image, name, description, and haptic feedback
- Product details with ingredients, drink/food-specific controls, size, add-ons, notes, quantity, and live price
- Persistent, user-scoped cart with quantity changes and consistent line/order totals
- Swipe left to remove with Snackbar Undo
- Swipe right to edit a cart configuration
- Atomic checkout that snapshots order items and clears only the ordering customer's cart
- Observable active orders and completed history, with full order details
- Database-backed profile and secure logout with a cleared task stack

### Employee

- Secure login through a seeded `EMPLOYEE` account; no email-domain shortcut
- Live counts for total, active, completed, and delayed orders
- Observable Pending, Active, Completed, and Delayed queues
- Complete order/customer/item/total details
- Manual persisted status updates
- Swipe left to mark an order Delayed
- Swipe right through Pending → Preparing → Ready for Pickup → Completed
- Persistent menu add/edit/archive and availability management
- Searchable customer order history by customer name, email, or order number
- Secure logout and role-guarded Activities

## Approved MOBDEVE services

### Local SQLite storage

Room 2.8.4 is the SQLite abstraction. `AppDatabase` is a process singleton with five related tables, foreign keys, useful indexes, observable `Flow` queries, and exported schema version 1. The repository owns multi-table transactions, including checkout. No production screen uses an in-memory dummy list.

Passwords are never persisted in plaintext. Registration and seed accounts use a random 16-byte salt and PBKDF2-HMAC-SHA256-compatible derivation with 120,000 iterations and a 256-bit result. Session preferences store only user ID and role.

### Touch gestures

`ItemTouchHelper` implements four directional swipes with labelled colour feedback:

- Customer cart left: Remove
- Customer cart right: Edit
- Employee order left: Delay
- Employee order right: Advance

The fifth gesture is a long press on a customer menu item for quick preview. Cart deletion is recoverable with Undo, and cancelled edits reset the swiped row.

## Technology stack

- Kotlin with the Android Gradle Plugin's built-in Kotlin support
- XML layouts, Android Views, Activities, RecyclerView/ListAdapter/DiffUtil
- Android Gradle Plugin 9.2.1
- Gradle 9.4.1
- JDK 17
- compile SDK 36.1, target SDK 36, minimum SDK 24
- Room 2.8.4 with KSP 2.3.4
- Kotlin coroutines 1.10.2, lifecycle-runtime-ktx 2.10.0
- Material Components 1.14.0
- JUnit 4, AndroidX Test, Espresso, and in-memory Room tests

## Prerequisites

- Android Studio capable of importing AGP 9.2.1 projects
- JDK 17; the verified environment used JetBrains Runtime 17.0.14
- Android SDK Platform 36.1 and Build Tools resolved by Gradle
- An emulator or device on API 24 or later

Do not add or commit `local.properties`. Android Studio creates it for the local SDK path.

## Setup and run

1. Clone or extract the project.
2. Open the repository root in Android Studio.
3. Select JDK 17 for Gradle.
4. Allow Gradle sync to complete.
5. Select an API 24+ device.
6. Run the `app` configuration.

From the repository root, the verified command-line flow is:

```bash
./gradlew clean
./gradlew testDebugUnitTest
./gradlew assembleDebug
./gradlew lintDebug
./gradlew connectedDebugAndroidTest
```

The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

## Demo accounts

| Role | Email | Password |
|---|---|---|
| Customer | `mika.santos@gmail.com` | `Coffee123!` |
| Employee | `carlo.staff@sealcoffee.com` | `Staff123!` |

The credentials are case-insensitive for email and case-sensitive for password.

## First-run seed

Room's first-create callback inserts:

- Mika Santos as a customer
- Carlo Dela Cruz as an employee
- Nine bundled menu products using offline images
- One completed Mika order (`SC-DEMO-0001`) for history demonstrations

The seed runs only when `sealcoffee.db` is first created. App restarts do not reseed or overwrite changes.

## Architecture and source map

```text
app/src/main/java/com/mobdeve/s15/group4/sealcoffee/
├── *Activity.kt / *Adapter.kt      Screens, observable rendering, gestures
├── AuthNavigation.kt               Role guards, session routing, logout
├── SessionManager.kt               Private preference session
├── SealCoffeeApplication.kt        Application-scoped dependencies
├── UiCatalog.kt                    Images, money, dates, status colours
├── data/
│   ├── SealCoffeeRepository.kt     Business operations and transactions
│   ├── StringListCodec.kt          Stable list storage encoding
│   └── local/
│       ├── AppDatabase.kt          Room singleton and first-run seed
│       ├── Entities.kt             SQLite table models
│       ├── Daos.kt                 Observable/scoped SQL operations
│       └── Relations.kt            Cart/order projections
└── domain/
    ├── AppConstants.kt             Roles, categories, statuses, options
    ├── PasswordHasher.kt           Salted password derivation
    ├── PricingCalculator.kt        Central centavo pricing rules
    └── RegistrationValidator.kt    Registration validation
```

The UI depends on the repository; the repository coordinates DAOs and transactions; Room owns SQLite. `Flow` queries are collected with `repeatOnLifecycle`, so returning screens receive fresh data instead of stale Intent snapshots.

## Local-only live status limitation

“Live” status means observable changes within the same SQLite database on the same installed app/device. SealCoffee has no remote server, account synchronisation, push notification, or multi-device employee console. This is intentional and matches the approved SQLite-only service scope.

## Troubleshooting

- **Gradle reports an incompatible Java version:** choose JDK 17 in Android Studio's Gradle settings.
- **SDK 36.1 is missing:** install it through SDK Manager, then sync again.
- **Connected tests say no devices:** boot an emulator and confirm it appears in `adb devices`.
- **A demo account or menu is missing after development changes:** uninstall the app or clear app data to recreate the version-1 seed.
- **Dependency download fails:** confirm Gradle can access Google Maven and Maven Central.
- **A product cannot be ordered:** employees can keep unavailable items visible; its product button intentionally reads `Unavailable`.
- **A cart row blocks checkout:** remove any item that became unavailable after it was added.

## Documentation

- [Implementation report](docs/IMPLEMENTATION_REPORT.md)
- [Requirements traceability](docs/REQUIREMENTS_TRACEABILITY.md)
- [Database schema](docs/DATABASE_SCHEMA.md)
- [Testing report](docs/TESTING_REPORT.md)
- [Class/video demo guide](docs/DEMO_GUIDE.md)
- [Changelog](docs/CHANGELOG.md)
- [Final completion report](docs/FINAL_COMPLETION_REPORT.md)

