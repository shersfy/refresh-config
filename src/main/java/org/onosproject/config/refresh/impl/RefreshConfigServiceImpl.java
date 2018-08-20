package org.onosproject.config.refresh.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.onosproject.config.refresh.api.AnnotationException;
import org.onosproject.config.refresh.api.RefreshConfigService;
import org.onosproject.config.refresh.api.RefreshScope;
import org.onosproject.config.refresh.api.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true)
@Service
public class RefreshConfigServiceImpl implements RefreshConfigService{

    static Logger logger = LoggerFactory.getLogger(RefreshConfigServiceImpl.class);
    
//    private static String MQ_REFRESH_TOPIC = "onos-mq-refresh";
//    private static String MQ_REFRESH_VALUE = "post:/mq-refresh";
    
    
    private static Properties systemconfig;
    private static List<Object> refreshScope;
    
    static {
        refreshScope = new CopyOnWriteArrayList<>();
        systemconfig = new Properties();
        reload().clear();
    }
    
    public static synchronized Properties reload() {
        logger.info("---- loading properties files, start ... -------");
        
        String karafHome = System.getProperty("karaf.home");
        File etc = new File(karafHome, "etc");
        Collection<File> files = FileUtils.listFiles(etc, new String[] {"cfg", "properties"}, false);
        
        Properties update = new Properties();
        Properties prop   = new Properties();
        for(File cfg :files) {
            prop.clear();
            InputStreamReader reader = null;
            try {
                reader = new InputStreamReader(new FileInputStream(cfg), "UTF-8");
                prop.load(reader);
                for(Entry<Object, Object> kv :prop.entrySet()) {
                    String key = kv.getKey().toString();
                    String val = kv.getValue().toString();
                    
                    key = String.format("%s.%s", FilenameUtils.getBaseName(cfg.getName()), key);
                    if(!val.equals(systemconfig.get(key))) {
                        update.setProperty(key, val);
                    }
                    systemconfig.setProperty(key, val);
                }
                logger.info("loaded file {}", cfg.getName());
                
            } catch (Exception e) {
                logger.error("", e);
            } finally {
                IOUtils.closeQuietly(reader);
            }
        }
        
        logger.info("---- loading properties files, finished -------");
        return update;
    }
    
//    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
//    private KafkaConsumerService kafkaConsumerService;
//    
//    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
//    private KafkaPublisherService kafkaPublisherService;
    
//    public RefreshConfigServiceImpl() {
//        List<String> topics = new ArrayList<>();
//        topics.add(MQ_REFRESH_TOPIC);
//        kafkaConsumerService.subscribe(topics, new MQCallback(), 1);
//    }
//    
//    private class MQCallback implements KafkaConsumerCallBack{
//
//        @Override
//        public void process(ConsumerRecord<String, String> record) {
//            if(!MQ_REFRESH_TOPIC.equals(record.topic())) {
//                return;
//            }
//            
//            if(!MQ_REFRESH_VALUE.equals(record.value())) {
//                return;
//            }
//            
//            refresh();
//        }
//        
//    }

    @Override
    public void register(Object instance) {
        if(instance==null) {
            throw new NullPointerException("instance cannot be null");
        }

        // 重复注册
        if(refreshScope.contains(instance)) {
            return;
        }

        Class<? extends Object> clazz = instance.getClass();
        RefreshScope scope = clazz.getAnnotation(RefreshScope.class);
        if(scope==null) {
            throw new AnnotationException("Not a class tagged by @Refreshscope");
        }

        refreshScope.add(instance);
        
        refresh(systemconfig, true, instance);
    }
    
    @Override
    public void unregister(Object instance) {
        
        if(refreshScope.contains(instance)) {
            refreshScope.remove(instance);
        }
    }

    @Override
    public String refresh() {
        return refresh(reload());
    }

    @Override
    public String mqRresh() {
//        kafkaPublisherService.sendStr(new ProducerRecord<String, String>(MQ_REFRESH_TOPIC, MQ_REFRESH_VALUE));
        return "has notified all nodes refresh properties";
    }
    
    @Override
    public String refresh(Properties config) {
        return refresh(config, false, refreshScope);
    }
    
    /**
     * 刷新注入属性值
     * @param config
     * @param objects
     * @param active
     * @return
     */
    public String refresh(Properties config, boolean active, Object ...objects) {
        if(config == null) {
            return "";
        }
        
        if(!active) {
            logger.info("refresh start ...");
        }
        List<Field> fields = new ArrayList<>();
        try {
            
            for(Object obj :objects) {
                fields.clear();
                fields.addAll(Arrays.asList(obj.getClass().getDeclaredFields()));
                fields.addAll(Arrays.asList(obj.getClass().getSuperclass().getDeclaredFields()));
                
                fields.removeIf(field -> field.getAnnotation(Value.class)==null);
                
                for(Field field :fields) {
                    boolean acc = field.isAccessible();
                    field.setAccessible(true);
                    
                    Value annVal = field.getAnnotation(Value.class);
                    String value = annVal.value().trim();
                    
                    if(value.matches("\\$\\{[ ]*[a-zA-Z_0-9\\.]+[ ]*\\}")) {
                        String key = value.substring(2, value.length()-1).trim();
                        if(!config.containsKey(key)) {
                            if(!active) {
                                continue;
                            }
                            throw new AnnotationException(String.format("properties not contains key '%s'", key));
                        }
                        setValue(obj, field, config.getProperty(key));
                    } else {
                        setValue(obj, field, value);
                    }
                    
                    field.setAccessible(acc);
                }
            }
            
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            throw new AnnotationException(ex);
        }
        
        String res = config.toString();
        if(!active) {
            config.clear();
            logger.info("update properties: {}", res);
            logger.info("refresh finished");
        }
        
        return res;
    }
    
    private void setValue(Object obj, Field field, String value) throws IllegalArgumentException, IllegalAccessException {
        
       if(field.get(obj) instanceof Integer) {
           
           field.setInt(obj, Integer.parseInt(value));
       }
       
       else if(field.get(obj) instanceof Long) {
           
           field.setLong(obj, Long.parseLong(value));
       } 
       
       else if(field.get(obj) instanceof Boolean) {
           
           field.setBoolean(obj, Boolean.parseBoolean(value));
       } 
       
       else if(field.get(obj) instanceof Byte) {
           
           field.setByte(obj, Byte.parseByte(value));
       }
       
       else if(field.get(obj) instanceof Double) {
           
           field.setDouble(obj, Double.parseDouble(value));
       }
       
       else if(field.get(obj) instanceof Float) {
           
           field.setFloat(obj, Float.parseFloat(value));
       }
       
       else if(field.get(obj) instanceof Short) {
           
           field.setShort(obj, Short.parseShort(value));
       }
       else if(field.get(obj) instanceof Character) {
           
           field.setChar(obj, value.charAt(0));
       } else {
           field.set(obj, value);
       }
       
    }


}
