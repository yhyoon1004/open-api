package yh_project.openapi.ai;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
    private String userId;
    private String message;
}
