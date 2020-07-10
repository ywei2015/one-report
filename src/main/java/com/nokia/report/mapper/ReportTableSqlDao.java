package com.nokia.report.mapper;

import com.nokia.report.pojo.CfgSiteInfoTdlteDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;

@Mapper
public interface ReportTableSqlDao {
	List<HashMap<String, Object>> getListBySql(@Param("sqlStr") String sqlStr);

	Integer getIntegerBySql(@Param("sqlStr") String sqlStr);

	List<CfgSiteInfoTdlteDO> listCfgSiteinfoTdlteByCityAndSite(@Param("city")String city,@Param("site") String site);
}