# SealCoffee Final Implementation Report

## Scope and completion

This implementation converts the supplied interactive prototype into a working, offline coffee-ordering application while retaining its Kotlin/XML/Activity approach, package ID, bundled graphics, Inter font setup, and navy/cream identity. The approved SQLite and touch-gesture services are implemented as production behavior rather than demonstrations.

Implementation baseline: branch `1.2`, commit `7da5eff`.

Verified implementation milestone: `1c4909ecedb464386685649da7a04f823a7eefac`

## Original prototype audit

The code audit confirmed the issues identified in the task:

- `DummyData` and mutable model lists supplied nearly all runtime menu, cart, order, user, and dashboard data.
- Login accepted non-empty values and selected employee access by an email-domain rule.
- Registration did not create a persistent user or validate the complete form.
- Raw password fields were never securely transformed or checked against storage.
- No durable session linked screens to an authenticated user.
- Product “Add to Cart” ended at feedback; cart data was reconstructed from seeds.
- Checkout did not atomically create an order and clear the correct cart.
- Customer orders/history/profile showed prototype data independent of the signed-in identity.
- Employee status Apply and swipe changes were temporary or inconsistently represented.
- Menu changes existed only in a screen-local list.
- Customer history navigation was present but not backed by working persisted data.
- Activity snapshots became stale after edits.
- Logout could leave authenticated Activities in the task.
- Example unit/instrumentation tests asserted no project behavior.
- Redundant `MainActivity`, employee login, cart edit dialog, model classes, and navigation resources were no longer part of a coherent flow.

All of those runtime paths were replaced or removed.

## Implemented architecture

### Application and dependency ownership

`SealCoffeeApplication` lazily owns one `AppDatabase`, one `SealCoffeeRepository`, and one `SessionManager`. Activities obtain these through the `sealApp` extension. This avoids static mutable data while keeping dependency ownership understandable for a course-scale app.

### Domain layer

`AppConstants.kt` centrally defines:

- `UserRole`
- the four exact `MenuCategory` values
- all five `OrderStatus` values and normal progression
- canonical product size and add-on labels

`PricingCalculator` normalises category-specific choices and performs all centavo calculations. `RegistrationValidator` owns deterministic field validation. `PasswordHasher` owns salted credential derivation and verification.

### Persistence and repository

Room provides five entities, four DAOs, relation projections, foreign keys, indexes, observable queries, and exported schema version 1. `SealCoffeeRepository` is the only multi-DAO business-operation coordinator.

The repository:

- validates and creates accounts
- authenticates password hashes
- merges identical cart configurations
- scopes every cart/customer query to user ID
- performs transactional checkout with immutable snapshots
- exposes filtered customer/employee order flows
- persists order status changes
- persists menu CRUD/availability/archive operations

### UI and lifecycle

Activities retain the prototype's View-based structure. RecyclerViews use `ListAdapter` and `DiffUtil`. Room `Flow` values are collected with `repeatOnLifecycle(Lifecycle.State.STARTED)`, so lists and counts refresh when the Activity resumes and when the database changes.

`AuthNavigation` guards role-specific entry, restores destinations, and clears the task on logout. Shared order details permit employee access to every order but verify a customer owns the requested order ID.

## Feature-by-feature implementation

### Registration and authentication

- Birthday is selected through `DatePickerDialog` with a current-date maximum.
- First/last name, ISO birthday, email, contact, and password return field-specific errors.
- Email is trimmed/lowercased and backed by a case-insensitive unique index.
- Password policy requires at least eight characters with upper, lower, digit, and symbol.
- Passwords use a unique salt and 120,000-round HMAC-SHA256 derivation.
- Registration catches duplicate-email constraint races.
- Login checks stored credentials and stored role; it never interprets the email domain.
- Submit buttons disable while work is in flight to prevent double taps.

### Sessions, role isolation, and navigation

- Private `SharedPreferences` stores only active user ID and role.
- `OnboardingActivity` verifies the stored user still exists before restart routing.
- Customer and employee Activities call role guards before inflating protected content.
- Customer access to shared order details is checked against `customer_id`.
- Logout clears preferences and launches login with `NEW_TASK | CLEAR_TASK`.
- Manual Back verification after logout returned to the launcher rather than the employee screen.

### Customer menu and product configuration

- The menu is an observable Room query; archived products are excluded while unavailable products remain clearly labelled.
- Filter labels derive from the canonical category enum.
- Empty categories display an intentional message.
- Normal tap opens full details. Long press supplies haptic feedback and a separate preview dialog.
- Details display the image mapping, category, description, decoded ingredients, applicable sizes/add-ons, notes, quantity, and live total.
- Food hides drink-specific controls and is normalised to `Single`.
- Add to Cart writes or merges a database row. Editing updates or merges the resulting configuration.

### Cart and checkout

- Cart queries include only the authenticated customer.
- Quantity controls persist changes and enforce the minimum.
- Line totals and cart totals call the same pricing source as product details/checkout.
- Items that later become unavailable are identified, and checkout is disabled.
- Swipe-left removes the owned row and exposes an Undo action.
- Swipe-right resets the row before opening the editor.
- Checkout is a single Room transaction. It revalidates availability, calculates totals, inserts the order and snapshots, and clears only that customer cart.
- Confirmation identifies offline pickup/payment behavior and routes to active orders.

### Customer orders, history, and profile

- Active orders include Pending, Preparing, Ready for Pickup, and Delayed.
- Past orders include Completed only, newest first.
- Status, item summary, date/time, order number, and total are visible in the list.
- Shared detail shows all snapshots, customisations, notes, line amounts, subtotal, and total.
- Profile collects the authenticated `UserEntity` rather than rendering Mika for every account.

### Employee dashboard and queues

- One aggregate SQL Flow returns total, active preparation, completed, and delayed counts.
- Incoming observes Pending.
- Active observes Pending, Preparing, and Ready for Pickup.
- Completed and Delayed observe their exact canonical states.
- Each row includes order number, customer, time, item/quantity summary, amount, and status.
- Empty queues render a meaningful message.

### Employee status operations and gestures

- Order detail includes customer name, email/contact, timestamp, pickup/payment labels, immutable items, and totals.
- Manual status Apply persists through the DAO and reloads details.
- Delayed orders can be manually recovered to another status.
- Completed orders expose final status but disable further controls.
- Swipe-left persists Delayed.
- Swipe-right uses the central sequential progression and stops at Completed.
- A completed or delayed-right invalid action resets the row and explains the restriction.
- Observable filtered lists move a changed row to its correct queue automatically.

### Menu management and customer history

- Employee menu observes every non-archived product, including unavailable entries.
- The editor validates name, fixed category, description, ingredients, exact-decimal positive price, and availability.
- New items receive the bundled `custom` image key.
- Edit and availability update the persisted row.
- Remove asks for confirmation, then archives/disables the product instead of destroying history.
- Customer menu reacts to all of these changes through its Flow.
- Customer history performs a case-insensitive SQL search on customer name, email, or order number and opens full details.

## Key decisions and assumptions

1. **Room fulfils the approved SQLite service.** It provides safer SQL, transactions, relations, and observable queries without adding a remote backend.
2. **Centavos are authoritative.** `Long` values eliminate binary floating-point disagreement. Decimal employee input is converted exactly with `BigDecimal`.
3. **Completed is final.** Employee swipe and manual UI prevent changes after completion. Delayed remains manually recoverable.
4. **Delayed is customer-active but not dashboard preparation-active.** Customers must still track it; employees see it in the dedicated delayed queue and count.
5. **Soft removal is the normal product deletion.** This avoids broken cart/order relationships and retains historical snapshots.
6. **Bundled/default product images are offline.** A new item uses `img_custom`; camera/cloud storage is outside approved scope.
7. **Pickup and pay-at-counter are labels, not payment processing.** This matches the requested non-payment scope.
8. **List values use an escaped codec.** The bounded ingredients/add-ons sets did not justify additional relation tables; the encoding is deterministic and tested.
9. **A customer Orders screen consolidates active and past views.** Both remain reachable through visible tabs while avoiding redundant screens.

## Before and after

| Prototype behavior | Final behavior |
|---|---|
| Mutable `DummyData` list | Room tables and DAO Flow queries |
| Any non-empty login | Hash verification against stored account |
| Employee by email suffix | Employee by persisted `role` |
| Signup navigates only | Validated, hashed, unique database account |
| No session identity | User-ID/role session with restart validation |
| Add-to-cart Toast | Insert/merge scoped cart transaction |
| Seed cart returns after navigation | Persistent user cart |
| Checkout animation only | Atomic order/snapshot creation and cart clear |
| Static history/profile | Authenticated-user Room queries |
| Temporary order status | Canonical persisted status |
| Temporary menu edits | Shared database menu CRUD |
| Unwired customer history | Searchable all-customer purchase records |
| Logout leaves task history | Session clear and task clear |
| Example tests | 9 JVM plus 7 connected behavior tests |

## Important file inventory

### Created

- `SealCoffeeApplication.kt` — application-scoped database/repository/session container.
- `AuthNavigation.kt` — role guards, authenticated routing, logout task clearing.
- `SessionManager.kt` — minimal private preference session.
- `UiCatalog.kt` — image mapping, money/date formatting, initials, status colours.
- `data/local/AppDatabase.kt` — singleton database and first-run seed.
- `data/local/Entities.kt` — users, menu, cart, orders, order-item tables.
- `data/local/Daos.kt` — scoped mutations, filtered Flow queries, dashboard aggregate.
- `data/local/Relations.kt` — cart/order projections and counts.
- `data/SealCoffeeRepository.kt` — account/cart/checkout/order/menu orchestration.
- `data/StringListCodec.kt` — escaped list encoding.
- `domain/AppConstants.kt` — roles/categories/statuses/options.
- `domain/PasswordHasher.kt` — salted credential derivation/verification.
- `domain/PricingCalculator.kt` — central integer-money rules.
- `domain/RegistrationValidator.kt` — form validation and email normalisation.
- `app/schemas/.../1.json` — exported Room schema.
- Five JVM test classes — hashing, registration, pricing, status, codec.
- `RepositoryInstrumentedTest.kt` — in-memory Room/repository coverage.
- `UiSmokeTest.kt` — login/filter and end-to-end Activity flow.
- `drawable-nodpi/` — density-independent placement for bundled prototype bitmaps.

### Substantially modified

- Gradle version catalog, project/app build scripts — KSP, Room, lifecycle, RecyclerView, coroutines, Room test support, animation-safe UI tests, schema export.
- `AndroidManifest.xml` — Application class, backup exclusion, role-screen declarations, input resize, launcher/session route.
- `OnboardingActivity`, `LoginActivity`, `SignUpActivity` — restart routing and real accounts.
- `CustomerMenuActivity`, `MenuItemAdapter` — observable filters and long preview.
- `ProductDetailsActivity` — database product/customisation/pricing/cart write.
- `CartActivity`, `CartItemAdapter` — scoped observable cart, quantity, swipes, Undo, checkout.
- `CustomerOrdersActivity`, `CustomerOrdersAdapter` — active/past database tabs.
- `ProfileActivity` — real customer data/logout.
- `EmployeeDashboardActivity` — live counts and complete navigation.
- `IncomingOrdersActivity`, `ActiveOrdersActivity`, `CompletedOrdersActivity`, `DelayedOrdersActivity` — canonical filtered queues.
- `EmployeeOrderListBinder`, `EmployeeOrderAdapter` — shared observable list and swipe persistence.
- `EmployeeOrderDetailsActivity` — ownership-safe details and status Apply.
- `ManageMenuActivity`, `ManageMenuAdapter` — persistent validated CRUD.
- `OrderHistoryActivity`, `OrderHistoryAdapter` — searchable customer transactions.
- Customer/product/cart/order/menu/dialog/item layouts — complete states, controls, accessibility, scrolling, and dynamic content.
- `strings.xml`, themes/styles/colours — functional text, branded states, backup-safe themes, lint cleanup.
- Backup/data extraction XML — explicitly excludes credentials/database/session from platform backup.

### Deleted or consolidated

- `data/DummyData.kt` and the six legacy data model files — removed live mock source.
- `MainActivity.kt` and `activity_main.xml` — obsolete launcher shell.
- `activity_employee_login.xml` — duplicate role-specific login; one secure login now routes by stored role.
- `dialog_edit_cart_item.xml` — cart editing reuses the complete product details flow.
- `EmployeeCustomerHistoryAdapter.kt` — replaced by the database relation-based history adapter.
- `CustomerUiFormat.kt` — formatting consolidated in `UiCatalog.kt`.
- `include_employee_nav.xml` — unused prototype resource.
- Example unit and instrumented tests — replaced with behavior tests.
- Unused drawables/styles/colours and duplicate launcher foreground bitmaps — removed after reference/lint audit.

## Security, persistence, lifecycle, and errors

- Raw passwords are never persisted or placed in session preferences.
- Backups/device transfer are disabled for app-private account/order data.
- Authentication errors do not reveal which credential was wrong.
- Database failures return visible, concise feedback rather than crashing or being silently swallowed.
- Forms and checkout buttons disable during asynchronous submission.
- Repository rules are rechecked inside transactions; UI disabled state is not treated as a security boundary.
- Foreign keys prevent orphaned live records, while snapshots protect receipts.
- Role guards run before protected layouts are used.
- Missing menu/order IDs and empty query results show feedback/empty states and finish safely where necessary.
- Lifecycle-aware collection prevents stopped Activities from continuously updating and refreshes them on return.
