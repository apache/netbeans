/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.project;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.web.project.test.ProjectUtil;
import org.netbeans.modules.web.project.test.TestUtil;
import org.netbeans.modules.web.project.ui.customizer.WebProjectProperties;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Petr Hejl
 */
public class CopyOnSaveSupportTest extends NbTestCase {

    public CopyOnSaveSupportTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        MockLookup.setLayersAndInstances();
    }

    public void testSingleDir() throws Exception {
        File projectFile = ProjectUtil.getProjectAsFile(this, "CopyOnSaveTest");
        FileObject projectFileObject = FileUtil.toFileObject(FileUtil.normalizeFile(projectFile));
        Project project = ProjectManager.getDefault().findProject(projectFileObject);

        WebProject webProject = (WebProject) project;
        WebProjectTest.openProject(webProject);

        try {
            singleBasicTest(projectFileObject, "web");
        } finally {
            WebProjectTest.closeProject(webProject);
        }
    }

    public void testSeparateWebInf() throws Exception {
        File projectFile = ProjectUtil.getProjectAsFile(this, "CopyOnSaveTest");
        FileObject projectFileObject = FileUtil.toFileObject(FileUtil.normalizeFile(projectFile));
        final Project project = ProjectManager.getDefault().findProject(projectFileObject);

        final WebProject webProject = (WebProject) project;
        WebProjectTest.openProject(webProject);

        ProjectManager.mutex().postWriteRequest(new Runnable() {
            public void run() {
                try {
                    UpdateHelper helper = webProject.getUpdateHelper();
                    EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                    props.put(WebProjectProperties.WEB_DOCBASE_DIR, "web_1");
                    helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                    ProjectManager.getDefault().saveProject(project);
                } catch (IOException e) {
                    fail("Could not configure web docbase");
                }
            }
        });

        try {
            singleBasicTest(projectFileObject, "web_1");
        } finally {
            WebProjectTest.closeProject(webProject);
        }
    }

    public void testChangedWeb() throws Exception {
        File projectFile = ProjectUtil.getProjectAsFile(this, "CopyOnSaveTest");
        FileObject projectFileObject = FileUtil.toFileObject(FileUtil.normalizeFile(projectFile));
        final Project project = ProjectManager.getDefault().findProject(projectFileObject);

        final WebProject webProject = (WebProject) project;
        WebProjectTest.openProject(webProject);

        try {
            singleBasicTest(projectFileObject, "web");

            ProjectManager.mutex().postWriteRequest(new Runnable() {
                public void run() {
                    try {
                        UpdateHelper helper = webProject.getUpdateHelper();
                        EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                        props.put(WebProjectProperties.WEB_DOCBASE_DIR, "web_1");
                        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                        ProjectManager.getDefault().saveProject(project);
                    } catch (IOException e) {
                        fail("Could not configure web docbase");
                    }
                }
            });

            singleBasicTest(projectFileObject, "web_1");
        } finally {
            WebProjectTest.closeProject(webProject);
        }
    }

    private void singleBasicTest(FileObject projectFileObject, String webPrefix) throws Exception {
        File projectFile = FileUtil.toFile(projectFileObject);

        File sampleJsp = new File(getDataDir(), "sample.jsp");
        FileObject sampleJspFileObject = FileUtil.toFileObject(sampleJsp);

        File destFile = new File(new File(projectFile, webPrefix), "sample.jsp");
        FileObject toCheck = projectFileObject.getFileObject("build/web/sample.jsp");
        if (toCheck != null) {
            toCheck.delete();
        }

        FileObject webFileObject = FileUtil.toFileObject(destFile.getParentFile());

        // copy file
        FileObject destFileObject = FileUtil.copyFile(sampleJspFileObject, webFileObject, "sample");
        toCheck = projectFileObject.getFileObject("build/web/sample.jsp");
        assertFile(FileUtil.toFile(toCheck), sampleJsp);

        // delete file
        destFileObject.delete();
        assertFalse(toCheck.isValid());

        // change file
        File index = new File(new File(projectFile, webPrefix), "index.jsp");
        FileObject indexFileObject = FileUtil.toFileObject(index);
        InputStream is = new BufferedInputStream(new FileInputStream(sampleJsp));
        try {
            OutputStream os = new BufferedOutputStream(indexFileObject.getOutputStream());
            try {
                FileUtil.copy(is, os);
            } finally {
                os.close();
            }
        } finally {
            is.close();
        }

        toCheck = projectFileObject.getFileObject("build/web/index.jsp");
        assertFile(FileUtil.toFile(toCheck), sampleJsp);

        // cleanup a bit
        toCheck.delete();

        // change file in WEB-INF
        File sampleWebXml = new File(getDataDir(), "web.xml");

        File webXml = new File(new File(new File(projectFile, "web"), "WEB-INF"), "web.xml");
        FileObject webXmlFileObject = FileUtil.toFileObject(webXml);
        is = new BufferedInputStream(new FileInputStream(sampleWebXml));
        try {
            OutputStream os = new BufferedOutputStream(webXmlFileObject.getOutputStream());
            try {
                FileUtil.copy(is, os);
            } finally {
                os.close();
            }
        } finally {
            is.close();
        }

        toCheck = projectFileObject.getFileObject("build/web/WEB-INF/web.xml");
        assertFile(FileUtil.toFile(toCheck), sampleWebXml);

        // cleanup a bit
        toCheck.delete();
    }
}
