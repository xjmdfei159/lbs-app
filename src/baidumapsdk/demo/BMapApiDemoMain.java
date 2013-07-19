// by kabi 2013.7.19
package baidumapsdk.demo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.VersionInfo;


public class BMapApiDemoMain extends Activity {
	
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	//调用main视图
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        //输入用户名密码
        EditText editname = (EditText)findViewById(R.id.editName);
        EditText editpassword = (EditText)findViewById(R.id.editPassword);
         
//        editname.setOnFocusChangeListener(new View.OnFocusChangeListener()
//        {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                Toast.makeText(getBaseContext(),
//                    ((EditText) v).getId() + " has focus - " + hasFocus,
//                    Toast.LENGTH_LONG).show();
//            }
//        });
		Button button1 = (Button) findViewById(R.id.beginButton);
		button1.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
					// TODO Auto-generated method stub
					Intent intent1 = new Intent();
					intent1.setClass(BMapApiDemoMain.this, LocationOverlayDemo.class);
					startActivity(intent1);
				
			
				
			}
		});
    }


	
	@Override
	protected void onResume() {
	    DemoApplication app = (DemoApplication)this.getApplication();
		super.onResume();
	}

	@Override
	// 建议在APP整体退出之前调用MapApi的destroy()函数，不要在每个activity的OnDestroy中调用，
    // 避免MapApi重复创建初始化，提高效率
	protected void onDestroy() {
	    DemoApplication app = (DemoApplication)this.getApplication();
		if (app.mBMapManager != null) {
			app.mBMapManager.destroy();
			app.mBMapManager = null;
		}
		super.onDestroy();
		System.exit(0);
	}
	

}