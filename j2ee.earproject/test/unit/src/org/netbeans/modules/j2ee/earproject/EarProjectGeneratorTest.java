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

package org.netbeans.modules.j2ee.earproject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.j2ee.earproject.test.TestUtil;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.test.MockLookup;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * @author vkraemer
 */
public class EarProjectGeneratorTest extends NbTestCase {

    private static final String[] CREATED_FILES = {
        "build.xml",
        "nbproject/build-impl.xml",
        "nbproject/genfiles.properties",
        "nbproject/project.xml",
        "nbproject/project.properties",
        "nbproject/private/private.properties",
        "src/conf/application.xml"
    };

    private static final String[] CREATED_FILES_EXT_SOURCES = {
        "build.xml",
        "nbproject/build-impl.xml",
        "nbproject/genfiles.properties",
        "nbproject/project.xml",
        "nbproject/project.properties",
        "nbproject/private/private.properties",
    };

    private static final String[] CREATED_PROPERTIES = {
        "build.classes.excludes",
        "build.dir",
        "build.generated.dir",
        "client.module.uri",
        "client.urlPart",
        "debug.classpath",
        "display.browser",
        "dist.dir",
        "dist.jar",
        "j2ee.deploy.on.save",
        "j2ee.compile.on.save",
        "j2ee.platform",
        "j2ee.platform.classpath",
        "j2ee.server.type",
        "jar.compress",
        "jar.content.additional",
        "jar.name",
        "javac.debug",
        "javac.deprecation",
        "javac.source",
        "javac.target",
        "meta.inf",
        "no.dependencies",
        "platform.active",
        "resource.dir",
        "source.root",
    };

    private static final String[] CREATED_PROPERTIES_EXT_SOURCES = {
        "build.classes.excludes",
        "build.dir",
        "build.generated.dir",
        "client.module.uri",
        "client.urlPart",
        "debug.classpath",
        "display.browser",
        "dist.dir",
        "dist.jar",
        "j2ee.deploy.on.save",
        "j2ee.compile.on.save",
        "j2ee.platform",
        "j2ee.platform.classpath",
        "j2ee.server.type",
        "jar.compress",
        "jar.content.additional",
        "jar.name",
        "javac.debug",
        "javac.deprecation",
        "javac.source",
        "javac.target",
        "meta.inf",
        "no.dependencies",
        "platform.active",
        //"resource.dir",  -XXX- this is not found in project.props
        //        when the project is created from ex. sources. Bug or not???
        "source.root"
    };

    public EarProjectGeneratorTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        TestUtil.makeScratchDir(this);

        MockLookup.setLayersAndInstances();
    }

    public void testCreateProjectJavaEE5() throws Exception {
        File prjDirF = new File(getWorkDir(), "EARProject");
        AntProjectHelper aph = EarProjectGenerator.createProject(prjDirF, "test-project",
                Profile.JAVA_EE_5, TestUtil.SERVER_URL, "1.5", null);
        assertNotNull(aph);
        FileObject prjDirFO = aph.getProjectDirectory();
        for (String file : CREATED_FILES) {
            FileObject fo = prjDirFO.getFileObject(file);
            if ("src/conf/application.xml".equals(file)) {
                // deployment descriptor should not exist
                assertNull(file + " file/folder should not exist", fo);
            } else {
                assertNotNull(file + " file/folder should exist", fo);
            }
        }
        EditableProperties props = aph.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        @SuppressWarnings("unchecked")
        List createdProperties = new ArrayList(props.keySet());
        for (String property : CREATED_PROPERTIES) {
            assertNotNull(property + " property cannot be found in project.properties", props.getProperty(property));
            createdProperties.remove(property);
        }
        assertEquals("Found unexpected property: " + createdProperties,
                CREATED_PROPERTIES.length, props.keySet().size());
    }

    public void testCreateProjectJ2EE14() throws Exception {
        File prjDirF = new File(getWorkDir(), "EARProject");
        AntProjectHelper aph = EarProjectGenerator.createProject(prjDirF, "test-project",
                Profile.J2EE_14, TestUtil.SERVER_URL, "1.4", null);
        assertNotNull(aph);
        FileObject prjDirFO = aph.getProjectDirectory();
        for (String file : CREATED_FILES) {
            assertNotNull(file + " file/folder cannot be found", prjDirFO.getFileObject(file));
        }
        EditableProperties props = aph.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        @SuppressWarnings("unchecked")
        List createdProperties = new ArrayList(props.keySet());
        for (String property : CREATED_PROPERTIES) {
            assertNotNull(property + " property cannot be found in project.properties", props.getProperty(property));
            createdProperties.remove(property);
        }
        assertEquals("Found unexpected property: " + createdProperties,
                CREATED_PROPERTIES.length, props.keySet().size());
    }

    public void testImportProject() throws Exception {
        File prjDirF = new File(getWorkDir(), "EARProject");
        AntProjectHelper helper = EarProjectGenerator.importProject(prjDirF, prjDirF,
                "test-project-ext-src", Profile.JAVA_EE_5, TestUtil.SERVER_URL, null,
                "1.5", Collections.<FileObject, ModuleType>emptyMap(), null);
        assertNotNull(helper);
        FileObject prjDirFO = FileUtil.toFileObject(prjDirF);
        for (String createdFile : CREATED_FILES_EXT_SOURCES) {
            assertNotNull(createdFile + " file/folder cannot be found", prjDirFO.getFileObject(createdFile));
        }
        EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        @SuppressWarnings("unchecked")
        List createdProperties = new ArrayList(props.keySet());
        int extFileRefCount = 0;

        List<String> extProperties = new ArrayList<String>();
        Collections.addAll(extProperties, CREATED_PROPERTIES_EXT_SOURCES);
        extProperties.add("file.reference." + getWorkDir().getName() + "-EARProject");

        for (String propName : extProperties) {
            String propValue = props.getProperty(propName);
            assertNotNull(propName+" property cannot be found in project.properties", propValue);
            createdProperties.remove(propName);
            if ("manifest.file".equals(propName)) {
                assertEquals("Invalid value of manifest.file property.", "manifest.mf", propValue);
            }
        }
        assertEquals("Found unexpected property: " + createdProperties,
                extProperties.size(), props.keySet().size() - extFileRefCount);
    }

    public void testProjectNameIsSet() throws Exception { // #73930
        File prjDirF = new File(getWorkDir(), "EARProject");
        EarProjectGenerator.createProject(prjDirF, "test-project",
                Profile.JAVA_EE_5, TestUtil.SERVER_URL, "1.5", null);
        // test also build
        final File buildXML = new File(prjDirF, "build.xml");
        String projectName = ProjectManager.mutex().readAccess(new Mutex.ExceptionAction<String>() {
            public String run() throws Exception {
                Document doc = XMLUtil.parse(new InputSource(buildXML.toURI().toString()),
                        false, true, null, null);
                Element project = doc.getDocumentElement();
                return project.getAttribute("name");
            }
        });
        assertEquals("project name is set in the build.xml", "test-project", projectName);
    }

    public void testProjectNameIsEscaped() throws Exception {
        final File prjDirF = new File(getWorkDir(), "EARProject");
        EarProjectGenerator.createProject(prjDirF, "test project",
                Profile.JAVA_EE_5, TestUtil.SERVER_URL, "1.5", null);
        // test build.xml
        String buildXmlProjectName = ProjectManager.mutex().readAccess(new Mutex.ExceptionAction<String>() {
            public String run() throws Exception {
                Document doc = XMLUtil.parse(new InputSource(new File(prjDirF, "build.xml").toURI().toString()),
                        false, true, null, null);
                Element project = doc.getDocumentElement();
                return project.getAttribute("name");
            }
        });
        assertEquals("project name is escaped in build.xml", "test_project", buildXmlProjectName);
        // test build-impl.xml
        String buildImplXmlProjectName = ProjectManager.mutex().readAccess(new Mutex.ExceptionAction<String>() {
            public String run() throws Exception {
                Document doc = XMLUtil.parse(new InputSource(new File(prjDirF, "nbproject/build-impl.xml").toURI().toString()),
                        false, true, null, null);
                Element project = doc.getDocumentElement();
                return project.getAttribute("name");
            }
        });
        assertEquals("project name is escaped in build-impl.xml", "test_project-impl", buildImplXmlProjectName);
    }

}
