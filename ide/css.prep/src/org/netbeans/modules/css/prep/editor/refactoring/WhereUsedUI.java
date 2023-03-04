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
package org.netbeans.modules.css.prep.editor.refactoring;

import javax.swing.event.ChangeListener;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author mfukala@netbeans.org
 */
public class WhereUsedUI implements RefactoringUI {

    private WhereUsedPanel panel;
    private final WhereUsedQuery query;

    public WhereUsedUI(RefactoringElementContext context) {
	this.query = new WhereUsedQuery(Lookups.fixed(context));
    }

    @Override
    public String getName() {
	return NbBundle.getMessage(WhereUsedUI.class, "LBL_FindUsages"); //NOI18N
    }

    @Override
    public String getDescription() {
	return NbBundle.getMessage(WhereUsedUI.class, "LBL_FindUsages"); //NOI18N
    }

    @Override
    public boolean isQuery() {
	return true;
    }

    @Override
    public CustomRefactoringPanel getPanel(ChangeListener parent) {
       if(panel == null) {
           panel = new WhereUsedPanel();
       }
        return panel;
    }

    @Override
    public Problem setParameters() {
	return query.checkParameters();
    }

    @Override
    public Problem checkParameters() {
	return query.fastCheckParameters();
    }

    @Override
    public boolean hasParameters() {
	return true;
    }

    @Override
    public AbstractRefactoring getRefactoring() {
	return this.query;
    }

    @Override
    public HelpCtx getHelpCtx() {
	return null;
    }

}
