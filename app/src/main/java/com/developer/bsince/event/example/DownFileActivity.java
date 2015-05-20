package com.developer.bsince.event.example;

import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.developer.bsince.data.LoadingListener;
import com.developer.bsince.event.EventPublisher;
import com.developer.bsince.parser.FileDownloadParser;
import com.developer.bsince.request.CacheModel;
import com.developer.bsince.response.ResponseListener;

import java.io.File;


public class DownFileActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_down_file);
    }

    public  void onAction(View v ){
        FileDownloadParser parser = new FileDownloadParser(Environment.getExternalStorageDirectory().getAbsolutePath(),"inst.exe");
        parser.setLoadingListener(new LoadingListener() {
            @Override
            public void onLoadingStarted() {

                Log.i("down","onLoadingStarted");
            }

            @Override
            public void onLoadProgressChanged(int currentSize, int totalSize) {
                Log.i("down","onLoadProgressChanged:"+currentSize+"/"+totalSize);
            }

            @Override
            public void onLoadingError(Exception e) {
                Log.i("down","onLoadingError:"+e.toString());
            }

            @Override
            public void onLoadingEnd(boolean complete, Object... args) {
                Log.i("down","onLoadingEnd");
            }
        });
        EventPublisher.connect(File.class, "http://down.360safe.com/360/inst.exe")
                .parser(parser)
                .cacheMode(CacheModel.CACHE_NEVER)//禁用缓存
                .submit(new ResponseListener<File>() {
                    @Override
                    public void onSuccessResponse(File response) {
                        Toast.makeText(DownFileActivity.this,"下载成功",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onErrorResponse(Exception error) {
                        Toast.makeText(DownFileActivity.this,"下载失败",Toast.LENGTH_SHORT).show();
                    }
                });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_down_file, menu);
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
}
