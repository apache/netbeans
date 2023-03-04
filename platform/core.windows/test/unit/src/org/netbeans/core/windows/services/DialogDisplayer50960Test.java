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

package org.netbeans.core.windows.services;

import java.awt.Dialog;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

/**
 *
 * @author Jiri Rechtacek
 */
public class DialogDisplayer50960Test extends TestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(DialogDisplayer50960Test.class);
    }

    private boolean performed = false;

    public DialogDisplayer50960Test (String testName) {
        super (testName);
    }

    protected void setUp() throws Exception {
        performed = false;
    }


    // test issue #50960: avoid redundant actionPerformed() from DialogDescriptors
    public void testRedundantActionPerformed () {
        JButton b1 = new JButton ("Do");
        JButton b2 = new JButton ("Don't");
        ActionListener listener = new ActionListener () {
            public void actionPerformed (ActionEvent event) {
                assertFalse ("actionPerformed() only once.", performed);
                performed = true;
            }
        };
        DialogDescriptor dd = new DialogDescriptor (
                            "...",
                            "My Dialog",
                            true,
                            new JButton[] {b1, b2},
                            b2,
                            DialogDescriptor.DEFAULT_ALIGN,
                            null,
                            null
                        );
        dd.setButtonListener (listener);
        Dialog dlg = DialogDisplayer.getDefault ().createDialog (dd);
        b1.doClick ();
        assertTrue ("Button b1 invoked.", performed);
    }
    

}
