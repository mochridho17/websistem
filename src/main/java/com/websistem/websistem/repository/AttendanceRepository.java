package com.websistem.websistem.repository;

import com.websistem.websistem.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, String> {
    @Query("""
    SELECT a.badgenumber, a.checktime
    FROM Attendance a
    JOIN EmployeeFiwa e ON a.badgenumber = e.employeeNo
    WHERE a.checktime >= :startDate AND a.checktime <= :endDate
      AND e.factory = :factory
      AND (:badgenumber IS NULL OR a.badgenumber = :badgenumber)
    """)
    List<Object[]> findSyncedAttendance(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("factory") String factory,
        @Param("badgenumber") String badgenumber
    );
}