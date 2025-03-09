package com.securefromscratch.busybee.config;

import com.securefromscratch.busybee.safety.*;
import com.securefromscratch.busybee.storage.Task;
import com.securefromscratch.busybee.storage.TasksStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.ObjectInputFilter;
import java.util.List;

@Configuration
public class SerializationConfig {

    private static final Logger logger = LoggerFactory.getLogger(SerializationConfig.class);

    @PostConstruct
    public void setSerialFilter() {
        ObjectInputFilter filter = info -> {
            if (info.serialClass() != null && !List.class.isAssignableFrom(info.serialClass()) &&
                    !Task.class.isAssignableFrom(info.serialClass()) &&
                    !TasksStorage.class.isAssignableFrom(info.serialClass()) &&
                    !Description.class.isAssignableFrom(info.serialClass()) &&
                    !DueDate.class.isAssignableFrom(info.serialClass()) &&
                    !DueTime.class.isAssignableFrom(info.serialClass()) &&
                    !Name.class.isAssignableFrom(info.serialClass()) &&
                    !Username.class.isAssignableFrom(info.serialClass()) &&
                    !info.serialClass().getName().startsWith("java") &&
                    !info.serialClass().isArray() &&
            !info.serialClass().getName().startsWith("com.securefromscratch.busybee.")) {
                logger.error("Rejected class: " + info.serialClass().getName());
                return ObjectInputFilter.Status.REJECTED;
            }
            return ObjectInputFilter.Status.ALLOWED;
        };
        ObjectInputFilter.Config.setSerialFilter(filter);
    }
}