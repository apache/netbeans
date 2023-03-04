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

package org.netbeans.modules.projectimport.eclipse.core;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbCollections;

/**
 * Serveral helpers for parsing, managing, loading Eclipse projects and
 * workspace metadata.
 *
 * @author mkrauskopf
 */
public class EclipseUtils {

    /**
     * Returns whether there is a valid project in the given
     * <code>projectDir</code>.
     */
    public static boolean isRegularProject(String projectDir) {
        return projectDir != null &&
                isRegularProject(new File(projectDir.trim()));
    }
    
    /**
     * Returns whether there is a valid project in the given
     * <code>projectDir</code>.
     */
    public static boolean isRegularProject(File projectDir) {
        return projectDir != null
                && FileUtil.toFileObject(FileUtil.normalizeFile(projectDir)) != null
                && projectDir.isDirectory()
                && new File(projectDir, EclipseProject.PROJECT_FILE).isFile();
    }
    
    /**
     * Returns whether there is a valid workspace in the given
     * <code>workspaceDir</code>.
     */
    public static boolean isRegularWorkSpace(File workspaceDir) {
        assert workspaceDir == null || workspaceDir.equals(FileUtil.normalizeFile(workspaceDir)) : "#137407 problem: " + workspaceDir + " vs. " + FileUtil.normalizeFile(workspaceDir); //NOI18N
        return workspaceDir != null
                && FileUtil.toFileObject(workspaceDir) != null
                && workspaceDir.isDirectory()
                && new File(workspaceDir, Workspace.CORE_PREFERENCE).isFile()
                && new File(workspaceDir, Workspace.RESOURCE_PROJECTS_DIR).isDirectory();
    }
    
    private static final String TMP_NAME =
            "NB___TMP___ENOUGH___UNIQUE___CONSTANT___"; // NOI18N
    
    public static boolean isWritable(String projectDestination) {
        File tmpDir = new File(projectDestination.trim(),
                (TMP_NAME + System.currentTimeMillis()));
        if (tmpDir.mkdirs()) {
            tmpDir.delete();
            return true;
        }
        return false;
    }
    
    /**
     * Load properties from a given <code>file</code>.
     * <p>
     * <strong>Note: package private for unit tests only.</strong>
     * 
     * @throws IOException when reading file failed
     */
    static Map<String,String> loadProperties(File file) throws IOException {
        InputStream propsIS = new BufferedInputStream(new FileInputStream(file));
        Properties properties = new Properties();
        try {
            properties.load(propsIS);
        } finally {
            propsIS.close();
        }
        return NbCollections.checkedMapByFilter(properties, String.class, String.class, true);
    }
    
    /**
     * Splits Eclipse variable into variable and path, eg. MAVEN/commons/1.jar
     * is split into MAVEN and /commons/1.jar.
     */
    public static String[] splitVariable(String v) {
        int i = v.replace('\\', '/').indexOf('/');
        if (i == -1) {
            i = v.length();
        }
        return new String[]{v.substring(0, i), v.substring(i)};
    }        

    /**
     * Splits Eclipse internal jar reference into project name and path wihtin project, 
     * eg. /some-project/commons/1.jar is split into some-project and /commons/1.jar.
     */
    public static String[] splitProject(String v) {
        assert v.startsWith("/") : v; //NOI18N
        int i = v.replace('\\', '/').indexOf('/', 1); //NOI18N
        if (i == -1) {
            i = v.length();
        }
        return new String[]{v.substring(1, i), v.substring(i)};
    }        

    public static void tryLoad(Properties p, File base, String path) {
        if (base == null) {
            return;
        }
        File f = new File(base, path);
        tryLoad(p, f);
    }
    
    public static void tryLoad(Properties p, File f) {
        if (!f.isFile()) {
            return;
        }
        try {
            InputStream is = new FileInputStream(f);
            try {
                p.load(is);
            } finally {
                is.close();
            }
        } catch (IOException x) {
            Exceptions.printStackTrace(x);
        }
    }

}
