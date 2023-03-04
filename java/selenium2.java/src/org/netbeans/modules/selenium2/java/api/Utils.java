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
package org.netbeans.modules.selenium2.java.api;

import java.util.Collection;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.gsf.testrunner.api.TestCreatorProvider;
import org.netbeans.modules.java.testrunner.JavaUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Theofanis Oikonomou
 */
public class Utils {
    
    public static final String RUN_SELENIUM_TESTS_REGEXP = "**/*" + TestCreatorProvider.INTEGRATION_TEST_CLASS_SUFFIX + ".java";
    
    public static boolean isSupportEnabled(Class lookupClass, FileObject[] activatedFOs) {
        if (activatedFOs.length == 0) {
            return false;
        }

        final FileObject firstFile = activatedFOs[0];
        Project p = FileOwnerQuery.getOwner(firstFile);
        if (p == null) {
            return false;
        }
        if(p.getLookup().lookup(lookupClass) == null) {
            return false;
        }
        
        if(firstFile == p.getProjectDirectory()) { // "Run Selenium Tests" action should be active for the project node
            return true;
        }
        
        final SourceGroup sourceGroup = findSourceGroup(firstFile);
        if (sourceGroup == null) {
            return false;
        }
        final FileObject rootFolder = sourceGroup.getRootFolder();
        if (UnitTestForSourceQuery.findUnitTests(rootFolder).length == 0 && UnitTestForSourceQuery.findSources(rootFolder).length == 0) {
            return false;
        }

        /*
         * Now we know that source folder of the first file has a corresponding
         * test folder (possible non-existent).
         */
        if (activatedFOs.length == 1) {
            /* ... if there is just one file selected, it is all we need: */
            return true;
        }

        /*
         * ...for multiple files, we just check that all the selected files
         * have the same root folder:
         */
        for (int i = 1; i < activatedFOs.length; i++) {
            FileObject fileObj = activatedFOs[i];
            if (!FileUtil.isParentOf(rootFolder, fileObj)
                    || !sourceGroup.contains(fileObj)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Finds a Java source group the given file belongs to.
     * 
     * @param  file  {@literal FileObject} to find a {@literal SourceGroup} for
     * @return  the found {@literal SourceGroup}, or {@literal null} if the given
     *          file does not belong to any Java source group
     */
    private static SourceGroup findSourceGroup(FileObject file) {
        final Project project = FileOwnerQuery.getOwner(file);
        if (project == null) {
            return null;
        }

        Sources src = ProjectUtils.getSources(project);
        SourceGroup[] srcGrps = src.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        for (SourceGroup srcGrp : srcGrps) {
            FileObject rootFolder = srcGrp.getRootFolder();
            if (((file == rootFolder) || FileUtil.isParentOf(rootFolder, file)) 
                    && srcGrp.contains(file)) {
                return srcGrp;
            }
        }
        return null;
    }
    
    public static String[] getSourceAndTestClassNames(FileObject fileObj, boolean isTestNG, boolean isSelenium) {
        return JavaUtils.getSourceAndTestClassNames(fileObj, isTestNG, isSelenium);
    }

    public static Object[] getTestSourceRoots(Collection<SourceGroup> createdSourceRoots, FileObject refFileObject) {
        return JavaUtils.getTestSourceRoots(createdSourceRoots, refFileObject);
    }
    
}
