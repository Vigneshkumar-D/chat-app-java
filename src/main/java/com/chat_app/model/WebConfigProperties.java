    package com.chat_app.model;

    import lombok.AllArgsConstructor;
    import lombok.Getter;
    import lombok.NoArgsConstructor;
    import lombok.Setter;
    import org.springframework.boot.context.properties.ConfigurationProperties;
    import org.springframework.stereotype.Component;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Component
    @ConfigurationProperties(prefix = "web")
    public class WebConfigProperties {

        private Cors cors;

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Cors {
            private String[] allowedOrigins;
            private String[] allowedMethods;
            private long maxAge;
            private String[] allowedHeaders;
            private String[] exposedHeaders;

        }
    }
