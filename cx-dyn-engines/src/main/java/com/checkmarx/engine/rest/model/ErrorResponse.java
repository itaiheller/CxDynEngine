package com.checkmarx.engine.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.base.MoreObjects;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ErrorResponse {
	
	private Integer messageCode;
	private String messageDetails;
	private String message;
	
	public ErrorResponse() {
		// default .ctor for unmarshalling
	}
	
	public ErrorResponse(Integer messageCode, String messageDetails) {
		this.messageCode = messageCode;
		this.messageDetails = messageDetails;
	}

	public String getMessage() {
		return message;
	}

	public Integer getMessageCode() {
		return messageCode;
	}

	public String getMessageDetails() {
		return messageDetails;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("message", message)
				.add("messageCode", messageCode)
				.add("messageDetails", messageDetails)
				.omitNullValues()
				.toString();
	}

}
