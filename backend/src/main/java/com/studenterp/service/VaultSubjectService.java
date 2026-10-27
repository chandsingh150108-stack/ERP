package com.studenterp.service;

import com.studenterp.entity.*;
import com.studenterp.exception.ResourceNotFoundException;
import com.studenterp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class VaultSubjectService {

    private final VaultSubjectRepository vaultSubjectRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final VaultItemRepository vaultItemRepository;
    private final NoteHubSubjectRepository noteHubSubjectRepository;

    private static final String[] PALETTE = {
            "#3b82f6", "#10b981", "#8b5cf6", "#f59e0b",
            "#ec4899", "#06b6d4", "#f97316", "#6366f1"
    };

    public List<VaultSubject> findAllByStudent(Long studentId) {
        return findAllByStudent(studentId, null);
    }

    public List<VaultSubject> findAllByStudent(Long studentId, String yearFilter) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));

        List<VaultSubject> existing = vaultSubjectRepository.findByStudentId(studentId);
        if (existing.size() < 10 && student.getCourse() != null) {
            provisionCourseSubjects(student);
            existing = vaultSubjectRepository.findByStudentId(studentId);
        }

        // Backfill year and studyYear if missing on older records
        for (VaultSubject s : existing) {
            if (s.getStudyYear() == null || s.getYear() == null) {
                if (s.getCourse() != null && s.getCourse().getStudyYear() != null) {
                    s.setStudyYear(s.getCourse().getStudyYear());
                } else {
                    s.setStudyYear(student.getStudyYear() != null ? student.getStudyYear() : 2);
                }
                s.setYear(s.getStudyYear() == 1 ? "FY" : (s.getStudyYear() == 2 ? "SY" : "TY"));
                vaultSubjectRepository.save(s);
            }
        }

        if (yearFilter != null && !yearFilter.isBlank() && !yearFilter.equalsIgnoreCase("ALL")) {
            String yf = yearFilter.trim().toUpperCase();
            return existing.stream()
                    .filter(s -> (s.getYear() != null && s.getYear().equalsIgnoreCase(yf)) ||
                                 (yf.equals("FY") && Integer.valueOf(1).equals(s.getStudyYear())) ||
                                 (yf.equals("SY") && Integer.valueOf(2).equals(s.getStudyYear())) ||
                                 (yf.equals("TY") && Integer.valueOf(3).equals(s.getStudyYear())) ||
                                 (yf.equals("FINAL") && Integer.valueOf(4).equals(s.getStudyYear())))
                    .toList();
        }

        return existing;
    }

    public VaultSubject findById(Long id, Long studentId) {
        return vaultSubjectRepository.findByIdAndStudentId(id, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("VaultSubject", "id", id));
    }

    public VaultSubject create(VaultSubject subject, Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));
        subject.setStudent(student);

        if (vaultSubjectRepository.existsByStudentIdAndName(studentId, subject.getName())) {
            throw new IllegalArgumentException("Subject with name '" + subject.getName() + "' already exists");
        }

        if (subject.getCourse() != null && subject.getCourse().getId() != null) {
            Course course = courseRepository.findById(subject.getCourse().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Course", "id", subject.getCourse().getId()));
            if (vaultSubjectRepository.existsByStudentIdAndCourseId(studentId, course.getId())) {
                throw new IllegalArgumentException("Subject linked to this course already exists");
            }
            subject.setCourse(course);
            if (subject.getStudyYear() == null && course.getStudyYear() != null) {
                subject.setStudyYear(course.getStudyYear());
                subject.setYear(course.getStudyYear() == 1 ? "FY" : (course.getStudyYear() == 2 ? "SY" : "TY"));
            }
        } else {
            subject.setCourse(null);
        }

        subject.syncAcademicYear();
        return vaultSubjectRepository.save(subject);
    }

    public VaultSubject update(Long id, VaultSubject updated, Long studentId) {
        VaultSubject existing = findById(id, studentId);

        if (!existing.getName().equals(updated.getName()) &&
            vaultSubjectRepository.existsByStudentIdAndName(studentId, updated.getName())) {
            throw new IllegalArgumentException("Subject with name '" + updated.getName() + "' already exists");
        }

        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setColor(updated.getColor());
        if (updated.getYear() != null) existing.setYear(updated.getYear());
        if (updated.getStudyYear() != null) existing.setStudyYear(updated.getStudyYear());

        if (updated.getCourse() != null && updated.getCourse().getId() != null) {
            Course course = courseRepository.findById(updated.getCourse().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Course", "id", updated.getCourse().getId()));
            existing.setCourse(course);
            if (course.getStudyYear() != null) {
                existing.setStudyYear(course.getStudyYear());
                existing.setYear(course.getStudyYear() == 1 ? "FY" : (course.getStudyYear() == 2 ? "SY" : "TY"));
            }
        }

        existing.syncAcademicYear();
        return vaultSubjectRepository.save(existing);
    }

    @Transactional
    public void delete(Long id, Long studentId) {
        VaultSubject subject = findById(id, studentId);
        List<VaultItem> items = vaultItemRepository.findBySubjectId(id);
        for (VaultItem item : items) {
            item.setSubject(null);
            vaultItemRepository.save(item);
        }
        vaultSubjectRepository.delete(subject);
    }

    @Transactional
    public List<VaultSubject> syncCourseCurriculum(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));
        provisionCourseSubjects(student);
        return vaultSubjectRepository.findByStudentId(studentId);
    }

    public List<Map<String, Object>> getAvailableCoursesForStudent(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));
        String dept = resolveDepartmentCode(student);
        List<NoteHubSubject> nhSubjects = noteHubSubjectRepository.findForStudentCourse(dept);

        List<Map<String, Object>> list = new ArrayList<>();
        for (NoteHubSubject s : nhSubjects) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", s.getCourse().getId());
            m.put("code", s.getCode());
            m.put("name", s.getName());
            m.put("studyYear", s.getStudyYear());
            m.put("year", s.getStudyYear() == 1 ? "FY" : (s.getStudyYear() == 2 ? "SY" : (s.getStudyYear() == 3 ? "TY" : "FINAL")));
            m.put("courseContext", student.getCourse());
            m.put("departmentCode", s.getCourse().getDepartment() != null ? s.getCourse().getDepartment().getCode() : "");
            list.add(m);
        }
        return list;
    }

    @Transactional
    public void provisionCourseSubjects(Student student) {
        String dept = resolveDepartmentCode(student);
        List<NoteHubSubject> nhSubjects = noteHubSubjectRepository.findForStudentCourse(dept);
        String courseName = student.getCourse() != null ? student.getCourse() : "Curriculum";

        Set<String> seen = new HashSet<>();
        for (VaultSubject existingSub : vaultSubjectRepository.findByStudentId(student.getId())) {
            seen.add(existingSub.getName().trim().toLowerCase());
        }

        int i = 0;
        for (NoteHubSubject nhs : nhSubjects) {
            String cleanName = nhs.getName().trim().toLowerCase();
            if (!seen.contains(cleanName)) {
                seen.add(cleanName);
                String yStr = nhs.getStudyYear() == 1 ? "FY" : (nhs.getStudyYear() == 2 ? "SY" : "TY");
                String desc = yStr + (nhs.getStudyYear() == 1 ? " Foundation (" : " Core (") + courseName + ")";
                VaultSubject vs = VaultSubject.builder()
                        .name(nhs.getName())
                        .description(desc)
                        .color(PALETTE[i % PALETTE.length])
                        .studyYear(nhs.getStudyYear())
                        .year(yStr)
                        .student(student)
                        .course(nhs.getCourse())
                        .build();
                vaultSubjectRepository.save(vs);
                i++;
            }
        }
    }

    private String resolveDepartmentCode(Student student) {
        if (student.getProgram() != null && student.getProgram().getDepartment() != null) {
            String c = student.getProgram().getDepartment().getCode();
            if (c != null && !c.isBlank()) return c.trim().toUpperCase();
        }
        String cName = student.getCourse();
        if (cName == null && student.getProgram() != null) {
            cName = student.getProgram().getCode();
        }
        if (cName != null) {
            String upper = cName.toUpperCase();
            if (upper.contains("AIML")) return "AIML";
            if (upper.contains("CSE") || upper.contains("COMPUTER")) return "CSE";
            if (upper.contains("IT") || upper.contains("INFORMATION")) return "IT";
            if (upper.contains("AIDS")) return "AIDS";
            if (upper.contains("ENTC")) return "ENTC";
            if (upper.contains("MECH")) return "MECH";
            if (upper.contains("CHEM")) return "CHEM";
            if (upper.contains("INST")) return "INST";
        }
        return "AIML";
    }
}
