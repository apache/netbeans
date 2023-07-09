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
package org.netbeans.modules.languages.antlr.refactoring;

import javax.swing.event.ChangeListener;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.Lookups;


public class WhereUsedRefactoringUIImpl implements RefactoringUI {
    private final SymbolInformation symbolInformation;

    public WhereUsedRefactoringUIImpl(SymbolInformation symbolInformation) {
        this.symbolInformation = symbolInformation;
    }

    @Override
    public String getName() {
        return symbolInformation.getName();
    }

    @Override
    @Messages({
        "# {0} - identifier",
        "DESC_Usages=Usages of {0}"
    })
    public String getDescription() {
        return Bundle.DESC_Usages(symbolInformation.getName());
    }

    @Override
    public boolean isQuery() {
        return true;
    }

    @Override
    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        return null;
    }

    @Override
    public Problem setParameters() {
        return null;
    }

    @Override
    public Problem checkParameters() {
        return null;
    }

    @Override
    public boolean hasParameters() {
        return false;
    }

    @Override
    public AbstractRefactoring getRefactoring() {
        return new WhereUsedQuery(Lookups.fixed(symbolInformation));
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

}
