package com.bitty.history.examples;

import com.bitty.history.BHistory;
import com.bitty.history.HistoryEntry;

import java.util.Iterator;

/**
 * Created by julianliebl on 07.02.15.
 */
public class GetHistoryData {
    public static void main(String[] args) {
        BHistory bHistory = new BHistory("krakenEUR");

        bHistory.addStatusListener(new BHistory.BHistoryStatusListener() {
            @Override
            public void onUpdate() {

            }

            @Override
            public void onFinish() {
                Iterator<HistoryEntry> historyEntries = bHistory.getHistoryEntries();
                historyEntries.forEachRemaining(entry -> System.out.println(entry.toString()));
            }

            @Override
            public void onError() {

            }
        });

    }
}
