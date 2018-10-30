package com.whd.conf.core;

import com.whd.conf.core.core.ConfLocalCacheConf;
import com.whd.conf.core.core.ConfMirrorConf;
import com.whd.conf.core.core.ConfZkConf;
import com.whd.conf.core.listener.ConfListener;
import com.whd.conf.core.listener.ConfListenerFactory;
import com.whd.conf.core.exception.ConfException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * conf client
 *
 * @author hayden 2015-8-28 15:35:20
 */
public class ConfClient {
	private static Logger logger = LoggerFactory.getLogger(ConfClient.class);

	/**
	 * get conf
	 *
	 * @param key
	 * @param defaultVal
	 * @return
	 */
	public static String get(String key, String defaultVal) {

		// level 1: local cache
		ConfLocalCacheConf.CacheNode cacheNode = ConfLocalCacheConf.get(key);
		if (cacheNode != null) {
			return cacheNode.getValue();
		}

		// level 2	(get-and-watch, add-local-cache)
		String zkData = null;
		try {
			zkData = ConfZkConf.get(key);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);

			// mirror then fail
			zkData = ConfMirrorConf.get(key);
		}


		ConfLocalCacheConf.set(key, zkData, "SET");		// support cache null value
		if (zkData != null) {
			return zkData;
		}

		return defaultVal;
	}

	/**
	 * get conf (string)
	 *
	 * @param key
	 * @return
	 */
	public static String get(String key) {
		return get(key, null);
	}

	/**
	 * get conf (boolean)
	 *
	 * @param key
	 * @return
	 */
	public static boolean getBoolean(String key) {
		String value = get(key, null);
		if (value == null) {
			throw new ConfException("config key [" + key + "] does not exist");
		}
		return Boolean.valueOf(value);
	}

	/**
	 * get conf (short)
	 *
	 * @param key
	 * @return
	 */
	public static short getShort(String key) {
		String value = get(key, null);
		if (value == null) {
			throw new ConfException("config key [" + key + "] does not exist");
		}
		return Short.valueOf(value);
	}

	/**
	 * get conf (int)
	 *
	 * @param key
	 * @return
	 */
	public static int getInt(String key) {
		String value = get(key, null);
		if (value == null) {
			throw new ConfException("config key [" + key + "] does not exist");
		}
		return Integer.valueOf(value);
	}

	/**
	 * get conf (long)
	 *
	 * @param key
	 * @return
	 */
	public static long getLong(String key) {
		String value = get(key, null);
		if (value == null) {
			throw new ConfException("config key [" + key + "] does not exist");
		}
		return Long.valueOf(value);
	}

	/**
	 * get conf (float)
	 *
	 * @param key
	 * @return
	 */
	public static float getFloat(String key) {
		String value = get(key, null);
		if (value == null) {
			throw new ConfException("config key [" + key + "] does not exist");
		}
		return Float.valueOf(value);
	}

	/**
	 * get conf (double)
	 *
	 * @param key
	 * @return
	 */
	public static double getDouble(String key) {
		String value = get(key, null);
		if (value == null) {
			throw new ConfException("config key [" + key + "] does not exist");
		}
		return Double.valueOf(value);
	}

	/**
	 * add listener with conf change
	 *
	 * @param key
	 * @param confListener
	 * @return
	 */
	public static boolean addListener(String key, ConfListener confListener){
		return ConfListenerFactory.addListener(key, confListener);
	}
	
}
