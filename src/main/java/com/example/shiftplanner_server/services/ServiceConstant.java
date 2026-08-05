package com.example.shiftplanner_server.services;

import java.time.LocalTime;

public class ServiceConstant {
    public static final LocalTime LUNCH_START = LocalTime.of(12, 0); // 12:00
    public static final LocalTime LUNCH_END = LocalTime.of(14, 0);   // 14:00
    public static final LocalTime ASSIGNMENT_START = LocalTime.of(9, 0);
    public static final LocalTime ASSIGNMENT_END = LocalTime.of(18, 0);

    public static final String ERROR_FORMAT_1 ="The current shift does not comply with policy %s. Please update the shift until all scheduling rules are satisfied.";
    public static final String ERROR_FORMAT_2 ="The current shift contains the following scheduling rule conflicts(%s). Please review and edit the shift to resolve all conflicts before proceeding.";
    public static final String ERROR_FORMAT_3 ="No feasible schedule can be generated with the current scheduling requirements. To generate a valid schedule, please remove one or more scheduling rules, starting with Rule 7 and then Rule 6, Rule 5, and Rule 4, until a feasible solution is found.";

}
