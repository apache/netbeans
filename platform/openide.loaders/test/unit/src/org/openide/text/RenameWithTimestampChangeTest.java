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
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.util.Date;
import java.util.Stack;
import java.util.logging.Level;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;

import org.openide.cookies.EditorCookie;
import org.openide.filesystems.DefaultAttributes;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Mutex;

/** Subversion is said to change timestamp of files on rename. Let's test
 * the editor behavior in such case.
 *
 * @author Jaroslav Tulach
 */
public class RenameWithTimestampChangeTest extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(RenameWithTimestampChangeTest.class);
    }

    static {
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }
    private DataObject obj;
    private EditorCookie edit;
    private long lastM;
    
    
    public RenameWithTimestampChangeTest (java.lang.String testName) {
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
        MyFS fs = new MyFS();
        
        FileObject fo = fs.findResource("dir/x.txt");
        lastM = fo.lastModified().getTime();
        
        obj = DataObject.find(fo);
        edit = obj.getCookie(EditorCookie.class);
        assertNotNull("we have editor", edit);

        DD.type = -1;
        DD.toReturn = new Stack<Object>();
    }

    public void testRenameTheDocumentWhilechangingTheTimestamp() throws Exception {
        Document doc = edit.openDocument();
        
        assertFalse("Not Modified", edit.isModified());

        doc.insertString(0, "Base change\n", null);
        assertTrue("Is Modified", edit.isModified());
        
        edit.open();
        waitEQ();

        JEditorPane[] arr = getPanes();
        assertNotNull("There is one opened pane", arr);
        
        obj.getFolder().rename("newName");

        assertEquals("Last modified incremented by 10000", lastM + 10000, obj.getPrimaryFile().lastModified().getTime());
        assertTrue("Name contains newName: " + obj.getPrimaryFile(), obj.getPrimaryFile().getPath().contains("newName/"));
        
        waitEQ();
        edit.saveDocument();
        
        if (DD.error != null) {
            fail("Error in dialog:\n" + DD.error);
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
        
        @Override
        public java.awt.Dialog createDialog(org.openide.DialogDescriptor descriptor) {
            throw new IllegalStateException ("Not implemented");
        }
        
        @Override
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

    private static final class MyFS extends TestFileSystem {
        long lastM = 10000;
        
        @SuppressWarnings("LeakingThisInConstructor")
        public MyFS() {
            this.info = this;
            this.list = this;
            this.change = this;
            this.attr = new DefaultAttributes(info, change, list);
        }


        @Override
        public Date lastModified(String name) {
            return new Date(lastM);
        }


        @Override
        public void rename(String oldName, String newName) throws IOException {
            if (oldName.equals(dir)) {
                dir = newName;
                lastM += 10000;
            } else {
                throw new IOException();
            }
        }

    }
}
