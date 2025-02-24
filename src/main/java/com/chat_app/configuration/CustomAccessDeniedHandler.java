package com.chat_app.configuration;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {


    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        System.out.println("Handled");
        // Set 401 status code
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        // Write custom error message or handle the exception as needed
        response.getWriter().write("Access  Denied : Unauthorized");
    }
}
