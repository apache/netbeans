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
package org.netbeans.modules.php.project.runconfigs;

import java.io.File;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;

/**
 * Run configuration for SCRIPT.
 */
public final class RunConfigScript extends BaseRunConfig<RunConfigScript> {

    private boolean useDefaultInterpreter;
    private String interpreter;
    private String options;
    private String workDir;


    private RunConfigScript() {
    }

    public static PhpProjectProperties.RunAsType getRunAsType() {
        return PhpProjectProperties.RunAsType.SCRIPT;
    }

    public static String getDisplayName() {
        return getRunAsType().getLabel();
    }

    //~ Factories

    public static RunConfigScript create() {
        return new RunConfigScript();
    }

    public static RunConfigScript forProject(final PhpProject project) {
        return ProjectManager.mutex().readAccess(new Mutex.Action<RunConfigScript>() {
            @Override
            public RunConfigScript run() {
                return new RunConfigScript()
                        .setUseDefaultInterpreter(false) // always false, interpreter itself is correctly set below
                        .setInterpreter(ProjectPropertiesSupport.getPhpInterpreter(project))
                        .setOptions(ProjectPropertiesSupport.getPhpArguments(project))
                        .setIndexParentDir(FileUtil.toFile(ProjectPropertiesSupport.getSourcesDirectory(project)))
                        .setIndexRelativePath(ProjectPropertiesSupport.getIndexFile(project))
                        .setArguments(ProjectPropertiesSupport.getArguments(project))
                        .setWorkDir(ProjectPropertiesSupport.getWorkDir(project));
            }
        });
    }

    //~ Methods

    @Override
    public File getIndexFile() {
        // #237370 - index file can start with "../"
        return FileUtil.normalizeFile(super.getIndexFile());
    }

    public String getHint() {
        StringBuilder sb = new StringBuilder(100);
        sb.append(interpreter);
        if (StringUtils.hasText(options)) {
            sb.append(" "); // NOI18N
            sb.append(options);
        }
        if (StringUtils.hasText(indexRelativePath)) {
            sb.append(" "); // NOI18N
            sb.append(indexRelativePath);
        }
        if (StringUtils.hasText(arguments)) {
            sb.append(" "); // NOI18N
            sb.append(arguments);
        }
        return sb.toString();
    }

    //~ Getters & Setters

    public String getInterpreter() {
        return interpreter;
    }

    public RunConfigScript setInterpreter(String interpreter) {
        this.interpreter = interpreter;
        return this;
    }

    public String getOptions() {
        return options;
    }

    public RunConfigScript setOptions(String options) {
        this.options = options;
        return this;
    }

    public boolean getUseDefaultInterpreter() {
        return useDefaultInterpreter;
    }

    public RunConfigScript setUseDefaultInterpreter(boolean useDefaultInterpreter) {
        this.useDefaultInterpreter = useDefaultInterpreter;
        return this;
    }

    public String getWorkDir() {
        return workDir;
    }

    public RunConfigScript setWorkDir(String workDir) {
        this.workDir = workDir;
        return this;
    }

}
