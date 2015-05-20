# BsinceEvent
这是一个基于HttpUrlConnection的Android异步网络框架。<br>


##特点:

* 高度的可配置化.全局配置与单次事件配置都是可控的.<br>
* 支持请求的重试，重定义，代理，认证,取消等.<br>
* 支持自动化的Cookie管理<br>
* 支持http缓存.<br>
* 实现请求参数的封装与响应的回调.<br>
* 支持Http的所有谓词,支持异步与同步两种调用方式.
* 支持其它异步任务的回调.
* 连缀式调用方式，希望你喜欢.
* 其实它还有更多,等待你去发现.

##使用

####1.添加引用
* Eclipse <br>
>>下载jar文件，添加至项目的libs目录下.<br>
* Gradle<br>
>>waitting...<br>

####2.配置
你需要在你项的Application的子类中（如果没有继承自该类，需要建类继承，并在Manifest文件中注册）的oncreate方法中进行相关参数的配置.
```Java
public class AppContext extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        HttpGlobalConfiguration globalConfiguration
                = new HttpGlobalConfiguration.Builder()
                .connectTimeOut(5000)
                .readTimeOut(5000)
                .bufferSize(2048)
                .debug(true)
                .endPoint("http://192.168.1.109:8080")
                .userAgent("Bsince/test/engine")
                .useCookie(true)
                .engineType(EngineType.Engine_Bsince)
                .threadSize(3)
                .defaultAllowRedirect(false)
                .keepAlive(true)
//              .proxy(...)
//              .urlRewriter(...)
//              .defaultSSLSocketFactory(...)
//              .defaultHostnameVerifier(...)
//              .defaultContentHandlerFactory(...)
//              .authenticator(...)
//              .cookieHandler(...)
//              .netCacheDir(...)
                //more you want ... 
                .retryCount(2)
                .addDefaultRequestProperty("customHeader", "customerHeaderValue")
                .build(this);
        EventPublisher.init(globalConfiguration);
    }
}
```
上面的每一项配置都不是必须的，因为程序会默认一个值，如果你不想改变它，可以直接像下面这样
```Java
HttpGlobalConfiguration globalConfiguration= new HttpGlobalConfiguration.Builder().build(this);
EventPublisher.init(globalConfiguration);
```

####3.调用
完成如上配置后，你就可以愉快地开发了。比如
#####Get请求
```Java
 EventPublisher.connect(String.class, "http://www.baidu.com")
                .tag("baidu")
                .submit(new ResponseListener<String>() {
                    @Override
                    public void onSuccessResponse(String s) {
                        editext.setText(s);
                    }
                    @Override
                    public void onErrorResponse(Exception e) {
                        editext.setText(e.toString());
                    }
                });
```

#####Post请求
```Java
        FormEncodeDataSet ds = new FormEncodeDataSet();
        ds.put("name", "张三");
        ds.put("pwd", "123456");
        EventPublisher.connect(String.class, "/HttpService/login")
                .data(ds)
                .method(Method.POST)
                .tag("login")
                .submit(new ResponseListener<String>() {
                    @Override
                    public void onSuccessResponse(String s) {
                        editext.setText(s);
                    }

                    @Override
                    public void onErrorResponse(Exception e) {
                        editext.setText(e.toString());
                    }
                });
```
`注意:在使用EventPublisher.Connect("")时,未指定method将会被默认成GET请求，上面示例中FormEncodeDataset是用于封装请求参数的实体类，Get请求也可以使用.data()方法来指定请求参数，请求参数将默认以表单提交的方式追加到Url后面`

如果你的返回类型不是String类型,你还必须为其指定一个parser作为解析器,我们为你提供以下几种默认的解析器，以便你针对不同情况时使用：
* BitmapParserImp     //默认解析后返回Bitmap对象
* FileDownloadParser     // 默认解析后返回文件对象
* JSONArrayParserImp      //抽象类，返回jsonArray由你去实现将其转换为javaBean对象
* JSONObjectParserImp     // 抽象类，返回JsonObject由你去实现将其转换为javaBean对象
* SimpleParserImp       //抽象类，返回String由你去实现将其转换为javaBean对象
* TextParserImp      //默认返回String字符串
* XmlParserImp       //抽象类，返回Xmlpullparser由你去实现将其转换为javaBean对象<br>
* 
如下是使用JSONObjectParserImp的示例
```Java
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
                    public void onErrorResponse(Exception error) {
                        editext.setText(e.toString());
                    }
                });
```

#####3. File Upload<br>

BsinceEvent支持File与InputStream两种形式的文件上传<br>

```java
        MulitDataSet ds = new MulitDataSet();
        ds.put("title", "xxxxx");
        ds.put("videofile", file,filename);
        ds.put("stream", is, streamName);
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
```
相信你也发现了，这里我们用的是MulitDataset来封装参数.是的我们为你提拱了以下几种Dataset的实现
* JsonDataSet<br>
用来传输Json格式的字符参数 <br>
* JsonStreamDataSet <br>
将文件流转化为Json形式上传，支持Gzip压缩 <br>
* FormEncodeDataset  <br>
普通的表单提交，支持集合类型<br>
* NameValueDataSet <br>
键值对型，与FormEncodeDataSet功能近似,支持集合类型，但集合若有序键会追加下标(eg:key[index]) <br>
* SoapDataSet <br>
构建webService服务的请求参数，下面会提到 <br>

到这里，也许关于文件上传就差不多了，文件、流、都能上传，甚至可以Gzip压缩成Json字符串上传,如此说来确实很方便。但是？
是的这里存在一些问题：
* 太大的文件最好不要通过Http协议上传（一般是2M）,会崩的.怎么办呢？<br>
当然你可以改成ChunkingMode，BsinceEvent也支持这种方式,重写DataSet下面这个方法,并重写setpropertybeforeConnect，设置块大小.
```Java
@Override
	public boolean isChunkedStreamingMode() {
		return true;
	}
	@Override
	public void setPropertyBeforeConnect(HttpURLConnection conn) {
    conn.setChunkedStreamingMode(5); 
	}
```
当然我们更乐意你用另一种方式,我们额外提供了一个SocketClient;
```Java
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
```
* 如何监听上传进度？<br>
是的，相信你也发现了，用Socket上传还能监听到上传进度。是的，因为这里我们只实现了socket文件的上传，而文件的总长度是可知的，所以监听到其上传进度并不意外。实现上，如果你使用Mulitdataset，也只向里面添加了文件，而没有流的话，也是可以实现监听的。
```Java
        MulitDataSet ds = new MulitDataSet();
        ds.put("title", "xxx文件");
        ds.put("timelength", "15");
        try {
            ds.put("videofile", new File(Environment.getExternalStorageDirectory(), "xiong.jpg"));
        } catch (FileNotFoundException e) {
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
```
好了，文件上传部份暂时就到这里
#####4.WebService请求
有时其实会遇到这样的情况，需要向一些公众支持接口调取服务，比如天气，归属地等，它们一般都是以webservice服务,这时你大可不必担忧，因为这里提供了一种Dataset的实现
* doNet平台
```Java
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
```
* 非doNet平台
```Java
         SoapDataSet ds2 = new SoapDataSet(SoapDataSet.SOAP_VER_11);
        ds2.setMethodName("getTempVerifiCode");
        ds2.setNameSpace("http://jdk.study.hermit.org/client");
        ds2.setSoapAction("");
        ds2.put("userCode", "42a3c56c9e45c968");
        ds2.put("password", "");
        ds2.put("userType", "02");
        ds2.dotNet = false;
        EventPublisher.connect(String.class, "http://wwww...........")
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
```
#####5.Cookie
在配置Globalconfiguration时,useCookie设为true,将会自动处理Cookie;
#####6.同步与异步
BsinceEvent在submit时，设置了对应回调ResponseListener时，为异步调用，不传入参数时，是为同步调用。
```Java
String s =EventPublisher.connect(String.class, "/HttpService/habit")
                        .data(ds)
                        .method(Method.POST)
                        .tag("habit")
                        .submit();
```
同时BsinceEvent还支持异步任务的调用
```java
public static void submit(Event<?> task) {
     
}
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
```
#####7.图片的加载
```Java
EventPublisher.connect(Bitmap.class,"http://img0.bdstatic.com/img/image/f3165146edddf5807b7cb40a426ffaaf1426747348.jpg").bitmapExtras(0,0, Bitmap.Config.RGB_565)
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
```
在列表中显示图片，使用NetworkImageview
```Java
            NetworkImageView view = (NetworkImageView) convertView.findViewById(R.id.head);
            TextView tex= (TextView) convertView.findViewById(R.id.url);
            tex.setText(getItem(position));
            view.setDefaultImageResId(R.mipmap.ic_launcher);
            view.setImageUrl(getItem(position), imageLoader);
```
#####8.文件下载
```Java
 FileDownloadParser parser = new FileDownloadParser(Environment.getExternalStorageDirectory().getAbsolutePath(),"inst.exe");
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
```
`下载文件时，设置缓存模式为CacheModel.CACHE_NEVER`<br>
如果要监听下载进度，在parser中进行设置
```Java
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
```

#####9.Extras
* 清除http磁盘缓存
```Java
EventPublisher.clearHttpCache(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ListActivity.this,"缓存清除成功",Toast.LENGTH_SHORT).show();
                }
            });
```
* 取消请求
```Java
        EventPublisher.cancel("tag");//通过标记请求
        EventPublisher.cancelAll();//取消全部
        EventPublisher.cancel(3);//通过序号取消
        EventPublisher.cancel(new EventFilter() {//自定义筛选条件取消
            @Override
            public boolean apply(Event<?> task) {
                return false;
            }
        });
```
`目前项目中的注释大量‘清白’，主要是想全部改成英文注释，删完原先注释之后，才发现工作量不小,只能以后尽快补上了,若造成了你的不便，请谅解`<br>
如果你在使用过程中发现Bug或遇到什么问题，请与我联系 oeager@foxmail.com



