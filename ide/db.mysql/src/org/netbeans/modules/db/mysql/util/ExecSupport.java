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

package org.netbeans.modules.db.mysql.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.modules.db.mysql.impl.MySQLDatabaseServer;
import org.openide.windows.InputOutput;


/**
 * Provides utility for rerouting process output to an output window
 * 
 * @author  ludo, David Van Couvering
 */
public class ExecSupport {
    private static final Logger LOGGER = Logger.getLogger(ExecSupport.class.getName());

    private OutputCopier[] copyMakers;

    /** Creates a new instance of ExecSupport */
    public ExecSupport() {
    }
    
    /**
     * Redirect the standard output and error streams of the child
     * process to an output window.
     */
    public InputOutput displayProcessOutputs(final Process child)
    throws IOException, InterruptedException {
        // Get a tab on the output window.  If this client has been
        // executed before, the same tab will be returned.
        InputOutput io = MySQLDatabaseServer.getOutput();
        try {
            io.getOut().reset();
        } catch (IOException e) {
            // not a critical error, continue
            LOGGER.log(Level.INFO, e.getLocalizedMessage(), e);
        }

        copyMakers = new OutputCopier[3];
        
        (copyMakers[0] = new OutputCopier(
                new InputStreamReader(child.getInputStream()), 
                io.getOut(), true)).start();
        
        (copyMakers[1] = new OutputCopier(
                new InputStreamReader(child.getErrorStream()), 
                io.getErr(), true)).start();
        
        (copyMakers[2] = new OutputCopier(io.getIn(), 
                new OutputStreamWriter(child.getOutputStream()), 
                true)).start();
        
        // Start a thread that listens to see when the process ends,
        // and then notifies the copyMakers that they are done
        new Thread() {
            @Override
            public void run() {
                try {
                    child.waitFor();
                    Thread.sleep(2000);  // time for copymakers
                } catch (InterruptedException e) {
                } finally {
                    try {
                        copyMakers[0].interrupt();
                        copyMakers[1].interrupt();
                        copyMakers[2].interrupt();
                    } catch (Exception e) {
                        LOGGER.log(Level.INFO, null, e);
                    }
                }
            }
        }.start();
        
        return io;
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
        
        
        public OutputCopier(Reader is, Writer os, boolean b) {
            this.os = os;
            this.is = is;
            autoflush = b;
        }
                
        /* Makes copy. */
        @Override
        public void run() {
            int read;
            char[] buff = new char [256];
            try {
                while ((read = read(is, buff, 0, 256)) > 0x0) {
                    if (os != null) {
                        os.write(buff,0,read);
                        
                        if (autoflush) {
                            os.flush();
                        }
                    }
                }
            } catch (IOException ex) {
                LOGGER.log(Level.FINE, null, ex);
            } catch (InterruptedException e) {
                LOGGER.log(Level.FINE, null, e);
            }
        }
        
        @Override
        public void interrupt() {
            super.interrupt();
            done = true;
        }
        
        private int read(Reader is, char[] buff, int start, int count) 
                throws InterruptedException, IOException {
            
            while (!is.ready() && !done) {
                sleep(100);
            }
            
            return is.read(buff, start, count);
        }

    }       
}
