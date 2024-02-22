package com.tong.fpl.service.db;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tong.fpl.domain.entity.EntryEventResultEntity;
import com.tong.fpl.mapper.EntryEventResultMapper;
import org.springframework.stereotype.Service;

/**
 * Create by tong on 2021/8/30
 */
@Service
public class EntryEventResultService extends ServiceImpl<EntryEventResultMapper, EntryEventResultEntity> implements IService<EntryEventResultEntity> {

	public int sumEventPoints(int current, int startGw, int endGw, int entry) {
		if (current < startGw) {
			return 0;
		} else if (current > endGw) {
			current = endGw;
		}
		return this.baseMapper.sumEventPoints(startGw, current, entry);
	}

	public int sumEventTransferCost(int current, int startGw, int endGw, int entry) {
		if (current < startGw) {
			return 0;
		} else if (current > endGw) {
			current = endGw;
		}
		return this.baseMapper.sumEventTransfersCost(startGw, current, entry);
	}

	public int sumEventNetPoints(int current, int startGw, int endGw, int entry) {
		if (current < startGw) {
			return 0;
		} else if (current > endGw) {
			current = endGw;
		}
		return this.baseMapper.sumEventNetPoints(startGw, current, entry);
	}

}
