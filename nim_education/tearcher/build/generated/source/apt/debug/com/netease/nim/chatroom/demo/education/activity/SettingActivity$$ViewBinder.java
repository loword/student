// Generated code from Butter Knife. Do not modify!
package com.netease.nim.chatroom.demo.education.activity;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class SettingActivity$$ViewBinder<T extends com.netease.nim.chatroom.demo.education.activity.SettingActivity> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131231086, "field 'toolbar'");
    target.toolbar = finder.castView(view, 2131231086, "field 'toolbar'");
    view = finder.findRequiredView(source, 2131231007, "field 'rtsRecordOpenRadio'");
    target.rtsRecordOpenRadio = finder.castView(view, 2131231007, "field 'rtsRecordOpenRadio'");
    view = finder.findRequiredView(source, 2131231005, "field 'rtsRecordCloseRadio'");
    target.rtsRecordCloseRadio = finder.castView(view, 2131231005, "field 'rtsRecordCloseRadio'");
    view = finder.findRequiredView(source, 2131231006, "field 'rtsRecordGroup'");
    target.rtsRecordGroup = finder.castView(view, 2131231006, "field 'rtsRecordGroup'");
    view = finder.findRequiredView(source, 2131230970, "field 'platformBuiltinRadio'");
    target.platformBuiltinRadio = finder.castView(view, 2131230970, "field 'platformBuiltinRadio'");
    view = finder.findRequiredView(source, 2131231019, "field 'sdkBuiltinRadio'");
    target.sdkBuiltinRadio = finder.castView(view, 2131231019, "field 'sdkBuiltinRadio'");
    view = finder.findRequiredView(source, 2131230831, "field 'disableAudioEffectRadio'");
    target.disableAudioEffectRadio = finder.castView(view, 2131230831, "field 'disableAudioEffectRadio'");
    view = finder.findRequiredView(source, 2131230775, "field 'audioEffectGroup'");
    target.audioEffectGroup = finder.castView(view, 2131230775, "field 'audioEffectGroup'");
  }

  @Override public void unbind(T target) {
    target.toolbar = null;
    target.rtsRecordOpenRadio = null;
    target.rtsRecordCloseRadio = null;
    target.rtsRecordGroup = null;
    target.platformBuiltinRadio = null;
    target.sdkBuiltinRadio = null;
    target.disableAudioEffectRadio = null;
    target.audioEffectGroup = null;
  }
}
