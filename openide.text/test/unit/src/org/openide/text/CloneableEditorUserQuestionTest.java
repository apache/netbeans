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

import java.awt.Dialog;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import javax.swing.SwingUtilities;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.UserQuestionException;
import org.openide.util.test.MockLookup;
import org.openide.windows.CloneableOpenSupport;

/** Testing usage of UserQuestionException in CES.
 *
 * @author Jaroslav Tulach
 */
@RandomlyFails
public class CloneableEditorUserQuestionTest extends NbTestCase
implements CloneableEditorSupport.Env {
    static {
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }
    /** the support to work with */
    private CloneableEditorSupport support;

    // Env variables
    private String content = "";
    private boolean valid = true;
    private boolean modified = false;
    /** if not null contains message why this document cannot be modified */
    private String cannotBeModified;
    private Date date = new Date();
    private VetoableChangeListener vetoL;
    private IOException toThrow;

    public CloneableEditorUserQuestionTest(String testName) {
        super(testName);
    }
    
    protected @Override void setUp() {
        MockLookup.setInstances(new DD());
        support = new CES(this, Lookup.EMPTY);
    }

    public void testExceptionThrownWhenDocumentIsBeingReadInAWT () throws Exception {
        class Run implements Runnable {
            public Exception ex;
            public Error err;
            public void run () {
                try {
                    doExceptionThrownWhenDocumentIsBeingRead ();
                } catch (Exception x) {
                    this.ex = x;
                } catch (Error x) {
                    this.err = x;
                }
            }
        }
        Run r = new Run ();
        SwingUtilities.invokeAndWait (r);
        if (r.ex != null) throw r.ex;
        if (r.err != null) throw r.err;
    }

    public void testExceptionThrownWhenDocumentIsBeingRead () throws Exception {
        assertFalse (SwingUtilities.isEventDispatchThread ());
        doExceptionThrownWhenDocumentIsBeingRead ();
    }
    
    public void testOpenDocumentIsLoadedUsingIOException() throws Exception{
        doOpenDocumentIsLoaded (new IOException ("Plain I/O exc"));
    }
    
    public void testOpenDocumentIsLoadedUsingUserQuestionException() throws Exception{
        class MyEx extends UserQuestionException {
            private int confirmed;
            
            public @Override String getLocalizedMessage() {
                return "locmsg";
            }
            
            public @Override String getMessage() {
                return "msg";
            }
            
            public void confirmed () {
                confirmed++;
                toThrow = null;
            }
        }
        doOpenDocumentIsLoaded (new MyEx ());
    }
    
    private void doOpenDocumentIsLoaded (IOException my) throws Exception {
        toThrow = my;
        try{
            support.openDocument();
            fail ("Document should not be loaded, we throw an exception");
        }
        catch (IOException e){
            assertSame ("The expected exception", my, e);
        }
        
        assertNull ("No document", support.getDocument());
        assertFalse ("Not loaded", support.isDocumentLoaded());

        toThrow = null;
        support.openDocument ();
        
        assertNotNull ("We can later open the document", support.getDocument ());
        assertTrue ("And it is correctly marked as loaded", support.isDocumentLoaded ());
    }
    
    private void doExceptionThrownWhenDocumentIsBeingRead () throws Exception {
        class MyEx extends UserQuestionException {
            private int confirmed;
            
            public @Override String getLocalizedMessage() {
                return "locmsg";
            }
            
            public @Override String getMessage() {
                return "msg";
            }
            
            public void confirmed () {
                confirmed++;
                toThrow = null;
            }
        }
        
        MyEx my = new MyEx ();
        toThrow = my;

        DD.toReturn = NotifyDescriptor.NO_OPTION;
        support.open ();
        
        if (!SwingUtilities.isEventDispatchThread ()) {
            javax.swing.SwingUtilities.invokeAndWait (new Runnable () { public void run () {} });
        }
        
        assertNotNull ("Some otions", DD.options);
        assertEquals ("Two options", 2, DD.options.length);
        assertEquals ("Yes", NotifyDescriptor.YES_OPTION, DD.options[0]);
        assertEquals ("No", NotifyDescriptor.NO_OPTION, DD.options[1]);
        assertEquals ("confirmed not called", 0, my.confirmed);
        
        assertNull ("Still no document", support.getDocument ());
        
        DD.options = null;
        DD.toReturn = NotifyDescriptor.YES_OPTION;
        support.open ();

        if (!SwingUtilities.isEventDispatchThread ()) {
            javax.swing.SwingUtilities.invokeAndWait (new Runnable () { public void run () {} });
        }
        
        assertEquals ("confirmed called", 1, my.confirmed);
        assertNotNull ("Some otions", DD.options);
        assertEquals ("Two options", 2, DD.options.length);
        assertEquals ("Yes", NotifyDescriptor.YES_OPTION, DD.options[0]);
        assertEquals ("No", NotifyDescriptor.NO_OPTION, DD.options[1]);
        DD.options = null;
        
        assertNotNull ("Document opened", support.getDocument ());
        
    }
        
    //
    // Implementation of the CloneableEditorSupport.Env
    //
    
    public void addPropertyChangeListener(PropertyChangeListener l) {}

    public void removePropertyChangeListener(PropertyChangeListener l) {}
    
    public synchronized void addVetoableChangeListener(VetoableChangeListener l) {
        assertNull ("This is the first veto listener", vetoL);
        vetoL = l;
    }
    public void removeVetoableChangeListener(VetoableChangeListener l) {
        assertEquals ("Removing the right veto one", vetoL, l);
        vetoL = null;
    }
    
    public CloneableOpenSupport findCloneableOpenSupport() {
        return support;
    }
    
    public String getMimeType() {
        return "text/plain";
    }
    
    public Date getTime() {
        return date;
    }
    
    public InputStream inputStream() throws IOException {
        if (toThrow != null) {
            throw toThrow;
        }
        return new ByteArrayInputStream(content.getBytes());
    }

    public OutputStream outputStream() throws IOException {
        class ContentStream extends ByteArrayOutputStream {
            public @Override void close() throws IOException {
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

    /** Our own dialog displayer.
     */
    private static final class DD extends DialogDisplayer {
        public static Object[] options;
        public static Object toReturn;
        
        public Dialog createDialog(DialogDescriptor descriptor) {
            throw new IllegalStateException ("Not implemented");
        }
        
        public Object notify(NotifyDescriptor descriptor) {
            assertNull (options);
            assertNotNull (toReturn);
            options = descriptor.getOptions();
            Object r = toReturn;
            toReturn = null;
            return r;
        }
        
    } // end of DD
    
}
