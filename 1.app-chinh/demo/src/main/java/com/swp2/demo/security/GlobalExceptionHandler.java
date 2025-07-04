package com.swp2.demo.security; // Or a dedicated 'exception' package

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.PrintWriter;
import java.io.StringWriter;

@ControllerAdvice // This annotation makes it a global exception handler
public class GlobalExceptionHandler {

    // Handles 403 Access Denied errors
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN) // Sets the HTTP status code to 403
    public String handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request, Model model) {

        System.err.println("GlobalExceptionHandler caught AccessDeniedException for URI: " + request.getRequestURI());
        ex.printStackTrace(); // For development debugging

        // Check if the user is an Admin and should be redirected
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("Admin"));

            if (isAdmin) {
                // Redirect Admins to their dashboard on 403 (Access Denied)
                return "redirect:/admin";
            }
        }

        // For non-admins or if redirect didn't occur, show the 403 page
        model.addAttribute("status", HttpStatus.FORBIDDEN.value());
        model.addAttribute("error", "Access Denied");
        model.addAttribute("message", "You do not have permission to access this page.");
        model.addAttribute("trace", getStackTrace(ex)); // Include stack trace for development

        return "403"; // Return the name of your 403.html Thymeleaf template
    }

    // Handles all other unexpected exceptions (typically results in 500 Internal Server Error)
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // Sets the HTTP status code to 500
    public String handleGenericException(Exception ex, HttpServletRequest request, Model model) {

        System.err.println("GlobalExceptionHandler caught general Exception for URI: " + request.getRequestURI());
        ex.printStackTrace(); // For development debugging

        model.addAttribute("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        model.addAttribute("error", "Internal Server Error");
        model.addAttribute("message", "An unexpected error occurred. Please try again later.");
        model.addAttribute("trace", getStackTrace(ex)); // Include stack trace for development

        return "500"; // Return the name of your 500.html Thymeleaf template
    }

    // Helper method to convert stack trace to string
    private String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }
}