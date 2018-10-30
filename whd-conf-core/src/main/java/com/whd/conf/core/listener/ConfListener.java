package com.whd.conf.core.listener;

/**
 * conf listener
 *
 * @author hayden 2018-02-04 01:27:30
 */
public interface ConfListener {

    /**
     * invoke when conf change
     *
     * @param key
     */
    public void onChange(String key, String value) throws Exception;

}
