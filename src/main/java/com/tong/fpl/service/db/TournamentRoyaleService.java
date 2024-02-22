package com.tong.fpl.service.db;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tong.fpl.domain.entity.TournamentRoyaleEntity;
import com.tong.fpl.mapper.TournamentRoyaleMapper;
import org.springframework.stereotype.Service;

/**
 * Create by tong on 18/8/2023
 */
@Service
public class TournamentRoyaleService extends ServiceImpl<TournamentRoyaleMapper, TournamentRoyaleEntity> implements IService<TournamentRoyaleEntity> {

}
