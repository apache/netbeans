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
package org.netbeans.modules.refactoring.php.delete;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.SafeDeleteRefactoring;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUIBypass;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Radek Matous
 */
public class PhpDeleteRefactoringUI implements RefactoringUI, RefactoringUIBypass {
    private static final RequestProcessor RP = new RequestProcessor(PhpDeleteRefactoringUI.class);
    private static final Logger LOGGER = Logger.getLogger(PhpDeleteRefactoringUI.class.getName());
    private final SafeDeleteRefactoring refactoring;
    private final boolean regulardelete;
    private final FileObject file;
    private SafeDeletePanel panel;

    public PhpDeleteRefactoringUI(SafeDeleteSupport support, boolean regularDelete) {
        Collection<Object> lookupContent = new ArrayList<>();
        lookupContent.add(support);
        this.refactoring = new SafeDeleteRefactoring(new ProxyLookup(Lookups.fixed(support.getFile()), Lookups.fixed(lookupContent.toArray())));
        this.file = support.getFile();
        this.regulardelete = regularDelete;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(PhpDeleteRefactoringUI.class, "LBL_SafeDel"); //NOI18N
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(PhpDeleteRefactoringUI.class, "LBL_SafeDel_Descr"); //NOI18N
    }

    @Override
    public boolean isQuery() {
        return false;
    }

    @Override
    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        if (panel == null) {
            panel = new SafeDeletePanel(refactoring, regulardelete, parent);
        }

        return panel;
    }

    @Override
    public Problem setParameters() {
        return refactoring.checkParameters();
    }

    @Override
    public Problem checkParameters() {
        return refactoring.checkParameters();
    }

    @Override
    public boolean hasParameters() {
        return true;
    }

    @Override
    public AbstractRefactoring getRefactoring() {
        return this.refactoring;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.refactoring.php.delete.PhpDeleteRefactoringUI"); //NOI18N
    }

    @Override
    public boolean isRefactoringBypassRequired() {
        return panel.isRegularDelete();
    }

    @Override
    public void doRefactoringBypass() throws IOException {
        RP.post(new Runnable() {

            @Override
            public void run() {
                try {
                    // #172199
                    FileUtil.runAtomicAction(new FileSystem.AtomicAction() {
                        @Override
                        public void run() throws IOException {
                            file.delete();
                        }
                    });
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            }

        });
    }
}
