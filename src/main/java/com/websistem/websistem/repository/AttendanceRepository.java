package com.websistem.websistem.repository;

import com.websistem.websistem.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    @Query("SELECT a FROM Attendance a " +
   "WHERE a.tanggal >= :startDate AND a.tanggal <= :endDate " +
   "AND (:factory IS NULL OR a.factory = :factory) " +
   "AND (:employeeNik IS NULL OR a.employeeNik = :employeeNik)")
List<Attendance> findByTanggalBetweenAndFactory(
    @Param("startDate") String startDate,
    @Param("endDate") String endDate,
    @Param("factory") String factory,
    @Param("employeeNik") String employeeNik
);
}