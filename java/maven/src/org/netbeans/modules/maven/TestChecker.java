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

package org.netbeans.modules.maven;

import org.codehaus.plexus.util.StringUtils;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.maven.api.classpath.ProjectSourcesClassPathProvider;
import org.netbeans.modules.maven.api.execute.PrerequisitesChecker;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.execute.DefaultReplaceTokenProvider;
import org.netbeans.modules.maven.options.MavenSettings;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;

/**
 *
 * @author mkleint
 */
@ProjectServiceProvider(service=PrerequisitesChecker.class, projectType="org-netbeans-modules-maven")
public class TestChecker implements PrerequisitesChecker {

    /**
     * Skip test execution.
     * Do not use maven.test.skip as that skips also compilation; see #189466 for background.
     * http://maven.apache.org/plugins/maven-surefire-plugin/examples/skipping-test.html
     */
    public static final String PROP_SKIP_TEST = "skipTests"; // NOI18N

    @Override public boolean checkRunConfig(RunConfig config) {
        String action = config.getActionName();
        if (ActionProvider.COMMAND_TEST.equals(action) ||
            ActionProvider.COMMAND_TEST_SINGLE.equals(action) ||
            ActionProvider.COMMAND_DEBUG_TEST_SINGLE.equals(action) ||
            "profile-tests".equals(action)) 
        { //NOI18N - profile-tests is not really nice but well.
                String test = config.getProperties().get("test");
                String method = config.getProperties().get(DefaultReplaceTokenProvider.METHOD_NAME);
                if (test != null && method != null) {
                    config.setProperty(DefaultReplaceTokenProvider.METHOD_NAME, null);
                    config.setProperty("test", test + '#' + method);
                }
        }
        if (ActionProviderImpl.COMMAND_INTEGRATION_TEST_SINGLE.equals(action) ||
            ActionProviderImpl.COMMAND_DEBUG_INTEGRATION_TEST_SINGLE.equals(action) ||
            "profile-tests".equals(action)) //NOI18N - profile-tests is not really nice but well. 
        {
                String test = config.getProperties().get("it.test"); //NOI18N
                String method = config.getProperties().get(DefaultReplaceTokenProvider.METHOD_NAME);
                if (test != null && method != null) {
                    config.setProperty(DefaultReplaceTokenProvider.METHOD_NAME, null);
                    config.setProperty("it.test", test + '#' + method); //NOI18N
                }
        }
        if (MavenSettings.getDefault().isSkipTests()) {
            if (!String.valueOf(config.getGoals()).contains("test")) { // incl. integration-test
                if (config.getProperties().get(PROP_SKIP_TEST) == null) {
                    config.setProperty(PROP_SKIP_TEST, "true"); //NOI18N
                }
            }
        }
        if (ActionProvider.COMMAND_TEST_SINGLE.equals(action) ||
            ActionProviderImpl.COMMAND_INTEGRATION_TEST_SINGLE.equals(action) ||
            ActionProvider.COMMAND_DEBUG_TEST_SINGLE.equals(action) ||
            ActionProviderImpl.COMMAND_DEBUG_INTEGRATION_TEST_SINGLE.equals(action) ||
            ActionProvider.COMMAND_PROFILE_TEST_SINGLE.equals(action)) {
            String test;
            if (ActionProviderImpl.COMMAND_INTEGRATION_TEST_SINGLE.equals(action) ||
                ActionProviderImpl.COMMAND_DEBUG_INTEGRATION_TEST_SINGLE.equals(action)) {
                test = config.getProperties().get("it.test"); //NOI18N
            } else {
                test = config.getProperties().get("test"); //NOI18N
            }
            if (test != null) {
                //#213783  when running tests validate that the test file exists
                FileObject origFile = config.getSelectedFileObject();
                if (origFile != null) {
                    //first level - see if the selected file is from test source root or main root..
                    ClassPath nontestsrc = config.getProject().getLookup().lookup(ProjectSourcesClassPathProvider.class).getProjectSourcesClassPath(ClassPath.SOURCE);
                    if (nontestsrc.contains(origFile)) { //only when executed on non-test file..
                        String[] tests = StringUtils.split(test, ",");
                        boolean found = false;
                        ClassPath[] src = config.getProject().getLookup().lookup(ProjectSourcesClassPathProvider.class).getProjectClassPaths(ClassPath.SOURCE);
                        for (String tt : tests) {
                            if (tt.contains("#")) { //should not happen when invoked from projects ui, method present means we probably got it right
                                found = true; //don't skip execution here
                                break;
                            }
                            if (tt.contains("*")) {
                                found = true; //don't skip execution here
                                break;
                            }
                            String testPath = tt.replace(".", "/");

                            if (!testPath.endsWith(".java")) {
                                testPath = testPath + ".java"; //TODO what about groovy or scala test files?
                            }

                            for (ClassPath cp : src) {
                                if (cp.findResource(testPath) != null) {
                                    found = true;
                                    break;
                                }
                            }

                        }
                        if (!found) {
                            StatusDisplayer.getDefault().setStatusText("Could not find tests for selected files. Skipping execution.", StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT);
                            return false;
                        }
                    }
                }
            }
        }
        
        return true;
    }

}
