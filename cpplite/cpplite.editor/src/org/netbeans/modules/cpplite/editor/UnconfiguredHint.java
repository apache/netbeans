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
package org.netbeans.modules.cpplite.editor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cpplite.editor.file.MIMETypes;
import org.netbeans.modules.cpplite.editor.lsp.LanguageServerImpl;
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

/**
 *
 * @author lahvac
 */
@OnStart
public class UnconfiguredHint implements Runnable {

    private static final Set<String> C_MIME_TYPES = new HashSet<>(Arrays.asList(MIMETypes.C, MIMETypes.CPP, MIMETypes.H, MIMETypes.HPP));

    private JTextComponent selectedComponent;

    @Override
    public void run() {
        EditorRegistry.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                updateFocused();
            }
        });
        Utils.settings().addPreferenceChangeListener(new PreferenceChangeListener() {
            @Override
            public void preferenceChange(PreferenceChangeEvent evt) {
                updateFocused();
            }
        });
        updateFocused();
    }
    
    private synchronized void updateFocused() {
        selectedComponent = EditorRegistry.focusedComponent();
        if (selectedComponent == null) {
            return ;
        }
        Document doc = selectedComponent.getDocument();
        FileObject file = NbEditorUtilities.getFileObject(doc);
        if (file == null || !C_MIME_TYPES.contains(FileUtil.getMIMEType(file))) {
            return ;
        }
        List<ErrorDescription> errors = new ArrayList<>();
        String ccls = Utils.getCCLSPath();
        String clangd = Utils.getCLANGDPath();
        if ((ccls == null || !new File(ccls).canExecute() || !new File(ccls).isFile()) &&
            (clangd == null || !new File(clangd).canExecute() || !new File(clangd).isFile())) {
            errors.add(ErrorDescriptionFactory.createErrorDescription(Severity.WARNING, "Neither ccls nor clangd configured!", Collections.singletonList(new ConfigureCCLS()), doc, 0));
        } else {
            Project prj = FileOwnerQuery.getOwner(file);
            if (prj != null && LanguageServerImpl.getCompileCommandsDir(prj) == null) {
                errors.add(ErrorDescriptionFactory.createErrorDescription(Severity.WARNING, "compile commands not configured", doc, 0));
            }
        }
        HintsController.setErrors(doc, UnconfiguredHint.class.getName(), errors);
    }
    
    private static final class ConfigureCCLS implements Fix {

        @Override
        public String getText() {
            return "Configure ccls";
        }

        @Override
        public ChangeInfo implement() throws Exception {
            OptionsDisplayer.getDefault().open("CPlusPlus/cpplite");
            return null;
        }
    
    }

}
