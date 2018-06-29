package com.iqiyi.android.sdk.dlan.mediacontroller.demo;

import java.util.ArrayList;
import java.util.List;

import org.cybergarage.upnp.Device;
import org.cybergarage.upnp.DeviceList;
import org.cybergarage.upnp.Icon;
import org.cybergarage.upnp.IconList;
import org.cybergarage.upnp.device.DeviceChangeListener;
import org.cybergarage.util.Debug;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.iqiyi.android.dlna.sdk.controlpoint.DeviceType;
import com.iqiyi.android.dlna.sdk.controlpoint.MediaControlPoint;
import com.iqiyi.android.dlna.sdk.controlpoint.NotifyMessageListener;

@SuppressLint("NewApi")
public class MainActivity extends Activity implements OnGestureListener {

    private final static String debugTag = "iqiyidlna";

    private MediaControlPoint mediaController;

    private ScreenActionReceiver screenActionReceiver = null;

    private ListView TextViewDeviceList;
    private Handler hander;

    private ArrayAdapter adapter = null;

    private Button BtnStart;
    private Button BtnStop;
    private Button BtnPushVideo;

    private Button BtnSearchDevice;
    private Button BtnGoBack;
    private Button BtnMenu;

    private Button BtnStartTest;
    private Button BtnStopTest;
    private Button BtnSendTest;// 自动化测试消息发送效率

    private int TestCount = 0;// 自动化测试的次数
    private static int MAX_TEST_COUNT = 500;// 最多测试500次
    private int deviceCount = 0;// 发现到的设备数
    private boolean isStartTest = false;
    private boolean isStart = true;

    private static int MAX_SEND_COUNT = 100;

    private Handler autoTestHandler;

    private GestureDetector gestureScanner;

    private Button button1;
    private Button button2;
    private Button button3;
    private Button button4;
    private Button button5;
    private Button button6;
    private Button button7;
    private Button button8;
    private Button button9;
    private Button button10;

    // 目标设备，click时更改
    private Device connectingDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());

        TextViewDeviceList = (ListView) findViewById(R.id.textViewDeviceList);

        BtnSearchDevice = (Button) findViewById(R.id.buttonSearchDevice);

        BtnStart = (Button) findViewById(R.id.BtnStart);
        BtnStop = (Button) findViewById(R.id.BtnStop);

        BtnGoBack = (Button) findViewById(R.id.BtnGoBack);
        BtnMenu = (Button) findViewById(R.id.BtnMenu);

        BtnStartTest = (Button) findViewById(R.id.BtnAutoStartTest);
        BtnStopTest = (Button) findViewById(R.id.BtnStopTest);

        BtnSendTest = (Button) findViewById(R.id.BtnSendTest);//与其他的自动化测试互斥使用

        BtnPushVideo = (Button) findViewById(R.id.BtnPushVideo);

        if (isStart) {
            BtnStart.setEnabled(false);
        }

        if (isStartTest == false) {
            BtnStartTest.setEnabled(true);
            BtnStopTest.setEnabled(false);

        } else {
            BtnStartTest.setEnabled(false);
            BtnStopTest.setEnabled(true);
        }

        autoTestHandler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                if (isStartTest == true) {
                    if (TestCount < MAX_TEST_COUNT)//少于总数
                    {
                        if (isStart == false) {
                            if (mediaController != null) {
                                isStart = true;
                                mediaController.start();
                                TestCount++;
                                Message msg = new Message();
                                msg.what = 1200;
                                msg.obj = TestCount;
                                hander.sendMessage(msg);
                            }
                        } else {
                            if (mediaController != null) {
                                isStart = false;
                                mediaController.stop();
                                Message msg = new Message();
                                msg.what = 1201;
                                msg.obj = TestCount;
                                hander.sendMessage(msg);
                            }
                        }
                    }
                } else//停止测试
                {

                }
                //要做的事情
                autoTestHandler.postDelayed(this, 10000);
            }
        };

        autoTestHandler.postDelayed(runnable, 10000);

		/*
         * 开始自动化测试
		 */
        BtnStartTest.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                if (isStartTest == false) {
                    Toast.makeText(MainActivity.this, "开始进行自动化测试", Toast.LENGTH_LONG).show();
                    isStartTest = true;
                    BtnStartTest.setEnabled(false);
                    BtnStopTest.setEnabled(true);
                    BtnSendTest.setEnabled(false);
                }
            }

        });

        // test 推片的测试数据
        final  String pushContent = "{\"control\":\"pushvideo\",\"type\":\"control\",\"value\":{\"AudioDataPort\":0,\"ControlPort\":0,\"TimingPort\":0,\"aid\":\"205479801\",\"app_remote\":false,\"cec\":false,\"channel_id\":\"2\",\"danmaku\":true,\"fc\":\"recommend\",\"history\":\"20000\",\"key\":\"e4:90:7e:12:0b:eb\",\"platform\":\"02023241030000000000\",\"seek_control\":false,\"session\":\"-1661173689\",\"show_vipqr\":false,\"source\":\"tvguoapp\",\"title\":\".........44...\",\"tvid\":\"781475100\",\"vol_control\":false,\"weburl\":\"http://m.iqiyi.com/v_19rr94erg0.html\"},\"version\":\"reserved\"}";
        BtnPushVideo.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                if (mediaController != null) {
                    if (connectingDevice != null) {
                        mediaController.sendMessage(pushContent, true, connectingDevice);
                    }
                }
            }

        });

		/*
         * 停止自动化测试
		 */
        BtnStopTest.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (isStartTest == true) {
                    Toast.makeText(MainActivity.this, "停止自动化测试", Toast.LENGTH_LONG).show();
                    isStartTest = false;
                    BtnStartTest.setEnabled(true);
                    BtnStopTest.setEnabled(false);
                    BtnSendTest.setEnabled(true);
                }
            }

        });

        BtnSendTest.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Toast.makeText(MainActivity.this, "开始自动化发送消息测试，按钮先置为不可用!", Toast.LENGTH_LONG).show();
                BtnSendTest.setEnabled(false);
                if (mediaController != null) {
                    new Thread(new Runnable() {
                        public void run() {

                            //发送消息100个
                            for (int i = 0; i < MAX_SEND_COUNT; i++) {
                                if (i % 6 == 0) {
                                    if (connectingDevice != null) {
                                        mediaController.sendMessage(SingleByteCode.LEFT, connectingDevice);
                                    }

                                } else if (i % 6 == 1) {
                                    if (connectingDevice != null) {
                                        mediaController.sendMessage(SingleByteCode.RIGHT, connectingDevice);
                                    }

                                } else if (i % 6 == 2) {
                                    if (connectingDevice != null) {
                                        mediaController.sendMessage(SingleByteCode.TOP, connectingDevice);
                                    }

                                } else if (i % 6 == 3) {
                                    if (connectingDevice != null) {
                                        mediaController.sendMessage(SingleByteCode.BOTTOM, connectingDevice);
                                    }

                                } else if (i % 6 == 4) {
                                    if (connectingDevice != null) {
                                        mediaController.sendMessage(SingleByteCode.CLICK, connectingDevice);
                                    }

                                } else if (i % 6 == 5) {
                                    if (connectingDevice != null) {
                                        mediaController.sendMessage(SingleByteCode.BACK, connectingDevice);
                                    }

                                }
                                try {
                                    Thread.sleep(2000);
                                } catch (InterruptedException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                                Message msg = new Message();
                                msg.obj = i;
                                msg.what = 1401;
                                hander.sendMessage(msg);
                            }

                            Message msg = new Message();
                            msg.what = 1400;
                            hander.sendMessage(msg);

                        }
                    }).start();
                }

            }
        });

        ////////////////////////////////////////////////////////////////////

        BtnStart.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                if (isStart == false) {
                    if (mediaController.start()) {
                        Debug.message("DMC开启");
                        BtnStart.setEnabled(false);
                        BtnStop.setEnabled(true);
                        isStart = true;
                    }

                }
            }

        });

        BtnStop.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                if (isStart == true) {
                    if (mediaController.stop()) {
                        Debug.message("DMC关闭成功");
                        BtnStart.setEnabled(true);
                        BtnStop.setEnabled(false);
                        isStart = false;
                    }

                }
            }

        });

        BtnGoBack.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub

                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        if (connectingDevice != null && mediaController.sendMessage(SingleByteCode.BACK, connectingDevice) == false) {
                            Message msg = new Message();
                            msg.what = 1008;
                            hander.sendMessage(msg);
                        }
                    }
                }).start();

            }

        });

        BtnMenu.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        if (connectingDevice != null && !mediaController.sendMessage(SingleByteCode.MENU, connectingDevice)) {
                            Message msg = new Message();
                            msg.what = 1009;
                            hander.sendMessage(msg);
                        }
                    }
                }).start();

            }

        });


        TextViewDeviceList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                //
                if (mediaController.getDeviceList() != null) {
                    if (mediaController.getDeviceList().getDevice(arg2) != null) {
                        connectingDevice = mediaController.getDeviceList().getDevice(arg2);
                    }
                }
            }

        });

        screenActionReceiver = new ScreenActionReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                String action = intent.getAction();
                if (action.equals(Intent.ACTION_SCREEN_ON)) {
                    Debug.message("screen_on");
                    mediaController.NotifyDmcSleep(false);
                } else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                    Debug.message("screen_of");
                    mediaController.NotifyDmcSleep(true);
                }
            }
        };
        screenActionReceiver.registerScreenActionReceiver(this);


        hander = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                switch (msg.what) {
                    case 1001:
                        DeviceList mylist = (DeviceList) msg.obj;
                        List<String> data = new ArrayList<String>();
                        for (int i = 0; i < mylist.size(); i++) {
                            data.add(mylist.getDevice(i).getFriendlyName());
                        }
                        if (adapter == null) {
                            adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_expandable_list_item_1, data);
                        } else {
                            adapter.clear();
                            adapter.notifyDataSetChanged();
                            TextViewDeviceList.setAdapter(adapter);
                            adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_expandable_list_item_1, data);
                        }
                        //适配器
                        if (adapter != null) {
                            TextViewDeviceList.setAdapter(adapter);
                        }

                        if (isStartTest == true) {
                            deviceCount++;
                            Toast.makeText(MainActivity.this, "共测试了" + TestCount + "次，发现到设备" + deviceCount + "个", Toast.LENGTH_LONG).show();
                        }

                        break;
                    case 1002:
                        Toast.makeText(MainActivity.this, "接收到DMR消息:" + (String) msg.obj, Toast.LENGTH_LONG).show();
                        break;
                    case 1003:
                        Toast.makeText(MainActivity.this, "发送失败", Toast.LENGTH_SHORT).show();
                        break;
                    case 1004://左边
                        MainActivity.this.setTitle(System.currentTimeMillis() + " 左边");
                        break;
                    case 1005://右边
                        MainActivity.this.setTitle(System.currentTimeMillis() + " 右边");
                        break;
                    case 1006:
                        MainActivity.this.setTitle(System.currentTimeMillis() + " 上边");
                        break;//上边
                    case 1007:
                        MainActivity.this.setTitle(System.currentTimeMillis() + " 下边");
                        break;//下边
                    case 1008:
                        MainActivity.this.setTitle(System.currentTimeMillis() + " 返回");
                        break;
                    case 1009:
                        MainActivity.this.setTitle(System.currentTimeMillis() + " 菜单");
                        break;
                    case 1200:
                        Toast.makeText(MainActivity.this, "开始第" + msg.obj + "次测试   开启成功!", Toast.LENGTH_SHORT).show();
                        break;
                    case 1201:
                        Toast.makeText(MainActivity.this, "第" + msg.obj + "次测试结束 !", Toast.LENGTH_SHORT).show();
                        if (TextViewDeviceList != null) {
                            TextViewDeviceList.setAdapter(null);
                        }
                        break;
                    case 1400:
                        Toast.makeText(MainActivity.this, "自动发送消息该轮测试完毕！！！", Toast.LENGTH_SHORT).show();
                        BtnSendTest.setEnabled(true);
                    case 1401:
                        Toast.makeText(MainActivity.this, "发送第" + msg.obj + "次消息", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };

        BtnSearchDevice.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                mediaController.search();
            }

        });


        gestureScanner = new GestureDetector(this);
        gestureScanner.setIsLongpressEnabled(true);

        Debug.on();

        mediaController = new MediaControlPoint();
        /*
		 * 设置要发现的设备类型，这里统一这么写，可以加快设备发现过程
		 */
        mediaController.setFindDeviceType(DeviceType.MEDIA_QIYI);
        mediaController.setMaxDelayTolerateTime(5000);//最大延时时间是5000ms
        mediaController.setOpenRealTimeFunction(true);//开启实时体验功能

        mediaController.addDeviceChangeListener(new DeviceChangeListener() {

            @Override
            public void deviceRemoved(Device dev) {
                // TODO Auto-generated method stub
                Debug.message("fan", "deviceRemoved.." + dev.getFriendlyName());
                Log.d(debugTag, "被删除的设备名是:" + dev.getFriendlyName() + " 设备的个数是:" + mediaController.getDeviceList().size());
                Message msg = new Message();
                msg.what = 1001;
                msg.obj = mediaController.getDeviceList();
                hander.sendMessage(msg);
            }

            @Override
            public void deviceAdded(Device dev) {
                // TODO Auto-generated method stub
                Debug.message("fan", "deviceAdded.." + dev.getFriendlyName());
                Log.d(debugTag, "增加的设备名是:" + dev.getFriendlyName() + " 设备的个数是:" + mediaController.getDeviceList().size());
                IconList iconList = dev.getIconList();
                if (iconList != null && iconList.size() > 0) {
                    Icon icon = iconList.getIcon(0);
                    Log.d(debugTag, "icon url:" + icon.getURL());
                }
                Message msg = new Message();
                msg.what = 1001;
                msg.obj = mediaController.getDeviceList();
                hander.sendMessage(msg);
            }

            @Override
            public void deviceUpdated(Device dev) {
                // TODO Auto-generated method stub

            }

        });

        mediaController.setReceiveNotifyMessageListener(new NotifyMessageListener() {

            @Override
            public void onReceiveMessage(Device dev, String msg) {
                // TODO Auto-generated method stub
                Debug.message("fan", "notify receive.." + msg);
                Message message = new Message();
                message.what = 1002;
                message.obj = msg;
                hander.sendMessage(message);
            }
        });

        mediaController.start();
        //mediaController.search();


        button1 = (Button) findViewById(R.id.button1);
        button1.setOnClickListener(searchClickListener);

        button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(searchClickListener);

        button3 = (Button) findViewById(R.id.button3);
        button3.setOnClickListener(searchClickListener);

        button4 = (Button) findViewById(R.id.button4);
        button4.setOnClickListener(searchClickListener);

        button5 = (Button) findViewById(R.id.button5);
        button5.setOnClickListener(searchClickListener);

        button6 = (Button) findViewById(R.id.button6);
        button6.setOnClickListener(searchClickListener);

        button7 = (Button) findViewById(R.id.button7);
        button7.setOnClickListener(searchClickListener);

        button8 = (Button) findViewById(R.id.button8);
        button8.setOnClickListener(searchClickListener);

        button9 = (Button) findViewById(R.id.button9);
        button9.setOnClickListener(searchClickListener);

        button10 = (Button) findViewById(R.id.button10);
        button10.setOnClickListener(searchClickListener);
    }

    public OnClickListener searchClickListener = new Button.OnClickListener() {

        @Override
        public void onClick(View arg0) {
            // TODO Auto-generated method stub
            if (arg0.getId() == R.id.button1) {
                mediaController.search();
            } else if (arg0.getId() == R.id.button2) {
                for (int i = 1; i <= 2; i++) {
                    mediaController.search();
                }
            } else if (arg0.getId() == R.id.button3) {
                for (int i = 1; i <= 3; i++) {
                    mediaController.search();
                }
            } else if (arg0.getId() == R.id.button4) {
                for (int i = 1; i <= 4; i++) {
                    mediaController.search();
                }
            } else if (arg0.getId() == R.id.button5) {
                for (int i = 1; i <= 5; i++) {
                    mediaController.search();
                }
            } else if (arg0.getId() == R.id.button6) {
                for (int i = 1; i <= 6; i++) {
                    mediaController.search();
                }
            } else if (arg0.getId() == R.id.button7) {
                for (int i = 1; i <= 7; i++) {
                    mediaController.search();
                }
            } else if (arg0.getId() == R.id.button8) {
                for (int i = 1; i <= 8; i++) {
                    mediaController.search();
                }
            } else if (arg0.getId() == R.id.button9) {
                for (int i = 1; i <= 9; i++) {
                    mediaController.search();
                }
            } else if (arg0.getId() == R.id.button10) {
                for (int i = 1; i <= 10; i++) {
                    mediaController.search();
                }
            }
        }

    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (screenActionReceiver != null) {
            screenActionReceiver.unRegisterScreenActionReceiver(this);
        }
        if (mediaController != null) {
            mediaController.stop();
            mediaController = null;
            finish();
            System.exit(0);
        }
    }

    private int verticalMinDistance = 35;
    private int minVelocity = 0;
    private byte direction = -1;

    @Override
    public boolean onTouchEvent(MotionEvent me) {
        return gestureScanner.onTouchEvent(me);
    }

    public void HowToSendMessage(float xx1, float yy1, float xx2, float yy2) {
        if (xx2 - xx1 == 0)
            return;
        Message msg = new Message();

        float k = (yy2 - yy1) / (xx2 - xx1);
        if (xx1 - xx2 > verticalMinDistance && Math.abs(k) < 1) // 左边
        {
            if (mediaController != null) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        if (connectingDevice != null && mediaController.sendMessage(SingleByteCode.LEFT, connectingDevice) == false) {
                            Message msg = new Message();
                            msg.what = 1003;
                            hander.sendMessage(msg);
                        }
                    }
                }).start();
            }
            msg.what = 1004;
            hander.sendMessage(msg);
        } else if (xx2 - xx1 > verticalMinDistance && Math.abs(k) < 1)// 右边
        {
            if (mediaController != null) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        if (mediaController.sendMessage(SingleByteCode.RIGHT, connectingDevice) == false) {
                            Message msg = new Message();
                            msg.what = 1003;
                            hander.sendMessage(msg);
                        }
                    }
                }).start();
            }
            msg.what = 1005;
            hander.sendMessage(msg);
        } else if (yy1 - yy2 > verticalMinDistance && Math.abs(k) > 1)// 向上
        {
            if (mediaController != null) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        if (mediaController.sendMessage(SingleByteCode.TOP, connectingDevice) == false) {
                            Message msg = new Message();
                            msg.what = 1003;
                            hander.sendMessage(msg);
                        }
                    }
                }).start();
            }
            msg.what = 1006;
            hander.sendMessage(msg);

        } else if (yy2 - yy1 > verticalMinDistance && Math.abs(k) > 1)// 向下
        {
            if (mediaController != null) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        if (mediaController.sendMessage(SingleByteCode.BOTTOM, connectingDevice) == false) {
                            Message msg = new Message();
                            msg.what = 1003;
                            hander.sendMessage(msg);
                        }
                    }
                }).start();
            }
            msg.what = 1007;
            hander.sendMessage(msg);
        }
    }

    @Override
    public boolean onDown(MotionEvent arg0) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2,
                           float arg3) {
        // TODO Auto-generated method stub
        HowToSendMessage(arg0.getX(), arg0.getY(), arg1.getX(), arg1.getY());
        return false;
    }

    @Override
    public void onLongPress(MotionEvent arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
                            float arg3) {
        // TODO Auto-generated method stub
        float xx1 = arg0.getX();
        float yy1 = arg0.getY();
        float xx2 = arg1.getX();
        float yy2 = arg1.getY();
        float k = (yy2 - yy1) / (xx2 - xx1);

        if (xx1 - xx2 > verticalMinDistance && Math.abs(k) < 1) // 左边
        {
            direction = SingleByteCode.LEFT;

        } else if (xx2 - xx1 > verticalMinDistance && Math.abs(k) < 1)// 右边
        {
            direction = SingleByteCode.RIGHT;

        } else if (yy1 - yy2 > verticalMinDistance && Math.abs(k) > 1)// 向上
        {
            direction = SingleByteCode.TOP;

        } else if (yy2 - yy1 > verticalMinDistance && Math.abs(k) > 1)// 向下
        {
            direction = SingleByteCode.BOTTOM;
        }

        return false;
    }

    @Override
    public void onShowPress(MotionEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onSingleTapUp(MotionEvent arg0) {
        // TODO Auto-generated method stub
        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                if (mediaController.sendMessage(SingleByteCode.CLICK, connectingDevice) == false) {
                    Message msg = new Message();
                    msg.what = 1003;
                    hander.sendMessage(msg);
                }
            }
        }).start();
        return false;
    }
}
