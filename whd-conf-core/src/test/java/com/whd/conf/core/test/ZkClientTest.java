package com.whd.conf.core.test;

import com.whd.conf.core.util.ZkClient;

public class ZkClientTest {

    public static void main(String[] args) throws InterruptedException {
        ZkClient client = new ZkClient("127.0.0.1:2181", "/conf", null, null);

        System.out.println(client.getClient());
        System.out.println(client.getClient());
    }

}
