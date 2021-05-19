package com.dsy.idcardlib.global;


import com.dsy.idcardlib.utils.FileUtils;

import java.io.File;


public class Constant {
    //app名称
    public static final String APP_NAME = "DsyIDCardCamera";
    //DsyIDCardCamera/
    public static final String BASE_DIR = APP_NAME + File.separator;
    //文件夹根目录 /storage/emulated/0/DsyIDCardCamera/
    public static final String DIR_ROOT = FileUtils.getRootPath() + File.separator + Constant.BASE_DIR;
}