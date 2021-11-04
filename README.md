### 身份证拍照Lib使用文档

## 必须

```java
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}

android {
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation 'com.github.zqf-dev:IDCardCamera:1.0.1'
 }

```

## 使用

### 1、正反面拍照or[相册选择]

```java
*注 【原有的单独拍照Type、调用方式不变】
TYPE_IDCARD_FRONT：正
TYPE_IDCARD_BACK：反

新增的Type[传入此类型即可]
TYPE_IDCARD_All：正反面

IDCardCameraSelect.create(this).openCamera(IDCardCameraSelect.TYPE_IDCARD_All);

*注
 由于【相册选择】图片时可能存在不规则的图片以及不知道身份证在图片中的具体位置，
 为了减少噪声干扰，提高识别正确率避免用户二次选择操作，因此默认对图片做以下操作：
 1、裁剪功能；
 2、移动功能；
 3、放大功能；
 4、缩小功能；

```

### 2、返回的结果

```java

@Override
protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == IDCardCameraSelect.RESULT_CODE) {
            List<String> path = IDCardCameraSelect.getImagePath(data);
            if (path != null && path.size() > 0) {
            	//解析path返回bitmap，则可回调到UI层显示Bitmap
                Bitmap bitmap = BitmapFactory.decodeFile(path.get(0 / 1));
    	}
	}
}

```

### 3、清除缓存

```java
* 建议在页面销毁时处理
FileUtils.clearCache(getApplicationContext());
```

