package com.example.shiftplanner_server.repositories;

import com.example.shiftplanner_server.entities.Staff;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
class StaffRepositoryTest extends PostgresRepositoryTestBase {

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        createTablesFromSql(jdbcTemplate);
    }

    @AfterEach
    void tearDown() {
        dropSchema(jdbcTemplate);
    }

    @Test
    void saveAndFindById() {
        Staff staff = new Staff();
        staff.setStaffName("Alex");
        staff.setWorkingHours(BigDecimal.valueOf(38));
        staff.setLunchBreak(BigDecimal.valueOf(60));
        staff.setActive(true);

        Staff saved = staffRepository.save(staff);

        assertTrue(staffRepository.findById(saved.getStaffId()).isPresent());
    }

    @Test
    void findAllByActiveTrueReturnsOnlyActiveStaff() {
        Staff active = new Staff();
        active.setStaffName("Active Staff");
        active.setWorkingHours(BigDecimal.valueOf(40));
        active.setLunchBreak(BigDecimal.valueOf(60));
        active.setActive(true);

        Staff inactive = new Staff();
        inactive.setStaffName("Inactive Staff");
        inactive.setWorkingHours(BigDecimal.valueOf(35));
        inactive.setLunchBreak(BigDecimal.valueOf(60));
        inactive.setActive(false);

        staffRepository.save(active);
        staffRepository.save(inactive);

        List<Staff> activeOnly = staffRepository.findAllByActiveTrue();

        assertEquals(1, activeOnly.size());
        assertEquals("Active Staff", activeOnly.get(0).getStaffName());
    }

    @Test
    void deleteByIdRemovesStaff() {
        Staff staff = new Staff();
        staff.setStaffName("Delete Staff");
        staff.setWorkingHours(BigDecimal.valueOf(30));
        staff.setLunchBreak(BigDecimal.valueOf(45));
        staff.setActive(true);

        Staff saved = staffRepository.save(staff);
        staffRepository.deleteById(saved.getStaffId());

        assertFalse(staffRepository.findById(saved.getStaffId()).isPresent());
    }
}

