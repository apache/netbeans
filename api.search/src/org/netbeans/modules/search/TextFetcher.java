/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
     * @param  receiver  displayer that will actually show the item in the UI
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
