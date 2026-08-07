package com.example.shiftplanner_server.services.auto;

import com.example.shiftplanner_server.entities.Staff;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ScoreTest {

    @Test
    void recordStoresValuesAndSupportsValueEquality() {
        Staff staff = new Staff();
        staff.setStaffId(1);

        Score score = new Score(staff, 7L);
        Score same = new Score(staff, 7L);
        Score different = new Score(staff, 8L);

        assertEquals(staff, score.staff());
        assertEquals(7L, score.value());
        assertEquals(same, score);
        assertNotEquals(different, score);
    }
}

