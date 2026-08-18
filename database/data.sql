-- Test Data

-- policies
insert into sp.policies(description, param_1)
values ('Excluding staff who are on their lunch break, there must be at least {1} staff members present in the library at all times.',
        3),
       ('Between 12:00 p.m. and 2:00 p.m., every staff member must be allocated one of the four tasks: lunch break, lunch/ check-in, lunch/bell or lunch/roaming.',
        null),
       ('At least one staff member must be assigned to the service desk during every hourly time slot.', null),
       ('A staff member must not be assigned to two consecutive Desk, Check-in, Picking, Roaming or Shelving tasks.',
        null),
       ('At most two staff members should be assigned to the service desk during each hourly time slot whenever possible.',
        null),
       ('Staff members working an eight-hour shift must be allocated at least one Optional (unassigned) time slot during the day.',
        null),
       ('Where staffing levels permit, a staff member should be assigned to the Check-in task during every hourly time slot.',
        null);

-- users
insert into sp.users(username, password, is_manager)
values ('Manager', '$2a$10$5B5abE.REDO6KmDqBr/w8O2OJaiZ8VVenmdc9VRXx8vexSP6pVCvm', true),
       ('Senior Librarian', '$2a$10$7cgpbFYv5wq9FZUBsWx8D.0iHU0c.B0P96CrcsO9cFUEcA8XHYnmO', false);
-- ('Manager', 'manager2026', true),
-- ('Senior Librarian', 'librarian2026', false);

-- staffs
insert into sp.staff(staff_name, is_active)
values ('Emma Li', true),
       ('Noah Patel',  true),
       ('Olivia Chen', true),
       ('Lucas Singh', true),
       ('Ava Wilson', true),
       ('Ethan Brown', true),
       ('Sophia Kumar', true);

-- schedules

-- schedule_assignments

-- tasks
insert into sp.tasks (task_name, colour, is_lunch, is_auto)
values ('Desk', '#4da3ff', false, false),
       ('Check-in', '#f4c542', false, false),
       ('Picking', '#4ecb71', false, false),
       ('Shelving', '#d96df0', false, false),
       ('Meeting', '#ff9f68', false, false),
       ('Lunch', '#ffcf5a', true, false),
       ('Event', '#45c7c7', false, false),
       ('Event Prep', '#8f7cff', false, false),
       ('Closing-15min', '#ff7b7b', false, false),
       ('Training', '#5f8bff', false, false),
       ('Block', '#dad8c9', false, false),
       ('Bell', '#ff8aa1', false, false),
       ('Roaming', '#6ed3ff', false, false),
       ('Lunch/Check-in', '#f7b267', true, false),
       ('Lunch/Bell', '#ffb3c7', true, false),
       ('Lunch/Roaming', '#8ee3ef', true, false),
       ('Optional', '#c7d2e2', false, true),
       ('Off-site', '#444444', true, false);

-- Map lunch-combo tasks to the same alias as their base task
UPDATE sp.tasks t
SET task_alias = CASE t.task_name
                     WHEN 'Check-in' THEN lc.task_id
                     WHEN 'Lunch/Check-in' THEN c.task_id
                     WHEN 'Roaming' THEN lr.task_id
                     WHEN 'Lunch/Roaming' THEN r.task_id
    END
FROM (SELECT task_id FROM sp.tasks WHERE task_name = 'Check-in') c,
     (SELECT task_id FROM sp.tasks WHERE task_name = 'Lunch/Check-in') lc,
     (SELECT task_id FROM sp.tasks WHERE task_name = 'Roaming') r,
     (SELECT task_id FROM sp.tasks WHERE task_name = 'Lunch/Roaming') lr
WHERE t.task_name IN (
                      'Check-in', 'Lunch/Check-in',
                      'Roaming', 'Lunch/Roaming'
    );