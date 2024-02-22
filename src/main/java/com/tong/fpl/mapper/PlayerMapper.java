package com.tong.fpl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tong.fpl.domain.entity.PlayerEntity;
import org.apache.ibatis.annotations.Select;

/**
 * Create by tong on 2021/8/30
 */
public interface PlayerMapper extends BaseMapper<PlayerEntity> {

    @Select("TRUNCATE TABLE player; ")
    void truncate();

}
