package com.jimbo.mycrop;

import android.graphics.*;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class ShowResultActivity extends AppCompatActivity {

    ShowResultView imgaeView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_result);
        imgaeView = (ShowResultView) findViewById(R.id.showResultView);

        android.graphics.Point[] points = new Point[4*6];
        points[0] = new Point(0,0);
        points[1] = new Point(100,0);
        points[2] = new Point(0,100);
        points[3] = new Point(100,100);

        points[4] = new Point(100,100);
        points[5] = new Point(200,100);
        points[6] = new Point(100,200);
        points[7] = new Point(200,200);

        points[8] = new Point(200,200);
        points[9] = new Point(300,200);
        points[10] = new Point(200,300);
        points[11] = new Point(300,300);

        points[12] = new Point(300,300);
        points[13] = new Point(400,300);
        points[14] = new Point(300,400);
        points[15] = new Point(400,400);

        points[16] = new Point(400,400);
        points[17] = new Point(500,400);
        points[18] = new Point(400,500);
        points[19] = new Point(500,500);

        points[20] = new Point(500,500);
        points[21] = new Point(600,500);
        points[22] = new Point(500,600);
        points[23] = new Point(600,600);
        imgaeView.setPoints(points);
        if (null == CropActivity.mBitmap) {
            Toast.makeText(ShowResultActivity.this, "worry", Toast.LENGTH_SHORT).show();
            return;
        }
        imgaeView.setImageBitmap(CropActivity.mBitmap);
    }
}
