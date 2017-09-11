/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.server.ui.node;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Arrays;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.server.ServerRegistry;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CallableSystemAction;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class RootNodeTest extends NbTestCase {

    public RootNodeTest(String s) {
        super(s);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
    }

    @Override
    protected void tearDown() throws Exception {
        FileObject fo = FileUtil.getConfigFile("Servers/Actions");
        FileObject m = fo.getFileObject(MyAction.class.getName().replace('.', '-') + ".instance");
        if (m != null) {
            m.delete();
        }
        FileObject c = fo.getFileObject("A2.instance");
        if (c != null) {
            c.delete();
        }
        super.tearDown();
    }

    public void testGetActions() throws Exception {
        RootNode rn = RootNode.getInstance();
        FileObject fo = FileUtil.getConfigFile("Servers/Actions");
        assertNotNull("Folder for actions precreated", fo);
        FileObject x = fo.createData(MyAction.class.getName().replace('.', '-') + ".instance");
        x.setAttribute("position", 37);
        Action[] arr = rn.getActions(true);
        assertEquals("Two actions and one separator found: " + Arrays.asList(arr), 3, arr.length);
        assertEquals("Last one is separator", null, arr[2]);
        MyAction a = MyAction.get(MyAction.class);

        if (a != arr[0] && a != arr[1]) {
            fail("My action shall be present in the node context actions: " + arr[0] + " 2nd: " + arr[1]);
        }
    }

    public void testInvokeActionsOnProperties() throws Throwable {
        class Work implements Runnable {
            int action;
            Throwable t;
            CntAction a;


            public void run() {
                switch (action) {
                    case 0: setup(); break;
                    case 1: check1(); break;
                    default: fail();
                }
            }

            private void setup() {
                try {
                    FileObject fo = FileUtil.getConfigFile("Servers/Actions");
                    assertNotNull("Folder for actions precreated", fo);
                    a = new CntAction();
                    FileObject afo = fo.createData("A2.instance");
                    afo.setAttribute("instanceCreate", a);
                    afo.setAttribute("property-myprop", "true");
                    afo.setAttribute("position", 98);
                    FileObject x = fo.createData(MyAction.class.getName().replace('.', '-') + ".instance");
                    x.setAttribute("position", 37);
                    MyAction a = MyAction.get(MyAction.class);
                } catch (IOException ex) {
                    this.t = ex;
                }
            }

            private void check1() {
                try {
                    RootNode.enableActionsOnExpand(ServerRegistry.getInstance());
                    assertEquals("No action called", 0, a.cnt);
                    assertEquals("No action called2", 0, MyAction.cnt);

                    System.setProperty("myprop", "ahoj");
                    RootNode.enableActionsOnExpand(ServerRegistry.getInstance());
                    assertEquals("CntAction called", 1, a.cnt);
                    assertEquals("No Myaction", 0, MyAction.cnt);
                } catch (Throwable ex) {
                    this.t = ex;
                }
            }
        }

        Work w = new Work();
        w.action = 0;
        FileUtil.runAtomicAction(w);
        w.action = 1;
        EventQueue.invokeAndWait(w);

        if (w.t != null) {
            throw w.t;
        }
    }

    public static final class CntAction extends AbstractAction {
        int cnt;
        
        public void actionPerformed(ActionEvent e) {
            assertEquals("noui", e.getActionCommand());
            cnt++;
        }
    }

    public static final class MyAction extends CallableSystemAction {
        static int cnt;

        @Override
        public void performAction() {
            cnt++;
        }

        @Override
        public String getName() {
            return "My";
        }

        @Override
        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

    }
}
