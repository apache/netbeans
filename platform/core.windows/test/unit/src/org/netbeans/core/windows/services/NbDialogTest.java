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
package org.netbeans.core.windows.services;

import java.awt.Dialog;
import java.awt.GraphicsEnvironment;
import java.awt.Window;
import javax.swing.JLabel;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.DialogDescriptor;

public class NbDialogTest extends NbTestCase {
    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(NbDialogTest.class);
    }

    public NbDialogTest(String testName) {
        super(testName);
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    public void testModalityIsDefaultWhenModal() {
        NbDialog d = new NbDialog(new DialogDescriptor(null, null, true, null), (Window) null);
        assertEquals(Dialog.DEFAULT_MODALITY_TYPE, d.getModalityType());
    }

    public void testModalityIsModelessWhenNotModal() {
        NbDialog d = new NbDialog(new DialogDescriptor(null, null, false, null), (Window) null);
        assertEquals(Dialog.ModalityType.MODELESS, d.getModalityType());
    }
}
