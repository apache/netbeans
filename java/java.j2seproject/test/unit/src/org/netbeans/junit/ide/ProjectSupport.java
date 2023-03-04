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

package org.netbeans.junit.ide;

import java.io.File;
import java.io.IOException;
import org.netbeans.api.java.source.SourceUtils;
import org.openide.ErrorManager;
import org.netbeans.modules.java.j2seproject.J2SEProjectGenerator;


/**
 * A helper class to work with projects in test cases.
 */
public class ProjectSupport {
    
    /** This class is just a helper class and it should not be instantiated. */
    private ProjectSupport() {
        throw new UnsupportedOperationException("It is just a helper class.");
    }
    
    /** Creates an empty Java project in specified directory and opens it.
     * Its name is defined by name parameter.
     * @param projectParentPath path to directory where to create name subdirectory and
     * new project structure in that subdirectory.
     * @param name name of the project
     * @return Project instance of created project
     */
    public static Object createProject(String projectParentPath, String name) {
        return createProject(new File(projectParentPath), name);
    }
    
    /** Creates an empty Java project in specified directory and opens it.
     * Its name is defined by name parameter.
     * @param projectParentDir directory where to create name subdirectory and
     * new project structure in that subdirectory.
     * @param name name of the project
     * @return Project instance of created project
     */
    public static Object createProject(File projectParentDir, String name) {
        String mainClass = null;
        try {
            File projectDir = new File(projectParentDir, name);
            J2SEProjectGenerator.createProject(projectDir, name, mainClass, null, null, false);
            return openProject(projectDir);
        } catch (IOException e) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
            return null;
        }
    }
    
    /** Waits until metadata scanning is finished. */
    @SuppressWarnings("deprecation")
    public static void waitScanFinished() {
        try {
            SourceUtils.waitScanFinished();
        } catch (InterruptedException ex) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
        }
    }
    
    /** @deprecated Use {@link org.netbeans.modules.project.ui.test.ProjectSupport} instead. */
    @Deprecated
    public static Object openProject(File projectDir) {
        return org.netbeans.modules.project.ui.test.ProjectSupport.openProject(projectDir);
    }
    
    /** @deprecated Use {@link org.netbeans.modules.project.ui.test.ProjectSupport} instead. */
    @Deprecated
    public static boolean closeProject(String name) {
        return org.netbeans.modules.project.ui.test.ProjectSupport.closeProject(name);
    }
    
    /** @deprecated Use {@link org.netbeans.modules.project.ui.test.ProjectSupport} instead. */
    @Deprecated
    public static Object openProject(String projectPath) {
        return org.netbeans.modules.project.ui.test.ProjectSupport.openProject(projectPath);
    }
    
}
