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
