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
package org.netbeans.modules.rust.grammar.lsp;

import java.beans.PropertyChangeEvent;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.rust.options.api.RustAnalyzerOptions;

import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.HintsController;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.OnStart;


@OnStart
public class UnconfiguredHint implements Runnable {

    private static final Set<String> RUST_MIME_TYPES = Collections.singleton("text/x-rust");

    @Override
    public void run() {
        EditorRegistry.addPropertyChangeListener((PropertyChangeEvent evt) -> {
            updateFocused();
        });
        updateFocused();
    }

    private void updateFocused() {
        JTextComponent selectedComponent = EditorRegistry.focusedComponent();
        if (selectedComponent == null) {
            return;
        }
        Document doc = selectedComponent.getDocument();
        FileObject file = NbEditorUtilities.getFileObject(doc);
        if (file == null || !RUST_MIME_TYPES.contains(FileUtil.getMIMEType(file))) {
            return;
        }
        List<ErrorDescription> errors = new ArrayList<>();
        Path rustLS = RustAnalyzerOptions.getRustAnalyzerLocation(true, false);
        if (rustLS == null) {
            errors.add(ErrorDescriptionFactory.createErrorDescription(Severity.WARNING, "For improved Rust support please install and configure rust-analyzer", Collections.singletonList(new ConfigureRustAnalyzer()), doc, 0));
        }
        HintsController.setErrors(doc, UnconfiguredHint.class.getName(), errors);
    }

    private static final class ConfigureRustAnalyzer implements Fix {

        @Override
        public String getText() {
            return "Configure rust-analyzer";
        }

        @Override
        public ChangeInfo implement() throws Exception {
            OptionsDisplayer.getDefault().open("Rust/RustAnalyzer");
            return null;
        }

    }

}
