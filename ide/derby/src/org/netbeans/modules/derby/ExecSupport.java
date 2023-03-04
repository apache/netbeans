/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
/*
 * ExecSupport.java
 *
 * Created on March 5, 2004, 12:57 PM
 */

package org.netbeans.modules.derby;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.windows.*;
/**
 *
 * @author  ludo
 */
public class ExecSupport {

    private String lookFor;
    private OutputCopier[] copyMakers;
    private Thread t;
    private Connect connect;
    private Map<String, Runnable> outputStringHandlers = null;
    private String displayName;
    private Process child;

    /** Creates a new instance of ExecSupport */
    public ExecSupport(final Process child, String displayName) {
        this.displayName = displayName;
        this.child = child;
    }

    /**
     * Redirect the standard output and error streams of the child process to an
     * output window.
     */
    public void start() {
        // Get a tab on the output window.  If this client has been
        // executed before, the same tab will be returned.
        InputOutput io = org.openide.windows.IOProvider.getDefault().getIO(
            displayName, false);
        try {
            io.getOut().reset();
        }
        catch (IOException e) {
            // not a critical error, continue
            Logger.getLogger("global").log(Level.INFO, null, e);
        }
        io.select();
        copyMakers = new OutputCopier[3];
        (copyMakers[0] = new OutputCopier(new InputStreamReader(child.getInputStream()), io.getOut(), true, lookFor, outputStringHandlers)).start();
        (copyMakers[1] = new OutputCopier(new InputStreamReader(child.getErrorStream()), io.getErr(), true, lookFor, outputStringHandlers)).start();
        (copyMakers[2] = new OutputCopier(io.getIn(), new OutputStreamWriter(child.getOutputStream()), true)).start();
        new Thread() {
            @Override
            public void run() {
                try {
                    int ret = child.waitFor();
                    Thread.sleep(2000);  // time for copymakers
                } catch (InterruptedException e) {
                } finally {
                    try {
                        copyMakers[0].interrupt();
                        copyMakers[1].interrupt();
                        copyMakers[2].interrupt();
                    } catch (Exception e) {
                        Logger.getLogger("global").log(Level.INFO, null, e);
                    }
                }
            }
        }.start();
    }
    
    public void setStringToLookFor(String lookFor) {
        this.lookFor = lookFor;
    }
    
    /**
     * Add an output string handler. If the specified string will be found in
     * the output stream, the handler will be invoked.
     *
     * @param string
     * @param handler
     */
    public void addOutputStringHandler(String string, Runnable handler) {
        if (outputStringHandlers == null) {
            outputStringHandlers = new HashMap<>();
        }
        outputStringHandlers.put(string, handler);
    }

    public boolean isStringFound() {
        if (copyMakers == null) {
            return false;
        }
        return (copyMakers[0].stringFound() ||
                copyMakers[1].stringFound() ||
                copyMakers[2].stringFound());
    }
    
    /** This thread simply reads from given Reader and writes read chars to given Writer. */
    public static  class OutputCopier extends Thread {
        final Writer os;
        final Reader is;
        /** while set to false at streams that writes to the OutputWindow it must be
         * true for a stream that reads from the window.
         */
        final boolean autoflush;
        private boolean done = false;
        private String stringToLookFor;
        private boolean stringFound = false;
        private Map<String, Runnable> outputStreamHandlers;
        
        private static final int FOUND = SearchUtil.FOUND;
        
        public OutputCopier(Reader is, Writer os, boolean b, String lookFor,
                Map<String, Runnable> outputStreamHandlers) {
            this.os = os;
            this.is = is;
            autoflush = b;
            this.stringToLookFor = lookFor;
            this.outputStreamHandlers = outputStreamHandlers;
        }
        
        public OutputCopier(Reader is, Writer os, boolean b) {
            this(is, os, b, null, null);
        }
        
        public boolean stringFound() {
            return stringFound;
        }
        
        /* Makes copy. */
        @Override
        public void run() {
            int read;
            int stringFoundChars = 0;
            Map<String, Integer> stringFoundCharsMap
                    = outputStreamHandlers != null
                    ? new HashMap<String, Integer>() : null;
            char[] buff = new char [256];
            try {
                while ((read = read(is, buff, 0, 256)) > 0x0) {
                    if (stringToLookFor != null) {
                        stringFoundChars = SearchUtil.checkForString(stringToLookFor, stringFoundChars, buff, read);
                        if (stringFoundChars == FOUND) {
                            stringToLookFor = null;
                            stringFound = true;
                        }
                    }
                    if (outputStreamHandlers != null) {
                        checkOutputHandlers(stringFoundCharsMap, buff, read);
                    }
                    if (os!=null){
                        os.write(buff,0,read);
                        if (autoflush) {
                            os.flush();
                        }
                    }
                }
            } catch (IOException | InterruptedException ex) {
            }
        }

        /**
         * Check whether the output contains some strings for which a handler
         * should be invoked.
         *
         * @param stringFoundCharsMap Map of pairs [string, prefix found
         * in previous iterations.]
         * @param buff Buffer with the current part of the output.
         * @param read How many characters have been read into the buffer.
         */
        private void checkOutputHandlers(
                Map<String, Integer> stringFoundCharsMap, char[] buff, int read) {
            assert outputStreamHandlers != null;
            for (Map.Entry<String, Runnable> e
                    : outputStreamHandlers.entrySet()) {
                Integer alreadyFoundOb = stringFoundCharsMap.get(
                        e.getKey());
                int alreadyFound = alreadyFoundOb == null ? 0 : alreadyFoundOb;
                int found = SearchUtil.checkForString(e.getKey(),
                        alreadyFound, buff, read);
                if (found == SearchUtil.FOUND) {
                    stringFoundCharsMap.remove(e.getKey());
                    e.getValue().run();
                } else if (found > 0) {
                    stringFoundCharsMap.put(e.getKey(), found);
                } else if (found == 0) {
                    stringFoundCharsMap.remove(e.getKey());
                }
            }
        }
        
        @Override
        public void interrupt() {
            super.interrupt();
            done = true;
        }
        
        private int read(Reader is, char[] buff, int start, int count) throws InterruptedException, IOException {
            
            while (!is.ready() && !done) {
                sleep(100);
            }
            
            return is.read(buff, start, count);
        }

    }

    /** Waits for startup of a server, waits until the message set through the setStringToLookFor() method. 
     *  @param timeout timeout
     *  @return true if the connection was successfully established, false if timed out
     */ 
    public boolean waitForMessage(int timeout) {
        int retryTime = 10;
        connect = new Connect(retryTime); 
        t = new Thread(connect);
        t.start();
        try {
            t.join(timeout);
        } catch(InterruptedException ie) {
        }
        if (t.isAlive()) {
            connect.finishLoop();
            t.interrupt();//for thread deadlock
        }
        return connect.getStatus();
    }

    public boolean interruptWaiting() {
        if (t == null) {
            return false;
        } else {
            if (t.isAlive()) {
                connect.finishLoop();
                t.interrupt();
                return true;
            } else {
                return false;
            }
        }
    }
    
    public void terminate() {
        this.child.destroy();
    }
    
    private class Connect implements Runnable  {

        int retryTime;
        boolean status = false;
        boolean loop = true;

        public Connect(int retryTime) {
            this.retryTime = retryTime; 
        } 

        public void finishLoop() {
            loop = false;
        }

        @Override
        public void run() {
            while (loop) {
                if (isStringFound()) {
                    status = true;
                    break;
                }
                try {
                    Thread.sleep(retryTime);
                } catch(InterruptedException ie) {
                }
            }
        }

        boolean getStatus() {
            return status;
        }
    }
    
    
    
}
