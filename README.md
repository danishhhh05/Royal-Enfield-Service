# 🏍️ Royal Enfield Service Management System

> A desktop application for managing Royal Enfield service center operations — customers, bike servicing, status tracking, and revenue analytics.

---

## 📌 About

A Java Swing desktop application built for Royal Enfield authorized service centers to replace paper registers with a structured, searchable, and analytics-driven management system. Supports full CRUD on customers and service records with a modern dark-themed UI.

---

## ✨ Features

- 📋 **Customer Management** — Add, update, delete, and search customers
- 🔧 **Service Records** — Log bike model, service type, status, amount, and date
- 📊 **Live Dashboard** — Real-time stats (total customers, active services, revenue)
- 📈 **Reports Module** — Recent service history and summary analytics
- ⚙️ **Settings Panel** — Configure MySQL connection without touching code
- 🗄️ **Auto Setup** — Database and tables created automatically on first run

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|------------|
| Language | Java JDK 17+ |
| GUI | Java Swing (javax.swing) |
| Database | MySQL 8.x |
| DB Driver | MySQL Connector/J 9.7.0 |
| Config | db.properties |
| Build | javac + run.bat |

---

## 🏗️ Architecture

Presentation Layer  →  Java Swing UI (Dashboard, Panels)
Business Layer      →  Java POJOs (Customer, ServiceRecord)
Data Access Layer   →  DAO Pattern (CustomerDAO, ServiceDAO)
Database Layer      →  MySQL (re_service_db)

**Design Patterns:** DAO Pattern, Singleton JDBC Connection, Layered Architecture

---

## 🗃️ Database Schema

customers
  id, name, phone, email, address, created_at

service_records
  id, customer_id (FK), bike_model, service_type,
  status, amount, service_date, notes

Relationship: One customer → Many service records (CASCADE DELETE)

---

## 🚀 How to Run

**Prerequisites:**
- Java JDK 17+
- MySQL 8.x running locally

**Step 1 — Configure database:**
Edit db.properties with your MySQL credentials

**Step 2 — Compile and run:**
run.bat

**Or manually:**
javac -cp "lib/*" -d out (Get-ChildItem -Path "src" -Recurse -Filter "*.java" | % { $_.FullName })
java -cp "lib/*;out" com.royalenfield.service.Main

---

## 📁 Project Structure

gudi1/
├── lib/
│   └── mysql-connector-j-9.7.0.jar
├── src/com/royalenfield/service/
│   ├── Main.java
│   ├── Dashboard.java
│   ├── CustomerPanel.java / ServicePanel.java
│   ├── ReportsPanel.java / SettingsPanel.java
│   ├── CustomerDAO.java / ServiceDAO.java
│   ├── DBConnection.java
│   └── AppColors.java / UIHelper.java
├── database.sql
├── db.properties
└── run.bat

---

## ✍️ Author

**Danish** — AI/ML Engineer &nbsp; GitHub: [@danishhhh05](https://github.com/danishhhh05)