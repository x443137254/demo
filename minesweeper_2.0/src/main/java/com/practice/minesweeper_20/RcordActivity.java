package com.practice.minesweeper_20;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RcordActivity extends Activity {

    private String TAG = "debug";

    private float mEasyRate;
    private float mNormalRate;
    private float mHardRate;
    private long mEasyRecord;
    private long mNormRecord;
    private long mHardRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rcord);
        getActionBar().setTitle("最高纪录");
        ListView listView = (ListView) findViewById(R.id.recordlist);
        listView.setAdapter(new RecordAdapter());
        Bundle bundle = getIntent().getBundleExtra(FieldName.RECORD);
        mEasyRate = bundle.getFloat(FieldName.ERATE, 0);
        mNormalRate = bundle.getFloat(FieldName.NRATE,0);
        mHardRate = bundle.getFloat(FieldName.HRATE,0);
        mEasyRecord = bundle.getLong(FieldName.EASY, 0);
        mNormRecord = bundle.getLong(FieldName.NORMAL,0);
        mHardRecord = bundle.getLong(FieldName.HARD,0);

    }
    public class RecordAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = getLayoutInflater().inflate(R.layout.itemlayout,null);
            TextView tv1 = (TextView) view.findViewById(R.id.text_level);
            TextView tv2 = (TextView) view.findViewById(R.id.text_record);
            TextView tv3 = (TextView) view.findViewById(R.id.text_rate);
            SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss:SSS");
            DecimalFormat rateFormat = new DecimalFormat("0%");
            switch (position){
                case 0:
                    tv1.setText("难度");
                    tv2.setText("时间");
                    tv3.setText("胜率");
                    return view;
                case 1:
                    tv1.setText("初级");
                    tv2.setText(dateFormat.format(new Date(mEasyRecord)));
                    tv3.setText(rateFormat.format(mEasyRate));
                    return view;
                case 2:
                    tv1.setText("中级");
                    tv2.setText(dateFormat.format(new Date(mNormRecord)));
                    tv3.setText(rateFormat.format(mNormalRate));
                    return view;
                case 3:
                    tv1.setText("高级");
                    tv2.setText(dateFormat.format(new Date(mHardRecord)));
                    tv3.setText(rateFormat.format(mHardRate));
                    return view;
            }
            return null;
        }
    }
    public void click(View v){
        finish();
    }
}
