package com.netease.nim.student.question.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.student.work.question.R;
import com.student.work.question.activity.SearchActivity;


/**
 * @author peterliu
 */
public class SearchFragment extends Fragment {
    private View rootView;
    private TextView sv;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView=inflater.inflate(R.layout.fragment_search,container,false);
        this.rootView=rootView;
        sv=rootView.findViewById(R.id.tv_sv);
        sv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getActivity(), SearchActivity.class);
                startActivity(intent);
            }
        });
        return rootView;
    }
}
