package com.bitty.history;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

/**
 * Created by julianliebl on 08.03.2015.
 */
public class Playground {

    private BHistory mBHistory;
    
    public Playground(){
        mBHistory = new BHistory("btceUSD");
        mBHistory.addStatusListener(new BHistory.BHistoryStatusListener() {
            @Override
            public void onFinish() {
                doTheMath();
            }

            @Override
            public void onUpdate() {}

            @Override
            public void onError() {}
        });
        mBHistory.execute();
    }
    
    public void doTheMath(){
        Collection<HistoryEntry> historyEntries = mBHistory.getHistoryEntries();

        Date firstEntryDate = mBHistory.getFirstEntryDate();
        Date lastEntryDate = mBHistory.getLastEntryDate();

        System.out.println(firstEntryDate);
        System.out.println(lastEntryDate);

        Calendar calendar = Calendar.getInstance();
        calendar.set(2014,Calendar.JANUARY,1);
        Date minDate = calendar.getTime();
        calendar.set(2014,Calendar.DECEMBER,31);
        Date maxDate = calendar.getTime();
        historyEntries.stream()
                .filter(e -> e.getTimestamp() > minDate.getTime())
                .filter(e -> e.getTimestamp() > minDate.getTime())
    }
    
    public static void main(String[] args) {
        new Playground();
    }
}
