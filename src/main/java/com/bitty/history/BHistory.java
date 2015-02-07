package com.bitty.history;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;
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
    private static final String TMP_PATH = "data/tmp";

    private static final Integer DATA_TIMEOUT_MS = 1000 * 60 * 15; // 15 minutes

    private String mSymbol;

    private File mSymbolCsvFile;
    private File mCsvPath;
    private File mTmpPath;

    public BHistory(String symbol) {
        mSymbol = symbol;

        mTmpPath = new File(WORKING_DIR  + "/" + TMP_PATH + "/");
        mCsvPath = new File(WORKING_DIR  + "/" + CSV_PATH + "/");
        mSymbolCsvFile = new File(mCsvPath.getAbsolutePath() + "/" + mSymbol + BITCOIN_CHARTS_BASE_CSV);

        checkDirs();

        if(!mSymbolCsvFile.exists() || isOldData()) {
            download();
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
            download.addObserver(new Observer() {
                @Override
                public void update(Observable o, Object arg) {
                    System.out.print("\rDownloading: " + download.getProgress().intValue() + "%");

                    if(download.getStatus() == Download.COMPLETE){
                        System.out.println("");
                        System.out.println("Download finished. Extracting csv...");
                        decompress();
                        //TODO: implement check if everything is valid
                        System.out.println("Data fetched or still fresh. We are all good!");
                    }
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
