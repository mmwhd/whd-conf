package com.whd.conf.sample.demo;

import com.whd.conf.core.annotation.Conf;
import org.springframework.stereotype.Component;

/**
 *  测试示例（可删除）
 *
 *  @author hayden
 */
@Component
public class DemoConf {

	@Conf("default.key02")
	public String paramByAnno;


	@Conf("openapi.netease.url")
	public String neteaseurl;
}
