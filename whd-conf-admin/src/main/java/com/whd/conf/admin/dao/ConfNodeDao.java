package com.whd.conf.admin.dao;

import com.whd.conf.admin.core.model.ConfNode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by hayden on 16/10/8.
 */
@Mapper
public interface ConfNodeDao {

	public List<ConfNode> pageList(@Param("offset") int offset,
								   @Param("pagesize") int pagesize,
								   @Param("env") String env,
								   @Param("appname") String appname,
								   @Param("key") String key);
	public int pageListCount(@Param("offset") int offset,
							 @Param("pagesize") int pagesize,
							 @Param("env") String env,
							 @Param("appname") String appname,
							 @Param("key") String key);

	public int delete(@Param("env") String env, @Param("key") String key);

	public void insert(ConfNode confNode);

	public ConfNode load(@Param("env") String env, @Param("key") String key);

	public int update(ConfNode confNode);
	
}
