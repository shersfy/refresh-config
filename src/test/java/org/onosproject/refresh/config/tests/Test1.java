package org.onosproject.refresh.config.tests;

import org.onosproject.config.refresh.api.RefreshScope;
import org.onosproject.config.refresh.api.Value;

@RefreshScope
public class Test1 {

    @Value(" ${ spring.application.name } ")
    private String name;

    @Value("${server.port}")
    private int port;

    @Value("${server.timeout}")
    private long timeout;

    @Value("1024")
    private int size;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Test1 [name=" + name + ", port=" + port + ", timeout=" + timeout + ", size=" + size
            + "]";
    }

}
