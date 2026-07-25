# Requirements Traceability Matrix

Status legend: **Complete** means the behavior is implemented in production code, persisted where required, and covered by the cited automated or manual verification.

| Proposal/course requirement | Role | Screen or interaction | Implementation classes/layouts/DAO methods | Database tables | Verification | Status |
|---|---|---|---|---|---|---|
| Register full name, birthday, email, contact, password | Customer | Create Account form/date picker | `SignUpActivity`, `RegistrationValidator`, `PasswordHasher`, `activity_sign_up.xml`, `UserDao.insert` | `users` | `RegistrationValidatorTest`; `RepositoryInstrumentedTest`; walkthrough 2 | Complete |
| Reject blank/invalid/future/weak/duplicate values with field errors | Customer | Registration validation | `RegistrationValidator.validate`, `SignUpActivity`, case-insensitive user index | `users` | Unit invalid-fields test; duplicate repository test; manual form flow | Complete |
| Normalise unique email and securely hash password | Both | Registration/authentication | `normaliseEmail`, `PasswordHasher`, `UserEntity`, `UserDao.findByEmail` | `users` | `PasswordHasherTest`; duplicate/auth instrumentation | Complete |
| Authenticate stored credentials without role inference | Both | Login | `LoginActivity`, `SealCoffeeRepository.authenticate`, stored `UserRole` | `users` | Authentication instrumentation; login UI smoke; walkthrough 3/11 | Complete |
| Prevent empty/double login submissions | Both | Login button | Inline `EditText.error`; disabled submit during coroutine | `users` | UI smoke/manual incorrect/valid login checks | Complete |
| Persist and restore valid session | Both | Launcher/restart | `SessionManager`, `OnboardingActivity`, `AuthNavigation.routeAuthenticated` | `users` plus private preferences | Walkthrough 18 force-stop/relaunch | Complete |
| Load menu from SQLite | Customer | Menu list | `CustomerMenuActivity`, `MenuDao.observeCustomerMenu`, `MenuItemAdapter` | `menu_items` | Menu instrumentation; walkthrough 1/4/16 | Complete |
| Exact All/Coffee/Non-Coffee/Snacks/Desserts filters | Customer | Menu filter chips | `MenuCategory`, `CustomerMenuActivity.applyFilter` | `menu_items` | Menu filter UI smoke; walkthrough 4 | Complete |
| Show product availability; unavailable cannot be added | Customer | Menu/details | `MenuItemAdapter`, `ProductDetailsActivity`, repository add/update revalidation | `menu_items`, `cart_items` | Menu availability instrumentation; walkthrough 16 | Complete |
| Menu empty state | Customer | Empty filtered category | `menuEmptyText`, `CustomerMenuActivity.applyFilter` | `menu_items` | Empty-list-safe code inspection and instrumentation query | Complete |
| Long-press quick preview, distinct from tap | Customer | Menu row long press | `MenuItemAdapter` haptic long listener; `dialog_product_preview.xml` | `menu_items` | Walkthrough 4 | Complete |
| Product image/name/category/description/ingredients | Customer | Product Details | `ProductDetailsActivity`, `StringListCodec`, `ImageCatalog` | `menu_items` | Walkthrough 5; UI checkout smoke | Complete |
| Drink sizes/category-appropriate add-ons; food hides irrelevant controls | Customer | Product customisation | `MenuCategory.isDrink`, `ProductOptions`, `ProductDetailsActivity` | `menu_items`, `cart_items` | `PricingCalculatorTest`; walkthrough 5 | Complete |
| Live final price for size/add-ons/quantity | Customer | Product Details | `PricingCalculator`, change listeners, `formatMoney` | centavo fields | 3 pricing tests; walkthrough 5/7 | Complete |
| Persistent Add to Cart, merge identical configuration | Customer | Product action | `SealCoffeeRepository.addToCart`, `CartDao.findMatching/insert/update` | `cart_items`, `menu_items` | Cart merge instrumentation; Activity checkout smoke; walkthrough 5 | Complete |
| Customer-scoped cart survives navigation/restart | Customer | Cart | `CartDao.observeCart(customerId)`, `SessionManager` | `cart_items` | Cart scope instrumentation; walkthrough 3/5/18 | Complete |
| Cart images/config/quantity/line/subtotal/total | Customer | Cart list | `CartActivity`, `CartItemAdapter`, central pricing | `cart_items`, `menu_items` | Pricing/cart tests; walkthrough 5/6 | Complete |
| Persist quantity; minimum one | Customer | +/- controls | `updateCartQuantity`; UI 1–99 guard | `cart_items` | Cart update instrumentation; walkthrough 6 | Complete |
| Handle later-unavailable cart item | Customer | Warning/checkout disabled | `CartActivity.renderCart`; checkout transaction revalidation | `cart_items`, `menu_items` | Menu availability test; code transaction test; walkthrough 16 | Complete |
| Empty cart and disabled Place Order | Customer | Cart empty state | `cartEmptyText`, `renderCart` | `cart_items` | Empty result handling; post-checkout walkthrough 9 | Complete |
| Cart swipe left removes | Customer | Left `ItemTouchHelper` gesture | `CartActivity.attachCartGestures`, `removeCartItem` | `cart_items` | Cart remove instrumentation; walkthrough 8 | Complete |
| Cart removal is recoverable | Customer | Snackbar Undo | `restoreCartItem` action | `cart_items` | Walkthrough 8 performed Undo and observed restored row | Complete |
| Cart swipe right edits and cancellation resets row | Customer | Right gesture → Product Details | `notifyItemChanged`, `openEditor`, `updateCartConfiguration` | `cart_items` | Cart update instrumentation; walkthrough 7 | Complete |
| Place order as one transaction | Customer | Confirmation/Place Order | `SealCoffeeRepository.checkout`, `Room.withTransaction` | `cart_items`, `orders`, `order_items`, `menu_items` | Checkout instrumentation; Activity checkout smoke; walkthrough 9 | Complete |
| Validate non-empty/orderable cart | Customer | Checkout | Transaction reads cart and availability | `cart_items`, `menu_items` | Checkout test plus unavailable manual check | Complete |
| Initial Pending, immutable snapshots, totals, unique number | Customer | Checkout result | `OrderEntity`, `OrderItemEntity`, `generateOrderNumber`, pricing | `orders`, `order_items` | Checkout assertions; walkthrough order `SC-260725-134716-49AE` | Complete |
| Clear only ordering customer's cart | Customer | Checkout commit | `CartDao.clearCustomerCart(customerId)` | `cart_items` | Two-customer checkout instrumentation | Complete |
| Order confirmation and active order navigation | Customer | Toast → Orders/Active | `CartActivity.performCheckout`, `CustomerOrdersActivity` | `orders` | Activity checkout smoke; walkthrough 9/10 | Complete |
| Active Pending/Preparing/Ready/Delayed status | Customer | Orders Active tab | `OrderStatus.active`, `observeCustomerOrders` | `orders`, `order_items` | Order status unit tests; walkthrough 10/15 | Complete |
| Completed automatically appears in history | Customer | Orders Past tab | Completed Flow query | `orders`, `order_items` | Walkthrough 14/15 | Complete |
| History newest first with number/date/status/summary/total | Customer | Orders Past | `OrderDao.observeCustomerOrders ORDER BY`, `CustomerOrdersAdapter` | `orders`, `order_items` | Walkthrough 15 | Complete |
| Open customer-owned order details only | Customer | History row | `EmployeeOrderDetailsActivity` ownership check | `orders`, `order_items`, `users` | Customer isolation instrumentation; walkthrough 15 | Complete |
| Real signed-in profile | Customer | Profile | `ProfileActivity`, `UserDao.observeById` | `users` | Walkthrough 2/15 profile showed Codex Tester values | Complete |
| Customer logout clears session/back stack | Customer | Profile Logout | `AuthNavigation.logout` | private preferences | Walkthrough logout/login and Back-stack check | Complete |
| Employee account seeded and securely authenticated | Employee | Shared login | `SeedCallback`, `PasswordHasher`, stored role | `users` | Auth instrumentation; walkthrough 11 | Complete |
| Customers cannot reach employee screens | Both | Direct protected Activity | `AuthNavigation.requireRole` on every employee Activity | `users`/session | Role-guard code audit; shared detail ownership check | Complete |
| Live dashboard total/active/completed/delayed counts | Employee | Dashboard cards | `OrderDao.observeDashboardCounts`, `DashboardCounts` | `orders` | Walkthrough 12/14; Flow query instrumentation compile/run | Complete |
| Incoming Pending newest first with customer/time/items/amount/status | Employee | Incoming Orders | `IncomingOrdersActivity`, `EmployeeOrderListBinder/Adapter` | `orders`, `order_items`, `users` | Walkthrough 12 | Complete |
| Active/Completed/Delayed filtered lists and empty states | Employee | Four queues | Queue Activities, central status sets, `observeOrdersByStatuses` | `orders`, `order_items` | Status tests; walkthrough 12/14 | Complete |
| Full employee order details | Employee | Order Details | `EmployeeOrderDetailsActivity`, Room relation | `orders`, `order_items`, `users` | Walkthrough 13 | Complete |
| Manual persisted valid status update | Employee | Status radios/Apply | `updateOrderStatus`, details reload | `orders` | Activity UI smoke; walkthrough 13 | Complete |
| Employee swipe left delays | Employee | Left `ItemTouchHelper` gesture | `EmployeeOrderListBinder`, `updateOrderStatus(DELAYED)` | `orders` | Walkthrough 14 | Complete |
| Employee swipe right sequentially advances | Employee | Right gesture | `advanceOrder`, `OrderStatus.nextPreparationStatus` | `orders` | `OrderStatusTest`; walkthrough 14 | Complete |
| Stop at Completed; Delayed manually recoverable | Employee | Gesture/manual status | Gesture guard; completed controls disabled; manual delayed selection | `orders` | Status unit test; walkthrough 14 | Complete |
| Add menu item | Employee | Manage Menu editor | `ManageMenuActivity`, `saveMenuItem`, `MenuDao.insert` | `menu_items` | Menu CRUD instrumentation; walkthrough 16 | Complete |
| Edit menu item | Employee | Edit action/editor | `saveMenuItem`, `MenuDao.update` | `menu_items` | Menu CRUD test; walkthrough renamed Codex Cocoa Plus | Complete |
| Remove/archive safely | Employee | Remove confirmation | `archiveMenuItem`, `MenuDao.archive` | `menu_items`, retained `order_items` snapshots | Menu archive test; walkthrough 16 | Complete |
| Persist availability | Employee | Mark Available/Unavailable | `setAvailability`, observable employee/customer queries | `menu_items` | Menu test; walkthrough 16 customer saw disabled button | Complete |
| Fixed category and offline default image | Employee | Editor Spinner | `MenuCategory.labels`, `imageKey = custom`, `ImageCatalog` | `menu_items` | Menu CRUD instrumentation; walkthrough 16 | Complete |
| Search customer order history | Employee | Customer Order History search | `OrderHistoryActivity`, `OrderDao.observeCustomerHistory` | `orders`, `order_items`, `users` | Walkthrough 17 searched Codex | Complete |
| Employee logout clears session/task | Employee | Dashboard Logout | `AuthNavigation.logout` | private preferences | Walkthrough 11/15 role switch and Back check | Complete |
| Shared local SQLite source of truth | Both | Every production data screen | `AppDatabase`, DAOs, repository, no `DummyData` | all five tables | Repository tests; source scan; restart walkthrough | Complete |
| Foreign keys/indexes/transactions | Both | Persistence layer | Room annotations/schema JSON/transactions | all five tables | In-memory Room tests; schema inspection | Complete |
| Integer money and shared formatter | Both | Product/cart/order/menu | `Long` centavos, `PricingCalculator`, `formatMoney` | money columns | Pricing/checkout tests; manual totals | Complete |
| Canonical roles/categories/statuses | Both | Domain and queries | `AppConstants.kt` | role/category/status columns | Order status tests; source scan | Complete |
| Observable non-stale lists/counts | Both | Menu/cart/orders/dashboard | DAO `Flow`, `repeatOnLifecycle`, ListAdapter | all relevant tables | Connected tests; cross-role walkthrough | Complete |
| No plaintext passwords/static mutable live lists | Both | Security/data | `PasswordHasher`; deleted `DummyData` | `users` | Hash unit test; production source scan | Complete |
| Input/errors/duplicate action safety | Both | Forms/buttons/repository | Inline errors, Results, disabled actions, transaction rechecks | relevant tables | Unit/instrumented/UI/manual tests | Complete |
| Empty/missing ID safety | Both | Lists/details | Empty views, null checks, access checks, safe finish | relevant tables | Code paths exercised by empty queues and tests | Complete |
| Five approved gestures documented/demonstrable | Both | 4 swipes + long press | `CartActivity`, `EmployeeOrderListBinder`, `MenuItemAdapter` | cart/orders/menu | Manual walkthrough and `DEMO_GUIDE.md` | Complete |

## Verification summary

- JVM: 9 tests, 0 failed.
- Connected Room/UI: 7 tests, 0 failed on Pixel 7 AVD, Android 17/API 37.
- Final `assembleDebug`: successful.
- Final `lintDebug`: successful with zero errors.
- Manual requirement walkthrough: all 18 requested steps passed.

See [TESTING_REPORT.md](TESTING_REPORT.md) for exact commands and observations.

