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
package org.netbeans.modules.refactoring.java.ui;

import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.api.JavaMoveMembersProperties;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Ralph Ruijs
 */
@NbBundle.Messages("LBL_MoveMembers=Move Members")
public class MoveMembersUI implements RefactoringUI {
    private MoveMembersPanel panel;
    private final TreePathHandle[] selectedElements;
    private transient MoveRefactoring refactoring;
    private InstanceContent ic;

    public MoveMembersUI(TreePathHandle... selectedElement) {
        this.selectedElements = selectedElement;
        this.ic = new InstanceContent();
        this.refactoring = new MoveRefactoring(new AbstractLookup(ic));
        refactoring.getContext().add(new JavaMoveMembersProperties(selectedElement));
    }

    @Override
    public String getName() {
        return getString ("LBL_MoveMembers");
    }

    @Override
    public String getDescription() {
        return getName();
    }

    @Override
    public boolean isQuery() {
        return false;
    }

    @Override
    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        if (panel == null) {
            panel = new MoveMembersPanel(selectedElements, parent);
        }
        return panel;
    }

    private Problem setParameters(boolean checkOnly) {
        List<? extends TreePathHandle> handles = panel.getHandles();
        ic.set(handles, null);
        TreePathHandle target = panel.getTarget();
        refactoring.setTarget(target == null? Lookup.EMPTY:Lookups.fixed(target));
        JavaMoveMembersProperties properties = refactoring.getContext().lookup(JavaMoveMembersProperties.class);
        if(properties == null) {
            refactoring.getContext().add(properties = new JavaMoveMembersProperties(selectedElements));
        }
        properties.setVisibility(panel.getVisibility());
        properties.setDelegate(panel.getDelegate());
        properties.setUpdateJavaDoc(panel.getUpdateJavaDoc());
        properties.setAddDeprecated(panel.getDeprecated());
        return checkOnly? refactoring.fastCheckParameters() : refactoring.checkParameters();
    }

    @Override
    public Problem checkParameters() {
        return setParameters(true);
    }

    @Override
    public Problem setParameters() {
        return setParameters(false);
    }

    @Override
    public boolean hasParameters() {
        return true;
    }

    @Override
    public AbstractRefactoring getRefactoring() {
        return refactoring;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.refactoring.java.ui.MoveMembersUI"); // NOI18N
    }

    private String getString(String key) {
        return NbBundle.getMessage(MoveMembersUI.class, key);
    }
}
