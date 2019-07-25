package com.autotool.sample.view;

import com.autotool.sample.bean.SampleBean;
import com.autotool.sample.util.GsonUtils;
import com.autotool.sample.R;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView mTvTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
    }

    private void initData() {
        SampleBean bean = new SampleBean();
        bean.testName = "codeScanPlugin";
        bean.testAction = "inject code";
        System.out.println("==========" + GsonUtils.objectToString(bean));
    }

    private void initView() {
        mTvTest = findViewById(R.id.tvTest);
        mTvTest.setText("设置按钮文字");
        mTvTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initData();
            }
        });
    }
}
