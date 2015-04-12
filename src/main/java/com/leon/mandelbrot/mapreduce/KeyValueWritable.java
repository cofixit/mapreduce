package com.leon.mandelbrot.mapreduce;

import org.apache.hadoop.io.Writable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class KeyValueWritable<K extends Writable, V extends Writable> implements Writable {

    private K key;
    private V value;

    public KeyValueWritable(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public KeyValueWritable() {

    }

    public void set(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public void setValue(V value) {
        this.value = value;
    }

    public K getKey() {
        return this.key;
    }

    public V getValue() {
        return this.value;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        key.write(out);
        value.write(out);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        key.readFields(in);
        value.readFields(in);
    }
}