package com.tananushka.elastic.controller;

import com.tananushka.elastic.dto.EmployeeDTO;
import com.tananushka.elastic.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

   private final EmployeeService employeeService;

   @Operation(
         summary = "Get all employees",
         description = "Retrieve a list of all employees. Default page size is 10.",
         parameters = {
               @Parameter(name = "page", description = "The page number (0-based index)", example = "0"),
               @Parameter(name = "size", description = "The size of the page (number of employees per page)", example = "10")
         },
         responses = {
               @ApiResponse(responseCode = "200", description = "Employees retrieved successfully"),
               @ApiResponse(responseCode = "400", description = "Invalid input data")
         }
   )
   @GetMapping
   public ResponseEntity<List<EmployeeDTO>> getAllEmployees(
         @RequestParam(defaultValue = "0") int page,
         @RequestParam(defaultValue = "10") int size) throws IOException {
      List<EmployeeDTO> employees = employeeService.getAllEmployees(page, size);
      return ResponseEntity.ok(employees);
   }

   @Operation(summary = "Get employee by ID", description = "Retrieve employee details by ID")
   @GetMapping("/{id}")
   public ResponseEntity<EmployeeDTO> getEmployeeById(@PathVariable String id) throws IOException {
      EmployeeDTO employee = employeeService.getEmployeeById(id);
      return ResponseEntity.ok(employee);
   }

   @Operation(
         summary = "Create a new employee",
         description = "Create a new employee in the system using the provided ID and employee data",
         parameters = {
               @Parameter(
                     name = "id",
                     description = "ID of the employee",
                     required = true,
                     example = "1"
               )
         },
         requestBody = @RequestBody(
               description = "Employee data to be created",
               required = true,
               content = @Content(
                     mediaType = "application/json",
                     schema = @Schema(implementation = EmployeeDTO.class),
                     examples = @ExampleObject(
                           name = "Create Employee Example",
                           value = """
                                 {
                                     "name": "Ana Brown",
                                     "dob": "1993-03-19",
                                     "address": {
                                         "country": "Belarus",
                                         "town": "Gomel"
                                     },
                                     "email": "anabrown9@gmail.com",
                                     "skills": ["Java", "AWS"],
                                     "experience": 10,
                                     "rating": 9.2,
                                     "description": "confident, ambitious, highly motivated Java experience interview learning python",
                                     "verified": true,
                                     "salary": 30000
                                 }
                                 """
                     )
               )
         ),
         responses = {
               @ApiResponse(responseCode = "200", description = "Employee created successfully"),
               @ApiResponse(responseCode = "400", description = "Invalid input data")
         }
   )
   @PostMapping("/{id}")
   public ResponseEntity<String> createEmployee(@PathVariable String id, @org.springframework.web.bind.annotation.RequestBody EmployeeDTO employee) throws IOException {
      String result = employeeService.createEmployee(id, employee);
      return ResponseEntity.ok(result);
   }

   @Operation(
         summary = "Delete employee by ID",
         description = "Delete an employee by their ID",
         parameters = {
               @Parameter(
                     name = "id",
                     description = "ID of the employee",
                     required = true,
                     example = "1"
               )
         })
   @DeleteMapping("/{id}")
   public ResponseEntity<String> deleteEmployeeById(@PathVariable String id) throws IOException {
      String result = employeeService.deleteEmployeeById(id);
      return ResponseEntity.ok(result);
   }

   @Operation(
         summary = "Search employees",
         description = "Search for employees by a specific field, value, and query type.",
         parameters = {
               @Parameter(
                     name = "field",
                     description = "Field to search in",
                     required = true,
                     examples = {
                           @ExampleObject(name = "Search by Skill", value = "skills"),
                           @ExampleObject(name = "Search by Name", value = "name"),
                           @ExampleObject(name = "Search by Email", value = "email")
                     }
               ),
               @Parameter(
                     name = "value",
                     description = "Value to search for in the specified field",
                     required = true,
                     examples = {
                           @ExampleObject(name = "Search for Java skill", value = "Java"),
                           @ExampleObject(name = "Search for Name", value = "Brandon"),
                           @ExampleObject(name = "Search for Email", value = "brandon6089@gmail.com")
                     }
               ),
               @Parameter(
                     name = "queryType",
                     description = "Type of query to use: 'match' or 'term'",
                     required = true,
                     examples = {
                           @ExampleObject(name = "Match Query", value = "match"),
                           @ExampleObject(name = "Term Query", value = "term")
                     }
               )
         },
         responses = {
               @ApiResponse(responseCode = "200", description = "Employees retrieved successfully"),
               @ApiResponse(responseCode = "400", description = "Invalid input data"),
               @ApiResponse(responseCode = "404", description = "No employees found")
         }
   )
   @GetMapping("/search")
   public ResponseEntity<String> searchEmployees(
         @RequestParam String field,
         @RequestParam String value,
         @RequestParam String queryType) throws IOException {
      String result = employeeService.searchEmployees(field, value, queryType);
      return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(result);
   }

   @Operation(
         summary = "Aggregate employees",
         description = "Perform an aggregation on a specific numeric field with metric calculation. You can also filter based on a field and its value.",
         parameters = {
               @Parameter(name = "field", description = "Field to filter by (e.g., skills)", required = true,
                     examples = {
                           @ExampleObject(name = "Filter by skill", value = "skills")
                     }),
               @Parameter(name = "fieldValue", description = "Value for the filter field (e.g., Java for skills)", required = true,
                     examples = {
                           @ExampleObject(name = "Filter value", value = "Java")
                     }),
               @Parameter(name = "metricType", description = "Type of metric aggregation (e.g., avg, min, max)", required = true,
                     examples = {
                           @ExampleObject(name = "Average", value = "avg"),
                           @ExampleObject(name = "Max", value = "max"),
                           @ExampleObject(name = "Min", value = "min")
                     }),
               @Parameter(name = "metricField", description = "The numeric field to apply the metric on (e.g., salary, experience)", required = true,
                     examples = {
                           @ExampleObject(name = "Salary", value = "salary"),
                           @ExampleObject(name = "Experience", value = "experience")
                     })
         },
         responses = {
               @ApiResponse(responseCode = "200", description = "Aggregation performed successfully"),
               @ApiResponse(responseCode = "400", description = "Invalid input parameters")
         }
   )
   @GetMapping("/aggregate")
   public ResponseEntity<String> aggregateEmployees(
         @RequestParam String field,
         @RequestParam String fieldValue,
         @RequestParam String metricType,
         @RequestParam String metricField) throws IOException {
      String result = employeeService.aggregateEmployees(field, fieldValue, metricType, metricField);
      return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(result);
   }
}
