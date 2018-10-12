package cn.android.support.v7.lib.eep.kera.utils

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.util.Log
import java.io.*

//fixme 注意调用前，需要在Activity中添加以下方法。[不过我已经在BaseActivity中添加以下方法了，如果继承了BaseActvity就不用再写了]
//override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
//    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//    PermissionUtils.onRequestPermissionsResult(getActivity(), requestCode, permissions, grantResults)
//}
//
//override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//    super.onActivityResult(requestCode, resultCode, data)
//    PictureUtils.onActivityResult(this, requestCode, resultCode, data)
//}

/**
 *相册，相机，视频，剪切
 */
object KPictureUtils {
    val DEFAULT_KEYS_PICTURE_PHOTO = 3828//相册图库选择
    val DEFAULT_KEYS_CROP_PHOTO = 3829//图片剪切
    val DEFAULT_KEYS_CAMARA_PHOTO = 3830//相机
    val DEFAULT_KEYS_VIDEO_PHOTO = 3831//视频相册
    val DEFAULT_KEYS_VIDEO_CAPTURE = 3832//视频录制
    var cllback: ((file: File) -> Unit)? = null//fixme 回调，返回是原始数据文件哦。是本地原文件
    //打开相册【不需要任何权限，亲测百分百可用】,系统会跳出一个相册选择框。无法跳过这一步【无解】。
    //只能选择一个。系统没有多选。都是单选。
    var galleryPackName: String? = null//相册包名
    var packNameError = "指定包名异常"
    fun photo(activity: Activity, callback2: (file: File) -> Unit) {
        try {
            val intent = Intent()
            //i.setType("image/jpeg");//一般拍照的格式就是jpeg【jpeg就是.jpg】
            intent.type = "image/*"
            intent.putExtra("return-data", false)//true的话直接返回bitmap，可能会很占内存 不建议。我们这里直接返回File文件
            intent.action = Intent.ACTION_PICK//不能再调用intent.addCategory(),会出错。
            intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI//必不可少，不然图片返回错误
            if (galleryPackName == null) {
                galleryPackName = KAppUtils.getInstance().getGalleryPackName(activity)//获取系统相册包名
            }
            galleryPackName?.let {
                if (!it.equals(packNameError)) {
                    intent.setPackage(galleryPackName)//指定系统相册（不会再跳选择框了。欧耶！）
                }
            }
            activity.startActivityForResult(intent, DEFAULT_KEYS_PICTURE_PHOTO)//自定义相册标志
            this.cllback = callback2
        } catch (e: Exception) {
            Log.e("test", "相册崩坏" + e.message)
            galleryPackName?.let {
                if (e.message?.contains(it) ?: false) {
                    if (!it.equals(packNameError)) {
                        galleryPackName = packNameError
                        photo(activity, callback2)
                    }
                }
            }

        }
    }

    var fileUri: Uri? = null// 相机拍照时创建的Uri链接,fileUri.getPath()获取拍照图片路径
    var cramefile: File? = null//相机照片
    var CameraPackName: String? = null//相机包名
    //相机拍照【需要相机权限,如果清单里不写明相机权限,部分设备默认是开启。但是有的设备就不行，可能异常奔溃。所以保险还是在清单里加上权限声明】
    fun camera(activity: Activity, callback2: (file: File) -> Unit) {
        KPermissionUtils.requestPermissionsCamera(activity) {
            if (it) {
                try {
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    //PNG格式的不能显示在相册中
                    cramefile = KFileUtils.getInstance().createFile(getAppCaclePath(activity), System.currentTimeMillis().toString() + ".jpg")//相机拍摄的照片位置。不使用SD卡。这样就不需要SDK权限。
                    if (Build.VERSION.SDK_INT >= 23) {//7.0及以上版本(版本号24),为了兼容6.0(版本号23)，防止6.0也可能会有这个问题。
                        //getPackageName()和${applicationId}显示的都是当前应用的包名。无论是在library还是moudle中，都是一样的。都显示的是当前应用moudle的。与类库无关。请放心使用。
                        fileUri = FileProvider.getUriForFile(activity, activity.getPackageName() + ".provider", //与android:authorities="${applicationId}.provider"对应上
                                cramefile!!)
                    } else {
                        fileUri = Uri.fromFile(cramefile!!)
                    }
                    //以下两个addFlags必不可少。【以防万一出错】
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)//必不可少
                    intent.putExtra("return-data", false)

                    if (CameraPackName == null) {
                        CameraPackName = KAppUtils.getInstance().getCameraPackName(activity)//获取系统相机包名
                    }
                    CameraPackName?.let {
                        if (!it.equals(packNameError)) {
                            intent.setPackage(CameraPackName)//指定系统相机（不会再跳选择框了。欧耶！）
                        }
                    }

                    activity.startActivityForResult(intent, DEFAULT_KEYS_CAMARA_PHOTO)//自定义相机标志
                    this.cllback = callback2
                } catch (e: Exception) {
                    // TODO: handle exception
                    Log.e("test", "相机崩坏" + e.message)
                    CameraPackName?.let {
                        if (e.message?.contains(it) ?: false) {
                            if (!it.equals(packNameError)) {
                                CameraPackName = packNameError
                                camera(activity, callback2)
                            }
                        }
                    }
                }
            } else {
                KPermissionUtils.showFailure(activity, KPermissionUtils.perMissionTypeCamera)
            }
        }
    }

    var cropfile: File? = null//裁剪文件
    //图片剪辑【可以在相册，拍照回调成功后手动调用哦。】,兼容7.0。模拟器上没有上面效果。6.0的真机都没问题。7.0的真机没有测试。
    //w:h 宽和高的比率
    //width:height实际裁剪的宽和高的具体值。
    fun crop(activity: Activity, file: File, w: Int, h: Int, callback2: (file: File) -> Unit) {
        crop(activity, file, w, h, -1, -1, callback2)
    }

    fun crop(activity: Activity, file: File, w: Int, h: Int, width: Int, height: Int, callback2: (file: File) -> Unit) {
        //fixme AssetsUtils.getInstance().getBitmapFromFile(it.path, true,false)
        //fixme [注意了哦。如果图片剪切了，就不要读取缓存哦。]
        cropfile = KFileUtils.getInstance().copyFile(file, getAppCropPath(activity), file.name)
        val intent = Intent("com.android.camera.action.CROP")
        if (Build.VERSION.SDK_INT >= 23) {//7.0及以上版本(版本号24),为了兼容6.0(版本号23)，防止6.0也可能会有这个问题。
            //相机里面也使用了这个，多次使用不会出错。可以重复使用，不冲突。
            fileUri = FileProvider.getUriForFile(activity, activity.getPackageName() + ".provider", //与android:authorities="${applicationId}.provider"对应上
                    cropfile!!)
        } else {
            fileUri = Uri.fromFile(cropfile)//这个是原图
        }
        //剪辑图片之后，保存的位置。
        //这个是裁剪之后的图,截图保存的uri必须使用Uri.fromFile(),之前测试是这样，现在好像又不是这样了。也需要使用FileProvider了
        //保存位置只能是SD卡或者file源文件位置。无法指定我们app的私有目录。
        //var cropUri = Uri.fromFile(cropfile)
        var cropUri = fileUri//两个uri指向同一个才有效。这样裁剪才能访问我们app的私有目录。亲测。
        //以下两个addFlags必不可少。
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_PREFIX_URI_PERMISSION)
        //告诉系统需要裁剪,以下這兩个参数可有可无，为了兼容性，两个都设置成true,以防万一。
        intent.putExtra("crop", "true")
        intent.putExtra("scale", true)//缩放功能禁止不了，系统裁剪，肯定自带缩放的功能。无法固定大小，只能固定宽高比例裁剪。总之缩放功能禁止不了，无论你设置true还是false都一样。自带缩放。
        //width:height 裁剪框的宽高比
        //intent.putExtra("aspectX", 1);
        //intent.putExtra("aspectY", 1);
        intent.putExtra("aspectX", w)
        intent.putExtra("aspectY", h)
        //裁剪的宽和高。具体的数值。
        // [亲测，真实有效,不管数值是多少（小于0无效，其他都有效，多大都有效）。
        // 宽高比例不对，会对图片进行拉伸。即会变形。]
        //图片太小会模糊，太大不会模糊。
        if (width > 0 && height > 0) {
            //如果不传，就会根据裁剪，自定义决定大小。
            //必须是Int类型才有效。
            intent.putExtra("outputX", width);
            intent.putExtra("outputY", height);
        }
        intent.putExtra("return-data", false)//true的话直接返回bitmap，可能会很占内存 不建议
        intent.putExtra("noFaceDetection", true)//去除默认的人脸识别，否则和剪裁匡重叠
        intent.setDataAndType(fileUri, "image/*")//读取的uri,即要裁剪的uri
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cropUri)//截图保存的uri必须使用Uri.fromFile()
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())//输出格式
        activity.startActivityForResult(intent, DEFAULT_KEYS_CROP_PHOTO)//自定义剪辑标志
        this.cllback = callback2
    }

    //打开系统本地视频,只能选择一个。系统没有多选。都是单选。
    //【不需要任何权限，亲测百分百可用】
    //本地视频，一般只能识别mp4和3gp格式。其他特殊格式暂时识别不出。
    var videoPackName: String? = null//视频包名

    /**
     * 选取本地视频（在小米上，有时明明选择的是视频，返回的确实图片。这个Bug是硬伤啊。其他的还好。）
     * fixme 注意了，小米上，可能返回的是图片哦。一般都是视频。
     * fixme 之所以返回的是图片，是因为该是视频存储在云端。视频还没下载下来，所以返回的图片。
     * fixme 如果该视频已经下载下来了。肯定返回的就是视频。视频还没下载下来。只能返回图片了。
     */
    fun video(activity: Activity, callback2: (file: File) -> Unit) {
        try {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "video/*"
            intent.putExtra("return-data", false)//true的话直接返回bitmap，可能会很占内存 不建议
            //intent.action = Intent.ACTION_PICK//不能再调用intent.addCategory(),会出错。
            //亲测，不加这个也能获取流。也能获取文件名。只能获取文件名。无法获取标题路径和ID等。最好不要加这个，加了这个就没有本地视频选项，只有相册【相册里面也是视频】。
            //intent.setData(MediaStore.Video.Media.EXTERNAL_CONTENT_URI);//不加这个，也能获取流，获取文件名。还能有本地视频选项。加了这句就没有本地视频选项。
            //以下两个addFlags必不可少。【以防万一出错】
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1)

            if (videoPackName == null) {
                videoPackName = KAppUtils.getInstance().getGalleryPackName(activity)//fixme 获取系统相册包名（本地视频的无法获取。）
            }
            videoPackName?.let {
                if (!it.equals(packNameError)) {
                    intent.setPackage(videoPackName)//指定系统相册（不会再跳选择框了。欧耶！）
                }
            }
            activity.startActivityForResult(intent, DEFAULT_KEYS_VIDEO_PHOTO)//自定义视频相册标志
            this.cllback = callback2
        } catch (e: Exception) {
            videoPackName?.let {
                if (e.message?.contains(it) ?: false) {
                    if (!it.equals(packNameError)) {
                        videoPackName = packNameError
                        video(activity, callback2)
                    }
                }
            }
            Log.e("test", "视频相册崩坏2" + e.message)
        }

    }

    var cameraVideoFile: File? = null//视频录制文件
    /**
     * 跳转系统相机视频拍摄【需要相机权限】,进行视频录制
     * 手机拍摄的格式一般都是mp4的格式。
     */
    fun cameraVideo(activity: Activity, callback2: (file: File) -> Unit) {
        KPermissionUtils.requestPermissionsCamera(activity) {
            if (it) {
                try {
                    var fileUri: Uri? = null
                    val path = getAppVideoPath(activity)//相机视频拍摄存储位置。不使用SD卡。使用自己应用私有SD卡目录，这样就不需要外部的SD卡权限。
                    cameraVideoFile = KFileUtils.getInstance().createFile(path, System.currentTimeMillis().toString() + ".mp4")//视频拍摄基本都是MP4格式。每次都以当前毫秒数重新创建拍摄文件。
                    cameraVideoFile?.let {
                        if (Build.VERSION.SDK_INT >= 23) {//7.0及以上版本(版本号24),为了兼容6.0(版本号23)，防止6.0也可能会有这个问题。
                            //getPackageName()和${applicationId}显示的都是当前应用的包名。无论是在library还是moudle中，都是一样的。都显示的是当前应用moudle的。与类库无关。请放心使用。
                            fileUri = FileProvider.getUriForFile(activity, activity.getPackageName() + ".provider", //与android:authorities="${applicationId}.provider"对应上
                                    it)
                        } else {
                            fileUri = Uri.fromFile(cameraVideoFile)
                        }
                        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
                        intent.putExtra("return-data", false)//true的话直接返回bitmap，可能会很占内存 不建议
                        //以下两个addFlags必不可少。【以防万一出错】
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1)
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)//必不可少。拍摄视频使用自定义文件路径。如果没有。默认使用系统的路径。那样就需要申请SD卡权限。不使用系统默认的。

                        if (CameraPackName == null) {
                            CameraPackName = KAppUtils.getInstance().getCameraPackName(activity)//获取系统相机包名
                        }
                        CameraPackName?.let {
                            if (!it.equals(packNameError)) {
                                intent.setPackage(CameraPackName)//指定系统相机（不会再跳选择框了。欧耶！）
                            }
                        }

                        activity.startActivityForResult(intent, DEFAULT_KEYS_VIDEO_CAPTURE)
                        this.cllback = callback2
                    }
                } catch (e: Exception) {
                    // TODO: handle exception
                    Log.e("test", "相机崩坏" + e.message)
                    CameraPackName?.let {
                        if (e.message?.contains(it) ?: false) {
                            if (!it.equals(packNameError)) {
                                CameraPackName = packNameError
                                camera(activity, callback2)
                            }
                        }
                    }
                }
            } else {
                KPermissionUtils.showFailure(activity, KPermissionUtils.perMissionTypeCamera)
            }
        }
    }


    fun onActivityResult(activity: Activity, requestCode: Int, resultCode: Int, data: Intent?) {
        //Log.e("test","requestCode：\t"+requestCode+"\tresultCode:\t"+resultCode+"\tdata:\t"+data+"\tdata.data:\t"+data?.data)
        //resultCode 0 系统设置默认就是取消。
        if (requestCode == DEFAULT_KEYS_PICTURE_PHOTO && data != null && data.data != null) {
            //相册
            try {
                var file: File? = null
                val uri = data.data
                var photoPath: String? = null
                var photoName: String? = null
                try {
                    photoPath = getPhotoPath(activity, data)// 获取相册图片原始路径
                    photoPath?.let {
                        photoName = it.substring(it.lastIndexOf("/") + 1)
                    }
                } catch (e: Exception) {
                    // TODO: handle exception
                    Log.e("test", "相册图片路径获取失败" + e.message)
                }
                if (KPermissionUtils.requestPermissionsStorage(activity)) {
                    //有SD卡权限（可以直接操作原始图片）
                    //Log.e("test","系统图片:\t"+photoPath)
                    file = File(photoPath)
                } else {
                    //没有SD卡权限（不能操作原始图片）
                    var f = KCacheUtils.getInstance().getAsString(photoPath)
                    f?.let {
                        file = File(it)//获取缓存文件，避免重复创建。
                        file?.let {
                            if (it.exists()) {
                                if (it.length() <= 0) {
                                    file = null
                                }
                            } else {
                                file = null
                            }
                        }
                    }
                    if (file == null) {
                        //创建新图片文件
                        file = KFileUtils.getInstance().createFile(getAppCaclePath(activity), photoName)
                        // 将Uri图片的内容复制到file上
                        writeFile(activity.getContentResolver(),
                                file, uri)
                        KCacheUtils.getInstance().put(photoPath, file?.getPath())//存储文件路径
                    }
                }
                cllback?.let {
                    if (file != null) {
                        it(file!!)
                    }
                }
            } catch (e: Exception) {
                Log.e("test", "图库相册异常:\t" + e.message)
            }
        } else if (requestCode == DEFAULT_KEYS_CROP_PHOTO) {
            //裁剪
            //此时的data是空的。直接返回临时保存的裁剪文件
            cllback?.let {
                if (cropfile != null && cropfile?.exists() ?: false && cropfile!!.length() > 0) {
                    it(cropfile!!)
                }
            }
        } else if (requestCode == DEFAULT_KEYS_CAMARA_PHOTO) {
            //相机
            cllback?.let {
                if (cramefile != null && cramefile?.exists() ?: false && cramefile!!.length() > 0) {
                    it(cramefile!!)
                }
            }
        } else if (requestCode == DEFAULT_KEYS_VIDEO_PHOTO && data != null && data.data != null) {
            //本地视频
            val uri = data?.data
            //uri.getPath() 这个路径不行。靠不住。不要用。
            val cursor = activity.getContentResolver().query(uri!!, null, null, null, null)
            if (cursor != null) {
                cursor!!.moveToFirst()
                // 视频ID:MediaStore.Audio.Media._ID
                //int videoId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
                // 视频标题【没有后缀】：MediaStore.Audio.Media.TITLE
                // 视频名称【文件名带后缀】：MediaStore.Audio.Media.DISPLAY_NAME，这个无论是相册里的视频，还是本地视频里的视频都能过获取。
                //val fileName = cursor!!.getString(cursor!!.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME))//亲测能够获取。
                //Log.e("test","名称:\t"+fileName);
                var videoPath: String? = null
                try {
                    // 视频路径：MediaStore.Audio.Media.DATA。相册里面的视频可以获取。但是本地视频里的视频，路径无法获取。
                    videoPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))
                    if (videoPath == null) {
                        videoPath = getPhotoPath(activity, data)//这个能获取图片路径，自然也能获取视频路径。
                    }
                } catch (e: Exception) {
                    videoPath = null
                    Log.e("test", "本地视频路径获取失败:\t" + e.message);
                }
                //Log.e("test", "视频路径:\t" + videoPath + "\t路径:\t" + getPhotoPath(activity, data) + "\t名称:\t" + fileName + "\turi路径:\t" + data.data.path)
                videoPath?.let {
                    var file = File(videoPath)
                    cllback?.let {
                        if (file != null && file.exists()) {
                            it(file!!)
                        }
                    }
                }
            }
        } else if (requestCode == DEFAULT_KEYS_VIDEO_CAPTURE && data != null && data.data != null) {
            //相机，视频录制
            cameraVideoFile?.let {
                if (it.exists() && it.length() > 0) {
                    cllback?.let {
                        it(cameraVideoFile!!)
                    }
                }
            }
        }
    }

    //文件路径【相机不能使用私有目录，必须使用sd卡。这个sd卡区域，不需要权限哦】
    fun getAppCaclePath(context: Context): String {
        //defaultConfig {
        //targetSdkVersion 23//getExternalFilesDir才能正常访问，无需权限。但是如果是22及以下。就需要开启SD卡读取权限。
        //}
        //return getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
        return context.getFilesDir().getAbsoluteFile().getAbsolutePath() + "/cache"
    }

    //视频录制路径
    fun getAppVideoPath(context: Context): String {
        return context.getFilesDir().getAbsoluteFile().getAbsolutePath() + "/video"
    }

    //文件裁剪路径
    fun getAppCropPath(context: Context): String {
        return context.getFilesDir().getAbsoluteFile().getAbsolutePath() + "/crop"
    }

    //获取相册图片路径
    fun getPhotoPath(activtiy: Activity, data: Intent): String? {
        var photoPath: String? = null
        try {
            val uri = data.data
            // 获取相册图片路径
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            // 好像是android多媒体数据库的封装接口，具体的看Android文档
            var cursor: Cursor? = null
            if (Build.VERSION.SDK_INT >= 19) {//4.4版本
                //managedQuery()现在已经被getContentResolver().query()替代了，不过它们的参数都是一样的。效果也是一样的。
                cursor = activtiy.getContentResolver().query(uri!!, proj, null, null, null)
            } else {
                //低版本
                cursor = activtiy.managedQuery(uri, proj, null, null, null)
            }
            // 按我个人理解 这个是获得用户选择的图片的索引值
            val column_index = cursor!!
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            // 将光标移至开头 ，这个很重要，不小心很容易引起越界
            cursor.moveToFirst()
            // 最后根据索引值获取图片路径
            photoPath = cursor.getString(column_index)
            // bm = BitmapFactory.decodeFile(path);
        } catch (e: Exception) {
            // TODO: handle exception
            Log.e("test", "相册图片路径获取失败" + e.message)
        }
        return photoPath
    }

    //处理相册中的图片旋转问题
    //每张图片都有自己的显示方向，ExifInterface
    //桌面是看不出来方向的，都是处理过的。你要直接放进studio里面。就可以看出方向了。

    /**
     * bitmap 位图
     * path 位图文件对于的路径
     */
    fun rotateBitmap(bitmap: Bitmap?, path: String): Bitmap? {
        var degree = readPictureDegree(path)
        return rotateBitmap(bitmap, degree)
    }

    fun rotateBitmap(bitmap: Bitmap?, input: InputStream): Bitmap? {
        var degree = readPictureDegree(input = input)
        return rotateBitmap(bitmap, degree)
    }

    //获取图片的旋转角度。（文件路径或流，随便传一个都行，SD卡的路径需要SD卡权限，无论是流还是文件。）
    fun readPictureDegree(path: String? = null, input: InputStream? = null): Int {
        var degree = 0
        try {
            var exifInterface: ExifInterface? = null
            path?.let {
                exifInterface = ExifInterface(path)
            }
            if (exifInterface != null && android.os.Build.VERSION.SDK_INT >= 24) {
                input?.let {
                    exifInterface = ExifInterface(input)//通过流取加载，最后还是会转换为File,所以还是需要SD卡权限。
                }
            }
            exifInterface?.let {
                val orientation = it.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL)
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> degree = 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270
                }
            }
        } catch (e: IOException) {
            //Log.e("test", "获取图片角度异常:\t" + e.message)
        }
        return degree
    }

    //对位图进行指定角度旋转。
    private fun rotateBitmap(bitmap: Bitmap?, rotate: Int): Bitmap? {
        if (bitmap == null)
            return null
        if (rotate == 0) {
            return bitmap
        }
        val w = bitmap.width
        val h = bitmap.height

        // Setting post rotate to 90
        val mtx = Matrix()
        mtx.postRotate(rotate.toFloat())
        var bm = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true)
        bitmap.recycle()//释放掉
        return bm
    }

    //将Uri图片的内容复制到file上,成功返回Bitmap,错误返回null
    fun writeFile(cr: ContentResolver, file: File?, uri: Uri) {
        if (file == null) {
            return
        }
        var bitmap: Bitmap? = null//位图
        try {
            val fout = FileOutputStream(file)
            bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri), null, KAssetsUtils.getInstance().optionsARGB_8888)
            //保存位图原文件
            bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, fout)// 80是压缩率，表示压缩20%;取值范围在0~100，代表质量
            try {
                fout.flush()
                fout.close()
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("test", "相册图片异常0" + e.message)
            }
            bitmap.recycle()//释放
            bitmap = null
        } catch (e: FileNotFoundException) {
            Log.e("test", "相册图片异常1" + e.message)
        } catch (e: Exception) {
            Log.e("test", "相册图片异常2" + e.message)
        }
    }

}