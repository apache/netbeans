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

package org.netbeans.modules.search;

import java.awt.EventQueue;
import java.io.IOException;
import java.nio.channels.ClosedByInterruptException;
import org.openide.filesystems.FileObject;
import org.openide.util.RequestProcessor;

/**
 * Fetches text from an Item off the event thread and passes it to a
 * TextReceiever on the event thread.
 *
 * @author Tim Boudreau
 */
final class TextFetcher implements Runnable {
    
    private final Item source;
    private final TextDisplayer textDisplayer;
    private final RequestProcessor.Task task;
    
    /** */
    private TextDetail location;                    //accessed only from the event DT
    /** */
    private boolean done = false;              //accessed only from the event DT
    /** */
    private boolean cancelled = false;         //accessed only from the event DT
    
    /** */
    private volatile String text = null;
    
    /**
     */
    TextFetcher(Item source,
                TextDisplayer receiver,
                RequestProcessor rp) {
        assert EventQueue.isDispatchThread();
        
        this.source = source;
        this.textDisplayer = receiver;
        this.location = source.getLocation();
        task = rp.post(this, 50);
    }

    
    void cancel() {
        assert EventQueue.isDispatchThread();
        
        cancelled = true;
        task.cancel();
    }

    @Override
    public void run() {
        if (EventQueue.isDispatchThread()) {
            if (cancelled) {
                return;
            }
            
            FileObject fob = source.matchingObj.getFileObject(); 
            String mimeType = fob.getMIMEType();
            //We don't want the swing html editor kit, and even if we 
            //do get it, it will frequently throw a random NPE 
            //in StyleSheet.removeHTMLTags that appears to be a swing bug
            if ("text/html".equals(mimeType)) {                         //NOI18N
                mimeType = "text/plain";                                //NOI18N
            }
            textDisplayer.setText(text,
                                  mimeType,
                                  getLocation());
            done = true;
        }  else {
            
            /* called from the request processor's thread */
            
            if (Thread.interrupted()) {
                return;
            }
            
            String invalidityDescription
                    = source.matchingObj.getInvalidityDescription();
            if (invalidityDescription != null) {
                text = invalidityDescription;
            } else {
                try {
                    text = source.matchingObj.getText();
                } catch (ClosedByInterruptException cbie) {
                    cancelled = true;
                    return;
                } catch (IOException ioe) {
                    text = ioe.getLocalizedMessage();
                    
    //                cancel();
                }
            }
            
            if (Thread.interrupted()) {
                return;
            }
            
            EventQueue.invokeLater(this);
        }
    }
    
    /**
     * If a new request comes to display the same file, just possibly at a
     * different location, simply change the location we're scheduled to
     * display and return true, else return false (in which case we'll be
     * cancelled and a new request will be scheduled).
     * 
     * @param  item  item to be shown
     * @param  textDisplayer displayer that will actually show the item in the UI
     * @return  {@code true} if the previous item has not been shown yet
     *          and we are about to show the same file, just at a possible
     *          different location;
     *          {@code false} otherwise
     */
    boolean replaceLocation(Item item, TextDisplayer textDisplayer) {
        assert EventQueue.isDispatchThread();
        
        if (done || (textDisplayer != this.textDisplayer)) {
            return false;
        }
        
        boolean result = source.matchingObj.getFileObject()
                         .equals(item.matchingObj.getFileObject());
        if (result) {
            setLocation(item.getLocation());
            task.schedule(50);
        }
        return result;
    }

    private synchronized void setLocation(TextDetail location) {
        assert EventQueue.isDispatchThread();
        
        this.location = location;
    }

    private synchronized TextDetail getLocation() {
        assert EventQueue.isDispatchThread();
        
        return location;
    }
}
