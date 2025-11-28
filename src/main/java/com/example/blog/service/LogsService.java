package com.example.blog.service;

import com.example.blog.mapper.LogsMapper;
import com.example.blog.model.Logs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LogsService {
    @Autowired
    private LogsMapper logsMapper;

    public void insertLog(Logs logs){
        logsMapper.insertLog(logs);
    }

}
