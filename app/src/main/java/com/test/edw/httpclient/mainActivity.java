package com.test.edw.httpclient;


import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import net.HttpDownload;
import net.HttpDownloader;
import net.HttpVisit;



public class mainActivity extends ActionBarActivity {
    private final static String TAG="MainActivity";
    //http visit flag for handler
    private final int SUCCESS=1, FAILED=-1;
    //http download flag for handler
    private final int GOTONE=2, LOSEONE=-2,EMPTY=-3;

    private TextView usrText;
    private TextView pswText;
    private String url="http://192.168.1.4:8080/HelloAndroid/src/com/servlets/EchoServlet";
    private String res="http://192.168.1.7:8080/HelloAndroid/hiAndroid.jpg";
    private int antNum=3;

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            int blockNum=-1;
            switch (msg.what){
                case SUCCESS:
                    System.out.println("Log in success");
                    Toast.makeText(mainActivity.this,"Log in success",Toast.LENGTH_SHORT).show();
                    break;
                case FAILED:
                    System.out.println("Wrong password or username");
                    Toast.makeText(mainActivity.this,"Wrong password or username",Toast.LENGTH_SHORT).show();
                    break;
                case GOTONE:
                    blockNum++;
                    break;
                case LOSEONE:
                    break;
                case EMPTY:
                    Toast.makeText(mainActivity.this,"no resource found in given url",Toast.LENGTH_SHORT).show();
                    break;
            }
            if(blockNum!=-1){
                Toast.makeText(mainActivity.this,"downloaded"+(blockNum+1)+"/"+antNum,Toast.LENGTH_SHORT).show();
            }

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        usrText=(EditText)findViewById(R.id.usr_text);
        pswText=(EditText)findViewById(R.id.psw_text);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
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

    public void submit(View view){
        //Toast.makeText(this, "submit", Toast.LENGTH_SHORT).show();
        String usr=usrText.getText().toString();
        String psw=pswText.getText().toString();
        HttpVisit visit=new HttpVisit(url,usr,psw,handler);
        visit.start();
    }

    public void download(View view){

        HttpDownloader downloader=new HttpDownloader(res,handler,antNum);
        //HttpDownload download=new HttpDownload(res,handler);
        Log.i(TAG,"try to download");
        downloader.start();
    }

}
