package cn.android.support.v7.lib.eep.kera.base;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import cn.android.support.v7.lib.eep.kera.utils.KProportionUtils;


/**
 * Created by  彭治铭 on 2017/9/10.
 */
//必须在AndroidManifest.xml中application指明
//<application android:name=".base.BaseApplication">
//配置文件声明之后，才会调用onCreate()等什么周期。
// fixme Application就用java写，之前用kotlin写。结果发生错误。找不到上下文。
public class KApplication extends Application {

    private static KApplication sInstance;

    //通过反射获取ActivityThread【隐藏类】
    private static Object getActivityThread() {
        try {
            final Class<?> clz = Class.forName("android.app.ActivityThread");
            final Method method = clz.getDeclaredMethod("currentActivityThread");
            method.setAccessible(true);
            final Object activityThread = method.invoke(null);
            return activityThread;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //初始化
    public static KApplication getInstance() {
        if (sInstance == null) {
            //如果配置文件没有声明，也没有手动初始化。则通过反射自动初始化。【反射是最后的手段，效率不高】
            //通过反射，手动获取上下文。
            final Object activityThread = getActivityThread();
            if (null != activityThread) {
                try {
                    final Method getApplication = activityThread.getClass().getDeclaredMethod("getApplication");
                    getApplication.setAccessible(true);
                    Context applicationContext = (Context) getApplication.invoke(activityThread);
                    setsInstance(applicationContext);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return sInstance;
    }

    //如果没有在配置文件中配置，则需要手动调用以下方法，手动初始化BaseApplication
    //不会调用onCreate()等什么周期
    //BaseApplication.setsInstance(getApplication());
    public static void setsInstance(Context application) {
        if (sInstance == null) {
            sInstance = new KApplication();
            //统一上下文
            sInstance.attachBaseContext(application);
        }
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        sInstance = this;
    }

    //判断是否第一次启动，true 首次启动，false不是
    public boolean isFirstOpen(Context context) {
        String key = "versionCode" + getVersionCode();//确保每个版本都唯一
        SharedPreferences preferences = context.getSharedPreferences(
                "application", 0);// 0是默认模式
        Boolean bool = preferences.getBoolean(key, true);
        if (bool) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(key, false);
            editor.commit();
        }
        return bool;
    }

    //当前应用的版本名称
    public String getVersionName() {
        try {
            PackageManager manager = this.getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            String version = info.versionName;
            return version;
        } catch (Exception e) {
            Log.e("test", "获取应用版本号异常:\t" + e.getMessage());
        }
        return null;
    }

    //当前应用的版本号
    public int getVersionCode() {
        try {
            PackageManager manager = this.getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            return info.versionCode;
        } catch (Exception e) {
            Log.e("test", "获取应用版本号异常:\t" + e.getMessage());
        }
        return 1;
    }

    //获取SDK的版本号，23是6.0  21是5.0   14是4.0
    public int getSDK_INT() {
        return Build.VERSION.SDK_INT;
    }

    //退出程序
    public void exit() {
        try {
            KActivityManager.getInstance().finishAllActivity();
            android.os.Process.killProcess(android.os.Process.myPid());
        } catch (Exception e) {
            Log.e("test", "退出异常:\t" + e.getMessage());
        }
    }

    //跳转到桌面
    public void goHome() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//防止报错
        startActivity(intent);
    }


    //键值【作用范围应用全局】
    private Map _objectContainer = new HashMap();

    //存储键值
    public void put(Object key, Object value) {
        _objectContainer.put(key, value);
    }

    //获取键值
    public Object get(Object key) {
        return _objectContainer.get(key);
    }

    //移出键值
    public void remove(Object key) {
        _objectContainer.remove(key);
    }

    //清楚所有键值
    public void clear() {
        _objectContainer.clear();
    }

    /**
     * 获取当前屏幕截图
     *
     * @param activity
     * @param hasStatus true 保护状态栏，false不包含状态栏
     * @return
     */
    public Bitmap snapShotWindow(Activity activity, Boolean hasStatus) {
        View view = activity.getWindow().getDecorView();//最顶层控件就是DecorView
        view.setDrawingCacheEnabled(true);//==========================重点掌握
        view.buildDrawingCache();//=================================
        Bitmap bmp = view.getDrawingCache();//========================
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);//获取View的矩形
        int statusBarHeight = frame.top;

        int width = (int) KProportionUtils.realWidthPixels;
        int height = (int) KProportionUtils.realHeightPixels;
        Bitmap bp = null;
        if (hasStatus) {
            //包含状态栏
            bp = Bitmap.createBitmap(bmp, 0, 0, width, height);
        } else {
            //不包含状态栏
            bp = Bitmap.createBitmap(bmp, 0, statusBarHeight, width, height
                    - statusBarHeight);//对原 Bitmap进行截取，一定要新建Bitmap位图，尽量不要对原有的Bitmap进行操作。
        }
        view.destroyDrawingCache();//=================================要关闭。很好性能。
        return bp;

    }

    //获取当前View截图
    public Bitmap snapShotView(View view) {
        view.setDrawingCacheEnabled(true);//==========================重点掌握
        view.buildDrawingCache();
        Bitmap bmp = view.getDrawingCache();
        int width = view.getWidth() > view.getLayoutParams().width ? view.getWidth() : view.getLayoutParams().width;
        int height = view.getHeight() > view.getLayoutParams().height ? view.getHeight() : view.getLayoutParams().height;
        Bitmap bp = null;
        bp = Bitmap.createBitmap(bmp, 0, 0, width, height);//对原 Bitmap进行截取，一定要新建Bitmap位图，尽量不要对原有的Bitmap进行操作。
        view.destroyDrawingCache();//=================================要关闭。很好性能。
        return bp;
    }

    /**
     * 设置状态栏透明
     *
     * @param window getWindow()
     */
    public void setStatusBarTransparent(Window window) {
        if (Build.VERSION.SDK_INT < 19) {//4.4
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);//全屏,有效。因为4.4以下状态栏透明设置无效，奇丑无比，所以设置全屏。
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);//状态栏背景透明(和应用背景一样4.4及以上才有效,测试真机，亲测有效)
        //设置状态栏背景透明【亲测有效】
        try {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);   //去除半透明状态栏
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);  //一般配合fitsSystemWindows()使用, 或者在根部局加上属性android:fitsSystemWindows="true", 使根部局全屏显示
            if (Build.VERSION.SDK_INT >= 21) {
                window.setStatusBarColor(Color.TRANSPARENT);
            }
            ViewPager.DecorView decordView = (ViewPager.DecorView) window.getDecorView();     //获取DecorView实例
            Field field = ViewPager.DecorView.class.getDeclaredField("mSemiTransparentStatusBarColor");  //获取特定的成员变量
            field.setAccessible(true);   //设置对此属性的可访问性
            field.setInt(decordView, Color.TRANSPARENT);  //修改属性值
        } catch (Exception e) {
            //Log.e("test", "状态栏透明设置异常:\t" + e.getMessage());
        }
    }

    //fixme 状态栏是否为黑色,true黑色，false白色(浅色)（系统默认的这个颜色）
    public boolean darkmode = true;//fixme 在此默认设置为黑色（深色）。

    //这个是全局引用了，决定了全局状态栏字体的颜色
    public void setDarkmode(boolean darkmode) {
        this.darkmode = darkmode;
    }

    //调用案例：BaseApplication.getInstance().setStatusBarDrak(activity?.window, isDarkMode())

    /**
     * 设置状态栏字体颜色
     *
     * @param window
     * @param darkmode
     */
    //一般需要在 super.onCreate(savedInstanceState); 方法之后，调用才有效。
    //放到BaseApplication里面。方便全局调用。Activity和Dailog都可以调用
    //状态栏字体颜色，true 黑色。false 白色【一般默认就是白色,所以白色一般不需要調用】。
    // 如果要設置黑色狀態欄字體，在子類中調用setStatusBarDrak(true);即可。setContentView();之前之后調用都可以,最好在之前調用。
    public void setStatusBarDrak(Window window, boolean darkmode) {
        setAndroidStatusBarkMode(window, darkmode);
        setMiuiStatusBarDarkMode(window, darkmode);
        setFlyMeStatusBarDarkMode(window, darkmode);
    }

    //对于android6.0及以上（不是所有的都可以，部分可能無效。）,但是小米魅族不适配
    //测试发现，只对android api 24及以上才有效。即真实只对7.0及以上才有效。
    //字体颜色，true 黑色。false 白色。
    //亲测，对android 7.0及以上有效，api 24
    private void setAndroidStatusBarkMode(Window window, boolean darkmode) {
        if (Build.VERSION.SDK_INT >= 19) {//19是4.4
            if (darkmode) {
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);//深色，一般为黑色
            } else {
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);//浅色，一般为白色
            }
        }
    }

    //修改小米状态栏字体颜色【只针对miui6以上有效】。true 黑色。false 白色。
    // 亲测，对小米有效
    private boolean setMiuiStatusBarDarkMode(Window window, boolean darkmode) {
        Class<? extends Window> clazz = window.getClass();
        try {
            int darkModeFlag = 0;
            Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
            Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
            darkModeFlag = field.getInt(layoutParams);
            Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
            extraFlagField.invoke(window, darkmode ? darkModeFlag : 0, darkModeFlag);
            return true;
        } catch (Exception e) {
            //Log.e("test", "Miui状态栏字体颜色修改失败:\t" + e.getMessage());
        }
        return false;
    }

    //改变魅族的状态栏字体为黑色，要求FlyMe4以上,true 黑色。false 白色。
    //亲测，对魅族有效。
    private void setFlyMeStatusBarDarkMode(Window window, boolean darkmode) {
        WindowManager.LayoutParams lp = window.getAttributes();
        try {
            Class<?> instance = Class.forName("android.view.WindowManager$LayoutParams");
            int value = instance.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON").getInt(lp);
            Field field = instance.getDeclaredField("meizuFlags");
            field.setAccessible(true);
            int origin = field.getInt(lp);
            if (darkmode) {
                field.set(lp, origin | value);
            } else {
                field.set(lp, (~value) & origin);
            }
        } catch (Exception e) {
            //Log.e("test", "魅族状态栏字体颜色修改失败:\t" + e.getMessage());
        }
    }

}
