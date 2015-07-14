package io.razem.bitty.history;

import org.mapdb.Serializer;
import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.ParseLong;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

/**
 * Created by julianliebl on 07.02.15.
 */
public class HistoryEntry implements Comparable<HistoryEntry> {
    private Long timestamp;
    private Double price;
    private Double amount;

    public static final CellProcessor[] processors = new CellProcessor[] {
            new NotNull(new ParseLong()),
            new NotNull(new ParseDouble()),
            new NotNull(new ParseDouble())
    };

    public HistoryEntry(){}

    public HistoryEntry(Long timestamp, Double price, Double amount){
        this.timestamp = timestamp;
        this.price = price;
        this.amount = amount;
    }

    public Long getTimestamp() {
        return timestamp * 1000;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "HistoryEntry{" +
                "timestamp=" + timestamp +
                ", price=" + price +
                ", amount=" + amount +
                '}';
    }

    @Override
    public int compareTo(HistoryEntry o) {
        return timestamp.compareTo(o.getTimestamp());
    }

    public static class MapDbSerializer implements Serializer<HistoryEntry>, Serializable {

        @Override
        public void serialize(DataOutput out, HistoryEntry obj) throws IOException {
            out.writeLong(obj.timestamp);
            out.writeDouble(obj.price);
            out.writeDouble(obj.amount);
        }

        @Override
        public HistoryEntry deserialize(DataInput in, int available) throws IOException {
            return  new HistoryEntry(in.readLong(), in.readDouble(), in.readDouble());
        }

        @Override
        public int fixedSize() {
            return 0;
        }
    }
}
