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

package org.netbeans.projectopener;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Milan Kubec
 */
public class SavedProjects {
    
    private Collection/*<SavedProjects.OneProject>*/ savedProjects;
    private Collection/*<ProjectType>*/ projectTypes;
    
    public SavedProjects(List sp, Collection pt) {
        savedProjects = sp;
        projectTypes = pt;
    }
    
    /**
     * Returns sorted array of stored project's paths
     * @param mainPrjPath is the path in the zip file, separator is '/',
     * last folder in the path is considered to be project name
     */
    public String[] getSortedProjectsPaths(String mainPrjPath) {
        String transMainPrjPath = mainPrjPath.replace('/', File.separatorChar);
        String mainPrjName = transMainPrjPath.substring(transMainPrjPath.lastIndexOf(File.separatorChar) + 1);
        String prjPaths[] = new String[savedProjects.size()];
        String lastPrjPath = null;
        int index = 0;
        for (Iterator iter = savedProjects.iterator(); iter.hasNext(); ) {
            SavedProjects.OneProject sp = (SavedProjects.OneProject) iter.next();
            String prjPath = sp.getProjectPath();
            if (prjPath.indexOf(transMainPrjPath) != -1 && 
                    sp.getProjectName().equals(mainPrjName)) {
                lastPrjPath = prjPath;
            } else {
                prjPaths[index++] = prjPath;
            }
        }
        if (lastPrjPath != null) {
            prjPaths[index] = lastPrjPath;
        }
        return prjPaths;
    }
    
    public String[] getProjectPaths() {
        String prjPaths[] = new String[savedProjects.size()];
        int index = 0;
        for (Iterator iter = savedProjects.iterator(); iter.hasNext(); ) {
            SavedProjects.OneProject sp = (SavedProjects.OneProject) iter.next();
            prjPaths[index++] = sp.getProjectPath();
        }
        return prjPaths;
    }
    
    public Collection/*<ProjectType>*/ getTypes() {
        return projectTypes;
    }
    
    public static class OneProject {
        
        private File folder;
        
        public OneProject(File dir) {
            folder = dir;
        }
        
        public String getProjectName() {
            return folder.getName();
        }
        
        public String getProjectPath() {
            return folder.getAbsolutePath();
        }
        
        public String toString() {
            return folder.getAbsolutePath();
        }
        
    }
    
}
