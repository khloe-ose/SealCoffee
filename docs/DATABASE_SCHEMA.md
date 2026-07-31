# SealCoffee Database Schema

## Overview

SealCoffee uses Room 2.8.4 over SQLite. `AppDatabase` is a lazily created, application-context singleton named `sealcoffee.db`. Schema version 1 is exported to:

`app/schemas/com.mobdeve.s15.group4.sealcoffee.data.local.AppDatabase/1.json`

No DAO permits main-thread queries. Activities collect `Flow` data through lifecycle-aware coroutines, and the repository uses suspend functions for mutations.

## Entity relationship diagram

```mermaid
erDiagram
    USERS ||--o{ CART_ITEMS : owns
    USERS ||--o{ ORDERS : places
    MENU_ITEMS ||--o{ CART_ITEMS : configures
    ORDERS ||--|{ ORDER_ITEMS : snapshots
    MENU_ITEMS o|--o{ ORDER_ITEMS : originally_referenced

    USERS {
        INTEGER id PK
        TEXT full_name
        TEXT birth_date
        TEXT email UK
        TEXT contact_number
        TEXT password_hash
        TEXT password_salt
        TEXT role
        INTEGER created_at
    }
    MENU_ITEMS {
        INTEGER id PK
        TEXT seed_key
        TEXT name
        TEXT category
        TEXT description
        TEXT ingredients_csv
        INTEGER base_price_centavos
        TEXT image_key
        INTEGER featured
        INTEGER available
        INTEGER archived
    }
    CART_ITEMS {
        INTEGER id PK
        INTEGER customer_id FK
        INTEGER menu_item_id FK
        INTEGER quantity
        TEXT size
        TEXT add_ons_csv
        TEXT notes
    }
    ORDERS {
        INTEGER id PK
        TEXT order_number UK
        INTEGER customer_id FK
        TEXT status
        INTEGER placed_at
        INTEGER subtotal_centavos
        INTEGER total_centavos
        TEXT order_type
        TEXT payment_label
    }
    ORDER_ITEMS {
        INTEGER id PK
        INTEGER order_id FK
        INTEGER menu_item_id FK_NULLABLE
        TEXT item_name_snapshot
        TEXT category_snapshot
        INTEGER quantity
        TEXT size_snapshot
        TEXT add_ons_snapshot_csv
        TEXT notes_snapshot
        INTEGER unit_price_centavos
        INTEGER line_total_centavos
    }
```

## Tables

### `users`

| Field | SQLite/Room type | Rules |
|---|---|---|
| `id` | INTEGER / Long | Auto-generated primary key |
| `full_name` | TEXT / String | Trimmed first and last names combined |
| `birth_date` | TEXT / String | Validated ISO `yyyy-MM-dd` |
| `email` | TEXT / String | Unique index with `NOCASE` collation; app normalises lowercase/trim |
| `contact_number` | TEXT / String | Validated contact value |
| `password_hash` | TEXT / String | Hex-encoded 256-bit derived key |
| `password_salt` | TEXT / String | Hex-encoded random 16-byte salt |
| `role` | TEXT / String | Canonical `CUSTOMER` or `EMPLOYEE` |
| `created_at` | INTEGER / Long | Epoch milliseconds |

The application catches a uniqueness constraint race and returns the same duplicate-email result as its pre-insert lookup.

### `menu_items`

| Field | Type | Rules |
|---|---|---|
| `id` | INTEGER / Long | Auto-generated primary key |
| `seed_key` | TEXT nullable | Stable origin key for seeded products |
| `name` | TEXT | Employee-validated display name |
| `category` | TEXT | Coffee, Non-Coffee, Snacks, or Desserts |
| `description` | TEXT | Employee-validated description |
| `ingredients_csv` | TEXT | Escaped list encoding |
| `base_price_centavos` | INTEGER / Long | Positive whole centavos |
| `image_key` | TEXT | Offline bundled resource mapping |
| `featured` | INTEGER / Boolean | Customer menu ordering |
| `available` | INTEGER / Boolean | Visible but non-orderable when false |
| `archived` | INTEGER / Boolean | Hidden from both active menu lists when true |

Indexes cover name, category, and the archived/available pair.

### `cart_items`

| Field | Type | Rules |
|---|---|---|
| `id` | INTEGER / Long | Auto-generated primary key |
| `customer_id` | INTEGER / Long | FK → `users.id`, cascade on user deletion |
| `menu_item_id` | INTEGER / Long | FK → `menu_items.id`, no destructive cascade |
| `quantity` | INTEGER / Int | Repository enforces 1–99 in UI |
| `size` | TEXT | Canonical Regular, Large, or Single |
| `add_ons_csv` | TEXT | Normalised, de-duplicated add-on list |
| `notes` | TEXT | Trimmed, limited to 200 characters |

A unique composite index on customer, menu item, size, add-ons, and notes makes a configuration identity. Adding the same configuration merges quantity. Every read/update/delete is scoped with `customer_id`.

### `orders`

| Field | Type | Rules |
|---|---|---|
| `id` | INTEGER / Long | Auto-generated primary key |
| `order_number` | TEXT | Unique `SC-yyMMdd-HHmmss-XXXX` display number |
| `customer_id` | INTEGER / Long | FK → `users.id`, no deletion cascade |
| `status` | TEXT | Canonical enum storage name |
| `placed_at` | INTEGER / Long | Epoch milliseconds |
| `subtotal_centavos` | INTEGER / Long | Checkout-calculated subtotal |
| `total_centavos` | INTEGER / Long | Stored transaction total |
| `order_type` | TEXT | Offline label, default `Pickup` |
| `payment_label` | TEXT | Offline label, default `Pay at counter` |

Indexes support customer history, status queues, timestamp ordering, and unique order-number lookup.

### `order_items`

| Field | Type | Rules |
|---|---|---|
| `id` | INTEGER / Long | Auto-generated primary key |
| `order_id` | INTEGER / Long | FK → `orders.id`, cascade with order |
| `menu_item_id` | INTEGER nullable / Long? | FK → `menu_items.id`, set null if product is physically deleted |
| `item_name_snapshot` | TEXT | Name at checkout |
| `category_snapshot` | TEXT | Category at checkout |
| `quantity` | INTEGER / Int | Purchased quantity |
| `size_snapshot` | TEXT | Size at checkout |
| `add_ons_snapshot_csv` | TEXT | Add-ons at checkout |
| `notes_snapshot` | TEXT | Notes at checkout |
| `unit_price_centavos` | INTEGER / Long | Configured unit price at checkout |
| `line_total_centavos` | INTEGER / Long | Unit price × quantity |

Order items are immutable snapshots. Editing, repricing, disabling, or archiving a menu product therefore cannot rewrite a historical receipt. The nullable product reference is only an optional link back to the current menu record.

## Relations and projections

- `CartItemWithMenu` embeds a cart row and relates its current menu item.
- `OrderWithDetails` embeds an order and relates its customer and immutable item rows.
- `DashboardCounts` maps one aggregate query for all dashboard counts.

Room `@Transaction` relation queries prevent partially assembled projections.

## Money and pricing

All stored monetary values are `Long` centavos. No persisted or business-rule calculation uses `Double`.

`PricingCalculator` is the single pricing source:

- Large drink: +2,500 centavos
- Extra espresso shot: +2,000 centavos, Coffee only
- Oat milk: +3,000 centavos
- Caramel drizzle: +1,500 centavos
- Food: canonical `Single` size and no drink add-ons

It normalises options, calculates unit price, uses overflow-safe multiplication for line totals, and overflow-safe addition for order totals. UI formatting converts the final integer amount to Philippine peso text with `BigDecimal`.

## Password storage

`PasswordHasher` uses:

- cryptographically secure random 16-byte salt per account
- HMAC-SHA256 pseudo-random function
- 120,000 PBKDF2 iterations
- 32-byte derived key
- constant-time `MessageDigest.isEqual` verification

Raw passwords are not stored in the database or session. Temporary character/byte arrays are cleared where practical.

## List encoding

Ingredients and add-ons use `StringListCodec`, an escaped delimiter encoding. Backslash and pipe characters are escaped before storage and decoded deterministically. This keeps the small offline schema maintainable without introducing several low-value join tables, and the codec has JVM tests.

## Transaction boundaries

Room `withTransaction` protects:

- add/merge cart configuration
- edit/merge cart configuration
- checkout
- sequential order advancement

Checkout re-reads the customer cart, rejects empty/unavailable contents, calculates all totals, inserts the order, inserts snapshots, and clears only that customer's cart. Any exception rolls back every step.

## Seed data

`SeedCallback.onCreate` inserts two hashed demo users, nine bundled menu products, and one completed Mika order. It runs once for a newly created database and never replaces user changes on restart.

## Version and migration strategy

The current schema version is 1 and its JSON is committed for review and future migration tests. Destructive migration fallback is intentionally not enabled. Any future entity change must:

1. increment the Room version,
2. add an explicit `Migration`,
3. export the new JSON schema, and
4. add a migration test before release.
