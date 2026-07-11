-- Create all tables for the scheduling application in the "sp" schema

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
    policies                             VARCHAR(255) NOT NULL,

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

END;