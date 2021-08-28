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
package org.netbeans.modules.gradle.nodes;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gradle.ActionProviderImpl;
import org.netbeans.modules.gradle.FavoriteTaskManager;
import org.netbeans.modules.gradle.api.GradleTask;
import org.netbeans.modules.gradle.api.execute.ActionMapping;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

import static org.netbeans.modules.gradle.nodes.Bundle.*;
import org.netbeans.modules.gradle.spi.actions.ProjectActionMappingProvider;
import org.openide.util.Lookup;

/**
 *
 * @author lkishalmi
 */
public final class TaskNode extends AbstractNode {

    @StaticResource
    private static final String TASK_ICON = "org/netbeans/modules/gradle/resources/gradle-task.gif";

    public static final String EXECUTE_TASK_ACTION = "execute.task"; //NOI18N

    final Project project;
    final GradleTask task;

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public TaskNode(Project project, GradleTask task) {
        super(Children.LEAF, Lookups.fixed(project, task));
        this.project = project;
        this.task = task;
        setIconBaseWithExtension(TASK_ICON);
        setName(task.getPath());
        setDisplayName(task.getName());
        setShortDescription(task.getDescription());
    }

    @NbBundle.Messages({
        "LBL_ExecTask=Run Task",
        "LBL_ExecCust=Execute Custom..."
    })
    @Override
    public Action[] getActions(boolean context) {
        ArrayList<Action> actions = new ArrayList<>(3);
        
        ActionMapping mapping = project.getLookup().lookup(ProjectActionMappingProvider.class).findMapping(EXECUTE_TASK_ACTION);
        if (mapping != null) {
            Lookup ctx = Lookups.fixed(project, task);
            actions.add(ActionProviderImpl.createCustomGradleAction(project, LBL_ExecTask(), mapping, ctx, false));
            actions.add(ActionProviderImpl.createCustomGradleAction(project, LBL_ExecCust(), mapping, ctx, true));
        }
        
        FavoriteTaskManager fvmgr = project.getLookup().lookup(FavoriteTaskManager.class);
        if (fvmgr != null) {
            actions.add(new FavoriteAction(task));
        }
        return actions.toArray(new Action[actions.size()]);
    }

    public GradleTask getTask() {
        return task;
    }

    @Override
    public Action getPreferredAction() {
        return getActions(false)[0];
    }

    @NbBundle.Messages({
        "LBL_AddToFavorites=Add to Favorites",
        "LBL_RemoveFromFavorites=Remove from Favorites"
    })
    private class FavoriteAction extends AbstractAction {

        private final GradleTask task;

        public FavoriteAction(GradleTask task) {
            this.task = task;
            putValue(Action.NAME, getName());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            FavoriteTaskManager fvmgr = project.getLookup().lookup(FavoriteTaskManager.class);
            boolean favorite = fvmgr.isFavorite(task);
            fvmgr.setFavorite(task, !favorite);
            putValue(Action.NAME, getName());
        }

        private String getName() {
            FavoriteTaskManager fvmgr = project.getLookup().lookup(FavoriteTaskManager.class);
            return fvmgr.isFavorite(task) ? LBL_RemoveFromFavorites() : LBL_AddToFavorites();
        }
    }
}
