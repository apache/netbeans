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

package org.netbeans.modules.editor.indent;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.editor.BaseKit;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.NbEditorKit;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.editor.indent.spi.ExtraLock;
import org.netbeans.modules.editor.indent.spi.IndentTask;

/**
 *
 * @author Miloslav Metelka
 */
public class IndentActionsTest extends NbTestCase {
    
    private static final String MIME_TYPE = "text/x-test-actions";

    public IndentActionsTest(String name) {
        super(name);
    }

    public void testIndentActions() {
        // Must run in AWT thread (BaseKit.install() checks for that)
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        testIndentActions();
                    }
                }
            );
            return;
        }

        assertTrue(SwingUtilities.isEventDispatchThread());
        TestIndentTask.TestFactory factory = new TestIndentTask.TestFactory();
        
        MockServices.setServices(MockMimeLookup.class);
        MockMimeLookup.setInstances(MimePath.parse(MIME_TYPE), factory);
        
        TestKit kit = new TestKit();
        JEditorPane pane = new JEditorPane();
        pane.setEditorKit(kit);
        assertEquals(MIME_TYPE, pane.getDocument().getProperty("mimeType"));
        //doc.putProperty("mimeType", MIME_TYPE);
        
        // Test insert new line action
        Action a = kit.getActionByName(BaseKit.insertBreakAction);
        assertNotNull(a);
        a.actionPerformed(new ActionEvent(pane, 0, ""));
        // Check that the factory was used
        assertTrue(TestIndentTask.TestFactory.lastCreatedTask.indentPerformed);

        // Test reformat action
         a = kit.getActionByName(BaseKit.formatAction);
        assertNotNull(a);
        a.actionPerformed(new ActionEvent(pane, 0, ""));

    }

    private static final class TestIndentTask implements IndentTask {
        
        private Context context;
        
        TestExtraLocking lastCreatedLocking;
        
        boolean indentPerformed;

        TestIndentTask(Context context) {
            this.context = context;
        }

        public void reindent() throws BadLocationException {
            assertTrue(lastCreatedLocking.locked);
            context.document().insertString(0, " ", null);
            indentPerformed = true;
        }
        
        public ExtraLock indentLock() {
            return (lastCreatedLocking = new TestExtraLocking());
        }
        
        static final class TestFactory implements IndentTask.Factory {
            
            static TestIndentTask lastCreatedTask;

            public IndentTask createTask(Context context) {
                return (lastCreatedTask = new TestIndentTask(context));
            }
            
        }

    }
    
    private static final class TestExtraLocking implements ExtraLock {
        
        Boolean locked;
        
        public Boolean locked() {
            return locked;
        }

        public void lock() {
            if (locked != null)
                assertFalse(locked);
            locked = true;
        }

        public void unlock() {
            assertTrue(locked);
            locked = false;
        }
        
    }
    
    private static final class TestKit extends NbEditorKit {
        
        public @Override String getContentType() {
            return MIME_TYPE;
        }
        
    }
}
