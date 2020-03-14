package com.broll.networklib;

import java.util.ArrayList;
import java.util.List;

public class PingStats implements PingTimeout{

    public final static int PING_MAX = -1;
    private final static int CHECK_COUNT = 10;
    private final static int TIMEOUT_DURATION = 1000;
    private List<Integer> pings = new ArrayList<>(CHECK_COUNT);
    private long lastCheckTime = System.currentTimeMillis();
    private boolean timeout;

    public PingStats(){
    }

    public void receivedPing(){
        timeout = false;
        int msDiff = getTimeDiff(true);
        pings.add(0,msDiff);
        int size = pings.size()-1;
        if(size>CHECK_COUNT){
            pings.remove(size);
        }
    }

    private int getTimeDiff(boolean update){
        long time = System.currentTimeMillis();
        int msDiff = (int)(time-lastCheckTime);
        lastCheckTime = time;
        return msDiff;
    }

    public int getPing(){
        if(timeout){
            return PING_MAX;
        }
        return pings.stream().reduce(0,(i1,i2)->i1+i2) / pings.size();
    }

    public boolean isTimeout() {
        return timeout;
    }

    @Override
    public void checkTimeout() {
        int msDiff = getTimeDiff(false);
        if(msDiff>= TIMEOUT_DURATION){
            timeout = true;
        }
    }
}
