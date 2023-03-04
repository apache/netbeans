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
