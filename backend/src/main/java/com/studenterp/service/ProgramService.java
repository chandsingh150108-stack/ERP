package com.studenterp.service;

import com.studenterp.entity.Department;
import com.studenterp.entity.Program;
import com.studenterp.exception.ResourceNotFoundException;
import com.studenterp.repository.DepartmentRepository;
import com.studenterp.repository.ProgramRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProgramService {

    private final ProgramRepository programRepository;
    private final DepartmentRepository departmentRepository;

    public List<Program> findAll() {
        return programRepository.findAll();
    }

    public Program findById(Long id) {
        return programRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Program", "id", id));
    }

    public List<Program> findByDepartment(Long departmentId) {
        return programRepository.findByDepartmentId(departmentId);
    }

    public Program create(Program program) {
        if (programRepository.existsByCode(program.getCode())) {
            throw new IllegalArgumentException("Program code already exists");
        }
        Department dept = departmentRepository.findById(program.getDepartment().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", program.getDepartment().getId()));
        program.setDepartment(dept);
        return programRepository.save(program);
    }

    public Program update(Long id, Program updated) {
        Program program = findById(id);
        Department dept = departmentRepository.findById(updated.getDepartment().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", updated.getDepartment().getId()));
        program.setCode(updated.getCode());
        program.setName(updated.getName());
        program.setDegreeType(updated.getDegreeType());
        program.setDurationYears(updated.getDurationYears());
        program.setTotalCredits(updated.getTotalCredits());
        program.setDepartment(dept);
        program.setActive(updated.getActive());
        return programRepository.save(program);
    }

    public void delete(Long id) {
        Program program = findById(id);
        programRepository.delete(program);
    }
}
