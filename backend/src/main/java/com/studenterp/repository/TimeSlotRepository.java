package com.studenterp.repository;

import com.studenterp.entity.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {
    List<TimeSlot> findBySectionIdOrderByDayOfWeekAscSlotNumberAsc(Long sectionId);
    List<TimeSlot> findByFacultyIdOrderByDayOfWeekAscSlotNumberAsc(Long facultyId);
    List<TimeSlot> findByRoomIdAndDayOfWeekOrderBySlotNumberAsc(Long roomId, String dayOfWeek);
}
