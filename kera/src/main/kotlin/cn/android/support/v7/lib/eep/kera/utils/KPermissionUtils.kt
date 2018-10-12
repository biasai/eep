package cn.android.support.v7.lib.eep.kera.utils

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.Camera
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.view.View
import android.widget.TextView
import cn.android.support.v7.lib.eep.kera.R
import java.io.File

//   fixme 在Activity里面调用一下方法。回调才有效。[不过我已经在BaseActivity中添加以下方法了，如果继承了BaseActvity就不用再写了]
//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        PermissionUtils.onRequestPermissionsResult(getActivity(),requestCode, permissions, grantResults)
//    }

//  调用案例（虽然有两个参数，但只要打出第一个参数的第一个字母，后面会自动提示出来的。现在已经改成只有一个参数了。）
//        PermissionUtils.requestPermissionsStorage(this){
//            Log.e("test","权限是否允许:\t"+it)
//            if(it){
//                //权限申请成功
//            }else{
//                PermissionUtils.showFailure(getActivity())//显示默认失败界面
//            }
//        }

/**
 * 权限相关
 */
object KPermissionUtils {

    //fixme 注意：申请权限之前，一定要在清单里注册一下。没有注册权限，是无法申请权限的。没有注册只会返回失败，不会弹出权限询问框。
    //权限数组
    val DANGEROUS_PERMISSION = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
    val DANGEROUS_PERMISSION_CAMERA = arrayOf(Manifest.permission.CAMERA)//相机
    val DANGEROUS_PERMISSION_STORAGE = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)//SD卡读写
    val DANGEROUS_PERMISSION_RECORD = arrayOf(Manifest.permission.RECORD_AUDIO)//录音
    val DANGEROUS_PERMISSION_READ_PHONE_STATE = arrayOf(Manifest.permission.READ_PHONE_STATE)//用于调用 JNI ,及读取设备的权限，如手机设备号
    // requestCode 权限请求码[fixme 数字标志为 0~6万，负数会奔溃，高于7万也会奔溃。]
    val READ_PHONE_STATE_REQUEST_CODE = 3820//权限请求标志

    var perMissionType = 3821//当前权限申请的标识。默认为相机拍照。
    var perMissionTypeALL = 3822//以下所有权限的集合。
    var perMissionTypeCamera = 3823//相机拍照权限申请标识。图片,相机拍照和拍摄都需要相机权限。
    var perMissionTypeVideo = 3824//相机拍摄权限申请标识。视频
    var perMissionTypeStorage = 3825//SD卡权限申请标识。
    var perMissionTypeRecording = 3826//录音权限申请标识。
    var perMissionTypeReadPhoneState = 3827//手机状态权限申请，如手机设备号等。

    //fixme 动态权限申请，SDK必须大于等于23，且targetSdkVersion也必须大于等于23才有效。
    //低于23的版本，权限默认就是开启的。
    fun isVersion23(activity: Activity): Boolean {
        if (Build.VERSION.SDK_INT >= 23 && activity.getApplicationInfo().targetSdkVersion >= 23) {
            return true
        }
        return false
    }
    //fixme 注意，如果该权限已经申请成功。再次调用权限申请，不会弹出权限申请窗口。会直接返回成功回调。

    //一般的权限申请，手机信息，存储卡权限。相机，录音。
    fun requestPermissionsALL(activity: Activity, onRequestPermissionsResult2: ((isAllow: Boolean) -> Unit)? = null): Boolean {
        perMissionType = perMissionTypeALL
        if (isVersion23(activity)) {//Android6.0权限申请
            // 权限已经授予,直接初始化
            if (ActivityCompat.checkSelfPermission(activity,
                            Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(activity,
                            Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(activity,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(activity,
                            Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(activity,
                            Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                onRequestPermissionsResult2?.let {
                    this.onRequestPermissionsResult = it
                    // 申请权限（回调不为空，才会申请）
                    ActivityCompat.requestPermissions(activity, DANGEROUS_PERMISSION, READ_PHONE_STATE_REQUEST_CODE)
                }
                return false
            } else {
                onRequestPermissionsResult2?.let {
                    it(true)
                }
                return true
            }
        } else {
            onRequestPermissionsResult2?.let {
                it(true)
            }
            return true
        }
    }

    //相机权限请求，true通过，false 会弹出权限请求窗口。【6.0(api 23)以上才需要】
    fun requestPermissionsCamera(activity: Activity, onRequestPermissionsResult2: ((isAllow: Boolean) -> Unit)? = null): Boolean {
        perMissionType = perMissionTypeCamera
        if (isVersion23(activity)) {
            if (ActivityCompat.checkSelfPermission(activity,
                            Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {//判断权限是否已授权。没有授权再发生请求回调。已经授权，就不再申请。
                onRequestPermissionsResult2?.let {
                    this.onRequestPermissionsResult = it
                    // 申请权限【sdk6.0即以上才有效。targetSdkVersion23及以上才有效。】
                    ActivityCompat.requestPermissions(activity, DANGEROUS_PERMISSION_CAMERA, READ_PHONE_STATE_REQUEST_CODE)
                }
                return false
            } else {
                onRequestPermissionsResult2?.let {
                    it(true)
                }
                return true
            }
        } else {
            //6.0以下。第一次会弹出系统询问权限框。后面就不会弹了。
            var mCamera: Camera? = null
            try {
                mCamera = Camera.open()//第一次会弹出系统询问权限框【是线程阻塞的。 询问弹框消失了才会继续向下执行。】
                val mParameters = mCamera!!.parameters
                mCamera.parameters = mParameters
            } catch (e: Exception) {
                onRequestPermissionsResult2?.let {
                    it(false)
                }
                return false
            }
            if (mCamera != null) {
                try {
                    mCamera.release()
                } catch (e: Exception) {
                    onRequestPermissionsResult2?.let {
                        it(false)
                    }
                    return false
                }
            }
            onRequestPermissionsResult2?.let {
                it(true)
            }
            return true
        }
    }


    //SD卡权限申请
    fun requestPermissionsStorage(activity: Activity, onRequestPermissionsResult2: ((isAllow: Boolean) -> Unit)? = null): Boolean {
        perMissionType = perMissionTypeStorage//当前SD卡申请标志
        if (isVersion23(activity)) {
            if (ActivityCompat.checkSelfPermission(activity,
                            Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(activity,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                onRequestPermissionsResult2?.let {
                    this.onRequestPermissionsResult = it
                    // 申请权限(回调不为空，才申请)
                    ActivityCompat.requestPermissions(activity, DANGEROUS_PERMISSION_STORAGE, READ_PHONE_STATE_REQUEST_CODE)
                }
                return false
            } else {
                onRequestPermissionsResult2?.let {
                    it(true)
                }
                return true
            }
        } else {
            //可能返回内置存储卡，也可能返回外置存储卡。如：/storage/emulated/0 注意路径末尾是不带"/"的。
            try {
                var path = android.os.Environment.getExternalStorageDirectory().path
                path = path + "/permisonn000test554861655468.txt"
                var file = File(path)
                if (!file.exists()) {
                    file.createNewFile()
                }
                if (file.exists()) {
                    file.delete()
                }
                onRequestPermissionsResult2?.let {
                    it(true)
                }
                return true
            } catch (e: Exception) {
                onRequestPermissionsResult2?.let {
                    it(false)
                }
                return false
            }

        }
    }

    //录音权限申请
    fun requestPermissionsRecording(activity: Activity, onRequestPermissionsResult2: ((isAllow: Boolean) -> Unit)? = null): Boolean {
        perMissionType = perMissionTypeRecording
        if (isVersion23(activity)) {
            if (ActivityCompat.checkSelfPermission(activity,
                            Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                onRequestPermissionsResult2?.let {
                    this.onRequestPermissionsResult = it
                    // 申请权限
                    ActivityCompat.requestPermissions(activity, DANGEROUS_PERMISSION_RECORD, READ_PHONE_STATE_REQUEST_CODE)
                }
                return false
            } else {
                onRequestPermissionsResult2?.let {
                    it(true)
                }
                return true
            }
        } else {
            onRequestPermissionsResult2?.let {
                it(true)
            }
            return true
        }
    }

    //手机状态权限申请，如手机设备号等。
    fun requestPermissionsReadPhoneState(activity: Activity, onRequestPermissionsResult2: ((isAllow: Boolean) -> Unit)? = null): Boolean {
        perMissionType = perMissionTypeReadPhoneState
        if (isVersion23(activity)) {
            if (ActivityCompat.checkSelfPermission(activity,
                            Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                onRequestPermissionsResult2?.let {
                    this.onRequestPermissionsResult = it
                    // 申请权限
                    ActivityCompat.requestPermissions(activity, DANGEROUS_PERMISSION_READ_PHONE_STATE, READ_PHONE_STATE_REQUEST_CODE)
                }
                return false
            } else {
                onRequestPermissionsResult2?.let {
                    it(true)
                }
                return true
            }
        } else {
            onRequestPermissionsResult2?.let {
                it(true)
            }
            return true
        }
    }

    //判断权限数组是否全部都授权，有一个没有授权都返回false。全部授权才返回true
    fun judgePermission(grantResults: IntArray): Boolean {
        var b = true
        for (i in grantResults.indices) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                b = false//没有授权
            }
        }
        return b
    }

    fun getContent(activity: Activity): View {
        return activity.getWindow().getDecorView().findViewById(android.R.id.content)
    }

    //消息提示
    fun Snackbarmake(activity: Activity, info: String) {
        val snackbar = Snackbar.make(getContent(activity), info, Snackbar.LENGTH_LONG)
                .setAction("立即设置") {
                    val packageURI = Uri.parse("package:" + activity.getPackageName())
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    activity.startActivity(intent)//跳转权限设置界面。基本上通用。小米是肯定行的。android6.0基本都可以。
                }
        val snackView = snackbar.view
        //int textSize= (int) (UtilProportion.getInstance(this).adapterInt(24) * UtilProportion.getInstance(this).getTextProportion()/ UtilProportion.getInstance(this).getDensity());
        snackView.setBackgroundColor(Color.parseColor("#61A465"))//浅绿色背景
        //snackView.setBackgroundResource(R.drawable.shape_drawable_snackbar);
        val snackbar_text = snackView.findViewById<View>(R.id.snackbar_text) as TextView
        snackbar_text.setTextColor(Color.parseColor("#ffffff"))//设置通知文本的颜色，白色
        //snackbar_text.setTextSize(textSize);
        val snackbar_action = snackView.findViewById<View>(R.id.snackbar_action) as TextView
        snackbar_action.setTextColor(Color.parseColor("#FF3B80"))//点击文本的颜色,绯红
        //snackbar_action.setTextSize(textSize);
        //snackbar_action.setBackground(null);
        snackbar_action.setBackgroundDrawable(null)
        snackbar.show()
    }

    var onRequestPermissionsResult: ((isAllow: Boolean) -> Unit)? = null
    //权限请求成功回调，返回参数为属于什么类型的权限申请。
    fun onRequestPermissionsResult(isAllow: Boolean) {
        //isAllow true权限允许，false权限禁止
        onRequestPermissionsResult?.let {
            it(isAllow)
            onRequestPermissionsResult = null
        }
    }

    //权限申请失败时，显示的界面，需要手动调用。如果不喜欢这个界面。需要自己去额外实现。
    fun showFailure(activity: Activity, perMissionType: Int = KPermissionUtils.perMissionType) {
        var info: String? = null
        if (perMissionType == perMissionTypeCamera || perMissionType == perMissionTypeVideo) {
            info = "需要开启相机权限"
        } else if (perMissionType == perMissionTypeStorage) {
            info = "需要开启手机存储权限"
        } else if (perMissionType == perMissionTypeRecording) {
            info = "需要开启手机录音权限"
        } else if (perMissionType == perMissionTypeReadPhoneState) {
            info = "需要开启手机信息权限"
        } else if (perMissionType == perMissionTypeALL) {
            info = "权限申请失败"
        }
        if (info != null) {
            Snackbarmake(activity, info)
        }
    }

    //权限申请结果
    fun onRequestPermissionsResult(activity: Activity, requestCode: Int, permissions: Array<String>, grantResults: IntArray) {//grantResults就是之前申请的权限数组。一模一样。权限的数量和顺序都一模一样。
        if (requestCode == READ_PHONE_STATE_REQUEST_CODE && grantResults.size > 0) {//一定要判断一下grantResults是否大于0，防止他脑抽。
            //if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {//PackageManager.PERMISSION_GRANTED 授权成功
            if (judgePermission(grantResults)) {
                // 权限授予成功
                onRequestPermissionsResult(true)
            } else {
                onRequestPermissionsResult(false)
            }
        }
    }

}