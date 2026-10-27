package com.studenterp;

import com.studenterp.entity.*;
import com.studenterp.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class DepartmentTest {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Test
    void testCreateAndFindDepartment() {
        Department dept = Department.builder()
                .code("PHY")
                .name("Physics")
                .description("Physics Department")
                .active(true)
                .build();
        Department saved = departmentRepository.save(dept);
        assertNotNull(saved.getId());
        assertEquals("PHY", saved.getCode());

        Department found = departmentRepository.findById(saved.getId()).orElse(null);
        assertNotNull(found);
        assertEquals("Physics", found.getName());
    }

    @Test
    void testDuplicateCodeFails() {
        Department d1 = Department.builder().code("MATH").name("Math1").active(true).build();
        departmentRepository.save(d1);

        Department d2 = Department.builder().code("MATH").name("Math2").active(true).build();
        assertThrows(Exception.class, () -> departmentRepository.save(d2));
    }
}
