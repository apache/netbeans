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
package org.netbeans.modules.project.ui.problems;

import java.awt.event.ActionEvent;
import java.util.Collection;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.ProjectProblems;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Zezula
 */
@ActionID(id = "org.netbeans.modules.project.ui.problems.BrokenProjectActionFactory", category = "Project")
@ActionRegistration(displayName = "#LBL_Fix_Broken_Links_Action", lazy=false)
public class BrokenProjectActionFactory extends AbstractAction implements ContextAwareAction {

    public BrokenProjectActionFactory() {
        putValue(Action.NAME, NbBundle.getMessage(BrokenProjectActionFactory.class, "LBL_Fix_Broken_Links_Action"));
        setEnabled(false);
        putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        throw new IllegalStateException();
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        final Collection<? extends Project> p = actionContext.lookupAll(Project.class);
        if (p.size() != 1) {
            return this;
        }
        return new BrokenProjectAction(p.iterator().next());
    }


    /** This action is created only when project has broken references.
     * Once these are resolved the action is disabled.
     */
    private static class BrokenProjectAction extends AbstractAction {

        private final Project prj;

        BrokenProjectAction(@NonNull final Project prj) {
            this.prj = prj;        
            putValue(Action.NAME, NbBundle.getMessage(BrokenProjectActionFactory.class, "LBL_Fix_Broken_Links_Action"));
            setEnabled(ProjectProblems.isBroken(prj));
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
//                helper.requestUpdate();
            ProjectProblems.showCustomizer(prj);
        }

    }

}