package com.whd.conf.core.core;

import com.whd.conf.core.exception.ConfException;
import com.whd.conf.core.util.ZkClient;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * ZooKeeper cfg client (Watcher + some utils)
 *
 * @author hayden 2015年8月26日21:36:43
 */
public class ConfZkManageConf {
	private static Logger logger = LoggerFactory.getLogger(ConfZkManageConf.class);


	// ------------------------------ zookeeper client ------------------------------

	private static final String zkBasePath = "/conf";
	private static String getZkEnvPath(String env){
		return zkBasePath.concat("/").concat(env);
	}

	private static ZkClient zkClient = null;
	public static void init(String zkaddress, String zkdigest) {

		// valid
		if (zkaddress==null || zkaddress.trim().length()==0) {
			throw new ConfException("conf zkaddress can not be empty");
		}

		// init
		zkClient = new ZkClient(zkaddress, zkBasePath, zkdigest, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                try {
                    logger.info(">>>>>>>>>> conf: watcher:{}", watchedEvent);

                    // session expire, close old and create new
                    if (watchedEvent.getState() == Event.KeeperState.Expired) {
                        zkClient.destroy();
                        zkClient.getClient();


                        logger.info(">>>>>>>>>> conf, zk re-connect reloadAll success.");
                    }

                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        });
		logger.info(">>>>>>>>>> conf, ConfZkConf init success.");
	}

	public static void destroy(){
		if (zkClient !=null) {
			zkClient.destroy();
		}
	}

	// ------------------------------ conf opt ------------------------------

	/**
	 * set zk conf
	 *
	 * @param key
	 * @param data
	 * @return
	 */
	public static void set(String env, String key, String data) {
		String path = keyToPath(env, key);
		zkClient.setPathData(path, data);
	}

	/**
	 * delete zk conf
	 *
	 * @param key
	 */
	public static void delete(String env, String key){
		String path = keyToPath(env, key);
		zkClient.deletePath(path);
	}

	/**
	 * get zk conf
	 *
	 * @param key
	 * @return
	 */
	public static String get(String env, String key){
		String path = keyToPath(env, key);
		return zkClient.getPathData(path);
	}


	// ------------------------------ key 2 path / genarate key ------------------------------

	/**
	 * path 2 key
	 * @param nodePath
	 * @return ZnodeKey
	 */
	public static String pathToKey(String env, String nodePath){
		String zkEnvPath = getZkEnvPath(env);

		if (nodePath==null || nodePath.length() <= zkEnvPath.length() || !nodePath.startsWith(zkEnvPath)) {
			return null;
		}
		return nodePath.substring(zkEnvPath.length()+1, nodePath.length());
	}

	/**
	 * key 2 path
	 * @param nodeKey
	 * @return znodePath
	 */
	public static String keyToPath(String env, String nodeKey){
		String zkEnvPath = getZkEnvPath(env);

		return zkEnvPath + "/" + nodeKey;
	}

}