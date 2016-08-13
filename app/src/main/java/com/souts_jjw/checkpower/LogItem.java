package com.souts_jjw.checkpower;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class LogItem implements java.io.Serializable {

    private long id;
    private String date;
    private long datetime;
    private String content;

    public LogItem() {
        content = "";
    }

    public LogItem(String content) {
        this.datetime = System.currentTimeMillis();
        this.date = getLocaleDate();
        this.content = content;
    }

    public LogItem(String date, String content) {
        this.datetime = System.currentTimeMillis();
        this.date = date;
        this.content = content;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getDatetime() {
        return datetime;
    }

    // 裝置區域的日期時間
    public String getLocaleDatetime() {
        return (String) android.text.format.DateFormat.format("yyyy-MM-dd hh:mm:ss", new Date(datetime));
    }

    // 裝置區域的日期
    public String getLocaleDate() {
        return (String) android.text.format.DateFormat.format("yyyy-MM-dd", new Date(datetime));
    }

    // 裝置區域的時間
    public String getLocaleTime() {
        return (String) android.text.format.DateFormat.format("hh:mm:ss", new Date(datetime));
    }

    public void setDatetime(long datetime) {
        this.datetime = datetime;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!LogItem.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        final LogItem other = (LogItem) obj;
        if ((this.date == null) ? (other.date != null) : !this.date.equals(other.date)) {
            return false;
        }
        if (this.datetime != other.datetime) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.date != null ? this.date.hashCode() : 0);
        hash = 53 * hash + Integer.parseInt(String.valueOf(this.datetime));
        return hash;
    }

}
