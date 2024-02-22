package com.tong.fpl.service.db;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tong.fpl.domain.entity.EventLiveExplainEntity;
import com.tong.fpl.mapper.EventLiveExplainMapper;
import org.springframework.stereotype.Service;

/**
 * Create by tong on 2021/9/8
 */
@Service
public class EventLiveExplainService extends ServiceImpl<EventLiveExplainMapper, EventLiveExplainEntity> implements IService<EventLiveExplainEntity> {

}
