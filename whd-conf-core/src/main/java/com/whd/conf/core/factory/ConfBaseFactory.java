package com.whd.conf.core.factory;

import com.whd.conf.core.core.ConfLocalCacheConf;
import com.whd.conf.core.core.ConfMirrorConf;
import com.whd.conf.core.core.ConfZkConf;
import com.whd.conf.core.listener.ConfListenerFactory;
import com.whd.conf.core.listener.impl.BeanRefreshConfListener;
import com.whd.conf.core.util.PropUtil;

import java.util.Properties;

/**
 * Conf Base Factory
 *
 * @author hayden 2015-9-12 19:42:49
 */
public class ConfBaseFactory {

	/**
	 * init
	 *
	 * @param envprop
	 */
	public static void init(String envprop) {

		String zkaddress = null;
		String zkdigest = null;
		String env = null;
		String mirrorfile = null;

		// env prop
		if (envprop!=null && envprop.trim().length()>0) {
			Properties envPropFile = PropUtil.loadProp(envprop);
			if (envPropFile!=null && envPropFile.stringPropertyNames()!=null && envPropFile.stringPropertyNames().size()>0) {
				for (String key: envPropFile.stringPropertyNames()) {
					if ("whd.conf.zkaddress".equals(key)) {
						zkaddress = envPropFile.getProperty(key);	// replace if envprop not exist
					} else if ("whd.conf.zkdigest".equals(key)) {
						zkdigest = envPropFile.getProperty(key);
					} else if ("whd.conf.env".equals(key)) {
						env = envPropFile.getProperty(key);
					} else if ("whd.conf.mirrorfile".equals(key)) {
						mirrorfile = envPropFile.getProperty(key);
					}
				}
			}
		}


		init(zkaddress, zkdigest, env, mirrorfile);
	}

	/**
	 * init
	 *
	 * @param zkaddress
	 * @param zkdigest
	 * @param env
	 */
	public static void init(String zkaddress, String zkdigest, String env, String mirrorfile) {
		// init
		ConfZkConf.init(zkaddress, zkdigest, env);										// init zk client
		ConfMirrorConf.init(mirrorfile);													// init file mirror
		ConfLocalCacheConf.init();														// init cache
		ConfListenerFactory.addListener(null, new BeanRefreshConfListener());    // listener all key change

	}

	/**
	 * destory
	 */
	public static void destroy() {
		ConfLocalCacheConf.destroy();	// destroy
		ConfZkConf.destroy();			// destroy zk client
	}

}
