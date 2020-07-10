package com.nokia.report.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;

@Mapper
public interface DynamicSqlMapper {

	List<HashMap<String, Object>> getListBySql(@Param("sqlStr") String sqlStr);

	List<String> getStrsBySql(@Param("sqlStr")String sqlStr);

	String querySqlByParam(@Param("sqlName")String sqlName, @Param("sqlType")String sqlType,@Param("project") String project, @Param("module")String module);

	String findPasswordByUserId(@Param("userId")String userId);

	List<HashMap<String, Object>> queryLongitudeRange();
}