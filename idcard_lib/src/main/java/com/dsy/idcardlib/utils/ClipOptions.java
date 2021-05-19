package com.dsy.idcardlib.utils;

import android.content.Intent;
import android.text.TextUtils;

/**
 * Author: zqf
 * Date: 2021/05/19
 */
public class ClipOptions {

    private int aspectX;
    private int aspectY;
    private int maxWidth;
    private String tip;
    private String inputPath;
    private String outputPath;

    private ClipOptions() {
    }

    public ClipOptions aspectX(int aspectX) {
        this.aspectX = aspectX;
        return this;
    }

    public ClipOptions aspectY(int aspectY) {
        this.aspectY = aspectY;
        return this;
    }

    public ClipOptions maxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
        return this;
    }

    public ClipOptions tip(String tip) {
        this.tip = tip;
        return this;
    }

    public ClipOptions inputPath(String path) {
        this.inputPath = path;
        return this;
    }

    public ClipOptions outputPath(String path) {
        this.outputPath = path;
        return this;
    }

    public int getAspectX() {
        return aspectX;
    }

    public int getAspectY() {
        return aspectY;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public String getTip() {
        return tip;
    }

    public String getInputPath() {
        return inputPath;
    }

    public String getOutputPath() {
        return outputPath;
    }

    private void checkValues() {
        if (TextUtils.isEmpty(inputPath)) {
            throw new IllegalArgumentException("The input path could not be empty");
        }
        if (TextUtils.isEmpty(outputPath)) {
            throw new IllegalArgumentException("The output path could not be empty");
        }
    }

    public static ClipOptions createFromBundle(Intent intent) {
        return new ClipOptions()
                .aspectX(intent.getIntExtra("aspectX", 1))
                .aspectY(intent.getIntExtra("aspectY", 1))
                .maxWidth(intent.getIntExtra("maxWidth", 0))
                .tip(intent.getStringExtra("tip"))
                .inputPath(intent.getStringExtra("inputPath"))
                .outputPath(intent.getStringExtra("outputPath"));
    }
}
