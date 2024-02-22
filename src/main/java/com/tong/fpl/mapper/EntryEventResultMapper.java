package com.tong.fpl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tong.fpl.domain.entity.EntryEventResultEntity;
import org.apache.ibatis.annotations.Select;

/**
 * Create by tong on 2021/8/30
 */
public interface EntryEventResultMapper extends BaseMapper<EntryEventResultEntity> {

    @Select("SELECT IFNULL(SUM(event_points), 0) FROM entry_event_result WHERE entry = #{entry} And `event` between #{startGw} and #{endGw} ")
    int sumEventPoints(int startGw, int endGw, int entry);

    @Select("SELECT IFNULL(SUM(event_transfers_cost), 0) FROM entry_event_result WHERE entry = #{entry} And `event` between #{startGw} and #{endGw} ")
    int sumEventTransfersCost(int startGw, int endGw, int entry);

    @Select("SELECT IFNULL(SUM(event_net_points), 0) FROM entry_event_result WHERE entry = #{entry} And `event` between #{startGw} and #{endGw} ")
    int sumEventNetPoints(int startGw, int endGw, int entry);

}
