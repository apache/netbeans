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

import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.multiview.MultiViewProcessorTest.LP;
import org.netbeans.junit.NbTestCase;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Provider;
import org.openide.util.io.NbMarshalledObject;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.CloneableOpenSupport;
import org.openide.windows.CloneableTopComponent;


/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class MultiViewEditorElementTest extends NbTestCase {
    
    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(MultiViewEditorElementTest.class);
    }

    public MultiViewEditorElementTest(String n) {
        super(n);
    }

    public void testLookupMustContainCES() throws Exception {
        try {
            MultiViewEditorElement mvee = new MultiViewEditorElement(Lookup.EMPTY);
        } catch (IllegalArgumentException ex) {
            // OK
            return;
        }
        fail("Should throw an exception");
    }
    
    public void testSerializeAndDeserialize() throws Exception {
        InstanceContent ic = new InstanceContent();
        Lookup context = new AbstractLookup(ic);
        
        CloneableEditorSupport ces = createSupport(context);
        ic.add(ces);
        ic.add(10);
        
        MultiViewEditorElement mvee = new MultiViewEditorElement(context);
        
        assertEquals("ces", ces, mvee.getLookup().lookup(CloneableEditorSupport.class));
        assertEquals("ten", Integer.valueOf(10), mvee.getLookup().lookup(Integer.class));
        
        NbMarshalledObject mar = new NbMarshalledObject(mvee);
        MultiViewEditorElement deser = (MultiViewEditorElement)mar.get();
        
        assertEquals("ten", Integer.valueOf(10), deser.getLookup().lookup(Integer.class));
        assertEquals("ces", ces, deser.getLookup().lookup(CloneableEditorSupport.class));
    }
    
    public void testLookupProvidersAreConsistent() throws Exception {
        InstanceContent ic = new InstanceContent();
        Lookup context = new AbstractLookup(ic);

        CloneableEditorSupport ces = createSupport(context);
        ic.add(ces);
        ic.add(10);

        final CloneableTopComponent tc = MultiViews.createCloneableMultiView("text/plaintest", new LP(context));
        final CloneableEditorSupport.Pane p = (CloneableEditorSupport.Pane) tc;
        EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                tc.open();
                tc.requestActive();
                p.updateName();
            }
        });

        assertNull("No icon yet", tc.getIcon());
        MultiViewHandler handler = MultiViews.findMultiViewHandler(tc);
        final MultiViewPerspective[] one = handler.getPerspectives();
        assertEquals("Two elements only" + Arrays.asList(one), 2, handler.getPerspectives().length);
        assertEquals("First one is source", "source", one[0].preferredID());
        assertEquals("Second one is also source", "source", one[1].preferredID());
        handler.requestVisible(one[0]);
        
        List<Lookup.Provider> arr = new ArrayList<Provider>();
        findProviders(tc, arr);
        assertEquals("Two providers: " + arr, 2, arr.size());

        assertSame("Both return same lookup", arr.get(0).getLookup(), arr.get(1).getLookup());
    }    
    
    public static CloneableEditorSupport createSupport(Lookup lkp) {
        final Env env = new Env();
        CES ces = new CES(env, lkp);
        env.setSupport(ces);
        return ces;
    }

    private void findProviders(Component tc, List<Provider> arr) {
        System.err.println("test: " + tc);
        if (tc instanceof Provider) {
            arr.add((Provider)tc);
        }
        if (tc instanceof Container) {
            Container c = (Container) tc;
            for (Component child : c.getComponents()) {
                findProviders(child, arr);
            }
        }
    }

    
    static class CES extends CloneableEditorSupport {
        public CES(Env env, Lookup l) {
            super(env, l);
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
        private transient CloneableOpenSupport support;

        /** Constructor. Attaches itself as listener to 
         * the data object so, all property changes of the data object
         * are also rethrown to own listeners.
         *
         * @param obj data object to be attached to
         */
        public Env() {
            LAST_ONE = this;
        }

        public void setSupport(CloneableOpenSupport support) {
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
            return false;
        }

        /** Support for marking the environement modified.
         * @exception IOException if the environment cannot be marked modified
         *    (for example when the file is readonly), when such exception
         *    is the support should discard all previous changes
         */
        @Override
        public void markModified() throws java.io.IOException {
        }

        /** Reverse method that can be called to make the environment 
         * unmodified.
         */
        @Override
        public void unmarkModified() {
        }
        
        private Object readResolve() {
            return LAST_ONE;
        }
    } // End of Env class.
    
}
