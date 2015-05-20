package com.developer.bsince.event.example;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.developer.bsince.cache.LRUMemoryCache;
import com.developer.bsince.event.Event;
import com.developer.bsince.event.EventFilter;
import com.developer.bsince.event.EventPublisher;
import com.developer.bsince.extras.ImageLoader;
import com.developer.bsince.extras.NetworkImageView;
import com.developer.bsince.parser.BitmapParserImp;
import com.developer.bsince.request.CacheModel;
import com.developer.bsince.response.ResponseListener;

import java.util.List;


public class ListActivity extends ActionBarActivity {

    private ImageView image;
        private ImageLoader.ImageCache cache = new LRUMemoryCache(5*1024*1024);
//    private ImageLoader.ImageCache cache = new ImageLoader.ImageCache() {
//        @Override
//        public Bitmap getBitmap(String url) {
//            return null;
//        }
//
//        @Override
//        public void putBitmap(String url, Bitmap bitmap) {
//
//        }
//    };

    private ImageLoader imageLoader = new ImageLoader(cache);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        image = (ImageView) findViewById(R.id.image);
        ListView mlist = (ListView) findViewById(R.id.list);
        mlist.setAdapter(new MyAdapter());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventPublisher.cancel("tag");
        EventPublisher.cancelAll();
        EventPublisher.cancel(3);
        EventPublisher.cancel(new EventFilter() {
            @Override
            public boolean apply(Event<?> task) {
                return false;
            }
        });
    }

    public void onAction(View v){
        EventPublisher.connect(Bitmap.class,"http://img0.bdstatic.com/img/image/f3165146edddf5807b7cb40a426ffaaf1426747348.jpg")
                .bitmapExtras(0,0, Bitmap.Config.RGB_565)
                .cacheMode(CacheModel.CACHE_NEVER)
                .parser(BitmapParserImp.INSTANCE)
                .submit(new ResponseListener<Bitmap>() {
                    @Override
                    public void onSuccessResponse(Bitmap response) {
                        image.setImageBitmap(response);
                    }

                    @Override
                    public void onErrorResponse(Exception error) {
                        Toast.makeText(ListActivity.this,error.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list, menu);
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

    class MyAdapter extends BaseAdapter{


        @Override
        public int getCount() {
            return Constants.images.length;
        }

        @Override
        public String getItem(int position) {
            return Constants.images[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView==null){
                convertView = LayoutInflater.from(ListActivity.this).inflate(R.layout.item, null);
            }
            NetworkImageView view = (NetworkImageView) convertView.findViewById(R.id.head);
            TextView tex= (TextView) convertView.findViewById(R.id.url);
            tex.setText(getItem(position));
            view.setDefaultImageResId(R.mipmap.ic_launcher);
            view.setImageUrl(getItem(position), imageLoader);
            return  convertView;
        }
    }
}
