package kr.hhplus.be.server;

import jakarta.annotation.PreDestroy;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

	private static final String REDIS_IMAGE = "redis:latest";
	private static final int REDIS_PORT = 6379;
	private static final String MYSQL_IMAGE = "mysql:8.0";
	private static final String KAFKA_IMAGE = "confluentinc/cp-kafka:latest";

	private static class CustomMySQLContainer extends MySQLContainer<CustomMySQLContainer> {
		public CustomMySQLContainer(DockerImageName dockerImageName) {
			super(dockerImageName);
		}

		@Override
		public String getJdbcUrl() {
			return super.getJdbcUrl() + "?characterEncoding=UTF-8&serverTimezone=UTC";
		}
	}

	@Bean
	@ServiceConnection("redis")
	GenericContainer<?> redisContainer() {
		return new GenericContainer<>(DockerImageName.parse(REDIS_IMAGE))
				.withExposedPorts(REDIS_PORT)
				.withReuse(true);
	}

	@Bean
	@ServiceConnection
	MySQLContainer<?> mysqlContainer() {
		return new CustomMySQLContainer(DockerImageName.parse(MYSQL_IMAGE))
				.withDatabaseName("hhplus")
				.withUsername("test")
				.withPassword("test")
				.withReuse(true);
	}

	@Bean
	@ServiceConnection
	KafkaContainer kafkaContainer() {
		return new KafkaContainer(DockerImageName.parse(KAFKA_IMAGE))
				.withReuse(true);
	}
}
