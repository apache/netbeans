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
