package com.whd.conf.admin.service.impl;

import com.whd.conf.admin.core.model.*;
import com.whd.conf.admin.core.util.ReturnT;
import com.whd.conf.admin.dao.ConfEnvDao;
import com.whd.conf.admin.dao.ConfNodeDao;
import com.whd.conf.admin.dao.ConfNodeLogDao;
import com.whd.conf.admin.service.IConfNodeService;
import com.whd.conf.admin.dao.ConfProjectDao;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 配置
 * @author hayden 2016-08-15 22:53
 */
@Service
public class ConfNodeServiceImpl implements IConfNodeService {


	@Resource
	private ConfNodeDao confNodeDao;
	@Resource
	private ConfProjectDao confProjectDao;
	@Resource
	private ConfManager confManager;
	@Resource
	private ConfNodeLogDao confNodeLogDao;
	@Resource
	private ConfEnvDao confEnvDao;

	@Override
	public Map<String,Object> pageList(int offset,
									   int pagesize,
									   String env,
									   String appname,
									   String key,
									   ConfUser loginUser) {

		// project permission
		if (StringUtils.isBlank(env) || StringUtils.isBlank(appname) || !ifHasProjectPermission(loginUser, appname)) {
			//return new ReturnT<String>(500, "您没有该项目的配置权限,请联系管理员开通");
			Map<String, Object> emptyMap = new HashMap<String, Object>();
			emptyMap.put("data", new ArrayList<>());
			emptyMap.put("recordsTotal", 0);
			emptyMap.put("recordsFiltered", 0);
			return emptyMap;
		}

		// ConfNode in mysql
		List<ConfNode> data = confNodeDao.pageList(offset, pagesize, env, appname, key);
		int list_count = confNodeDao.pageListCount(offset, pagesize, env, appname, key);

		// fill value in zk
		if (CollectionUtils.isNotEmpty(data)) {
			for (ConfNode node: data) {
				String realNodeValue = confManager.get(node.getEnv(), node.getKey());
				node.setZkValue(realNodeValue);
			}
		}

		// package result
		Map<String, Object> maps = new HashMap<String, Object>();
		maps.put("data", data);
		maps.put("recordsTotal", list_count);		// 总记录数
		maps.put("recordsFiltered", list_count);	// 过滤后的总记录数
		return maps;

	}

	private boolean ifHasProjectPermission(ConfUser loginUser, String appname){
		if (loginUser.getPermission() == 1) {
			return true;
		}
		if (ArrayUtils.contains(StringUtils.split(loginUser.getPermissionProjects(), ","), appname)) {
			return true;
		}
		return false;
	}

	@Override
	public ReturnT<String> delete(String env, String key, ConfUser loginUser) {
		if (StringUtils.isBlank(key)) {
			return new ReturnT<String>(500, "参数缺失");
		}
		ConfNode existNode = confNodeDao.load(env, key);
		if (existNode == null) {
			return new ReturnT<String>(500, "参数非法");
		}

		// project permission
		if (!ifHasProjectPermission(loginUser, existNode.getAppname())) {
			return new ReturnT<String>(500, "您没有该项目的配置权限,请联系管理员开通");
		}

		confManager.delete(env, key);
		confNodeDao.delete(env, key);
		confNodeLogDao.deleteTimeout(env, key, 0);
		return ReturnT.SUCCESS;
	}

	@Override
	public ReturnT<String> add(ConfNode confNode, ConfUser loginUser) {

		// valid
		if (StringUtils.isBlank(confNode.getAppname())) {
			return new ReturnT<String>(500, "AppName不可为空");
		}

		// project permission
		if (!ifHasProjectPermission(loginUser, confNode.getAppname())) {
			return new ReturnT<String>(500, "您没有该项目的配置权限,请联系管理员开通");
		}

		// valid group
		ConfProject group = confProjectDao.load(confNode.getAppname());
		if (group==null) {
			return new ReturnT<String>(500, "AppName非法");
		}

		// valid env
		if (StringUtils.isBlank(confNode.getEnv())) {
			return new ReturnT<String>(500, "配置Env不可为空");
		}
		ConfEnv confEnv = confEnvDao.load(confNode.getEnv());
		if (confEnv == null) {
			return new ReturnT<String>(500, "配置Env非法");
		}

		// valid key
		if (StringUtils.isBlank(confNode.getKey())) {
			return new ReturnT<String>(500, "配置Key不可为空");
		}
		confNode.setKey(confNode.getKey().trim());

		ConfNode existNode = confNodeDao.load(confNode.getEnv(), confNode.getKey());
		if (existNode != null) {
			return new ReturnT<String>(500, "配置Key已存在，不可重复添加");
		}
		if (!confNode.getKey().startsWith(confNode.getAppname())) {
			return new ReturnT<String>(500, "配置Key格式非法");
		}

		// valid title
		if (StringUtils.isBlank(confNode.getTitle())) {
			return new ReturnT<String>(500, "配置描述不可为空");
		}

		// value force null to ""
		if (confNode.getValue() == null) {
			confNode.setValue("");
		}

		confManager.set(confNode.getEnv(), confNode.getKey(), confNode.getValue());
		confNodeDao.insert(confNode);
		return ReturnT.SUCCESS;
	}

	@Override
	public ReturnT<String> update(ConfNode confNode, ConfUser loginUser) {

		// valid
		if (StringUtils.isBlank(confNode.getKey())) {
			return new ReturnT<String>(500, "配置Key不可为空");
		}
		ConfNode existNode = confNodeDao.load(confNode.getEnv(), confNode.getKey());
		if (existNode == null) {
			return new ReturnT<String>(500, "配置Key非法");
		}

		// project permission
		if (!ifHasProjectPermission(loginUser, existNode.getAppname())) {
			return new ReturnT<String>(500, "您没有该项目的配置权限,请联系管理员开通");
		}


		if (StringUtils.isBlank(confNode.getTitle())) {
			return new ReturnT<String>(500, "配置描述不可为空");
		}

		// value force null to ""
		if (confNode.getValue() == null) {
			confNode.setValue("");
		}

		confManager.set(confNode.getEnv(), confNode.getKey(), confNode.getValue());

		existNode.setTitle(confNode.getTitle());
		existNode.setValue(confNode.getValue());
		int ret = confNodeDao.update(existNode);
		if (ret < 1) {
			return ReturnT.FAIL;
		}

		// node log
		ConfNodeLog nodeLog = new ConfNodeLog();
		nodeLog.setEnv(existNode.getEnv());
		nodeLog.setKey(existNode.getKey());
		nodeLog.setTitle(existNode.getTitle() + "(配置更新)" );
		nodeLog.setValue(existNode.getValue());
		nodeLog.setOptuser(loginUser.getUsername());
		confNodeLogDao.add(nodeLog);
		confNodeLogDao.deleteTimeout(existNode.getEnv(), existNode.getKey(), 10);

		return ReturnT.SUCCESS;
	}

	@Override
	public ReturnT<String> syncConf(String env, String appname, ConfUser loginUser) {

		// valid
		ConfEnv confEnv = confEnvDao.load(env);
		if (confEnv == null) {
			return new ReturnT<String>(500, "配置Env非法");
		}
		ConfProject group = confProjectDao.load(appname);
		if (group==null) {
			return new ReturnT<String>(500, "AppName非法");
		}

		// project permission
		if (!ifHasProjectPermission(loginUser, appname)) {
			return new ReturnT<String>(500, "您没有该项目的配置权限,请联系管理员开通");
		}

		List<ConfNode> confNodeList = confNodeDao.pageList(0, 10000, env, appname, null);
		if (CollectionUtils.isEmpty(confNodeList)) {
			return new ReturnT<String>(500, "操作失败，该项目下不存在配置项");
		}

		// un sync node
		List<ConfNode> unSyncConfNodeList = new ArrayList<>();
		for (ConfNode node: confNodeList) {
			String realNodeValue = confManager.get(node.getEnv(), node.getKey());
			if (!node.getValue().equals(realNodeValue)) {
				unSyncConfNodeList.add(node);
			}
		}

		if (CollectionUtils.isEmpty(unSyncConfNodeList)) {
			return new ReturnT<String>(500, "操作失败，该项目下不存未同步的配置项");
		}

		// do sync
		String logContent = "操作成功，共计同步 " + unSyncConfNodeList.size() + " 条配置：";
		for (ConfNode node: unSyncConfNodeList) {

			confManager.set(node.getEnv(), node.getKey(), node.getValue());

			// node log
			ConfNodeLog nodeLog = new ConfNodeLog();
			nodeLog.setEnv(node.getEnv());
			nodeLog.setKey(node.getKey());
			nodeLog.setTitle(node.getTitle() + "(全量同步)" );
			nodeLog.setValue(node.getValue());
			nodeLog.setOptuser(loginUser.getUsername());
			confNodeLogDao.add(nodeLog);
			confNodeLogDao.deleteTimeout(node.getEnv(), node.getKey(), 10);

			logContent += "<br>" + node.getKey();
		}
		logContent.substring(logContent.length() - 1);

		return new ReturnT<String>(ReturnT.SUCCESS.getCode(), logContent);
	}


}
