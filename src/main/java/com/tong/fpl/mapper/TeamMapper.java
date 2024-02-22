package com.tong.fpl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tong.fpl.domain.entity.TeamEntity;
import org.apache.ibatis.annotations.Select;

/**
 * Create by tong on 2021/8/30
 */
public interface TeamMapper extends BaseMapper<TeamEntity> {

    @Select("TRUNCATE TABLE team; ")
    void truncate();

}
