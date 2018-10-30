package com.whd.conf.admin.dao;

import com.whd.conf.admin.core.model.ConfEnv;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by hayden on 2018-05-30
 */
@Mapper
public interface ConfEnvDao {

    public List<ConfEnv> findAll();

    public int save(ConfEnv confEnv);

    public int update(ConfEnv confEnv);

    public int delete(@Param("env") String env);

    public ConfEnv load(@Param("env") String env);

}