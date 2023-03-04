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
