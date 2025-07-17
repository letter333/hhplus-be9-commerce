package kr.hhplus.be.server.config.Swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("항해플러스 백엔드 9기 API")
                        .version("v1.0")
                        .description("항해플러스 백엔드 9기 2주차 MockAPI"));
    }

    @Bean
    public GroupedOpenApi api() {
        String[] paths = {"/api/v1/**"};
        String[] packagesToScan = {"kr.hhplus.be.server.mock"};
        return GroupedOpenApi.builder().group("hhplus-be9")
                .pathsToMatch(paths)
                .packagesToScan(packagesToScan)
                .build();
    }
}
