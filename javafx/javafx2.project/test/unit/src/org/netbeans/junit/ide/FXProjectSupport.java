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
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.modules.javafx2.platform.Utils;
import org.netbeans.modules.javafx2.platform.api.JavaFXPlatformUtils;
import org.netbeans.modules.javafx2.project.JFXProjectGenerator;
import org.netbeans.modules.javafx2.project.JavaFXProjectWizardIterator.WizardType;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Petr Somol
 */
/**
 * A helper class to work with projects in test cases.
 */
public class FXProjectSupport {
    
    /** This class is just a helper class and it should not be instantiated. */
    private FXProjectSupport() {
        throw new UnsupportedOperationException("It is just a helper class.");
    }
    
    /** Creates mock JavaFX RT directory and mock jar files that are
     * later referenced from endorsed classpath when creating a FX project.
     * The purpose is to prevent Broken Reference dialog appearing during test run.
     * @param projectParentPath path to directory where to create mock FX RT subdirectory
     * with mock files.
     * @return path to mock FX RT directory
     */
    public static File getMockFXRuntime(String projectParentPath) throws IOException{
        File runTimeDir = new File(projectParentPath + "/MockFXRT");
        for(String path : getJavaFXClassPath()) {
            String subPath = path.substring(path.indexOf("/"), path.indexOf(".jar") + ".jar".length());
            File file = new File(runTimeDir.getAbsolutePath() + subPath);
            FileUtil.createData(file);
        }
        return runTimeDir;
    }
    
    private static String[] getJavaFXClassPath() {
        return new String[] {
                    "/lib/ext/jfxrt.jar:", // NOI18N
                    "/lib/jfxrt.jar:", // NOI18N
                    "/lib/deploy.jar:", // NOI18N
                    "/lib/javaws.jar:", // NOI18N
                    "/lib/plugin.jar" // NOI18N
        };
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
            System.setProperty(Utils.NO_PLATFORM_CHECK_PROPERTY, "true");
            JavaPlatform platform = JavaFXPlatformUtils.findJavaFXPlatform();
            if(platform == null) {
                throw new RuntimeException("No Java platform with JavaFX RT can be found.");
            }
            System.out.println("MockFXPlatform: " + platform.getProperties().get(JavaFXPlatformUtils.PLATFORM_ANT_NAME));
            File projectDir = new File(projectParentDir, name);
            JFXProjectGenerator.createProject(
                    projectDir, name, mainClass, 
                    null, null, null, 
                    platform.getProperties().get(JavaFXPlatformUtils.PLATFORM_ANT_NAME), 
                    null, 
                    WizardType.APPLICATION);
            return org.netbeans.modules.project.ui.test.ProjectSupport.openProject(projectDir);
        } catch (Exception e) {
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

}
