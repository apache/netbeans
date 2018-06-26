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
