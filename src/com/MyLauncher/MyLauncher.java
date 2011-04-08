package com.MyLauncher;

import java.util.Collections;
import java.util.List;

import javax.microedition.khronos.opengles.GL;

import se.jayway.opengl.tutorial.mesh.MatrixTrackingGL;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class MyLauncher extends Activity implements OnItemClickListener {

	private GridView mGridView;
	private Context mContext;
	private PackageManager mPackageManager;
	private List<ResolveInfo> mAllApps;

//	public int getGLVersion() 
//	{
//	    ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
//	    ConfigurationInfo info = am.getDeviceConfigurationInfo();
//	    return info.reqGlEsVersion;
//	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		System.out.println("My Starting Android launcher!");
		
		//setContentView(R.layout.main);
		
		//System.out.println( "GLVersion:" + getGLVersion() );
		
		MyGLSurfaceView mGLSurfaceView = new MyGLSurfaceView( this );
        mGLSurfaceView.setGLWrapper(new GLSurfaceView.GLWrapper() {
            public GL wrap(GL gl) {
                return new MatrixTrackingGL(gl);
            }});
		setContentView( mGLSurfaceView );
		
		//setupViews();
		System.out.println("My Ending of Starting Android launcher!");

	}
	
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // Close the menu
        if (Intent.ACTION_MAIN.equals(intent.getAction())) {
            getWindow().closeAllPanels();
        }
    }

//	boolean mWallpaperChecked = false;
//    /**
//     * When no wallpaper was manually set, a default wallpaper is used instead.
//     */
//    private void setDefaultWallpaper() {
//        if (!mWallpaperChecked) {
//            Drawable wallpaper = peekWallpaper();
//            if (wallpaper == null) {
//                try {
//                    clearWallpaper();
//                } catch (IOException e) {
//                    System.out.println( "Failed to clear wallpaper " + e );
//                }
//            } else {
//                getWindow().setBackgroundDrawable(new ClippedDrawable(wallpaper));
//            }
//            mWallpaperChecked = true;
//        }
//    }
	
	public void setupViews() {
		mContext = MyLauncher.this;
		mPackageManager = getPackageManager();
		mGridView = (GridView) findViewById(R.id.allapps);
		bindAllApps();

		mGridView.setAdapter(new GridItemAdapter(mContext, mAllApps));
		mGridView.setNumColumns(4);
		mGridView.setOnItemClickListener(this);
	}

	public void bindAllApps() {
		// 这里是关键哦，我们平时写的应用总有一个activity申明成这两个属性
		// 也就是应用的入口
		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		// 符合上面条件的全部查出来,并且排序
		mAllApps = mPackageManager.queryIntentActivities(mainIntent, 0);
		Collections.sort(mAllApps, new ResolveInfo.DisplayNameComparator(
				mPackageManager));
	}

	// gridview点击事件，点击进入相关应用
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		ResolveInfo res = mAllApps.get(position);
		// 该应用的包名和主Activity
		String pkg = res.activityInfo.packageName;
		String cls = res.activityInfo.name;

		ComponentName componet = new ComponentName(pkg, cls);

		Intent i = new Intent();
		i.setComponent(componet);
		startActivity(i);
	}

	// 不明白BaseAdapter的用法 我高手进阶里有
	private class GridItemAdapter extends BaseAdapter {
		private Context context;
		private List<ResolveInfo> resInfo;

		// 构造函数
		public GridItemAdapter(Context c, List<ResolveInfo> res) {
			context = c;
			resInfo = res;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return resInfo.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			// 不明白LayoutInflater的我android高手进阶里有
			convertView = LayoutInflater.from(context).inflate(
					R.layout.application_layout, null);

			ImageView app_icon = (ImageView) convertView
					.findViewById(R.id.app_icon);
			TextView app_tilte = (TextView) convertView
					.findViewById(R.id.app_title);

			ResolveInfo res = resInfo.get(position);
			app_icon.setImageDrawable(res.loadIcon(mPackageManager));
			app_tilte.setText(res.loadLabel(mPackageManager).toString());
			return convertView;
		}

	}
}