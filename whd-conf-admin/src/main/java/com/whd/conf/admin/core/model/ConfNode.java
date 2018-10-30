package com.whd.conf.admin.core.model;

import java.util.List;

/**
 * 配置节点
 * @author hayden 2015-9-4 15:26:01
 */
public class ConfNode {

	private String env;
	private String key;			// 配置Key
	private String appname; 	// 所属项目AppName
	private String title; 		// 配置描述
	private String value;		// 配置Value

	// plugin
	private String zkValue; 				// ZK中配置Value
	private List<ConfNodeLog> logList;	// 配置变更Log

	public String getEnv() {
		return env;
	}

	public void setEnv(String env) {
		this.env = env;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getAppname() {
		return appname;
	}

	public void setAppname(String appname) {
		this.appname = appname;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getZkValue() {
		return zkValue;
	}

	public void setZkValue(String zkValue) {
		this.zkValue = zkValue;
	}

	public List<ConfNodeLog> getLogList() {
		return logList;
	}

	public void setLogList(List<ConfNodeLog> logList) {
		this.logList = logList;
	}
}
