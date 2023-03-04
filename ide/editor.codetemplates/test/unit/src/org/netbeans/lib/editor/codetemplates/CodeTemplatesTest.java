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

package org.netbeans.lib.editor.codetemplates;

import java.awt.EventQueue;
import java.awt.event.KeyEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplateManager;
import org.netbeans.modules.editor.NbEditorKit;


/**
 * Testing correctness of the code templates processing.
 *
 * @author mmetelka
 */
public class CodeTemplatesTest extends NbTestCase {

    public CodeTemplatesTest(java.lang.String testName) {
        super(testName);
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    @RandomlyFails
    public void testMemoryRelease() throws Exception { // Issue #147984
        org.netbeans.junit.Log.enableInstances(Logger.getLogger("TIMER"), "CodeTemplateInsertHandler", Level.FINEST);

        JEditorPane pane = new JEditorPane();
        NbEditorKit kit = new NbEditorKit();
        pane.setEditorKit(kit);
        Document doc = pane.getDocument();
        assertTrue(doc instanceof BaseDocument);
        CodeTemplateManager mgr = CodeTemplateManager.get(doc);
        String templateText = "Test with parm ";
        CodeTemplate ct = mgr.createTemporary(templateText + " ${a}");
        ct.insert(pane);
        assertEquals(templateText + " a", doc.getText(0, doc.getLength()));

        // Send Enter to stop editing
        KeyEvent enterKeyEvent = new KeyEvent(pane, KeyEvent.KEY_PRESSED,
                EventQueue.getMostRecentEventTime(),
                0, KeyEvent.VK_ENTER, KeyEvent.CHAR_UNDEFINED);

        SwingUtilities.processKeyBindings(enterKeyEvent);
        // CT editing should be finished

        org.netbeans.junit.Log.assertInstances("CodeTemplateInsertHandler instances not GCed");
    }


}
