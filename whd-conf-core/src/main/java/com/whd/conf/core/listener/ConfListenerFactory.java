package com.whd.conf.core.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * conf listener
 *
 * @author hayden 2018-02-04 01:27:30
 */
public class ConfListenerFactory {
    private static Logger logger = LoggerFactory.getLogger(ConfListenerFactory.class);

    /**
     * conf listener repository
     */
    private static ConcurrentHashMap<String, List<ConfListener>> keyListenerRepository = new ConcurrentHashMap<>();
    private static List<ConfListener> noKeyConfListener = Collections.synchronizedList(new ArrayList<ConfListener>());

    /**
     * add listener with conf change
     *
     * @param key   empty will listener all key
     * @param confListener
     * @return
     */
    public static boolean addListener(String key, ConfListener confListener){
        if (confListener == null) {
            return false;
        }
        if (key==null || key.trim().length()==0) {
            noKeyConfListener.add(confListener);
            return true;
        } else {
            List<ConfListener> listeners = keyListenerRepository.get(key);
            if (listeners == null) {
                listeners = new ArrayList<>();
                keyListenerRepository.put(key, listeners);
            }
            listeners.add(confListener);
            return true;
        }
    }

    /**
     * invoke listener on conf change
     *
     * @param key
     */
    public static void onChange(String key, String value){
        if (key==null || key.trim().length()==0) {
            return;
        }
        List<ConfListener> keyListeners = keyListenerRepository.get(key);
        if (keyListeners!=null && keyListeners.size()>0) {
            for (ConfListener listener : keyListeners) {
                try {
                    listener.onChange(key, value);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        if (noKeyConfListener.size() > 0) {
            for (ConfListener confListener: noKeyConfListener) {
                try {
                    confListener.onChange(key, value);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

}
