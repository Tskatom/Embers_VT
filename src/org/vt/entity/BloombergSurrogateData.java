package org.vt.entity;

import java.util.UUID;

public class BloombergSurrogateData {
	public String embersId; // Unique UUID
	public String date; // The day related, format yyyy/MM/dd
	public String[] derivedFrom; //from which EnrichData,recoding the number of sequenceID
	public String model; //Using which model
	public String location; //To country level
	public String population; //The name of the stock index
	public int confidence; // TODO: For test just using staic value 60
	public boolean confidenceIsProbability; //TODO: For test just using false
	public String shiftType; //Trend
	public String valueSpectrum; //value: Zscore Sigma
	public String direction; //value: positive|calm|negative
	public String strength;//Sigma Value
	public String shiftDate; //the date related, format yyyy/MM/dd
	
	public BloombergSurrogateData()
	{
		embersId = UUID.randomUUID().toString();
	}
	
	
	public String getEmbersId() {
		return embersId;
	}
	public void setEmbersId(String embersId) {
		this.embersId = embersId;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String[] getDerovedFrom() {
		return derivedFrom;
	}
	public void setDerovedFrom(String[] derovedFrom) {
		this.derivedFrom = derovedFrom;
	}
	public String getModel() {
		return model;
	}
	public void setModel(String model) {
		this.model = model;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getPopulation() {
		return population;
	}
	public void setPopulation(String population) {
		this.population = population;
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
	public String getShiftType() {
		return shiftType;
	}
	public void setShiftType(String shiftType) {
		this.shiftType = shiftType;
	}
	public String getValueSpectrum() {
		return valueSpectrum;
	}
	public void setValueSpectrum(String valueSpectrum) {
		this.valueSpectrum = valueSpectrum;
	}
	public String getDirection() {
		return direction;
	}
	public void setDirection(String direction) {
		this.direction = direction;
	}
	public String getStrength() {
		return strength;
	}
	public void setStrength(String strength) {
		this.strength = strength;
	}
	public String getShiftDate() {
		return shiftDate;
	}
	public void setShiftDate(String shiftDate) {
		this.shiftDate = shiftDate;
	}

}

