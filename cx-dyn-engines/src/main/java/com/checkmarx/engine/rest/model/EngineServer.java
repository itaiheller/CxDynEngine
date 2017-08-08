package com.checkmarx.engine.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class EngineServer {
	
	private Long id;
	private String name;
	private String uri;
	private int minLoc;
	private int maxLoc;
	@JsonProperty(value="isAlive")
	private Boolean alive; 
	private int maxScans;
	@JsonProperty(value="isBlocked")
	private boolean blocked;
	private String cxVersion;
	
	public EngineServer() {
		// default .ctor for unmarshalling
	}
	
	public EngineServer(String name, String uri, int minLoc, int maxLoc, int maxScans, boolean isBlocked) {
		this.name = name;
		this.uri = uri;
		this.minLoc = minLoc;
		this.maxLoc = maxLoc;
		this.maxScans = maxScans;
		this.blocked = isBlocked;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getUri() {
		return uri;
	}

	public int getMinLoc() {
		return minLoc;
	}

	public void setMinLoc(int minLoc) {
		this.minLoc = minLoc;
	}

	public int getMaxLoc() {
		return maxLoc;
	}

	public void setMaxLoc(int maxLoc) {
		this.maxLoc = maxLoc;
	}

	public Boolean isAlive() {
		return alive;
	}

	public int getMaxScans() {
		return maxScans;
	}

	public boolean isBlocked() {
		return blocked;
	}

	public void setBlocked(boolean blocked) {
		this.blocked = blocked;
	}

	public String getCxVersion() {
		return cxVersion;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("id", id)
				.add("name", name)
				.add("uri", uri)
				.add("minLOC", minLoc)
				.add("maxLOC", maxLoc)
				.add("isAlive", alive)
				.add("maxScans", maxScans)
				.add("isBlocked", blocked)
				.add("cxVersion", cxVersion)
				.omitNullValues()
				.toString();
	}
}
