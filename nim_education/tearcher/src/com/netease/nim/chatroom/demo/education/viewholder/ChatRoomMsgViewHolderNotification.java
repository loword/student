package com.netease.nim.chatroom.demo.education.viewholder;

import android.widget.TextView;

import com.netease.nim.chatroom.demo.R;
import com.netease.nim.chatroom.demo.im.session.viewholder.MsgViewHolderBase;
import com.netease.nim.chatroom.demo.education.helper.ChatRoomNotificationHelper;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomNotificationAttachment;

public class ChatRoomMsgViewHolderNotification extends MsgViewHolderBase {

    protected TextView notificationTextView;

    @Override
    protected int getContentResId() {
        return R.layout.nim_message_item_notification;
    }

    @Override
    protected void inflateContentView() {
        notificationTextView = (TextView) view.findViewById(R.id.message_item_notification_label);
    }

    @Override
    protected void bindContentView() {
        notificationTextView.setText(ChatRoomNotificationHelper.getNotificationText((ChatRoomNotificationAttachment) message.getAttachment()));
    }

    @Override
    protected boolean isMiddleItem() {
        return true;
    }
}

