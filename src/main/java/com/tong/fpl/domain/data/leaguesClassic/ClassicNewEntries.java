package com.tong.fpl.domain.data.leaguesClassic;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Create by tong on 2021/8/30
 */
@Data
public class ClassicNewEntries {

	@JsonProperty("has_next")
	private boolean hasNext;
	private int page;
	private List<ClassicResult> results;

}
