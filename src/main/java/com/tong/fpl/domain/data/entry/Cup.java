package com.tong.fpl.domain.data.entry;

import lombok.Data;

import java.util.List;

/**
 * Create by tong on 2021/8/30
 */
@Data
public class Cup {

	private List<Match> matches;
	private CupStatus status;

}
