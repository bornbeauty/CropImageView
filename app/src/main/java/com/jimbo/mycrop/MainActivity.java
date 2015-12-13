package com.jimbo.mycrop;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

/**
 * Created by jimbo on 15-12-5.
 */
public class MainActivity extends AppCompatActivity {
    TextView textView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.text);
        (findViewById(R.id.button1)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(MainActivity.this, CropActivity.class)
                        .putExtra("iscamera", false),
                        CropActivity.CROP_IMAGE_REQUEST);
            }
        });
        (findViewById(R.id.button2)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(MainActivity.this, CropActivity.class)
                                .putExtra("iscamera", true),
                        CropActivity.CROP_IMAGE_REQUEST);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropActivity.CROP_IMAGE_REQUEST &&
                resultCode == CropActivity.CROP_IMAGE_RESULT) {
            try {
                int[] points = data.getExtras().
                        getIntArray(CropActivity.CROP_IMAGE_POINTS);
                String s = "左上角:"+points[0]+","+points[1]
                        +"右上角:"+points[2]+","+points[3]
                        +"左下角:"+points[4]+","+points[5]
                        +"右下角:"+points[6]+","+points[7];
                textView.setText(s);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }
}


