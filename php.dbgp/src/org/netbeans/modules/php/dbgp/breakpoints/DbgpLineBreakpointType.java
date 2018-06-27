/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
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
