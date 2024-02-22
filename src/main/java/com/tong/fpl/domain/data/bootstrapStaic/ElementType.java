package com.tong.fpl.domain.data.bootstrapStaic;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Create by tong on 2021/8/30
 */
@Data
public class ElementType {

	private int id;
	@JsonProperty("plural_name")
	private String pluralName;
	@JsonProperty("plural_name_short")
	private String pluralNameShort;
	@JsonProperty("singular_name")
	private String singularName;
	@JsonProperty("singular_name_short")
	private String singularNameShort;
	@JsonProperty("squad_select")
	private int squadSelect;
	@JsonProperty("squad_min_play")
	private int squadMinPlay;
	@JsonProperty("squad_max_play")
	private int squadMaxPlay;
	@JsonProperty("ui_shirt_specific")
	private boolean uiShirtSpecific;
	@JsonProperty("sub_positions_locked")
	private List<String> subPositionsLocked;
	@JsonProperty("element_count")
	private int elementCount;

}
