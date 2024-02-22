package com.tong.fpl.config.collector;

import com.google.common.collect.Maps;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.domain.entity.PlayerValueEntity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * input: all records in play_value table
 * accumulate: key:element; value:map-key:change_date; value: player value
 * return: key:element; value: last player value
 * <p>
 * Create by tong on 2021/8/30
 */
public class PlayerValueCollector implements Collector<PlayerValueEntity, Map<Integer, Map<String, PlayerValueEntity>>, Map<Integer, PlayerValueEntity>> {

	@Override
	public Supplier<Map<Integer, Map<String, PlayerValueEntity>>> supplier() {
		return Maps::newHashMap;
	}

	@Override
	public BiConsumer<Map<Integer, Map<String, PlayerValueEntity>>, PlayerValueEntity> accumulator() {
		return (Map<Integer, Map<String, PlayerValueEntity>> elementValuesMap, PlayerValueEntity playerValueEntity) -> {
			Map<String, PlayerValueEntity> map;
			if (elementValuesMap.containsKey(playerValueEntity.getElement())) {
				map = elementValuesMap.get(playerValueEntity.getElement());
			} else {
				map = Maps.newHashMap();
			}
			map.put(playerValueEntity.getChangeDate(), playerValueEntity);
			elementValuesMap.put(playerValueEntity.getElement(), map);
		};
	}

	@Override
	public BinaryOperator<Map<Integer, Map<String, PlayerValueEntity>>> combiner() {
		return (map1, map2) -> {
			map1.putAll(map2);
			return map1;
		};
	}

	@Override
	public Function<Map<Integer, Map<String, PlayerValueEntity>>, Map<Integer, PlayerValueEntity>> finisher() {
		return elementValuesMap -> {
			Map<Integer, PlayerValueEntity> valueMap = Maps.newHashMap();
			elementValuesMap.keySet().forEach(element -> {
				// sort by changDate, first record is the last one
				Map<String, PlayerValueEntity> sortedMap = Maps.newLinkedHashMap();
				elementValuesMap.get(element).entrySet()
						.stream()
						.sorted((o1, o2) ->
								LocalDate.parse(o2.getKey(), DateTimeFormatter.ofPattern(Constant.SHORTDAY))
										.compareTo(LocalDate.parse(o1.getKey(), DateTimeFormatter.ofPattern(Constant.SHORTDAY))))
						.forEachOrdered(o -> sortedMap.put(o.getKey(), o.getValue()));
				sortedMap.values().stream().findFirst().ifPresent(o -> valueMap.put(element, o));
			});
			return valueMap;
		};
	}

	@Override
	public Set<Characteristics> characteristics() {
		return Collections.unmodifiableSet(EnumSet.of(Characteristics.UNORDERED));
	}

}
