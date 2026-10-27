package com.studenterp.controller;

import com.studenterp.entity.*;
import com.studenterp.exception.ResourceNotFoundException;
import com.studenterp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/timetable")
@RequiredArgsConstructor
public class TimetableController {

    private final TimeSlotRepository timeSlotRepository;
    private final RoomRepository roomRepository;
    private final StudentSectionRepository studentSectionRepository;
    private final FacultyRepository facultyRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final SectionRepository sectionRepository;
    private final CourseRepository courseRepository;
    private final FacultyCourseRepository facultyCourseRepository;

    private static final List<String> DAYS = List.of("MON", "TUE", "WED", "THU", "FRI", "SAT");
    private static final List<Integer> SLOTS = List.of(1, 2, 3, 4, 5, 6, 7, 8);

    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<TimeSlot>> getMyTimetable(Authentication auth) {
        Long studentId = resolveStudentId(auth);
        List<StudentSection> enrollments = studentSectionRepository.findByStudentId(studentId);
        List<TimeSlot> timetable = new ArrayList<>();
        for (StudentSection ss : enrollments) {
            timetable.addAll(timeSlotRepository.findBySectionIdOrderByDayOfWeekAscSlotNumberAsc(ss.getSection().getId()));
        }
        return ResponseEntity.ok(timetable);
    }

    @GetMapping("/faculty/me")
    @PreAuthorize("hasRole('FACULTY')")
    public ResponseEntity<List<TimeSlot>> getFacultyTimetable(Authentication auth) {
        String username = auth.getName();
        Faculty faculty = facultyRepository.findByUsername(username).orElse(null);
        if (faculty != null) {
            return ResponseEntity.ok(timeSlotRepository.findByFacultyIdOrderByDayOfWeekAscSlotNumberAsc(faculty.getId()));
        }
        return ResponseEntity.ok(new ArrayList<>());
    }

    @GetMapping("/section/{id}")
    public ResponseEntity<List<TimeSlot>> getSectionTimetable(@PathVariable Long id) {
        return ResponseEntity.ok(timeSlotRepository.findBySectionIdOrderByDayOfWeekAscSlotNumberAsc(id));
    }

    @GetMapping("/days")
    public ResponseEntity<List<String>> getDays() {
        return ResponseEntity.ok(DAYS);
    }

    @GetMapping("/slots")
    public ResponseEntity<List<Integer>> getSlots() {
        return ResponseEntity.ok(SLOTS);
    }

    @GetMapping("/rooms")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Room>> getRooms() {
        return ResponseEntity.ok(roomRepository.findAll());
    }

    @PostMapping("/generate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> generateTimetable() {
        // Auto-generate from FacultyCourse assignments
        List<FacultyCourse> facultyCourses = facultyCourseRepository.findAll();
        int created = 0;

        for (FacultyCourse fc : facultyCourses) {
            // Check if time slots already exist for this section+course
            List<TimeSlot> existing = timeSlotRepository.findBySectionIdOrderByDayOfWeekAscSlotNumberAsc(fc.getSection().getId());
            boolean hasCourse = existing.stream().anyMatch(ts -> ts.getCourse().getId().equals(fc.getCourse().getId()));
            if (hasCourse) continue;

            // Find available slot
            for (String day : DAYS) {
                boolean dayHasCourse = existing.stream().anyMatch(ts -> ts.getDayOfWeek().equals(day) && ts.getCourse().getId().equals(fc.getCourse().getId()));
                if (dayHasCourse) continue;

                for (Integer slot : SLOTS) {
                    final int slotNum = slot;
                    boolean slotTaken = existing.stream().anyMatch(ts -> ts.getDayOfWeek().equals(day) && ts.getSlotNumber().equals(slotNum));
                    if (slotTaken) continue;

                    // Find available room
                    List<Room> rooms = roomRepository.findAll();
                    Room availableRoom = rooms.stream().filter(r -> {
                        List<TimeSlot> roomSlots = timeSlotRepository.findByRoomIdAndDayOfWeekOrderBySlotNumberAsc(r.getId(), day);
                        return roomSlots.stream().noneMatch(ts -> ts.getSlotNumber().equals(slotNum));
                    }).findFirst().orElse(rooms.isEmpty() ? null : rooms.get(0));

                    if (availableRoom != null) {
                        int hour = 8 + slotNum;
                        TimeSlot ts = TimeSlot.builder()
                                .dayOfWeek(day)
                                .startTime(java.time.LocalTime.of(hour, 0))
                                .endTime(java.time.LocalTime.of(hour + 1, 0))
                                .slotNumber(slot)
                                .course(fc.getCourse())
                                .faculty(fc.getFaculty())
                                .room(availableRoom)
                                .section(fc.getSection())
                                .type("LECTURE")
                                .build();
                        timeSlotRepository.save(ts);
                        created++;
                        break;
                    }
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("generated", created);
        result.put("message", "Generated " + created + " time slots");
        return ResponseEntity.ok(result);
    }

    @PostMapping("/room")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Room> createRoom(@RequestBody Room room) {
        return ResponseEntity.ok(roomRepository.save(room));
    }

    @PostMapping("/slot")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TimeSlot> createSlot(@RequestBody TimeSlot timeSlot) {
        return ResponseEntity.ok(timeSlotRepository.save(timeSlot));
    }

    @PutMapping("/slot/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TimeSlot> updateSlot(@PathVariable Long id, @RequestBody TimeSlot updated) {
        TimeSlot existing = timeSlotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TimeSlot", "id", id));
        if (updated.getDayOfWeek() != null) existing.setDayOfWeek(updated.getDayOfWeek());
        if (updated.getStartTime() != null) existing.setStartTime(updated.getStartTime());
        if (updated.getEndTime() != null) existing.setEndTime(updated.getEndTime());
        if (updated.getSlotNumber() != null) existing.setSlotNumber(updated.getSlotNumber());
        if (updated.getCourse() != null && updated.getCourse().getId() != null) {
            Course c = courseRepository.findById(updated.getCourse().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Course", "id", updated.getCourse().getId()));
            existing.setCourse(c);
        }
        if (updated.getFaculty() != null && updated.getFaculty().getId() != null) {
            Faculty f = facultyRepository.findById(updated.getFaculty().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Faculty", "id", updated.getFaculty().getId()));
            existing.setFaculty(f);
        }
        if (updated.getRoom() != null && updated.getRoom().getId() != null) {
            Room r = roomRepository.findById(updated.getRoom().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Room", "id", updated.getRoom().getId()));
            existing.setRoom(r);
        }
        if (updated.getSection() != null && updated.getSection().getId() != null) {
            Section s = sectionRepository.findById(updated.getSection().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Section", "id", updated.getSection().getId()));
            existing.setSection(s);
        }
        if (updated.getType() != null) existing.setType(updated.getType());
        return ResponseEntity.ok(timeSlotRepository.save(existing));
    }

    @DeleteMapping("/slot/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSlot(@PathVariable Long id) {
        timeSlotRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    private Long resolveStudentId(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        return studentRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "user", username))
                .getId();
    }
}
