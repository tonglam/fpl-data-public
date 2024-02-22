package com.tong.fpl.service.db;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tong.fpl.domain.entity.EntryLeagueInfoEntity;
import com.tong.fpl.mapper.EntryLeagueInfoMapper;
import org.springframework.stereotype.Service;

/**
 * Create by tong on 2021/8/30
 */
@Service
public class EntryLeagueInfoService extends ServiceImpl<EntryLeagueInfoMapper, EntryLeagueInfoEntity> implements IService<EntryLeagueInfoEntity> {

}
