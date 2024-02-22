package com.tong.fpl.config.collector;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.tong.fpl.constant.enums.MatchPlayStatus;
import com.tong.fpl.domain.letletme.live.LiveFixtureData;
import com.tong.fpl.domain.entity.EventFixtureEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * input: event_fixture
 * accumulate: map-> key:teamId, value:liveFixtureData
 * return: table-> row:teamId, column:playing;not start;finished, value:List<LiveFixtureData>
 * <p>
 * Create by tong on 2021/8/30
 */
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LiveFixtureCollector implements Collector<EventFixtureEntity, Map<Integer, List<LiveFixtureData>>, Table<Integer, MatchPlayStatus, List<LiveFixtureData>>> {

	private final Map<String, String> teamNameMap;
	private final Map<String, String> teamShortNameMap;

	@Override
	public Supplier<Map<Integer, List<LiveFixtureData>>> supplier() {
		return Maps::newHashMap;
	}

	@Override
	public BiConsumer<Map<Integer, List<LiveFixtureData>>, EventFixtureEntity> accumulator() {
		return (Map<Integer, List<LiveFixtureData>> map, EventFixtureEntity o) -> {
			// home team
			int teamH = o.getTeamH();
			List<LiveFixtureData> homeFixtureList = Lists.newArrayList();
			if (map.containsKey(teamH)) {
				homeFixtureList = map.get(teamH);
			}
			homeFixtureList.add(new LiveFixtureData()
					.setTeamId(o.getTeamH())
					.setTeamName(teamNameMap.getOrDefault(String.valueOf(o.getTeamH()), ""))
					.setTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(o.getTeamH()), ""))
					.setTeamScore(o.getTeamHScore())
					.setAgainstId(o.getTeamA())
					.setAgainstName(teamNameMap.getOrDefault(String.valueOf(o.getTeamA()), ""))
					.setAgainstShortName(teamShortNameMap.getOrDefault(String.valueOf(o.getTeamA()), ""))
					.setAgainstTeamScore(o.getTeamAScore())
					.setKickoffTime(o.getKickoffTime())
					.setScore(o.getTeamHScore() + "-" + o.getTeamAScore())
					.setWasHome(true)
					.setStarted(o.getStarted())
					.setFinished(o.getFinishedProvisional())
			);
			map.put(teamH, homeFixtureList);
			// away team
			int teamA = o.getTeamA();
			List<LiveFixtureData> awayFixtureList = Lists.newArrayList();
			if (map.containsKey(teamA)) {
				awayFixtureList = map.get(teamA);
			}
			awayFixtureList.add(new LiveFixtureData()
					.setTeamId(o.getTeamA())
					.setTeamName(teamNameMap.getOrDefault(String.valueOf(o.getTeamA()), ""))
					.setTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(o.getTeamA()), ""))
					.setTeamScore(o.getTeamAScore())
					.setAgainstId(o.getTeamH())
					.setAgainstName(teamNameMap.getOrDefault(String.valueOf(o.getTeamH()), ""))
					.setAgainstShortName(teamShortNameMap.getOrDefault(String.valueOf(o.getTeamH()), ""))
					.setAgainstTeamScore(o.getTeamHScore())
					.setKickoffTime(o.getKickoffTime())
					.setScore(o.getTeamAScore() + "-" + o.getTeamHScore())
					.setWasHome(false)
					.setStarted(o.getStarted())
					.setFinished(o.getFinished())
			);
			map.put(teamA, awayFixtureList);
		};
	}

	@Override
	public BinaryOperator<Map<Integer, List<LiveFixtureData>>> combiner() {
		return (map1, map2) -> {
			map1.putAll(map2);
			return map1;
		};
	}

	@Override
	public Function<Map<Integer, List<LiveFixtureData>>, Table<Integer, MatchPlayStatus, List<LiveFixtureData>>> finisher() {
		return map -> {
			Table<Integer, MatchPlayStatus, List<LiveFixtureData>> table = HashBasedTable.create();
			map.keySet().forEach(teamId -> {
				List<LiveFixtureData> fixtureList = map.get(teamId);
				fixtureList.forEach(o -> {
					MatchPlayStatus playStatus = this.getPlayStatus(o.isStarted(), o.isFinished());
					List<LiveFixtureData> list = Lists.newArrayList();
					if (table.contains(teamId, playStatus)) {
						list = table.get(teamId, playStatus);
					}
					if (CollectionUtils.isEmpty(list)) {
						list = Lists.newArrayList();
					}
					list.add(o);
					table.put(teamId, playStatus, list);
				});
			});
			return table;
		};
	}

	private MatchPlayStatus getPlayStatus(boolean started, boolean finished) {
		if (finished) {
			return MatchPlayStatus.Finished;
		} else {
			if (!started) {
				return MatchPlayStatus.Not_Start;
			}
		}
		return MatchPlayStatus.Playing;
	}

	@Override
	public Set<Characteristics> characteristics() {
		return Collections.unmodifiableSet(EnumSet.of(Characteristics.UNORDERED));
	}

}
