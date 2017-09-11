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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
