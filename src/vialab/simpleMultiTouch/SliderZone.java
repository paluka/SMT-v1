package vialab.simpleMultiTouch;



public class SliderZone extends RectZone {
	int min, max, init, numValues, value, slider;
	int[] slideColour, sliderColour;
/**
	 * 
	 * @param x int - X-coordinate of the upper left corner of the zone
	 * @param y int - Y-coordinate of the upper left corner of the zone
	 * @param width int - Width of the zone
	 * @param height int - Height of the zone
	 */
	public SliderZone(float x, float y, float width, float height, int min, int max, int init){
		super(x, y, width, height);
		this.min = min;
		this.max = max;
		this.init = init;
		value = this.init;
		//calculate the initial position of the slider
		slider = (int)(((init - min)/(float)(max-min))*height + y);


		slideColour = new int[3];
		sliderColour = new int[3];
		
		slideColour[0] = 0;
		slideColour[1] = 0;
		slideColour[2] = 0;
		
		sliderColour[0] = 255;
		sliderColour[1] = 255;
		sliderColour[2] = 255;
	}
	
	public void setSlideColour(int r, int g, int b){
		slideColour[0] = r;
		slideColour[1] = g;
		slideColour[2] = b;
	}
	
	public void setSliderColour(int r, int g, int b){
		sliderColour[0] = r;
		sliderColour[1] = g;
		sliderColour[2] = b;
	}
	
	public int getValue(){
		return value;
	}
	
	public void drawZone(){
		TouchClient.parent.noStroke();
		TouchClient.parent.fill(slideColour[0], slideColour[1], slideColour[2]);
		TouchClient.parent.rect(getX(), getY(), getWidth(), getHeight());
		
		TouchClient.parent.fill(sliderColour[0], sliderColour[1], sliderColour[2]);
		TouchClient.parent.rect(getX(), slider, getWidth(), getHeight()/10);
	}
	
	public boolean addTouch(Touch t){
		if(t.getScreenY(TouchClient.parent.height) < getY() + getHeight() - getHeight()/10 && this.contains(t.x, t.y)){
			slider = t.getScreenY(TouchClient.parent.height);
			value = (int)(((slider-this.getY())/(float)(this.getHeight()))*(max-min) + min);
		}
		return true;
	}
	
	public boolean updateTouch(Touch t){
		if(t.getScreenY(TouchClient.parent.height) < getY() + getHeight() - getHeight()/10 && this.contains(t.x, t.y)){
			slider = t.getScreenY(TouchClient.parent.height);
			value = (int)(((slider-this.getY())/(float)(this.getHeight()))*(max-min) + min);
		}
		return true;
	}
}

