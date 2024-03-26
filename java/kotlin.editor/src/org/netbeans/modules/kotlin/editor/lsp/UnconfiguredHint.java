/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.kotlin.editor.lsp;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.PreferenceChangeEvent;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.autoupdate.PluginInstaller;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.HintsController;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.OnStart;
import org.openide.util.NbBundle.Messages;

@OnStart
public class UnconfiguredHint implements Runnable {

    private static final Set<String> KOTLIN_MIME_TYPES = new HashSet<>(Arrays.asList("text/x-kotlin"));

    @Override
    public void run() {
        EditorRegistry.addPropertyChangeListener((PropertyChangeEvent evt) -> updateFocused());
        Utils.settings().addPreferenceChangeListener((PreferenceChangeEvent evt) -> updateFocused());
        updateFocused();
    }

    @Messages("ERR_KotlinSupportMissing=Enhanced Kotlin support is missing, click to install.")
    private synchronized void updateFocused() {
        JTextComponent selectedComponent = EditorRegistry.focusedComponent();
        if (selectedComponent == null) {
            return ;
        }
        Document doc = selectedComponent.getDocument();
        FileObject file = NbEditorUtilities.getFileObject(doc);
        if (file == null || !KOTLIN_MIME_TYPES.contains(FileUtil.getMIMEType(file))) {
            return ;
        }
        List<ErrorDescription> errors = new ArrayList<>();
        File server = Utils.getServer();
        if (server == null) {
            errors.add(ErrorDescriptionFactory.createErrorDescription(Severity.WARNING, Bundle.ERR_KotlinSupportMissing(), Collections.singletonList(new InstallKotlinLSP()), doc, 0));
        }
        HintsController.setErrors(doc, UnconfiguredHint.class.getName(), errors);
    }
    
    private static final class InstallKotlinLSP implements Fix {

        @Override
        public String getText() {
            return "Install Kotlin LSP";
        }

        @Override
        @Messages("DN_KotlinLSP=Kotlin LSP")
        public ChangeInfo implement() throws Exception {
            PluginInstaller.getDefault().install("org.netbeans.libs.kotlin.lsp", null, null, Bundle.DN_KotlinLSP()); // NOI18N
            return null;
        }
    
    }

}
