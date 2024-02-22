package com.tong.fpl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tong.fpl.domain.entity.EventEntity;
import org.apache.ibatis.annotations.Select;

/**
 * Create by tong on 2021/8/30
 */
public interface EventMapper extends BaseMapper<EventEntity> {

    @Select("TRUNCATE TABLE event; ")
    void truncate();

}
