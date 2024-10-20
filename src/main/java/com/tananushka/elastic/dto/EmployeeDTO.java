package com.tananushka.elastic.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmployeeDTO {
   private String id;
   private String name;
   private String dob;
   private AddressDTO address;
   private String email;
   private String[] skills;
   private int experience;
   private double rating;
   private String description;
   private boolean verified;
   private int salary;

   @Data
   @JsonInclude(JsonInclude.Include.NON_NULL)
   public static class AddressDTO {
      private String country;
      private String town;
   }
}
