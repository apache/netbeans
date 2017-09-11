/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.core.spi.multiview.text;

import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.swing.text.StyledDocument;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.api.actions.Savable;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.multiview.MultiViewProcessorTest.LP;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.actions.AbstractSavable;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.CloneableOpenSupport;
import org.openide.windows.CloneableTopComponent;


/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class MultiViewEditorDiscardTest extends NbTestCase {
    
    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(MultiViewEditorDiscardTest.class);
    }

    public MultiViewEditorDiscardTest(String n) {
        super(n);
    }

    @Override
    protected int timeOut() {
        return 10000;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockServices.setServices(DD.class);
    }
    
    

    public void testModifyCloseDiscard() throws Exception {
        InstanceContent ic = new InstanceContent();
        Lookup context = new AbstractLookup(ic);
        
        final CES ces = createSupport(context, ic);
        ic.add(ces);
        ic.add(10);
        
        EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                ces.open();
            }
        });
        final CloneableTopComponent tc = (CloneableTopComponent) ces.findPane();
        assertNotNull("Component found", tc);
        
        ces.openDocument();
        
        EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                assertEquals("One pane is open", 1, ces.getOpenedPanes().length);
            }
        });
        
        StyledDocument doc = ces.getDocument();
        
        assertEquals("Is empty", 0, doc.getLength());
        
        assertNotNull("Document is opened", doc);
        doc.insertString(0, "Ahoj", null);
        assertTrue("Is modified", ces.isModified());

        final Savable sava = tc.getLookup().lookup(Savable.class);
        assertNotNull("Savable present", sava);
        
        assertEquals("Also part of the global registry", sava, Savable.REGISTRY.lookup(Savable.class));
        
        final CountDownLatch docChanged = new CountDownLatch(1);
        ces.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (EditorCookie.Observable.PROP_DOCUMENT.equals(evt.getPropertyName())) {
                    docChanged.countDown();
                }
            }
        });
        EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                assertTrue("Can be closed without problems", tc.close());
            }
        });
        
        docChanged.await(5000, TimeUnit.MILLISECONDS);
        
        assertTrue("CES did not fire change of the document after close", docChanged.getCount() == 0);
        
        assertFalse("Not modified", ces.isModified());
        
        StyledDocument newDoc = ces.openDocument();
        assertEquals("The document is reverted to empty state", 0, newDoc.getLength());
    }
    
    private static CES createSupport(Lookup lkp, InstanceContent ic) {
        final Env env = new Env(ic);
        CES ces = new CES(env, lkp);
        env.setSupport(ces);
        return ces;
    }

    
    static class CES extends CloneableEditorSupport {
        private Lookup lkp;
        public CES(Env env, Lookup l) {
            super(env, l);
            this.lkp = l;
        }

        @Override
        protected Pane createPane() {
            return (Pane) MultiViews.createCloneableMultiView("text/plaintest", new LP(lkp));
        }
        
        public Pane findPane() {
            return (Pane)allEditors.getArbitraryComponent();
        }

        @Override
        protected String messageSave() {
            return "do save";
        }

        @Override
        protected String messageName() {
            return "MY NAME";
        }

        @Override
        protected String messageToolTip() {
            return "tool tip";
        }

        @Override
        protected String messageOpening() {
            return "about to open";
        }

        @Override
        protected String messageOpened() {
            return "done opening";
        }
    } // end of CES
    
    /** Helper Env implementation. */
    public static class Env extends Object
    implements CloneableEditorSupport.Env, Serializable {
        private static Env LAST_ONE;
        
        /** object to serialize and be connected to*/
        private transient String output;
        /** Reference to support instance. */
        private transient CloneableEditorSupport support;
        private transient boolean modified;
        private transient InstanceContent ic;

        /** Constructor. Attaches itself as listener to 
         * the data object so, all property changes of the data object
         * are also rethrown to own listeners.
         *
         * @param obj data object to be attached to
         */
        public Env(InstanceContent ic) {
            LAST_ONE = this;
            this.ic = ic;
        }

        public void setSupport(CloneableEditorSupport support) {
            this.support = support;
        }

        /** Method that allows environment to find its 
         * cloneable open support.
         * @return the support or null if the environemnt is not in valid 
         * state and the CloneableOpenSupport cannot be found for associated
         * data object
         */
        @Override
        public CloneableOpenSupport findCloneableOpenSupport() {
            return support;
        }

        /** Obtains the input stream.
         * @exception IOException if an I/O error occures
         */
        @Override
        public InputStream inputStream() throws IOException {
            byte[] arr = output == null ? null : output.getBytes();
            if (arr == null) {
                arr = new byte[0];
            }
            return new ByteArrayInputStream(arr);
        }

        /** Obtains the output stream.
         * @exception IOException if an I/O error occures
         */
        @Override
        public OutputStream outputStream() throws IOException {
            return new ByteArrayOutputStream() {

                @Override
                public void close() throws IOException {
                    super.close();
                    output = new String(toByteArray());
                }
            };
        }

        Date date = new Date();
        /** The time when the data has been modified */
        @Override
        public Date getTime() {
            return date;
        }

        /** Mime type of the document.
         * @return the mime type to use for the document
         */
        @Override
        public String getMimeType() {
            return "text/test";
        }

        /** Adds property listener.
         */
        @Override
        public void addPropertyChangeListener(PropertyChangeListener l) {
        }

        /** Removes property listener.
         */
        @Override
        public void removePropertyChangeListener(PropertyChangeListener l) {
        }

        /** Adds veto listener.
         */
        @Override
        public void addVetoableChangeListener(VetoableChangeListener l) {
        }

        /** Removes veto listener.
         */
        @Override
        public void removeVetoableChangeListener(VetoableChangeListener l) {
        }

        /** Test whether the support is in valid state or not.
         * It could be invalid after deserialization when the object it
         * referenced to does not exist anymore.
         *
         * @return true or false depending on its state
         */
        @Override
        public boolean isValid() {
            return true;
        }

        /** Test whether the object is modified or not.
         * @return true if the object is modified
         */
        @Override
        public boolean isModified() {
            return modified;
        }

        private Savable s = new AbstractSavable() {
            {
                register();
            }
            @Override
            protected void handleSave() throws IOException {
                support.saveDocument();
            }

            @Override
            protected String findDisplayName() {
                return "Name";
            }

            @Override
            public boolean equals(Object obj) {
                return obj == this;
            }

            @Override
            public int hashCode() {
                return System.identityHashCode(this);
            }
        };
        
        @Override
        public void markModified() throws java.io.IOException {
            modified = true;
            ic.add(s);
        }

        /** Reverse method that can be called to make the environment 
         * unmodified.
         */
        @Override
        public void unmarkModified() {
            modified = false;
            ic.remove(s);
        }
        
        private Object readResolve() {
            return LAST_ONE;
        }
    } // End of Env class.

    public static final class DD extends DialogDisplayer {

        @Override
        public Object notify(NotifyDescriptor descriptor) {
            return descriptor.getOptions()[1];
        }

        @Override
        public Dialog createDialog(DialogDescriptor descriptor) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }
}
