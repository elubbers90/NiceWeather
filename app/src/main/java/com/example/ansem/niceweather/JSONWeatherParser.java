
package com.example.ansem.niceweather;

import com.example.ansem.niceweather.model.Weather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONWeatherParser {

	public static Weather getWeather(String data) throws JSONException  {
		Weather weather = new Weather();

		JSONObject jObj = new JSONObject(data);

		JSONArray jArr = jObj.getJSONArray("weather");
		JSONObject JSONWeather = jArr.getJSONObject(0);
		weather.currentCondition.setMain(getString("main", JSONWeather));
		weather.currentCondition.setId(getInt("id", JSONWeather));

		JSONObject mainObj = getObject("main", jObj);
		weather.temperature.setTemp(Math.round((getFloat("temp", mainObj) - 273.15)));

		JSONObject wObj = getObject("wind", jObj);
		weather.wind.setSpeed(getFloat("speed", wObj));

		JSONObject cObj = getObject("clouds", jObj);
		weather.clouds.setPerc(getInt("all", cObj));
		return weather;
	}
	
	
	private static JSONObject getObject(String tagName, JSONObject jObj)  throws JSONException {
		return jObj.getJSONObject(tagName);
	}
	
	private static String getString(String tagName, JSONObject jObj) throws JSONException {
		return jObj.getString(tagName);
	}

	private static float  getFloat(String tagName, JSONObject jObj) throws JSONException {
		return (float) jObj.getDouble(tagName);
	}
	
	private static int  getInt(String tagName, JSONObject jObj) throws JSONException {
		return jObj.getInt(tagName);
	}
	
}
