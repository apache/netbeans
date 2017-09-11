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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
