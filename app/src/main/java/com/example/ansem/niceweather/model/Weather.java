package com.example.ansem.niceweather.model;

public class Weather {
	
	public CurrentCondition currentCondition = new CurrentCondition();
	public Temperature temperature = new Temperature();
	public Wind wind = new Wind();
	public Rain rain = new Rain();
	public Snow snow = new Snow()	;
	public Clouds clouds = new Clouds();
	
	public byte[] iconData;
	
	public  class CurrentCondition {
		private String main;
		private int id;

		public String getMain() {
			return main;
		}
		public void setMain(String main) {
			this.main = main;
		}
		public int getId(){
			return id;
		}
		public void setId(int id){
			this.id=id;
		}
	}
	
	public  class Temperature {
		private float temp;
		
		public float getTemp() {
			return temp;
		}
		public void setTemp(float temp) {
			this.temp = temp;
		}
	}
	
	public  class Wind {
		private float speed;
		public float getSpeed() {
			return speed;
		}
		public void setSpeed(float speed) {
			this.speed = speed;
		}
	}
	
	public  class Rain {
		private float amount;
		public float getAmount() {
			return amount;
		}
		public void setAmount(float amount) {
			this.amount = amount;
		}
	}

	public  class Snow {
		private float amount;
		public float getAmount() {
			return amount;
		}
		public void setAmount(float ammount) {
			this.amount = ammount;
		}
	}
	
	public  class Clouds {
		private int perc;
		public int getPerc() {
			return perc;
		}
		public void setPerc(int perc) {
			this.perc = perc;
		}
	}

}
