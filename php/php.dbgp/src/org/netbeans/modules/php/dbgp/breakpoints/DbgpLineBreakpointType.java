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
package org.netbeans.modules.php.dbgp.breakpoints;

import javax.swing.JComponent;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.dbgp.ui.DbgpLineBreakpointCustomizerPanel;
import org.netbeans.spi.debugger.ui.BreakpointType;
import org.netbeans.spi.debugger.ui.Controller;
import org.openide.filesystems.FileObject;
import org.openide.text.Line;
import org.openide.util.NbBundle;

@NbBundle.Messages({
    "DbgpLineBreakpointType.CategoryDisplayName=PHP",
    "DbgpLineBreakpointType.TypeDisplayName=Line"
})
@BreakpointType.Registration(displayName = "#DbgpLineBreakpointType.TypeDisplayName")
public class DbgpLineBreakpointType extends BreakpointType {

    private Controller controller;

    @Override
    public String getCategoryDisplayName() {
        return Bundle.DbgpLineBreakpointType_CategoryDisplayName();
    }

    @Override
    public String getTypeDisplayName() {
        return Bundle.DbgpLineBreakpointType_TypeDisplayName();
    }

    @Override
    public JComponent getCustomizer() {
        JTextComponent lastFocusedComponent = EditorRegistry.lastFocusedComponent();
        Line line;
        if (lastFocusedComponent != null) {
            FileObject fileObject = NbEditorUtilities.getFileObject(lastFocusedComponent.getDocument());
            int caretPosition = lastFocusedComponent.getCaretPosition();
            try {
                int lineNumber = LineDocumentUtils.getLineIndex((BaseDocument) lastFocusedComponent.getDocument(), caretPosition);
                line = Utils.getLine(fileObject, lineNumber);
            } catch (BadLocationException ex) {
                line = null;
            }
        } else {
            line = null;
        }
        DbgpLineBreakpointCustomizerPanel customizer = new DbgpLineBreakpointCustomizerPanel(line);
        controller = customizer.getController();
        return customizer;
    }

    @Override
    public Controller getController() {
        return controller;
    }

    @Override
    public boolean isDefault() {
        JTextComponent lastFocusedComponent = EditorRegistry.lastFocusedComponent();
        if (lastFocusedComponent == null) {
            return false;
        }
        FileObject fileObject = NbEditorUtilities.getFileObject(lastFocusedComponent.getDocument());
        if (fileObject == null) {
            return false;
        }
        return FileUtils.isPhpFile(fileObject);
    }

}
