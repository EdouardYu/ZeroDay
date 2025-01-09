package com.software.security.zeroday.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Controller
@RequestMapping("admin")
public class AdminController {
    @GetMapping("logs")
    public String viewLogs(Model model) {
        try {
            Path logFilePath = Path.of("logs/user-logs.log");
            List<String> logs = Files.exists(logFilePath)
                ? Files.readAllLines(logFilePath)
                : List.of("No logs available");
            model.addAttribute("logs", logs);
        } catch (Exception e) {
            model.addAttribute("logs", List.of("Error reading log file: " + e.getMessage()));
        }
        return "admin/logs";
    }
}
