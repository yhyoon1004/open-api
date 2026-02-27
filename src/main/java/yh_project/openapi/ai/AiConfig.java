package yh_project.openapi.ai;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
@Configuration
@ConfigurationProperties(prefix = "ai")
@Data
public class AiConfig {
    private String systemPrompt = "You are a helpful assistant.";
    private double costLimitKrw = 30000.0;
    private int reqLimitPerUser = 10;
}
