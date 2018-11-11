package com.stu.chatgirl.view;

import android.content.Context;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.stu.chatgirl.R;
import com.stu.chatgirl.model.Msg;

import java.util.ArrayList;
import java.util.List;

/**
 * @author peterliu
 */
public class ChatAdapter extends RecyclerView.Adapter {

    private Context context;

    private static final int ME = 0;
    private static final int ROBOT = 1;

    private List<Msg> list = new ArrayList<>();

    public ChatAdapter(Context context, ArrayList<Msg> list) {
        this.context = context;
        this.list = list;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout me;
        LinearLayout robot;

        public ViewHolder(View itemView) {
            super(itemView);
            me = (LinearLayout) itemView.findViewById(R.id.me);
            robot = (LinearLayout) itemView.findViewById(R.id.other);
        }

        public LinearLayout getMe() {
            return me;
        }

        public LinearLayout getRobot() {
            return robot;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        ViewHolder viewHolder = null;

        switch (viewType) {
            case ME:
                viewHolder = new ViewHolder(LayoutInflater.from(context).inflate(R.layout.chat_item_me, parent, false));
                break;
            case ROBOT:
                viewHolder = new ViewHolder(LayoutInflater.from(context).inflate(R.layout.chat_item_robot, parent, false));
                break;
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder) holder;
        TextView tv = new TextView(context);
        Msg msg = list.get(position);
        tv.setText(msg.getMsg());
        tv.setAutoLinkMask(Linkify.ALL);
        tv.setMovementMethod(LinkMovementMethod.getInstance());

        switch (msg.getType()) {
            case ME:
                tv.setPadding(25, 25, 35, 25);
                viewHolder.getMe().removeAllViews();
                tv.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_robot_me));
                viewHolder.getMe().addView(tv);
                break;
            case ROBOT:
                tv.setPadding(35, 25, 25, 25);
                viewHolder.getRobot().removeAllViews();
                tv.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_robot_chat));
                viewHolder.getRobot().addView(tv);
                break;
        }
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(int position) {
        return list.get(position).getType() == 0 ? ME : ROBOT;
    }
}
