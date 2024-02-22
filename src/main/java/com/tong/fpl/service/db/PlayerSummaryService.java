package com.tong.fpl.service.db;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tong.fpl.domain.entity.PlayerSummaryEntity;
import com.tong.fpl.mapper.PlayerSummaryMapper;
import org.springframework.stereotype.Service;

/**
 * Create by tong on 2022/7/14
 */
@Service
public class PlayerSummaryService extends ServiceImpl<PlayerSummaryMapper, PlayerSummaryEntity> implements IService<PlayerSummaryEntity> {

}
