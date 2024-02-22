package com.tong.fpl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tong.fpl.domain.entity.PlayerStatEntity;
import org.apache.ibatis.annotations.Select;

/**
 * Create by tong on 2021/8/30
 */
public interface PlayerStatMapper extends BaseMapper<PlayerStatEntity> {

    @Select("TRUNCATE TABLE player_stat; ")
    void truncate();

}
