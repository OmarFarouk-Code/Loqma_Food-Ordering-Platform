# Loqma

*Skip the cooking, we've got your back.*

Online food ordering and delivery platform for customers, restaurant owners, drivers and admins.

**Stack:** HTML / CSS / Bootstrap / vanilla JavaScript (frontend) · Java Spring Boot (backend) · MySQL (database)

---

## 1. Install these first (one time)

| Tool | Version | Notes |
|---|---|---|
| Git | any recent | https://git-scm.com |
| JDK | **21** | Must be 21. Check with `java -version` |
| Maven | 3.9+ | Check with `mvn -version`. Download from https://maven.apache.org and add its `bin` folder to PATH |
| MySQL | 8.x | Remember the **root password** you set during install |
| VS Code | latest | https://code.visualstudio.com |

**VS Code extensions** (Extensions tab, search by name):
- Extension Pack for Java
- Spring Boot Extension Pack
- Live Server
- Lombok Annotations Support for VS Code

You do **not** need Postman, Node, Sass, Tailwind or any bundler.
Java libraries (Spring, MySQL driver, ...) download automatically the first time you run the backend.
Bootstrap loads from a link in the HTML, nothing to install.

---

## 2. Get the project

```bash
git clone <REPO-URL-HERE>
cd loqma
git checkout dev
```

Open the `loqma` folder in VS Code (File > Open Folder).

---

## 3. Run the backend

1. Create your personal settings file by copying the template:

   **Windows (Command Prompt):**
   ```
   cd backend\src\main\resources
   copy application-local.properties.example application-local.properties
   ```
   **Mac / Linux:**
   ```bash
   cd backend/src/main/resources
   cp application-local.properties.example application-local.properties
   ```

2. Open `application-local.properties` and replace `CHANGE_ME` with **your own MySQL root password**.
   This file is git-ignored, so your password never gets uploaded. **Never commit it.**

3. Make sure MySQL is running, then from the `backend` folder:

   ```bash
   cd backend
   mvn spring-boot:run
   ```

   The first run takes a few minutes (it downloads libraries). The database `loqma` is created automatically.

4. Check it works: open http://localhost:8080/api/health in your browser.
   You should see `{"status":"ok","app":"Loqma"}`.

---

## 4. Run the frontend

1. In VS Code, open `frontend/index.html`.
2. Right-click in the editor and choose **Open with Live Server**.
3. It opens at `http://127.0.0.1:5500`. The page should say **"Backend connected"** (the backend must be running).

---

## 5. Project structure

```
loqma/
├── backend/                 Java server (Spring Boot, Maven)
│   ├── pom.xml              Project info + list of libraries
│   └── src/main/
│       ├── java/com/loqma/
│       │   ├── LoqmaApplication.java   Starts the server
│       │   ├── common/                 Shared code (security, errors)
│       │   └── auth, customer, owner, driver, admin, notification
│       │         ├── controller/   Receives HTTP requests, returns responses
│       │         ├── service/      Business rules
│       │         ├── repository/   Talks to the database
│       │         ├── entity/       Classes that become database tables
│       │         └── dto/          Shapes of the data sent to / from the API
│       └── resources/              Settings files
├── frontend/                Website
│   ├── index.html           Backend connection test page
│   ├── assets/css, js, images   Shared styles, scripts, images
│   └── pages/               One folder per area: auth, customer, owner, driver, admin, notifications
├── docs/                    Requirements and design documents
├── CONTRIBUTING.md          Team workflow details
└── README.md                This file
```

**Calling the backend from a page:** include `config.js` and `api.js`, then:
```js
api.get('/api/health').then(data => console.log(data));
api.post('/api/something', { name: 'Omar' });
```
If the backend runs on a different address, change the one line in `frontend/assets/js/config.js`.

---

## 6. Who owns what

| Team | Screens | Backend package | Frontend folder |
|---|---|---|---|
| 1. Owner | Menu, Account, Orders, Order Details, Order History, Hours | `owner` | `pages/owner` |
| 2. Admin | Dashboard, Approvals, Promotions, Orders Monitor, Profile | `admin` | `pages/admin` |
| 3. Driver + Auth | Active Delivery, Available Orders, Earnings, Register, Login, Notifications | `driver`, `auth`, `notification` | `pages/driver`, `pages/auth`, `pages/notifications` |
| 4. Customer | Home, Restaurant List, Restaurant Details, Cart, Checkout, Order Tracking, Order History, Promotions | `customer` | `pages/customer` |

Work only inside your team's folders. Shared files (`common/`, `frontend/assets/`) are changed rarely:
tell the group chat before editing them.

---

## 7. Git workflow

```
main                 final, always working (updated at milestones only)
 └─ dev              all teams merge here
     ├─ team/owner
     ├─ team/admin
     ├─ team/driver-auth
     └─ team/customer
```

**Daily routine:**

```bash
git checkout team/<your-team>      # switch to your team branch
git pull                           # get your teammates' latest work
git checkout -b owner/hours-page   # make a short branch for your task
# ... write code ...
git add .
git commit -m "Add hours page layout"
git push -u origin owner/hours-page
```

Then open a **Pull Request** on GitHub from your task branch into your **team branch**, and ask a teammate to review it.

**Rules:**
- Never push straight to `main` or `dev`.
- Commit small and often, with a clear message.
- Before merging your team branch into `dev`, first pull `dev` into your team branch and fix conflicts there:
  ```bash
  git checkout team/<your-team>
  git pull origin dev
  ```
- Merge team branches into `dev` often (at least weekly) so work doesn't drift apart.
- Never commit passwords or `application-local.properties`.

---

## 8. Common problems

| Problem | Fix |
|---|---|
| `mvn` is not recognized | Maven isn't installed or not on PATH. Reopen the terminal after installing |
| `java -version` is not 21 | Install JDK 21 and set it as the default (JAVA_HOME) |
| `Access denied for user 'root'` | Wrong password in `application-local.properties` |
| `Communications link failure` | MySQL isn't running. Start the MySQL service |
| `Port 8080 already in use` | Another program uses 8080. Close it, or change `server.port` in `application.properties` |
| Page says "Backend not reachable" | Backend isn't running, or you opened the page without Live Server (use `127.0.0.1:5500`) |
| Red errors on `@Getter` / `@Data` in VS Code | Install the Lombok extension and reload VS Code |
| Git says "merge conflict" | Don't panic. Open the file, keep the right code, remove the `<<<<` / `====` / `>>>>` lines, then `git add` and commit. Ask for help if unsure |

Stuck? Copy the **full error message** and send it to the group chat.