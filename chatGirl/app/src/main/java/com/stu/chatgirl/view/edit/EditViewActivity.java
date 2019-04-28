package com.stu.chatgirl.view.edit;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.liangmutian.mypicker.DataPickerDialog;
import com.example.liangmutian.mypicker.DatePickerDialog;
import com.example.liangmutian.mypicker.DateUtil;
import com.nex3z.flowlayout.FlowLayout;
import com.stu.chatgirl.R;
import com.stu.chatgirl.utils.SharedPreferencesUtils;
import com.stu.chatgirl.view.BaseGrilActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author peterliu
 */
public class EditViewActivity extends BaseGrilActivity implements View.OnClickListener {

    private Dialog dateDialog, chooseDialog;
    int hobby = 4;
    final int sign = 1;
    final int birthday = 3;
    final int sex = 5;
    private TextView result;
    private EditText etMessage;
    private int type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ((TextView) findViewById(R.id.tvBackTitle)).setText(getTitleString());
        View view = LayoutInflater.from(this).inflate(R.layout.activity_edit_view, null);
        ((LinearLayout) findViewById(R.id.container)).addView(view);
        result = ((TextView) view.findViewById(R.id.result));
        etMessage = (EditText) view.findViewById(R.id.etMessage);
        type = getIntent().getIntExtra("type", 0);
        getResult();
        findViewById(R.id.save).setVisibility(View.VISIBLE);
        findViewById(R.id.save).setOnClickListener(this);


        if (type == hobby) {
            result.setVisibility(View.GONE);
            etMessage.setVisibility(View.GONE);
            findViewById(R.id.save).setVisibility(View.GONE);
            List data = new ArrayList<String>();
            data.add("外语");
            data.add("舞蹈");
            data.add("看电影和听歌");
            data.add("吃零食");
            data.add("逛街");
            data.add("读书");
            data.add("看言情小说");
            data.add("爱美妆");
            FlowLayout flowLayout = (FlowLayout) findViewById(R.id.flow);
            for (int i = 0; i < data.size(); i++) {
                TextView textView = buildLabel((String) data.get(i));
                flowLayout.addView(textView);
            }
        }

    }

    private TextView buildLabel(final String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        textView.setPadding((int) dpToPx(16), (int) dpToPx(8), (int) dpToPx(16), (int) dpToPx(8));
        textView.setBackgroundResource(R.drawable.bg_love);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //et.setText(text);
            }
        });
        return textView;
    }

    private float dpToPx(float dp) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private void getResult() {
        Object o = SharedPreferencesUtils.getParam(this, type + "", "请编辑内容");
        result.setText((String) o);

        if (type == sign) {
            result.setVisibility(View.GONE);
            etMessage.setVisibility(View.VISIBLE);
            etMessage.setText((String) o);
            ((TextView) findViewById(R.id.save)).setText("保存");
        }
    }

    @Override
    public CharSequence getTitleString() {
        return "编辑内容";
    }

    @Override
    public void onClick(View v) {
        switch (type) {
            case birthday:
                showBirthdayDialog(DateUtil.getDateForString("1990-01-01"));
                break;
            case sex:
                ArrayList<String> list = new ArrayList<>();
                list.add("保密");
                list.add("男");
                list.add("女");
                showSexDialog(list);
                break;
            case sign:
                SharedPreferencesUtils.setParam(EditViewActivity.this, type + "", etMessage.getText().toString());
                Toast.makeText(EditViewActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                finish();
                break;
            default:
                break;
        }
    }


    private void showSexDialog(List<String> mlist) {
        DataPickerDialog.Builder builder = new DataPickerDialog.Builder(this);
        chooseDialog = builder.setData(mlist).setSelection(1).setTitle("取消")
                .setOnDataSelectedListener(new DataPickerDialog.OnDataSelectedListener() {
                    @Override
                    public void onDataSelected(String itemValue, int position) {
                        result.setText(itemValue);
                        SharedPreferencesUtils.setParam(EditViewActivity.this, type + "", result.getText().toString());
                    }

                    @Override
                    public void onCancel() {

                    }
                }).create();

        chooseDialog.show();
    }

    private void showBirthdayDialog(List<Integer> date) {
        DatePickerDialog.Builder builder = new DatePickerDialog.Builder(this);
        builder.setOnDateSelectedListener(new DatePickerDialog.OnDateSelectedListener() {
            @Override
            public void onDateSelected(int[] dates) {

                result.setText(dates[0] + "-" + (dates[1] > 9 ? dates[1] : ("0" + dates[1])) + "-"
                        + (dates[2] > 9 ? dates[2] : ("0" + dates[2])));
                SharedPreferencesUtils.setParam(EditViewActivity.this, type + "", result.getText().toString());
            }

            @Override
            public void onCancel() {

            }
        })

                .setSelectYear(date.get(0) - 1)
                .setSelectMonth(date.get(1) - 1)
                .setSelectDay(date.get(2) - 1);

        builder.setMaxYear(DateUtil.getYear());
        builder.setMaxMonth(DateUtil.getDateForString(DateUtil.getToday()).get(1));
        builder.setMaxDay(DateUtil.getDateForString(DateUtil.getToday()).get(2));
        dateDialog = builder.create();
        dateDialog.show();
    }
}

