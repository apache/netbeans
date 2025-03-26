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
package org.openide.text;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import org.netbeans.junit.Filter;
import org.netbeans.junit.NbTestCase;
import org.openide.cookies.EditorCookie;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Miloslav Metelka
 */
public class CloneableEditorSupportOpenCloseTest extends NbTestCase
implements CloneableEditorSupport.Env {
    
    private CloneableEditorSupport support;
    /** the content of lookup of support */
    private InstanceContent ic;
    /** Delay in miliseconds to simulate delay between closing stream and file modification time. */
    private long delay = 0L;

    // Env variables
    private String content = "";
    private boolean valid = true;
    private boolean modified = false;
    /** if not null contains message why this document cannot be modified */
    private String cannotBeModified;
    private java.util.Date date = new java.util.Date ();
    private java.util.List<java.beans.PropertyChangeListener> propL = new ArrayList<PropertyChangeListener>();
    private java.beans.VetoableChangeListener vetoL;

    public CloneableEditorSupportOpenCloseTest(String name) {
        super(name);
        List<String> includes = new ArrayList<String>();
//        includes.add("testOpenParallel");
//        includes.add("testOpenFromOpenNotification");
//        includes.add("testCloseWhileOpening");
//        filterTests(includes);
    }

    private void filterTests(List<String> includeTestNames) {
        List<Filter.IncludeExclude> includeTests = new ArrayList<Filter.IncludeExclude>();
        for (String testName : includeTestNames) {
            includeTests.add(new Filter.IncludeExclude(testName, ""));
        }
        Filter filter = new Filter();
        filter.setIncludes(includeTests.toArray(new Filter.IncludeExclude[0]));
        setFilter(filter);
    }

    @Override
    protected void setUp () {
        ic = new InstanceContent ();
        support = new CES (this, new AbstractLookup (ic));
    }
    
    public void testOpenFromOpenNotification() throws Exception {
        content = "Ahoj\nMyDoc";
        final CountDownLatch latch1 = new CountDownLatch(1);
        support.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (EditorCookie.Observable.PROP_DOCUMENT.equals(evt.getPropertyName())) {
                    try {
                        if (evt.getNewValue() != null) { // Document opened
                            support.openDocument();
                            latch1.countDown();
                        }
                    } catch (IOException ex) {
                        throw new IllegalStateException(ex);
                    }
                }
            }
        });
        support.openDocument();

        latch1.await();
    }

    public void testOpenParallel() throws Exception {
        content = "Ahoj\nMyDoc";
        final CountDownLatch latch1 = new CountDownLatch(1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    support.openDocument();
                    latch1.countDown();
                } catch (IOException ex) {
                    throw new IllegalStateException(ex);
                }
            }
        }).start();
        support.openDocument();

        latch1.await();
    }

    public void testCloseWhileOpening() throws Exception {
        content = "Ahoj\nMyDoc";
        final CountDownLatch latch1 = new CountDownLatch(1);
        final CountDownLatch latch2 = new CountDownLatch(2);
        support.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (latch2.getCount() == 0) {
                    return;
                }
                if (EditorCookie.Observable.PROP_DOCUMENT.equals(evt.getPropertyName())) {
                    try {
                        if (evt.getNewValue() != null) {
                            latch1.countDown();
                            Thread.sleep(100);//XXX: hack we need the support.openDocument to start waiting
                            support.openDocument();
                            latch2.countDown();
                        } else {
                            support.openDocument();
                            latch2.countDown();
                        }
                    } catch (IOException ex) {
                        throw new IllegalStateException(ex);
                    } catch (InterruptedException ex) {
                        throw new IllegalStateException(ex);
                    }
                }
            }
        });
        support.openDocument();

        latch1.await();
        support.close();
        // The test would not proceed past the following latch2.await() so it's commented out. Description follows.
//        latch2.await();
        // Since CES.closeDocument() is now asynchronous it allows to cancel a pending close task
        // that was not processed yet. The execution flow of the threads is the following:
        // M (main thread); DPRP (Document Processing RP thread)
        // M: openDocument(): schedules open task to DPRP followed by extra task for firing doc change (to DPRP)
        // DPRP: open task finished.
        // M: latch1.await() waiting...
        // DPRP: firing doc change task started. proceeds to listener's latch1.countDown() and waits on Thread.sleep(100)
        // M: proceeds to close() call which schedules a close task
        // DPRP: Thread.sleep(100) finishes and openDocument() finds a pending close task which it cancels
        //       so the currently opened document is retained. The "else" part of the listener is never called.
    }

    //
    // Implementation of the CloneableEditorSupport.Env
    //
    
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        propL.add (l);
    }    
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        propL.remove (l);
    }
    
    public synchronized void addVetoableChangeListener(java.beans.VetoableChangeListener l) {
        assertNull ("This is the first veto listener", vetoL);
        vetoL = l;
    }
    public void removeVetoableChangeListener(java.beans.VetoableChangeListener l) {
        assertEquals ("Removing the right veto one", vetoL, l);
        vetoL = null;
    }
    
    public org.openide.windows.CloneableOpenSupport findCloneableOpenSupport() {
        return support;
    }
    
    public String getMimeType() {
        return "text/plain";
    }
    
    public java.util.Date getTime() {
        return date;
    }
    
    public java.io.InputStream inputStream() throws java.io.IOException {
        return new java.io.ByteArrayInputStream (content.getBytes ());
    }
    public java.io.OutputStream outputStream() throws java.io.IOException {
        class ContentStream extends java.io.ByteArrayOutputStream {
            @Override
            public void close() throws java.io.IOException {
                super.close ();
                content = new String (toByteArray ());
                date = new Date(System.currentTimeMillis() + delay);
            }
        }
        
        return new ContentStream ();
    }
    
    public boolean isValid() {
        return valid;
    }
    
    public boolean isModified() {
        return modified;
    }

    public void markModified() throws java.io.IOException {
        if (cannotBeModified != null) {
            final String notify = cannotBeModified;
            IOException e = new IOException () {
                @Override
                public String getLocalizedMessage () {
                    return notify;
                }
            };
            Exceptions.attachLocalizedMessage(e, cannotBeModified);
            throw e;
        }
        
        modified = true;
    }
    
    public void unmarkModified() {
        modified = false;
    }

    /** Implementation of the CES */
    private static final class CES extends CloneableEditorSupport {
        public CES (CloneableEditorSupport.Env env, Lookup l) {
            super (env, l);
        }
        
        protected String messageName() {
            return "Name";
        }
        
        protected String messageOpened() {
            return "Opened";
        }
        
        protected String messageOpening() {
            return "Opening";
        }
        
        protected String messageSave() {
            return "Save";
        }
        
        protected String messageToolTip() {
            return "ToolTip";
        }
        
    }
}
