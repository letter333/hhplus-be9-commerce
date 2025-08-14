package kr.hhplus.be.server;

import jakarta.annotation.PreDestroy;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

@Configuration
class TestcontainersConfiguration {
	private static final String REDIS_IMAGE = "redis:latest";
	private static final int REDIS_PORT = 6379;
	private static final String MYSQL_IMAGE = "mysql:8.0";

	private static final GenericContainer REDIS_CONTAINER;
	private static final MySQLContainer<?> MYSQL_CONTAINER;

	static {
		REDIS_CONTAINER = new GenericContainer<>(REDIS_IMAGE)
				.withExposedPorts(REDIS_PORT)
				.withReuse(true);
		MYSQL_CONTAINER = new MySQLContainer<>(DockerImageName.parse(MYSQL_IMAGE))
				.withDatabaseName("hhplus")
				.withUsername("test")
				.withPassword("test");

		REDIS_CONTAINER.start();
		MYSQL_CONTAINER.start();
		System.setProperty("spring.datasource.password", MYSQL_CONTAINER.getPassword());
		System.setProperty("spring.datasource.url", MYSQL_CONTAINER.getJdbcUrl() + "?characterEncoding=UTF-8&serverTimezone=UTC");
		System.setProperty("spring.datasource.username", MYSQL_CONTAINER.getUsername());
		System.setProperty("spring.datasource.password", MYSQL_CONTAINER.getPassword());

		System.setProperty("spring.data.redis.host", REDIS_CONTAINER.getHost());
		System.setProperty("spring.data.redis.port", REDIS_CONTAINER.getMappedPort(REDIS_PORT).toString());
	}

	@PreDestroy
	public void preDestroy() {
		if (MYSQL_CONTAINER.isRunning()) {
			MYSQL_CONTAINER.stop();
		}

		if (REDIS_CONTAINER.isRunning()) {
			REDIS_CONTAINER.stop();
		}
	}
}