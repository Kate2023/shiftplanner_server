-- Test Data

-- policies
insert into sp.policies(description, param_1) values
('1. Excluding staff who are on their lunch break, there must be at least {1} staff members present in the library at all times.', 3),
('2. Between 12:00 p.m. and 2:00 p.m., every staff member must be allocated one of the four tasks: lunch break, lunch/ check-in, lunch/bell or lunch/roaming.', null),
('3. At least one staff member must be assigned to the service desk during every hourly time slot.', null),
('4. A staff member must not be assigned to two consecutive Desk, Check-in, Picking, Roaming or Shelving tasks.', null),
('5. At most two staff members should be assigned to the service desk during each hourly time slot whenever possible.', null),
('6. Staff members working an eight-hour shift must be allocated at least one Optional (unassigned) time slot during the day.', null),
('7. Where staffing levels permit, a staff member should be assigned to the Check-in task during every hourly time slot.', null);

-- users
insert into sp.users(username, password, is_manager) values
('Manager', 'manager2026', true),
('Senior Librarian', 'librarian2026', false);

-- stafff
insert into sp.staff(staff_name, working_hours, lunch_break, is_active) values
('Emma Li', 40, 60, true),
('Noah Patel', 35, 60, true),
('Olivia Chen', 30, 60, true),
('Lucas Singh', 38, 60, true),
('Ava Wilson', 40, 60, true),
('Ethan Brown', 32, 60, true),
('Sophia Kumar', 36, 60, true);

-- schedules
insert into sp.schedules (date, notes, roster_staff, banking_staff, banking_backup_staff, building_inspector, policies) values
('2026-07-04', 'Test day 1', 7, 1, 2, 3, '1,2,3,4,5,6,7'),
('2026-07-05', 'Test day 2', 7, 4, 5, 6, '1,2,3,4,5,6,7');

-- schedule_assignments
select * from sp.schedule_assignments;

-- tasks
insert into sp.tasks (task_name, colour, is_lunch, is_auto) values
('Desk', '#4da3ff', false, false),
('Check-in', '#f4c542', false, false),
('Picking', '#4ecb71', false, false),
('Shelving', '#d96df0', false, false),
('Meeting', '#ff9f68', false, false),
('Lunch', '#ffcf5a', true, false),
('Event', '#45c7c7', false, false),
('Event Prep', '#8f7cff', false, false),
('Closing-15min', '#ff7b7b', false, false),
('Training', '#5f8bff', false, false),
('Block', '#444444', false, false),
('Bell', '#ff8aa1', false, false),
('Roaming', '#6ed3ff', false, false),
('Lunch/Check-in', '#f7b267', true, false),
('Lunch/Bell', '#ffb3c7', true, false),
('Lunch/Roaming', '#8ee3ef', true, false),
('Optional', '#c7d2e2', false, true);