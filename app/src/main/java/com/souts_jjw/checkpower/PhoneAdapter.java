package com.souts_jjw.checkpower;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

public class PhoneAdapter extends BaseAdapter {

    private List<String> arrays = null;
    private Context mContext;
    private Button curDel_btn;
    private float x, ux;

    public PhoneAdapter(Context mContext) {
        this.mContext = mContext;
        arrays = new ArrayList<String>();
    }

    public PhoneAdapter(Context mContext, List<String> arrays) {
        this.mContext = mContext;
        this.arrays = arrays;
    }

    @Override
    public int getCount() {
        return this.arrays.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void add(String input) {
        arrays.add(input);
        notifyDataSetChanged();
    }

    public void clear() {
        arrays = new ArrayList<String>();
        notifyDataSetChanged();
    }

    public List<String> getList() {
        return arrays;
    }

    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder = null;

        if (view == null) {
            viewHolder = new ViewHolder();
            view = LayoutInflater.from(mContext).inflate(R.layout.listitem, null);
            viewHolder.tvTitle = (TextView)view.findViewById(R.id.title);
            viewHolder.btnDel = (Button)view.findViewById(R.id.del);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)view.getTag();
        }

        // 為每一個view項設置觸控監聽
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                final ViewHolder holder = (ViewHolder) view.getTag();
                // 當按下時處理
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    // 設置背景為選中狀態
                    view.setBackgroundResource(R.drawable.mm_listitem_pressed);
                    // 獲取按下時的x軸座標
                    x = motionEvent.getX();
                    // 判斷之前是否出現了刪除按鈕，如果存在就隱藏
                    if (curDel_btn != null) {
                        curDel_btn.setVisibility(View.GONE);
                    }
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) { // 鬆開處理
                    // 設置背景為未選中正常狀態
                    view.setBackgroundResource(R.drawable.mm_listitem_simple);
                    // 獲取鬆開時的x座標
                    ux = motionEvent.getX();
                    // 判斷當前項目中按鈕控件不為空時
                    if (holder.btnDel != null) {
                        // 按下和鬆開絕對值差當大於20時顯示刪除按鈕，否刪不顯示
                        if (Math.abs(x - ux) > 20) {
                            holder.btnDel.setVisibility(View.VISIBLE);
                            curDel_btn = holder.btnDel;
                        }
                    }
                } else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) { // 當滑動時時背景為選中狀態
                    view.setBackgroundResource(R.drawable.mm_listitem_pressed);
                } else { // 其他模式
                    view.setBackgroundResource(R.drawable.mm_listitem_simple);
                }

                return true;
            }
        });

        viewHolder.tvTitle.setText(this.arrays.get(position));
        // 為刪除按鈕添加監聽事件，實現點擊刪除按鈕時刪除該項
        viewHolder.btnDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (curDel_btn != null) {
                    curDel_btn.setVisibility(View.GONE);
                    arrays.remove(position);
                    notifyDataSetChanged();
                }
            }
        });

        return view;
    }

    final static class ViewHolder {
        TextView tvTitle;
        Button btnDel;
    }
}
