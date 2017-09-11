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

import java.awt.GraphicsEnvironment;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;
import java.util.logging.Level;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.DialogDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.LocalFileSystem;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.Task;
import org.openide.util.UserQuestionException;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/** Modified editor shall not be closed when its file is externally removed.
 *
 * @author Jaroslav Tulach
 */
public class ExternalDeleteOfModifiedContentTest extends NbTestCase 
implements CloneableEditorSupport.Env  {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(ExternalDeleteOfModifiedContentTest.class);
    }

    static {
       System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }
    /** the support to work with */
    private CloneableEditorSupport support;
    /** the content of lookup of support */
    private InstanceContent ic;

    
    // Env variables
    private String content = "";
    private boolean valid = true;
    private boolean modified = false;
    private Date date = new Date();
    private List<PropertyChangeListener> propL = new ArrayList<PropertyChangeListener>();
    private java.beans.VetoableChangeListener vetoL;
    private IOException toThrow;
    private CloneableEditorSupport edit;
    
    
    public ExternalDeleteOfModifiedContentTest (java.lang.String testName) {
        super(testName);
    }
    

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }

    @Override
    protected int timeOut() {
        return 20000;
    }
    @Override
    protected void setUp () throws Exception {
        MockServices.setServices(DD.class);
        
        
        clearWorkDir();
        LocalFileSystem fs = new LocalFileSystem();
        fs.setRootDirectory(getWorkDir());
        
        FileObject fo = fs.getRoot().createData("Ahoj", "txt");

        ic = new InstanceContent();
        support = new CES(this, new AbstractLookup(ic));
        edit = support;
        assertNotNull("we have editor", edit);

        DD.type = -1;
        DD.toReturn = new Stack<Object>();
    }

    public void testReloadOfABigFile() throws Exception {
        Document doc = edit.openDocument();
        
        doc.insertString(0, "Ahoj", null);
        assertTrue("Modified", edit.isModified());
        
        edit.open();
        waitEQ();

        JEditorPane[] arr = getPanes();
        assertNotNull("There is one opened pane", arr);
        
        java.awt.Component c = arr[0];
        while (!(c instanceof CloneableEditor)) {
            c = c.getParent();
        }
        CloneableEditor ce = (CloneableEditor)c;

        // select Yes
        DD.toReturn.push(DialogDescriptor.YES_OPTION);

        toThrow = new UserQuestionException() {
            @Override
            public void confirmed() throws IOException {
                // ok, confirmed
                toThrow = null;
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                for (int i = 0; i < 1024 * 1024 + 50000; i++) {
                    os.write('A');
                    if (i % 80 == 0) {
                        os.write('\n');
                    }
                }
                os.close();
                content = os.toString();
            }
            @Override
            public String getLocalizedMessage() {
                return "Ahoj";
            }
        };
        PropertyChangeEvent evt = new PropertyChangeEvent(this, PROP_TIME, null, null);
        for (PropertyChangeListener propertyChangeListener : propL) {
            propertyChangeListener.propertyChange(evt);
        }

        /*
        java.io.File f = FileUtil.toFile(obj.getPrimaryFile());
        assertNotNull("There is file behind the fo", f);

        obj.getPrimaryFile().getParent().refresh();
        */
        
        waitEQ();
        
        Task reloadTask = support.reloadDocument();
        reloadTask.waitFinished(); // Notification will be resolved in EDT so wait again for EDT

        waitEQ();
        Thread.sleep(3000);

        assertNotNull ("Text message was there", DD.message);
        assertEquals ("Text message was there", "Ahoj", DD.message);

        if (doc.getLength() < 1024) {
            fail("Should be pretty big: " + doc.getLength());
        }
    }

    private JEditorPane[] getPanes() {
        return Mutex.EVENT.readAccess(new Mutex.Action<JEditorPane[]>() {
            public JEditorPane[] run() {
                return edit.getOpenedPanes();
            }
        });
    }
    
    private void waitEQ() throws InterruptedException, java.lang.reflect.InvocationTargetException {
        javax.swing.SwingUtilities.invokeAndWait(new Runnable() { 
            public void run () { 
            } 
        });
    }

    /** Our own dialog displayer.
     */
    public static final class DD extends org.openide.DialogDisplayer {
        public static Object[] options;
        public static Stack<Object> toReturn;
        public static Object message;
        public static int type;
        
        public static void clear(Object t) {
            type = -1;
            message = null;
            options = null;
            toReturn.clear();
            toReturn.push(t);
        }
        
        public java.awt.Dialog createDialog(org.openide.DialogDescriptor descriptor) {
            throw new IllegalStateException ("Not implemented");
        }
        
        public Object notify(org.openide.NotifyDescriptor descriptor) {
            assertNull (options);
            if (type != -1) {
                fail("Second question: " + type);
            }
            if (toReturn.isEmpty()) {
                fail("Not specified what we shall return: " + toReturn);
            }
            Object r = toReturn.pop();
            if (toReturn.isEmpty()) {
                options = descriptor.getOptions();
                message = descriptor.getMessage();
                type = descriptor.getOptionType();
            }
            return r;
        }
        
    } // end of DD
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
        if (toThrow != null) {
            throw toThrow;
        }
        return new java.io.ByteArrayInputStream (content.getBytes ());
    }
    public java.io.OutputStream outputStream() throws java.io.IOException {
        class ContentStream extends java.io.ByteArrayOutputStream {
            @Override
            public void close () throws java.io.IOException {
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

    public void markModified() throws java.io.IOException {
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
        
    } // end of CES
    
}
