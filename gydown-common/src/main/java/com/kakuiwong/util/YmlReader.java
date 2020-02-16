package com.kakuiwong.util;

import com.kakuiwong.domain.CoreYmlConfig;
import com.kakuiwong.domain.GydownException;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
public class YmlReader {

    public static void readYml(InputStream inputStream, Object bean) {
        if (Objects.isNull(bean)) {
            throw new GydownException("YmlReader: Bean cannot be null");
        }
        if (Objects.isNull(inputStream)) {
            throw new GydownException("YmlReader: InputStream cannot be null");
        }
        Yaml yaml = new Yaml();
        Map obj = (Map) yaml.load(inputStream);
        if (Objects.isNull(obj)) {
            throw new GydownException("YmlReader: read null");
        }
        setField(bean, obj);
    }

    private static void setField(Object bean, Map obj) {
        Class<?> aClass = bean.getClass();
        Field[] declaredFields = aClass.getDeclaredFields();
        Arrays.stream(declaredFields).forEach(field -> {
            field.setAccessible(true);
            String name = field.getName();
            try {
                field.set(bean, obj.get(name));
            } catch (IllegalAccessException e) {
                throw new GydownException("YmlReader: set field error");
            }
        });
    }

    public static void main(String[] args) throws FileNotFoundException {
        CoreYmlConfig c = new CoreYmlConfig();
        FileInputStream inputStream = new FileInputStream(System.getProperty("user.dir") + File.separator + "app1.yml");
    }
}
