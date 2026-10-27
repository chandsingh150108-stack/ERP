package com.studenterp.dto;

import lombok.Data;
import java.util.Map;

@Data
public class DashboardStats {
    private long totalStudents;
    private long totalFaculty;
    private long totalDepartments;
    private long totalPrograms;
    private long activeCourses;
    private long pendingFees;
    private long openComplaints;
    private long upcomingEvents;
    private long totalBooks;
    private long issuedBooks;
}
