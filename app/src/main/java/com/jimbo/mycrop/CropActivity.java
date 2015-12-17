package com.jimbo.mycrop;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;

public class CropActivity extends Activity {

    public static final int TAKE_PICTURE = 0x444;
    public static final int CROP_IMAGE_RESULT = 0x222;
    public static final int CROP_IMAGE_REQUEST = 0x333;
    public static final String CROP_IMAGE_POINTS = "CROP_IMAGE_POINTS";
    public static final String INTISTATUS_POINTS = "INTISTATUS_POINTS";

    private static final String PATH = "image.jpg";
    private static final int START_PICK_CODE = 0x111;
    CropImageView mCrop = null;
    FrameLayout mDone = null;
    FrameLayout mCancel = null;

    public static Bitmap mBitmap;

    private Uri mUri;

    private boolean isCamera = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);

        initView();

        initPoints();
        isCamera = getIntent().getBooleanExtra("iscamera", false);
        pickImage(isCamera);
    }

    private void initPoints() {
        Point[] points = null;
        try {
            Bundle bundle = getIntent().getExtras();
            int[] pointsArr = bundle.getIntArray(INTISTATUS_POINTS);
            points = new Point[]{
                    new Point(pointsArr[0],pointsArr[1]),
                    new Point(pointsArr[2], pointsArr[3]),
                    new Point(pointsArr[4], pointsArr[5]),
                    new Point(pointsArr[6], pointsArr[7])
            };
        } catch (NullPointerException e) {
            points = new Point[]{
                    new Point(0,0),
                    new Point(200, 0),
                    new Point(0, 200),
                    new Point(200, 200)
            };
        }
        mCrop.setPoints(points);
    }

    private void initView(){
        mCrop = (CropImageView) findViewById(R.id.cropView);
        mCancel = (FrameLayout) findViewById(R.id.btn_cancel);
        mDone = (FrameLayout) findViewById(R.id.btn_done);

        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isCamera) {
                    finish();
                } else {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    try {
                        Uri imageUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(),PATH));
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                        startActivityForResult(intent, TAKE_PICTURE);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        mDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(CropActivity.this, ShowResultActivity.class));

                /*
                Point[] points = null;
                try {
                    points = mCrop.getPoints();
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                int[] p = new int[]{points[0].x, points[0].y,
                        points[1].x, points[1].y,
                        points[2].x, points[2].y,
                        points[3].x, points[3].y,
                };
                bundle.putIntArray(CROP_IMAGE_POINTS, p);
                intent.putExtras(bundle);
                setResult(CROP_IMAGE_RESULT, intent);
                finish();*/
            }
        });
    }

    void pickImage(boolean isCamera) {
        if (isCamera) {
            Log.d("CropActivity", "拍照~");
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            try {
                Uri imageUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(),PATH));
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                intent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, "vertical");
                startActivityForResult(intent, TAKE_PICTURE);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        } else {
            Log.d("CropActivity", "图库选取");
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, START_PICK_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (RESULT_OK == resultCode && START_PICK_CODE == requestCode) {
            Log.d("CropActivity", "获取图库图片");
            mUri = data.getData();
            mBitmap = getBitmapFromUri(data.getData());
            mCrop.setImageBitmap(mBitmap);
        } else if (resultCode == RESULT_OK && requestCode == TAKE_PICTURE){
            Log.d("CropActivity", "获取拍照图片");
            mUri = Uri.parse(Environment.getExternalStorageDirectory()
                    +"/"+PATH);
            mBitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory()
                    +"/"+PATH);
            mCrop.setImageBitmap(mBitmap);
        } else {
            CropActivity.this.finish();
        }
//        else if(requestCode == TAKE_PICTURE) {
//            Log.d("CropActivity", "拍照~");
//            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//            try {
//                Uri imageUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(),PATH));
//                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
//                startActivityForResult(intent, TAKE_PICTURE);
//            } catch (NullPointerException e) {
//                e.printStackTrace();
//            }
//        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private Bitmap getBitmapFromUri(Uri pathOfImage) {
        ContentResolver cr = getContentResolver();
        try {
            return BitmapFactory.decodeStream(cr.openInputStream(pathOfImage));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
