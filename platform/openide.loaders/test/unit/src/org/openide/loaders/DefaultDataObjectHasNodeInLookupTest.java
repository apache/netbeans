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

import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.nodes.Node;
import org.openide.util.Mutex;
import org.openide.windows.TopComponent;

/** DefaultDataObject is supposed to have node in its editor in both modes,
 * when executed without core.multiview or with it. This test is subclassed 
 * in core.multiview
 *
 * @author  Jaroslav Tulach
 */
public class DefaultDataObjectHasNodeInLookupTest extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(DefaultDataObjectHasNodeInLookupTest.class);
    }

    static {
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }

    private FileSystem lfs;
    private DataObject obj;

    public DefaultDataObjectHasNodeInLookupTest(String name) {
        super(name);
    }
    
    @Override
    protected int timeOut() {
        return 60000;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        String[] fsstruct = {
            "AA/a.test"
        };

        TestUtilHid.destroyLocalFileSystem(getName());
        lfs = TestUtilHid.createLocalFileSystem(getWorkDir(), fsstruct);

        FileObject fo = lfs.findResource("AA/a.test");
        assertNotNull("file not found", fo);
        obj = DataObject.find(fo);
        
        assertEquals("The right object", DefaultDataObject.class, obj.getClass());

        assertFalse("Designed to run outside of AWT", SwingUtilities.isEventDispatchThread());
    }

    private void waitForAWT() throws Exception {
        // just wait till all the stuff from AWT is finished
        Mutex.EVENT.readAccess(new Mutex.Action<Void>() {
            @Override
            public Void run() {
                return null;
            }
        });
    }

    @Override
    protected void tearDown() throws Exception {
        waitForAWT();

        super.tearDown();
        if (obj != null) {
            CloseCookie cc;
            cc = obj.getCookie(CloseCookie.class);
            if (cc != null) {
                cc.close();
            }
        }

        waitForAWT();
    }
    
    public void testIsThereANodeInTheLookup() throws Exception {
        // make sure it has the cookie
        OpenCookie open = obj.getCookie(OpenCookie.class);
        open.open();
        open = null;

        FileObject fo = obj.getPrimaryFile();
        FileLock lock = fo.lock();
        final Object[] o = new Object[1];
        EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                    final EditorCookie c = obj.getCookie(EditorCookie.class);
                    assertNotNull(c);
                    c.open();

                    JEditorPane[] arr = c.getOpenedPanes();

                    assertNotNull("Something opened", arr);
                    assertEquals("One opened", 1, arr.length);

                    o[0] = SwingUtilities.getAncestorOfClass(TopComponent.class, arr[0]);
                    assertNotNull("Top component found", o);
                } catch (Exception x) {
                    throw new RuntimeException(x);
                }
            }
        });
        TopComponent tc = (TopComponent)o[0];

        DataObject query = tc.getLookup().lookup(DataObject.class);
        assertEquals("Object is in the lookup", obj, query);

        Node node = tc.getLookup().lookup(Node.class);
        assertEquals("Node is in the lookup", obj.getNodeDelegate(), node);
    }

    
}
