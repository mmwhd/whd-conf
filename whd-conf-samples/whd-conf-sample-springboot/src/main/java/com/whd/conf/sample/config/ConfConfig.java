package com.whd.conf.sample.config;

import com.whd.conf.core.spring.ConfFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * xconf config
 *
 * @author hayden 2017-04-28
 */
@Configuration
public class ConfConfig {
    private Logger logger = LoggerFactory.getLogger(ConfConfig.class);


    @Value("${whd.conf.zkaddress}")
    private String zkaddress;

    @Value("${whd.conf.zkdigest}")
    private String zkdigest;

    @Value("${whd.conf.env}")
    private String env;

    @Value("${whd.conf.mirrorfile}")
    private String mirrorfile;


    @Bean
    public ConfFactory confFactory() {

        ConfFactory confFactory = new ConfFactory();
        confFactory.setZkaddress(zkaddress);
        confFactory.setZkdigest(zkdigest);
        confFactory.setEnv(env);
        confFactory.setMirrorfile(mirrorfile);

        logger.info(">>>>>>>>>>> confFactory config init.");
        return confFactory;
    }

}