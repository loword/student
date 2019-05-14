package com.netease.nim.chatroom.demo.education.adapter;

import android.content.Context;

import com.netease.nim.chatroom.demo.base.ui.TAdapter;
import com.netease.nim.chatroom.demo.base.ui.TAdapterDelegate;
import com.netease.nim.chatroom.demo.education.helper.VideoListener;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMember;

import java.util.List;

/**
 * Created by hzxuwen on 2016/4/24.
 */
public class OnlinePeopleAdapter extends TAdapter {
    public static class OnlinePeopleItem {
        private String creator;
        private ChatRoomMember chatRoomMember;

        public OnlinePeopleItem(String creator, ChatRoomMember chatRoomMember) {
            this.creator = creator;
            this.chatRoomMember = chatRoomMember;
        }

        public String getCreator() {
            return creator;
        }

        public ChatRoomMember getChatRoomMember() {
            return chatRoomMember;
        }

        public void setChatRoomMember(ChatRoomMember chatRoomMember) {
            this.chatRoomMember = chatRoomMember;
        }
    }

    private VideoListener videoListener;

    public VideoListener getVideoListener() {
        return videoListener;
    }

    public OnlinePeopleAdapter(Context context, List<?> items, TAdapterDelegate delegate, VideoListener videoListener) {
        super(context, items, delegate);
        this.videoListener = videoListener;
    }
}
