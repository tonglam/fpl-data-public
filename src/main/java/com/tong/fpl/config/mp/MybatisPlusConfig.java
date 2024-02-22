package com.tong.fpl.config.mp;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.DynamicTableNameInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.tong.fpl.constant.enums.DynamicTableName;
import com.tong.fpl.utils.CommonUtils;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * Create by tong on 2021/8/30
 */
@Configuration
@MapperScan("com.tong.fpl.mapper")
public class MybatisPlusConfig {

    public static ThreadLocal<String> season = new ThreadLocal<>();

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        DynamicTableNameInnerInterceptor dynamicTableNameInnerInterceptor = new DynamicTableNameInnerInterceptor();
        Arrays.stream(DynamicTableName.values()).forEach(dynamicTableName ->
                dynamicTableNameInnerInterceptor.setTableNameHandler((sql, tableName) -> {
                    String season = MybatisPlusConfig.season.get();
                    if (StringUtils.isBlank(season) || StringUtils.equals(season, CommonUtils.getCurrentSeason())) {
                        return tableName;
                    }
                    return tableName + "_" + season;
                }));
        interceptor.addInnerInterceptor(dynamicTableNameInnerInterceptor);
        return interceptor;
    }

}
