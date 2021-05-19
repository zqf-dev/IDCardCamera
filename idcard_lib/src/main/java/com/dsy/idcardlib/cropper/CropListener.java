package com.dsy.idcardlib.cropper;

import android.graphics.Bitmap;

/**
 * 裁剪监听接口
 */
public interface CropListener {

    void onFinish(Bitmap bitmap);

}
