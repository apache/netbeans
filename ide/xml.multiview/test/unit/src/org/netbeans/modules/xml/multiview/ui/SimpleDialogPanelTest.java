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

package org.netbeans.modules.xml.multiview.ui;

import javax.swing.JLabel;
import junit.framework.TestCase;
import org.openide.util.Utilities;

/**
 * This set of tests do test labels menominics.
 * There are 3 basic ways how mnemonics for labels may be used:
 * (1) labels with no mnemonics,
 * (2) labels with mnemonics specified as char[],
 * (3) labels with menomnics specified via '&' escape characters
 *
 * @author Petr Slechta
 */
public class SimpleDialogPanelTest extends TestCase {

    private static final String[] LABELS1 = {
        "Test label 1", "Test label 2", "Test label 3"
    };
    private static final char[] MNEMONICS1 = {
        'T', 'E', '3'
    };
    private static final String[] LABELS2 = {
        "&Test label 1", "T&est label 2", "Test label &3"
    };

    public SimpleDialogPanelTest(String testName) {
        super(testName);
    }

    public void testLabels1() {
        SimpleDialogPanel.DialogDescriptor dd = new SimpleDialogPanel.DialogDescriptor(LABELS1);
        SimpleDialogPanel sdp = new SimpleDialogPanel(dd);
        JLabel[] labels = sdp.getLabels();
        for (int i=0,maxi=LABELS1.length; i<maxi; i++) {
            assertEquals(0, labels[i].getDisplayedMnemonic());
        }
    }

    public void testLabels2() {
        SimpleDialogPanel.DialogDescriptor dd = new SimpleDialogPanel.DialogDescriptor(LABELS1);
        dd.setMnemonics(MNEMONICS1);
        SimpleDialogPanel sdp = new SimpleDialogPanel(dd);
        JLabel[] labels = sdp.getLabels();
        for (int i=0,maxi=LABELS1.length; i<maxi; i++) {
            int expectedMnemonic = Utilities.isMac() ? 0 : MNEMONICS1[i];
            assertEquals(expectedMnemonic, labels[i].getDisplayedMnemonic());
        }
    }

    public void testLabels3() {
        SimpleDialogPanel.DialogDescriptor dd = new SimpleDialogPanel.DialogDescriptor(LABELS2, true);
        SimpleDialogPanel sdp = new SimpleDialogPanel(dd);
        JLabel[] labels = sdp.getLabels();
        for (int i=0,maxi=LABELS1.length; i<maxi; i++) {
            int expectedMnemonic = Utilities.isMac() ? 0 : MNEMONICS1[i];
            assertEquals(expectedMnemonic, labels[i].getDisplayedMnemonic());
        }
    }

}
