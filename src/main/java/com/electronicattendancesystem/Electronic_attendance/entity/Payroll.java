// Payroll.java (Entity)
package com.electronicattendancesystem.Electronic_attendance.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Entity
@Data
public class Payroll {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @ManyToOne
   @ToString.Exclude
   private Teachers teacher;

   private int year;
   private int month;

   private int totalCredits;
   private double creditRate;

   private double grossSalary;
   private double tax;
   private double netSalary;
}
