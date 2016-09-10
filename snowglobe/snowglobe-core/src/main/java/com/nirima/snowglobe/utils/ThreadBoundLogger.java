package com.nirima.snowglobe.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;


public class ThreadBoundLogger<E> extends UnsynchronizedAppenderBase<E> {



    @Override
    protected void append(E eventObject) {
        LoggingEvent event = (LoggingEvent) eventObject;
        ThreadLogBase log = ThreadLog.get();
        if( !log.enabled() )
            return;

        String evt = event.toString() + "\n";

        try {
            log.write(evt.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }




}
