package com.example.blog.controller;

import com.example.blog.model.Logs;
import com.example.blog.service.LogsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/logs")
public class LogsController {
    @Autowired
    private LogsService logsService;

    @PostMapping("/add")
    public String addLogs(String logName, String logDescribe) {
        Logs logs = new Logs();
        logs.setLogName(logName);
        logs.setLogDescribe(logDescribe);
        logs.setTime(LocalDateTime.now());
        logsService.insertLog(logs);

        return "添加成功";
    }
}
