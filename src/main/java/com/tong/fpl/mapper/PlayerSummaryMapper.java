package com.tong.fpl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tong.fpl.domain.entity.PlayerSummaryEntity;
import org.apache.ibatis.annotations.Select;

/**
 * Create by tong on 2022/7/14
 */
public interface PlayerSummaryMapper extends BaseMapper<PlayerSummaryEntity> {

    @Select("TRUNCATE TABLE player_summary; ")
    void truncate();

}
