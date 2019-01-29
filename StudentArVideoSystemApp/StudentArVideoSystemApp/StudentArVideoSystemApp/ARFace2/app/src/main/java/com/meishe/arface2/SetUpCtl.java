package com.meishe.arface2;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.meishe.arface2.beauty_shape.BeautyShapeAdapter;
import com.meishe.arface2.beauty_shape.BeautyShapeListItem;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Administrator on 2017/10/24.
 */

public class SetUpCtl extends LinearLayout {

    private static final String TAG = "SetUpCtl";
    private TextView m_tabLeftTxt;
    private TextView m_tabRightTxt;
    private View m_tabLeftLine;
    private View m_tabRightLine;
    private Button m_stickerBtn;
    private Button m_beautyBtn;
    private RelativeLayout m_recordLayout;
    private LinearLayout m_tabLayout;
    private RelativeLayout m_contentLayout;
    private RelativeLayout m_bottomLayout;
    private LinearLayout m_rootLayout;
    private ImageView m_recordImageView;
    private TextView m_recordTxt;
    private RecyclerView m_filterList;
    private RecyclerView m_stickerList;
    private RelativeLayout m_stickerLayout;
    private RelativeLayout m_filterLayout;
    private LinearLayout m_beautyColorLayout;
    private LinearLayout m_beautyShapeLayout;
     private Button m_resetBeautyColorBtn;
    private Button m_resetBeautyShapeBtn;
    private SeekBar m_meiBaiSeekBar;
    private TextView m_meiBaiTxt;
    private SeekBar m_blurSeekBar;
    private TextView m_blurTxt;
    private SeekBar m_ruddySeekBar;
    private TextView m_ruddyTxt;
    private RecyclerView m_beautyShapeTypeRv;
    private BeautyShapeAdapter m_beautyShapeTypeAdapter;
    private List<BeautyShapeListItem> m_beautyShapeTypeData = new ArrayList<>();
    private SeekBar m_beautyShapeSeekbar;
    private TextView m_beautyShapeTxt;
    private String m_currentBeautyShapeType;

    private ItemSelectAdapter m_DaojuRecyclerAdapter;
    private ItemSelectAdapter m_FilterRecyclerAdapter;
    private Context m_context;
    private static final int STICKER_MODE = 0;
    private static final int BEAUTY_MODE = 1;
    private int m_mode = -1;
    private SetUpCtlListener m_listener;



    public SetUpCtl(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        m_context = context;
        LayoutInflater.from(context).inflate(R.layout.set_up_ctl, this);
        init();
        initStickerList();
    }

    public void setListener(SetUpCtlListener listener){
        m_listener = listener;
        initListener();
    }

    private void init(){
        m_tabLeftTxt = (TextView) findViewById(R.id.tab_left);
        m_tabRightTxt = (TextView) findViewById(R.id.tab_right);
        m_beautyBtn = (Button) findViewById(R.id.beauty_btn);
        m_stickerBtn = (Button) findViewById(R.id.sticker_btn);
        m_recordLayout = (RelativeLayout) findViewById(R.id.record_layout);
        m_recordImageView = (ImageView) findViewById(R.id.record_imageview);
        m_recordTxt = (TextView) findViewById(R.id.record_txt);
        m_tabLeftLine = findViewById(R.id.tab_left_line);
        m_tabRightLine = findViewById(R.id.tab_right_line);
        m_tabLayout = (LinearLayout) findViewById(R.id.tab_layout);
        m_contentLayout = (RelativeLayout) findViewById(R.id.content_layout);
        m_bottomLayout = (RelativeLayout) findViewById(R.id.bottom_layout);
        m_rootLayout = (LinearLayout) findViewById(R.id.root);
        m_recordImageView = (ImageView) findViewById(R.id.record_imageview);
        m_recordTxt = (TextView) findViewById(R.id.record_txt);
        m_filterList = (RecyclerView) findViewById(R.id.filter_list);
        m_stickerList = (RecyclerView) findViewById(R.id.sticker_list);
        m_stickerLayout = (RelativeLayout) findViewById(R.id.sticker_layout);
        m_filterLayout = (RelativeLayout) findViewById(R.id.filter_layout);
        m_beautyColorLayout = (LinearLayout) findViewById(R.id.beauty_color);
        m_beautyShapeLayout = (LinearLayout) findViewById(R.id.beauty_shape);
        m_resetBeautyColorBtn = (Button) findViewById(R.id.reset_beauty_color_btn);
        m_blurSeekBar = (SeekBar) findViewById(R.id.blur_seekbar);
        m_blurTxt = (TextView) findViewById(R.id.blur_txt);
        m_ruddySeekBar = (SeekBar) findViewById(R.id.ruddy_seekbar);
        m_ruddyTxt = (TextView) findViewById(R.id.ruddy_txt);
        m_meiBaiSeekBar = (SeekBar) findViewById(R.id.meibai_seekbar);
        m_meiBaiTxt = (TextView) findViewById(R.id.meibai_txt);
        m_beautyShapeTypeRv = (RecyclerView) findViewById(R.id.beauty_shape_type_rv);
        m_beautyShapeSeekbar = (SeekBar) findViewById(R.id.beauty_shape_seekbar);
        m_beautyShapeTxt = (TextView) findViewById(R.id.beauty_shape_txt);
        m_resetBeautyShapeBtn = (Button) findViewById(R.id.reset_beauty_shape_btn);


        m_tabLeftTxt.setSelected(true);
        m_tabLayout.setVisibility(GONE);
        m_contentLayout.setVisibility(GONE);
        m_rootLayout.setBackgroundColor(Color.parseColor("#00000000"));

        m_blurSeekBar.setProgress(22);
        m_ruddySeekBar.setProgress(22);
        m_meiBaiSeekBar.setProgress(22);

        m_beautyShapeSeekbar.setMax(200);
        m_beautyShapeSeekbar.setProgress(100);
        m_beautyShapeTxt.setText("0");
        initBeautyShapeTypeList();
    }

    public void setBlurProgress(int progress){
        m_blurSeekBar.setProgress(progress);
    }

    public void setWhiteProgress(int progress){
        m_meiBaiSeekBar.setProgress(progress);
    }

    public void setRuddyProgress(int progress){
        m_ruddySeekBar.setProgress(progress);
    }

    public void resetData(){
        m_resetBeautyColorBtn.callOnClick();
    }

    public void hideContent(){

        m_bottomLayout.setVisibility(VISIBLE);
        if(m_mode == STICKER_MODE){
            m_tabLayout.setVisibility(INVISIBLE);
            m_contentLayout.setVisibility(INVISIBLE);
            m_rootLayout.setBackgroundColor(Color.parseColor("#00000000"));
            m_stickerBtn.getBackground().setLevel(1);
            return;
        }

        if(m_mode == BEAUTY_MODE){
            m_tabLayout.setVisibility(INVISIBLE);
            m_contentLayout.setVisibility(INVISIBLE);
            m_rootLayout.setBackgroundColor(Color.parseColor("#00000000"));
            m_beautyBtn.getBackground().setLevel(1);
            return;
        }
    }

    public void setRecordImageViewSelected(){
        if(m_recordImageView.isSelected()){
            m_recordImageView.setSelected(false);
            m_recordTxt.setVisibility(VISIBLE);
            m_beautyBtn.setVisibility(VISIBLE);
            m_stickerBtn.setVisibility(VISIBLE);

            if(m_stickerBtn.getBackground().getLevel() == 2 || m_beautyBtn.getBackground().getLevel() == 2){
                m_tabLayout.setVisibility(VISIBLE);
                m_contentLayout.setVisibility(VISIBLE);
                m_rootLayout.setBackgroundColor(Color.parseColor("#CCffffff"));
            }

        }else{
            m_recordImageView.setSelected(true);
            m_recordTxt.setVisibility(INVISIBLE);
            m_beautyBtn.setVisibility(INVISIBLE);
            m_stickerBtn.setVisibility(INVISIBLE);
            m_tabLayout.setVisibility(INVISIBLE);
            m_contentLayout.setVisibility(INVISIBLE);
            m_rootLayout.setBackgroundColor(Color.parseColor("#00000000"));

        }
    }

    private void initListener(){
        m_tabLeftTxt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                m_tabLeftTxt.setSelected(true);
                m_tabRightTxt.setSelected(false);
                m_tabLeftLine.setVisibility(VISIBLE);
                m_tabRightLine.setVisibility(INVISIBLE);

                if(m_mode == STICKER_MODE){
                    m_stickerLayout.setVisibility(VISIBLE);
                    m_filterLayout.setVisibility(GONE);
                    m_beautyColorLayout.setVisibility(GONE);
                    m_beautyShapeLayout.setVisibility(GONE);
                }else if(m_mode == BEAUTY_MODE){
                    m_stickerLayout.setVisibility(GONE);
                    m_filterLayout.setVisibility(GONE);
                    m_beautyColorLayout.setVisibility(VISIBLE);
                    m_beautyShapeLayout.setVisibility(GONE);
            }
            }
        });

        m_tabRightTxt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                m_tabRightTxt.setSelected(true);
                m_tabLeftTxt.setSelected(false);
                m_tabRightLine.setVisibility(VISIBLE);
                m_tabLeftLine.setVisibility(INVISIBLE);

                if(m_mode == STICKER_MODE){
                    m_stickerLayout.setVisibility(GONE);
                    m_filterLayout.setVisibility(VISIBLE);
                    m_beautyColorLayout.setVisibility(GONE);
                    m_beautyShapeLayout.setVisibility(GONE);
                }else if(m_mode == BEAUTY_MODE){
                    m_stickerLayout.setVisibility(GONE);
                    m_filterLayout.setVisibility(GONE);
                    m_beautyColorLayout.setVisibility(GONE);
                    m_beautyShapeLayout.setVisibility(VISIBLE);
                }
            }
        });

        m_stickerBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(m_stickerBtn.getBackground().getLevel() == 2){
                    m_tabLayout.setVisibility(INVISIBLE);
                    m_contentLayout.setVisibility(INVISIBLE);
                    m_stickerBtn.getBackground().setLevel(1);
                    m_rootLayout.setBackgroundColor(Color.parseColor("#00000000"));
                    m_bottomLayout.setVisibility(VISIBLE);
                    return;
                }
                if(m_mode == STICKER_MODE){
                    m_tabLayout.setVisibility(VISIBLE);
                    m_contentLayout.setVisibility(VISIBLE);
                    m_rootLayout.setBackgroundColor(Color.parseColor("#CCffffff"));
                    m_stickerBtn.getBackground().setLevel(2);
                    m_bottomLayout.setVisibility(GONE);
                    return;
                }
                m_stickerBtn.getBackground().setLevel(2);
                m_beautyBtn.getBackground().setLevel(1);
                m_tabLeftTxt.setText("动态贴纸");
                m_tabRightTxt.setText("视频滤镜");
                m_tabLayout.setVisibility(VISIBLE);
                m_contentLayout.setVisibility(VISIBLE);
                m_bottomLayout.setVisibility(GONE);
                m_rootLayout.setBackgroundColor(Color.parseColor("#CCffffff"));

                m_mode = STICKER_MODE;
                m_tabLeftTxt.callOnClick();

            }
        });

        m_beautyBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                if(m_beautyBtn.getBackground().getLevel() == 2){
                    m_tabLayout.setVisibility(INVISIBLE);
                    m_contentLayout.setVisibility(INVISIBLE);
                    m_beautyBtn.getBackground().setLevel(1);
                    m_rootLayout.setBackgroundColor(Color.parseColor("#00000000"));
                    m_bottomLayout.setVisibility(VISIBLE);
                    return;
                }

                if(m_mode == BEAUTY_MODE){
                    m_tabLayout.setVisibility(VISIBLE);
                    m_contentLayout.setVisibility(VISIBLE);
                    m_rootLayout.setBackgroundColor(Color.parseColor("#CCffffff"));
                    m_beautyBtn.getBackground().setLevel(2);
                    m_bottomLayout.setVisibility(GONE);
                    return;
                }

                m_beautyBtn.getBackground().setLevel(2);
                m_stickerBtn.getBackground().setLevel(1);
                m_tabLeftTxt.setText("美颜");
                m_tabRightTxt.setText("美型");
                m_tabLayout.setVisibility(VISIBLE);
                m_contentLayout.setVisibility(VISIBLE);
                m_bottomLayout.setVisibility(GONE);
                m_rootLayout.setBackgroundColor(Color.parseColor("#CCffffff"));

                m_mode = BEAUTY_MODE;
                m_tabLeftTxt.callOnClick();
            }
        });

        m_recordLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                if(m_listener != null){
                    m_listener.onRecordBtnClicked();
                }
            }
        });

        m_meiBaiSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                m_meiBaiTxt.setText(i + "");
                if(m_listener != null){
                   m_listener.onWhiteSelected(i, 100);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        m_blurSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                m_blurTxt.setText(i + "");
                if(m_listener != null){
                    m_listener.onBlurSelected(i, 100);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        m_ruddySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                m_ruddyTxt.setText(i + "");
                if(m_listener != null){
                    m_listener.onRuddySelected(i, 100);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        m_resetBeautyColorBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(m_listener != null){
                    m_listener.onBeautyColorReset();
                }
            }
        });

        m_resetBeautyShapeBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                m_beautyShapeTxt.setText("0");
                m_beautyShapeSeekbar.setProgress(100);
                if(m_listener != null){
                    m_listener.onBeautyShapeValue(m_currentBeautyShapeType, 100, m_beautyShapeSeekbar.getMax());
                }
            }
        });

        m_beautyShapeSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                String str = "Face Length Warp Ratio";
                if(str.equals(m_currentBeautyShapeType)) {
                    m_beautyShapeTxt.setText(String.valueOf(i/2));
                } else {
                    m_beautyShapeTxt.setText(String.valueOf(i-100));
                }
                if(b) {
                    if(m_listener != null){
                        m_listener.onBeautyShapeValue(m_currentBeautyShapeType, i, m_beautyShapeSeekbar.getMax());
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public void setStickerata(ArrayList list){
        ItemSelectAdapter.STICER_NAME = list;
        initStickerList();
    }

    private void initStickerList(){
        m_stickerList.setLayoutManager(new LinearLayoutManager(m_context, LinearLayoutManager.HORIZONTAL, false));
        m_DaojuRecyclerAdapter = new ItemSelectAdapter(m_stickerList, ItemSelectAdapter.RECYCLEVIEW_TYPE_EFFECT);
        m_DaojuRecyclerAdapter.setOnItemSelectedListener(new ItemSelectAdapter.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int itemPosition) {
                Log.d(TAG, "daoju selected " + itemPosition);
                if(m_listener != null) {
                    m_listener.onDaojuItemSelected(ItemSelectAdapter.STICER_NAME.get(itemPosition).toString());
                }
            }
        });

        m_stickerList.setAdapter(m_DaojuRecyclerAdapter);
        SpaceItemDecoration decoration = new SpaceItemDecoration(dip2px(m_context, 12));
        m_stickerList.addItemDecoration(decoration);
    }


    public void setCaptureFxData(ArrayList list){
        ItemSelectAdapter.FILTERS_NAME = list;
        initFilterList();
    }

    private void initFilterList(){
        m_filterList.setLayoutManager(new LinearLayoutManager(m_context, LinearLayoutManager.HORIZONTAL, false));
        m_FilterRecyclerAdapter = new ItemSelectAdapter(m_filterList, ItemSelectAdapter.RECYCLEVIEW_TYPE_FILTER);
        m_FilterRecyclerAdapter.setOnItemSelectedListener(new ItemSelectAdapter.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int itemPosition) {
                Log.d(TAG, "filter selected " + itemPosition);
                if(m_listener != null){
                    m_listener.onFilterSelected(ItemSelectAdapter.FILTERS_NAME.get(itemPosition).toString());
                }
            }
        });

        m_filterList.setAdapter(m_FilterRecyclerAdapter);
        SpaceItemDecoration decoration = new SpaceItemDecoration(dip2px(m_context, 12));
        m_filterList.addItemDecoration(decoration);
    }


    private int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public void setBeautyShapeProgress(int progress, float value){
        m_beautyShapeSeekbar.setProgress(progress);
        m_beautyShapeTxt.setText(String.valueOf((int) value));
    }

    private void initBeautyShapeTypeList() {
        if (m_beautyShapeTypeAdapter == null) {
            BeautyShapeListItem item_none0 = new BeautyShapeListItem();
            item_none0.nameCH = "瘦脸";
            item_none0.image_drawable = ContextCompat.getDrawable(m_context, R.mipmap.beauty_shape_face);
            m_beautyShapeTypeData.add(item_none0);

            BeautyShapeListItem item_none1 = new BeautyShapeListItem();
            item_none1.nameCH = "大眼";
            item_none1.image_drawable = ContextCompat.getDrawable(m_context, R.mipmap.beauty_shape_eye);
            m_beautyShapeTypeData.add(item_none1);

            BeautyShapeListItem item_none2 = new BeautyShapeListItem();
            item_none2.nameCH = "下巴";
            item_none2.image_drawable = ContextCompat.getDrawable(m_context, R.mipmap.beauty_shape_jaw);
            m_beautyShapeTypeData.add(item_none2);

            BeautyShapeListItem item_none3 = new BeautyShapeListItem();
            item_none3.nameCH = "小脸";
            item_none3.image_drawable = ContextCompat.getDrawable(m_context, R.mipmap.beauty_shape_face_little);
            m_beautyShapeTypeData.add(item_none3);

            BeautyShapeListItem item_none4 = new BeautyShapeListItem();
            item_none4.nameCH = "窄脸";
            item_none4.image_drawable = ContextCompat.getDrawable(m_context, R.mipmap.beauty_shape_face_thin);
            m_beautyShapeTypeData.add(item_none4);

            BeautyShapeListItem item_none5 = new BeautyShapeListItem();
            item_none5.nameCH = "额头";
            item_none5.image_drawable = ContextCompat.getDrawable(m_context, R.mipmap.beauty_shape_forehead);
            m_beautyShapeTypeData.add(item_none5);

            BeautyShapeListItem item_none6 = new BeautyShapeListItem();
            item_none6.nameCH = "瘦鼻";
            item_none6.image_drawable = ContextCompat.getDrawable(m_context, R.mipmap.beauty_shape_nose);
            m_beautyShapeTypeData.add(item_none6);

            BeautyShapeListItem item_none7 = new BeautyShapeListItem();
            item_none7.nameCH = "长鼻";
            item_none7.image_drawable = ContextCompat.getDrawable(m_context, R.mipmap.beauty_shape_nose_long);
            m_beautyShapeTypeData.add(item_none7);

            BeautyShapeListItem item_none8 = new BeautyShapeListItem();
            item_none8.nameCH = "眼角";
            item_none8.image_drawable = ContextCompat.getDrawable(m_context, R.mipmap.beauty_shape_eye_corner);
            m_beautyShapeTypeData.add(item_none8);

            BeautyShapeListItem item_none9 = new BeautyShapeListItem();
            item_none9.nameCH = "嘴形";
            item_none9.image_drawable = ContextCompat.getDrawable(m_context, R.mipmap.beauty_shape_mouse_shape);
            m_beautyShapeTypeData.add(item_none9);

            BeautyShapeListItem item_none10 = new BeautyShapeListItem();
            item_none10.nameCH = "嘴角";
            item_none10.image_drawable = ContextCompat.getDrawable(m_context, R.mipmap.beauty_shape_mouse_corner);
            m_beautyShapeTypeData.add(item_none10);

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(m_context, LinearLayoutManager.HORIZONTAL, false);
            m_beautyShapeTypeAdapter = new BeautyShapeAdapter(m_context, m_beautyShapeTypeData);
            m_beautyShapeTypeRv.setLayoutManager(linearLayoutManager);
            m_beautyShapeTypeRv.setAdapter(m_beautyShapeTypeAdapter);

            m_beautyShapeTypeAdapter.setOnItemClickListener(new BeautyShapeAdapter.OnItemClickListener() {
                @Override
                public void fxSelected(int pos, BeautyShapeListItem audioFxListItem) {
                    switch (pos) {
                        case 0: // 瘦脸
                            m_currentBeautyShapeType = "Shrink Face Ratio";
                            break;
                        case 1: // 大眼
                            m_currentBeautyShapeType = "Eye Enlarge Ratio";
                            break;
                        case 2: // 下巴
                            m_currentBeautyShapeType = "Chin Warp Ratio";
                            break;
                        case 3: // 小脸
                            m_currentBeautyShapeType = "Face Length Warp Ratio";
                            break;
                        case 4: // 窄脸
                            m_currentBeautyShapeType = "Face Width Warp Ratio";
                            break;
                        case 5: // 额头
                            m_currentBeautyShapeType = "Forehead Warp Ratio";
                            break;
                        case 6: // 瘦鼻
                            m_currentBeautyShapeType = "Nose Width Warp Ratio";
                            break;
                        case 7: // 长鼻
                            m_currentBeautyShapeType = "Nose Length Warp Ratio";
                            break;
                        case 8: // 眼角
                            m_currentBeautyShapeType = "Eye Corner Stretch Ratio";
                            break;
                        case 9: // 嘴形
                            m_currentBeautyShapeType = "Mouth Size Warp Ratio";
                            break;
                        case 10: // 嘴角
                            m_currentBeautyShapeType = "Mouth Corner Lift Ratio";
                            break;
                    }
                    if(m_listener != null) {
                        m_listener.onBeautyShapeSelected(m_currentBeautyShapeType);
                    }
                }
            });
        }
    }

    private class SpaceItemDecoration extends RecyclerView.ItemDecoration{
        private int mSpace;

        public SpaceItemDecoration(int space){
            mSpace = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);

            outRect.left = mSpace;
            outRect.right = mSpace;
        }

    }

    public interface SetUpCtlListener{
        // 道具
        void onDaojuItemSelected(String effectItemName);
        // 滤镜
        void onFilterSelected(String filterName);
        // 磨皮
        void onBlurSelected(int progress, int max);
        // 美白
        void onWhiteSelected(int progress, int max);
        // 红润
        void onRuddySelected(int progress, int max);
        // 拍摄
        void onRecordBtnClicked();
        // 美型强度
        void onBeautyShapeSelected(String beauty_shape_type);
        void onBeautyShapeValue(String beauty_shape_type, int progress, int max);
        // 重置
        void onBeautyColorReset();
    }


}
