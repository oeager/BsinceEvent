package com.developer.bsince.event.example;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.developer.bsince.core.Headers;
import com.developer.bsince.data.FormEncodeDataSet;
import com.developer.bsince.data.JSONStreamDataSet;
import com.developer.bsince.data.LoadingListener;
import com.developer.bsince.data.MulitDataSet;
import com.developer.bsince.data.ProgressChangeListener;
import com.developer.bsince.data.SoapDataSet;
import com.developer.bsince.event.AbstractEvent;
import com.developer.bsince.event.EventPublisher;
import com.developer.bsince.exceptions.ParseException;
import com.developer.bsince.extras.SocketClient;
import com.developer.bsince.parser.JSONObjectParserImp;
import com.developer.bsince.request.Method;
import com.developer.bsince.response.ResponseListener;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Created by oeager on 2015/4/23.
 */
public class SampleActivity extends Activity {


    private EditText editext;

    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_main);
        editext = (EditText) findViewById(R.id.editText);
        dialog = new ProgressDialog(this);
    }


    public void onAction(View v) {

        dialog.show();
        FormEncodeDataSet ds = new FormEncodeDataSet();
        ds.put("name", "张三");
        ds.put("pwd", "123456");
        EventPublisher.connect(String.class, "/HttpService/login")
                .data(ds)

                .tag("login")
                .submit(new ResponseListener<String>() {
                    @Override
                    public void onSuccessResponse(String s) {
                        editext.setText(s);
                        dialog.dismiss();
                    }

                    @Override
                    public void onErrorResponse(Exception e) {
                        editext.setText(e.toString());
                        dialog.dismiss();
                    }
                });

        EventPublisher.connect(User.class,"/login?....")
                .parser(new JSONObjectParserImp<User>() {
                    @Override
                    public User parsed(JSONObject object, Headers mHeaders) throws ParseException {
                        User u = new User();
                        u.name = object.optString("name");
                        u.value =object.optString("value");
                        return u;
                    }
                })
                .submit(new ResponseListener<User>() {
                    @Override
                    public void onSuccessResponse(User response) {
                        editext.setText(response.name);
                    }

                    @Override
                    public void onErrorResponse(Exception e) {
                        editext.setText(e.toString());
                    }
                });

    }

    class User {
        String name;
        String value;
    }

    public void onAction2(View v) {
        dialog.show();
        FormEncodeDataSet ds = new FormEncodeDataSet();
        ds.put("user", "oeager");
        ds.put("habit", new String[]{"码程序", "看电影", "撸啊撸"});
        EventPublisher.connect(String.class, "/HttpService/habit")
                .data(ds)
                .method(Method.POST)
                .tag("habit")
                .submit(new ResponseListener<String>() {
                    @Override
                    public void onSuccessResponse(String s) {
                        editext.setText(s);
                        dialog.dismiss();
                    }

                    @Override
                    public void onErrorResponse(Exception e) {
                        dialog.dismiss();
                        editext.setText(e.toString());
                    }
                });

    }

    public void onAction3(View v) throws FileNotFoundException {
        dialog.show();
        MulitDataSet ds = new MulitDataSet();
        ds.put("title", "xxxxx");
        ds.put("timelength", "3333333");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher).compress(Bitmap.CompressFormat.PNG, 100, baos);
        InputStream is = new ByteArrayInputStream(baos.toByteArray());
        ds.put("videofile", new File(Environment.getExternalStorageDirectory(), "xiong.jpg"));
        ds.put("stream", is, "ic_laun.png");
        EventPublisher.connect(String.class, "/HttpService/upload")
                .method(Method.POST)
                .data(ds)
                .tag("upload")
                .submit(new ResponseListener<String>() {
                    @Override
                    public void onSuccessResponse(String s) {
                        editext.setText(s);
                        dialog.dismiss();
                    }

                    @Override
                    public void onErrorResponse(Exception e) {
                        dialog.dismiss();
                        editext.setText(e.toString());
                    }
                });

    }

    public void onAction4(View v) {

        dialog.show();
        SoapDataSet ds = new SoapDataSet(SoapDataSet.SOAP_VER_12);
        ds.setMethodName("getSupportCity");
        ds.setNameSpace("http://WebXml.com.cn/");
        ds.setSoapAction("http://WebXml.com.cn/getSupportCity");
        ds.put("byProvinceName", "北京");
        ds.dotNet = true;

        EventPublisher.connect(String.class, "http://www.webxml.com.cn/WebServices/WeatherWebService.asmx")
                .data(ds)
                .method(Method.POST)

                .submit(new ResponseListener<String>() {
                    @Override
                    public void onSuccessResponse(String s) {
                        editext.setText(s);
                        dialog.dismiss();
                    }

                    @Override
                    public void onErrorResponse(Exception e) {
                        dialog.dismiss();
                        editext.setText(e.toString());
                    }
                });

        SoapDataSet ds2 = new SoapDataSet(SoapDataSet.SOAP_VER_11);
        ds2.setMethodName("getTempVerifiCode");
        ds2.setNameSpace("http://jdk.study.hermit.org/client");
        ds2.setSoapAction("");
        ds2.put("userCode", "42a3c56c9e45c968");
        ds2.put("password", "");
        ds2.put("userType", "02");
        ds2.dotNet = false;

        EventPublisher.connect(String.class, "http://172.16.0.100:8080/wmsService/ws/userService")
                .data(ds2)
                .method(Method.POST)

                .submit(new ResponseListener<String>() {
                    @Override
                    public void onSuccessResponse(String s) {
                        editext.setText(s);
                        dialog.dismiss();
                    }

                    @Override
                    public void onErrorResponse(Exception e) {
                        dialog.dismiss();
                        editext.setText(e.toString());
                    }
                });

    }

    public void onAction5(View v) {
        dialog.show();
        JSONStreamDataSet ds = new JSONStreamDataSet(true, null);
        ds.put("name", "value");
        ds.put("key", "vaulue2");
        EventPublisher.connect(String.class, "/HttpService/jsonData").method(Method.POST).data(ds).submit(new ResponseListener<String>() {
            @Override
            public void onSuccessResponse(String s) {
                editext.setText(s);
                dialog.dismiss();
            }

            @Override
            public void onErrorResponse(Exception e) {
                dialog.dismiss();
                editext.setText(e.toString());
            }
        });
        JSONStreamDataSet ds2 = new JSONStreamDataSet(true, null);
        ds2.put("name", "value.png");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher).compress(Bitmap.CompressFormat.PNG, 100, baos);
        InputStream is = new ByteArrayInputStream(baos.toByteArray());
        ds2.put("file", is);
        EventPublisher.connect(String.class, "/HttpService/jsonUpload").method(Method.POST).data(ds2).submit(new ResponseListener<String>() {
            @Override
            public void onSuccessResponse(String s) {
                editext.setText(s);
                dialog.dismiss();
            }

            @Override
            public void onErrorResponse(Exception e) {
                dialog.dismiss();
                editext.setText(e.toString());
            }
        });


    }


    public void onAction6(View v) {

        dialog.show();
        FormEncodeDataSet ds = new FormEncodeDataSet();
        ds.put("name", "张三");
        ds.put("pwd", "123456");
        EventPublisher.connect(String.class, "/HttpService/cookie")
                .data(ds)
                .tag("login")
                .submit(new ResponseListener<String>() {
                    @Override
                    public void onSuccessResponse(String s) {
                        editext.setText(s);
                        dialog.dismiss();
                    }

                    @Override
                    public void onErrorResponse(Exception e) {
                        editext.setText(e.toString());
                        dialog.dismiss();
                    }
                });
    }


    public void onAction7(View v) {//SocketHttpClient
        File f =new File(Environment.getExternalStorageDirectory(), "xiong.jpg");
        Map<String,String> map = new HashMap<>();
        map.put("usercode","japson");
        dialog.show();
        SocketClient.doUpload("http://192.168.1.109:8080/HttpService/upload",map, new MulitDataSet.FileWrapper[]{new MulitDataSet.FileWrapper(f,null,"xiong.jpg")},new LoadingListener() {
            @Override
            public void onLoadingStarted() {
                Log.e("load","onLoadingStarted");
            }

            @Override
            public void onLoadProgressChanged(int i, int i2) {
                Log.e("load","onLoadProgressChanged"+i+"/"+i2);
            }

            @Override
            public void onLoadingError(Exception e) {
                Log.e("load","onLoadingError");
                dialog.dismiss();
            }

            @Override
            public void onLoadingEnd(boolean b, Object... objects) {
                Log.e("load","onLoadingEnd");
                dialog.dismiss();
            }
        });
    }

    public void onAction8(View v) {//Async任务回调

        EventPublisher.task().submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Thread.sleep(2000);
                return "hello";
            }
        }, new ResponseListener<String>() {
            @Override
            public void onSuccessResponse(String response) {

            }

            @Override
            public void onErrorResponse(Exception error) {

            }
        });

//        Toast.makeText(this,s,Toast.LENGTH_SHORT).show();
    }

    public void onAction9(View v) {//异步线程同步调取

        dialog.show();
        EventPublisher.submit(new AbstractEvent<String>(new ResponseListener<String>() {
            @Override
            public void onSuccessResponse(String s) {

                editext.setText(s);
                dialog.dismiss();
            }

            @Override
            public void onErrorResponse(Exception e) {
                editext.setText(e.toString());
                dialog.dismiss();
            }
        }) {
            @Override
            public String run() throws Exception {
                FormEncodeDataSet ds = new FormEncodeDataSet();
                ds.put("user", "oeager");
                ds.put("habit", new String[]{"码程序", "看电影", "撸啊撸"});
              return  EventPublisher.connect(String.class, "/HttpService/habit")
                        .data(ds)
                        .method(Method.POST)
                        .tag("habit")
                        .submit();
            }
        });
    }

    public void onAction10(View v) {//上传文件的上传进度
        dialog.show();
        MulitDataSet ds = new MulitDataSet();
        ds.put("title", "xxx文件");
        ds.put("timelength", "15");




        try {
            ds.put("videofile", new File(Environment.getExternalStorageDirectory(), "xiong.jpg"));
        } catch (Exception e) {
            e.printStackTrace();
            editext.setText(e.toString());
            return;
        }
        ds.setProgressChangeListener(new ProgressChangeListener() {
            @Override
            public void onLoadProgressChanged(int currentSize, int totalSize) {
                Log.d("Sample",currentSize+"/"+totalSize);
            }
        });
        EventPublisher.connect(String.class, "/HttpService/upload")
                .method(Method.POST)
                .data(ds)
                .tag("upload")
                .submit(new ResponseListener<String>() {
                    @Override
                    public void onSuccessResponse(String s) {
                        editext.setText(s);
                        dialog.dismiss();
                    }

                    @Override
                    public void onErrorResponse(Exception e) {
                        dialog.dismiss();
                        editext.setText(e.toString());
                    }
                });
    }

    public void onAction11(View v) {

    }


}
