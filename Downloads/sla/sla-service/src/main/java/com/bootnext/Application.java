package com.bootnext;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.bootnext.commons.configuration.ConfigUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityScheme;

@SpringBootApplication
@EntityScan(basePackages = { "com.bootnext.platform.sla" })
@Configuration
@ComponentScan({ "com.bootnext" })
@EnableFeignClients(basePackages = { "com.bootnext" })
public class Application implements WebMvcConfigurer {

	private Logger logger = LogManager.getLogger(Application.class);

	@Value("${springdoc.packagesToScan}")
	private String packageToScan;


	@Override
	public void configurePathMatch(PathMatchConfigurer configurer) {
		configurer.addPathPrefix("rest", HandlerTypePredicate.forAnnotation(RestController.class));
	}

	public static void main(String[] args) {
		ConfigUtils.setPropertiesFilePath("application.properties", "config.properties");
		SpringApplication.run(Application.class);
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");

		registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
	}

	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI().info(new Info().title("sla-api").version("3.1.0"))
				.components(new Components().addSecuritySchemes("default", createOAuthSecurityScheme()));

	}

	private SecurityScheme createOAuthSecurityScheme() {
		Scopes scopesArray = new Scopes();
		OAuthFlow oAuthFlow = new OAuthFlow().authorizationUrl("http://localhost/auth");
		try {

			logger.debug("Permission SCopes Size is " + scopesArray.size());
			oAuthFlow.scopes(scopesArray);

			oAuthFlow.scopes(readAuthorizationScopes(packageToScan, new Scopes()));

		} catch (Exception e) {
			logger.error("error while getting permission {}", e.getMessage());
		}
		return new SecurityScheme().type(SecurityScheme.Type.OAUTH2).description("Oauth2 flow")
				.flows(new OAuthFlows().implicit(oAuthFlow));

	}
	
	private static Scopes readAuthorizationScopes(String packageName, Scopes scopesArray) {
		List<String> scopes = new ArrayList<>();

		// Create a new instance of Reflections for the specified package
		Reflections reflections = new Reflections(packageName);

		// Get all classes with the @Operation annotation
		Set<Class<?>> classes = reflections.getTypesAnnotatedWith(FeignClient.class);

		// Process each class
		for (Class<?> clazz : classes) {

			// Process each method in the class
			for (java.lang.reflect.Method method : clazz.getDeclaredMethods()) {
				// Check for @Operation annotation
				if (method.isAnnotationPresent(Operation.class)) {
					Operation apiOperation = method.getAnnotation(Operation.class);

					// Check for @Authorization annotation
					if (apiOperation.security().length > 0) {
						io.swagger.v3.oas.annotations.security.SecurityRequirement[] security = apiOperation
								.security();

						// Process each authorization
						for (io.swagger.v3.oas.annotations.security.SecurityRequirement authorization : security) {
							// Check for @AuthorizationScope annotation
							if (authorization.scopes().length > 0) {
								String[] authorizationScopes = authorization.scopes();

								// Process each authorization scope
								for (String scope : authorizationScopes) {
									scopesArray.addString(scope, scope);
									scopes.add(scope);
//	                                    scope.description();

								}
							}
						}
					}
				}
			}
		}
		System.out.println(scopesArray.values());

		return scopesArray;
	}


}
