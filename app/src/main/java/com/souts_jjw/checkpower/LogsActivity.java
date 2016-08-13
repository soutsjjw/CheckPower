package com.souts_jjw.checkpower;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class LogsActivity extends Activity {

    private GroupListAdapter adapter = null;
    private ListView listView = null;
    GlobalVariable globalVariable;
    private List<LogItem> list = new ArrayList<LogItem>();
    private List<LogItem> listTag = new ArrayList<LogItem>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logs);

        globalVariable = (GlobalVariable)getApplicationContext();

        setData();
        adapter = new GroupListAdapter(this, list, listTag);
        listView = (ListView)findViewById(R.id.listView);
        listView.setEmptyView(findViewById(R.id.empty));
        listView.setAdapter(adapter);
    }

    public void setData(){
        List<String> group = globalVariable.getLogsGroup();
        List<LogItem> logs;

        for(String g: group) {
            list.add(new LogItem(g, ""));
            listTag.add(new LogItem(g, ""));

            logs = globalVariable.getLogsByDate(g, true);
            for(LogItem l: logs) {
                list.add(l);
            }
        }
    }

    private static class GroupListAdapter extends ArrayAdapter<LogItem> {

        private List<LogItem> listTag = null;

        public GroupListAdapter(Context context, List<LogItem> objects, List<LogItem> tags) {
            super(context, 0, objects);
            this.listTag = tags;
        }

        @Override
        public boolean isEnabled(int position) {
            if(listTag.contains(getItem(position))){
                return false;
            }
            return super.isEnabled(position);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;

            if(listTag.contains(getItem(position))) {
                view = LayoutInflater.from(getContext()).inflate(R.layout.group_list_item_tag, null);

                TextView tvTag = (TextView) view.findViewById(R.id.tvTag);
                tvTag.setText(getItem(position).getDate());
            }else{
                view = LayoutInflater.from(getContext()).inflate(R.layout.group_list_item, null);

                TextView tvDateTime = (TextView)view.findViewById(R.id.tvDateTime);
                TextView tvContent = (TextView)view.findViewById(R.id.tvContent);

                tvDateTime.setText(String.valueOf(getItem(position).getLocaleDatetime()));
                tvContent.setText(getItem(position).getContent());
            }

            return view;
        }
    }
}
