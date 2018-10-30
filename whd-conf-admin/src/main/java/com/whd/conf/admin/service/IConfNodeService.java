package com.whd.conf.admin.service;


import com.whd.conf.admin.core.model.ConfNode;
import com.whd.conf.admin.core.model.ConfUser;
import com.whd.conf.admin.core.util.ReturnT;

import java.util.Map;

/**
 * @author hayden 2015-9-4 18:19:52
 */
public interface IConfNodeService {

	public Map<String,Object> pageList(int offset,
									   int pagesize,
									   String env,
									   String appname,
									   String key,
									   ConfUser loginUser);

	public ReturnT<String> delete(String env, String key, ConfUser loginUser);

	public ReturnT<String> add(ConfNode confNode, ConfUser loginUser);

	public ReturnT<String> update(ConfNode confNode, ConfUser loginUser);

    ReturnT<String> syncConf(String env, String appname, ConfUser loginUser);

}
