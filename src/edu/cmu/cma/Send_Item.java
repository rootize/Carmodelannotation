package edu.cmu.cma;


import android.graphics.Bitmap;
import android.graphics.Point;

public class Send_Item {
	private static final int sent_width=640;
	private static final int sent_height=480;
private Point touchPoint;
private Bitmap bm_toSend;

public Send_Item(Bitmap bm, Point car_loc){
	
	double ratiox=(double)(bm_toSend.getWidth())/sent_width;
	double ratioy=(double)(bm_toSend.getHeight())/sent_height;
	setTouchPoint(new Point((int)(ratiox*car_loc.x), (int)(ratioy*car_loc.y)));
	
	bm_toSend=Bitmap.createScaledBitmap(bm, sent_width, sent_height, true);
}

public Point getTouchPoint() {
	return touchPoint;
}

public void setTouchPoint(Point touchPoint) {
	this.touchPoint = touchPoint;
}


}
