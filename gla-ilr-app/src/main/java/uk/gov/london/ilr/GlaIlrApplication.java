/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.jdbc.lock.DefaultLockRepository;
import org.springframework.integration.jdbc.lock.JdbcLockRegistry;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.thymeleaf.templateresolver.FileTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

import javax.sql.DataSource;
import java.util.function.Function;

@SpringBootApplication
public class GlaIlrApplication {

	@Autowired
	private ThymeleafProperties properties;

	@Autowired
	private JdbcTemplate template;

	@Value("${spring.thymeleaf.templates_root:}")
	private String templatesRoot;

	public static void main(String[] args) {
		SpringApplication.run(GlaIlrApplication.class, args);
	}

    @Profile("local")
	@Bean
	public ITemplateResolver defaultTemplateResolver() {
		FileTemplateResolver resolver = new FileTemplateResolver();
		resolver.setSuffix(properties.getSuffix());
		resolver.setPrefix(templatesRoot);
		resolver.setTemplateMode(properties.getMode());
		resolver.setCacheable(properties.isCache());
		return resolver;
	}

	@Bean
	public Function<String, String> currentUrlWithoutParam() {
		return param ->   ServletUriComponentsBuilder.fromCurrentRequest().replaceQueryParam(param).toUriString();
	}

	@Bean
	public JdbcLockRegistry getLockRegistry() {
		DataSource ds = template.getDataSource();
		DefaultLockRepository repo = new DefaultLockRepository(ds);
		repo.afterPropertiesSet();
		return new JdbcLockRegistry(repo);
	}

}
