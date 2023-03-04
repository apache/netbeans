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
package org.netbeans.test.java.editor.actions;

import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.test.java.editor.lib.JavaEditorTestCase;

/**
 *
 * @author Jiri.Prox@oracle.com
 */
public class JavaEditorActionsTestCase extends EditorActionsTestCase {

    EditorOperator editor;

    /**
     * Creates a new instance of JavaCodeFoldingTest
     */
    public JavaEditorActionsTestCase(String testMethodName) {
        super(testMethodName);
    }

    @Override
    protected String getDefaultProjectName() {
        return JavaEditorTestCase.PROJECT_NAME;
    }

    public void setEditorState(EditorOperator editor, String goldenFile, int caretLine, int caretColumn) {
        JEditorPaneOperator txtOper = editor.txtEditorPane();
        StringBuilder fileData = new StringBuilder(1000);
        try {
            BufferedReader reader = new BufferedReader(new FileReader(getGoldenFile(goldenFile)));
            char[] buf = new char[1024];
            int numRead;
            while ((numRead = reader.read(buf)) != -1) {
                fileData.append(buf, 0, numRead);
            }
        } catch (IOException ex) {
            fail(ex);
        }
        txtOper.removeAll();
        txtOper.setText(fileData.toString());
        txtOper.pushKey(KeyEvent.VK_BACK_SPACE); // replace the last NL...
        editor.setCaretPosition(caretLine, caretColumn);
    }

}
