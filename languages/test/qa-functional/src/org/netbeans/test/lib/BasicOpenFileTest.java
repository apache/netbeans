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
package org.netbeans.test.lib;

import java.io.File;
import javax.swing.SwingUtilities;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.JellyTestCase;
import org.openide.util.actions.SystemAction;
import org.openide.actions.UndoAction;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
/**
 *
 * @author Jindrich Sedek
 */
public class BasicOpenFileTest extends JellyTestCase {

    private EditorOperator operator;

    public BasicOpenFileTest(String str) {
        super(str);
    }

    protected EditorOperator openFile(String projectName, String fileName) {
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(projectName);
        Node node = new Node(rootNode, "Source Packages|files|" + fileName);
        node.select();
        node.performPopupAction("Open");
        operator = new EditorOperator(fileName);
        assertNotNull(operator.getText());
        assertTrue(operator.getText().length() > 0);
        return operator;
    }

    protected EditorOperator openStandaloneTokenFile(String fileName) throws Exception {
        File tokensDir = new File(getDataDir(), "tokens");
        File file = new File(tokensDir, fileName);
        DataObject dataObj = DataObject.find(FileUtil.toFileObject(file));
        EditorCookie ed = dataObj.getCookie(EditorCookie.class);
        ed.open();
        operator = new EditorOperator(fileName);
        return operator;
    }

    protected void edit(String insertion) throws Exception {
        operator.insert(insertion, 1, 1);
        assertTrue(operator.getText().contains(insertion));
        operator.save();
        assertTrue(operator.getText().contains(insertion));
        undo();
        assertFalse(operator.getText().contains(insertion));
        operator.save();
    }

    protected void closeFile() {
        EditorOperator.closeDiscardAll();
    }

    private void undo() throws Exception {
        final UndoAction ua = SystemAction.get(UndoAction.class);
        assertNotNull("Cannot obtain UndoAction", ua);
        while (ua.isEnabled()) {
            SwingUtilities.invokeAndWait(new Runnable() {

                public void run() {
                    ua.performAction();
                }
            });
        }
    }
}
