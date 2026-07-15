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
-- and run the following SQL script to verify your context first
SELECT current_database(), current_schema();
-- Should output: shiftplanner | sp

-- now run the table.sql to create tables for the shift planner application.

-- after creating the tables, verify your them
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
-- token

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
-- token
select * from sp.token;
