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
package org.netbeans.modules.php.project.ui.actions;

import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ui.actions.support.CommandUtils;
import org.netbeans.modules.php.project.ui.actions.support.ConfigAction;
import org.netbeans.modules.php.project.ui.actions.support.Displayable;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * @author Radek Matous, Tomas Mysik
 */
public class RunFileCommand extends Command implements Displayable {
    public static final String ID = ActionProvider.COMMAND_RUN_SINGLE;
    public static final String DISPLAY_NAME = RunProjectCommand.DISPLAY_NAME;

    public RunFileCommand(PhpProject project) {
        super(project);
    }

    @Override
    public void invokeActionInternal(Lookup context) {
        FileObject fileObj = CommandUtils.fileForContextOrSelectedNodes(context);
        if (isSeleniumFile(fileObj)) {
            // selenium
            ConfigAction.get(ConfigAction.Type.SELENIUM, getProject()).runFile(context);
        } else if (isTestFile(fileObj)) {
            // test
            ConfigAction.get(ConfigAction.Type.TEST, getProject()).runFile(context);
        } else {
            // source
            ConfigAction configAction = getConfigAction();
            if (!configAction.isFileValid()) {
                // property not set yet
                return;
            }
            configAction.runFile(context);
        }
    }

    @Override
    public boolean isActionEnabledInternal(Lookup context) {
        FileObject fileObj = CommandUtils.fileForContextOrSelectedNodes(context);
        if (isSeleniumFile(fileObj)) {
            // selenium
            return ConfigAction.get(ConfigAction.Type.SELENIUM, getProject()).isRunFileEnabled(context);
        }
        if (isTestFile(fileObj)) {
            // test
            return ConfigAction.get(ConfigAction.Type.TEST, getProject()).isRunFileEnabled(context);
        }
        // source
        return getConfigAction().isRunFileEnabled(context);
    }

    @Override
    public String getCommandId() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }
}
