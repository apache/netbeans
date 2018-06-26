/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.project.ui.actions;

import java.util.Enumeration;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ui.actions.support.CommandUtils;
import org.netbeans.modules.php.project.ui.actions.support.ConfigAction;
import org.netbeans.modules.php.project.ui.actions.support.Displayable;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * Run all tests in the selected folder.
 */
public class RunTestsCommand extends Command implements Displayable {

    public static final String ID = "runTestsInFolder"; // NOI18N
    @NbBundle.Messages("RunTestsCommand.label=Run Tests")
    public static final String DISPLAY_NAME = Bundle.RunTestsCommand_label();


    public RunTestsCommand(PhpProject project) {
        super(project);
    }

    @Override
    public String getCommandId() {
        return ID;
    }

    @Override
    public void invokeActionInternal(Lookup context) {
        FileObject folder = findFolderWithTest(context);
        if (folder == null) {
            logger.warning("Folder should be found for running tests");
            return;
        }
        ConfigAction.get(ConfigAction.Type.TEST, getProject()).runFile(Lookups.fixed(folder));
    }

    @Override
    public boolean isActionEnabledInternal(Lookup context) {
        return findFolderWithTest(context) != null;
    }

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    private FileObject findFolderWithTest(Lookup context) {
        FileObject[] files = CommandUtils.filesForContextOrSelectedNodes(context);
        if (files.length != 1) {
            return null;
        }
        FileObject file = files[0];
        if (!file.isFolder()) {
            return null;
        }
        Enumeration<? extends FileObject> children = file.getChildren(true);
        while (children.hasMoreElements()) {
            FileObject child = children.nextElement();
            if (child.isData()
                    && isTestFile(child)
                    && FileUtils.isPhpFile(child)) {
                return file;
            }
        }
        return null;
    }

}
