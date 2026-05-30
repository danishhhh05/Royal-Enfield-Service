# Royal Enfield Service Management System

A premium dark-mode Java Swing desktop app for managing customers and bike service records.

## Requirements

- **Java 17+** (JDK with `javac` and `java`)
- **MySQL Server 8.x** (running locally)
- **MySQL Connector/J** — download `mysql-connector-j-x.x.x.jar` from [MySQL](https://dev.mysql.com/downloads/connector/j/) and place it in the `lib` folder

## Quick start

### 1. Install and start MySQL

- Install [MySQL Community Server](https://dev.mysql.com/downloads/mysql/)
- Start the MySQL service (Windows: Services → MySQL80 → Start)
- Remember the **root password** you set during installation

### 2. Configure the database

**Option A — In the app (easiest)**

1. Run the application (see step 4)
2. Open **Settings** in the sidebar
3. Enter:
   - Host: `localhost`
   - Port: `3306`
   - Database: `re_service_db`
   - Username: `root`
   - Password: *your MySQL root password*
4. Click **Save & Connect**
5. Click **Create Tables** (adds tables + sample data on first run)

**Option B — Manual SQL**

```bash
mysql -u root -p < database.sql
```

Then create `db.properties` from the example:

```bash
copy db.properties.example db.properties
```

Edit `db.properties` and set your password.

### 3. Add JDBC driver

```
f:\gudi1\lib\mysql-connector-j-9.3.0.jar
```

(Any recent `mysql-connector-j*.jar` filename is fine.)

### 4. Run the application

**Windows:**

```bat
run.bat
```

**Manual:**

```bat
cd F:\gudi1\src
javac -cp ".;..\lib\*" com\royalenfield\service\*.java
java -cp ".;..\lib\*" com.royalenfield.service.Main
```

## Features

| Module | Features |
|--------|----------|
| **Dashboard** | Live stats, clock, quick actions |
| **Customers** | Search, add, update, delete |
| **Services** | Link to customer, status, amount, date |
| **Reports** | Summary + recent services table |
| **Settings** | Test/save DB connection |

## Project structure

```
src/com/royalenfield/service/
  Main.java           — Entry point
  AppColors.java      — Theme colors
  DBConnection.java   — MySQL + db.properties
  CustomerDAO.java    — Customer CRUD
  ServiceDAO.java     — Service CRUD
  UIHelper.java       — Styled components
  Dashboard.java      — Main window
  CustomerPanel.java
  ServicePanel.java
  ReportsPanel.java
  SettingsPanel.java
```

## Troubleshooting

| Problem | Solution |
|---------|----------|
| `MySQL JDBC driver not found` | Put `mysql-connector-j.jar` in `lib/` |
| `Access denied for user 'root'` | Wrong password in Settings or `db.properties` |
| `Communications link failure` | Start MySQL service |
| Stats show 0 | Connect DB in Settings → Create Tables |
| `java` not recognized | Install JDK and add to PATH |

## Environment variables (optional)

Instead of `db.properties`, you can set:

- `RE_DB_HOST`, `RE_DB_PORT`, `RE_DB_NAME`, `RE_DB_USER`, `RE_DB_PASSWORD`
