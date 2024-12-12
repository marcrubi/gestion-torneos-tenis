//// src/main/java/com/ISII/gestion_torneos_tenis/config/MailConfig.java
//
//package com.ISII.gestion_torneos_tenis.config;
//
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//@ConfigurationProperties(prefix = "spring.mail")
//public class MailConfig {
//    private String host;
//    private Integer port;
//    private String username;
//    private String password;
//    private Properties properties;
//    private String from;
//
//    // Getters y Setters
//
//    public static class Properties {
//        private SMTP smtp;
//
//        // Getters y Setters
//
//        public static class SMTP {
//            private Boolean auth;
//            private Boolean starttlsEnable;
//            private Boolean starttlsRequired;
//            private Integer connectiontimeout;
//            private Integer timeout;
//            private Integer writetimeout;
//
//            // Getters y Setters
//        }
//
//        // Getters y Setters
//    }
//
//    // Getters y Setters
//}
