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

package org.netbeans.modules.editor.lib2.actions;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.text.TextAction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mmetelka
 */
public class MacroRecordingTest {

    static final String TEST_ACTION_NAME = "test-action";

    public MacroRecordingTest() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testGet() {
        MacroRecording macroRecording = MacroRecording.get();
        assertNotNull(macroRecording);
    }

    @Test
    public void testRecording() {
        MacroRecording macroRecording = MacroRecording.get();
        assertNotNull(macroRecording.startRecording());
        JEditorPane pane = new JEditorPane();
        macroRecording.recordAction(new MacroRecordingTestAction(), new ActionEvent(pane, 0, ""), pane);
        String text = macroRecording.stopRecording();
        assertEquals(TEST_ACTION_NAME, text);
    }

    @Test
    public void testStopRecording() {
        MacroRecording macroRecording = MacroRecording.get();
        assertNull(macroRecording.stopRecording());
    }

    private static final class MacroRecordingTestAction extends TextAction {

        MacroRecordingTestAction() {
            super(TEST_ACTION_NAME);
        }

        public void actionPerformed(ActionEvent arg0) {
            // do nothing
        }

    }

}
