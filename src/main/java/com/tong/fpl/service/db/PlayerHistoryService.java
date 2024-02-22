package com.tong.fpl.service.db;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tong.fpl.domain.entity.PlayerHistoryEntity;
import com.tong.fpl.mapper.PlayerHistoryMapper;
import org.springframework.stereotype.Service;

/**
 * Create by tong on 2021/9/4
 */
@Service
public class PlayerHistoryService extends ServiceImpl<PlayerHistoryMapper, PlayerHistoryEntity> implements IService<PlayerHistoryEntity> {

}
