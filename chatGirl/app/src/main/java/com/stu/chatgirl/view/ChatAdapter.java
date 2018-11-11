package com.stu.chatgirl.view;

import android.content.Context;
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
    private static final int OTHRE = 1;

    private List<Msg> list = new ArrayList<>();

    public ChatAdapter(Context context, ArrayList<Msg> list) {
        this.context = context;
        this.list = list;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvContentMe;
        private final TextView tvContextRobot;
        LinearLayout me;
        LinearLayout other;

        public ViewHolder(View itemView) {
            super(itemView);
            me = (LinearLayout) itemView.findViewById(R.id.me);
            other = (LinearLayout) itemView.findViewById(R.id.other);
            tvContentMe = ((TextView) itemView.findViewById(R.id.tvContextMe));
            tvContextRobot = ((TextView) itemView.findViewById(R.id.tvContextRobot));
        }

        public LinearLayout getMe() {
            return me;
        }

        public LinearLayout getOther() {
            return other;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        ViewHolder viewHolder = null;

        switch (viewType) {
            case ME:
                viewHolder = new ViewHolder(LayoutInflater.from(context).inflate(R.layout.chat_item_me, parent, false));
                break;
            case OTHRE:
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
                /**
                 * 当recyclerview承载的数据过多的时候，去滑动recyclerview，
                 * 划出屏幕以外的item会重新绑定数据，如果这个时候绑定数据的方式是
                 * viewgroup的addView（）方法的话，会出现item添加很多重复的view
                 * 所以这之前应该执行清除里面view的操作，即removeAllViews（）
                 */
                viewHolder.getMe().removeAllViews();
                tv.setBackgroundResource(R.mipmap.chat_me);
                viewHolder.getMe().addView(tv);
                break;
            case OTHRE:
                viewHolder.getOther().removeAllViews();
                tv.setBackgroundResource(R.mipmap.chat_other);
                viewHolder.getOther().addView(tv);
                break;
        }
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(int position) {
        return list.get(position).getType() == 0 ? ME : OTHRE;
    }

    public void addMsg(Msg msg) {
        list.add(msg);
    }
}
