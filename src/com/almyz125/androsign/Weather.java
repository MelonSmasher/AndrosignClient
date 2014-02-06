package com.almyz125.androsign;

public class Weather {
	String description;
	String city;
	String region;
	String country;

	String windChill;
	String windDirection;
	String windSpeed;

	String sunrise;
	String sunset;

	String conditiontext;
	String conditiondate;
	String conditioncode;

	String day1, day2, day3, day4, day5;

	public String toString() {

		return description.replaceFirst("Yahoo!", "") + "\n\n"

		+ "Condition: " + conditiontext + "\n"

		+ "Temperature: " + windChill + (char) 0x00B0 + "F\n"
				+ "Wind direction: " + windDirection + (char) 0x00B0 + "\n"
				+ "Wind speed: " + windSpeed + " mph\n\n";

	}
}