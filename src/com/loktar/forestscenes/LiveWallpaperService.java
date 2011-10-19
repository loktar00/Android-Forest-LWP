package com.loktar.forestscenes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import android.content.res.Configuration;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.service.wallpaper.WallpaperService;
import android.service.wallpaper.WallpaperService.Engine;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.WindowManager;
 
public class LiveWallpaperService extends WallpaperService {
	public static final String PREFERENCES = "com.loktar.forestscenes.livewallpaper";
	
	  @Override
	    public void onCreate() {
	        super.onCreate();
	    }

	    @Override
	    public void onDestroy() {
	        super.onDestroy();
	    }

	    @Override
	    public Engine onCreateEngine() {
	        return new ForestEngine();
	    }
	        
	    class ForestEngine extends Engine implements SharedPreferences.OnSharedPreferenceChangeListener {

	        private final Handler mHandler = new Handler();
        	
	        private int width = 0;
	        private int height = 0;
	        
	        private final Paint mPaint = new Paint();
	        private float mOffset;
	        private float mTouchX = -1;
	        private float mTouchY = -1;
	        private long mStartTime;
	        private float mCenterX;
	        private float mCenterY;
	        
	        /** Trees **/
	        private List<Tree> trees = new ArrayList<Tree>();
	        
	        private final Runnable mForest = new Runnable() {
	            public void run() {
	                drawFrame();
	            }
	        };
	        private boolean mVisible;
	        private SharedPreferences mPrefs;

	        ForestEngine() {
	            // Create a Paint to draw the lines for our cube
	            final Paint paint = mPaint;
	            paint.setColor(Color.rgb(54, 255, 0));
	            paint.setAntiAlias(true);
	            paint.setStrokeWidth(2);
	            paint.setStrokeCap(Paint.Cap.ROUND);
	            paint.setStyle(Paint.Style.STROKE);
	                
	            mStartTime = SystemClock.elapsedRealtime();
	            mPrefs = LiveWallpaperService.this.getSharedPreferences(PREFERENCES, 0);
	            mPrefs.registerOnSharedPreferenceChangeListener(this);
	            onSharedPreferenceChanged(mPrefs, null);
	        }

	        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
	        	
	        }

	        @Override
	        public void onCreate(SurfaceHolder surfaceHolder) {
	            super.onCreate(surfaceHolder);
	            setTouchEventsEnabled(true);
	        }

	        @Override
	        public void onDestroy() {
	            super.onDestroy();
	            mHandler.removeCallbacks(mForest);
	        }

	        @Override
	        public void onVisibilityChanged(boolean visible) {
	            mVisible = visible;
	            if (visible) {
	                drawFrame();
	            } else {
	                mHandler.removeCallbacks(mForest);
	            }
	        }

	        @Override
	        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	            super.onSurfaceChanged(holder, format, width, height);
	            
	            Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay(); 
	            
	            this.width = display.getWidth();
	            this.height = display.getHeight();
	            
	            drawFrame();
	        }

	        @Override
	        public void onSurfaceCreated(SurfaceHolder holder) {
	            super.onSurfaceCreated(holder);
	        }

	        @Override
	        public void onSurfaceDestroyed(SurfaceHolder holder) {
	            super.onSurfaceDestroyed(holder);
	            mVisible = false;
	            mHandler.removeCallbacks(mForest);
	        }

	        @Override
	        public void onOffsetsChanged(float xOffset, float yOffset,float xStep, float yStep, int xPixels, int yPixels) {
	            mOffset = xOffset;
	           // Log.v("trees", String.valueOf(xPixels));
	            drawFrame();
	        }

	        /*
	         * Store the position of the touch event so we can use it for drawing later
	         */
	        @Override
	        public void onTouchEvent(MotionEvent event) {
	            if (event.getAction() == MotionEvent.ACTION_MOVE) {
	                mTouchX = event.getX();
	                mTouchY = event.getY();
	            } else {
	                mTouchX = -1;
	                mTouchY = -1;
	            }
	            super.onTouchEvent(event);
	        }

	        /*
	         * Draw one frame of the animation. This method gets called repeatedly
	         */
	        void drawFrame() {
	            final SurfaceHolder holder = getSurfaceHolder();
	            // Create the initial trees
	            if(trees.size() == 0){
		            for(int i = 0; i < 60; i++){
		            	float yPos = this.height-((float)Math.random() * (this.height-190));
		            	
		            	float xPos = (float)Math.random() * this.width;
		            	if(xPos>this.width){
		            		xPos = this.width - 10;
		            	}
		            	trees.add(new Tree(xPos,yPos, this.width));
		            }
	            }
	            Collections.sort(trees,new Comparator<Tree>() {
		    		@Override
		    		public int compare(Tree one, Tree other) {
		    			Float change1 = Float.valueOf(one.y);
		    	        Float change2 = Float.valueOf(other.y);
		    	        return change1.compareTo(change2);
		            }
		        });
	            
	            Canvas c = null;
	            try {
	                c = holder.lockCanvas();
	                if (c != null) {
	                    // draw something
	                	drawForest(c);
	                	for (Tree tree : trees) {
                			tree.draw(c, mOffset);
	                	}
	                    drawTouchPoint(c);
	                }
	            } finally {
	                if (c != null) holder.unlockCanvasAndPost(c);
	            }

	            mHandler.removeCallbacks(mForest);
	            if (mVisible) {
	                mHandler.postDelayed(mForest, 1000 / 15);
	            }
	        }

	        
	        void drawForest(Canvas c) {
	            // Draw the background
	            Paint paint = new Paint();
	            paint.setColor(Color.rgb(180,210,230));
	            c.drawRect(0, 0, this.width, 220, paint);
	            paint.setColor(Color.rgb(200,123,26));
	            c.drawRect(0, 220, this.width, this.height, paint);
	        }

	        void drawTouchPoint(Canvas c) {
	           
	        }
	    }
	    
	    /** For the TREES **/
	    class Tree {
	    	
	    	private float x = 0;
	    	private float y = 0;
	    	private float radius = 0;
	    	private float[] ranPoints;
	    	private int screenWidth = 0;
	    	private float curOffset = 0;
	    	
	    	private Paint trunkPaint;
	    	private Paint shadowPaint;
	    	private Paint canopyPaint;
	    	
	    	public Tree(float x, float y, int width){
	    		
	    		this.x = x;
	    		this.y = y;
	    		this.screenWidth = width;
	    		this.radius = (float)Math.random()*16+5;
	    		this.ranPoints = new float[20];
	    		
	    		trunkPaint = new Paint();
	    		trunkPaint.setColor(Color.BLACK);
	    		
	    		shadowPaint = new Paint();
	    		shadowPaint.setColor(Color.rgb(165,96,7));
	    		
	    		canopyPaint = new Paint();
	    		canopyPaint.setColor(Color.rgb(233+(int)Math.floor(Math.random()*20),117+(int)Math.floor(Math.random()*100),18));
	    		
	    		for(int i = 0; i < 20; i++){
	    			this.ranPoints[i] = (float)(Math.random() * this.radius / 2)+3;
	    		}
	    	}
	    	
	    	public void draw(Canvas c, float offset){
				
	    		if(offset!=curOffset){
	    			if(curOffset < offset){
	    				this.x -= this.y/100;
	    			}else{
	    				this.x += this.y/100;
	    			}
	    			curOffset = offset;
	    		}
	    		if(this.x <= -20){
	    			this.x = this.screenWidth+20; 
	    		}else if(this.x > this.screenWidth+20){
	    			this.x = -20;
	    		}
	    		if(this.x >0 && this.x < this.screenWidth){
		    		Path path = new Path();
					
		    		// shadow
					path.moveTo(x,y);
					path.lineTo(x,y+32);
					path.lineTo(x+22,y+38);
					path.lineTo(x,y+35);
					path.close();
					c.drawPath(path, shadowPaint);	
					
					// Trunk
		    		path.reset();
					path.moveTo(x,y);
					path.lineTo(x+radius/4,y+34);
					path.lineTo(x,y+35);
					path.lineTo(x-radius/4,y+34);
					path.close();
					c.drawPath(path, trunkPaint);
					
					path.reset();
					path.addCircle(this.x, this.y, radius, Path.Direction.CW); 
					path.close();
					c.drawPath(path, canopyPaint);
					
					for(int i = 0; i < 10; i++){
						float angle = (float)((float)i/10 * Math.PI);
						float x1 = (float)(this.x + Math.cos(angle)*this.radius);
						float y1 = (float)(this.y + Math.sin(angle)*this.radius);
	
		                path.reset();
						path.addCircle(x1,y1, this.ranPoints[i], Path.Direction.CW); 
						path.close();
						c.drawPath(path, canopyPaint);
					}
	    		}
	    	}
	    
	    }
	   
    
}