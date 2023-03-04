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
