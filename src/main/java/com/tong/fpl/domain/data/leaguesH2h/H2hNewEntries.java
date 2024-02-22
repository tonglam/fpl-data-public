package com.tong.fpl.domain.data.leaguesH2h;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Create by tong on 2021/8/30
 */
@Data
public class H2hNewEntries {

	@JsonProperty("has_next")
	private boolean hasNext;
	private int page;
	private List<H2hResult> results;

}
