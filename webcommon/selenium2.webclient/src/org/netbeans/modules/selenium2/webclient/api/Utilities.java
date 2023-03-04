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

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gsf.testrunner.ui.api.TestRunnerNodeFactory;
import org.netbeans.modules.selenium2.webclient.spi.JumpToCallStackCallback;
import org.netbeans.modules.selenium2.webclient.ui.SeleniumTestRunnerNodeFactory;
import org.netbeans.modules.web.clientproject.api.ProjectDirectoriesProvider;
import org.netbeans.spi.project.ui.CustomizerProvider2;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Theofanis Oikonomou
 */
public class Utilities {

    private Utilities() {
    }
    
    @CheckForNull
    public static FileObject getTestsFolder(Project project, boolean showFileChooser) {
        ProjectDirectoriesProvider directoriesProvider = project.getLookup().lookup(ProjectDirectoriesProvider.class);
        if (directoriesProvider == null) {
            return null;
        }
        return directoriesProvider.getTestDirectory(showFileChooser);
    }
    
    @CheckForNull
    public static FileObject getTestsSeleniumFolder(Project project, boolean showFileChooser) {
        ProjectDirectoriesProvider directoriesProvider = project.getLookup().lookup(ProjectDirectoriesProvider.class);
        if (directoriesProvider == null) {
            return null;
        }
        return directoriesProvider.getTestSeleniumDirectory(showFileChooser);
    }

    public static void openCustomizer(Project project, String category) {
        assert project != null;
        assert category != null;
        CustomizerProvider2 customizerProvider = project.getLookup().lookup(CustomizerProvider2.class);
        assert customizerProvider != null : "CustomizerProvider2 must be found in lookup of " + project.getClass().getName();
        customizerProvider.showCustomizer(category, null);
    }
    
    public static TestRunnerNodeFactory getTestRunnerNodeFactory(JumpToCallStackCallback callback) {
        return new SeleniumTestRunnerNodeFactory(callback);
    }
    
}
