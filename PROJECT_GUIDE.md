# PHINParks Project Guide

## What it does

PHINParks is a school parking web app. A customer can create an account, register
a vehicle, park in an available slot, and check out. An administrator can view
parking records, remove vehicles, and add parking slots.

## Where to start reading

Start with these files, in this order:

1. `src/main/java/com/phinpark/Main.java` starts the Spring Boot application.
2. `src/main/resources/static/index.html` contains the web page and the
   JavaScript that calls the backend.
3. `src/main/java/com/phinpark/controller/` contains the API endpoints used by
   that page.
4. `src/main/java/com/phinpark/service/` contains account, vehicle, and
   persistent parking rules.
5. `src/main/java/com/phinpark/model/` contains parking objects such as
   `Vehicle`, `ParkingSlot`, and `Ticket`.

Other useful files:

- `src/main/java/com/phinpark/entity/` defines database records for users,
  vehicles, parking slots, and tickets.
- `src/main/java/com/phinpark/repository/` provides database queries.
- `src/main/java/com/phinpark/service/ParkingDataService.java` stores parking
  slots and ticket activity in the database.
- `src/main/java/com/phinpark/config/ApiAuthorizationInterceptor.java` checks
  the signed-in user and blocks client accounts from admin-only API actions.
- `src/main/java/com/phinpark/config/DataInitializer.java` creates the sample
  parking slots when the app starts.
- `src/main/resources/application.properties` configures the local server and
  H2 database. `application-prod.properties` configures PostgreSQL for Render.

## How one request moves through the app

The page sends a request to an API endpoint. A controller receives the request
and calls the relevant service or repository. The result is returned to the
page as JSON.

For example, when a customer registers a vehicle:

1. The page sends the vehicle details to `POST /api/vehicles`.
2. `VehicleController` gets the signed-in user's ID from the server session.
3. `VehicleService` checks the details and creates a vehicle record.
4. `VehicleRepository` saves that record in the configured database.

The browser does not choose the account ID for the new vehicle.

## Main parking flow

1. Register as a customer or log in.
2. Add a vehicle.
3. Choose that vehicle and an available parking slot.
4. The app creates a ticket with the vehicle, slot, and entry time.
5. The customer views or prints the bill showing the fee due.
6. The parking attendant receives cash, then an administrator records the
   payment in the app. Checkout records the exit and frees the slot.

The hourly rates in the Java vehicle classes are ₱50 for cars, ₱40 for
motorcycles, and ₱30 for bikes. Partial hours are rounded up to a full hour.
Cash payment in the app is a school-project demonstration only; it does not
process or verify real money.

## Accounts and admin access

Public registration always creates a `CLIENT` account. The first admin is
created through the registration form using the setup key configured in the
`PHINPARKS_ADMIN_SETUP_KEY` environment variable. Setup is refused if an admin
already exists. Keep the setup key private; it is only needed for first-admin
setup, not for later logins. Typing a key in the form does not configure it:
configure the environment variable in the Render service's Environment page
and redeploy before using the first-admin form. The key must be at least 32
characters long.

Passwords are stored as BCrypt hashes. After login, the server creates a
session. The authorization interceptor reads the user's role from the database
for protected requests; hiding admin buttons in the page is not the security
check.

## Run the app

From the project folder, open a PowerShell terminal and run:

```powershell
.\apache-maven-3.9.4\bin\mvn.cmd spring-boot:run
```

Wait for `Started Main`, then open `http://localhost:7777`.

For first-admin setup, set the environment key in the same PowerShell terminal
before starting the app:

```powershell
$env:PHINPARKS_ADMIN_SETUP_KEY = "your-private-key-with-at-least-32-characters"
.\apache-maven-3.9.4\bin\mvn.cmd spring-boot:run
```

If the terminal is Command Prompt (`cmd`) instead, use
`set "PHINPARKS_ADMIN_SETUP_KEY=your-private-key-with-at-least-32-characters"`
before running Maven.

## Where the data is saved

Locally, users, vehicles, parking slots, and tickets are saved in the file-backed
H2 database in the project folder. Keep the `phinparks.mv.db` file; deleting it
deletes that saved data. The H2 Console is for local development and is disabled
in the production profile.

The Render deployment uses PostgreSQL for users, vehicles, parking slots, and
ticket history. The production database starts empty; local H2 data is not
migrated automatically. Render's free PostgreSQL database expires after 30
days, so do not treat it as permanent storage.
Vehicle colors are stored as text, so preset colors and custom color-picker
hex values can both be retained in active tickets.

To view the hosted records, open the `phinparks-db` database in the Render
dashboard and copy its **external** connection details into a PostgreSQL
connection in DBeaver. Do not share the database password. Example read-only
queries:

```sql
SELECT id, first_name, last_name, email, account_type, created_at FROM users;
SELECT vehicle_id, owner_id, vehicle_type, plate_number FROM registered_vehicle;
SELECT slot_number, occupied, slot_type FROM parking_slot;
SELECT ticket_id, owner_id, plate_number, slot_number, entry_time, exit_time, total_fee
FROM parking_ticket;
```

## Deploy and connect `phinpark.com`

1. Push the project to its GitHub repository, then create a **Blueprint**
   in Render and select that repository. Render reads `render.yaml` to create
   the web service and free PostgreSQL database in Singapore.
2. Before creating the first administrator, set a new
   `PHINPARKS_ADMIN_SETUP_KEY` in the Render service's environment settings.
   Generate a private key of at least 32 characters; never put it in this file
   or in source control. A setup key was previously included in this guide,
   so treat that value as compromised and do not reuse it.
3. After the service deploys, open its **Settings > Custom Domains** in Render
   and add `phinpark.com`. At your domain registrar, create the DNS records
   Render displays for that domain. Render provisions HTTPS after DNS
   verification; wait for the domain and certificate statuses to become
   active.

Render's free web service can sleep when idle, and the free database expires
after 30 days. This setup is intended for a school demo, not production use.

## Short explanation for a presentation

> PHINParks is a Spring Boot web app for managing school parking. The web page
> sends requests to Java API controllers. The local app uses H2, and the Render
> deployment uses PostgreSQL. Both databases store user accounts, vehicles,
> parking slots, and tickets. A ticket records a vehicle's slot and parking
> times. The customer can view the bill, and an administrator records the cash
> payment before checkout makes the slot available again.

## Questions to prepare for

- **Why use a ticket?** It connects a vehicle, its parking slot, and its entry
  and exit times.
- **How does a customer become an administrator?** Public registration cannot
  choose the admin role. First-admin setup requires the private setup key, and
  admin API actions check the role stored in the database.
- **What stays after restart?** The database stores accounts, vehicles, parking
  slots, and tickets. Render's free PostgreSQL database expires after 30 days.
- **What would you improve next?** Save parking slots and tickets in the
  database, and add production-ready security configuration before deploying
