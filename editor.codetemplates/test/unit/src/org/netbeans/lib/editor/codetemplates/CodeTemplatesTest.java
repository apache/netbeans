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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
