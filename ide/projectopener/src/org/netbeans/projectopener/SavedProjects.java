/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
