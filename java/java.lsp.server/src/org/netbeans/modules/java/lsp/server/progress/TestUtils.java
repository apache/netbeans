/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.java.lsp.server.progress;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

public class TestUtils {
    private static final Logger LOG = Logger.getLogger(TestUtils.class.getName());

    public static List<FileObject> getModuleTestPaths(Project project) {        
        if (project == null) {
            return null;
        }
        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        Set<FileObject> paths = new LinkedHashSet<>();
        for (SourceGroup sourceGroup : sourceGroups) {
            URL[] urls = UnitTestForSourceQuery.findUnitTests(sourceGroup.getRootFolder());
            for (URL u : urls) {
                FileObject f = URLMapper.findFileObject(u);
                if (f != null) {
                    paths.add(f);
                }
            }
        }
        return paths.isEmpty() ? null : new ArrayList<>(paths);
    }
    
    public static FileObject findModulePath(String moduleName, List<FileObject> testRoots, FileObject testLocation) {
        if (testRoots == null || testRoots.isEmpty()) {
            return null;
        }
        
        if (testLocation != null) {
            for (FileObject root : testRoots) {
                if (FileUtil.isParentOf(root, testLocation)) {
                    return root;
                }
            }

            return null;
        }
        if (testRoots.size() > 1) {
            LOG.log(Level.WARNING, "Multiple test roots are not yet supported for module {0}", moduleName);
        }

        return testRoots.iterator().next();
    }

    //XXX: should be toUri??
    public static String toPath(FileObject file) {
        return file != null ? FileUtil.toFile(file).getAbsolutePath() : null;
    }
}
