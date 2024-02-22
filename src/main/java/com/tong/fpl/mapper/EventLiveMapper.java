package com.tong.fpl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tong.fpl.domain.entity.EventLiveEntity;
import org.apache.ibatis.annotations.Select;

/**
 * Create by tong on 2021/8/30
 */
public interface EventLiveMapper extends BaseMapper<EventLiveEntity> {

    @Select("TRUNCATE TABLE event_live; ")
    void truncate();

}
