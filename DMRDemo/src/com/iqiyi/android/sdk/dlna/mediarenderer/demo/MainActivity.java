package com.iqiyi.android.sdk.dlna.mediarenderer.demo;


import java.util.Random;

import org.cybergarage.upnp.Icon;
import org.cybergarage.upnp.IconList;
import org.cybergarage.util.Debug;
import org.cybergarage.xml.Node;

import com.iqiyi.android.dlna.sdk.mediarenderer.ControlPointConnectRendererListener;
import com.iqiyi.android.dlna.sdk.mediarenderer.MediaRenderer;
import com.iqiyi.android.dlna.sdk.mediarenderer.QiyiDLNAListener;
import com.iqiyi.android.dlna.sdk.mediarenderer.QuicklySendMessageListener;
import com.iqiyi.android.dlna.sdk.mediarenderer.StandardDLNAListener;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


@SuppressLint("NewApi")
public class MainActivity extends Activity {
	
	private  MediaRenderer  mediaRenderer=null;
	
	private Handler   hander;
	
	private  Button  BtnStart;
	private  Button  BtnStop;

	private TextView  textViewMessage;
	
	/*
	 * 发送消息
	 */
	public void SendMessage(int instance,String infor,StringBuffer outResult)
	{
		/*
		 * 回复的消息内容直接填充在outResult即可，这里是给DMC回复 hahaha的内容
		 */
		Random rand = new Random();
		outResult.append("hahaha:"+rand.nextInt());
	}
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
	    StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());
	    
	    textViewMessage=(TextView)findViewById(R.id.textViewMessage);
	    
	    BtnStart=(Button)findViewById(R.id.BtnStart);
	    BtnStop=(Button)findViewById(R.id.BtnStop);
	    
	    BtnStart.setOnClickListener(new Button.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(mediaRenderer!=null)
				{
					mediaRenderer.start();
					Toast.makeText(MainActivity.this,"开启成功", Toast.LENGTH_SHORT);
				}
			}
		});
	    
	    BtnStop.setOnClickListener(new Button.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(mediaRenderer!=null)
				{
					mediaRenderer.stop();
					Toast.makeText(MainActivity.this,"关闭成功", Toast.LENGTH_SHORT);
				}
			}
		});
	    		
		hander=new Handler()
		{
			@Override
	        public void handleMessage(Message msg) {
	            // TODO Auto-generated method stub
				switch(msg.what)
				{
				case  1100:
					textViewMessage.setText((String)msg.obj);
					break;
				}
	        }
		};
	    
	    
		Debug.on();
		mediaRenderer = new MediaRenderer(1,0);
		//设置奇艺盒子的基本信息，必须填写，若没有填写，生成的默认值，都将是qiyi的信息
		
		mediaRenderer.setFriendlyName("IQIYI_TV_(24:CF:21:C0:00:19)");
		mediaRenderer.setManufacture("iqiyi");
		mediaRenderer.setManufactureURL("http://www.iqiyi.com");
		mediaRenderer.setModelDescription("QiYi AV Media Renderer Device");
		mediaRenderer.setModelName("IQIYI AV Media Renderer Device");
		mediaRenderer.setModelNumber("1234");
		mediaRenderer.setModelURL("http://www.iqiyi.com/qiyi");
		/*
		 * 设置icon列表
		 */
		Icon icon =new Icon();
		icon.setMimeType("image/jpg");
		icon.setDepth("8");
		icon.setHeight(24);
		icon.setWidth(24);
		icon.setURL("http://www.iqiyi.com/icon_24.jpg");
		IconList iconList = new IconList();
		iconList.add(icon);
		mediaRenderer.setIconList(iconList);
		
		mediaRenderer.setQiyiDLNAListener(new QiyiDLNAListener() {
			
			@Override
			public void onReceiveSendMessage(int instance, String infor,
					StringBuffer outResult) {
				// TODO Auto-generated method stub
				SendMessage(instance,infor,outResult);
			}
		});
		
		mediaRenderer.setStandardDLNAListener(new StandardDLNAListener() {
			
			@Override
			public void Next(int InstanceID) {
				// TODO Auto-generated method stub
				Debug.message("++++++++++++++++Next:"+InstanceID);
			}

			@Override
			public void Pause(int InstanceID) {
				// TODO Auto-generated method stub
				Debug.message("++++++++++++++++Pause:"+InstanceID);
			}

			@Override
			public void Play(int InstanceID, String Speed) {
				// TODO Auto-generated method stub
				Debug.message("++++++++++++++++Play:"+InstanceID+" Speed:"+Speed);
			}

			@Override
			public void Previous(int InstanceID) {
				// TODO Auto-generated method stub
				Debug.message("++++++++++++++++Previous:"+InstanceID);
			}

			@Override
			public void Stop(int InstanceID) {
				// TODO Auto-generated method stub
				Debug.message("++++++++++++++++Stop:"+InstanceID);
			}

			@Override
			public void GetMute(int InstanceID, String Channel,
					Boolean outCurrentMute) {
				// TODO Auto-generated method stub
				if(outCurrentMute==true)
				{
					outCurrentMute=false;
				}
				Debug.message("++++++++++++++++GetMute:"+InstanceID);
			}

			@Override
			public void GetVolume(int InstanceID, String Channel,
					Integer outCurrentVolume) {
				// TODO Auto-generated method stub
				outCurrentVolume+=10;
				outCurrentVolume%=100;
				Debug.message("++++++++++++++++GetVolume:"+InstanceID+" outCurrentVolume:"+outCurrentVolume);
			}

			@Override
			public void SetMute(int InstanceID, String Channel,
					boolean DesireMute) {
				// TODO Auto-generated method stub
				Debug.message("++++++++++++++++SetMute:"+InstanceID+" DesireMute:"+DesireMute);
			}

			@Override
			public void SetVolume(int InstanceID, String Channel,
					int DesireVolume) {
				// TODO Auto-generated method stub
				Debug.message("++++++++++++++++SetVolume:"+InstanceID+" DesireVolume:"+DesireVolume);
			}

			@Override
			public void SetPlayMode(int InstanceID, String NewPlayMode) {
				// TODO Auto-generated method stub
				Debug.message("++++++++++++++++SetPlayMode:"+InstanceID+" NewPlayMode:"+NewPlayMode);
			}

			@Override
			public void SetAVTransportURI(int instanceID, String currentURI,
					AVTransportURIMetaData currentURIMetaData) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void SetNextAVTransportURI(int instanceID, String nextURI,
					AVTransportURIMetaData nextAVTransportURIMetaData) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void Seek(int instanceID, SEEKMODE unit, String target) {
				// TODO Auto-generated method stub
				
			}
		});
		
		/*
		 * 监听快速通道的数据
		 */
		mediaRenderer.setQuicklySend(true);
		mediaRenderer.setQuicklySendMessageListener(new QuicklySendMessageListener() {
			
			@Override
			public void onQuicklySendMessageRecieved(byte data) {
				// TODO Auto-generated method stub
				Debug.message("data is:"+data);
			}
		});
		
		/*
		 * 是否有设备的连接的回调函数
		 */
		mediaRenderer.setControlPointConnectRendererListener(new ControlPointConnectRendererListener() {
			
			@Override
			public void onReceiveDeviceConnect(boolean isConnect) {
				// TODO Auto-generated method stub
				if(isConnect==true)
				{
					Debug.message("有设备连接..........");
				}else
				{
					Debug.message("设备都断开了.........");
				}
			}
		});
		
		mediaRenderer.initialize();
		mediaRenderer.start();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

}
