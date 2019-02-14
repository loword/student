package com.stu.chatgirl.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.stu.chatgirl.R;
import com.stu.chatgirl.model.HttpUtils;
import com.stu.chatgirl.model.Msg;
import com.stu.chatgirl.utils.SharedPreferencesUtils;
import com.stu.chatgirl.utils.StatusBarUtils;

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

    private String text;
    private String url;
    private boolean sex;
    private String chatKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMyTitle();
        initView();
        btnSend.setOnClickListener(this);
        sex = ((boolean) SharedPreferencesUtils.getParam(MainActivity.this, "sex", false));
    }

    private void getIntentValue(Intent intent) {
        chatKey = intent.getStringExtra("chatKey");
        if (!TextUtils.isEmpty(chatKey)) {
            sendChatContent(chatKey);
        }
    }

    private void setMyTitle() {
        setTheme(R.style.CustomTitleBarTheme);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_main);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.my_title_bar);
        StatusBarUtils.setStatusBarColor(this, android.R.color.holo_red_light);
    }

    private void initView() {
        etMsg = findViewById(R.id.et_msg);
        btnSend = findViewById(R.id.btn_send);
        rvChat = findViewById(R.id.rv_chat);
        rvChat.setLayoutManager(new WrapContentLinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        list = new ArrayList<>();
        list.add(new Msg("你好呀！,我叫美美,我可以陪你聊天", 1));
        list.add(new Msg("对了，也可以帮你查询日常信息，如天气，百科全书，明星。。。  太多了 ，太多了。。。 ", 1));
        chatAdapter = new ChatAdapter(this, list, sex);
        rvChat.setAdapter(chatAdapter);
    }

    @Override
    public void onClick(View v) {
        final String content = etMsg.getText().toString();
        sendChatContent(content);
    }

    private void sendChatContent(final String content) {
        list.add(new Msg(content, 0));
        chatAdapter.notifyItemInserted(chatAdapter.getItemCount() - 1);
        rvChat.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
        etMsg.setText("");

        new Thread() {
            @Override
            public void run() {
                try {
                    text = new HttpUtils().sendPost(content);
                    readJSON(text);
                    list.add(new Msg(text, 1));
                    if (url != null && !TextUtils.isEmpty(url)) {
                        list.add(new Msg("哼！自己打开看 。。。" + url, 1));
                    }
                    handler.sendEmptyMessage(0);
                } catch (IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast toast = Toast.makeText(MainActivity.this, "系统维护中，请稍等...", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                        }
                    });
                    Log.e("IOException", e.toString());
                }
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
            } else {
                url = null;
            }
        } catch (JSONException e) {
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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null) {
            getIntentValue(intent);
        }
    }

    public void onSetting(View view) {
        startActivity(new Intent(this, SettingActivity.class));
    }

}
