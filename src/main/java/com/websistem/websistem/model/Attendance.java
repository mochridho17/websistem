package com.websistem.websistem.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "data_attendance")
@Data
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dept_code")
    private String deptCode;

    @Column(name = "employee_nik")
    private String employeeNik;

    @Column(name = "name")
    private String name;

    @Column(name = "tanggal")
    private String tanggal;

    @Column(name = "jam")
    private String jam;

    @Column(name = "factory")
    private String factory;
}