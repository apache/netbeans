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
