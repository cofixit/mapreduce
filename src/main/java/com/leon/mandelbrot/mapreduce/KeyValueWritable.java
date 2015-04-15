package com.leon.mandelbrot.mapreduce;

import org.apache.hadoop.io.ArrayWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableFactories;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class KeyValueWritable implements Writable {

    private IntWritable key;
    private ArrayWritable value;

    public KeyValueWritable(IntWritable key, ArrayWritable value) {
        this.key = key;
        this.value = value;
    }

    public KeyValueWritable() {

    }

    public void set(IntWritable key, ArrayWritable value) {
        this.key = key;
        this.value = value;
    }

    public void setKey(IntWritable key) {
        this.key = key;
    }

    public void setValue(ArrayWritable value) {
        this.value = value;
    }

    public IntWritable getKey() {
        return this.key;
    }

    public ArrayWritable getValue() {
        return this.value;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        if (key != null) key.write(out);
        if (value != null) value.write(out);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        IntWritable readKey = new IntWritable();
        ArrayWritable readValue = new ArrayWritable(IntWritable.class);
        readKey.readFields(in);
        readValue.readFields(in);
        this.key = readKey;
        this.value = readValue;
    }
}