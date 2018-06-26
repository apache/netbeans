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
package org.netbeans.test.web;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Constructor;
import java.util.Enumeration;
import java.util.logging.Logger;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jindrich Sedek
 */
public class RecurrentSuiteFactory {

    public static Test createSuite(Class clazz, File projectsDir, FileObjectFilter filter) {
        String clazzName = clazz.getName();
        NbTestSuite suite = new NbTestSuite(clazzName);
        try {
            //get list of projects to be used for testing
            File[] projects = projectsDir.listFiles(new FilenameFilter() {
                // filter out non-project folders
                public boolean accept(File dir, String fileName) {
                    return !fileName.equals("CVS");
                }
            });
            if (projects != null) {
                for (int i = 0; i < projects.length; i++) {
                    Logger.getLogger(RecurrentSuiteFactory.class.getName()).info("Prj Folder: " + projects[i].getName());
                    // find recursively all test.*[.jsp|.jspx|.jspf|.html] files in
                    // the web/ folder

                    // TODO check if the project is of current version and if necessery update it.
                    // enables transparent update, see: org.netbeans.modules.web.project.UpdateHelper
                    // System.setProperty("webproject.transparentUpdate",  "true");

                    FileObject prjDir = FileUtil.toFileObject(projects[i]);
                    Enumeration fileObjs = prjDir.getChildren(true);

                    while (fileObjs.hasMoreElements()) {
                        FileObject fo = (FileObject) fileObjs.nextElement();
                        if (filter.accept(fo)) {
                            String testName = projects[i].getName() + "_" + FileUtil.getRelativePath(prjDir, fo).replaceAll("[/.]", "_");
                            Constructor cnstr = clazz.getDeclaredConstructor(new Class[]{String.class, FileObject.class});
                            NbTestCase test = (NbTestCase) cnstr.newInstance(new Object[]{testName, fo});
                            suite.addTest(test);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
        }
        return suite;
    }

}
