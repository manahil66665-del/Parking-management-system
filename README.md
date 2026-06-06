# Parking Management System

Desktop semester project built with Java OOP, Java Swing, SQLite, JDBC, DAO pattern, and a small MVC-style separation between UI, services, models, and persistence.

## Features

- Secure login with roles, active users, PBKDF2 password hashing, and access-control service hooks.
- Dashboard with metrics, unpaid amount, slot status chart, and periodic refresh.
- Billing with invoice creation, payment status, bill table, mark-paid action, and delete action.
- Slot management with availability display, car allocation, release action, and refresh timer.
- Slot allocation prevents assigning one car to multiple slots.
- Car information lookup by registration, car type tracking (`SEDAN`, `JEEP`, `TRUCK`, `OTHER`), basic car creation, and admin-only car deletion.
- Deleting a car also removes its related bills/bookings so old cars do not remain in the Billing tab.
- Billing is standardized by car type and days. Rates are `SEDAN = 300`, `JEEP = 400`, `TRUCK = 600`, `OTHER = 250` per day.
- Billing screen shows rate/day, billed days, calculated total, status filter, paid/unpaid totals, owner, registration, and car type.
- Slots screen shows availability summary, status filter, assigned car type, and colored status cells.
- Slots can be added from the Slots tab and deleted when they are empty and available.
- Sample cars and slots are seeded only for a fresh database and will not reappear after you delete them.
- Admin-only user management for creating, editing, activating, deactivating, and deleting users.
- The default `admin` account is only seeded when the users table is completely empty, so deleting it will not bring it back if another user exists.
- The login form is blank by default instead of prefilled with demo credentials.
- SQLite schema and seed data for User, Role, Car, Slot, Bill, and Booking.

## Default Login

```text
Username: admin
Password: admin123
```

The Java initializer creates this account on first run with a generated PBKDF2 hash.

## Project Structure

```text
ParkingManagementSystem/
|-- src/com/parking/
|   |-- Main.java
|   |-- config/              Database connection and schema bootstrap
|   |-- dao/                 SQLite DAO classes using PreparedStatement
|   |-- model/               OOP entities
|   |-- service/             Business logic, validation, access checks
|   |-- ui/                  Swing frames, theme, panels
|   `-- util/                Password hashing and validation helpers
|-- database/
|   |-- schema.sql
|   `-- seed.sql
|-- docs/
|   `-- PROJECT_REPORT.md
`-- lib/
    `-- README.md
```

## Compile And Run

Install JDK 11 or newer. Put SQLite JDBC at:

```text
ParkingManagementSystem/lib/sqlite-jdbc.jar
```

Use the main binary JAR only. Do not use `sqlite-jdbc-...-javadoc.jar` or `sqlite-jdbc-...-sources.jar`.

### Simple IDE Run

Open the project in IntelliJ IDEA, NetBeans, Eclipse, or VS Code, then run:

```text
src/com/parking/Main.java
```

The app now loads `lib/sqlite-jdbc.jar` automatically if the IDE does not add it to the classpath.

From the project folder:

```powershell
mkdir out
javac -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
java -cp "out;lib/sqlite-jdbc.jar" com.parking.Main
```

Or run the included PowerShell launcher:

```powershell
.\run.ps1
```

If Windows blocks PowerShell scripts, use:

```powershell
powershell -ExecutionPolicy Bypass -File .\run.ps1
```

Or double-click/run:

```bat
run.bat
```

On Linux/macOS:

```bash
mkdir -p out
javac -d out $(find src -name "*.java")
java -cp "out:lib/sqlite-jdbc.jar" com.parking.Main
```

For IntelliJ IDEA, NetBeans, or Eclipse:

1. Open `ParkingManagementSystem` as a Java project.
2. Add `lib/sqlite-jdbc.jar` to project libraries.
3. Set `com.parking.Main` as the main class.
4. Run the project. The SQLite file `parking_management.db` is created automatically.

## MVP Phases

1. Phase 1: Login, schema creation, seed data, dashboard metrics.
2. Phase 2: Slot availability, car allocation/release, car lookup.
3. Phase 3: Billing CRUD, invoice status, payment tracking.
4. Phase 4: Better role screens, booking history, search filters, printable invoice.
5. Phase 5: UI polish, validation coverage, export reports, backup/restore.
