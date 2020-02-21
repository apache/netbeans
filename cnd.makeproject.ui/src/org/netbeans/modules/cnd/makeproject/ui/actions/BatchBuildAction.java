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
package org.netbeans.modules.cnd.makeproject.ui.actions;

import javax.swing.Action;
import javax.swing.JButton;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.MakeActionProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.spi.project.ui.support.MainProjectSensitiveActions;
import org.netbeans.spi.project.ui.support.ProjectActionPerformer;
import org.openide.util.NbBundle;

public final class BatchBuildAction {

    private static final String actionName = NbBundle.getMessage(BatchBuildAction.class, "BatchBuildActionName");

    private BatchBuildAction() {
    }

    public static Action mainBatchBuildAction() {
        return MainProjectSensitiveActions.mainProjectSensitiveAction(new BatchBuildActionPerformer(), actionName, null);
    }

    private static class BatchBuildActionPerformer implements ProjectActionPerformer {

        @Override
        public boolean enable(Project project) {
            boolean ret = false;
            if (project != null) {
                ret = project.getLookup().lookup(ConfigurationDescriptorProvider.class) != null;
            }
            return ret;
        }

        @Override
        public void perform(Project project) {
            Action action = MainProjectSensitiveActions.mainProjectCommandAction(MakeActionProvider.COMMAND_BATCH_BUILD, BatchBuildAction.actionName, null);
            JButton jButton = new JButton(action);
            jButton.doClick();
        }
    }

}
