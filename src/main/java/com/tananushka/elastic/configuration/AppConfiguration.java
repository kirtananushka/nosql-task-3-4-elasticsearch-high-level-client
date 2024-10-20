package com.tananushka.elastic.configuration;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfiguration {

   @Bean
   public ElasticsearchClient elasticsearchClient() {
      RestClient restClient = RestClient.builder(new HttpHost("localhost", 9200, "http")).build();

      ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());

      return new ElasticsearchClient(transport);
   }
   
   @Bean
   public ObjectMapper objectMapper() {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
      return objectMapper;
   }

   @Bean
   public OpenAPI customOpenAPI() {
      return new OpenAPI()
            .info(new Info()
                  .title("Employee API (With Java High Level API Client)")
                  .version("1.0")
                  .description("API for managing employees in Elasticsearch"));
   }
}
