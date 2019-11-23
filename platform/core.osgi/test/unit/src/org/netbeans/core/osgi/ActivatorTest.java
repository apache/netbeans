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

package org.netbeans.core.osgi;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import javax.swing.JOptionPane;
import javax.swing.plaf.basic.BasicOptionPaneUI;
import org.netbeans.junit.NbTestCase;
import org.openide.modules.ModuleInstall;

public class ActivatorTest extends NbTestCase {

    public ActivatorTest(String n) {
        super(n);
    }

    protected @Override void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    }

    public void testModuleInstall() throws Exception {
        new OSGiProcess(getWorkDir()).newModule().clazz(ModuleInstallInstall.class).manifest(
                "OpenIDE-Module: custom",
                "OpenIDE-Module-Install: " + ModuleInstallInstall.class.getName(),
                "OpenIDE-Module-Module-Dependencies: org.openide.modules").done().run(false);
        assertTrue(Boolean.getBoolean("my.bundle.ran"));
    }
    public static class ModuleInstallInstall extends ModuleInstall {
        public @Override void restored() {
            System.setProperty("my.bundle.ran", "true");
        }
    }

    public void testModuleInstallBackwards() throws Exception {
        new OSGiProcess(getWorkDir()).newModule().clazz(ModuleInstallBackwardsInstall.class).manifest(
                "OpenIDE-Module: custom",
                "OpenIDE-Module-Install: " + ModuleInstallBackwardsInstall.class.getName(),
                "OpenIDE-Module-Module-Dependencies: org.openide.modules").done().backwards().run(false);
        assertTrue(Boolean.getBoolean("my.bundle.ran.again"));
    }
    public static class ModuleInstallBackwardsInstall extends ModuleInstall {
        public @Override void restored() {
            System.setProperty("my.bundle.ran.again", "true");
        }
    }

    public void testOnStartStop() throws Exception {
        new OSGiProcess(getWorkDir()).newModule().
                clazz(OnStartImpl.class).namedservice("Modules/Start", Runnable.class, OnStartImpl.class).
                clazz(OnStopImpl1.class).namedservice("Modules/Stop", Callable.class, OnStopImpl1.class).
                clazz(OnStopImpl2.class).namedservice("Modules/Stop", Runnable.class, OnStopImpl2.class).
                manifest(
                "OpenIDE-Module: custom",
                "OpenIDE-Module-Module-Dependencies: org.openide.modules").done().run(true);
        assertTrue(Boolean.getBoolean("my.bundle.started"));
        assertTrue(Boolean.getBoolean("my.bundle.stopping"));
        assertTrue(Boolean.getBoolean("my.bundle.stopped"));
    }
    public static class OnStartImpl implements Runnable {
        @Override public void run() {
            System.setProperty("my.bundle.started", "true");
        }
    }
    public static class OnStopImpl1 implements Callable<Boolean> {
        @Override public Boolean call() {
            System.setProperty("my.bundle.stopping", "true");
            return true; // Activator cannot do anything if we return false anyway!
        }
    }
    public static class OnStopImpl2 implements Runnable {
        @Override public void run() {
            System.setProperty("my.bundle.stopped", "true");
        }
    }

    public void testURLStreamHandler() throws Exception {
        new OSGiProcess(getWorkDir()).newModule().clazz(URLStreamHandlerInstall.class).sourceFile("custom/stuff", "some text").manifest(
                "OpenIDE-Module: custom",
                "OpenIDE-Module-Install: " + URLStreamHandlerInstall.class.getName(),
                "OpenIDE-Module-Module-Dependencies: org.openide.modules").done().
                backwards(). // XXX will not pass otherwise
                run(false);
        assertEquals("10", System.getProperty("my.url.length"));
    }
    public static class URLStreamHandlerInstall extends ModuleInstall {
        public @Override void restored() {
            try {
                System.setProperty("my.url.length",
                        Integer.toString(new URL("nbres:/custom/stuff").openConnection().getContentLength()));
            } catch (IOException x) {
                x.printStackTrace();
            }
        }
    }

    public void testJREPackageImport() throws Exception {
        new OSGiProcess(getWorkDir()).newModule().clazz(JREPackageImportInstall.class).manifest(
                "OpenIDE-Module: custom",
                "OpenIDE-Module-Install: " + JREPackageImportInstall.class.getName(),
                "OpenIDE-Module-Module-Dependencies: org.openide.modules").done().run(false);
        assertTrue(Boolean.getBoolean("my.bundle.worked"));
    }
    public static class JREPackageImportInstall extends ModuleInstall {
        public @Override void restored() {
            // javax.swing.plaf.OptionPaneUI mentioned in method signature:
            new JOptionPane().setUI(new BasicOptionPaneUI());
            System.setProperty("my.bundle.worked", "true");
        }
    }

    public void testProvidesRequiresNeedsParsing() throws Exception {
        Hashtable<String,String> headers = new Hashtable<String,String>();
        assertEquals(Collections.emptySet(), Activator.provides(headers));
        assertEquals(Collections.emptySet(), Activator.requires(headers));
        assertEquals(Collections.emptySet(), Activator.needs(headers));
        headers.put("Bundle-SymbolicName", "org.netbeans.modules.projectui");
        headers.put("OpenIDE-Module-Provides", "org.netbeans.modules.project.uiapi.ActionsFactory,   " +
                "org.netbeans.modules.project.uiapi.OpenProjectsTrampoline,  org.netbeans.modules.project.uiapi.ProjectChooserFactory");
        assertEquals(new TreeSet<String>(Arrays.asList(
                "cnb.org.netbeans.modules.projectui",
                "org.netbeans.modules.project.uiapi.ActionsFactory",
                "org.netbeans.modules.project.uiapi.OpenProjectsTrampoline",
                "org.netbeans.modules.project.uiapi.ProjectChooserFactory"
                )), Activator.provides(headers));
        assertEquals(Collections.emptySet(), Activator.requires(headers));
        assertEquals(Collections.emptySet(), Activator.needs(headers));
        headers.clear();
        headers.put("Require-Bundle", "org.netbeans.api.progress;bundle-version=\"[101.0.0,200)\", " +
                "org.netbeans.spi.quicksearch;bundle-version=\"[1.0.0,100)\"");
        headers.put("OpenIDE-Module-Requires", "org.openide.modules.InstalledFileLocator");
        assertEquals(Collections.emptySet(), Activator.provides(headers));
        assertEquals(new TreeSet<String>(Arrays.asList(
                "cnb.org.netbeans.api.progress",
                "cnb.org.netbeans.spi.quicksearch",
                "org.openide.modules.InstalledFileLocator"
                )), Activator.requires(headers));
        assertEquals(Collections.emptySet(), Activator.needs(headers));
        headers.clear();
        headers.put("OpenIDE-Module-Needs", "org.netbeans.modules.java.preprocessorbridge.spi.JavaSourceUtilImpl");
        assertEquals(Collections.emptySet(), Activator.provides(headers));
        assertEquals(Collections.emptySet(), Activator.requires(headers));
        assertEquals(Collections.singleton("org.netbeans.modules.java.preprocessorbridge.spi.JavaSourceUtilImpl"), Activator.needs(headers));
        headers.clear();
        String os = System.getProperty("os.name");
        System.setProperty("os.name", "Windows 2000");
        try {
            headers.put("Bundle-SymbolicName", "org.openide.modules");
            final TreeSet<String> export = new TreeSet<String>(Arrays.asList(
                "cnb.org.openide.modules",
                "org.openide.modules.os.Windows"
            ));
            if (isJavaFX()) {
                export.add("org.openide.modules.jre.JavaFX");
            }
            assertEquals(export, Activator.provides(headers));
            assertEquals(Collections.emptySet(), Activator.requires(headers));
            assertEquals(Collections.emptySet(), Activator.needs(headers));
        } finally {
            System.setProperty("os.name", os);
        }
    }
    
    private static boolean isJavaFX() {
        File jdk = new File(System.getProperty("java.home"));
        File lib = new File(jdk, "lib");
        File ext = new File(lib, "ext");
        File jdk7 = new File(lib, "jfxrt.jar");
        File jdk8 = new File(ext, "jfxrt.jar");
        return jdk7.exists() || jdk8.exists();
    }

    public void testRequireToken() throws Exception {
        new OSGiProcess(getWorkDir()).
                newModule().manifest(
                "OpenIDE-Module: zz.api",
                "OpenIDE-Module-Public-Packages: api.*",
                "OpenIDE-Module-Needs: api.Interface").
                sourceFile("api/Interface.java", "package api;",
                "public interface Interface {}").done().
                newModule().manifest(
                "OpenIDE-Module: zz.impl",
                "OpenIDE-Module-Module-Dependencies: zz.api",
                "OpenIDE-Module-Provides: api.Interface").
                sourceFile("impl/Provider.java", "package impl;",
                "@org.openide.util.lookup.ServiceProvider(service=api.Interface.class)",
                "public class Provider implements api.Interface {}").done().
                newModule().manifest(
                "OpenIDE-Module: zz.client",
                "OpenIDE-Module-Install: client.Install",
                "OpenIDE-Module-Module-Dependencies: org.openide.modules, org.openide.util.lookup, zz.api").
                sourceFile("client/Install.java", "package client;",
                "public class Install extends org.openide.modules.ModuleInstall {",
                "public @Override void restored() {System.setProperty(\"provider.name\",",
                "org.openide.util.Lookup.getDefault().lookup(api.Interface.class).getClass().getName());}",
                "}").done().
                run(false);
        assertEquals("impl.Provider", System.getProperty("provider.name"));
    }

    public void testClassPathExtensions() throws Exception {
        new OSGiProcess(getWorkDir())
                .newModule()
                .sourceFile(
                    "custom/Install.java", 
                    "package custom;",
                    "public class Install extends org.openide.modules.ModuleInstall {",
                    "  public @Override void restored() {",
                    "    boolean jnaVersionRead = com.sun.jna.Native.VERSION != null;",
                    "    System.setProperty(\"used.jna\", Boolean.toString(jnaVersionRead));",
                    "  }",
                    "}")
                .manifest(
                    "OpenIDE-Module: custom",
                    "OpenIDE-Module-Install: custom.Install",
                    "OpenIDE-Module-Module-Dependencies: org.openide.modules, org.netbeans.libs.jna/2")
                .done()
                .module("org.netbeans.libs.jna")
                .run(false);
        assertTrue(Boolean.getBoolean("used.jna"));
    }

    public void testComSunPackages() throws Exception {
        new OSGiProcess(getWorkDir()).
                newModule().sourceFile("com/sun/java/swing/Painter.java", "package com.sun.java.swing;",
                "public interface Painter extends Runnable {}").
                manifest("OpenIDE-Module: painter", "OpenIDE-Module-Public-Packages: com.sun.java.swing.*").done().
                newModule().sourceFile("custom/Install.java", "package custom;",
                "public class Install extends org.openide.modules.ModuleInstall {",
                "public @Override void restored() {System.setProperty(\"com.sun.available\"," +
                "String.valueOf(Runnable.class.isAssignableFrom(com.sun.java.swing.Painter.class)));}",
                "}").manifest(
                "OpenIDE-Module: custom",
                "OpenIDE-Module-Install: custom.Install",
                "OpenIDE-Module-Module-Dependencies: org.openide.modules, painter").done().run(false);
        assertTrue(Boolean.getBoolean("com.sun.available"));
    }

}
