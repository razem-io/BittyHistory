package io.razem.bitty.history.examples;

import io.razem.bitty.history.BHistory;
import io.razem.bitty.history.HistoryEntry;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

/**
 * Created by julianliebl on 08.03.2015.
 */
public class Playground {

    private BHistory mBHistory;
    private Collection<HistoryEntry> historyEntries;
    
    public Playground(){
        mBHistory = new BHistory("btceUSD");
        mBHistory.addStatusListener(new BHistory.BHistoryStatusListener() {
            @Override
            public void onFinish() {
                historyEntries = mBHistory.getHistoryEntries();
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
        Date firstEntryDate = mBHistory.getFirstEntryDate();
        Date lastEntryDate = mBHistory.getLastEntryDate();

        System.out.println(firstEntryDate);
        System.out.println(lastEntryDate);

        Calendar calendar = Calendar.getInstance();
        calendar.set(2014,Calendar.JANUARY,1);
        Date minDate = calendar.getTime();
        calendar.set(2014,Calendar.DECEMBER,31);
        Date maxDate = calendar.getTime();

        System.out.println("Trades: " + historyEntries.stream().count());
        
        for(int year = 2012; year <= 2014; year++){
            System.out.println("Trades      " + year + ": " + countTradesForYear(year));
            System.out.println("$   Volume " + year + ": " + countCashVolumeForYear(year).intValue());
            System.out.println("BTC Volume " + year + ": " + countBitcoinVolumeForYear(year).intValue());
        }
    }
    
    public Long countTradesForYear(int year){
        Calendar calendar = Calendar.getInstance();
        calendar.set(year,Calendar.JANUARY,1);
        Date minDate = calendar.getTime();
        calendar.set(year,Calendar.DECEMBER,31);
        Date maxDate = calendar.getTime();

        return countTrades(minDate, maxDate);
    }
    
    public Long countTrades(Date minDate, Date maxDate){
        return historyEntries.stream()
                .filter(e -> e.getTimestamp() > minDate.getTime())
                .filter(e -> e.getTimestamp() < maxDate.getTime()).count();
    }

    public Double countCashVolumeForYear(int year){
        Calendar calendar = Calendar.getInstance();
        calendar.set(year,Calendar.JANUARY,1);
        Date minDate = calendar.getTime();
        calendar.set(year,Calendar.DECEMBER,31);
        Date maxDate = calendar.getTime();

        return countCashVolume(minDate, maxDate);
    }

    public Double countCashVolume(Date minDate, Date maxDate){
        return historyEntries.stream()
                .filter(e -> e.getTimestamp() > minDate.getTime())
                .filter(e -> e.getTimestamp() < maxDate.getTime())
                .mapToDouble(e -> e.getPrice() * e.getAmount())
                .sum();
    }

    public Double countBitcoinVolumeForYear(int year){
        Calendar calendar = Calendar.getInstance();
        calendar.set(year,Calendar.JANUARY,1);
        Date minDate = calendar.getTime();
        calendar.set(year,Calendar.DECEMBER,31);
        Date maxDate = calendar.getTime();

        return countBitcoinVolume(minDate, maxDate);
    }

    public Double countBitcoinVolume(Date minDate, Date maxDate){
        return historyEntries.stream()
                .filter(e -> e.getTimestamp() > minDate.getTime())
                .filter(e -> e.getTimestamp() < maxDate.getTime())
                .mapToDouble(HistoryEntry::getAmount)
                .sum();
    }




    public static void main(String[] args) {
        new Playground();
    }
}
