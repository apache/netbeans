/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.selenium2.webclient.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gsf.testrunner.api.RerunHandler;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Theofanis Oikonomou
 */
public class RunInfo {

    private final Project project;
    private final RerunHandler rerunHandler;
    private final String testFile;
    private final Map<String, String> envVars = new ConcurrentHashMap<>();
    private boolean testingProject;
    private FileObject[] activatedFOs;
    private boolean isSelenium;
    private boolean showOutput;

    private volatile boolean absoluteUrls = false;


    private RunInfo(Builder builder) {
        assert builder != null;
        assert builder.project != null;
        assert builder.rerunHandler != null;
        activatedFOs = builder.activatedFOs;
        project = builder.project;
        rerunHandler = builder.rerunHandler;
        testFile = builder.testFile;
        envVars.putAll(builder.envVars);
        testingProject = builder.testingProject;
        isSelenium = builder.isSelenium;
        showOutput = builder.showOutput;
    }

    public FileObject[] getActivatedFOs() {
        return activatedFOs;
    }

    public Project getProject() {
        return project;
    }

    public RerunHandler getRerunHandler() {
        return rerunHandler;
    }

    @CheckForNull
    public String getTestFile() {
        return testFile;
    }

    public Map<String, String> getEnvVars() {
        return new HashMap<>(envVars);
    }

    public boolean isAbsoluteUrls() {
        return absoluteUrls;
    }

    public void setAbsoluteUrls(boolean absoluteUrls) {
        this.absoluteUrls = absoluteUrls;
    }

    public boolean isTestingProject() {
        return testingProject;
    }

    public boolean isSelenium() {
        return isSelenium;
    }

    public boolean isShowOutput() {
        return showOutput;
    }

    @Override
    public String toString() {
//        return "RunInfo{" + "project=" + project + ", rerunHandler=" + rerunHandler + ", nbConfigFile=" + nbConfigFile // NOI18N
//                + ", projectConfigFile=" + projectConfigFile + ", testFile=" + testFile + ", envVars=" + envVars + ", absoluteUrls=" + absoluteUrls + '}'; // NOI18N
        return "RunInfo{" + "project=" + project + ", testFile=" + testFile + ", absoluteUrls=" + absoluteUrls + '}'; // NOI18N
    }


    //~ Inner classes

    public static final class Builder {

        final Project project;
        RerunHandler rerunHandler;
        String nbConfigFile;
        String projectConfigFile;
        String testFile;
        Map<String, String> envVars = new HashMap<>();
        boolean testingProject;
        FileObject[] activatedFOs;
        boolean isSelenium;
        boolean showOutput = true;

        public Builder(FileObject[] activatedFOs) {
            assert activatedFOs != null;
            assert activatedFOs.length > 0;
            this.activatedFOs = activatedFOs;
            project = FileOwnerQuery.getOwner(activatedFOs[0]);
        }

        public Builder setShowOutput(boolean showOutput) {
            this.showOutput = showOutput;
            return this;
        }

        public Builder setIsSelenium(boolean isSelenium) {
            this.isSelenium = isSelenium;
            return this;
        }

        public Builder setTestingProject(boolean testingProject) {
            this.testingProject = testingProject;
            return this;
        }

        public Builder setRerunHandler(RerunHandler rerunHandler) {
            assert rerunHandler != null;
            this.rerunHandler = rerunHandler;
            return this;
        }

        public Builder setTestFile(@NullAllowed String testFile) {
            this.testFile = testFile;
            return this;
        }

        public Builder addEnvVar(String name, String value) {
            assert name != null;
            assert value != null;
            envVars.put(name, value);
            return this;
        }

        public Builder addEnvVars(Map<String, String> envVars) {
            assert envVars != null;
            this.envVars.putAll(envVars);
            return this;
        }

        public RunInfo build() {
            return new RunInfo(this);
        }

    }

}
