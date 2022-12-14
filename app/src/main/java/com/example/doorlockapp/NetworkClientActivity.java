package com.example.doorlockapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class NetworkClientActivity extends AppCompatActivity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_network_client, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private static final String TAG = "SensorQuery";

    final static int SERVER_SELECT = 2001;
    final static int SERVER_PORT = 8888;

    final static String  PREF_FILE_NAME = "ServerInfo";
    final static String  PREF_KEY_SERVERIP = "ServerIp";

    int  phoneID = 1; // phone id 1~128
    SharedPreferences prefs;

    String   ServerIP;

    Timer QuerySensorTimer;
    byte[]  packet;

    NetManager NetMgr;

    TextView NetStatus;
    TextView DoorStatus;

    final static int DOOR_CLOSE = 0;
    final static int DOOR_OPEN = 1;

    byte nSendDoorVal;

    boolean  connectingActionDoneFlag = false;

    int nSensingVal = 0;
    int nSensingVal1 = 0;
    int nSensingVal2 = 0;

//    private OutputStream outs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_client);

        NetMgr = new NetManager();
        NetMgr.setRxHandler(mNetHandler);

        packet = new byte[100];

        // get server ip , port
        prefs = getSharedPreferences(PREF_FILE_NAME,MODE_PRIVATE);
        ServerIP = prefs.getString(PREF_KEY_SERVERIP, "192.168.10.56");

        // Server IP??? Server Port??? Activity??? ??????
        disSeverSet(ServerIP, SERVER_PORT);
        Log.d(TAG, "ServerIP:" + ServerIP);
        Log.d(TAG, "ServerPort:" + SERVER_PORT);


        NetStatus = (TextView)findViewById(R.id.textViewNetStatus);
        DoorStatus = (TextView)findViewById(R.id.textViewDoorStatus);

        Button btn = (Button)findViewById(R.id.buttonServiceStart);
        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                // UI ?????? ?????? ??????
                arg0.setClickable(false);
                // ?????? ?????????
                arg0.setEnabled(false);

                NetMgr.setIpAndPort(ServerIP, SERVER_PORT);
                NetMgr.startThread();

                connectingActionDoneFlag = true;
                // set Timer 4?????? ??????, 1????????? ??????, SendCmd()??????
                startQuerySensorTimer();
            }
        });

        ImageButton btn2 = (ImageButton)findViewById(R.id.buttonServerSet);
        btn2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                serverSel();
            }
        });

        Switch sw = (Switch)findViewById(R.id.switchChangeDoor);
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    changeDoorStatus(DOOR_OPEN);
                }
                else {
                    changeDoorStatus(DOOR_CLOSE);
                }
            }
        });
    }

    public void clickButton(View v) {
        Intent intent = new Intent(this, ShowPicActivity.class);
        startActivity(intent);
    }

    public void serverSel()
    {
        // Intent??? ?????? Activity(AnotherActivity.class)??? ???????????? ???,
        Intent intent = new Intent(this, ServerSetActivity.class);

        // AnotherActivity??? ???????????? ????????? ??? ???????????? putExtra
        // AnotherActivity????????? getExtras()??? ?????? ???????????? ?????? - ?????? onCreate() ???????????? ??????
        intent.putExtra(ServerSetActivity.SERVER_IP, ServerIP);
        startActivityForResult(intent, SERVER_SELECT);
    }
    private void startQuerySensorTimer()
    {
        QuerySensorTimer = new Timer();
        QuerySensorTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                SendCmd();
                Log.d(TAG, "Send SensorQuery1");
            }

        }, 4000, 1000); // schedule(TimerTask task , long delay , long period)
    }
    private void stopQuerySensorTimer()
    {
        QuerySensorTimer.cancel();
        QuerySensorTimer.purge();
    }

    final static int PKT_INDEX_STX = 0;
    final static int PKT_INDEX_CMD = 1;
    //    final static int PKT_INDEX_DATA = 2;
    final static int PKT_INDEX_DATA1 = 2;
    final static int PKT_INDEX_DATA2 = 3;
    final static int PKT_INDEX_ETX = 4;

    final static byte PKT_STX = 0x01;
    final static byte PKT_ETX = 0x05;

    // data cmd
    final static int CMD_SENSOR_REQ = 0x10;
    final static int CMD_SENSOR_RES = 0x90;

    private int unSignedByteToInt(byte value)
    {
        int nTemp;
        if ( value >= 0)
            nTemp = (int)value;
        else
            nTemp = (int)value + 256;
        return nTemp;
    }
    private int SendCmd()
    {
        if ( NetMgr.getNetStatus() != NetManager.NET_CONNECTED)
        {
            return -1;
        }
        packet[PKT_INDEX_STX] = PKT_STX;
        packet[PKT_INDEX_CMD] = CMD_SENSOR_REQ;
//        packet[PKT_INDEX_DATA] = 0x00;
        packet[PKT_INDEX_DATA1] = nSendDoorVal;
        packet[PKT_INDEX_DATA2] = 0x00;
        packet[PKT_INDEX_ETX] = PKT_ETX;

        return NetMgr.SendData(packet,  5);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if ( requestCode == SERVER_SELECT)
        {
            if (resultCode == RESULT_OK)
            {
                ServerIP = data.getStringExtra(ServerSetActivity.SERVER_IP);

                Log.d(TAG,"setting ServerIP:" + ServerIP);
                phoneID = data.getIntExtra(ServerSetActivity.PHONEID,1);

                disSeverSet(ServerIP,SERVER_PORT);
                // save
                SharedPreferences.Editor ed = prefs.edit();
                ed.putString(PREF_KEY_SERVERIP, ServerIP);

                ed.commit();

            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
    private void disSeverSet(String ip , int port)
    {
        TextView tv = (TextView)findViewById(R.id.textViewServerIP);
        tv.setText("IP:" + ip + ", PORT:" + Integer.toString(port));
    }
    private Handler mNetHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);

            switch(msg.what)
            {
                case NetManager.HANDLE_RXCMD:
                    doRxCmd(msg.getData());
                    break;
                case NetManager.HANDLE_NETSTATUS:
                    doNetStatus(msg.arg1);
                    break;
            }
        }
    };

    private void doRxCmd(Bundle data)
    {
        int len = data.getInt(NetManager.RX_LENGHT);
        if ( len < 5)
            return;
        byte[] dataArr = data.getByteArray(NetManager.RX_DATA);

        if (unSignedByteToInt(dataArr[PKT_INDEX_STX]) != PKT_STX)
        {
            Log.d(TAG,"doRxCmd - PKT_STX fail");
            return ;
        }
        Log.d(TAG,"dataArr[PKT_INDEX_CMD] : " + unSignedByteToInt(dataArr[PKT_INDEX_CMD]));
        Log.d(TAG,"CMD_SENSOR_RES : " + CMD_SENSOR_RES);
        if (unSignedByteToInt(dataArr[PKT_INDEX_CMD]) != CMD_SENSOR_RES)
        {
            Log.d(TAG,"doRxCmd - CMD_SENSOR_RES fail");
            return ;
        }
        if (unSignedByteToInt(dataArr[PKT_INDEX_ETX]) != PKT_ETX)
        {
            Log.d(TAG,"doRxCmd - PKT_ETX fail");
            return;
        }

        // SensingVal query
//        nSensingVal1 = dataArr[PKT_INDEX_DATA];
        nSensingVal1 = unSignedByteToInt(dataArr[PKT_INDEX_DATA1]);
        nSensingVal2 = unSignedByteToInt(dataArr[PKT_INDEX_DATA2]);

        Log.d(TAG, "nSensingVal1" + nSensingVal1);
        Log.d(TAG, "nSensingVal2" + nSensingVal2);

        nSensingVal = (nSensingVal2*256)+nSensingVal1;
        Log.d(TAG, "nSensingVal" + nSensingVal);

        doDoorStatus(nSensingVal);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        Log.d(TAG,"OnDestroy");
        if (connectingActionDoneFlag) {
            NetMgr.stopThread();
        }
        super.onDestroy();
    }
    @Override
    protected void onRestart() {
        // TODO Auto-generated method stub
        super.onRestart();
        Log.d(TAG, "onRestart");
        if (connectingActionDoneFlag)
        {
            startQuerySensorTimer();
        }
    }

    @Override
    protected void onStop() {
        Log.d(TAG,"onStop");
        // TODO Auto-generated method stub
        if (connectingActionDoneFlag)
        {
            stopQuerySensorTimer();
        }
        super.onStop();
    }

    private void doNetStatus(int status)
    {
        switch(status)
        {
            case NetManager.NET_NONE:
                NetStatus.setText("Unkwown Network Status");
                break;
            case NetManager.NET_DISCONNECT:
                NetStatus.setText("disconnected");
                break;
            case NetManager.NET_CONNECTING:
                NetStatus.setText("connecting ...");
                break;
            case NetManager.NET_CONNECTED:
                NetStatus.setText("connected");
                break;
        }
    }

    private void doDoorStatus(int status)
    {
        Switch swc = (Switch)findViewById(R.id.switchChangeDoor);

        switch(status)
        {
            case DOOR_CLOSE:
                DoorStatus.setText("CLOSE");
                swc.setChecked(false);
                break;
            case DOOR_OPEN:
                DoorStatus.setText("OPEN");
                swc.setChecked(true);
                break;
        }
    }
    private int changeDoorStatus(int txCmd)
    {
        switch(txCmd)
        {
            case DOOR_CLOSE:
                DoorStatus.setText("CLOSE");
                nSendDoorVal = DOOR_CLOSE;
                break;
            case DOOR_OPEN:
                DoorStatus.setText("OPEN");
                nSendDoorVal = DOOR_OPEN;
                break;
        }
        return nSendDoorVal;
    }

}
