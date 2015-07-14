package io.razem.bitty.history;

import com.google.common.base.Stopwatch;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.prefs.CsvPreference;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * Created by julianliebl on 03.02.2015.
 */
public class BHistory {
    private static final String WORKING_DIR = "./";

    private static final String BITCOIN_CHARTS_BASE_URL = "http://api.bitcoincharts.com/v1/csv/";
    private static final String BITCOIN_CHARTS_BASE_EXT = ".csv.gz";
    private static final String BITCOIN_CHARTS_BASE_CSV = ".csv";

    private static final String CSV_PATH = "data/csv";
    private static final String DB_PATH  = "data/db";
    private static final String TMP_PATH = "data/tmp";

    private static final Integer DATA_TIMEOUT_MS = 1000 * 60 * 15; // 15 minutes

    private String mSymbol;

    private File mSymbolCsvFile;
    private File mCsvPath;
    private File mTmpPath;
    private File mDbPath;

    private DB mDb;
    private BTreeMap<Long, HistoryEntry> historyEntriesMap;

    private List<BHistoryStatusListener> mStatusListenersList;

    public interface BHistoryStatusListener{
        public void onUpdate();
        public void onFinish();
        public void onError();
    }

    public BHistory(String symbol) {
        mSymbol = symbol;

        mTmpPath = new File(WORKING_DIR  + "/" + TMP_PATH);
        mCsvPath = new File(WORKING_DIR  + "/" + CSV_PATH);
        mDbPath  = new File(WORKING_DIR  + "/" + DB_PATH);

        mSymbolCsvFile = new File(mCsvPath.getAbsolutePath() + "/" + mSymbol + BITCOIN_CHARTS_BASE_CSV);

        mStatusListenersList = new ArrayList<>();
    }
    
    public void execute(){
        initLevel1();
    }

    public void addStatusListener(BHistoryStatusListener listener){
        mStatusListenersList.add(listener);
    }

    public void removeStatusListener(BHistoryStatusListener listener){
        mStatusListenersList.remove(listener);
    }

    public Collection<HistoryEntry> getHistoryEntries(){
        return historyEntriesMap.values();
    }
    
    
    
    public Date getFirstEntryDate(){
        Long firstEntryTimestamp = historyEntriesMap.keySet().first();
        return new Date(firstEntryTimestamp * 1000);
    }

    public Date getLastEntryDate(){
        Long lastEntryTimestamp = historyEntriesMap.keySet().last();
        return new Date(lastEntryTimestamp * 1000);
    }

    private void initLevel1(){
        checkDirs();

        if(!mSymbolCsvFile.exists()) {
            download();
        }else{
            initLevel2();
        }
    }

    private void initLevel2(){
        mDb = DBMaker.newFileDB(new File("data/db/" + mSymbol))
                .transactionDisable()
                .mmapFileEnableIfSupported()
                .closeOnJvmShutdown()
                .make();

        historyEntriesMap = mDb.createTreeMap("HistoryEntries").valueSerializer(new HistoryEntry.MapDbSerializer()).makeOrGet();

        if(historyEntriesMap.isEmpty()) {
            readCSV();
        }

        mStatusListenersList.forEach(BHistoryStatusListener::onFinish);
    }

    private void readCSV() {
        try (ICsvBeanReader inFile = new CsvBeanReader(new FileReader(mSymbolCsvFile), CsvPreference.STANDARD_PREFERENCE)) {
            try {
                String[] header = {"timestamp", "price", "amount"};
                HistoryEntry historyEntry;
                Stopwatch stopwatch = Stopwatch.createStarted();
                while ((historyEntry = inFile.read(HistoryEntry.class, header, HistoryEntry.processors)) != null) {
                    historyEntriesMap.put(historyEntry.getTimestamp(), historyEntry);
                }                
                System.out.println("Size: " + historyEntriesMap.size() + " took: " + stopwatch);
                mDb.commit();
            } finally {
                inFile.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean checkDirs(){
        if(!mTmpPath.exists() && !mTmpPath.mkdirs()){
            System.out.println("Could not create TMP folder.");
            return false;
        }

        if(!mCsvPath.exists() && !mCsvPath.mkdirs()){
            System.out.println("Could not create CSV folder.");
            return false;
        }

        if(!mDbPath.exists() && !mDbPath.mkdirs()){
            System.out.println("Could not create DB folder.");
            return false;
        }
        return true;
    }

    private boolean isOldData(){
        long lastModifiedTimestamp = mSymbolCsvFile.lastModified();
        long currentTimestamp = System.currentTimeMillis();

        return lastModifiedTimestamp < currentTimestamp - DATA_TIMEOUT_MS;
    }

    private void download(){
        try {
            URL website = new URL(BITCOIN_CHARTS_BASE_URL + mSymbol + BITCOIN_CHARTS_BASE_EXT);
            final Download download = new Download(website, TMP_PATH);
            download.addObserver((o, arg) -> {
                System.out.print("\rDownloading: " + download.getProgress().intValue() + "%");

                if(download.getStatus() == Download.COMPLETE){
                    System.out.println("");
                    System.out.println("Download finished. Extracting csv...");
                    decompress();
                    //TODO: implement check if everything is valid
                    System.out.println("Extracting complete.");
                    initLevel2();
                }
            });

        } catch (MalformedURLException e) {
            // We are pretty sure that the url is just fine. So lets just ignore the exception.
        }
    }

    public void decompress(){
        byte[] buffer = new byte[1024];

        File compressedFile = new File(mTmpPath.getAbsolutePath() + "/" + mSymbol + BITCOIN_CHARTS_BASE_EXT);

        try{
            GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(compressedFile));

            FileOutputStream out = new FileOutputStream(mSymbolCsvFile);

            int len;
            while ((len = gzis.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }

            gzis.close();
            out.close();

            if(compressedFile.exists()){
                System.out.println("Deleting compressed file.");
                if(!compressedFile.setWritable(true) || !compressedFile.delete()){
                    System.out.println("Was not able to delete file. You can remove the file manually here: -> " + compressedFile.getAbsoluteFile());
                }
            }else{
                System.out.println("Did not find compressed file -> " +  compressedFile.getAbsolutePath() + " Nothing to delete.");
            }

        }catch(IOException ex){
            ex.printStackTrace();
            System.out.println("Can't decompress downloaded file.");
        }
    }

    public static void main(String[] args) {
        new BHistory("krakenEUR");
    }
}
