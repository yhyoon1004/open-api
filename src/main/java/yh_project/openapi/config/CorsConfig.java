package yh_project.openapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
        .allowedOrigins(
                "http://localhost:3000",
                "https://yunhwan.kr",
                "https://www.yunhwan.kr",
                "https://rest.renteasy.co.kr",
                "https://renteasy.co.kr",
                "https://www.renteasy.co.kr"
        )
        .allowedMethods("GET", "POST", "PUT", "DELETE","OPTIONS")
        .allowedHeaders("*")
        .allowCredentials(true)
        .maxAge(3600);
  }
}
