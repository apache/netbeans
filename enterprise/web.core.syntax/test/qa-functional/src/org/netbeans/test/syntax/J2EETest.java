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
package org.netbeans.test.syntax;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import javax.swing.SwingUtilities;
import org.netbeans.test.web.FileObjectFilter;
import org.netbeans.test.web.RecurrentSuiteFactory;
import org.openide.filesystems.FileObject;
import junit.framework.Test;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestSuite;

/** 
 *
 * @author ms113234
 */
public class J2EETest extends CompletionTest {

        /** Creates a new instance of CompletionTesJ2EE */
    public J2EETest(String name, FileObject testFileObj) {
        super(name, testFileObj);
    }

    public static Test suite() {
//        NbModuleSuite.Configuration conf = NbModuleSuite.createConfiguration(J2EETest.class);
        NbModuleSuite.Configuration conf = NbModuleSuite.emptyConfiguration();
        addServerTests(Server.GLASSFISH, conf, new String[0]);//register server
        conf = conf.enableModules(".*").clusters(".*");
        return NbModuleSuite.create(conf.addTest(SuiteCreator.class));
    }

    public static final class SuiteCreator extends NbTestSuite {

        public SuiteCreator() {
            super();
            File datadir = new J2EETest(null, null).getDataDir();
            File projectsDir = new File(datadir, "J2EECompletionTestProjects");
            FileObjectFilter filter = new FileObjectFilter() {

                public boolean accept(FileObject fo) {
                    String ext = fo.getExt();
                    String name = fo.getName();
                    return (name.startsWith("test") || name.startsWith("Test")) && (XML_EXTS.contains(ext) || JSP_EXTS.contains(ext) || ext.equals("java"));
                }
            };

            int time = 0;
            while ((ConnectionManager.getDefault().getConnections().length == 0) && (time <= 12)) {
                time++;
                try {
                    Thread.sleep(5000);
                } catch (Exception e) {
                    e.printStackTrace(System.err);
                }
            }
            if (time > 12) {
                System.err.println("IMPOSSIBLE TO CONNECT THE DATABASE");
            } else {
                final DatabaseConnection dbconn = ConnectionManager.getDefault().getConnections()[0];
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {

                        public void run() {
                            ConnectionManager.getDefault().showConnectionDialog(dbconn);
                        }
                    });
                } catch (InterruptedException e) {
                } catch (InvocationTargetException e) {
                }
            }
            addTest(RecurrentSuiteFactory.createSuite(J2EETest.class, projectsDir, filter));
        }
    }

    @Override
    protected File getProjectsDir() {
        File datadir = new CompletionTest().getDataDir();
        return new File(datadir, "J2EECompletionTestProjects");
    }

    @Override
    public void runTest() throws Exception {
        if (testFileObj == null) {
            return;
        }

        String ext = testFileObj.getExt();
        if (JSP_EXTS.contains(ext)) {
            test(testFileObj, "<%--CC", "--%>");
        } else if (XML_EXTS.contains(ext)) {
            test(testFileObj, "<!--CC", "-->", false);
        } else if (JS_EXTS.contains(ext) || ext.equals("java")) {
            test(testFileObj, "/**CC", "*/", false);
        } else {
            throw new JemmyException("File extension of: " + testFileObj.getNameExt() + " is unsupported.");
        }
    }

    private void test(FileObject fileObj, String stepStart, String stepEnd) throws Exception {
        test(fileObj, stepStart, stepEnd, true);
    }
}
