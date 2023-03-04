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

package org.netbeans.core.osgi;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MultiFileSystem;
import org.openide.modules.ModuleInstall;

public class OSGiRepositoryTest extends NbTestCase {

    public OSGiRepositoryTest(String n) {
        super(n);
    }
    
    @Override protected Level logLevel() {
        return Level.FINE;
    }
    
    @Override protected String logRoot() {
        return "org.netbeans.core.osgi";
    }

    protected @Override void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    }

    public void testLayers() throws Exception {
        new OSGiProcess(getWorkDir()).newModule().clazz(LayersInstall.class).sourceFile("custom/layer.xml", "<filesystem>",
                "<file name='whatever'/>",
                "</filesystem>").manifest(
                "OpenIDE-Module: custom",
                "OpenIDE-Module-Install: " + LayersInstall.class.getName(),
                "OpenIDE-Module-Layer: custom/layer.xml",
                "OpenIDE-Module-Module-Dependencies: org.openide.modules, org.openide.filesystems").done().run(false);
        assertEquals("whatever", System.getProperty("my.file"));
    }
    public static class LayersInstall extends ModuleInstall {
        public @Override void restored() {
            System.setProperty("my.file", FileUtil.getConfigFile("whatever").getPath());
        }
    }

    @RandomlyFails // sometimes in NB-Core-Build:
    // FNFE: Invalid settings.providerPath=xml/lookups/NetBeans/DTD_XML_beans_1_0.instance under SFS/xml/memory/ for class custom.Install$Bean
    public void testSettings() throws Exception {
        new OSGiProcess(getWorkDir()).newModule().manifest(
                "OpenIDE-Module: custom",
                "OpenIDE-Module-Install: custom.Install",
                "OpenIDE-Module-Module-Dependencies: org.netbeans.modules.settings/1, org.openide.loaders, " +
                "org.openide.filesystems, org.openide.modules, org.openide.util").
                sourceFile("custom/Install.java", "package custom;",
                "public class Install extends org.openide.modules.ModuleInstall {",
                "public @Override void restored() {",
                "Bean b = new Bean(); b.setP(\"hello\");",
                "try {",
                "org.openide.loaders.InstanceDataObject.create(org.openide.loaders.DataFolder.findFolder(",
                "org.openide.filesystems.FileUtil.getConfigRoot().createFolder(\"d\")), \"x\", b, null);",
                "System.setProperty(\"my.settings\", org.openide.filesystems.FileUtil.getConfigFile(\"d/x.settings\").asText());",
                "} catch (Exception x) {x.printStackTrace();}",
                "}",
                "@org.netbeans.api.settings.ConvertAsJavaBean public static class Bean {",
                "private String p; public String getP() {return p;} public void setP(String p2) {p = p2;}",
                "public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {}",
                "public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {}",
                "}",
                "}").done().
                module("org.netbeans.modules.settings").
                run(false);
        String settings = System.getProperty("my.settings");
        assertNotNull(settings);
        assertTrue(settings, settings.contains("<string>hello</string>"));
    }

    public void testDynamic() throws Exception {
        new OSGiProcess(getWorkDir()).newModule().clazz(DynamicInstall.class).clazz(DynLayer.class).service(FileSystem.class, DynLayer.class).manifest(
                "OpenIDE-Module: custom",
                "OpenIDE-Module-Install: " + DynamicInstall.class.getName(),
                "OpenIDE-Module-Module-Dependencies: org.openide.modules, org.openide.filesystems").done().run(false);
        assertEquals("5", System.getProperty("dyn.file.length"));
    }
    public static class DynamicInstall extends ModuleInstall {
        public @Override void restored() {
            FileObject f = FileUtil.getConfigFile("whatever");
            System.setProperty("dyn.file.length", f != null ? Long.toString(f.getSize()) : "missing");
        }
    }
    public static class DynLayer extends MultiFileSystem {
        public DynLayer() throws Exception {
            FileSystem mem = org.openide.filesystems.FileUtil.createMemoryFileSystem();
            OutputStream os = mem.getRoot().createData("whatever").getOutputStream();
            os.write("hello".getBytes());
            os.close();
            setDelegates(mem);
        }
    }

    public void testMasks() throws Exception {
        // XXX does not fail even when masks do not work in real apps, why?
        new OSGiProcess(getWorkDir()).
                newModule().sourceFile("m1/layer.xml", "<filesystem><folder name='Menu'>",
                "<folder name='original'><file name='something'/></folder>",
                "</folder></filesystem>").manifest(
                "OpenIDE-Module: m1",
                "OpenIDE-Module-Layer: m1/layer.xml"
                ).done().
                newModule().clazz(MasksInstall.class).sourceFile("m2/layer.xml", "<filesystem><folder name='Menu'>",
                "<file name='original_hidden'/>",
                "<folder name='substitute'/>",
                "</folder></filesystem>").manifest(
                "OpenIDE-Module: m2",
                "OpenIDE-Module-Install: " + MasksInstall.class.getName(),
                "OpenIDE-Module-Layer: m2/layer.xml",
                "OpenIDE-Module-Module-Dependencies: org.openide.modules, org.openide.filesystems, m1").done().backwards().run(false);
        assertEquals("false", System.getProperty("original.visible"));
        assertEquals("true", System.getProperty("substitute.visible"));
        assertEquals("false", System.getProperty("mask.visible"));
    }
    public static class MasksInstall extends ModuleInstall {
        public @Override void restored() {
            System.setProperty("original.visible", Boolean.toString(FileUtil.getConfigFile("Menu/original") != null));
            System.setProperty("mask.visible", Boolean.toString(FileUtil.getConfigFile("Menu/original_hidden") != null));
            System.setProperty("substitute.visible", Boolean.toString(FileUtil.getConfigFile("Menu/substitute") != null));
        }
    }

    public void testBrandingLayers() throws Exception {
        System.setProperty("branding.token", "custom");
        new OSGiProcess(getWorkDir()).
                newModule().sourceFile("m1/layer.xml", "<filesystem><folder name='Menu'>",
                "<folder name='Help'/>",
                "</folder></filesystem>").sourceFile("m1/layer_custom.xml", "<filesystem><folder name='Menu'>",
                "<file name='Help_hidden'/>",
                "</folder></filesystem>").manifest(
                "OpenIDE-Module: m1",
                "OpenIDE-Module-Layer: m1/layer.xml",
                "OpenIDE-Module-Install: " + BrandingLayersInstall.class.getName()
                ).clazz(BrandingLayersInstall.class).done().run(false);
        assertEquals("true", System.getProperty("branded.out"));
    }
    public static class BrandingLayersInstall extends ModuleInstall {
        public @Override void restored() {
            System.setProperty("branded.out", Boolean.toString(FileUtil.getConfigFile("Menu/Help") == null));
        }
    }

    public void testURLs() throws Exception {
        new OSGiProcess(getWorkDir()).newModule().clazz(URLsInstall.class).sourceFile("custom/layer.xml", "<filesystem>",
                "<file name='hello'>world</file>",
                "</filesystem>").manifest(
                "OpenIDE-Module: custom",
                "OpenIDE-Module-Install: " + URLsInstall.class.getName(),
                "OpenIDE-Module-Layer: custom/layer.xml",
                "OpenIDE-Module-Module-Dependencies: org.openide.modules, org.openide.filesystems").done().run(false);
        assertEquals("5", System.getProperty("hello.contents.length"));
    }
    public static class URLsInstall extends ModuleInstall {
        public @Override void restored() {
            try {
                System.setProperty("hello.contents.length", Integer.toString(FileUtil.getConfigFile("hello").toURL().openConnection().getContentLength()));
            } catch (IOException x) {
                System.setProperty("hello.contents.length", x.toString());
            }
        }
    }

}
