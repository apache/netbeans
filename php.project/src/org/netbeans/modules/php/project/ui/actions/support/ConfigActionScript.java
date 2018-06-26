/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.ui.actions.support;

import java.io.File;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.runconfigs.RunConfigScript;
import org.netbeans.modules.php.project.runconfigs.validation.RunConfigScriptValidator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 * Action implementation for SCRIPT configuration.
 * It means running and debugging scripts.
 * @author Tomas Mysik
 */
class ConfigActionScript extends ConfigAction {
    private final FileObject sourceRoot;

    protected ConfigActionScript(PhpProject project) {
        super(project);
        sourceRoot = ProjectPropertiesSupport.getSourcesDirectory(project);
        assert sourceRoot != null;
    }

    @Override
    public boolean isProjectValid() {
        return isValid(RunConfigScriptValidator.validateConfigAction(RunConfigScript.forProject(project), true) == null);
    }

    @Override
    public boolean isFileValid() {
        return isValid(RunConfigScriptValidator.validateConfigAction(RunConfigScript.forProject(project), false) == null);
    }

    private boolean isValid(boolean valid) {
        if (!valid) {
            showCustomizer();
        }
        return valid;
    }

    @Override
    public boolean isRunFileEnabled(Lookup context) {
        FileObject file = CommandUtils.fileForContextOrSelectedNodes(context, sourceRoot);
        return file != null && FileUtils.isPhpFile(file);
    }

    @Override
    public boolean isDebugFileEnabled(Lookup context) {
        if (DebugStarterFactory.getInstance() == null) {
            return false;
        }
        return isRunFileEnabled(context);
    }

    @Override
    public void runProject() {
        createFileRunner(null).run();
    }

    @Override
    public void debugProject() {
        createFileRunner(null).debug();
    }

    @Override
    public void runFile(Lookup context) {
        createFileRunner(context).run();
    }

    @Override
    public void debugFile(Lookup context) {
        createFileRunner(context).debug();
    }

    private File getStartFile(Lookup context) {
        FileObject file;
        if (context == null) {
            file = FileUtil.toFileObject(RunConfigScript.forProject(project).getIndexFile());
        } else {
            file = CommandUtils.fileForContextOrSelectedNodes(context, sourceRoot);
        }
        assert file != null : "Start file must be found";
        return FileUtil.toFile(file);
    }

    private FileRunner createFileRunner(Lookup context) {
        RunConfigScript configScript = RunConfigScript.forProject(project);
        return new FileRunner(getStartFile(context))
                .project(project)
                .command(configScript.getInterpreter())
                .workDir(configScript.getWorkDir())
                .phpArgs(configScript.getOptions())
                .fileArgs(configScript.getArguments());
    }

}
