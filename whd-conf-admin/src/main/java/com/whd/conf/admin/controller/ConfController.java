package com.whd.conf.admin.controller;

import com.whd.conf.admin.core.model.ConfNode;
import com.whd.conf.admin.core.model.ConfProject;
import com.whd.conf.admin.core.model.ConfUser;
import com.whd.conf.admin.core.util.ReturnT;
import com.whd.conf.admin.dao.ConfProjectDao;
import com.whd.conf.admin.service.IConfNodeService;
import com.whd.conf.admin.service.impl.LoginService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 配置管理
 *
 * @author hayden
 */
@Controller
@RequestMapping("/conf")
public class ConfController {

	@Resource
	private ConfProjectDao confProjectDao;
	@Resource
	private IConfNodeService ConfNodeService;

	@RequestMapping("")
	public String index(Model model, String appname){

		List<ConfProject> list = confProjectDao.findAll();
		if (CollectionUtils.isEmpty(list)) {
			throw new RuntimeException("系统异常，无可用项目");
		}

		ConfProject project = list.get(0);
		for (ConfProject item: list) {
			if (item.getAppname().equals(appname)) {
				project = item;
			}
		}

		model.addAttribute("ProjectList", list);
		model.addAttribute("project", project);

		return "conf/conf.index";
	}

	@RequestMapping("/pageList")
	@ResponseBody
	public Map<String, Object> pageList(HttpServletRequest request,
										@RequestParam(required = false, defaultValue = "0") int start,
										@RequestParam(required = false, defaultValue = "10") int length,
										String env,
										String appname,
										String key) {
		ConfUser loginUser = (ConfUser) request.getAttribute(LoginService.LOGIN_IDENTITY);
		return ConfNodeService.pageList(start, length, env, appname, key, loginUser);
	}
	
	/**
	 * get
	 * @return
	 */
	@RequestMapping("/delete")
	@ResponseBody
	public ReturnT<String> delete(HttpServletRequest request, String env, String key){
		ConfUser loginUser = (ConfUser) request.getAttribute(LoginService.LOGIN_IDENTITY);
		return ConfNodeService.delete(env, key, loginUser);
	}

	/**
	 * create/update
	 * @return
	 */
	@RequestMapping("/add")
	@ResponseBody
	public ReturnT<String> add(HttpServletRequest request, ConfNode confNode){
		ConfUser loginUser = (ConfUser) request.getAttribute(LoginService.LOGIN_IDENTITY);
		return ConfNodeService.add(confNode, loginUser);
	}
	
	/**
	 * create/update
	 * @return
	 */
	@RequestMapping("/update")
	@ResponseBody
	public ReturnT<String> update(HttpServletRequest request, ConfNode confNode){
		ConfUser loginUser = (ConfUser) request.getAttribute(LoginService.LOGIN_IDENTITY);
		return ConfNodeService.update(confNode, loginUser);
	}

	@RequestMapping("/syncConf")
	@ResponseBody
	public ReturnT<String> syncConf(HttpServletRequest request,
										String env,
										String appname) {
		ConfUser loginUser = (ConfUser) request.getAttribute(LoginService.LOGIN_IDENTITY);
		return ConfNodeService.syncConf(env, appname, loginUser);
	}

}
