package com.whd.conf.admin.dao;

import com.whd.conf.admin.core.model.ConfProject;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by hayden on 16/10/8.
 */
@Mapper
public interface ConfProjectDao {

    public List<ConfProject> findAll();

    public int save(ConfProject confProject);

    public int update(ConfProject confProject);

    public int delete(@Param("appname") String appname);

    public ConfProject load(@Param("appname") String appname);

}