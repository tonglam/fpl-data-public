package com.tong.fpl.domain.data.bootstrapStaic;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Create by tong on 2021/8/30
 */
@Data
public class Event {

	private int id;
	private String name;
	@JsonProperty("deadline_time")
	private String deadlineTime;
	@JsonProperty("average_entry_score")
	private int averageEntryScore;
	private boolean finished;
	@JsonProperty("data_checked")
	private boolean dataChecked;
	@JsonProperty("highest_scoring_entry")
	private int highestScoringEntry;
	@JsonProperty("deadline_time_epoch")
	private int deadlineTimeEpoch;
	@JsonProperty("deadline_time_game_offset")
	private int deadlineTimeGameOffset;
	@JsonProperty("highest_score")
	private int highestScore;
	@JsonProperty("is_previous")
	private boolean isPrevious;
	@JsonProperty("is_current")
	private boolean isCurrent;
	@JsonProperty("is_next")
	private boolean isNext;
	@JsonProperty("chip_plays")
	private List<Chips> chipPlays;
	@JsonProperty("most_selected")
	private int mostSelected;
	@JsonProperty("most_transferred_in")
	private int mostTransferredIn;
	@JsonProperty("top_element")
	private int topElement;
	@JsonProperty("top_element_info")
	private TopElementInfo topElementInfo;
	@JsonProperty("transfers_made")
	private int transfersMade;
	@JsonProperty("most_captained")
	private int mostCaptained;
	@JsonProperty("most_vice_captained")
	private int mostViceCaptained;

}
