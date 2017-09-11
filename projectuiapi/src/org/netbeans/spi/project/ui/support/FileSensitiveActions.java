/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
