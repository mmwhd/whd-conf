package com.whd.conf.admin.controller.interceptor;

import com.whd.conf.admin.core.model.ConfEnv;
import com.whd.conf.admin.core.util.CookieUtil;
import com.whd.conf.admin.dao.ConfEnvDao;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * push cookies to model as cookieMap
 * @author hayden 2015-12-12 18:09:04
 */
@Component
public class EnvInterceptor extends HandlerInterceptorAdapter {

	public static final String CURRENT_ENV = "CONF_CURRENT_ENV";

	@Resource
	private ConfEnvDao confEnvDao;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

		// env list
		List<ConfEnv> envList = confEnvDao.findAll();
		if (envList==null || envList.size()==0) {
			throw new RuntimeException("系统异常，获取Env数据失败");
		}

		// current env
		ConfEnv currentEnv = envList.get(0);

		String currentEnvCookie = CookieUtil.getValue(request, CURRENT_ENV);
		if (currentEnvCookie!=null && currentEnvCookie.trim().length()>0) {
			for (ConfEnv envItem: envList) {
				if (currentEnvCookie.equals(envItem.getEnv())) {
					currentEnv = envItem;
				}
			}
		}

		request.setAttribute("envList", envList);
		request.setAttribute(CURRENT_ENV, currentEnv);

		return super.preHandle(request, response, handler);
	}
	
}
