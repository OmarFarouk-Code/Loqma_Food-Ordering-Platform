# Team workflow

1. Never push directly to `main`. Work on a branch.
2. Branch names: `<team>/<short-description>`, e.g. `driver/available-orders`, `auth/register-api`, `customer/cart-page`.
3. Before starting work: `git checkout main && git pull`, then `git checkout -b <branch>`.
4. Commit small and often with clear messages: `Add accept order endpoint`.
5. Push your branch and open a Pull Request into `main`. At least one teammate reviews it.
6. Stay inside your own team's folders to avoid merge conflicts:

| Team | Screens | Backend package (`backend/.../com/loqma/`) | Frontend (`frontend/pages/`) |
|---|---|---|---|
| 1. Owner | Menu, Account, Orders, Order Details, Order History, Hours | `owner` | `owner` |
| 2. Admin | Dashboard, Approvals, Promotions, Orders Monitor, Profile | `admin` | `admin` |
| 3. Driver + Auth | Active Delivery, Available Orders, Earnings, Register, Login, Notifications | `driver`, `auth`, `notification` | `driver`, `auth`, `notifications` |
| 4. Customer | Home, Restaurant List, Restaurant Details, Cart, Checkout, Order Tracking, Order History, Promotions | `customer` | `customer` |

Shared code (`common/`, `frontend/assets/`) is edited rarely: tell the group chat first.

## Entities several teams need

User, Restaurant, MenuItem and Order are used by more than one team. Agree on one owner for each
before anyone writes them, and everyone else only reads/uses them (do not create duplicate versions).
