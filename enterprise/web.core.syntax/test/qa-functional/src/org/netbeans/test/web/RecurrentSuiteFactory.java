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
