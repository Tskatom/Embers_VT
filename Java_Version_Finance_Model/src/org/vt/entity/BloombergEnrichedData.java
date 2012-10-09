package org.vt.entity;

import java.util.UUID;

public class BloombergEnrichedData {
	public int sequenceId;
	public int subSequence;
	public int endSubSequence;
	public String stockIndex;
	public String date;
	public float zscore30;
	public float zscore90;
	public float posMa3Before;
	public float negMa3before;
	public float dayChange;
	public float posMa3After;
	public float negMa3After;
	public String location;
	
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public int getSequenceId() {
		return sequenceId;
	}
	public void setSequenceId(int sequenceId) {
		this.sequenceId = sequenceId;
	}
	public int getSubSequence() {
		return subSequence;
	}
	public void setSubSequence(int subSequence) {
		this.subSequence = subSequence;
	}
	public int getEndSubSequence() {
		return endSubSequence;
	}
	public void setEndSubSequence(int endSubSequence) {
		this.endSubSequence = endSubSequence;
	}
	public String getStockIndex() {
		return stockIndex;
	}
	public void setStockIndex(String stockIndex) {
		this.stockIndex = stockIndex;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public float getZscore30() {
		return zscore30;
	}
	public void setZscore30(float zscore30) {
		this.zscore30 = zscore30;
	}
	public float getZscore90() {
		return zscore90;
	}
	public void setZscore90(float zscore90) {
		this.zscore90 = zscore90;
	}
	public float getPosMa3Before() {
		return posMa3Before;
	}
	public void setPosMa3Before(float posMa3Before) {
		this.posMa3Before = posMa3Before;
	}
	public float getNegMa3before() {
		return negMa3before;
	}
	public void setNegMa3before(float negMa3before) {
		this.negMa3before = negMa3before;
	}
	public float getDayChange() {
		return dayChange;
	}
	public void setDayChange(float dayChange) {
		this.dayChange = dayChange;
	}
	public float getPosMa3After() {
		return posMa3After;
	}
	public void setPosMa3After(float posMa3After) {
		this.posMa3After = posMa3After;
	}
	public float getNegMa3After() {
		return negMa3After;
	}
	public void setNegMa3After(float negMa3After) {
		this.negMa3After = negMa3After;
	}
	
	

}
