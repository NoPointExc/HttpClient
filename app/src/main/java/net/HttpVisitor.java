package net;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.Buffer;

/**
 * Created by sjyhe on 2015/8/13.
 */
public class HttpVisitor extends Thread {
    //handler flag
    private static final int SUCCESS=1, FAILED=-1;
    //log tag
    private static final String TAG="HttpVisit";

    private String url;
    private Handler handler;
    private String usr;
    private String pwd;


    public HttpVisitor(String url, String usr, String pwd, Handler handler) {
        this.url=url;
        this.usr=usr;
        this.pwd=pwd;
        this.handler=handler;
    }

    @Override
    public void run() {
        //doPost();
        doGet();
    }

    private String attendReq(String url){
        StringBuffer sb=new StringBuffer(url);
        sb.append("?username=");
        sb.append(usr);
        sb.append("&password=");
        sb.append(pwd);
        return  sb.toString();
    }
    private void doGet(){
        HttpURLConnection connection=null;
        OutputStream out=null;
        InputStream in=null;
        Message msg=new Message();
        msg.what=FAILED;
        try {
            URL httpUrl=new URL(attendReq(url));
            connection=(HttpURLConnection) httpUrl.openConnection();
            connection.setRequestMethod("GET");
            in=connection.getInputStream();
            InputStreamReader inReader=new InputStreamReader(in);
            BufferedReader reader=new BufferedReader(inReader);
            String line="";
            StringBuffer sb=new StringBuffer();
            while( (line=reader.readLine())!=null){
                sb.append(line);
            }
            if(sb.toString().equals("success")) msg.what=SUCCESS;
            handler.sendMessage(msg);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(in!=null) in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(connection!=null) connection.disconnect();

        }
    }

    private void doPost() {
        HttpURLConnection connection=null;
        OutputStream out=null;
        InputStream in=null;
        Message msg=new Message();
        msg.what=FAILED;
        try {
            URL httpUrl=new URL(url);
            connection=(HttpURLConnection) httpUrl.openConnection();
            Log.i(TAG, "get connection");
            connection.setRequestMethod("POST");
            connection.setReadTimeout(5000);
            out=connection.getOutputStream();
            Log.i(TAG, "get output stream");
            String content="?username="+usr+"&password="+pwd;
            Log.i(TAG, "content:" + content);
            out.write(content.getBytes());
            Log.i(TAG, "write content");
            //out.flush();
            in=connection.getInputStream();
            Log.i(TAG,"get input stream");
            InputStreamReader inReader=new InputStreamReader(in);
            BufferedReader reader=new BufferedReader(inReader);
            //BufferedReader reader=new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line="";
            StringBuffer sb=new StringBuffer();
            out.flush();
            while( (line=reader.readLine())!=null){
                sb.append(line);
            }
            String received=sb.toString();
            Log.i(TAG,"reveived "+received);
            if(received.equals("success")) msg.what=SUCCESS;
            handler.sendMessage(msg);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(connection!=null) connection.disconnect();
            try {
                if(out!=null) out.close();
                if(in!=null) in.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        }
    }
}
