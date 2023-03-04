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

package org.netbeans.modules.server.ui.wizard;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.JRadioButton;
import org.junit.Test;
import org.netbeans.modules.server.ServerRegistry;
import static org.junit.Assert.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class AddServerInstanceWizardTest {

    public AddServerInstanceWizardTest() {
    }

    @Test
    public void testListAvailableProviders() throws Throwable {
        class Work implements Runnable {
            int action;
            Throwable t;
            CntAction a;
            CntAction b;


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
                    a.putValue("wizardMessage", "Ahoj");
                    FileObject afo = fo.createData("A2.instance");
                    afo.setAttribute("instanceCreate", a);
                    afo.setAttribute("position", 309);
                    b = new CntAction();
                    FileObject bfo = fo.createData("A3.instance");
                    bfo.setAttribute("instanceCreate", b);
                    bfo.setAttribute("position", 159);
                } catch (IOException ex) {
                    this.t = ex;
                }
            }

            private void check1() {
                try {
                    JRadioButton[] result = AddServerInstanceWizard.listAvailableProviders(ServerRegistry.SERVERS_PATH);
                    assertEquals("One action found", 1, result.length);
                    assertEquals("Message is taken from attribute", "Ahoj", result[0].getText());
                    assertSame("Not part of API, but behaviour: Action is stored in property",
                        a,
                        result[0].getClientProperty("action")
                    );
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

}
