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
import java.awt.GraphicsEnvironment;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;
import java.util.logging.Level;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.DialogDescriptor;

import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.loaders.DataObject;
import org.openide.util.Mutex;
import org.openide.util.UserQuestionException;

/** Modified editor shall not be closed when its file is externally changed.
 *
 * @author Jaroslav Tulach
 */
public class ExternalChangeOfModifiedFileTest extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(ExternalChangeOfModifiedFileTest.class);
    }

    static {
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }
    private DataObject obj;
    private EditorCookie edit;
    
    
    public ExternalChangeOfModifiedFileTest (java.lang.String testName) {
        super(testName);
    }
    
    @Override
    protected Level logLevel() {
        return Level.FINE;
    }

    @Override
    protected int timeOut() {
        return 0;//20000;
    }

    @Override
    protected void setUp () throws Exception {
        MockServices.setServices(DD.class);
        
        
        clearWorkDir();
        LocalFileSystem fs = new LocalFileSystem();
        fs.setRootDirectory(getWorkDir());
        
        FileObject fo = fs.getRoot().createData("Ahoj", "txt");
        
        obj = DataObject.find(fo);
        edit = obj.getCookie(EditorCookie.class);
        assertNotNull("we have editor", edit);

        DD.type = -1;
        DD.toReturn = new Stack<Object>();
    }

    public void testModifyTheFileAndThenPreventItToBeSavedOnFileDisappear() throws Exception {
        Document doc = edit.openDocument();
        
        assertFalse("Not Modified", edit.isModified());

        doc.insertString(0, "Base change\n", null);
        edit.saveDocument();
        
        edit.open();
        waitEQ();

        JEditorPane[] arr = getPanes();
        assertNotNull("There is one opened pane", arr);
        
        java.awt.Component c = arr[0];
        while (!(c instanceof CloneableEditor)) {
            c = c.getParent();
        }
        CloneableEditor ce = (CloneableEditor)c;

        // to change timestamps
        Thread.sleep(1000);

        java.io.File f = FileUtil.toFile(obj.getPrimaryFile());
        FileOutputStream os = new FileOutputStream(f);
        os.write("Ahoj\n".getBytes());
        os.close();

        // to change timestamps
        Thread.sleep(1000);

        doc.remove(0, doc.getLength());
        doc.insertString(0, "Internal change\n", null);

        String txt = doc.getText(0, doc.getLength());
        assertEquals("The right text is there", txt, "Internal change\n");
        
        arr = getPanes();
        assertNotNull("Panes are still open", arr);
        assertTrue("Document is remains modified", edit.isModified());

        DD.toReturn.push(DialogDescriptor.YES_OPTION);

        SaveCookie sc = obj.getLookup().lookup(SaveCookie.class);
        assertNotNull("File is modified and has save cookie", sc);
        try {
            edit.saveDocument();
            // Since fix of #186364 UQE is catched in DataEditorSupport
//            fail("External modification detected, expect UserQuestionException");
        } catch (UserQuestionException ex) {
            // Since fix of #186364 UQE is catched in DataEditorSupport
            fail("UserQuestionException should no longer be thrown");

            waitEQ();
            String txt2 = doc.getText(0, doc.getLength());
            assertEquals("The right text from the IDE remains", txt2, "Internal change\n");
            assertFileObject("Ahoj\n");
            // rerun the action
            ex.confirmed();
            String txt3 = doc.getText(0, doc.getLength());
            assertEquals("No reload, text saved", "Internal change\n", txt3);
        }
        assertFalse("Editor saved", edit.isModified());
        assertFileObject("Internal change\n");

        waitEQ();
        assertTrue("No dialog", DD.toReturn.isEmpty());
        if (DD.error != null) {
            fail("Error in dialog:\n" + DD.error);
        }
    }

    private void assertFileObject(String text) throws IOException {
        byte[] stream = new byte[4096];
        InputStream is = obj.getPrimaryFile().getInputStream();
        int len = is.read(stream);
        String s = new String(stream, 0, len);
        assertEquals(text, s);
        is.close();
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

    //
    // Our fake lookup
    //
    public static final class Lkp extends org.openide.util.lookup.AbstractLookup {
        static final long serialVersionUID = 3L;

        public Lkp () {
            this (new org.openide.util.lookup.InstanceContent ());
        }
        
        private Lkp (org.openide.util.lookup.InstanceContent ic) {
            super (ic);
            ic.add (new DD ());
        }
    }

    /** Our own dialog displayer.
     */
    public static final class DD extends org.openide.DialogDisplayer {
        public static Object[] options;
        public static Stack<Object> toReturn;
        public static Object message;
        public static int type;
        public static String error;
        
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
                error = "Second question: " + type;
                fail(error);
            }
            if (toReturn.isEmpty()) {
                error = "Not specified what we shall return: " + toReturn;
                fail(error);
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
    
}
