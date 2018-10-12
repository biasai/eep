package cn.android.support.v7.lib.eep.kera.utils;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.NinePatch;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cn.android.support.v7.lib.eep.kera.base.KApplication;


/**
 * Created by 彭治铭 on 2017/3/6.
 */

public class KAssetsUtils {
    private AssetManager am;
    private static KAssetsUtils assets;

    private KAssetsUtils() {
        am = KApplication.getInstance().getResources().getAssets();
        map = new HashMap<>();
        mapViews = new HashMap<>();
    }

    //初始化(兼容了Context和Activity,以Activity为主)
    public static KAssetsUtils getInstance() {
        if (assets == null) {
            assets = new KAssetsUtils();
        }
        return assets;
    }

    //位图数组(java对象赋值是传引用，都指向同一个对象)
    private HashMap<String, Bitmap> map;

    //获取缓存位图
    public Bitmap getCacleBitmap(String key) {
        if (map.containsKey(key) && map.get(key) != null && !map.get(key).isRecycled()) {//确保Bitmap不为null
            return map.get(key);//防止重复加载，浪费内存
        }
        if (map.containsKey(key)) {
            map.remove(key);//移除多余无用的键值
        }
        return null;
    }

    //设置缓存位图
    public void setCacleBiatmap(String key, Bitmap bitmap) {
        if (key == null || bitmap == null) {
            return;
        }
        map.put(key, bitmap);
    }

    //释放单个位图
    public void recycleBitmap(String key) {
        if (map.containsKey(key) && map.get(key) != null && !map.get(key).isRecycled()) {//确保Bitmap不为null
            map.get(key).recycle();
            map.remove(key);
//            Log.e("test","位图释放1:\t"+key);
        } else if (map.containsKey(key)) {
            map.remove(key);
//            Log.e("test","位图释放2:\t"+key);
        }
    }

    //保存每一个View，防止View重复适配
    private HashMap<String, View> mapViews;

    /**
     * 【asset/res和被绑定在apk里，并不会解压到/data/data/YourApp目录下去，所以我们无法直接获取到assets的绝对路径，因为它们根本就没有。只能把里面的文件复制出来再操作】
     * 获取assets下文件的绝对路径【只是针对html的展示来使用的，比如webview。其他情况不行】
     *
     * @param fileName 文件名,如("文件夹/文件名.后缀"),直接写assets下的文件目录即可。
     * @return
     */
    public String getAssetsPath(String fileName) {
        return "file:///android_asset/" + fileName;
    }

    /**
     * 保存Bitmap位图到本地。
     * 兼容以前。首页保留这个方法。
     *
     * @param bitmap
     * @param path    路径 如：context.getApplicationContext().getFilesDir().getAbsolutePath();
     * @param picName 图片名称，记得要有.png的后缀。【一定要加.png的后缀】
     * @return 返回保存文件
     */
    public File saveBitmap(Bitmap bitmap, String path, String picName) {
        return KFileUtils.getInstance().saveBitmap(bitmap, path, picName);
    }

    /**
     * 复制assets文件到指定目录
     *
     * @param assetsFile assets 里的文件。如("文件夹/文件名.后缀")
     * @param path       指定路径 如：context.getApplicationContext().getFilesDir().getAbsolutePath();
     * @param fileName   文件名
     * @return
     */
    public String copyFileFromAssets(String assetsFile, String path, String fileName) {
        try {
            File fs = new File(path, fileName);
            if (!fs.exists()) {//判断文件是否存在，不存在则创建
                InputStream myInput;
                OutputStream myOutput = new FileOutputStream(fs);
                myInput = am.open(assetsFile);
                byte[] buffer = new byte[1024];
                int length = myInput.read(buffer);
                while (length > 0) {
                    myOutput.write(buffer, 0, length);
                    length = myInput.read(buffer);
                }
                myOutput.flush();
                myInput.close();
                myOutput.close();
            }
            return fs.getAbsolutePath();
        } catch (Exception e) {
            Log.e("test", "assets文件复制错误:\t" + e.getMessage());
            return null;
        }
    }

    /**
     * 从本地加载Bitmap
     *
     * @param pathName  图片完整路径，保存路径和文件后缀名。
     * @param isRGB_565 true 节省内存(推荐)，false不节省内存(效果较好)
     * @param isCache   fixme 是否读取缓存[注意了哦。如果图片剪切了，就不要读取缓存哦。]
     * @return
     */
    public Bitmap getBitmapFromFile(String pathName, boolean isRGB_565, boolean isCache) {
        return getBitmapFromFile(pathName, isRGB_565, 1, isCache);
    }

    public Bitmap getBitmapFromFile(String pathName, boolean isRGB_565) {
        return getBitmapFromFile(pathName, isRGB_565, 1, true);//默认读取缓存
    }

    public Bitmap getBitmapFromFile(String pathName, boolean isRGB_565, int inSampleSize) {
        return getBitmapFromFile(pathName, isRGB_565, inSampleSize, true);//默认读取缓存
    }

    /**
     * 根据路径获取SD卡上面的，文件。
     *
     * @param pathName
     * @param isRGB_565
     * @param inSampleSize SD卡上的文件，添加了 采样率。防止内存溢出。1 是正常。2长和宽缩小到2分之一。4就缩小到4分之一。
     * @param isCache      是否读取缓存
     * @return
     */
    public Bitmap getBitmapFromFile(String pathName, boolean isRGB_565, int inSampleSize, Boolean isCache) {
        String key = pathName + inSampleSize + isRGB_565;
        Bitmap bitmap = getCacleBitmap(key);//获取缓存位图
        if (bitmap != null && !bitmap.isRecycled()) {
            if (isCache) {
                return bitmap;//防止重复加载，浪费内存
            } else {
                bitmap.recycle();//释放
            }
        }
        try {
            if (isRGB_565) {
                bitmap = BitmapFactory.decodeFile(pathName, getOptionsRGB_565(inSampleSize));
            } else {
                bitmap = BitmapFactory.decodeFile(pathName, getOptionsARGB_8888(inSampleSize));
            }
            //解决图片方向显示不正确的问题。
            bitmap = KPictureUtils.INSTANCE.rotateBitmap(bitmap, pathName);
            //保存当前Bitmap
            setCacleBiatmap(key, bitmap);
        } catch (Exception e) {
            Log.e("test", "File流异常" + e.getMessage());
        }
        return bitmap;
    }

    /**
     * @param path 文件路径，SD上的路径。不是assets
     * @return 返回位图的宽或高，谁大返回谁。返回较大的一方。（主要用于计算inSampleSize）
     */
    public int getBitmapSize(String path) {
        /**
         * 重要说明，一个BitmapFactory.Options对应一个Bitmap位图，不能共用【共用了之后，反而占内存】。
         * 必须重新实例化一个Options，才有效果。
         */
        //bitmap所占内存大小计算方式：图片长度 x 图片宽度 x 一个像素点占用的字节数（bitmap占用内存大小和图片本身大小无关）
        BitmapFactory.Options options = new BitmapFactory.Options();
        //为true时不会真正加载图片到内存，仅仅是得到图片尺寸信息，存在Options.outHeight和outWidth和outMimeType中
        options.inJustDecodeBounds = true;
        if (path != null && !path.trim().equals("")) {
            BitmapFactory.decodeFile(path, options);
        }
//        else {
//            BitmapFactory.decodeResource(BaseApplication.getInstance().getResources(), resId, options);
//        }
        return options.outHeight > options.outWidth ? options.outHeight : options.outWidth;//返回位图宽或高。较大的一个。谁大返回谁。
    }

    /**
     * @param path 文件路径，SD上的路径。不是assets
     * @return 返回位图的宽
     */
    public int getBitmapWidth(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        //为true时不会真正加载图片到内存，仅仅是得到图片尺寸信息，存在Options.outHeight和outWidth和outMimeType中
        options.inJustDecodeBounds = true;
        if (path != null && !path.trim().equals("")) {
            BitmapFactory.decodeFile(path, options);
        }
        return options.outWidth;//返回位图宽
    }

    /**
     * @param path 文件路径，SD上的路径。不是assets
     * @return 返回位图的高。
     */
    public int getBitmapHeight(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        //为true时不会真正加载图片到内存，仅仅是得到图片尺寸信息，存在Options.outHeight和outWidth和outMimeType中
        options.inJustDecodeBounds = true;
        if (path != null && !path.trim().equals("")) {
            BitmapFactory.decodeFile(path, options);
        }
        return options.outHeight;//返回位图高
    }

    public BitmapFactory.Options getOptionsRGB_565() {
        return getOptionsRGB_565(1);
    }

    /**
     * @param inSampleSize 采样率。1 是正常。2长和宽缩小到2分之一。4就缩小到4分之一。
     * @return
     */
    public BitmapFactory.Options getOptionsRGB_565(int inSampleSize) {
        /**
         * 重要说明，一个BitmapFactory.Options对应一个Bitmap位图，不能共用【共用了之后，反而占内存】。
         * 必须重新实例化一个Options，才有效果。
         */
        //bitmap所占内存大小计算方式：图片长度 x 图片宽度 x 一个像素点占用的字节数（bitmap占用内存大小和图片本身大小无关）
        BitmapFactory.Options optionsRGB_565 = new BitmapFactory.Options();
        optionsRGB_565.inPurgeable = true;//这个是关键。使用之后，基本不吃内存(内存不足是允许系统自动回收)
        optionsRGB_565.inInputShareable = true;//和inPurgeable一起使用才有效。
        //其实如果不需要 alpha 通道，特别是资源本身为 jpg 格式的情况下，用这个格式RGB_565比较理想。
        optionsRGB_565.inPreferredConfig = Bitmap.Config.RGB_565;//ARGB8888格式的图片(默认)，每像素占用 4 Byte，而 RGB565则是 2 Byte。内存可以直接缩小一半
        optionsRGB_565.inSampleSize = inSampleSize;//如果采样率为 2，那么读出来的图片只有原始图片的 1/4 大小。即长宽缩小一半,10就是缩小到原来的10分之1。一般不使用。图片质量会缩水。
        // options.inBitmap=inBitmap;//重用该bitmap的内存。节省内存。两个bitmap的长度和宽度必须一致才有效。才能重用。尽量不要使用。(会报错Problem decoding into existing bitmap)
        return optionsRGB_565;
    }

    public BitmapFactory.Options getOptionsARGB_8888() {
        return getOptionsARGB_8888(1);
    }

    public BitmapFactory.Options getOptionsARGB_8888(int inSampleSize) {
        /**
         * 一个BitmapFactory.Options对应一个Bitmap位图，不然没有效果。
         */
        BitmapFactory.Options optionsARGB_8888 = new BitmapFactory.Options();
        optionsARGB_8888.inPurgeable = true;
        optionsARGB_8888.inInputShareable = true;
        optionsARGB_8888.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return optionsARGB_8888;
    }

    //设置位图，如("文件夹/文件名.后缀"),如果在assets文件夹共目录下直接写文件名即可。assets支持中文文件夹，如:"中文/nicks2.png"
    //从asstes里面加载图片和从mipmap-nodpi里面加载图片占用内存是一样的。主要还是要看Bitmap的优化。布局文件尽量不要直接引用mipmap-nodpi里的图片。没有对内存进行优化，很占内存。
    //以下方式是最省内存的加载Bitmap方法。
    //isRGB_565 true 节省内存(推荐)，false不节省内存(效果较好)
    public Bitmap getBitmapFromAssets(String fileName, int resID, boolean isRGB_565) {
        String key;
        if (fileName != null && !fileName.equals("")) {
            key = fileName + isRGB_565;
        } else {
            key = "" + resID + isRGB_565;
        }
        //Log.e("test","key：\t"+key);
        Bitmap bitmap = getCacleBitmap(key);//获取缓存位图
        if (bitmap != null) {
            return bitmap;//防止重复加载，浪费内存
        }
        try {
//            Log.e("ui", "开始新建");
            if (fileName != null && !fileName.trim().equals("")) {
                InputStream is = am.open(fileName);
                if (isRGB_565) {
                    bitmap = BitmapFactory.decodeStream(is, null, getOptionsRGB_565());
                } else {
                    bitmap = BitmapFactory.decodeStream(is, null, getOptionsARGB_8888());
                }
                //byte[] b = UtilConnBitimap.InputStreamTOByte(is);
                //bitmap = BitmapFactory.decodeByteArray(b, 0, b.length, getOptions());//使用字节比使用流更省内存。
                //b = null;
                is.close();
                is = null;
            } else {
                if (isRGB_565) {
                    bitmap = BitmapFactory.decodeResource(KApplication.getInstance().getResources(), resID, getOptionsRGB_565());
                } else {
                    bitmap = BitmapFactory.decodeResource(KApplication.getInstance().getResources(), resID, getOptionsARGB_8888());
                }

            }
//            view.setBackgroundDrawable(bitmapDrawable);//设置背景图片，背景图片会拉升和控件同等大小。即这个方法，背景图片始终和控件同等大小。所以只要对控件进行适配即可。图片保持原图。
//            bitmap=UtilProportion.getInstance(activity).adapterBitmap(bitmap);//对图片进行统一适配。因为View图片时是放在背景里，背景里的图片不需要做适配。
//            Log.e("ui", "大小:\t" + bitmap.getByteCount() / 1024 + "KB" + "\t宽度:\t" + bitmap.getWidth() + "\t高度:\t" + bitmap.getHeight() + "\tconfig:\t" + bitmap.getConfig());
            //保存当前Bitmap
            setCacleBiatmap(key, bitmap);
        } catch (Exception e) {
            Log.e("ui", "assets流异常" + e.getMessage());
        }
        return bitmap;
    }

    /**
     * 这样获取的九文件，基本不占内存
     * <p>
     * 设置九文件图片：view.setBackground(ninePatchDrawable);经查阅后才知道.9只针对background来进行拉伸。不管是九文件还是其他图片。background都是对图片拉伸到和控件同等大小。
     *
     * @param resID drawable-nodpi文件夹下的九文件ID
     * @return NinePatchDrawable九文件，无则返回null
     */
    public NinePatchDrawable getNinePatchDrawable(int resID, boolean isRGB_565) {
        //九文件必须放在drawable-nodpi等系统文件夹下才有效。放在assets里是没有伸拉效果的，切记！
        Bitmap bitmap = getBitmapFromAssets(null, resID, isRGB_565);
        //确认Bitmap是合法的NinePatch文件
        if (NinePatch.isNinePatchChunk(bitmap.getNinePatchChunk())) {
            //Log.e("test","我是合法九文件");
            NinePatchDrawable ninePatchDrawable = new NinePatchDrawable(KApplication.getInstance().getResources(), bitmap, bitmap.getNinePatchChunk(), new Rect(), null);
            return ninePatchDrawable;
        } else {
            bitmap.recycle();
            bitmap = null;
            System.gc();
            return null;
        }
    }

    /**
     * 设置背景图片(控件选中样式，自己手动调用该方法。选中监听事件没有)
     *
     * @param view     控件
     * @param fileName assets文件夹下背景图片名称
     * @param resID    如果fileName为null，才有效。
     */
    public void setBackGraound(final View view, final String fileName, final int resID, boolean isRGB_565) {
        setBackGraound(view, fileName, resID, view.getLayoutParams().width, view.getLayoutParams().height, false, isRGB_565);
    }

    /**
     * @param isRepeatAdapter 适配强制重新适配，true每次都重新适配。false只适配一次【默认就是false】
     */
    public void setBackGraound(final View view, final String fileName, final int resID, boolean isRepeatAdapter, boolean isRGB_565) {
        setBackGraound(view, fileName, resID, view.getLayoutParams().width, view.getLayoutParams().height, isRepeatAdapter, isRGB_565);
    }

    public void setBackGraound(final View view, final String fileName, final int resID, int width, int heigh, boolean isRepeatAdapter, boolean isRGB_565) {
        setBackGraound(view, fileName, resID, width, heigh, isRepeatAdapter, isRGB_565, true);//最后一个参数。默认都做适配
    }

    //最后一个参数 isadapter 是否做适配，true做适配。false不做适配
    public void setBackGraound(final View view, final String fileName, final int resID, int width, int heigh, boolean isRepeatAdapter, boolean isRGB_565, boolean isadapter) {
        if (view == null) {
            return;
        }
        //不要再线程中加载，会延迟。
        Bitmap bitmap = getBitmapFromAssets(fileName, resID, isRGB_565);
        BitmapDrawable bitmapDrawable = new BitmapDrawable(bitmap);
        view.setBackgroundDrawable(bitmapDrawable);//设置背景图片，背景图片会拉伸到控件同等大小。即这个方法，背景图片始终和控件同等大小。所以只要对控件进行适配即可。图片保持原图。

        if (!isadapter) {
            return;//不做适配。
        } else {
            //做适配
            //isRepeatAdapter true没次都重新适配，以图片尺寸为标准。不能以自身。否则尺寸会出错。
            if (width <= 0 || isRepeatAdapter) {
                width = bitmap.getWidth();
            }
            if (heigh <= 0 || isRepeatAdapter) {
                heigh = bitmap.getHeight();
            }
            adapterView(bitmap, view, width, heigh, isRepeatAdapter);
        }
        bitmapDrawable = null;
    }

    //针对ImageView的Src
    public void setImageBitmap(final Class clazz, final ImageView view, final String fileName, final int resID, int width, int heigh, boolean isRGB_565) {
        if (view == null) {
            return;
        }
        //不要再线程中加载，会延迟。
        Bitmap bitmap = getBitmapFromAssets(fileName, resID, isRGB_565);
        view.setImageBitmap(bitmap);

        if (width <= 0) {
            width = bitmap.getWidth();
        }
        if (heigh <= 0) {
            heigh = bitmap.getHeight();
        }

        adapterView(bitmap, view, width, heigh, false);
    }

    //UI适配
    private void adapterView(Bitmap bitmap, final View view, int width, int heigh, boolean isRepeatAdapter) {

        if (!isRepeatAdapter && mapViews.containsKey(view.hashCode() + "") && mapViews.get(view.hashCode() + "").equals(view)) {
//            Log.e("test", "重复适配:\t" + view.hashCode());
            return;//重复适配。防止控件重复适配，浪费时间
        }

        mapViews.put(view.hashCode() + "", view);
//        Log.e("test", "适配:\t" + view.hashCode());
        //控件大小与图片大小一致
        ViewGroup.LayoutParams laParams = view
                .getLayoutParams();
        if (width <= 0) {
            laParams.width = bitmap.getWidth();
        } else {
            laParams.width = width;//自定义宽度
        }
        if (heigh <= 0) {
            laParams.height = bitmap.getHeight();//自定义高度
        } else {
            laParams.height = heigh;
        }
        view.setLayoutParams(laParams);
        //屏幕适配(调用UtilAssets就不需要再调用UtilProportion),只要对文字没有适配，基本都可以使用adapterView()
        if (view instanceof TextView) {
            KProportionUtils.getInstance().adapterTextView((TextView) view);//button也能转化成textView。textView是button和eidtText的父类。子类是可以转化成父类的。
        } else if (view instanceof GridView) {
            KProportionUtils.getInstance().adapterGridview((GridView) view);
        } else {
            KProportionUtils.getInstance().adapterView(view);
        }
        bitmap = null;
        System.gc();
    }

    private View.OnFocusChangeListener oldOnFocusChangeListener;//记录旧焦点的聚焦事件,防止旧焦点失灵。

    /**
     * 设置聚焦图片
     *
     * @param view      控件(所有的控件包括布局都继承View)
     * @param falseName 非聚焦图片名称(正常图片)
     * @param trueName  聚焦图片名称
     * @param falseID   非聚焦图片ID,falseName为null才有效
     * @param trueID    聚焦图片ID,trueName为null才有效
     * @param Bfaouse   是否聚焦
     */
    public void setOnFocusChanged(final View view, final String falseName, final String trueName, final int falseID, final int trueID, Boolean Bfaouse, final boolean isRGB_565) {
        view.setFocusable(true);//是否具备聚焦能力
        if (Bfaouse) {
            view.requestFocus();
            setBackGraound(view, trueName, trueID, isRGB_565);
        } else {
            setBackGraound(view, falseName, falseID, isRGB_565);//正常样式
        }
        //保存原有的聚焦事件,失去焦点时要恢复原有聚焦事件
        oldOnFocusChangeListener = view.getOnFocusChangeListener();
        //Log.e("ui", "聚焦变化初始化:\t" + view.getId());
        view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                //Log.e("ui", "聚焦:" + hasFocus);
                if (hasFocus) {
                    setBackGraound(view, trueName, trueID, isRGB_565);
                } else {
                    //Log.e("test","失去焦点：:\t"+falseID);
                    setBackGraound(view, falseName, falseID, isRGB_565);
                }
                //集成原有聚焦事件，防止原有聚焦事件失灵。
                if (oldOnFocusChangeListener != null) {
                    oldOnFocusChangeListener.onFocusChange(v, hasFocus);
                }
            }
        });
    }


    /**
     * 设置按下图片样式(手指按下不会聚焦，聚焦和手指是否按下没有直接影响)
     *
     * @param view            控件(所有的控件包括布局都继承View)
     * @param gennerName      手指离开图片样式(一般样式)
     * @param pressName       手机按下图片样式
     * @param gennerID        手指离开图片资源ID,gennerName为null有效
     * @param pressID         手指按下图片ID,pressName为null有效
     * @param isRGB_565
     * @param isRepeatAdapter true 每次都适配。false只适配一次。
     * @param isAdapter       是否做适配
     */
    public void setOnTouch(final View view, final String gennerName, final String pressName, final int gennerID, final int pressID, boolean isRGB_565, final boolean isRepeatAdapter, final boolean isAdapter) {
        setOnTouch(view, gennerName, pressName, gennerID, pressID, null, isRGB_565, isRepeatAdapter, isAdapter);
    }

    //兼容原有触摸事件【放心，和点击事件不会冲突】
    public void setOnTouch(final View view, final String gennerName, final String pressName, final int gennerID, final int pressID, final View.OnTouchListener onTouchListener, final boolean isRGB_565, final boolean isRepeatAdapter, final boolean isAdapter) {
        //setBackGraound(clazz, view, gennerName, gennerID, isRGB_565);//正常样式
        setBackGraound(view, gennerName, gennerID, view.getLayoutParams().width, view.getLayoutParams().height, isRepeatAdapter, isRGB_565, isAdapter);
        view.setClickable(true);//是否具备点击能力,必须设置否则无效
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //Log.e("ui","事件:"+event.getAction());
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN://按下
//                        Log.e("ui", "按下");
                        //setBackGraound(clazz, view, pressName, pressID, isRGB_565);
                        setBackGraound(view, pressName, pressID, view.getLayoutParams().width, view.getLayoutParams().height, isRepeatAdapter, isRGB_565, isAdapter);
                        break;
                    case MotionEvent.ACTION_UP://离开
//                        Log.e("ui", "离开");
                        //setBackGraound(clazz, view, gennerName, gennerID, isRGB_565);
                        setBackGraound(view, gennerName, gennerID, view.getLayoutParams().width, view.getLayoutParams().height, isRepeatAdapter, isRGB_565, isAdapter);
                        break;
                    default:
                        break;
                }
                if (onTouchListener != null) {//防止原有触摸事件无效。兼容原有触摸事件。
                    return onTouchListener.onTouch(v, event);
                }
                return false;
            }
        });
    }

    /**
     * 获取帧动画
     *
     * @param clazz    getClass()
     * @param view     控件
     * @param fileName 文件名。不包括数字标志和后缀。如"star_0.png",传 "star_"
     * @param size     帧动画个数。下标从0开始。size就是最后一个数。如"0,1,2,3",就传3
     * @param duration 帧动画时间，单位毫秒。1000等于一秒。
     * @param BAssets  true图片在asstes文件夹。false图片在mipmap文件夹。
     * @return
     */
    public AnimationDrawable getBackGraoundAAnimationDrawable(final Class clazz, final View view, String fileName, int size, int duration, Boolean BAssets, boolean isRGB_565) {
        AnimationDrawable anim = new AnimationDrawable();
        for (int i = 0; i <= size; i++) {
//            Log.e("ui", " " + (fileName + i));
            Bitmap bitmap = null;
            if (BAssets) {
                fileName = fileName + i + ".png";
                bitmap = getBitmapFromAssets(fileName, 0, isRGB_565);//资源在asstes文件下
            } else {
                int id = KApplication.getInstance().getResources().getIdentifier(fileName + i, "mipmap", KApplication.getInstance().getPackageName());//图片资源在mipmap下面
                bitmap = getBitmapFromAssets(null, id, isRGB_565);
            }

            Drawable drawable = new BitmapDrawable(bitmap);
            anim.addFrame(drawable, duration);
        }
        //anim.setOneShot(false);//是否只循环一次
        //view.setBackground(anim);
        view.setBackgroundDrawable(anim);
        return (AnimationDrawable) view.getBackground();
    }

    //获取assets下文本，参数("文件夹/文件名.后缀")
    public String getStringFromAssets(String fileName) {
        //将json数据变成字符串
        StringBuilder stringBuilder = new StringBuilder();
        try {
            //通过管理器打开文件并读取
            BufferedReader bf = new BufferedReader(new InputStreamReader(
                    am.open(fileName)));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            Log.e("test", "asset json");
        }
        return stringBuilder.toString();
    }

    /**
     * 销毁所有位图,释放内存
     */
    public void recycleAll() {
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            if (entry.getValue() == null) {
                continue;
            }
            Bitmap bitmap = (Bitmap) entry.getValue();
            if (bitmap != null && !bitmap.isRecycled()) {
                // 回收并且置为null
                bitmap.recycle();
                bitmap = null;
            }
        }
        map.clear();
        System.gc();//回收无用的对象,释放内存
    }
}
