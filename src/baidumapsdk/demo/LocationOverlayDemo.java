package baidumapsdk.demo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.Geometry;
import com.baidu.mapapi.map.Graphic;
import com.baidu.mapapi.map.GraphicsOverlay;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.mapapi.map.Symbol;

import com.baidu.mapapi.map.PopupClickListener;
import com.baidu.mapapi.map.PopupOverlay;
import com.baidu.platform.comapi.basestruct.GeoPoint;
/**
 * 此demo用来展示如何结合定位SDK实现定位，并使用MyLocationOverlay绘制定位位置
 * 同时展示如何使用自定义图标绘制并点击时弹出泡泡
 *
 */
public class LocationOverlayDemo extends Activity {
	
	// 定位相关
	LocationClient mLocClient;

	LocationData locData = null;
    LocationData locDataFalse = null;

	public MyLocationListenner myListener = new MyLocationListenner();


	//定位图层
	locationOverlay myLocationOverlay = null;

//	//弹出泡泡图层
	private PopupOverlay   pop  = null;//弹出泡泡图层，浏览节点时使用
	private TextView  popupText = null;//泡泡view
	private View viewCache = null;
	
	//地图相关，使用继承MapView的MyLocationMapView目的是重写touch事件实现泡泡处理
	//如果不处理touch事件，则无需继承，直接使用MapView即可
	MyLocationMapView mMapView = null;	// 地图View

	private MapController mMapController = null;


	//UI相关
	OnCheckedChangeListener radioButtonListener = null;
	Button requestLocButton = null;
	boolean isRequest = false;//是否手动触发请求定位
	boolean isFirstLoc = true;//是否首次定位
	boolean isLocationClientStop = false;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //这个是对应layout
        setContentView(R.layout.activity_locationoverlay);
        CharSequence titleLable="定位功能";
        setTitle(titleLable);
        requestLocButton = (Button)findViewById(R.id.button1);
        OnClickListener btnClickListener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//手动定位请求
				requestLocClick();
			}
		};
	    requestLocButton.setOnClickListener(btnClickListener);
        
		//地图初始化
        mMapView = (MyLocationMapView)findViewById(R.id.bmapView);
        mMapController = mMapView.getController();
        mMapView.getController().setZoom(14);
        mMapView.getController().enableClick(true);
        mMapView.setBuiltInZoomControls(true); 

      //创建 弹出泡泡图层
        createPaopao();
        
        //定位初始化
        mLocClient = new LocationClient( this ); 
        locData = new LocationData(); 
        locDataFalse = new LocationData();
        mLocClient.registerLocationListener( myListener );
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);//打开gps
        option.setCoorType("bd09ll");     //设置坐标类型
        option.setScanSpan(5000);
        mLocClient.setLocOption(option);
        mLocClient.start();
       
        //定位图层初始化
		myLocationOverlay = new locationOverlay(mMapView);
		//设置定位数据
        myLocationOverlay.setData(locDataFalse);
	    myLocationOverlay.setData(locData);

	    //添加定位图层
		mMapView.getOverlays().add(myLocationOverlay);
    //    mMapView.getOverlays().add(myLocationOverlayFalse);
		myLocationOverlay.enableCompass();
		//修改定位数据后刷新图层生效
		mMapView.refresh();
        //界面加载的时候添加图层 作用是添加新的点 
        addCustomElementsDemo();
        
		Button managebutton = (Button) findViewById(R.id.managerButton);
		managebutton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
					// TODO Auto-generated method stub
					Intent intent2 = new Intent();
					intent2.setClass(LocationOverlayDemo.this, manager.class);
					startActivity(intent2);
				
			
				
			}
		});
		
    }
    
    public void addCustomElementsDemo(){
        GraphicsOverlay graphicsOverlay = new GraphicsOverlay(mMapView);
        mMapView.getOverlays().add(graphicsOverlay);
        //添加点
        graphicsOverlay.setData(drawCircle());
       
        //执行地图刷新使生效
        mMapView.refresh();
    }
    public Graphic drawCircle(){
    	double mLat = 39.90923; 
       	double mLon = 116.447428; 
    	int lat = (int) (mLat*1E6);
	   	int lon = (int) (mLon*1E6);   	
	   	GeoPoint pt1 = new GeoPoint(lat, lon);
	   	
	   	//构建圆
  		Geometry circleGeometry = new Geometry();
  	
  		//设置圆中心点坐标和半径
  		circleGeometry.setCircle(pt1, 2500);
  		//设置样式
  		Symbol circleSymbol = new Symbol();
 		Symbol.Color circleColor = circleSymbol.new Color();
 		circleColor.red = 0;
 		circleColor.green = 255;
 		circleColor.blue = 0;
 		circleColor.alpha = 126;
  		circleSymbol.setSurface(circleColor,1,3);
  		//生成Graphic对象
  		Graphic circleGraphic = new Graphic(circleGeometry, circleSymbol);
  		return circleGraphic;
    }
    /**
     * 手动触发一次定位请求
     */
    public void requestLocClick(){
    	isRequest = true;
        mLocClient.requestLocation();
        Toast.makeText(LocationOverlayDemo.this, "正在定位…", Toast.LENGTH_SHORT).show();
    }
    /**
     * 修改位置图标
     * @param marker
     */
    public void modifyLocationOverlayIcon(Drawable marker){
    	//当传入marker为null时，使用默认图标绘制
    	myLocationOverlay.setMarker(marker);
    	//修改图层，需要刷新MapView生效
    	mMapView.refresh();
    }
    /**
	 * 创建弹出泡泡图层
	 */
	public void createPaopao(){
		viewCache = getLayoutInflater().inflate(R.layout.custom_text_view, null);
        popupText =(TextView) viewCache.findViewById(R.id.textcache);
        //泡泡点击响应回调
        PopupClickListener popListener = new PopupClickListener(){
			@Override
			public void onClickedPopup(int index) {
				Log.v("click", "clickapoapo");
			}
        };
        pop = new PopupOverlay(mMapView,popListener);
        MyLocationMapView.pop = pop;
	}
	/**
     * 定位SDK监听函数
     */
    public class MyLocationListenner implements BDLocationListener {
    	
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null || isLocationClientStop)
                return ;
            locData.latitude = location.getLatitude();
            locData.longitude = location.getLongitude();

            //如果不显示定位精度圈，将accuracy赋值为0即可
            locData.accuracy = location.getRadius();
            locData.direction = location.getDerect();

            locDataFalse.latitude = 39.945;
            locDataFalse.longitude = 116.404;
            locDataFalse.direction = 2.0f;
            //更新定位数据
            myLocationOverlay.setData(locDataFalse);
            myLocationOverlay.setData(locData);            
            //更新图层数据执行刷新后生效
            mMapView.refresh();
            //是手动触发请求或首次定位时，移动到定位点
            if (isRequest || isFirstLoc){
            	//移动地图到定位点
                 mMapController.animateTo(new GeoPoint((int)(locData.latitude* 1e6), (int)(locData.longitude *  1e6)));
                 isRequest = false;
            }
            //首次定位完成
            isFirstLoc = false;
        }
        
        public void onReceivePoi(BDLocation poiLocation) {
            if (poiLocation == null){
                return ;
            }
        }
    }

    //继承MyLocationOverlay重写dispatchTap实现点击处理
  	public class locationOverlay extends MyLocationOverlay{

  		public locationOverlay(MapView mapView) {
  			super(mapView);
  			// TODO Auto-generated constructor stub
  		}
  		
  		//可以弹出具体信息 
         @Override
  		protected boolean dispatchTap() {
  			// TODO Auto-generated method stub
  			//处理点击事件,弹出泡泡
  			popupText.setBackgroundResource(R.drawable.popup);
			popupText.setText("我想要xx类型的人");
			pop.showPopup(BMapUtil.getBitmapFromView(popupText),
					new GeoPoint((int)(locData.latitude*1e6), (int)(locData.longitude*1e6)),
					8);
  			return true;
  		}
  		
  	}

    @Override
    protected void onPause() {
    	isLocationClientStop = true;
        mMapView.onPause();
        super.onPause();
    }
    
    @Override
    protected void onResume() {
    	isLocationClientStop = false;
        mMapView.onResume();
        super.onResume();
    }
    
    @Override
    protected void onDestroy() {
    	//退出时销毁定位
        if (mLocClient != null)
            mLocClient.stop();
        isLocationClientStop = true;
        mMapView.destroy();
        super.onDestroy();
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    	mMapView.onSaveInstanceState(outState);
    	
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
    	super.onRestoreInstanceState(savedInstanceState);
    	mMapView.onRestoreInstanceState(savedInstanceState);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

}
/**
 * 继承MapView重写onTouchEvent实现泡泡处理操作
 * @author hejin
 *
 */
class MyLocationMapView extends MapView{
	static PopupOverlay   pop  = null;//弹出泡泡图层，点击图标使用
	public MyLocationMapView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	public MyLocationMapView(Context context, AttributeSet attrs){
		super(context,attrs);
	}
	public MyLocationMapView(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
	}
	@Override
    public boolean onTouchEvent(MotionEvent event){
		if (!super.onTouchEvent(event)){
			//消隐泡泡
			if (pop != null && event.getAction() == MotionEvent.ACTION_UP)
				pop.hidePop();
		}
		return true;
	}
}

