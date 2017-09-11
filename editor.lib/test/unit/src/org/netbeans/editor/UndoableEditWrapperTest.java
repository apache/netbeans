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

package org.netbeans.editor;

import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.undo.UndoableEdit;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.junit.NbTestCase;
import org.openide.awt.UndoRedo;
import org.openide.cookies.EditorCookie;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.windows.CloneableOpenSupport;
import org.openide.windows.CloneableTopComponent;

/**
 *
 * @author Miloslav Metelka
 */
public class UndoableEditWrapperTest extends NbTestCase {
    
    static {
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }

    /** Creates a new instance of ZOrderTest */
    public UndoableEditWrapperTest(String name) {
        super(name);
    }
    
    public void testWrapping() throws Exception {
        MimePath mimePath = MimePath.EMPTY;
        MockMimeLookup.setInstances(mimePath, new TestingUndoableEditWrapper(), new TestingUndoableEditWrapper2());
        CESEnv env = new CESEnv();
        Document doc = env.support.openDocument();
//        doc.addUndoableEditListener(new UndoableEditListener() {
//            @Override
//            public void undoableEditHappened(UndoableEditEvent e) {
//                UndoableEdit edit = e.getEdit();
//            }
//        });
        doc.insertString(0, "Test", null);
        Class wrapEditClass = TestingUndoableEditWrapper.WrapCompoundEdit.class;
        assertNotNull(NbDocument.getEditToBeUndoneOfType(env.support, wrapEditClass));
        Class wrapEditClass2 = TestingUndoableEditWrapper2.WrapCompoundEdit2.class;
        assertNotNull(NbDocument.getEditToBeUndoneOfType(env.support, wrapEditClass2));
        
        // A trick to get whole edit
        UndoableEdit wholeEdit = NbDocument.getEditToBeUndoneOfType(env.support, UndoableEdit.class);
        assertTrue(wholeEdit instanceof List);
        @SuppressWarnings("unchecked")
        List<? extends UndoableEdit> listEdit = (List<? extends UndoableEdit>) wholeEdit;
        assertEquals(3, listEdit.size());
        assertEquals(wrapEditClass, listEdit.get(1).getClass());
        assertEquals(wrapEditClass2, listEdit.get(2).getClass());
    }

    private static final class CESEnv implements CloneableEditorSupport.Env {

        static final String mimeType = "text/plain";

        /** the support to work with */
        transient final CES support;
        
        private transient String content = ""; // initial document content
        private transient boolean modified = false;
        /** if not null contains message why this document cannot be modified */
        private transient String cannotBeModified;
        private transient Date date = new Date ();
        private transient List/*<PropertyChangeListener>*/ propL = new ArrayList ();
        private transient VetoableChangeListener vetoL;

        public CESEnv() {
            support = new CES (this, Lookup.EMPTY);
        }
        
        @Override
        public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
            propL.add (l);
        }    
        @Override
        public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
            propL.remove (l);
        }

        @Override
        public synchronized void addVetoableChangeListener(VetoableChangeListener l) {
            assertNull ("This is the first veto listener", vetoL);
            vetoL = l;
        }

        @Override
        public void removeVetoableChangeListener(VetoableChangeListener l) {
            assertEquals ("Removing the right veto one", vetoL, l);
            vetoL = null;
        }

        @Override
        public CloneableOpenSupport findCloneableOpenSupport() {
            return support;
        }

        @Override
        public String getMimeType() {
            return mimeType;
        }

        @Override
        public Date getTime() {
            return date;
        }

        @Override
        public InputStream inputStream() throws IOException {
            return new ByteArrayInputStream (content.getBytes ());
        }
        @Override
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

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public boolean isModified() {
            return modified;
        }

        @Override
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

        @Override
        public void unmarkModified() {
            modified = false;
        }
    
    }

    /** Implementation of the CES */
    private static final class CES extends CloneableEditorSupport implements EditorCookie {

        public CES (CloneableEditorSupport.Env env, Lookup l) {
            super (env, l);
        }
        
        @Override
        protected EditorKit createEditorKit () {
            // Important to use NbLikeEditorKit since otherwise FilterDocument
            // would be created with improper runAtomic()
            return new MyKit ();
        }
        
        public CloneableTopComponent.Ref getRef () {
            return allEditors;
        }
        
        @Override
        protected String messageName() {
            return "Name";
        }
        
        @Override
        protected String messageOpened() {
            return "Opened";
        }
        
        @Override
        protected String messageOpening() {
            return "Opening";
        }
        
        @Override
        protected String messageSave() {
            return "Save";
        }
        
        @Override
        protected String messageToolTip() {
            return "ToolTip";
        }
        
        public UndoRedo getUndoRedoPublic() {
            return getUndoRedo();
        }
        
    }

    private static final class MyKit extends BaseKit {

        @Override
        public String getContentType() {
            return CESEnv.mimeType;
        }

    }

}
