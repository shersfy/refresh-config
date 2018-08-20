package org.onosproject.refresh.config.tests;

import java.util.Properties;

import org.junit.Test;
import org.onosproject.config.refresh.api.RefreshConfigService;
import org.onosproject.config.refresh.impl.RefreshConfigServiceImpl;

public class TestCases {
    
    @Test
    public void test01() {
        Test1 test = new Test1();
        
        RefreshConfigService service = new RefreshConfigServiceImpl();
        service.register(test);
        
        Properties config = new Properties();
        config.setProperty("spring.application.name", "onos");
        config.setProperty("server.port", "3306");
        config.setProperty("server.timeout", "10");
        config.setProperty("server.size", "12");
        
        System.out.println(test);
        System.out.println(service.refresh(config));
        System.out.println(test);
    }
    
    @Test
    public void test02() {
        String value = "${ spring.application.name }";
        boolean flg  = value.matches("\\$\\{[ ]*[a-zA-Z_0-9\\.]+[ ]*\\}");
        System.out.println(flg);
        if(flg) {
            String key = value.substring(2, value.length()-1);
            System.out.println(key.trim());
        }
    }

}
