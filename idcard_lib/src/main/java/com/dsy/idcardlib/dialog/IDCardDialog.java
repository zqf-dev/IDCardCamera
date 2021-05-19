package com.dsy.idcardlib.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.dsy.idcardlib.R;


public class IDCardDialog extends Dialog {

    public IDCardDialog(Context context, int layout) {
        super(context, R.style.Picture_Theme_Dialog);
        setContentView(layout);
        Window window = getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.gravity = Gravity.CENTER;
            window.setAttributes(params);
        }
    }
}
