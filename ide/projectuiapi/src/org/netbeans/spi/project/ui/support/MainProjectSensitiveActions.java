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

package org.netbeans.spi.project.ui.support;

import javax.swing.Action;
import javax.swing.Icon;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.project.uiapi.Utilities;
import org.openide.loaders.DataObject;

/**
 * Factory for creating actions sensitive to the project selected
 * as the main project in the UI.
 * <p>The precise definition of "selection" will depend on the UI implementation,
 * but will give preference to a main project compared to {@link ProjectSensitiveActions}.
 * For example:
 * <ol>
 * <li>The {@linkplain OpenProjects#getMainProject main project}, if one is set.
 * <li>The project mentioned in the {@linkplain org.openide.util.Utilities#actionsGlobalContext global selection}
 * (as {@link Project} or {@linkplain FileOwnerQuery#getOwner(FileObject) owner} of a {@link DataObject})
 * if there is exactly one such.
 * (Currently adjusted to allow for loss of window focus.)
 * <li>The {@linkplain OpenProjects#getOpenProjects open project}, if there is just one.
 * </ol>
 * @author Petr Hrebejk
 */
public class MainProjectSensitiveActions {

    private MainProjectSensitiveActions() {}

    /**
     * Creates an action sensitive to the project currently selected as main in the UI.
     * The action will invoke the given command on the main project. The action
     * may be disabled when no project is marked as main, or it may prompt the user
     * to select a main project, etc.
     * @param command the command which should be invoked when the action is
     *        performed
     * @param namePattern a pattern which should be used for determining the action's
     *        name (label). It takes two parameters a la {@link java.text.MessageFormat}:
     *        <code>{0}</code> - number of selected projects (or -1 if a main project is set);
     *        <code>{1}</code> - name of the first project (if >0).
     * @param icon icon of the action; may be null, in which case the action will
     *        not have an icon
     * @return an action sensitive to the main project
     */    
    public static Action mainProjectCommandAction(String command, String namePattern, Icon icon) {
        return Utilities.getActionsFactory().mainProjectCommandAction(command, namePattern, icon);
    }
        
    /**
     * Creates an action sensitive to the project currently selected as main in the UI.
     * When the action is invoked the supplied {@link ProjectActionPerformer#perform} 
     * will be called. The {@link ProjectActionPerformer#enable} method will
     * be consulted when the main project changes to determine whether the 
     * action should or should not be enabled. If no main project is selected the 
     * project parameter in the callback may be null.
     * @param performer callback class for enabling and performing the action    
     * @param namePattern a pattern which should be used for determining the action's
     *        name (label). It takes two parameters a la {@link java.text.MessageFormat}:
     *        <code>{0}</code> - number of selected projects (or -1 if a main project is set);
     *        <code>{1}</code> - name of the first project (if >0).
     * @param icon icon of the action; may be null, in which case the action will
     *        not have an icon
     * @return an action sensitive to the main project
     */
    public static Action mainProjectSensitiveAction(ProjectActionPerformer performer, String namePattern, Icon icon) {
        return Utilities.getActionsFactory().mainProjectSensitiveAction(performer, namePattern, icon);
    }
    
}
