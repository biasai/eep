package cn.android.support.v7.lib.eep.kera.base;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import java.util.Stack;

/**
 * Created by  彭治铭 on 2017/9/10.
 */

public class KActivityManager {
    private static KActivityManager sInstance;
    private Stack<Activity> mActivityStack;

    private KActivityManager() {
    }

    //初始化
    public static KActivityManager getInstance() {
        if (null == sInstance) {
            sInstance = new KActivityManager();
        }
        return sInstance;
    }

    //启动Activity
    public static void startActivity(Context cxt, Class<?> clazz) {
        Intent intent = new Intent(cxt, clazz);
        cxt.startActivity(intent);
    }


    //入栈
    public void pushActivity(Activity activity) {
        if (null == mActivityStack) {
            mActivityStack = new Stack<Activity>();
        }
        mActivityStack.push(activity);
    }

    // 当前Activity（栈中最后一个压入的）
    public Activity getStackTopActivity() {
        return mActivityStack.lastElement();
    }

    //结束当前的Activity
    public void finishActivity() {
        finishActivity(getStackTopActivity());
    }

    //结束指定的Activity
    public void finishActivity(Activity activity) {
        if (null != activity) {
            mActivityStack.remove(activity);
            activity.finish();
            activity = null;
        }
    }

    //结束指定类名的Activity
    public void finishActivity(Class<?> clazz) {
        for (Activity activity : mActivityStack) {
            if (activity.getClass().equals(clazz)) {
                finishActivity(activity);
            }
        }
    }

    //销毁所有Activity
    public void finishAllActivity() {
        for (Activity activity : mActivityStack) {
            if (null != activity) {
                activity.finish();
            }
        }
        mActivityStack.clear();
    }
}
