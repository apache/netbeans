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

package org.openide.text;

import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;
import org.openide.util.Task;
import org.openide.windows.CloneableOpenSupport;
import org.openide.windows.CloneableTopComponent;

public class Deadlock169717Test extends NbTestCase
implements CloneableEditorSupport.Env {
    static {
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }
    /** the support to work with */
    private transient CES support;

    // Env variables
    private transient String content = "";
    private transient boolean valid = true;
    private transient boolean modified = false;
    private transient Date date = new Date ();
    private transient List/*<PropertyChangeListener>*/ propL = new ArrayList ();
    private transient VetoableChangeListener vetoL;
    
    private static Deadlock169717Test RUNNING;
    
    public Deadlock169717Test(String s) {
        super(s);
    }
    
    protected void setUp () {
        support = new CES (this, Lookup.EMPTY);
        RUNNING = this;
    }
    
    protected boolean runInEQ() {
        return true;
    }
    
    private Object writeReplace () {
        return new Replace ();
    }

    private static boolean checkAbstractDoc(Document doc) {
        if (doc == null)
            throw new IllegalArgumentException("document is null");
        return (doc instanceof AbstractDocument);
    }

    private static boolean isReadLocked(Document doc) {
        if (checkAbstractDoc(doc)) {
            if (isWriteLocked(doc))
                return true;
            Field numReadersField;
            try {
                numReadersField = AbstractDocument.class.getDeclaredField("numReaders");
            } catch (NoSuchFieldException ex) {
                throw new IllegalStateException(ex);
            }
            numReadersField.setAccessible(true);
            try {
                synchronized (doc) {
                    return numReadersField.getInt(doc) > 0;
                }
            } catch (IllegalAccessException ex) {
                throw new IllegalStateException(ex);
            }
        }
        return false;
    }

    private static boolean isWriteLocked(Document doc) {
        if (checkAbstractDoc(doc)) {
            Field currWriterField;
            try {
                currWriterField = AbstractDocument.class.getDeclaredField("currWriter");
            } catch (NoSuchFieldException ex) {
                throw new IllegalStateException(ex);
            }
            currWriterField.setAccessible(true);
            try {
                synchronized (doc) {
                    return currWriterField.get(doc) == Thread.currentThread();
                }
            } catch (IllegalAccessException ex) {
                throw new IllegalStateException(ex);
            }
        }
        return false; // not AbstractDocument
    }

    private boolean isDocumentLocked;
    /** 
     * Test that ChangeEvent is not fired under document lock during loading document.
     *
     * NbLikeEditorKit is used so document can be write locked using NbDocument.runAtomic in CES.prepareDocument.
     * 
     * @throws java.lang.Exception
     */
    public void testDeadlock () throws Exception {
        support.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent evt) {
                Document doc = ((EnhancedChangeEvent) evt).getDocument();
                if (doc != null) {
                    isDocumentLocked = Deadlock169717Test.isReadLocked(doc);
                }
            }
        });
        //Use prepare document to make sure stateChanged above is called BEFORE test ends
        //so isDocumentLocked is set.
        Task t = support.prepareDocument();
        t.waitFinished();
        assertFalse("Document cannot be locked", isDocumentLocked);
    }
    
    //
    // Implementation of the CloneableEditorSupport.Env
    //
    
    public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
        propL.add (l);
    }    
    public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
        propL.remove (l);
    }
    
    public synchronized void addVetoableChangeListener(VetoableChangeListener l) {
        assertNull ("This is the first veto listener", vetoL);
        vetoL = l;
    }
    public void removeVetoableChangeListener(VetoableChangeListener l) {
        assertEquals ("Removing the right veto one", vetoL, l);
        vetoL = null;
    }
    
    public CloneableOpenSupport findCloneableOpenSupport() {
        return RUNNING.support;
    }
    
    public String getMimeType() {
        return "text/plain";
    }
    
    public Date getTime() {
        return date;
    }
    
    public InputStream inputStream() throws IOException {
        return new ByteArrayInputStream (content.getBytes ());
    }
    public OutputStream outputStream() throws IOException {
        class ContentStream extends ByteArrayOutputStream {
            @Override
            public void close () throws IOException {
                super.close ();
                content = new String (toByteArray ());
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

    public void markModified() throws IOException {
        modified = true;
    }
    
    public void unmarkModified() {
        modified = false;
    }
    
    /** Implementation of the CES */
    private static final class CES extends CloneableEditorSupport {

        public CES (Env env, Lookup l) {
            super (env, l);
        }
        
        protected CloneableEditor createCloneableEditor() {
            return super.createCloneableEditor();
        }
        
        public CloneableTopComponent.Ref getRef () {
            return allEditors;
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

        protected EditorKit createEditorKit () {
            return new NbLikeEditorKit ();
        }
        
    }

    private static final class Replace implements Serializable {
        public Object readResolve () {
            return RUNNING;
        }
    }
}
