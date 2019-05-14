// Generated code from Butter Knife. Do not modify!
package com.netease.nim.chatroom.demo.education.activity;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class EnterRoomActivity$$ViewBinder<T extends com.netease.nim.chatroom.demo.education.activity.EnterRoomActivity> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131231003, "field 'roomTip'");
    target.roomTip = finder.castView(view, 2131231003, "field 'roomTip'");
    view = finder.findRequiredView(source, 2131231001, "field 'roomEdit'");
    target.roomEdit = finder.castView(view, 2131231001, "field 'roomEdit'");
    view = finder.findRequiredView(source, 2131230833, "field 'done' and method 'onClick'");
    target.done = finder.castView(view, 2131230833, "field 'done'");
    view.setOnClickListener(
      new butterknife.internal.DebouncingOnClickListener() {
        @Override public void doClick(
          android.view.View p0
        ) {
          target.onClick();
        }
      });
  }

  @Override public void unbind(T target) {
    target.roomTip = null;
    target.roomEdit = null;
    target.done = null;
  }
}
