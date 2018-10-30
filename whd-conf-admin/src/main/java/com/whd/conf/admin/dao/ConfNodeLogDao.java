package com.whd.conf.admin.dao;

import com.whd.conf.admin.core.model.ConfNodeLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by hayden on 16/10/8.
 */
@Mapper
public interface ConfNodeLogDao {

	public List<ConfNodeLog> findByKey(@Param("env") String env, @Param("key") String key);

	public void add(ConfNodeLog confNode);

	public int deleteTimeout(@Param("env") String env,
							 @Param("key") String key,
							 @Param("length") int length);

}
