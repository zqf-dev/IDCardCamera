package com.dsy.idcardlib.camera;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.dsy.idcardlib.R;
import com.dsy.idcardlib.cropper.AlbumClipImageView;
import com.dsy.idcardlib.cropper.CropImageView;
import com.dsy.idcardlib.cropper.CropListener;
import com.dsy.idcardlib.dialog.IDCardDialog;
import com.dsy.idcardlib.utils.CommonUtils;
import com.dsy.idcardlib.utils.FileUtils;
import com.dsy.idcardlib.utils.ImageUtils;
import com.dsy.idcardlib.utils.PermissionChecker;
import com.dsy.idcardlib.utils.PermissionUtils;
import com.dsy.idcardlib.utils.ScreenUtils;
import com.dsy.idcardlib.utils.UriUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;



/**
 * Author: zqf
 * 拍照Camera 界面
 */
public class CameraActivity extends AppCompatActivity implements View.OnClickListener {

    private CameraPreview mCameraPreview;
    private CropImageView mCropImageView;
    private Bitmap mCropBitmap;
    private ImageButton mIvCameraCrop;
    private TextView mIdCardCameraTipStrTv;
    private Button mNextResultOk;
    private ImageView mTakePhoto;
    private FrameLayout mIdCardCropFly;
    private ImageView mAlbum;
    private AlbumClipImageView mAlbumClipIv;
    private RelativeLayout mIdCardCameraRl;
    //拍摄类型
    private int mType;
    //是否弹吐司，保证权限for循环只弹一次
    private boolean isToast = true;
    //是否进入setting
    protected boolean isEnterSetting;
    //0 拍照正面 1 拍照反面 2 相册选择正面 3 相册选择反面
    private int curIDCardCamera = 0;
    //结果结合
    private ArrayList<String> mIDCardResult = new ArrayList<>();
    //提示的LayoutParams
    private FrameLayout.LayoutParams tipParams;
    //标题
    private TextView titleTv;
    //max widget
    private int mMaxWidth;
    // 图片被旋转的角度
    private int mDegree;
    // 大图被设置之前的缩放比例
    private int mSampleSize;
    private int mSourceWidth;
    private int mSourceHeight;
    //相册选择图片裁剪后的输出路径
    private String mOutputPath;
    private String mInputPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean checkPermissionFirst = PermissionUtils.checkPermissionFirst(this, IDCardCameraSelect.PERMISSION_CODE_FIRST,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA});
        if (checkPermissionFirst) init();
    }

    /**
     * 处理请求权限的响应
     *
     * @param requestCode  请求码
     * @param permissions  权限数组
     * @param grantResults 请求权限结果数组
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean isPermissions = true;
        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                isPermissions = false;
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) {
                    //用户选择了"不再询问"
                    if (isToast) {
                        Toast.makeText(this, getString(R.string.permission_open_str), Toast.LENGTH_SHORT).show();
                        isToast = false;
                    }
                }
            }
        }
        isToast = true;
        if (isPermissions) {
            Log.e("onRequestPermission", "允许所有权限");
            init();
        } else {
            Log.e("onRequestPermission", "有权限不允许");
            showPermissionsDialog("未开启相关权限！");
        }
    }

    private void init() {
        setContentView(R.layout.idcard_camera_view);
        mType = getIntent().getIntExtra(IDCardCameraSelect.TAKE_TYPE, 0);
        initView();
        initListener();
        settingCameraType();
    }

    private void initView() {
        mCameraPreview = findViewById(R.id.camera_preview);
        mIvCameraCrop = findViewById(R.id.iv_camera_crop);
        mCropImageView = findViewById(R.id.crop_image_view);
        mIdCardCameraTipStrTv = findViewById(R.id.idcard_tip_str_tv);
        mNextResultOk = findViewById(R.id.iv_camera_result_ok);
        mTakePhoto = findViewById(R.id.iv_camera_take);
        mAlbum = findViewById(R.id.iv_camera_album);
        mIdCardCropFly = findViewById(R.id.idcard_crop_fly);
        titleTv = findViewById(R.id.idcard_title_tv);
        mAlbumClipIv = findViewById(R.id.album_clip_iv);
        mIdCardCameraRl = findViewById(R.id.idcard_camera_rl);
        //宽 1080 高2232
        float screenMinSize = Math.min(ScreenUtils.getScreenWidth(this), ScreenUtils.getScreenHeight(this));
        float screenMaxSize = Math.max(ScreenUtils.getScreenWidth(this), ScreenUtils.getScreenHeight(this));
        int mMargin = 8;
        float width = screenMinSize - (mMargin * 4);
        float height = screenMaxSize * 0.30f;
        tipParams = (FrameLayout.LayoutParams) mIdCardCameraTipStrTv.getLayoutParams();
        mIdCardCameraTipStrTv.setText(R.string.idcard_positive_reverse_tip_str);
        FrameLayout.LayoutParams cropParams = new FrameLayout.LayoutParams((int) width, (int) height);
        cropParams.setMargins(mMargin, mMargin, mMargin, mMargin);
        cropParams.gravity = Gravity.CENTER;
        mIvCameraCrop.setLayoutParams(cropParams);
        mIdCardCameraTipStrTv.setLayoutParams(tipParams);
    }

    private void settingCameraType() {
        switch (mType) {
            case IDCardCameraSelect.TYPE_IDCARD_FRONT:
                mIvCameraCrop.setImageResource(R.mipmap.idcard_lib_positive_bg_icon);
                titleTv.setText(R.string.type_idcard_front_str);
                tipParams.leftMargin = ScreenUtils.dip2px(this, 32);
                break;
            case IDCardCameraSelect.TYPE_IDCARD_BACK:
                mIvCameraCrop.setImageResource(R.mipmap.idcard_lib_reverse_bg_icon);
                tipParams.leftMargin = ScreenUtils.dip2px(this, 88);
                titleTv.setText(R.string.type_idcard_back_str);
                break;
            case IDCardCameraSelect.TYPE_IDCARD_All:
                if (curIDCardCamera == 0 || curIDCardCamera == 2) {
                    mIvCameraCrop.setImageResource(R.mipmap.idcard_lib_positive_bg_icon);
                    titleTv.setText(R.string.type_idcard_front_str);
                    tipParams.leftMargin = ScreenUtils.dip2px(this, 32);
                } else if (curIDCardCamera == 1 || curIDCardCamera == 3) {
                    mIvCameraCrop.setImageResource(R.mipmap.idcard_lib_reverse_bg_icon);
                    tipParams.leftMargin = ScreenUtils.dip2px(this, 88);
                    titleTv.setText(R.string.type_idcard_back_str);
                }
                break;
            default:
                break;
        }
        //增加0.5秒过渡界面，解决个别手机首次申请权限导致预览界面启动慢的问题
        new Handler().postDelayed(() -> runOnUiThread(() -> mCameraPreview.setVisibility(View.VISIBLE)), 500);
    }

    private void initListener() {
        findViewById(R.id.iv_camera_close).setOnClickListener(this);
        findViewById(R.id.idcard_title_refresh_iv).setOnClickListener(this);
        mCameraPreview.setOnClickListener(this);
        mNextResultOk.setOnClickListener(this);
        mTakePhoto.setOnClickListener(this);
        mAlbum.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.camera_preview) {
            //对焦
            mCameraPreview.focus();
        } else if (id == R.id.iv_camera_close) {
            //退出
            finish();
        } else if (id == R.id.idcard_title_refresh_iv) {
            //刷新重置拍照模式
            if (mNextResultOk.getVisibility() == View.VISIBLE) {
                mCameraPreview.setEnabled(true);
                mCameraPreview.addCallback();
                mCameraPreview.startPreview();
                setTakePhotoLayout();
            }
        } else if (id == R.id.iv_camera_album) {
            //相册选择图片
            albumChoosePhoto();
        } else if (id == R.id.iv_camera_take) {
            //确定拍照
            if (!CommonUtils.isFastClick()) {
                takePhoto();
            }
        } else if (id == R.id.iv_camera_result_ok) {
            //下一步【原有功能增加->接着拍反面】
            NextConfirm();
        }
    }

    private void albumChoosePhoto() {
        //系统图库选择一张图片
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        // 开启一个带有返回值的Activity，请求码为PHOTO_REQUEST_GALLERY
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        startActivityForResult(intent, IDCardCameraSelect.REQUEST_ALBUM_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == IDCardCameraSelect.REQUEST_ALBUM_CODE && data != null) {
                //拿到相册选择的图片show 到裁剪UI
                Uri uri = data.getData();
                if (uri != null) {
                    String path = UriUtil.getPath(this, uri);
                    Bitmap mSourceBitmap = BitmapFactory.decodeFile(path);
                    if (mSourceBitmap != null) {
                        mInputPath = path;
                        if (curIDCardCamera == 0) curIDCardCamera = 2;
                        if (curIDCardCamera == 1) curIDCardCamera = 3;
                        mCameraPreview.setEnabled(false);
                        mCameraPreview.onStop();
                        mIdCardCameraRl.setVisibility(View.GONE);
                        mAlbumClipIv.setVisibility(View.VISIBLE);
                        setCropLayout();
                        mAlbumClipIv.post(() -> {
                            mAlbumClipIv.setImageBitmap(mSourceBitmap);
                        });
                    }
                }
            }
        }
    }


    /**
     * 拍照 1080  x  2340
     */
    private void takePhoto() {
        mCameraPreview.setEnabled(false);
        CameraUtils.getCamera().setOneShotPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(final byte[] bytes, Camera camera) {
                //获取预览大小
                final Camera.Size size = camera.getParameters().getPreviewSize();
                camera.stopPreview();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final int w = size.width;
                        final int h = size.height;
                        Log.e("Tag", "w " + w + " h " + h);
                        Bitmap bitmap = ImageUtils.getBitmapFromByte(bytes, w, h);
                        if (bitmap != null) {
                            cropImage(ImageUtils.roteBitmap(bitmap));
                        }
                    }
                }).start();
            }
        });
    }

    /**
     * 正常拍照后裁剪图片
     */
    private void cropImage(Bitmap bitmap) {
        /*计算扫描框的坐标点*/
        //16
        float left = mIvCameraCrop.getLeft();
        float top = mIdCardCropFly.getTop() + mIvCameraCrop.getTop();
        float right = mIvCameraCrop.getRight() - left;
        float bottom = mIvCameraCrop.getBottom() + top;

        /*计算扫描框坐标点占原图坐标点的比例*/
        float leftProportion = left / mCameraPreview.getWidth();
        float topProportion = top / mCameraPreview.getHeight();
        float rightProportion = right / mCameraPreview.getWidth();
        float bottomProportion = bottom / mCameraPreview.getBottom();

        //系统先自动裁剪
        mCropBitmap = Bitmap.createBitmap(bitmap,
                (int) (leftProportion * (float) bitmap.getWidth()),
                (int) (topProportion * (float) bitmap.getHeight()),
                (int) ((rightProportion - leftProportion) * (float) bitmap.getWidth()),
                (int) ((bottomProportion - topProportion) * (float) bitmap.getHeight()));

        //设置显示手动裁剪模式
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //将手动裁剪区域设置成与扫描框一样大
                FrameLayout.LayoutParams cropParams = new FrameLayout.LayoutParams(mIdCardCropFly.getWidth(), mIdCardCropFly.getHeight());
                cropParams.gravity = Gravity.CENTER;
                mCropImageView.setLayoutParams(cropParams);
                setCropLayout();
                mCropImageView.setImageBitmap(mCropBitmap);
            }
        });
    }

    //设置裁剪布局
    private void setCropLayout() {
        mIvCameraCrop.setVisibility(View.GONE);
        mCameraPreview.setVisibility(View.GONE);
        mTakePhoto.setVisibility(View.GONE);
        mAlbum.setVisibility(View.GONE);
        mIdCardCameraTipStrTv.setVisibility(View.GONE);
        if (mAlbumClipIv.getVisibility() == View.GONE) {
            mCropImageView.setVisibility(View.VISIBLE);
        } else {
            mCropImageView.setVisibility(View.GONE);
        }
        mNextResultOk.setVisibility(View.VISIBLE);
    }

    //设置拍照布局
    private void setTakePhotoLayout() {
        mIdCardCameraRl.setVisibility(View.VISIBLE);
        mIvCameraCrop.setVisibility(View.VISIBLE);
        mCameraPreview.setVisibility(View.VISIBLE);
        mTakePhoto.setVisibility(View.VISIBLE);
        mAlbum.setVisibility(View.VISIBLE);
        mIdCardCameraTipStrTv.setVisibility(View.VISIBLE);
        mCropImageView.setVisibility(View.GONE);
        mNextResultOk.setVisibility(View.GONE);
        mAlbumClipIv.setVisibility(View.GONE);
        mCameraPreview.focus();
    }

    //点击确认【下一步】，返回图片路径
    private void NextConfirm() {
        if (curIDCardCamera < 2) {
            //拍照后裁剪确定的图片
            mCropImageView.crop(new CropListener() {
                @Override
                public void onFinish(Bitmap bitmap) {
                    if (bitmap == null) {
                        Toast.makeText(getApplicationContext(), getString(R.string.crop_fail), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    //保存图片到sdcard并返回图片路径
                    String imagePath = FileUtils.getImageCacheDir(CameraActivity.this)
                            + File.separator + System.currentTimeMillis() + ".jpg";
                    if (ImageUtils.save(bitmap, imagePath, Bitmap.CompressFormat.JPEG)) {
                        mIDCardResult.add(imagePath);
                        cameraCropNext();
                    }
                }
            }, true);
        } else {
            //相册选择图片后的裁剪
            clipImage();
        }
    }

    private void cameraCropNext() {
        if (mType == IDCardCameraSelect.TYPE_IDCARD_All) {
            if (curIDCardCamera == 0) {
                curIDCardCamera = 1;
                settingCameraType();
                if (mNextResultOk.getVisibility() == View.VISIBLE) {
                    mCameraPreview.setEnabled(true);
                    mCameraPreview.addCallback();
                    mCameraPreview.startPreview();
                    setTakePhotoLayout();
                }
            } else if (curIDCardCamera == 1) {
                Intent intent = new Intent();
                intent.putStringArrayListExtra(IDCardCameraSelect.IMAGE_PATH, mIDCardResult);
                setResult(IDCardCameraSelect.RESULT_CODE, intent);
                finish();
            }
        } else {
            Intent intent = new Intent();
            intent.putExtra(IDCardCameraSelect.IMAGE_PATH, mIDCardResult);
            setResult(IDCardCameraSelect.RESULT_CODE, intent);
            finish();
        }
    }

    private void albumCropNext() {
        if (mType == IDCardCameraSelect.TYPE_IDCARD_All) {
            if (curIDCardCamera == 2) {
                curIDCardCamera = 3;
                settingCameraType();
                if (mNextResultOk.getVisibility() == View.VISIBLE) {
                    mCameraPreview.setEnabled(true);
                    mCameraPreview.addCallback();
                    mCameraPreview.startPreview();
                    setTakePhotoLayout();
                }
            } else if (curIDCardCamera == 3) {
                Intent intent = new Intent();
                intent.putStringArrayListExtra(IDCardCameraSelect.IMAGE_PATH, mIDCardResult);
                setResult(IDCardCameraSelect.RESULT_CODE, intent);
                finish();
            }
        } else {
            Intent intent = new Intent();
            intent.putExtra(IDCardCameraSelect.IMAGE_PATH, mIDCardResult);
            setResult(IDCardCameraSelect.RESULT_CODE, intent);
            finish();
        }
    }


    @SuppressLint("StaticFieldLeak")
    private void clipImage() {
        mOutputPath = new File(getExternalCacheDir(), System.currentTimeMillis() + "_album.jpg").getPath();
        if (mOutputPath != null) {
            AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    //接收输入参数、执行任务中的耗时操作、返回 线程任务执行的结果
                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(mOutputPath);
                        //裁剪返回bitmap
                        Bitmap bitmap = createClippedBitmap();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                        if (!bitmap.isRecycled()) {
                            bitmap.recycle();
                        }
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), R.string.crop_fail, Toast.LENGTH_SHORT).show();
                    } finally {
                        if (fos != null) {
                            try {
                                fos.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    mIDCardResult.add(mOutputPath);
                    albumCropNext();
                }
            };
            task.execute();
        } else {
            finish();
        }
    }

    //裁剪
    private Bitmap createClippedBitmap() {
        if (mSampleSize <= 1) {
            return mAlbumClipIv.clip();
        }

        // 获取缩放位移后的矩阵值
        final float[] matrixValues = mAlbumClipIv.getClipMatrixValues();
        final float scale = matrixValues[Matrix.MSCALE_X];
        final float transX = matrixValues[Matrix.MTRANS_X];
        final float transY = matrixValues[Matrix.MTRANS_Y];

        // 获取在显示的图片中裁剪的位置
        final Rect border = mAlbumClipIv.getClipBorder();
        final float cropX = ((-transX + border.left) / scale) * mSampleSize;
        final float cropY = ((-transY + border.top) / scale) * mSampleSize;
        final float cropWidth = (border.width() / scale) * mSampleSize;
        final float cropHeight = (border.height() / scale) * mSampleSize;

        // 获取在旋转之前的裁剪位置
        final RectF srcRect = new RectF(cropX, cropY, cropX + cropWidth, cropY + cropHeight);
        final Rect clipRect = getRealRect(srcRect);

        final BitmapFactory.Options ops = new BitmapFactory.Options();
        final Matrix outputMatrix = new Matrix();

        outputMatrix.setRotate(mDegree);
        // 如果裁剪之后的图片宽高仍然太大,则进行缩小
        if (mMaxWidth > 0 && cropWidth > mMaxWidth) {
            ops.inSampleSize = findBestSample((int) cropWidth, mMaxWidth);

            final float outputScale = mMaxWidth / (cropWidth / ops.inSampleSize);
            outputMatrix.postScale(outputScale, outputScale);
        }

        // 裁剪
        BitmapRegionDecoder decoder = null;
        try {
            decoder = BitmapRegionDecoder.newInstance(mInputPath, false);
            final Bitmap source = decoder.decodeRegion(clipRect, ops);
            recycleImageViewBitmap();
            return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), outputMatrix, false);
        } catch (Exception e) {
            return mAlbumClipIv.clip();
        } finally {
            if (decoder != null && !decoder.isRecycled()) {
                decoder.recycle();
            }
        }
    }

    /**
     * 计算最好的采样大小。
     *
     * @param origin 当前宽度
     * @param target 限定宽度
     * @return sampleSize
     */
    private static int findBestSample(int origin, int target) {
        int sample = 1;
        for (int out = origin / 2; out > target; out /= 2) {
            sample *= 2;
        }
        return sample;
    }

    private void recycleImageViewBitmap() {
        mAlbumClipIv.post(() -> mAlbumClipIv.setImageBitmap(null));
    }

    private Rect getRealRect(RectF srcRect) {
        switch (mDegree) {
            case 90:
                return new Rect((int) srcRect.top, (int) (mSourceHeight - srcRect.right),
                        (int) srcRect.bottom, (int) (mSourceHeight - srcRect.left));
            case 180:
                return new Rect((int) (mSourceWidth - srcRect.right), (int) (mSourceHeight - srcRect.bottom),
                        (int) (mSourceWidth - srcRect.left), (int) (mSourceHeight - srcRect.top));
            case 270:
                return new Rect((int) (mSourceWidth - srcRect.bottom), (int) srcRect.left,
                        (int) (mSourceWidth - srcRect.top), (int) srcRect.right);
            default:
                return new Rect((int) srcRect.left, (int) srcRect.top, (int) srcRect.right, (int) srcRect.bottom);
        }
    }


    private void showPermissionsDialog(String errorMsg) {
        if (isFinishing()) {
            return;
        }
        final IDCardDialog dialog = new IDCardDialog(this, R.layout.picture_wind_base_dialog);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        Button btn_cancel = dialog.findViewById(R.id.btn_cancel);
        Button btn_commit = dialog.findViewById(R.id.btn_commit);
        btn_commit.setText(getString(R.string.picture_go_setting));
        TextView tvTitle = dialog.findViewById(R.id.tvTitle);
        TextView tv_content = dialog.findViewById(R.id.tv_content);
        tvTitle.setText(getString(R.string.picture_prompt));
        tv_content.setText(errorMsg);
        btn_cancel.setOnClickListener(v -> {
            if (!isFinishing()) {
                dialog.dismiss();
                finish();
            }
        });
        btn_commit.setOnClickListener(v -> {
            if (!isFinishing()) {
                dialog.dismiss();
            }
            PermissionChecker.launchAppDetailsSettings(this);
            isEnterSetting = true;
        });
        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 这里针对权限被手动拒绝后进入设置页面重新获取权限后的操作
        if (isEnterSetting) {
            boolean isExternalStorage = PermissionChecker.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) &&
                    PermissionChecker.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (isExternalStorage) {
                boolean isCameraPermissionChecker = PermissionChecker.checkSelfPermission(this, Manifest.permission.CAMERA);
                if (isCameraPermissionChecker) {
                    init();
                } else {
                    showPermissionsDialog(getString(R.string.picture_camera));
                }
            } else {
                showPermissionsDialog(getString(R.string.picture_jurisdiction));
            }
            isEnterSetting = false;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mCameraPreview != null) {
            mCameraPreview.onStart();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mCameraPreview != null) {
            mCameraPreview.onStop();
        }
    }
}