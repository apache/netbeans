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
package org.netbeans.modules.j2ee.clientproject.classpath;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.clientproject.api.AppClientProjectGenerator;
import org.netbeans.modules.j2ee.clientproject.test.TestUtil;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Jan Lahoda
 */
public class AppClientProjectClassPathExtenderTest extends NbTestCase {

    private FileObject workDir;

    public AppClientProjectClassPathExtenderTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = TestUtil.makeScratchDir(this);

        MockLookup.setLayersAndInstances();
    }

    public void testPropertyChangeDeadlock74204() throws Exception {
        File prjDirF = new File(FileUtil.toFile(workDir), "test");
        AntProjectHelper helper = AppClientProjectGenerator.createProject(prjDirF, "test-project",
                "test.MyMain", J2eeModule.JAVA_EE_5, TestUtil.SERVER_URL);
        final Project project = ProjectManager.getDefault().findProject(helper.getProjectDirectory());
        
        final Object privateLock = new Object();
        final CountDownLatch sync = new CountDownLatch(2);
        
        FileChangeListener l = new FileChangeAdapter() {
            public @Override void fileChanged(FileEvent fe) {
                try {
                    sync.countDown();
                    sync.await();
                    synchronized (privateLock) {}
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        };
        
        project.getProjectDirectory().getFileObject("nbproject").getChildren();
        project.getProjectDirectory().getFileObject("nbproject").addFileChangeListener(l);
        project.getProjectDirectory().getFileObject("nbproject/project.properties").addFileChangeListener(l);
        
        new Thread() {
            @Override
            public void run() {
                synchronized (privateLock) {
                    try {
                        sync.countDown();
                        sync.await();
                        ProjectManager.mutex().readAccess(new Runnable() {
                            public void run() {
                            }
                        });
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }.start();
        
        EditableProperties ep = new EditableProperties();
        
        ep.put(ProjectProperties.JAVAC_CLASSPATH, "y");
        
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
    }
    
}
