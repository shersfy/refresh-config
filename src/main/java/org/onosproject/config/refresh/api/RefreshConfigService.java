package org.onosproject.config.refresh.api;

import java.util.Properties;

/**
 * 刷新配置服务, 通过REST API刷新配置post: /refresh和post: /mq-refresh<br/>
 * /refresh 刷新单个节点<br/>
 * /mq-refresh 通过MQ刷新集群<br/>
 * <p/>
 * 支持热修改刷新配置：
 * <br/>
 * 1. 在需要刷新的类上面添加注解{@link RefreshScope};
 * <br/>
 * 2. 在需要刷新的属性成员添加注解{@link Value}, 格式如"${filename.key}", filename配置文件名;
 * <br/>
 * 3. 在需要刷新的实例初始化时，即active()时执行调用
 * {@link RefreshConfigService RefreshConfigService.register(Object instance)}注册实例，
 * 对象销毁时调用unregister(Object instance)方法卸载;<br/>
 * 
 * <p/>
 * @author py
 * 2018年8月17日
 */
public interface RefreshConfigService {
    /**
     * 注册实例到刷新容器，注意确保register(Object instance)和unregister(Object instance)成对使用
     * @param instance
     */
    void register(Object instance);
    
    /**
     * 移除刷新容器
     * @param instance
     */
    void unregister(Object instance);
    
    /**
     * 刷新配置
     * @param config 修改部分配置
     * @return
     */
    String refresh(Properties config);
    
    
    String refresh();
    
    String mqRresh();
}
