package com.websistem.websistem.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "employee_fiwa")
@Data
public class EmployeeFiwa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_no")
    private String employeeNo;

    @Column(name = "name")
    private String name;

    @Column(name = "gender")
    private String gender;

    @Column(name = "dept_code")
    private String deptCode;

    @Column(name = "group_name")
    private String groupName;

    @Column(name = "start_date")
    private String startDate;

    @Column(name = "factory")
    private String factory;
}