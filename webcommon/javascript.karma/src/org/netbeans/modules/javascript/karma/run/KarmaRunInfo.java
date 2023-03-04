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

package org.netbeans.modules.javascript.karma.run;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gsf.testrunner.api.RerunHandler;

public final class KarmaRunInfo {

    private final Project project;
    private final RerunHandler rerunHandler;
    private final String nbConfigFile;
    private final String projectConfigFile;
    private final String testFile;
    private final boolean failOnBrowserError;
    private final Map<String, String> envVars = new ConcurrentHashMap<>();


    private KarmaRunInfo(Builder builder) {
        assert builder != null;
        assert builder.project != null;
        assert builder.rerunHandler != null;
        assert builder.nbConfigFile != null;
        assert builder.projectConfigFile != null;
        project = builder.project;
        rerunHandler = builder.rerunHandler;
        nbConfigFile = builder.nbConfigFile;
        projectConfigFile = builder.projectConfigFile;
        testFile = builder.testFile;
        failOnBrowserError = builder.failOnBrowserError;
        envVars.putAll(builder.envVars);
    }

    public Project getProject() {
        return project;
    }

    public RerunHandler getRerunHandler() {
        return rerunHandler;
    }

    public String getNbConfigFile() {
        return nbConfigFile;
    }

    public String getProjectConfigFile() {
        return projectConfigFile;
    }

    @CheckForNull
    public String getTestFile() {
        return testFile;
    }

    public boolean isFailOnBrowserError() {
        return failOnBrowserError;
    }

    public Map<String, String> getEnvVars() {
        return new HashMap<>(envVars);
    }

    @Override
    public String toString() {
        return "KarmaRunInfo{" + "project=" + project + ", rerunHandler=" + rerunHandler + ", nbConfigFile=" + nbConfigFile // NOI18N
                + ", projectConfigFile=" + projectConfigFile + ", testFile=" + testFile + ", envVars=" + envVars + '}'; // NOI18N
    }


    //~ Inner classes

    public static final class Builder {

        final Project project;
        RerunHandler rerunHandler;
        String nbConfigFile;
        String projectConfigFile;
        String testFile;
        boolean failOnBrowserError;
        Map<String, String> envVars = new HashMap<>();


        public Builder(Project project) {
            this.project = project;
        }

        public Builder setRerunHandler(RerunHandler rerunHandler) {
            assert rerunHandler != null;
            this.rerunHandler = rerunHandler;
            return this;
        }

        public Builder setNbConfigFile(String nbConfigFile) {
            assert nbConfigFile != null;
            this.nbConfigFile = nbConfigFile;
            return this;
        }

        public Builder setProjectConfigFile(String projectConfigFile) {
            assert projectConfigFile != null;
            this.projectConfigFile = projectConfigFile;
            return this;
        }

        public Builder setTestFile(@NullAllowed String testFile) {
            this.testFile = testFile;
            return this;
        }

        public Builder setFailOnBrowserError(boolean failOnBrowserError) {
            this.failOnBrowserError = failOnBrowserError;
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

        public KarmaRunInfo build() {
            return new KarmaRunInfo(this);
        }

    }

}
