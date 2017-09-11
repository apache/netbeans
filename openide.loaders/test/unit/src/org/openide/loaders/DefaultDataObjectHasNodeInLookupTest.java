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
