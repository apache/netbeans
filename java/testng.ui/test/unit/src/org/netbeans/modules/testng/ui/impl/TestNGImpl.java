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
package org.netbeans.modules.testng.ui.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.testng.api.TestNGSupport.Action;
import org.netbeans.modules.testng.spi.TestConfig;
import org.netbeans.modules.testng.spi.TestNGSupportImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lukas
 */
@ServiceProvider(service = TestNGSupportImplementation.class,
    supersedes = {"org.netbeans.modules.testng.ant.AntTestNGSupport",
                  "org.netbeans.modules.testng.maven.MavenTestNGSupport"})
public class TestNGImpl extends TestNGSupportImplementation {

    private static List<Action> sa = new ArrayList<Action>();
    private boolean configured = false;
    private static TestExecutorImpl te = new TestExecutorImpl();

    public static void setSupportedActions(Action... a) {
        sa.clear();
        sa = Arrays.asList(a);
    }

    public static TestExecutorImpl getTestExecutor() {
        return te;
    }

    public boolean isConfigured() {
        return configured;
    }

    @Override
    public boolean isActionSupported(Action action, Project p) {
        return sa.contains(action);
    }

    @Override
    public void configureProject(FileObject createdFile) {
        configured = true;
    }

    @Override
    public TestExecutor createExecutor(Project p) {
        te.p = p;
        return te;
    }

    @Override
    public boolean isSupportEnabled(FileObject[] activatedFOs) {
        return true;
    }

    public static class TestExecutorImpl implements TestExecutor {

        private boolean hasFailed = false;
        private Project p;
        private Action executedAction = null;
        private TestConfig testConfig = null;

        public void setHasFailed(boolean hasFailed) {
            this.hasFailed = hasFailed;
        }

        public Action getExecutedAction() {
            return executedAction;
        }

        public TestConfig getTestConfig() {
            return testConfig;
        }

        public boolean hasFailedTests() {
            return hasFailed;
        }

        public void execute(Action action, TestConfig config) throws IOException {
            executedAction = action;
            testConfig = config;
        }
    }
}
