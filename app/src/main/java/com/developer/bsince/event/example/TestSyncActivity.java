package com.developer.bsince.event.example;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.developer.bsince.event.EventPublisher;
import com.developer.bsince.parser.BitmapParserImp;
import com.developer.bsince.request.CacheModel;
import com.developer.bsince.response.ResponseListener;

/**
 * Created by oeager on 2015/5/20.
 */
public class TestSyncActivity extends ActionBarActivity {
    private LinearLayout layout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);
        layout = (LinearLayout) findViewById(R.id.parent);;
    }

    public  void onAction(View v){
        layout.removeAllViews();
        for (int i =0;i<Constants.images.length;i++){
           final ImageView view = new ImageView(this);

            view.setImageResource(R.mipmap.ic_launcher);
            layout.addView(view);
            EventPublisher.connect(Bitmap.class,Constants.images[i])
                    .parser(BitmapParserImp.INSTANCE)
                    .cacheMode(CacheModel.CACHE_NEVER)
                    .submit(new ResponseListener<Bitmap>() {
                        @Override
                        public void onSuccessResponse(Bitmap response) {
                            view.setImageBitmap(response);
                        }

                        @Override
                        public void onErrorResponse(Exception error) {
                            view.setImageResource(R.mipmap.ic_launcher);
                        }
                    });

        }
    }
}
