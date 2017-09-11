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
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledDocument;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Task;
import org.openide.windows.CloneableOpenSupport;
import org.openide.windows.CloneableTopComponent;

/** Test to simulate problem #46885
 * @author  Jaroslav Tulach
 */
public class ReloadTest extends NbTestCase
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
    private transient PropertyChangeSupport propL = new PropertyChangeSupport (this);
    private transient VetoableChangeListener vetoL;

    private Logger err;
    
    public ReloadTest (String s) {
        super(s);
    }

    /** For subclasses to change to more nb like kits. */
    protected EditorKit createEditorKit () {
        return null;
    }
    
    protected @Override Level logLevel() {
        return Level.FINER;
    }
    
    protected @Override void setUp() {
        support = new CES (this, Lookup.EMPTY);
        Log.enable("", Level.ALL);
        err = Logger.getLogger(getName());
    }
    
    public void testRefreshProblem46885 () throws Exception {
        StyledDocument doc = support.openDocument ();
        
        doc.insertString (0, "A text", null);
        support.saveDocument ();
        
        content = "New";
        propL.firePropertyChange (CloneableEditorSupport.Env.PROP_TIME, null, null);
        
        waitAWT ();
        Task reloadTask = support.reloadDocument();
        reloadTask.waitFinished();
        
        String s = doc.getText (0, doc.getLength ());
        assertEquals ("Text has been updated", content, s);
        

        long oldtime = System.currentTimeMillis ();
        Thread.sleep(300);
        err.info("Document modified");
        doc.insertString (0, "A text", null);
        err.info("Document about to save");
        support.saveDocument ();
        err.info("Document saved");
        s = doc.getText (0, doc.getLength ());

        err.info("Current content: " + s);
        content = "NOT TO be loaded";
        propL.firePropertyChange (CloneableEditorSupport.Env.PROP_TIME, null, new Date (oldtime));
        
        waitAWT ();
        
        String s1 = doc.getText (0, doc.getLength ());
        err.info("New content: " + s1);
        assertEquals ("Text has not been updated", s, s1);
    }

    private void waitAWT () throws Exception {
        err.info("wait for AWT begin");
        assertFalse ("Not in AWT", SwingUtilities.isEventDispatchThread ());
        SwingUtilities.invokeAndWait (new Runnable () { public void run () { }});
        err.info("wait for AWT ends");
    }
    
    //
    // Implementation of the CloneableEditorSupport.Env
    //
    
    public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
        propL.addPropertyChangeListener (l);
    }    
    public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
        propL.removePropertyChangeListener (l);
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
        return null;
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
            public @Override void close() throws IOException {
                super.close ();
                content = new String (toByteArray ());
                date = new Date();
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
                public @Override String getLocalizedMessage() {
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

        protected @Override EditorKit createEditorKit() {
            EditorKit retValue = ReloadTest.this.createEditorKit ();
            if (retValue == null) {
                retValue = super.createEditorKit();
            }
            return retValue;
        }
    }
}
