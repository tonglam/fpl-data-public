package com.tong.fpl.service.db;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tong.fpl.domain.entity.TournamentEntryEntity;
import com.tong.fpl.mapper.TournamentEntryMapper;
import org.springframework.stereotype.Service;

/**
 * Create by tong on 2021/8/30
 */
@Service
public class TournamentEntryService extends ServiceImpl<TournamentEntryMapper, TournamentEntryEntity> implements IService<TournamentEntryEntity> {

}
