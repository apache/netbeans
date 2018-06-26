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
