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

package org.netbeans.modules.payara.common;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.ImageUtilities;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 * @author Peter Williams
 */
public class SimpleIO {

    /**
     * Time in milliseconds to wait between checks of the input stream.
     */
    private static final int DELAY = 1000;
    
    private final String name;
    private final InputOutput io;
    private final CancelAction cancelAction;
    private final AtomicReference<Process> process;
    
    public SimpleIO(String displayName, Process task) {
        name = displayName;
        process = new AtomicReference<Process>(task);
        cancelAction = new CancelAction();
        io = IOProvider.getDefault().getIO(displayName, new Action [] {
            cancelAction
        });
        io.select();
    }
    
    /**
     * Reads a newly included InputSreams
     *
     * @param inputStreams InputStreams to read
     */
    public void readInputStreams(InputStream... inputStreams) {
        RequestProcessor rp = RequestProcessor.getDefault();
        for(InputStream inputStream : inputStreams){
            rp.post(new IOReader(inputStream));
        }
    }

    /**
     * Writes a string to the output window
     * 
     * @param s string to be written
     */
    public synchronized void write(String s) {
        OutputWriter writer = io.getOut();
        writer.print(s);
        writer.flush();
    }

    /**
     * Selects output panel
     */
    public synchronized void selectIO() {
        io.select();
    }
    
    /**
     * Closes the output panel
     */
    public synchronized void closeIO() {
        // Don't close the window when finished -- in case of install or launching
        // failures, it makes problems easiesr for the user to diagnose.
        process.set(null);
        cancelAction.updateEnabled();
    }
    
    /**
     * Thread to read an I/O stream and write it to the output window managed
     */
    private class IOReader implements Runnable {
        
        private InputStream inputStream;
        
        public IOReader(InputStream inputStream) {
            this.inputStream = inputStream;
        }
        
        /**
         * Implementation of the Runnable interface. Here all tailing is
         * performed
         */
        public void run() {
            final String originalName = Thread.currentThread().getName();
            
            try {
                Thread.currentThread().setName(this.getClass().getName() + " - " + inputStream); // NOI18N
                
                // create a reader from the input stream
                Reader reader = new BufferedReader(new InputStreamReader(inputStream));
                
                // read from the input stream and put all the changes to the I/O window
                char [] chars = new char[1024];
                int len = 0;
                while(len != -1) {
                    while((len = reader.read(chars)) != -1) {
                        write(new String(chars, 0, len));
                        selectIO();
                        
                        if(!reader.ready()) {
                            break;
                        }
                    }
                    
                    // sleep for a while when the stream is empty
                    try {
                        Thread.sleep(DELAY);
                    } catch (InterruptedException e) {
                        // ignore
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger("payara").log(Level.INFO, ex.getLocalizedMessage(), ex); // NOI18N
            } finally {
                try {
                    inputStream.close();
                } catch (IOException ex) {
                    Logger.getLogger("payara").log(Level.INFO, ex.getLocalizedMessage(), ex); // NOI18N
                }
                
                Thread.currentThread().setName(originalName);
            }
        }
    }

    /** This action will be displayed in the server output window */
    public class CancelAction extends AbstractAction {
        
        private static final String PROP_ENABLED = "enabled"; // NOI18N
        private static final String ICON = 
                "org/netbeans/modules/payara/common/resources/stop.png"; // NOI18N
        
        public CancelAction() {
            super(NbBundle.getMessage(SimpleIO.class, "CTL_Cancel"),ImageUtilities.loadImageIcon(ICON, false)); // NOI18N
            putValue(SHORT_DESCRIPTION, NbBundle.getMessage(SimpleIO.class, "LBL_CancelDesc")); // NOI18N
        }

        public void actionPerformed(ActionEvent e) {
            if(process.get() != null) {
                String message = NbBundle.getMessage(SimpleIO.class, "MSG_QueryCancel", name); // NOI18N
                NotifyDescriptor nd = new NotifyDescriptor.Confirmation(message,
                        NotifyDescriptor.YES_NO_OPTION, NotifyDescriptor.QUESTION_MESSAGE);
                if(DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.YES_OPTION) {
                    Process p = process.getAndSet(null);
                    if(p != null) {
                        p.destroy();
                    } else {
                        Logger.getLogger("payara").log(Level.FINEST, "Process handle unexpectedly null, cancel aborted."); // NOI18N
                    }
                }
            }
        }

        @Override
        public boolean isEnabled() {
            return process.get() != null;
        }
        
        public void updateEnabled() {
            Mutex.EVENT.readAccess(new Runnable() {
                public void run() {
                    firePropertyChange(PROP_ENABLED, null, isEnabled() ? Boolean.TRUE : Boolean.FALSE);
                }
            });
        }
    }
    
}
