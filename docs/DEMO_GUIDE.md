# SealCoffee Final Demonstration Guide

## Demo at a glance

- Recommended duration: 12–15 minutes
- Device used during final verification: Pixel 7 AVD, Android 17 / API 37
- Customer: `mika.santos@gmail.com` / `Coffee123!`
- Employee: `carlo.staff@sealcoffee.com` / `Staff123!`
- Connectivity: none required; all accounts, menu data, carts, and orders are stored in the app's local Room/SQLite database.

For the most predictable presentation, start from a freshly installed application. The database then seeds the menu, both demo accounts, and one representative completed order for Mika.

## Preparation

1. Open the project in Android Studio using JDK 17.
2. Start a phone emulator with API 24 or newer.
3. Uninstall any existing copy of `com.mobdeve.s15.group4.sealcoffee` if a completely repeatable seed state is required.
4. Run the `app` configuration.
5. Keep these credentials available:
   - Customer: `mika.santos@gmail.com` / `Coffee123!`
   - Employee: `carlo.staff@sealcoffee.com` / `Staff123!`

## Demonstration script

| Time | Demonstration | What to show and say | Requirement proved |
|---|---|---|---|
| 0:00–1:00 | Registration and secure login | From the login screen, open registration. Show the date picker and field validation, then either register a new customer or return and sign in as Mika. Explain that email uniqueness and credentials are checked against SQLite and passwords are stored as salted PBKDF2-derived hashes. | Customer registration, authentication, secure storage, persistent session |
| 1:00–2:15 | Browse and filter the menu | Show All, Coffee, Non-Coffee, Snacks, and Desserts. Point out product images, categories, prices, and availability. | Database-backed menu and exact category filters |
| 2:15–2:45 | Gesture 1: menu long press | Long-press a menu card. Show the haptic quick-preview dialog containing the product image, name, and description. Dismiss it, then normally tap the same card to demonstrate that full details are a separate interaction. | Approved customer menu long-press gesture |
| 2:45–4:15 | Configure and persist a cart item | On a drink, select a size and add-on, change the quantity, and show the live recalculated total. Add it to the cart. Add another configuration if time permits. Explain that the configuration is merged only when it is identical. | Product details, central pricing, persistent cart |
| 4:15–4:45 | Cart quantity | Increase and decrease a quantity. Point out that subtotal and total update from the same centavo-based pricing rules. | Persistent quantity updates and consistent totals |
| 4:45–5:30 | Gesture 2: cart swipe right | Swipe a cart row right. Edit its size, add-ons, or quantity and save. Show the updated row and total. | Approved cart edit gesture and SQLite persistence |
| 5:30–6:00 | Gesture 3: cart swipe left | Swipe another row left. Show that it is removed, then tap Undo in the Snackbar and verify it returns. | Approved cart removal gesture and reversible deletion |
| 6:00–6:45 | Checkout | Tap Place Order and confirm. Show the generated `SC-...` order number and the new Pending order under Active Orders. Explain that order creation, immutable item snapshots, and cart clearing occur in one Room transaction. | Atomic SQLite checkout and current order status |
| 6:45–7:15 | Persistence | Leave the app with Home or force-stop and reopen it. Show that the signed-in destination and order remain. | SQLite and session persistence across process restart |
| 7:15–8:00 | Switch roles | Log out, verify Back cannot reopen the customer screen, and sign in with the employee account. Show the dashboard's live total, active, completed, and delayed counts. | Logout/back-stack protection, role isolation, dashboard summaries |
| 8:00–9:00 | Incoming order details | Open Incoming Orders, select the newly created order, and show the customer identity, contact data, timestamp, item customisations, line totals, and status selector. Apply Preparing manually. | Employee queues, complete order details, manual persistent status update |
| 9:00–9:30 | Gesture 4: employee swipe left | In an applicable order list, swipe the order left and show that it moves to Delayed. Open it and manually return it to Preparing to continue the normal flow. | Approved employee delayed gesture and manual recovery |
| 9:30–10:15 | Gesture 5: employee swipe right | Swipe right to move Preparing to Ready for Pickup, then swipe right again to move it to Completed. Show that a completed order cannot advance further and that the dashboard/list membership updates. | Approved sequential order gesture and canonical status progression |
| 10:15–11:30 | Persistent menu management | Open Manage Menu. Add a product using a fixed category, price, description, ingredients, availability, and bundled image. Edit its name. Toggle it unavailable. Optionally archive/remove it after the customer verification below. | Employee menu add, edit, availability, and safe removal |
| 11:30–12:15 | Customer response to employee changes | Log out and sign in as the customer. Show the new or edited product, verify the unavailable label and disabled ordering action, and then show the completed order under Past Orders with full details. | Shared local database, availability enforcement, customer history |
| 12:15–13:00 | Profile and customer isolation | Open Profile and show the signed-in customer's real stored details. Explain that cart, profile, active orders, and history queries are all scoped by the session's customer ID. | Real profile and customer data isolation |
| 13:00–14:00 | Customer history search | Return to the employee account. Open Customer Order History, search by the demonstrated customer's name or email, and open the result. | Employee customer-history search and order inspection |
| 14:00–15:00 | Persistence conclusion | Restart the app once more and show that it restores the valid employee destination and retains the menu/order changes. Log out to leave the application at a clean login screen. | Persistent Room data, session restoration, complete logout |

## Five required gestures checklist

- Customer menu item long press: quick product preview.
- Customer cart swipe left: remove, with Snackbar Undo.
- Customer cart swipe right: open persistent item editor.
- Employee order swipe left: set status to Delayed.
- Employee order swipe right: advance `Pending -> Preparing -> Ready for Pickup -> Completed`.

## Suggested narration points

- Room is an Android SQLite abstraction; this remains a local SQLite application and does not use Firebase or a remote backend.
- Both roles read and write the same database on the same installed app/device.
- “Live” order updates mean observable database updates on that installation. The app does not synchronise between different phones.
- Money is stored as integer centavos, avoiding floating-point rounding differences.
- Order items retain name, price, and configuration snapshots so later menu edits do not alter history.
- Authentication roles come from stored user records. An email domain never grants employee access.

## Reset instructions

For a completely fresh seed:

```bash
adb uninstall com.mobdeve.s15.group4.sealcoffee
./gradlew installDebug
```

Alternatively, use Android Settings > Apps > SealCoffee > Storage & cache > Clear storage, then launch the app again. Either action deletes the local demonstration data and causes first-run database seeding on the next launch.

Do not clear storage during the persistence segment; use Home, force-stop, or a normal app restart so the database and private session preferences remain intact.
