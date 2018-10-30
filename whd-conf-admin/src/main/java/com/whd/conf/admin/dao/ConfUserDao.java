package com.whd.conf.admin.dao;

import com.whd.conf.admin.core.model.ConfUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author hayden 2018-03-01
 */
@Mapper
public interface ConfUserDao {

    public List<ConfUser> pageList(@Param("offset") int offset,
                                   @Param("pagesize") int pagesize,
                                   @Param("username") String username,
                                   @Param("permission") int permission);
    public int pageListCount(@Param("offset") int offset,
                             @Param("pagesize") int pagesize,
                             @Param("username") String username,
                             @Param("permission") int permission);

    public int add(ConfUser confUser);

    public int update(ConfUser confUser);

    public int delete(@Param("username") String username);

    public ConfUser load(@Param("username") String username);

}
