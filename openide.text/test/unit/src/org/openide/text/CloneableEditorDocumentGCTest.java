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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.openide.text;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.VetoableChangeListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.Position.Bias;
import org.netbeans.junit.NbTestCase;
import org.openide.text.CloneableEditorSupport.Pane;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.windows.CloneableOpenSupport;
import org.openide.windows.CloneableTopComponent;

/**
 * Test that state when document is gc'ed but documentStatus is not yet synced ie. not set
 * to DOCUMENT_NO. It is synced in active reference queue so we need to block queue to get this state
 * so that CES.StrongRef.run cannot be called from queue.
 * 
 * @author Marek Slama
 */
public class CloneableEditorDocumentGCTest extends NbTestCase
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
    /** if not null contains message why this document cannot be modified */
    private transient String cannotBeModified;
    private transient Date date = new Date ();
    private transient PropertyChangeSupport prop = new PropertyChangeSupport(this);
    private transient VetoableChangeListener vetoL;

    private List<Exception> inits = new ArrayList<Exception>();
    
    private static CloneableEditorDocumentGCTest RUNNING;

    private static final Object LOCK = new Object();
    private boolean waiting;
    
    public CloneableEditorDocumentGCTest(String s) {
        super(s);
    }
    
    @Override
    protected void setUp () {
        support = new CES (this, Lookup.EMPTY);
        RUNNING = this;
    }
    
    @Override
    protected boolean runInEQ() {
        return false;
    }
    
    private Object writeReplace () {
        return new Replace ();
    }
    
    public void testDocumentGCed () throws Exception {
        class R extends WeakReference<Object> implements Runnable {

            public R (Object o) {
                super(o, org.openide.util.Utilities.activeReferenceQueue());
            }

            public void run() {
                waiting = true;
                synchronized(LOCK) {
                    try {
                        LOCK.wait();
                    } catch (InterruptedException ex) {
                    }
                }
                waiting = false;
            }
        }

        PositionRef one = support.createPositionRef(1, Bias.Forward);
        Document doc = support.openDocument();
        assertNotNull("Document is opened", doc);
        
        PositionRef two = support.createPositionRef(2, Bias.Forward);
        
        assertEquals(1, one.getOffset());
        assertEquals(2, two.getOffset());
        
        // For FilterDocument the test must check GC of real (delegated) document as well
        // Without this the test would not work for real docs because they would be held from
        // EditorSupportLineSet->LineListener->root field (made a weak-ref in order to make
        // this test to pass).
        Document nonFilterDoc = null;
        if (doc instanceof FilterDocument) {
            nonFilterDoc = doc.getDefaultRootElement().getDocument();
        }

        Object o = new Object();
        R r = new R(o);
        o = null;
        //It will block active reference queue thread
        System.gc();
        while (!waiting) {
            System.gc();
            Thread.sleep(100);
        }

        Reference<?> ref = new WeakReference<Object>(doc);
        doc = null;
        Reference<?> nonFilterRef = new WeakReference<Object>(nonFilterDoc);
        nonFilterDoc = null;

        assertGC("Document can disappear",ref);
        assertGC("Non filtered document can disappear", nonFilterRef);

        doc = support.getDocument();
        assertNull("No document is opened", doc);
        
        assertEquals(1, one.getOffset());
        assertEquals(2, two.getOffset());
        
        doc = support.openDocument();
        assertNotNull("Document is opened", doc);

        assertEquals(1, one.getOffset());
        assertEquals(2, two.getOffset());

        doc.insertString(0, "a", null);

        assertEquals(2, one.getOffset());//XXX
        assertEquals(3, two.getOffset());

        //Unblock active reference queue thread
        synchronized(LOCK) {
            LOCK.notifyAll();
        }
    }
    
    public void testReload() throws Exception {
        final Pane[] pane = new Pane[1];
        SwingUtilities.invokeAndWait(new Runnable() {

            @Override
            public void run() {
                support.open();
                pane[0] = support.getAnyEditor();
            }
        });
        PositionRef one = support.createPositionRef(1, Bias.Forward);
        Document doc = support.getDocument();
        assertNotNull("Document is opened", doc);
        
        PositionRef two = support.createPositionRef(2, Bias.Forward);
        
        assertEquals(1, one.getOffset());
        assertEquals(2, two.getOffset());
    
        setNewTime(new Date());

        Thread.sleep(2000);
        
        assertNotNull("Document is opened", doc);

        assertEquals(1, one.getOffset());
        assertEquals(2, two.getOffset());
        
        doc.insertString(0, "a", null);
        
        assertEquals(2, one.getOffset());
        assertEquals(3, two.getOffset());
    }
    
    //
    // Implementation of the CloneableEditorSupport.Env
    //
    
    public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
        prop.addPropertyChangeListener(l);
    }    
    public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
        prop.removePropertyChangeListener(l);
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
    
    void setNewTime(Date d) {
	date = d;
        prop.firePropertyChange (PROP_TIME, null, null);
    }
    
    public Date getTime() {
        return date;
    }
    
    public synchronized InputStream inputStream() throws IOException {
        return new ByteArrayInputStream("abcdef".getBytes());
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
    private final class CES extends CloneableEditorSupport {
        public CES (Env env, Lookup l) {
            super (env, l);
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
    }

    private static final class Replace implements Serializable {
        public Object readResolve () {
            return RUNNING;
        }
    }
}
