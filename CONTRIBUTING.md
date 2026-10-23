# Contributing to Loqma

How our team works together on GitHub. Read this once before writing any code.

The idea is simple: **nobody works directly on a shared branch. Everything goes through a Pull Request (PR).**
A PR is a request on GitHub that says "please review my changes and merge them into this branch."

---

## 1. Branches

```
main                       final, always working (updated at milestones only)
 └─ dev                    all 4 teams' work comes together here
     ├─ team/owner
     ├─ team/admin
     ├─ team/driver-auth
     └─ team/customer
          └─ owner/hours-page     your personal task branch (example)
```

Work always moves upward: **task branch → team branch → dev → main.**

| Branch          | Who touches it  | How code gets in                               |
| --------------- | --------------- | ---------------------------------------------- |
| `main`          | Nobody directly | PR from `dev` at milestones (demo, submission) |
| `dev`           | Nobody directly | PR from a team branch                          |
| `team/<team>`   | Nobody directly | PR from a task branch                          |
| `<team>/<task>` | You             | Commit and push freely                         |

**Task branch names:** `<team>/<short-description>`, lowercase, words joined by dashes.
Examples: `owner/hours-page`, `auth/register-api`, `customer/cart-page`, `admin/approvals-endpoint`.

---

## 2. Daily routine

```bash
git checkout team/<your-team>      # 1. go to your team branch
git pull                           # 2. get teammates' latest work
git checkout -b owner/hours-page   # 3. create your task branch

# ... write code ...

git add .                          # 4. stage your changes
git commit -m "Add hours page layout"
git push -u origin owner/hours-page   # 5. upload your branch
```

Then open a Pull Request (see section 3).

Tips:

- Run `git status` often. It shows which branch you are on and what changed.
- Always check you are on the right branch **before** you start coding.
- Commit small and often. Small commits are easier to review and easier to undo.

---

## 3. Opening a Pull Request

1. Go to the repo on GitHub and click **Compare & pull request** (it appears after you push).
2. Set **base** to your **team branch** (for example `team/owner`), **not** `main`.
3. Fill in the template: what it does, which page or module, how to test it.
4. Ask at least one teammate to review. They read the changes, comment, and approve.
5. After approval, click **Merge**, then **Delete branch**.

**Before opening a PR, check:**

- The backend starts and the frontend page works.
- No passwords or secrets are in your changes.
- You only edited files in your own team's folders (see section 6).

---

## 4. Sending your team's work to dev

Do this **at least once a week** so teams don't drift apart.

```bash
git checkout team/<your-team>
git pull origin dev        # bring dev's latest INTO your team branch first
# fix any conflicts here, then run the project and make sure it works
git push
```

Then open a PR from `team/<your-team>` into `dev`. One person from a **different team** should review it.

Why pull `dev` first? You fix conflicts on your own side, so `dev` only ever receives code that already works with everyone else's.

`dev` goes into `main` only at milestones, by the team leader.

---

## 5. Commit messages

Write what the commit does, starting with a verb.

| Good                                | Bad       |
| ----------------------------------- | --------- |
| `Add accept order endpoint`         | `fix`     |
| `Create register form validation`   | `stuff`   |
| `Fix cart total when quantity is 0` | `changes` |

---

## 6. Who owns what

Stay inside your own team's folders. If you only edit your own files, Git has nothing to conflict on.

| Team             | Screens                                                                                              | Backend package (`backend/src/main/java/com/loqma/`) | Frontend (`frontend/pages/`)      |
| ---------------- | ---------------------------------------------------------------------------------------------------- | ---------------------------------------------------- | --------------------------------- |
| 1. Owner         | Menu, Account, Orders, Order Details, Order History, Hours                                           | `owner`                                              | `owner`                           |
| 2. Admin         | Dashboard, Approvals, Promotions, Orders Monitor, Profile                                            | `admin`                                              | `admin`                           |
| 3. Driver + Auth | Active Delivery, Available Orders, Earnings, Register, Login, Notifications                          | `driver`, `auth`, `notification`                     | `driver`, `auth`, `notifications` |
| 4. Customer      | Home, Restaurant List, Restaurant Details, Cart, Checkout, Order Tracking, Order History, Promotions | `customer`                                           | `customer`                        |

**Shared files** (`common/`, `frontend/assets/`, `pom.xml`, `application.properties`): change them rarely.
Tell the group chat **before** editing them, and keep the change small.

Inside each backend package the layers are:
`controller` (HTTP requests) · `service` (business rules) · `repository` (database access) · `entity` (database tables) · `dto` (data shapes for the API).

---

## 7. Shared entities

`User`, `Restaurant`, `MenuItem` and `Order` are needed by more than one team.

- Agree on **one team to write each of them**, before anyone starts.
- Everyone else **uses** them and does not create their own copy.
- If you need a new field on a shared entity, ask the owning team (or the group chat) instead of editing it yourself.

Each team has its own controller for shared things like orders (for example `CustomerOrderController`, `OwnerOrderController`) so nobody edits the same file.

---

## 8. Never commit these

- Passwords, keys or tokens
- `application-local.properties` (your personal database settings, already git-ignored)
- `backend/target/` or IDE folders like `.idea/` (already git-ignored)

If you commit a password by mistake, tell the team leader straight away. Deleting it in a later commit is **not** enough, because it stays in the history and the password must be changed.

---

## 9. Merge conflicts

A conflict happens when two people changed the same lines. It is normal, not a disaster.

1. Open the file. Git marks the problem like this:
   ```
   <<<<<<< HEAD
   your version
   =======
   their version
   >>>>>>> other-branch
   ```
2. Decide what the final code should be, then delete the `<<<<<<<`, `=======` and `>>>>>>>` lines.
3. Save, then:
   ```bash
   git add .
   git commit -m "Resolve merge conflict"
   ```
4. If you are unsure which version is right, **ask the person who wrote the other one.**

---

## 10. Quick rules

1. Never push to `main` or `dev` directly.
2. Always branch from your team branch, and double-check which branch you are on.
3. Pull before you start working.
4. Small commits, clear messages.
5. Every change goes through a PR with a review.
6. Stay in your own folders.
7. Ask in the group chat when stuck, and include the full error message.
