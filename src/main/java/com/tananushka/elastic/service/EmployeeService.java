package com.tananushka.elastic.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch.core.DeleteRequest;
import co.elastic.clients.elasticsearch.core.DeleteResponse;
import co.elastic.clients.elasticsearch.core.GetRequest;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tananushka.elastic.dto.EmployeeDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeService {

   private static final String INDEX_NAME = "employees";
   private final ElasticsearchClient elasticsearchClient;
   private final ObjectMapper objectMapper;

   public List<EmployeeDTO> getAllEmployees(int page, int size) throws IOException {
      int from = page * size;

      SearchRequest searchRequest = SearchRequest.of(s -> s
            .index(INDEX_NAME)
            .from(from)
            .size(size)
      );

      SearchResponse<EmployeeDTO> response = elasticsearchClient.search(searchRequest, EmployeeDTO.class);
      List<EmployeeDTO> employees = new ArrayList<>();

      response.hits().hits().stream()
            .map(hit -> Optional.ofNullable(hit.source())
                  .map(employee -> {
                     employee.setId(hit.id());
                     return employee;
                  })
                  .orElse(null))
            .filter(Objects::nonNull)
            .forEach(employees::add);

      return employees;
   }

   public EmployeeDTO getEmployeeById(String id) throws IOException {
      GetRequest getRequest = GetRequest.of(g -> g.index(INDEX_NAME).id(id));
      GetResponse<EmployeeDTO> getResponse = elasticsearchClient.get(getRequest, EmployeeDTO.class);

      return getResponse.source();
   }

   public String createEmployee(String id, EmployeeDTO employee) throws IOException {
      employee.setId(id);

      IndexRequest<EmployeeDTO> indexRequest = IndexRequest.of(i -> i
            .index(INDEX_NAME)
            .id(id)
            .document(employee)
      );
      IndexResponse indexResponse = elasticsearchClient.index(indexRequest);

      return indexResponse.result().jsonValue();
   }

   public String deleteEmployeeById(String id) throws IOException {
      DeleteRequest deleteRequest = DeleteRequest.of(d -> d.index(INDEX_NAME).id(id));
      DeleteResponse deleteResponse = elasticsearchClient.delete(deleteRequest);

      return deleteResponse.result().jsonValue();
   }

   public String searchEmployees(String field, String value, String queryType) throws IOException {
      SearchRequest searchRequest;

      if ("match".equalsIgnoreCase(queryType)) {
         searchRequest = SearchRequest.of(s -> s
               .index(INDEX_NAME)
               .query(q -> q
                     .match(m -> m.field(field).query(value))
               )
         );
      } else if ("term".equalsIgnoreCase(queryType)) {
         searchRequest = SearchRequest.of(s -> s
               .index(INDEX_NAME)
               .query(q -> q
                     .term(t -> t.field(field + ".keyword").value(value))
               )
         );
      } else {
         throw new IllegalArgumentException("Invalid query type. Use 'match' or 'term'.");
      }

      SearchResponse<EmployeeDTO> searchResponse = elasticsearchClient.search(searchRequest, EmployeeDTO.class);
      List<EmployeeDTO> employees = new ArrayList<>();

      searchResponse.hits().hits().forEach(hit -> employees.add(hit.source()));

      return objectMapper.writeValueAsString(employees);
   }

   public String aggregateEmployees(String field, String fieldValue, String metricType, String metricField) throws IOException {
      String aggregationName = metricType + "_" + metricField;

      SearchRequest searchRequest = SearchRequest.of(s -> s
            .index(INDEX_NAME)
            .query(q -> q.term(t -> t.field(field.endsWith(".keyword") ? field : field + ".keyword").value(fieldValue)))
            .aggregations(aggregationName, a -> switch (metricType.toLowerCase()) {
               case "avg" -> a.avg(aa -> aa.field(metricField));
               case "min" -> a.min(mm -> mm.field(metricField));
               case "max" -> a.max(ma -> ma.field(metricField));
               default -> throw new IllegalArgumentException("Unsupported metric type. Use 'avg', 'min', or 'max'.");
            })
      );

      SearchResponse<Void> searchResponse = elasticsearchClient.search(searchRequest, Void.class);

      Aggregate aggregate = searchResponse.aggregations().get(aggregationName);

      Double aggregationResult = null;
      if (aggregate.isMax()) {
         aggregationResult = aggregate.max().value();
      } else if (aggregate.isMin()) {
         aggregationResult = aggregate.min().value();
      } else if (aggregate.isAvg()) {
         aggregationResult = aggregate.avg().value();
      }

      return objectMapper.writeValueAsString(aggregationResult);
   }
}
