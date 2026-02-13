package com.helloworld.config;

import com.helloworld.contract.UniversalContractFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers the universal contract filter for /api/v1 so headers are validated and request context is set.
 */
@Configuration
public class UniversalContractConfig {

    @Bean
    public FilterRegistrationBean<UniversalContractFilter> universalContractFilterRegistration(UniversalContractFilter filter) {
        FilterRegistrationBean<UniversalContractFilter> reg = new FilterRegistrationBean<>(filter);
        reg.addUrlPatterns("/api/v1/*");
        reg.setOrder(1);
        return reg;
    }
}
