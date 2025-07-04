package com.websistem.websistem.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_log")
@Data
public class Attendance {
    @Id
    @Column(name = "badgenumber")
    private String badgenumber;

    @Column(name = "name")
    private String name;

    @Column(name = "checktime")
    private LocalDateTime checktime;
}