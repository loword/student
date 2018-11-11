package com.stu.chatgirl.view;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


import com.stu.chatgirl.R;
import com.stu.chatgirl.model.HttpUtils;
import com.stu.chatgirl.model.Msg;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @author peterliu
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText etMsg;
    private Button btnSend;
    private RecyclerView rvChat;

    private ChatAdapter chatAdapter;
    private ArrayList<Msg> list;

    private String[] keys;
    private String[] values;
    private String text;
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        btnSend.setOnClickListener(this);
    }

    private void initView() {
        etMsg = findViewById(R.id.et_msg);
        btnSend = findViewById(R.id.btn_send);
        rvChat = findViewById(R.id.rv_chat);
        rvChat.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        list = new ArrayList<>();
        list.add(new Msg("hello", 1));
        list.add(new Msg("我是一个美丽的女孩，我叫美美", 1));
        list.add(new Msg("很高兴可以给你解答问题", 1));

        chatAdapter = new ChatAdapter(this, list);
        rvChat.setAdapter(chatAdapter);

        keys = new String[3];
        values = new String[3];
        keys[0] = "key";
        values[0] = "85cfa02113b04d26a13908874922e613";
        keys[1] = "userid";
        values[1] = "hellorobot";
    }

    @Override
    public void onClick(View v) {
        list.add(new Msg(etMsg.getText().toString(), 0));
        chatAdapter.notifyItemInserted(chatAdapter.getItemCount() - 1);
        rvChat.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
        keys[2] = "info";
        values[2] = etMsg.getText().toString();
        etMsg.setText("");
        new Thread() {
            @Override
            public void run() {
                try {
                    text = new HttpUtils().sendPost("http://tuling123.com/openapi/api", keys, values);
                    readJSON(text);
                    list.add(new Msg(text, 1));
                    if (url != null) {
                        list.add(new Msg(url, 1));
                    }
                } catch (IOException e) {
                    Log.e("IOException", e.toString());
                }
                handler.sendEmptyMessage(0);
            }
        }.start();

    }

    public void readJSON(String strJson) {
        Log.i("strJson", strJson);
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(strJson);
            text = jsonObject.getString("text");
            if (jsonObject.has("url")) {
                url = jsonObject.getString("url");
            }
        } catch (JSONException e) {
            url = null;
            e.printStackTrace();
        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    chatAdapter.notifyItemInserted(chatAdapter.getItemCount() - 1);
                    rvChat.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
