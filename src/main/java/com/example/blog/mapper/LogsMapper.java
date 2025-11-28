package com.example.blog.mapper;

import com.example.blog.model.Logs;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

@Mapper
public interface LogsMapper {
    @Insert("insert into logs(log_name, log_describe, time) values(#{logName}, #{logDescribe}, #{time})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertLog(Logs logs);
}
