package com.tong.fpl.domain.data.entry;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Create by tong on 2021/8/30
 */
@Data
public class Match {

    private int id;
    @JsonProperty("entry_1_entry")
    private int entry1Entry;
    @JsonProperty("entry_1_name")
    private String entry1Name;
    @JsonProperty("entry_1_player_name")
    private String entry1PlayerName;
    @JsonProperty("entry_1_points")
    private int entry1Points;
    @JsonProperty("entry_1_win")
    private int entry1Win;
    @JsonProperty("entry_1_draw")
    private int shortName;
    @JsonProperty("entry_1_loss")
    private int entry1Loss;
    @JsonProperty("entry_1_total")
    private int entry1Total;
    @JsonProperty("entry_2_entry")
    private int entry2Entry;
    @JsonProperty("entry_2_name")
    private String entry2Name;
    @JsonProperty("entry_2_player_name")
    private String entry2PlayerName;
    @JsonProperty("entry_2_points")
    private int entry2Points;
    @JsonProperty("entry_2_win")
    private int entry2Win;
    @JsonProperty("entry_2_draw")
    private int entry2Draw;
    @JsonProperty("entry_2_loss")
    private int entry2Loss;
    @JsonProperty("entry_2_total")
    private int entry2Total;
    @JsonProperty("is_knockout")
    private boolean isKnockout;
	private int winner;
	@JsonProperty("seed_value")
	private String seedValue;
	private int event;
	private String tiebreak;

}
