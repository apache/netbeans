/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.nette2.codegen;

import org.netbeans.modules.php.nette2.ui.codegen.ActionRenderCodeGeneratorTableModel;
import java.util.Collections;
import java.util.List;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplateManager;
import org.netbeans.modules.php.nette2.ui.codegen.ActionRenderVisualPanel;
import org.netbeans.modules.php.nette2.utils.FileUtils;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
@NbBundle.Messages("LBL_ActionRenderCodeGeneratorName=Action and/or Render Method...")
public class ActionRenderCodeGenerator implements CodeGenerator {
    private ActionRenderVisualPanel panel;
    private final JTextComponent textComp;
    private FileObject presenterFile;

    private ActionRenderCodeGenerator(Lookup context) {
        textComp = context.lookup(JTextComponent.class);
    }

    @MimeRegistration(mimeType = org.netbeans.modules.php.api.util.FileUtils.PHP_MIME_TYPE, service = CodeGenerator.Factory.class, position = 1982)
    public static class Factory implements CodeGenerator.Factory {

        @Override
        public List<? extends CodeGenerator> create(Lookup context) {
            return Collections.singletonList(new ActionRenderCodeGenerator(context));
        }
    }

    @Override
    public String getDisplayName() {
        return Bundle.LBL_ActionRenderCodeGeneratorName();
    }

    @Override
    public void invoke() {
        if (isProcessedDialog()) {
            assert panel != null;
            CodeTemplateManager manager = CodeTemplateManager.get(textComp.getDocument());
            CodeTemplate template = manager.createTemporary(createActionRenderMethods());
            template.insert(textComp);
        }
    }

    private boolean isProcessedDialog() {
        ActionRenderMethodChecker methodChecker = new ActionRenderMethodChecker(getPresenterFile());
        panel = new ActionRenderVisualPanel(new ActionRenderCodeGeneratorTableModel(methodChecker));
        panel.setMethodChecker(methodChecker);
        DialogDescriptor dd = new DialogDescriptor(
                panel,
                Bundle.LBL_ActionRenderCodeGeneratorName(),
                true,
                DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION,
                null);
        return DialogDisplayer.getDefault().notify(dd) == NotifyDescriptor.OK_OPTION;
    }

    private FileObject getPresenterFile() {
        if (presenterFile == null) {
            presenterFile = FileUtils.getFile(textComp);
        }
        return presenterFile;
    }

    private String createActionRenderMethods() {
        ActionRenderMethodChecker armc = new ActionRenderMethodChecker(getPresenterFile());
        StringBuilder sb = new StringBuilder();
        for (ActionRenderVisualPanel.Action action : panel.getActions()) {
            String actionName = action.getName();
            if (action.isGenerateAction() && !armc.existsActionMethod(actionName)) {
                sb.append(action.generateAction());
            }
            if (action.isGenerateRender() && !armc.existsRenderMethod(actionName)) {
                sb.append(action.generateRender());
            }
        }
        return sb.toString();
    }

}
