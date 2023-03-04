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

import java.text.MessageFormat;
import javax.swing.Action;
import javax.swing.Icon;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.project.uiapi.Utilities;
import org.netbeans.spi.project.ActionProvider;

/**
 * Factory for creating file-sensitive actions.
 * @author Petr Hrebejk
 */
public class FileSensitiveActions {

    private FileSensitiveActions() {}

    /**
     * Creates an action sensitive to the set of currently selected files.
     * When performed the action will call the given command on the {@link ActionProvider} of
     * the selected project(s) and pass the proper context to it. Enablement of the
     * action depends on the behavior of the project's action provider.
     * <p>As mentioned in {@link ActionProvider} Javadoc, the action may also be enabled
     * without the participation of any project in case some globally registered {@link ActionProvider}
     * can provide an implementation.
     * (This since {@code org.netbeans.modules.projectuiapi/1 1.37}.)
     * <p>Shortcuts for actions are shared according to command, i.e. actions based on the same command
     * will have the same shortcut.
     * @param command the command which should be invoked when the action is
     *        performed
     * @param namePattern pattern which should be used for determining the action's
     *        name (label). It takes two parameters a la {@link MessageFormat}: <code>{0}</code> - number of selected files;
     *        <code>{1}</code> - name of the first file.
     * @param icon icon of the action (or null)
     * @return newly created file-sensitive action
     */    
    public static Action fileCommandAction( @NonNull String command, @NonNull String namePattern, @NullAllowed Icon icon ) {
        return Utilities.getActionsFactory().fileCommandAction( command, namePattern, icon );
    }
    
    /**
     * Creates an action sensitive to the set of currently selected files.
     * When performed the action will call {@link FileActionPerformer#perform}
     * on the action performer supplied
     * as a parameter. The action will only be enabled when exactly one 
     * file is selected and {@link FileActionPerformer#enable}
     * returns true.<BR>
     * Note that it is not guaranteed that {@link FileActionPerformer#enable}
     * will be called unless the file selection changes and someone is
     * listening to the action or explicitly asks for some of the action's values.
     * @param performer an action performer. 
     * @param namePattern pattern which should be used for determining the action's
     *        name (label). It takes two parameters a la {@link java.text.MessageFormat}: <code>{0}</code> - number of selected files;
     *        <code>{1}</code> - name of the first file.
     * @param icon icon of the action (or null)
     * @return newly created file-sensitive action
     * 
     * @since 1.56.0
     */    
    public static Action fileSensitiveAction(@NonNull FileActionPerformer performer, @NonNull String namePattern, @NullAllowed Icon icon ) {
        return Utilities.getActionsFactory().fileSensitiveAction( performer, namePattern, icon );
    }
}
