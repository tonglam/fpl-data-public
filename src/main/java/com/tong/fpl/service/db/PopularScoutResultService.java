package com.tong.fpl.service.db;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tong.fpl.domain.entity.PopularScoutResultEntity;
import com.tong.fpl.mapper.PopularScoutResultMapper;
import org.springframework.stereotype.Service;

/**
 * Created by tong on 2022/08/08
 */
@Service
public class PopularScoutResultService extends ServiceImpl<PopularScoutResultMapper, PopularScoutResultEntity> implements IService<PopularScoutResultEntity> {

}
