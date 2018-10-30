package com.whd.conf.core.core;

import com.whd.conf.core.util.ZkClient;
import com.whd.conf.core.exception.ConfException;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * ZooKeeper cfg client (Watcher + some utils)
 *
 * @author hayden 2015年8月26日21:36:43
 */
public class ConfZkConf {
	private static Logger logger = LoggerFactory.getLogger(ConfZkConf.class);


	// ------------------------------ zookeeper client ------------------------------

	private static final String zkBasePath = "/conf";
	private static String zkEnvPath;

	private static ZkClient zkClient = null;
	public static void init(String zkaddress, String zkdigest, String env) {

		// valid
		if (zkaddress==null || zkaddress.trim().length()==0) {
			throw new ConfException("conf zkaddress can not be empty");
		}

		// init zkpath
		if (env==null || env.trim().length()==0) {
			throw new ConfException("conf env can not be empty");
		}

        ConfZkConf.zkEnvPath = zkBasePath.concat("/").concat(env);

		// init
		zkClient = new ZkClient(zkaddress, zkEnvPath, zkdigest, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                try {
                    logger.info(">>>>>>>>>> conf: watcher:{}", watchedEvent);

                    // session expire, close old and create new
                    if (watchedEvent.getState() == Event.KeeperState.Expired) {
                        zkClient.destroy();
                        zkClient.getClient();


						ConfLocalCacheConf.reloadAll();
                        logger.info(">>>>>>>>>> conf, zk re-connect reloadAll success.");
                    }

					String path = watchedEvent.getPath();
					String key = pathToKey(path);
					if (key != null) {
						// keep watch conf key：add One-time trigger
						zkClient.getClient().exists(path, true);
						if (watchedEvent.getType() == Event.EventType.NodeDeleted) {
							// conf deleted
						} else if (watchedEvent.getType() == Event.EventType.NodeDataChanged) {
							// conf updated
							String data = get(key);
							ConfLocalCacheConf.update(key, data);
						}
					}

                } catch (KeeperException e) {
                    logger.error(e.getMessage(), e);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        });
		logger.info(">>>>>>>>>> conf, ConfZkConf init success. [env={}]", env);
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
	public static void set(String key, String data) {
		String path = keyToPath(key);
		zkClient.setPathData(path, data);
	}

	/**
	 * delete zk conf
	 *
	 * @param key
	 */
	public static void delete(String key){
		String path = keyToPath(key);
		zkClient.deletePath(path);
	}

	/**
	 * get zk conf
	 *
	 * @param key
	 * @return
	 */
	public static String get(String key){
		String path = keyToPath(key);
		return zkClient.getPathData(path);
	}


	// ------------------------------ key 2 path / genarate key ------------------------------

	/**
	 * path 2 key
	 * @param nodePath
	 * @return ZnodeKey
	 */
	public static String pathToKey(String nodePath){
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
	public static String keyToPath(String nodeKey){
		return zkEnvPath + "/" + nodeKey;
	}

}