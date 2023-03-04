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
import org.netbeans.modules.project.uiapi.Utilities;

/**
 * Factory for creating project-sensitive actions.
 * @author Petr Hrebejk
 */
public class ProjectSensitiveActions {

    private ProjectSensitiveActions() {}

    /**
     * Creates an action sensitive to the set of currently selected projects.
     * When performed the action will call the given command on the {@link org.netbeans.spi.project.ActionProvider} of
     * the selected project(s). The action will only be enabled when exactly one
     * project is selected and the command is enabled in the project's action provider.<BR>
     * Shortcuts for actions are shared according to command, i.e. actions based on the same command
     * will have the same shortcut.
     * @param command the command which should be invoked when the action is
     *        performed (see e.g. constants in {@link org.netbeans.spi.project.ActionProvider})
     * @param namePattern a pattern which should be used for determining the action's
     *        name (label). It takes two parameters a la {@link java.text.MessageFormat}: <code>{0}</code> - number of selected projects;
     *        <code>{1}</code> - name of the first project.
     * @param icon icon of the action (or null)
     * @return an action sensitive to the current project
     */    
    public static /* TBD: declare as ContextAwareAction */Action projectCommandAction( String command, String namePattern, Icon icon ) {
        return Utilities.getActionsFactory().projectCommandAction( command, namePattern, icon );
    }
    
    /**
     * Creates an action sensitive to the set of currently selected projects.
     * When performed the action will call {@link ProjectActionPerformer#perform}
     * on the action performer supplied
     * as a parameter. The action will only be enabled when exactly one 
     * project is selected and {@link ProjectActionPerformer#enable}
     * returns true.<BR>
     * Note that it is not guaranteed that {@link ProjectActionPerformer#enable}
     * will be called unless the project selection changes and someone is
     * listening to the action or explicitly asks for some of the action's values.
     * @param performer an action performer. 
     * @param namePattern pattern which should be used for determining the action's
     *        name (label). It takes two parameters a la {@link java.text.MessageFormat}: <code>{0}</code> - number of selected projects;
     *        <code>{1}</code> - name of the first project.
     * @param icon icon of the action (XXX or null?)
     * @return an action sensitive to the current project
     */    
    public static Action projectSensitiveAction( ProjectActionPerformer performer, String namePattern, Icon icon ) {
        return Utilities.getActionsFactory().projectSensitiveAction( performer, namePattern, icon );
    }
    
}
