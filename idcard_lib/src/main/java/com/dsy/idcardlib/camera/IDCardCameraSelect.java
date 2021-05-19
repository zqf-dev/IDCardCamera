package com.dsy.idcardlib.camera;

import android.app.Activity;
import android.content.Intent;

import androidx.fragment.app.Fragment;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * 身份证拍摄入口
 */
public class IDCardCameraSelect {

    //身份证正面
    public final static int TYPE_IDCARD_FRONT = 1;
    //身份证反面
    public final static int TYPE_IDCARD_BACK = 2;
    //身份证正反面
    public final static int TYPE_IDCARD_All = 3;
    //结果码
    public final static int RESULT_CODE = 0X11;
    //判断权限的请求码
    final static int PERMISSION_CODE_FIRST = 0x12;
    //相册request code
    final static int REQUEST_ALBUM_CODE = 0x13;
    //拍摄类型标记
    final static String TAKE_TYPE = "take_type";
    //图片路径标记
    final static String IMAGE_PATH = "image_path";

    private final WeakReference<Activity> mActivity;
    private final WeakReference<Fragment> mFragment;

    public static IDCardCameraSelect create(Activity activity) {
        return new IDCardCameraSelect(activity);
    }

    public static IDCardCameraSelect create(Fragment fragment) {
        return new IDCardCameraSelect(fragment);
    }

    private IDCardCameraSelect(Activity activity) {
        this(activity, (Fragment) null);
    }

    private IDCardCameraSelect(Fragment fragment) {
        this(fragment.getActivity(), fragment);
    }

    private IDCardCameraSelect(Activity activity, Fragment fragment) {
        this.mActivity = new WeakReference(activity);
        this.mFragment = new WeakReference(fragment);
    }

    /**
     * 打开相机
     *
     * @param IDCardDirection 身份证方向
     */
    public void openCamera(int IDCardDirection) {
        Activity activity = this.mActivity.get();
        Fragment fragment = this.mFragment.get();
        Intent intent = new Intent(activity, CameraActivity.class);
        intent.putExtra(TAKE_TYPE, IDCardDirection);
        if (fragment != null) {
            fragment.startActivityForResult(intent, IDCardDirection);
        } else {
            activity.startActivityForResult(intent, IDCardDirection);
        }
    }

    /**
     * 获取图片路径
     *
     * @param data Intent
     * @return 图片路径
     */
    public static List<String> getImagePath(Intent data) {
        if (data != null) {
            return data.getStringArrayListExtra(IMAGE_PATH);
        }
        return null;
    }
}

