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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
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
