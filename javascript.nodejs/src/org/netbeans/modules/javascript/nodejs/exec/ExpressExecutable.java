/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript.nodejs.exec;

import java.awt.EventQueue;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.nodejs.file.PackageJson;
import org.netbeans.modules.javascript.nodejs.options.NodeJsOptions;
import org.netbeans.modules.javascript.nodejs.options.NodeJsOptionsValidator;
import org.netbeans.modules.javascript.nodejs.ui.options.NodeJsOptionsPanelController;
import org.netbeans.modules.javascript.nodejs.util.FileUtils;
import org.netbeans.modules.javascript.nodejs.util.NodeJsUtils;
import org.netbeans.modules.javascript.nodejs.util.StringUtils;
import org.netbeans.modules.web.common.api.ValidationResult;
import org.netbeans.modules.web.common.ui.api.ExternalExecutable;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;


public class ExpressExecutable {

    public static final String EXPRESS_NAME;

    private static final String FORCE_PARAM = "--force"; // NOI18N
    private static final String CSS_PARAM = "--css"; // NOI18N
    private static final String LESS_PARAM = "less"; // NOI18N

    protected final Project project;
    protected final String expressPath;


    static {
        if (Utilities.isWindows()) {
            EXPRESS_NAME = "express.cmd"; // NOI18N
        } else {
            EXPRESS_NAME = "express"; // NOI18N
        }
    }

    ExpressExecutable(String expressPath, @NullAllowed Project project) {
        assert expressPath != null;
        this.expressPath = expressPath;
        this.project = project;
    }

    @CheckForNull
    public static ExpressExecutable getDefault(@NullAllowed Project project, boolean showOptions) {
        ValidationResult result = new NodeJsOptionsValidator()
                .validateExpress()
                .getResult();
        if (validateResult(result) != null) {
            if (showOptions) {
                OptionsDisplayer.getDefault().open(NodeJsOptionsPanelController.OPTIONS_PATH);
            }
            return null;
        }
        return createExecutable(NodeJsOptions.getInstance().getExpress(), project);
    }

    private static ExpressExecutable createExecutable(String express, Project project) {
        if (Utilities.isMac()) {
            return new ExpressExecutable.MacExpressExecutable(express, project);
        }
        return new ExpressExecutable(express, project);
    }

    String getCommand() {
        return expressPath;
    }

    @NbBundle.Messages({
        "# {0} - project name",
        "ExpressExecutable.generate=Express ({0})",
    })
    public Future<Integer> generate(FileObject target, boolean less) {
        assert !EventQueue.isDispatchThread();
        assert project != null;
        String projectName = NodeJsUtils.getProjectDisplayName(project);
        Future<Integer> task = getExecutable(Bundle.ExpressExecutable_generate(projectName))
                .additionalParameters(getGenerateParams(target, less))
                .run(getDescriptor());
        assert task != null : expressPath;
        return task;
    }

    private ExternalExecutable getExecutable(String title) {
        assert title != null;
        return new ExternalExecutable(getCommand())
                .workDir(getWorkDir())
                .displayName(title)
                .optionsPath(NodeJsOptionsPanelController.OPTIONS_PATH)
                .noOutput(false);
    }

    private ExecutionDescriptor getDescriptor() {
        assert project != null;
        return ExternalExecutable.DEFAULT_EXECUTION_DESCRIPTOR
                .showSuspended(true)
                .optionsPath(NodeJsOptionsPanelController.OPTIONS_PATH)
                .outLineBased(true)
                .errLineBased(true)
                .postExecution(new Runnable() {
                    @Override
                    public void run() {
                        project.getProjectDirectory().refresh();
                    }
                });
    }

    private File getWorkDir() {
        if (project == null) {
            return FileUtils.TMP_DIR;
        }
        PackageJson packageJson = new PackageJson(project.getProjectDirectory());
        if (packageJson.exists()) {
            return new File(packageJson.getPath()).getParentFile();
        }
        File sourceRoot = NodeJsUtils.getSourceRoot(project);
        if (sourceRoot != null) {
            return sourceRoot;
        }
        File workDir = FileUtil.toFile(project.getProjectDirectory());
        assert workDir != null : project.getProjectDirectory();
        return workDir;
    }

    private List<String> getGenerateParams(FileObject target, boolean less) {
        List<String> params = new ArrayList<>(3);
        if (less) {
            params.add(CSS_PARAM);
            params.add(LESS_PARAM);
        }
        params.add(FORCE_PARAM);
        params.add(FileUtil.toFile(target).getAbsolutePath());
        return getParams(params);
    }

    List<String> getParams(List<String> params) {
        assert params != null;
        return params;
    }

    @CheckForNull
    private static String validateResult(ValidationResult result) {
        if (result.isFaultless()) {
            return null;
        }
        if (result.hasErrors()) {
            return result.getFirstErrorMessage();
        }
        return result.getFirstWarningMessage();
    }

    //~ Inner classes

    private static final class MacExpressExecutable extends ExpressExecutable {

        private static final String BASH_COMMAND = "/bin/bash -lc"; // NOI18N


        MacExpressExecutable(String expressPath, Project project) {
            super(expressPath, project);
        }

        @Override
        String getCommand() {
            return BASH_COMMAND;
        }

        @Override
        List<String> getParams(List<String> params) {
            StringBuilder sb = new StringBuilder(200);
            sb.append("\""); // NOI18N
            sb.append(expressPath);
            sb.append("\" \""); // NOI18N
            sb.append(StringUtils.implode(super.getParams(params), "\" \"")); // NOI18N
            sb.append("\""); // NOI18N
            return Collections.singletonList(sb.toString());
        }

    }

}
