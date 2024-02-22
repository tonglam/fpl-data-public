package com.tong.fpl.domain.data.userpick;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/8/30
 */
@Data
@Accessors(chain = true)
public class Pick {

	private int element;
	private int position;
	@JsonProperty("element_type_name")
	private String elementTypeName;
	@JsonProperty("web_name")
	private String webName;
	private int multiplier;
	@JsonProperty("is_captain")
	private boolean isCaptain;
	@JsonProperty("is_vice_captain")
	private boolean isViceCaptain;
	private int points;

}
