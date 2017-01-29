package com.downloadtool.arielmoreno.downloadtool;

/**
 * Created by Ariel.Moreno on 26/01/2017.
 */

/**
 * THis is interface defines hte functionality needed on the downloading tools to define whats going to happen with data after downloaded
 */
public interface ActionDataAdapter {
    void PerformOperation(byte[] data);
}

