/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.payara.jakartaee.ide;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport;
import org.openide.ErrorManager;
import org.openide.util.RequestProcessor;
import org.openide.windows.InputOutput;


/**
 * This class is capable of tailing the specified file or input stream. It
 * checks for changes at the specified intervals and outputs the changes to
 * the given I/O panel in NetBeans
 *
 * @author  Michal Mocnak
 */
public class Hk2Logger {
    
    /**
     * Amount of time in milliseconds to wait between checks of the input
     * stream
     */
    private static final int delay = 1000;
    
    /**
     * Singleton model pattern
     */
    private static Map<String, Hk2Logger> instances = new HashMap<String, Hk2Logger>();
    
    /**
     * The I/O window where to output the changes
     */
    private InputOutput io;
    
    /**
     * Creates and starts a new instance of Hk2Logger
     * 
     * @param uri the uri of the server
     */
    private Hk2Logger(String uri) {
        io = UISupport.getServerIO(uri);
        
        if (io == null) {
            return; // finish, it looks like this server instance has been unregistered
        }
        
        // clear the old output
        try {
            io.getOut().reset();
        } catch (IOException ioe) {
            // no op
        }
        
        io.select();
    }
    
    /**
     * Returns uri specific instance of Hk2Logger
     * 
     * @param uri the uri of the server
     * @return uri specific instamce of OCHk2Logger
     */
    public static Hk2Logger getInstance(String uri) {
        if (!instances.containsKey(uri))
            instances.put(uri, new Hk2Logger(uri));
        
        return instances.get(uri);
    }
    
    /**
     * Reads a newly included InputSreams
     *
     * @param inputStreams InputStreams to read
     */
    public void readInputStreams(InputStream[] inputStreams) {
        for(InputStream inputStream : inputStreams)
            RequestProcessor.getDefault().post(new LoggerRunnable(inputStream));
    }
    
    /**     
     * Reads a newly included Files
     * 
     * @param files Files to read
     */
    public void readFiles(File[] files) {
        for(InputStream inputStream : getInputStreamsFromFiles(files))
            RequestProcessor.getDefault().post(new LoggerRunnable(inputStream));
    }
    
    /**
     * Writes a message into output
     * 
     * @param s message to write
     */
    public synchronized void write(String s) {
        io.getOut().print(s);
    }
    
    /**
     * Selects output panel
     */
    public synchronized void selectIO() {
        io.select();
    }
    
    private static InputStream[] getInputStreamsFromFiles(File[] files) {
        InputStream[] inputStreams = new InputStream[files.length];
        int i = 0;
        try {
            for(i=0 ; i<files.length ; i++)
                inputStreams[i] = new FileInputStream(files[i]);
        } catch(FileNotFoundException ex) {
            Logger.getLogger("payara-jakartaee").log(Level.INFO, files[i].getAbsolutePath(), ex); // NOI18N
            return new InputStream[] {};
        }
        
        return inputStreams;
    }
    
    private class LoggerRunnable implements Runnable {
        
        private InputStream inputStream;
        
        public LoggerRunnable(InputStream inputStream) {
            this.inputStream = inputStream;
        }
        
        /**
         * Implementation of the Runnable interface. Here all tailing is
         * performed
         */
        public void run() {
            try {
                // create a reader from the input stream
                InputStreamReader reader = new InputStreamReader(inputStream);
                
                // read from the input stream and put all the changes to the
                // I/O window
                char[] chars = new char[1024];
                while (true) {
                    // while there is something in the stream to be read - read that
                    while (reader.ready()) {
                        write(new String(chars, 0, reader.read(chars)));
                        selectIO();
                    }
                    
                    // when the stream is empty - sleep for a while
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        // do nothing
                    }
                }
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
            } finally {
                // close the opened stream
                try {
                    inputStream.close();
                } catch (IOException e) {
                    ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
                }
            }
        }
    }
}
