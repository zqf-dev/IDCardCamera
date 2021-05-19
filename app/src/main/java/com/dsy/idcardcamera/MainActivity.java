package com.dsy.idcardcamera;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.dsy.idcardlib.camera.IDCardCameraSelect;
import com.dsy.idcardlib.utils.FileUtils;

import java.util.List;


/**
 * Author: zqf
 * Date: 2021/05/08
 */
public class MainActivity extends AppCompatActivity {
    private ImageView mIv;
    private TextView mShowPathTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mIv = findViewById(R.id.iv_front);
        mShowPathTv = findViewById(R.id.show_path_tv);
    }

    public void shootingClick(View view) {
        IDCardCameraSelect.create(this).openCamera(IDCardCameraSelect.TYPE_IDCARD_FRONT);
    }

    public void backClick(View view) {
        IDCardCameraSelect.create(this).openCamera(IDCardCameraSelect.TYPE_IDCARD_BACK);
    }

    public void commonIDCardClick(View view) {
        IDCardCameraSelect.create(this).openCamera(IDCardCameraSelect.TYPE_IDCARD_All);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == IDCardCameraSelect.RESULT_CODE) {
            List<String> path = IDCardCameraSelect.getImagePath(data);
            if (path != null) {
                if (path.size() > 2) {
                    mShowPathTv.setText(("1、" + path.get(0) + "\n" + "2、" + path.get(1)));
                } else {
                    mShowPathTv.setText(("1、" + path.get(0)));
                }
                mIv.setImageBitmap(BitmapFactory.decodeFile(path.get(0)));
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        FileUtils.clearCache(getApplicationContext());
    }
}
