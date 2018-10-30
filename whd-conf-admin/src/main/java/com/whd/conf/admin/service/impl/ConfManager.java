package com.whd.conf.admin.service.impl;

import com.whd.conf.core.core.ConfZkManageConf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


/**
 * ZooKeeper cfg client (Watcher + some utils)
 *
 * @author hayden 2015年8月26日21:36:43
 */
@Component
public class ConfManager implements InitializingBean, DisposableBean {
	private static Logger logger = LoggerFactory.getLogger(ConfManager.class);

	@Value("${whd.conf.zkaddress}")
	private String zkaddress;

	@Value("${whd.conf.zkdigest}")
	private String zkdigest;

	// ------------------------------ zookeeper client ------------------------------

	@Override
	public void afterPropertiesSet() throws Exception {
		ConfZkManageConf.init(zkaddress, zkdigest);
	}

	@Override
	public void destroy() throws Exception {
		ConfZkManageConf.destroy();
	}


	// ------------------------------ conf opt ------------------------------

	/**
	 * set zk conf
	 *
	 * @param key
	 * @param data
	 * @return
	 */
	public void set(String env, String key, String data) {
		ConfZkManageConf.set(env, key, data);
	}

	/**
	 * delete zk conf
	 *
	 * @param env
	 * @param key
	 */
	public void delete(String env, String key){
		ConfZkManageConf.delete(env, key);
	}

	/**
	 * get zk conf
	 *
	 * @param key
	 * @return
	 */
	public String get(String env, String key){
		return ConfZkManageConf.get(env, key);
	}

}