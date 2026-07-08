-- Setup PostgreSQL database for the shift planner application
--
-- Step 1: Running PostgreSQL with Docker
-- Open a terminal and run the following command to start a PostgreSQL container with Docker.
-- This will create a new PostgreSQL instance with the specified user and password, and map port
-- 5433 on your host machine to port 5432 in the container.

-- docker run --name dev-postgres -e POSTGRES_USER=admin -e POSTGRES_PASSWORD=App1234 -p 5433:5432 -d postgres:18.4

-- Step 2: Connecting to PostgreSQL via Intellij IDEA
-- 1. Open the Database tool window in IntelliJ IDEA.
-- 2. Click the "+" button to add a new data source and select "PostgreSQL".
-- 3. In the "Data Sources and Drivers" dialog, fill in the connection details:
--    - Name: dev_admin
--    - Host: localhost
--    - Port: 5433
--    - Database: admin
--    - User: admin
--    - Password: App1234
--    - (Or. URL jdbc:postgresql://localhost:5433/admin)
-- 4. Click "Test Connection" to ensure that the connection is successful.
-- 5. If the connection is successful, click "OK" to save the data source.
-- 6. You should now see the Data Source dev_admin in the Database tool window.
-- 7. Now you can run SQL scripts and queries against the PostgreSQL database using IntelliJ IDEA's SQL console.

-- Step 3: Create a new database and user for the shift planner application
-- Open a query console on dev_admin and run this single command to create the new database asset
-- 1. Create a brand new database for the app
CREATE DATABASE shiftplanner;

-- 2. Create a restricted application login user account
CREATE USER sp_user WITH ENCRYPTED PASSWORD 'secure_app_password';

-- 3. Create the custom namespace schema owned by the new user
CREATE SCHEMA SP AUTHORIZATION sp_user;

-- 4. Revoke generic public system access for security
REVOKE ALL ON DATABASE shiftplanner FROM PUBLIC;
GRANT CONNECT ON DATABASE shiftplanner TO sp_user;

-- 5. Grant explicit creation and usage rights to your user on the new schema
GRANT USAGE, CREATE ON SCHEMA SP TO sp_user;

-- 6. Set the default search path so your app doesn't require "SP." prefixes
ALTER USER sp_user SET search_path TO SP, public;

-- Step 4: Connecting to PostgreSQL shiftplanner as sp_user via Intellij IDEA
-- 1. Open the Database tool window in IntelliJ IDEA.
-- 2. Click the "+" button to add a new data source and select "PostgreSQL".
-- 3. In the "Data Sources and Drivers" dialog, fill in the connection details:
--     - Name: dev_sp
--     - Host: localhost
--     - Port: 5433
--     - Database: shiftplanner
--     - User: sp_user
--     - Password: secure_app_password
--     - (Or. URL jdbc:postgresql://localhost:5433/shiftplanner)
-- 4. Click "Test Connection" to ensure that the connection is successful.
-- 5. If the connection is successful, click "OK" to save the data source.
-- 6. Now you should see the Data Source dev_sp_user in the Database tool window.
-- 7. You can now run SQL scripts and queries against the shiftplanner database using IntelliJ IDEA's SQL console.

-- STEP 4: Build Your Table.
-- Open a query console on dev_sp
-- Switch current schema (from the top right corner) to shiftplanner
-- and run the following SQL script to create tables for the shift planner application.

-- Verify your context first
SELECT current_database(), current_schema();
-- Should output: shiftplanner | sp

BEGIN;

-- 1) Policies
CREATE TABLE IF NOT EXISTS sp.policies (
    policy_id   INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    description VARCHAR(255) NOT NULL UNIQUE,
    param_1     INT NULL
);


-- 2) Users
CREATE TABLE IF NOT EXISTS sp.users (
    user_id      INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username     VARCHAR(255) NOT NULL UNIQUE,
    password     VARCHAR(255) NOT NULL,
    is_manager   BOOLEAN NOT NULL DEFAULT FALSE
);

-- 3) Staff
CREATE TABLE IF NOT EXISTS sp.staff (
    staff_id                 INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    staff_name               VARCHAR(255) NOT NULL,
    working_hours            NUMERIC(5,2) NOT NULL CHECK (working_hours >= 0),
    lunch_break              NUMERIC(5,2) NOT NULL CHECK (lunch_break >= 0),
    is_active                BOOLEAN NOT NULL DEFAULT TRUE
);

-- 4) Tasks
CREATE TABLE IF NOT EXISTS sp.tasks (
    task_id      INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    task_name    VARCHAR(255) NOT NULL UNIQUE,
    colour       VARCHAR(7) NOT NULL CHECK (colour ~ '^#[0-9A-Fa-f]{6}$'),
    is_lunch     BOOLEAN NOT NULL DEFAULT FALSE,
    is_auto      BOOLEAN NOT NULL DEFAULT FALSE
);

-- 5) Schedules
CREATE TABLE IF NOT EXISTS sp.schedules (
    schedule_id                          INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    "date"                               DATE NOT NULL,
    notes                                TEXT NULL,
    roster_staff                         INT NULL,
    banking_staff                        INT NULL,
    banking_backup_staff                 INT NULL,
    building_inspector                   INT NULL,

    CONSTRAINT fk_schedules_roster_staff
    FOREIGN KEY (roster_staff) REFERENCES staff(staff_id),
    CONSTRAINT fk_schedules_banking_staff
    FOREIGN KEY (banking_staff) REFERENCES staff(staff_id),
    CONSTRAINT fk_schedules_banking_backup_staff
    FOREIGN KEY (banking_backup_staff) REFERENCES staff(staff_id),
    CONSTRAINT fk_schedules_inspection_staff
    FOREIGN KEY (building_inspector) REFERENCES staff(staff_id)
);

-- Optional: one schedule per date
CREATE UNIQUE INDEX IF NOT EXISTS ux_schedules_date ON schedules ("date");

-- 6) Schedule assignments
CREATE TABLE IF NOT EXISTS schedule_assignments (
    assignment_id     INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    schedule_id       INT NOT NULL,
    staff_id          INT NOT NULL,
    time_slot         TIME NOT NULL,
    task_id           INT NOT NULL,

    CONSTRAINT fk_assignments_schedule
    FOREIGN KEY (schedule_id) REFERENCES schedules(schedule_id) ON DELETE CASCADE,
    CONSTRAINT fk_assignments_staff
    FOREIGN KEY (staff_id) REFERENCES staff(staff_id),
    CONSTRAINT fk_assignments_task
    FOREIGN KEY (task_id) REFERENCES tasks(task_id),

    -- Prevent duplicate assignment rows for same person and exact slot in a schedule
    CONSTRAINT uq_assignment_staff_slot
    UNIQUE (schedule_id, staff_id, time_slot)
);

-- Helpful indexes
CREATE INDEX IF NOT EXISTS ix_assignments_schedule ON schedule_assignments (schedule_id);

COMMIT;

-- Verify your tables were created in the correct schema
SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'sp';
-- Should see:
-- policies
-- users
-- staff
-- schedules
-- schedule_assignments
-- tasks

-- policies
select * from sp.policies;
-- users
select * from sp.users;
-- staff
select * from sp.staff;
-- schedules
select * from sp.schedules;
-- schedule_assignments
select * from sp.schedule_assignments;
-- tasks
select * from sp.tasks;
