// Generated code from Butter Knife. Do not modify!
package com.netease.nim.chatroom.demo.education.activity;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class IdentifyActivity$$ViewBinder<T extends com.netease.nim.chatroom.demo.education.activity.IdentifyActivity> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131230818, "field 'teacherBtn' and method 'onClick'");
    target.teacherBtn = finder.castView(view, 2131230818, "field 'teacherBtn'");
    view.setOnClickListener(
      new butterknife.internal.DebouncingOnClickListener() {
        @Override public void doClick(
          android.view.View p0
        ) {
          target.onClick(p0);
        }
      });
    view = finder.findRequiredView(source, 2131231028, "field 'studentBtn' and method 'onClick'");
    target.studentBtn = finder.castView(view, 2131231028, "field 'studentBtn'");
    view.setOnClickListener(
      new butterknife.internal.DebouncingOnClickListener() {
        @Override public void doClick(
          android.view.View p0
        ) {
          target.onClick(p0);
        }
      });
  }

  @Override public void unbind(T target) {
    target.teacherBtn = null;
    target.studentBtn = null;
  }
}
