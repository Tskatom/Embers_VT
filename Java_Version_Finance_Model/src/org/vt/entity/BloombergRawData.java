package org.vt.entity;

public class BloombergRawData {
	public String embersId;
	public String stockIndex;
	public float currentValue;
	public float previousCloseValue;
	public String updateTime;
	public String queryTime;
	public String feed;
	
	public String getEmbersId() {
		return embersId;
	}
	public void setEmbersId(String embersId) {
		this.embersId = embersId;
	}
	public String getStockIndex() {
		return stockIndex;
	}
	public void setStockIndex(String stockIndex) {
		this.stockIndex = stockIndex;
	}
	public float getCurrentValue() {
		return currentValue;
	}
	public void setCurrentValue(float currentValue) {
		this.currentValue = currentValue;
	}
	public float getPreviousCloseValue() {
		return previousCloseValue;
	}
	public void setPreviousCloseValue(float previousCloseValue) {
		this.previousCloseValue = previousCloseValue;
	}
	public String getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}
	public String getQueryTime() {
		return queryTime;
	}
	public void setQueryTime(String queryTime) {
		this.queryTime = queryTime;
	}
	public String getFeed() {
		return feed;
	}
	public void setFeed(String feed) {
		this.feed = feed;
	} 
	
}

