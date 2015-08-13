package net;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by sjyhe on 2015/8/12.
 */
public class HttpDownload extends Thread {
    //TAG for debug
    private final static String TAG="HttpDownload";
    //Flag for handler
    private final static int GOTONE=2, LOSEONE=-2,EMPTY=-3;
    private  String url=null;
    private Handler handler=null;
    private int antNum=0;
    //thread pool to take download tasks
    private ExecutorService ants =null;


    public HttpDownload(String url, Handler handler, int antNum){
        this.url=url;
        this.handler=handler;
        this.antNum=antNum;
        ants = Executors.newFixedThreadPool(antNum);
        Log.i(TAG,"HttpDownloader created");
    }

    @Override
    public void run() {
        Log.i(TAG, "try to get connection");

        HttpURLConnection connection=null;
        try {
            URL httpUrl=new URL(url);
            connection=(HttpURLConnection)httpUrl.openConnection();
            Log.i(TAG, "open connection");
            connection.setReadTimeout(5000);
            connection.setRequestMethod("GET");

            int size;
            size = connection.getContentLength();
            Log.i(TAG, "file size:" + size);
            if(size==0) {
                Message msg=new Message();
                msg.what=EMPTY;
                handler.sendMessage(msg);
            }
            int block=size/antNum;
            String fileName=getFileName(url);
            File root= Environment.getExternalStorageDirectory();
            File file=new File(root,fileName);
            String saveTo=file.getAbsolutePath();
            Log.i(TAG,"save to"+saveTo);
            for(int i=0;i<antNum;i++){
                long from=i*block;
                long to=(from+block)<size?(from+block):size;
                AntTask task=new AntTask(httpUrl,handler,saveTo,from,to);
                ants.execute(task);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(connection!=null) connection.disconnect();
        }
    }

    private String getFileName(String url){
        return url.substring(url.lastIndexOf('/')+1);
    }

   //download task for each thread
    static class AntTask  implements Runnable{
        URL httpUrl=null;
        Handler handler=null;
        long from=0;
        long to=0;
        String saveTo;

        public AntTask(URL httpUrl,Handler handler,String saveTo,long from, long to){
            this.httpUrl=httpUrl;
            this.handler=handler;
            this.from=from;
            this.to=to;
            this.saveTo=saveTo;
        }

        @Override
        public void run() {
            HttpURLConnection connection=null;
            RandomAccessFile access=null;
            InputStream in=null;
            try {
                connection=(HttpURLConnection) httpUrl.openConnection();
                connection.setRequestMethod("GET");
                connection.setReadTimeout(5000);
                Log.i(TAG, "download from" + from + "to" + to);
                connection.setRequestProperty("Range", "bytes=" + from + "-" + to);
                access=new RandomAccessFile(new File(saveTo),"rwd");
                access.seek(from);
                in=connection.getInputStream();
                Log.i(TAG, "got inputStream");
                byte[] buffer=new byte[2048];
                int len=0;
                while ((len=in.read(buffer))!=-1){
                    access.write(buffer,0,len);
                }
                Message msg=new Message();
                msg.what=GOTONE;
                handler.sendMessage(msg);
            } catch (IOException e) {
                e.printStackTrace();
                Message msg=new Message();
                msg.what=LOSEONE;
                handler.sendMessage(msg);
            }finally {
                if(connection!=null) connection.disconnect();
                try {
                    if(access!=null) access.close();
                    if(in!=null) in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

}
