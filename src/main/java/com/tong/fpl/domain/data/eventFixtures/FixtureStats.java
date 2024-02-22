package com.tong.fpl.domain.data.eventFixtures;

import lombok.Data;

import java.util.List;

/**
 * Create by tong on 2021/8/30
 */
@Data
public class FixtureStats {

	private String identifier;
	private List<FixtureStatsDetail> a;
	private List<FixtureStatsDetail> h;

}
