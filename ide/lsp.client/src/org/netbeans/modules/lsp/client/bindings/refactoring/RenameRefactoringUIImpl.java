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
package org.netbeans.modules.lsp.client.bindings.refactoring;

import java.awt.Component;
import javax.swing.event.ChangeListener;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.RenameParams;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.netbeans.modules.lsp.client.LSPBindings;
import org.netbeans.modules.lsp.client.Utils;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author lahvac
 */
public class RenameRefactoringUIImpl implements RefactoringUI {

    private final LSPBindingsCollection servers;
    private final FileObject file;
    private final Position position;
    private final String name;
    private final RenameParams params;
    private RenamePanel panel;

    public RenameRefactoringUIImpl(LSPBindingsCollection servers, FileObject file, Position position, String name) {
        this.servers = servers;
        this.file = file;
        this.position = position;
        this.name = name;
        this.params = new RenameParams();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    @Messages({
        "# {0} - identifier",
        "DESC_Rename=Renaming {0}"
    })
    public String getDescription() {
        return Bundle.DESC_Rename(name);
    }

    @Override
    public boolean isQuery() {
        return false;
    }

    @Override
    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        if (panel == null) {
            panel = new RenamePanel();
        }
        panel.setName(name);
        return new CustomRefactoringPanel() {
            @Override
            public void initialize() {
            }

            @Override
            public Component getComponent() {
                return panel;
            }
        };
    }

    @Override
    public Problem setParameters() {
        params.setTextDocument(new TextDocumentIdentifier(Utils.toURI(file)));
        params.setPosition(position);
        params.setNewName(panel.getName());
        return null;
    }

    @Override
    public Problem checkParameters() {
        return null;
    }

    @Override
    public boolean hasParameters() {
        return true;
    }

    @Override
    public AbstractRefactoring getRefactoring() {
        return new RenameRefactoring(Lookups.fixed(servers, params));
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

}
