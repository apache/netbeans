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

package org.netbeans.modules.j2ee.ejbjarproject;

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
import org.netbeans.modules.j2ee.ejbjarproject.test.ProjectUtil;
import org.netbeans.modules.j2ee.ejbjarproject.test.TestUtil;
import org.netbeans.modules.j2ee.ejbjarproject.ui.customizer.EjbJarProjectProperties;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.project.uiapi.ProjectOpenedTrampoline;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
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
        super.setUp();
        MockLookup.setLayersAndInstances();
        System.setProperty("netbeans.user", getWorkDirPath());
    }

    public void testSingleDir() throws Exception {
        File projectFile = ProjectUtil.getProjectAsFile(this, "CopyOnSaveTest");
        FileObject projectFileObject = FileUtil.toFileObject(FileUtil.normalizeFile(projectFile));
        Project project = ProjectManager.getDefault().findProject(projectFileObject);

        EjbJarProject ejbProject = (EjbJarProject) project;
        openProject(ejbProject);

        try {
            singleBasicTest(projectFileObject, "src/conf");
        } finally {
            closeProject(ejbProject);
        }
    }


    public void testChangedMeta() throws Exception {
        File projectFile = ProjectUtil.getProjectAsFile(this, "CopyOnSaveTest");
        FileObject projectFileObject = FileUtil.toFileObject(FileUtil.normalizeFile(projectFile));
        final Project project = ProjectManager.getDefault().findProject(projectFileObject);

        final EjbJarProject webProject = (EjbJarProject) project;
        openProject(webProject);

        try {
            singleBasicTest(projectFileObject, "src/conf");

            ProjectManager.mutex().postWriteRequest(new Runnable() {
                public void run() {
                    try {
                        UpdateHelper helper = webProject.getUpdateHelper();
                        EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                        props.put(EjbJarProjectProperties.META_INF, "src/conf2");
                        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                        ProjectManager.getDefault().saveProject(project);
                    } catch (IOException e) {
                        fail("Could not configure web docbase");
                    }
                }
            });

            singleBasicTest(projectFileObject, "src/conf2");
        } finally {
            closeProject(webProject);
        }
    }

    private void singleBasicTest(FileObject projectFileObject, String metaPrefix) throws Exception {
        File sampleXml = new File(getDataDir(), "ejb-jar.xml");
        FileObject sampleXmlFileObject = FileUtil.toFileObject(sampleXml);

        File destFolder = FileUtil.toFile(projectFileObject.getFileObject(metaPrefix));
        File destFile = new File(destFolder, "ejb-jar-backup.xml");
        FileObject toCheck = projectFileObject.getFileObject("build/jar/META-INF/ejb-jar-backup.xml");
        if (toCheck != null) {
            toCheck.delete();
        }

        FileObject webFileObject = FileUtil.toFileObject(destFile.getParentFile());

        // copy file
        FileObject destFileObject = FileUtil.copyFile(sampleXmlFileObject, webFileObject, "ejb-jar-backup");
        toCheck = projectFileObject.getFileObject("build/jar/META-INF/ejb-jar-backup.xml");
        assertFile(FileUtil.toFile(toCheck), sampleXml);

        // delete file
        destFileObject.delete();
        assertFalse(toCheck.isValid());

        // change file
        destFolder = FileUtil.toFile(projectFileObject.getFileObject(metaPrefix));
        File index = new File(destFolder, "ejb-jar.xml");
        FileObject indexFileObject = FileUtil.toFileObject(index);
        InputStream is = new BufferedInputStream(new FileInputStream(sampleXml));
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

        toCheck = projectFileObject.getFileObject("build/jar/META-INF/ejb-jar.xml");
        assertFile(FileUtil.toFile(toCheck), sampleXml);

        // cleanup a bit
        toCheck.delete();
    }

    /**
     * Accessor method for those who wish to simulate open of a project and in
     * case of suite for example generate the build.xml.
     */
    public static void openProject(final EjbJarProject p) throws Exception {
        ProjectOpenedHook hook = p.getLookup().lookup(ProjectOpenedHook.class);
        assertNotNull("has an OpenedHook", hook);
        ProjectOpenedTrampoline.DEFAULT.projectOpened(hook);
    }

    public static void closeProject(final EjbJarProject p) throws Exception {
        ProjectOpenedHook hook = p.getLookup().lookup(ProjectOpenedHook.class);
        assertNotNull("has an OpenedHook", hook);
        ProjectOpenedTrampoline.DEFAULT.projectClosed(hook);
    }
}
