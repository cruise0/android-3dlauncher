package com.MyLauncher;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import se.jayway.opengl.tutorial.mesh.Group;
import se.jayway.opengl.tutorial.mesh.Mesh;
import se.jayway.opengl.tutorial.mesh.SimplePlane;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;

class RotateGroup extends Group
{
	private float mRotateYAngel = 0.0f;
	
	static final public int maxOneRow = 10;
	
	public void rotate( float rotAngel , float radius )
	{
		mRotateYAngel += rotAngel;
		
		int totalCount = mChildren.size();
		
		int oneRow = Math.min( totalCount , maxOneRow ); 
		
		for( int idx = 0 ; idx < totalCount ; idx++ )
		{
			Mesh mesh = mChildren.elementAt( idx );			
			
			float angel = (float) ( (idx % oneRow ) * Math.PI * 2.0f / oneRow);
			mesh.x = (float) (radius * Math.cos(mRotateYAngel + angel ));
			mesh.y = idx * 0.1f;
			mesh.z = (float) (radius * Math.sin(mRotateYAngel + angel ));
		}
	}
}

class AppPressedHandler implements PressedHandler
{
	private ActivityInfo mActivityInfo;
	private Context mContext;
	
	public AppPressedHandler( Context activity , ActivityInfo activityInfo )
	{
		mActivityInfo = activityInfo;
		mContext = activity;
	}
	@Override
	public void Pressed(float scrX, float scrY) {
		// TODO Auto-generated method stub
		System.out.println("Pressed:" + mActivityInfo.name );
		
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        
        intent.setComponent( new ComponentName(
        		mActivityInfo.packageName,
        		mActivityInfo.name) );
        
        intent.setFlags(  Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        
		mContext.startActivity( intent );
		
	}
	
}

public class MyGLSurfaceView extends GLSurfaceView implements
		View.OnTouchListener, OnGestureListener {

	private class MyGLRenderView implements Renderer {
		private final Group root;
		
		private Projector mProjector = new Projector();

		private float fps = 0.0f;
		private long lastMiliSec = -1;

		public MyGLRenderView() {
			// Initialize our root.
			Group group = new Group();
			root = group;
		}

		public float getFPS() {
			return fps;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.opengl.GLSurfaceView.Renderer#onSurfaceCreated(javax.microedition
		 * .khronos.opengles.GL10, javax.microedition.khronos.egl.EGLConfig)
		 */
		public void onSurfaceCreated(GL10 gl, EGLConfig config) {
			// Set the background color to black ( rgba ).
			gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);
			// Enable Smooth Shading, default not really needed.
			gl.glShadeModel(GL10.GL_SMOOTH);
			// Depth buffer setup.
			gl.glClearDepthf(1.0f);
			// Enables depth testing.
			gl.glEnable(GL10.GL_DEPTH_TEST);
			// The type of depth testing to do.
			gl.glDepthFunc(GL10.GL_LEQUAL);
			// Really nice perspective calculations.
			gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.opengl.GLSurfaceView.Renderer#onDrawFrame(javax.microedition.
		 * khronos.opengles.GL10)
		 */
		public synchronized void onDrawFrame(GL10 gl) {
			
			boolean checkPress = bCheckPress;
			
			// Clears the screen and depth buffer.
			gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
			gl.glMatrixMode( GL10.GL_MODELVIEW );
			// Replace the current matrix with the identity matrix
			gl.glLoadIdentity();
			// Translates 4 units into the screen.
			gl.glTranslatef(0, 0, -4);
			
			// Draw our scene.
			root.draw(gl , mProjector , checkPress , this.mScrX , this.mScrY );

			if( bCheckPress )
			{
				bCheckPress = false;
			}
			
			long nowMiliSec = System.currentTimeMillis();
			if (lastMiliSec < 0)
				lastMiliSec = nowMiliSec;
			else {
				fps = (float) (1000.0d / (double) (nowMiliSec - lastMiliSec));
				lastMiliSec = nowMiliSec;
			}
			
			
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.opengl.GLSurfaceView.Renderer#onSurfaceChanged(javax.microedition
		 * .khronos.opengles.GL10, int, int)
		 */
		public void onSurfaceChanged(GL10 gl, int width, int height) {
			
			// Sets the current view port to the new size.
			gl.glViewport(0, 0, width, height);
			mProjector.setCurrentView( 0 , 0 , width, height);
			
			// Select the projection matrix
			gl.glMatrixMode(GL10.GL_PROJECTION);
			// Reset the projection matrix
			gl.glLoadIdentity();
			// Calculate the aspect ratio of the window
			GLU.gluPerspective(gl, 45.0f, (float) width / (float) height, 0.1f,
					1000.0f);
			// Select the modelview matrix
			gl.glMatrixMode(GL10.GL_MODELVIEW);
			// Reset the modelview matrix
			gl.glLoadIdentity();
			
			mProjector.getCurrentProjection(gl);
		}

		/**
		 * Adds a mesh to the root.
		 * 
		 * @param mesh
		 *            the mesh to add.
		 */
		public void addMesh(Mesh mesh) {
			root.add(mesh);
		}

		boolean bCheckPress = false;
		boolean bSupportPress = false;
		
		float mScrX = 0.0f;
		float mScrY = 0.0f;
		public synchronized void press(float scrX, float scrY) {
			// TODO Auto-generated method stub
			bCheckPress = true;
			mScrX = scrX;
			mScrY = scrY;
			
		}

	}

	private void addFPSWindow(final MyGLRenderView render) {
		// Create a new plane.
		final SimplePlane plane = new SimplePlane(1.2f, 0.15f);

		// Move and rotate the plane.
		plane.x = -0.9f;
		plane.y = -1.5f;

		// Bitmap bmp = Bitmap.createBitmap(BitmapFactory.decodeResource(
		// getResources(), R.drawable.jay));
		Bitmap bmp = Bitmap.createBitmap( 256, 32, Bitmap.Config.ARGB_8888);

		bmp.eraseColor(Color.WHITE);
		Canvas canv = new Canvas(bmp);
		Paint pt = new Paint();
		pt.setColor(Color.BLUE);
		canv.drawText("Hello you fool", 0, bmp.getHeight(), pt);

		// Load the texture.
		plane.loadBitmap(bmp);

		// Add the plane to the renderer.
		render.addMesh(plane);
		System.out.println("End of MyGLSurfaceView started ");

		final Timer t = new Timer();
		t.schedule(new TimerTask() {

			private DecimalFormat df = new DecimalFormat("#.00");

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Bitmap bmp = plane.getBitmap();
				bmp.eraseColor(Color.WHITE);
				Canvas canv = new Canvas(bmp);
				Paint pt = new Paint();
				Typeface font = Typeface.create("Tahoma", Typeface.NORMAL);
				pt.setTypeface(font);
				pt.setColor(Color.BLUE);
				pt.setTextSize(30);

				canv.drawText("         FPS:" + df.format(render.getFPS()), 0,
						bmp.getHeight(), pt);
				plane.loadBitmap(bmp);
			}

		}, 0, 100);
	}

	private List<ResolveInfo> mAllApps;
	private PackageManager mPackageManager;
	private WallpaperManager mWallpaperManager;
	private final RotateGroup iconGroup = new RotateGroup();
	
	private float radius = 2.0f;
	
	private void addAppLogos(Context con, MyGLRenderView render) {
		// 这里是关键哦，我们平时写的应用总有一个activity申明成这两个属性
		// 也就是应用的入口
		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		// 符合上面条件的全部查出来,并且排序

		mAllApps = mPackageManager.queryIntentActivities(mainIntent, 0);
		Collections.sort(mAllApps, new ResolveInfo.DisplayNameComparator(
				mPackageManager));
		
		int totalLogoCount = mAllApps.size();
		int oneRow = Math.min( totalLogoCount , RotateGroup.maxOneRow ); 
		
		for (int idx = 0; idx < totalLogoCount ; idx++) {

			ResolveInfo rInfo = mAllApps.get(idx);
			Drawable icon = rInfo.loadIcon(mPackageManager);
			int width = icon.getMinimumWidth();
			int height = icon.getMinimumWidth();

			icon.setBounds(0, 0, width, height);

			CharSequence csLabel = rInfo.loadLabel(mPackageManager);

			Bitmap bmp = Bitmap.createBitmap( 64 , 64,
					Bitmap.Config.ARGB_8888);
			bmp.eraseColor(Color.TRANSPARENT);
			Canvas canv = new Canvas(bmp);
			icon.draw(canv);

			Paint pt = new Paint();
			Typeface font = Typeface.create("Tahoma", Typeface.NORMAL);
			pt.setTypeface(font);
			pt.setColor(Color.WHITE);
			pt.setTextSize(10);

			canv.drawText((String) csLabel, 0, bmp.getHeight(), pt);

			SimplePlane plane = new SimplePlane( 64 / 100.0f,
					64 / 100.0f);

			plane.bCulFace = true;
			plane.loadBitmap(bmp);

			// int idxRow = idx / colCount;
			// int idxCol = idx % colCount;

			// Move and rotate the plane.
			// plane.x = (float) idxCol * (float) 64.0f / 100.0f;
			// plane.y = -(float) idxRow * (float) 94.0f / 100.0f;
			// plane.ry = idx * 360.0f / 19.0f ;

			float angel = (float) ( (idx % oneRow ) * Math.PI * 2.0f / oneRow);
			plane.x = (float) (radius * Math.cos(angel));
			plane.y = idx * 0.1f;
			plane.z = (float) (radius * Math.sin(angel));

			plane.setPressedHandler( new AppPressedHandler( this.getContext() , rInfo.activityInfo ));
			iconGroup.add(plane);
		}

		iconGroup.x = 0.0f;
		iconGroup.y = -0.2f;
		iconGroup.z -= 3.5f;

		render.addMesh(iconGroup);
	}

	final MyGLRenderView render = new MyGLRenderView();
	
	public MyGLSurfaceView(Context context) {
		super(context);

		mPackageManager = context.getPackageManager();
		mWallpaperManager = WallpaperManager.getInstance( context );

		// TODO Auto-generated constructor stub
		System.out.println("MyGLSurfaceView started ");
		this.setRenderer(render);

		addAppLogos(context, render);

		addFPSWindow(render);
		
		SimplePlane plane = new SimplePlane( 4.0f,
				4.0f );
		Bitmap bmp = Bitmap.createBitmap( 128 , 128 ,
				Bitmap.Config.ARGB_8888);
		bmp.eraseColor(Color.TRANSPARENT);
		Canvas canv = new Canvas(bmp);
		
		Drawable wPaper = mWallpaperManager.getDrawable();
		wPaper.setBounds( 0 , 0 , bmp.getWidth() , bmp.getHeight() );
		wPaper.draw( canv );
		
		plane.loadBitmap( bmp );
		
		plane.rx = -90.0f;
		plane.y = -0.6f;
		
		render.addMesh( plane );

		this.setOnTouchListener(this);
		this.setLongClickable(true);

		mGestureDetector = new GestureDetector(this);
	}

	private GestureDetector mGestureDetector;

	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		// TODO Auto-generated method stub
		return mGestureDetector.onTouchEvent(arg1);
	}

	@Override
	public boolean onDown(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onFling(MotionEvent arg0, MotionEvent arg1, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
 
		return false;
	}

	@Override
	public void onLongPress(MotionEvent event) {
		// TODO Auto-generated method stub

		float scrX = event.getX();
		float scrY = event.getY();
		
		render.press( scrX , scrY );
	}

	@Override
	public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		
		iconGroup.rotate( distanceX / 100.0f , radius );
		
		iconGroup.y += distanceY / 100.0f;
		
		return false;
	}

	@Override
	public void onShowPress(MotionEvent arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean onSingleTapUp(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}

};