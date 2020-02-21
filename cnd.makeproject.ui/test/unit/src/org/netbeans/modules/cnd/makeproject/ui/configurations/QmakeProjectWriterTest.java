/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
