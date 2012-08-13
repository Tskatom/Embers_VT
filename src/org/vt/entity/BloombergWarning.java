package org.vt.entity;

public class BloombergWarning {
	
	public String ermbers_id;
	public String date;
	public String[] derivedFrom;
	public String model;
	public String eventType;
	public int confidence;
	public boolean confidenceIsProbability;
	public String eventDate;
	public String population;
	
	
	public String getErmbers_id() {
		return ermbers_id;
	}
	public void setErmbers_id(String ermbers_id) {
		this.ermbers_id = ermbers_id;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String[] getDerivedFrom() {
		return derivedFrom;
	}
	public void setDerivedFrom(String[] derivedFrom) {
		this.derivedFrom = derivedFrom;
	}
	public String getModel() {
		return model;
	}
	public void setModel(String model) {
		this.model = model;
	}
	public String getEventType() {
		return eventType;
	}
	public void setEventType(String eventType) {
		this.eventType = eventType;
	}
	public int getConfidence() {
		return confidence;
	}
	public void setConfidence(int confidence) {
		this.confidence = confidence;
	}
	public boolean isConfidenceIsProbability() {
		return confidenceIsProbability;
	}
	public void setConfidenceIsProbability(boolean confidenceIsProbability) {
		this.confidenceIsProbability = confidenceIsProbability;
	}
	public String getEventDate() {
		return eventDate;
	}
	public void setEventDate(String eventDate) {
		this.eventDate = eventDate;
	}
	public String getPopulation() {
		return population;
	}
	public void setPopulation(String population) {
		this.population = population;
	}
	
	
}
