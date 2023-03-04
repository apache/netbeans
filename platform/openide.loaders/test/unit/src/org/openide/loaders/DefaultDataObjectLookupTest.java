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

package org.openide.loaders;

import java.awt.Dialog;
import java.io.PrintStream;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.io.NbMarshalledObject;
import org.openide.windows.TopComponent;

/** Does the lookup behaves correctly?
 *
 * @author  Jaroslav Tulach
 */
public final class DefaultDataObjectLookupTest extends LoggingTestCaseHid {
    static {
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }

    private FileSystem lfs;
    private DataObject obj;

    public DefaultDataObjectLookupTest(String name) {
        super(name);
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    protected void setUp() throws Exception {
        super.setUp();

        registerIntoLookup(new DD());

        DD x = (DD)Lookup.getDefault().lookup(DD.class);
        assertNotNull("DD is there", x);

        String fsstruct [] = new String [] {
            "AA/a.test"
        };

        TestUtilHid.destroyLocalFileSystem(getName());
        lfs = TestUtilHid.createLocalFileSystem(getWorkDir(), fsstruct);
        Repository.getDefault().addFileSystem(lfs);

        FileObject fo = lfs.findResource("AA/a.test");
        assertNotNull("file not found", fo);
        obj = DataObject.find(fo);

        assertEquals("The right class", obj.getClass(), DefaultDataObject.class);

        assertTrue("Designed to run inside of AWT", SwingUtilities.isEventDispatchThread());
        DD.options = null;
        DD.disableTest = false;
    }

    private void waitForAWT() throws Exception {
        // just wait till all the stuff from AWT is finished
        Mutex.EVENT.readAccess(new Mutex.Action() {
            public Object run() {
                return null;
            }
        });
    }

    protected void tearDown() throws Exception {
        waitForAWT();
        DD.disableTest = true;

        super.tearDown();
        if (obj != null) {
            CloseCookie cc;
            cc = (CloseCookie)obj.getCookie(CloseCookie.class);
            if (cc != null) {
                DD.toReturn = NotifyDescriptor.NO_OPTION;
                cc.close();
            }
        }
        Repository.getDefault().removeFileSystem(lfs);

        waitForAWT();
    }

    
    public void testTryStackOverFlow101258() throws Exception {
        Node n = obj.getNodeDelegate();
        assertEquals(DataNode.class, n.getClass());
        DataNode dn = (DataNode)n;
        
        Lookup someLookup = dn.getLookup();
        assertNotNull(someLookup.lookup(EditCookie.class));
        
        MultiDataObject mdo = (MultiDataObject)obj;
        mdo.fireCookieChange();
        
        
    }
    
    /** Our own dialog displayer.
     */
    private static final class DD extends DialogDisplayer {
        public static Object[] options;
        public static Object toReturn;
        public static boolean disableTest;

        public Dialog createDialog(DialogDescriptor descriptor) {
            throw new IllegalStateException("Not implemented");
        }

        public Object notify(NotifyDescriptor descriptor) {
            if (disableTest) {
                return toReturn;
            } else {
                assertNull(options);
                assertNotNull(toReturn);
                options = descriptor.getOptions();
                Object r = toReturn;
                toReturn = null;
                return r;
            }
        }

    } // end of DD

}
