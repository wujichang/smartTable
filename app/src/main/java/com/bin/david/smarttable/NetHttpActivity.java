package com.bin.david.smarttable;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.bin.david.form.core.SmartTable;
import com.bin.david.form.core.TableConfig;
import com.bin.david.form.data.CellInfo;
import com.bin.david.form.data.Column;
import com.bin.david.form.data.format.bg.BaseCellBackgroundFormat;
import com.bin.david.form.data.style.FontStyle;
import com.bin.david.form.utils.DensityUtils;
import com.bin.david.smarttable.bean.PM25;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;

public class NetHttpActivity extends AppCompatActivity {

    private SmartTable<PM25> table;
    private Handler mHandler = new Handler();
    private boolean isFrist = true;
    private Runnable AddDataRunnable = new Runnable() {
        @Override
        public void run() {
           getData();
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_table);
        FontStyle.setDefaultTextSize(DensityUtils.sp2px(this,15));
        table = (SmartTable<PM25>) findViewById(R.id.table);

        table.getConfig().setContentBackgroundFormat(new BaseCellBackgroundFormat<CellInfo>() {
            @Override
            public int getBackGroundColor(CellInfo cellInfo) {
                if(cellInfo.position%2 == 1) {
                    return ContextCompat.getColor(NetHttpActivity.this, R.color.content_bg);
                }
                return TableConfig.INVALID_COLOR;
            }

        }).setColumnBackgroundFormat(new BaseCellBackgroundFormat<Column>() {
            @Override
            public int getBackGroundColor(Column column) {
                if("area".equals(column.getFieldName())) {
                    return ContextCompat.getColor(NetHttpActivity.this,R.color.column_bg);
                }
                return TableConfig.INVALID_COLOR;
            }
            @Override
            public int getTextColor(Column column) {
                if("area".equals(column.getFieldName())) {
                    return ContextCompat.getColor(NetHttpActivity.this, R.color.white);
                }else{
                    return TableConfig.INVALID_COLOR;
                }
            }
        });
        getData();

    }
    public void onClick(View view) {
        Intent intent = new Intent(this,AnnotationModeActivity.class);
        startActivity(intent);
    }

    public void getData(){
        String url = "http://www.pm25.in/api/querys/pm10.json?city=%E4%B8%8A%E6%B5%B7&token=5j1znBVAsnSf5xQyNQyq&avg";
        OkHttpUtils
                .get()
                .url(url)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {

                    }

                    @Override
                    public void onResponse(String response, int id) {

                        Gson gson = new Gson();
                        try {
                            Type type = new TypeToken<ArrayList<PM25>>() {}.getType();
                            List<PM25> pm25List = gson.fromJson(response,type);
                            if(isFrist) {
                                table.setData(pm25List);
                                isFrist = false;
                            }else{
                                table.addData(pm25List,true);
                                table.getMatrixHelper().flingBottom(200);
                                table.getMatrixHelper().flingLeft(200);
                            }
                            mHandler.postDelayed(AddDataRunnable,1000);
                        }catch (Exception e){

                        }


                    }

                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(AddDataRunnable);
    }
}
