# BittyHistory
A library which will aid in retrieving and storing data from bitcoincharts.com. [MapDB](https://github.com/jankotek/MapDB) is used in order to be fast and access data without using to much memory.

## Usage:
Look here what symbol you need: http://api.bitcoincharts.com/v1/csv/

For example if you want to get the history data for the kraken exchange in € the following would apply:

```
BHistory bHistory = new BHistory("krakenEUR");
bHistory.addStatusListener(new BHistory.BHistoryStatusListener() {
    @Override
    public void onFinish() {
        Iterator<HistoryEntry> historyEntries = bHistory.getHistoryEntries();
        historyEntries.forEachRemaining(entry -> System.out.println(entry.toString()));
    }
    
    @Override
    public void onUpdate() {}

    @Override
    public void onError() {}
});
```
