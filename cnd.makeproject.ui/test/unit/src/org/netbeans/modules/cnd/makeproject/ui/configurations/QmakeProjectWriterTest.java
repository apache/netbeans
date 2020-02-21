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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.makeproject.ui.configurations;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.Test;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.makeproject.ConfigurationDescriptorProviderImpl;
import org.netbeans.modules.cnd.makeproject.MakeProjectTypeImpl;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.ui.wizard.WizardConstants;
import org.netbeans.modules.cnd.makeproject.ui.wizards.MakeSampleProjectIterator;
import org.netbeans.modules.cnd.test.CndBaseTestCase;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;

/**
 */
public class QmakeProjectWriterTest extends CndBaseTestCase {

    public QmakeProjectWriterTest(String name) {
        super(name);
    }

    @Override
    protected List<Class<?>> getServices() {
        List<Class<?>> list = new ArrayList<>();
        list.add(MakeProjectTypeImpl.class);
        list.addAll(super.getServices());
        return list;
    }

    private static void instantiateSample(String name, File destdir) throws IOException {
        FileObject templateFO = FileUtil.getConfigFile("Templates/Project/Samples/Native/" + name);
        assertNotNull("FileObject for " + name + " sample not found", templateFO);
        DataObject templateDO = DataObject.find(templateFO);
        assertNotNull("DataObject for " + name + " sample not found", templateDO);
        MakeSampleProjectIterator projectCreator = new MakeSampleProjectIterator();
        TemplateWizard wiz = new TemplateWizard();
        wiz.setTemplate(templateDO);
        projectCreator.initialize(wiz);
        WizardConstants.PROPERTY_NAME.put(wiz, destdir.getName());
        ExecutionEnvironment ee = ExecutionEnvironmentFactory.getLocal();
        WizardConstants.PROPERTY_PROJECT_FOLDER.put(wiz, 
                new FSPath(FileSystemProvider.getFileSystem(ee), RemoteFileUtil.normalizeAbsolutePath(destdir.getAbsolutePath(), ee)));
        projectCreator.instantiate();
    }

    @Test
    public void testHelloQtWorld() throws IOException {
        File projectDir = new File(getWorkDir(), "HelloQtWorld_1");
        instantiateSample("HelloQtWorld", projectDir);

        FileObject projectDirFO = CndFileUtils.toFileObject(projectDir);
        ConfigurationDescriptorProvider descriptorProvider = new ConfigurationDescriptorProviderImpl(projectDirFO);
        MakeConfigurationDescriptor descriptor = descriptorProvider.getConfigurationDescriptor();
        descriptor.save(); // make sure all necessary configuration files in nbproject/ are written

        File qtDebug = new File(projectDir, "nbproject/qt-Debug.pro");
        assertFile(qtDebug, new Object[]{
                    "# This file is generated automatically. Do not edit.",
                    "# Use project properties -> Build -> Qt -> Expert -> Custom Definitions.",
                    "TEMPLATE = app",
                    Pattern.compile("DESTDIR = dist/Debug/.+"),
                    "TARGET = HelloQtWorld_1",
                    "VERSION = 1.0.0",
                    "CONFIG -= debug_and_release app_bundle lib_bundle",
                    "CONFIG += debug ",
                    "PKGCONFIG +=",
                    "QT = core gui widgets",
                    Pattern.compile("SOURCES \\+= (newmain.cpp HelloForm.cpp|HelloForm.cpp newmain.cpp)"),
                    "HEADERS += HelloForm.h",
                    "FORMS += HelloForm.ui",
                    "RESOURCES +=",
                    "TRANSLATIONS +=",
                    Pattern.compile("OBJECTS_DIR = build/Debug/.+"),
                    "MOC_DIR = ",
                    "RCC_DIR = ",
                    "UI_DIR = ",
                    Pattern.compile("QMAKE_CC = .+"),
                    Pattern.compile("QMAKE_CXX = .+"),
                    "DEFINES += ",
                    "INCLUDEPATH += ",
                    "LIBS += "
                });

        File qtRelease = new File(projectDir, "nbproject/qt-Release.pro");
        assertFile(qtRelease, new Object[]{
                    "# This file is generated automatically. Do not edit.",
                    "# Use project properties -> Build -> Qt -> Expert -> Custom Definitions.",
                    "TEMPLATE = app",
                    Pattern.compile("DESTDIR = dist/Release/.+"),
                    "TARGET = HelloQtWorld_1",
                    "VERSION = 1.0.0",
                    "CONFIG -= debug_and_release app_bundle lib_bundle",
                    "CONFIG += release ",
                    "PKGCONFIG +=",                    
                    "QT = core gui widgets",
                    Pattern.compile("SOURCES \\+= (newmain.cpp HelloForm.cpp|HelloForm.cpp newmain.cpp)"),
                    "HEADERS += HelloForm.h",
                    "FORMS += HelloForm.ui",
                    "RESOURCES +=",
                    "TRANSLATIONS +=",
                    Pattern.compile("OBJECTS_DIR = build/Release/.+"),
                    "MOC_DIR = ",
                    "RCC_DIR = ",
                    "UI_DIR = ",
                    Pattern.compile("QMAKE_CC = .+"),
                    Pattern.compile("QMAKE_CXX = .+"),
                    "DEFINES += ",
                    "INCLUDEPATH += ",
                    "LIBS += "
                });
    }

    private void assertFile(File file, Object lines[]) {
        assertTrue(file.getName() + " not found", file.exists());
        try {
            BufferedReader r = new BufferedReader(new FileReader(file));
            try {
                for (int i = 0; i < lines.length; ++i) {
                    String line = r.readLine();
                    assertNotNull("File is too short, only " + i + " lines", line);
                    Object pattern = lines[i];
                    if (pattern instanceof String) {
                        assertEquals((String)pattern, line);
                    } else if (pattern instanceof Pattern) {
                        assertTrue(((Pattern)pattern).matcher(line).matches());
                    } else {
                        fail("Expected String or Pattern instance, got " + pattern);
                    }
                }
            } finally {
                r.close();
            }
        } catch (IOException ex) {
            fail("Caught " + ex);
        }
    }
}
