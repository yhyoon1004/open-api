package yh_project.openapi.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

public class GeminiDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private List<Content> contents;
        private Content systemInstruction;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Content {
            private String role;
            private List<Part> parts;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Part {
            private String text;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private List<Candidate> candidates;
        private UsageMetadata usageMetadata;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Candidate {
            private Request.Content content;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class UsageMetadata {
            private int promptTokenCount;
            private int candidatesTokenCount;
            private int totalTokenCount;
        }
    }
}
