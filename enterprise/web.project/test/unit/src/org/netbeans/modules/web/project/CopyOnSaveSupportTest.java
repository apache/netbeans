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
