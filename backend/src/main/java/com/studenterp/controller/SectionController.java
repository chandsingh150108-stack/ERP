package com.studenterp.controller;

import com.studenterp.entity.AcademicYear;
import com.studenterp.entity.Program;
import com.studenterp.entity.Section;
import com.studenterp.exception.ResourceNotFoundException;
import com.studenterp.repository.AcademicYearRepository;
import com.studenterp.repository.ProgramRepository;
import com.studenterp.repository.SectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sections")
@RequiredArgsConstructor
public class SectionController {

    private final SectionRepository sectionRepository;
    private final ProgramRepository programRepository;
    private final AcademicYearRepository academicYearRepository;

    @GetMapping
    public ResponseEntity<List<Section>> findAll() {
        return ResponseEntity.ok(sectionRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Section> findById(@PathVariable Long id) {
        return ResponseEntity.ok(sectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Section", "id", id)));
    }

    @GetMapping("/program/{programId}")
    public ResponseEntity<List<Section>> findByProgram(@PathVariable Long programId) {
        List<Section> all = sectionRepository.findAll().stream()
                .filter(s -> s.getProgram() != null && s.getProgram().getId().equals(programId))
                .toList();
        return ResponseEntity.ok(all);
    }

    @GetMapping("/program/{id}/semester/{sem}")
    public ResponseEntity<List<Section>> findByProgramAndSemester(
            @PathVariable("id") Long programId, @PathVariable("sem") Integer semester) {
        return ResponseEntity.ok(sectionRepository.findByProgramIdAndSemester(programId, semester));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Section> create(@RequestBody Section section) {
        if (section.getProgram() != null && section.getProgram().getId() != null) {
            Program program = programRepository.findById(section.getProgram().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Program", "id", section.getProgram().getId()));
            section.setProgram(program);
        }
        if (section.getAcademicYear() != null && section.getAcademicYear().getId() != null) {
            AcademicYear ay = academicYearRepository.findById(section.getAcademicYear().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", "id", section.getAcademicYear().getId()));
            section.setAcademicYear(ay);
        }
        return ResponseEntity.ok(sectionRepository.save(section));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Section> update(@PathVariable Long id, @RequestBody Section updated) {
        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Section", "id", id));
        if (updated.getName() != null) section.setName(updated.getName());
        if (updated.getSemester() != null) section.setSemester(updated.getSemester());
        if (updated.getCapacity() != null) section.setCapacity(updated.getCapacity());
        if (updated.getProgram() != null && updated.getProgram().getId() != null) {
            Program program = programRepository.findById(updated.getProgram().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Program", "id", updated.getProgram().getId()));
            section.setProgram(program);
        }
        if (updated.getAcademicYear() != null && updated.getAcademicYear().getId() != null) {
            AcademicYear ay = academicYearRepository.findById(updated.getAcademicYear().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", "id", updated.getAcademicYear().getId()));
            section.setAcademicYear(ay);
        }
        return ResponseEntity.ok(sectionRepository.save(section));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        sectionRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
