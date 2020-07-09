package com.example.myapp2.commute;

public class Conv {
    public String date;
    public boolean seen;
//    public long timestamp;

    public Conv(String date, boolean seen) {
        this.date = date;
        this.seen = seen;
    }

    public  Conv(){

    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

//    public long getTimestamp() {
//        return timestamp;
//    }
//
//    public void setTimestamp(long timestamp) {
//        this.timestamp = timestamp;
//    }



//    public String getDate() {
//        return date;
//    }
//
//    public void setDate(String date) {
//        this.date = date;
//    }
//
//    public Conv(String date) {
//        this.date = date;
//    }
}
