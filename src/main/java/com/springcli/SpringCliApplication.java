package com.springcli;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringCliApplication {

    static {
        System.setProperty("io.netty.resolver.dns.defaultNameServers", "8.8.8.8");
        System.setProperty("io.netty.resolver.dns.preferredAddressType", "IPv4");
        System.setProperty("java.net.preferIPv4Stack", "true");

        System.setProperty("io.netty.resolver.dns", "false");
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringCliApplication.class, args);
    }
}