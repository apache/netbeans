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
package org.netbeans.modules.cnd.makeproject.api.configurations;

import java.io.File;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.test.CndBaseTestCase;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.openide.util.Utilities;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;

/**
 */
public class QmakeConfigurationTest extends CndBaseTestCase {

    private static final int[] QT_CONF_TYPES = {
        MakeConfiguration.TYPE_QT_APPLICATION,
        MakeConfiguration.TYPE_QT_DYNAMIC_LIB,
        MakeConfiguration.TYPE_QT_STATIC_LIB
    };

    public QmakeConfigurationTest(String name) {
        super(name);
    }
    
    @Override
    protected boolean addEditorSupport() {
        return false;
    }

    @Override @Before
    public void setUp() throws Exception {
        super.setUp();
        System.setProperty("org.netbeans.modules.cnd.makeproject.api.runprofiles", "true");
        ServerList.setDefaultRecord(ServerList.get(ExecutionEnvironmentFactory.getLocal()));
    }

    private static QmakeConfiguration newQmakeConfiguration(int confType) {
        File dir = new File(System.getProperty("java.io.tmpdir"), "QmakeConfigurationTest");
        MakeConfiguration conf = MakeConfiguration.createConfiguration(new FSPath(CndFileUtils.getLocalFileSystem(), dir.getPath()), "Dummy", confType, null, HostInfoUtils.LOCALHOST);
        //++ trace for #194772 -  QmakeConfigurationTest fails on Windows and Mac
        Platform platform = Platforms.getPlatform(conf.getDevelopmentHost().getBuildPlatform());
        System.out.println("Creating QmakeConfiguration for platform " + platform.getDisplayName());        
        System.out.println("platform.getQtLibraryName returned " + platform.getQtLibraryName("my-qt-lib", "1"));
        //-- trace for #194772 -  QmakeConfigurationTest fails on Windows and Mac
        return new QmakeConfiguration(conf);
    }

    @Test
    public void testDefaults() {
        for (int conftype : QT_CONF_TYPES) {
            QmakeConfiguration qconf = newQmakeConfiguration(conftype);
            assertEquals("", qconf.getDestdir().getValue());
            assertEquals("", qconf.getTarget().getValue());
            assertEquals("1.0.0", qconf.getVersion().getValue());
            assertEquals("debug ", qconf.getBuildMode().getOption());
            assertTrue(qconf.isCoreEnabled().getValue());
            assertTrue(qconf.isGuiEnabled().getValue());
            assertFalse(qconf.isNetworkEnabled().getValue());
            assertFalse(qconf.isOpenglEnabled().getValue());
            assertFalse(qconf.isPhononEnabled().getValue());
            assertFalse(qconf.isQt3SupportEnabled().getValue());
            assertFalse(qconf.isSqlEnabled().getValue());
            assertFalse(qconf.isSvgEnabled().getValue());
            assertFalse(qconf.isWebkitEnabled().getValue());
            assertFalse(qconf.isXmlEnabled().getValue());
            assertEquals("", qconf.getMocDir().getValue());
            assertEquals("", qconf.getRccDir().getValue());
            assertEquals("", qconf.getUiDir().getValue());
            assertEquals(Collections.emptyList(), qconf.getCustomDefs().getValue());
            assertEquals("", qconf.getQmakeSpec().getValue());
        }
    }

    @Test
    public void testGetDestdir() {
        for (int conftype : QT_CONF_TYPES) {
            QmakeConfiguration qconf = newQmakeConfiguration(conftype);
            assertEquals("", qconf.getDestdir().getValue());
            assertEquals(MakeConfiguration.CND_DISTDIR_MACRO+"/"+MakeConfiguration.CND_CONF_MACRO+"/"+MakeConfiguration.CND_PLATFORM_MACRO, qconf.getDestdirValue());
            qconf.getDestdir().setValue(".");
            assertEquals(".", qconf.getDestdirValue());
        }
    }

    @Test
    public void testGetTarget() {
        for (int conftype : QT_CONF_TYPES) {
            QmakeConfiguration qconf = newQmakeConfiguration(conftype);
            assertEquals("", qconf.getTarget().getValue());
            assertEquals("QmakeConfigurationTest", qconf.getTargetValue());
            qconf.getTarget().setValue("NotSoDummy");
            assertEquals("NotSoDummy", qconf.getTargetValue());
        }
    }

    @Test
    public void testGetOutputValueApp() {
        QmakeConfiguration qconf = newQmakeConfiguration(MakeConfiguration.TYPE_QT_APPLICATION);
        assertEquals(MakeConfiguration.CND_DISTDIR_MACRO+"/"+MakeConfiguration.CND_CONF_MACRO+"/"+MakeConfiguration.CND_PLATFORM_MACRO+"/QmakeConfigurationTest", qconf.getOutputValue());
        qconf.getDestdir().setValue(".");
        assertEquals("./QmakeConfigurationTest", qconf.getOutputValue());
        qconf.getTarget().setValue("Dummy");
        assertEquals("./Dummy", qconf.getOutputValue());
        qconf.getDestdir().reset();
        assertEquals(MakeConfiguration.CND_DISTDIR_MACRO+"/"+MakeConfiguration.CND_CONF_MACRO+"/"+MakeConfiguration.CND_PLATFORM_MACRO+"/Dummy", qconf.getOutputValue());
    }

    @Test
    public void testGetOutputValueDynamicLib() {
        QmakeConfiguration qconf = newQmakeConfiguration(MakeConfiguration.TYPE_QT_DYNAMIC_LIB);
        if (Utilities.isWindows()) {
            assertEquals(MakeConfiguration.CND_DISTDIR_MACRO+"/"+MakeConfiguration.CND_CONF_MACRO+"/"+MakeConfiguration.CND_PLATFORM_MACRO+"/QmakeConfigurationTest1.dll", qconf.getOutputValue());
            qconf.getVersion().setValue("2.0.0");
            assertEquals(MakeConfiguration.CND_DISTDIR_MACRO+"/"+MakeConfiguration.CND_CONF_MACRO+"/"+MakeConfiguration.CND_PLATFORM_MACRO+"/QmakeConfigurationTest2.dll", qconf.getOutputValue());
            qconf.getDestdir().setValue(".");
            assertEquals("./QmakeConfigurationTest2.dll", qconf.getOutputValue());
            qconf.getTarget().setValue("Dummy");
            assertEquals("./Dummy2.dll", qconf.getOutputValue());
            qconf.getDestdir().reset();
            assertEquals(MakeConfiguration.CND_DISTDIR_MACRO+"/"+MakeConfiguration.CND_CONF_MACRO+"/"+MakeConfiguration.CND_PLATFORM_MACRO+"/Dummy2.dll", qconf.getOutputValue());
        } else if (Utilities.isMac()) {
            assertEquals(MakeConfiguration.CND_DISTDIR_MACRO+"/"+MakeConfiguration.CND_CONF_MACRO+"/"+MakeConfiguration.CND_PLATFORM_MACRO+"/libQmakeConfigurationTest.1.0.0.dylib", qconf.getOutputValue());
            qconf.getVersion().setValue("2.3.4");
            assertEquals(MakeConfiguration.CND_DISTDIR_MACRO+"/"+MakeConfiguration.CND_CONF_MACRO+"/"+MakeConfiguration.CND_PLATFORM_MACRO+"/libQmakeConfigurationTest.2.3.4.dylib", qconf.getOutputValue());
            qconf.getDestdir().setValue(".");
            assertEquals("./libQmakeConfigurationTest.2.3.4.dylib", qconf.getOutputValue());
            qconf.getTarget().setValue("Dummy");
            assertEquals("./libDummy.2.3.4.dylib", qconf.getOutputValue());
            qconf.getDestdir().reset();
            assertEquals(MakeConfiguration.CND_DISTDIR_MACRO+"/"+MakeConfiguration.CND_CONF_MACRO+"/"+MakeConfiguration.CND_PLATFORM_MACRO+"/libDummy.2.3.4.dylib", qconf.getOutputValue());
        } else if (Utilities.isUnix()) {
            assertEquals(MakeConfiguration.CND_DISTDIR_MACRO+"/"+MakeConfiguration.CND_CONF_MACRO+"/"+MakeConfiguration.CND_PLATFORM_MACRO+"/libQmakeConfigurationTest.so.1.0.0", qconf.getOutputValue());
            qconf.getVersion().setValue("2.3.4");
            assertEquals(MakeConfiguration.CND_DISTDIR_MACRO+"/"+MakeConfiguration.CND_CONF_MACRO+"/"+MakeConfiguration.CND_PLATFORM_MACRO+"/libQmakeConfigurationTest.so.2.3.4", qconf.getOutputValue());
            qconf.getDestdir().setValue(".");
            assertEquals("./libQmakeConfigurationTest.so.2.3.4", qconf.getOutputValue());
            qconf.getTarget().setValue("Dummy");
            assertEquals("./libDummy.so.2.3.4", qconf.getOutputValue());
            qconf.getDestdir().reset();
            assertEquals(MakeConfiguration.CND_DISTDIR_MACRO+"/"+MakeConfiguration.CND_CONF_MACRO+"/"+MakeConfiguration.CND_PLATFORM_MACRO+"/libDummy.so.2.3.4", qconf.getOutputValue());
        } else {
            System.err.println("OS not recognized. Skipping test");
        }
    }

    @Test
    public void testGetOutputValueStaticLib() {
        QmakeConfiguration qconf = newQmakeConfiguration(MakeConfiguration.TYPE_QT_STATIC_LIB);
        assertEquals(MakeConfiguration.CND_DISTDIR_MACRO+"/"+MakeConfiguration.CND_CONF_MACRO+"/"+MakeConfiguration.CND_PLATFORM_MACRO+"/libQmakeConfigurationTest.a", qconf.getOutputValue());
        qconf.getDestdir().setValue(".");
        assertEquals("./libQmakeConfigurationTest.a", qconf.getOutputValue());
        qconf.getTarget().setValue("Dummy");
        assertEquals("./libDummy.a", qconf.getOutputValue());
        qconf.getDestdir().reset();
        assertEquals(MakeConfiguration.CND_DISTDIR_MACRO+"/"+MakeConfiguration.CND_CONF_MACRO+"/"+MakeConfiguration.CND_PLATFORM_MACRO+"/libDummy.a", qconf.getOutputValue());
    }

    @Test
    public void testGetEnabledModules() {
        for (int conftype : QT_CONF_TYPES) {
            QmakeConfiguration qconf = newQmakeConfiguration(conftype);
            assertEquals("core gui widgets", qconf.getEnabledModules());
            qconf.isCoreEnabled().setValue(false);
            qconf.isGuiEnabled().setValue(false);
            qconf.isWidgetsEnabled().setValue(false);
            assertEquals("", qconf.getEnabledModules());
            qconf.isCoreEnabled().setValue(false);
            qconf.isGuiEnabled().setValue(true);
            qconf.isNetworkEnabled().setValue(false);
            qconf.isOpenglEnabled().setValue(true);
            qconf.isPhononEnabled().setValue(false);
            qconf.isQt3SupportEnabled().setValue(true);
            qconf.isSqlEnabled().setValue(false);
            qconf.isSvgEnabled().setValue(true);
            qconf.isWebkitEnabled().setValue(false);
            qconf.isXmlEnabled().setValue(true);
            assertEquals("gui opengl qt3support svg xml", qconf.getEnabledModules());
        }
    }

    @Test
    public void testSetEnabledModules() {
        for (int conftype : QT_CONF_TYPES) {
            QmakeConfiguration qconf = newQmakeConfiguration(conftype);
            qconf.setEnabledModules("");
            assertFalse(qconf.isCoreEnabled().getValue());
            assertFalse(qconf.isGuiEnabled().getValue());
            assertFalse(qconf.isNetworkEnabled().getValue());
            assertFalse(qconf.isOpenglEnabled().getValue());
            assertFalse(qconf.isPhononEnabled().getValue());
            assertFalse(qconf.isQt3SupportEnabled().getValue());
            assertFalse(qconf.isSqlEnabled().getValue());
            assertFalse(qconf.isSvgEnabled().getValue());
            assertFalse(qconf.isWebkitEnabled().getValue());
            assertFalse(qconf.isXmlEnabled().getValue());
            qconf.setEnabledModules("gui opengl\tqt3support\rsvg  \n   xml");
            assertFalse(qconf.isCoreEnabled().getValue());
            assertTrue(qconf.isGuiEnabled().getValue());
            assertFalse(qconf.isNetworkEnabled().getValue());
            assertTrue(qconf.isOpenglEnabled().getValue());
            assertFalse(qconf.isPhononEnabled().getValue());
            assertTrue(qconf.isQt3SupportEnabled().getValue());
            assertFalse(qconf.isSqlEnabled().getValue());
            assertTrue(qconf.isSvgEnabled().getValue());
            assertFalse(qconf.isWebkitEnabled().getValue());
            assertTrue(qconf.isXmlEnabled().getValue());
        }
    }

    private void setNonStandardProperties(QmakeConfiguration qconf) {
        qconf.getDestdir().setValue("destdir");
        qconf.getTarget().setValue("target");
        qconf.getVersion().setValue("2.0.0");
        qconf.getBuildMode().setValue("Release");
        qconf.isCoreEnabled().setValue(false);
        qconf.isGuiEnabled().setValue(false);
        qconf.isNetworkEnabled().setValue(true);
        qconf.isOpenglEnabled().setValue(true);
        qconf.isPhononEnabled().setValue(true);
        qconf.isQt3SupportEnabled().setValue(true);
        qconf.isSqlEnabled().setValue(true);
        qconf.isSvgEnabled().setValue(true);
        qconf.isWebkitEnabled().setValue(true);
        qconf.isXmlEnabled().setValue(true);
        qconf.getMocDir().setValue("mocdir");
        qconf.getRccDir().setValue("rccdir");
        qconf.getUiDir().setValue("uidir");
        qconf.getQmakeSpec().setValue("solaris-cc-64");
        qconf.getCustomDefs().add("ZZZ=YYY");
    }

    private void assertNotSameButEquals(QmakeConfiguration qconf, QmakeConfiguration qconf2) {
        assertNotSame(qconf, qconf2);
        assertNotSame(qconf.getDestdir(), qconf2.getDestdir());
        assertEquals(qconf.getDestdir().getValue(), qconf2.getDestdir().getValue());
        assertNotSame(qconf.getTarget(), qconf2.getTarget());
        assertEquals(qconf.getTarget().getValue(), qconf2.getTarget().getValue());
        assertNotSame(qconf.getVersion(), qconf2.getVersion());
        assertEquals(qconf.getVersion().getValue(), qconf2.getVersion().getValue());
        assertNotSame(qconf.getBuildMode(), qconf2.getBuildMode());
        assertEquals(qconf.getBuildMode().getValue(), qconf2.getBuildMode().getValue());
        assertNotSame(qconf.isCoreEnabled(), qconf2.isCoreEnabled());
        assertEquals(qconf.isCoreEnabled().getValue(), qconf2.isCoreEnabled().getValue());
        assertNotSame(qconf.isGuiEnabled(), qconf2.isGuiEnabled());
        assertEquals(qconf.isGuiEnabled().getValue(), qconf2.isGuiEnabled().getValue());
        assertNotSame(qconf.isNetworkEnabled(), qconf2.isNetworkEnabled());
        assertEquals(qconf.isNetworkEnabled().getValue(), qconf2.isNetworkEnabled().getValue());
        assertNotSame(qconf.isOpenglEnabled(), qconf2.isOpenglEnabled());
        assertEquals(qconf.isOpenglEnabled().getValue(), qconf2.isOpenglEnabled().getValue());
        assertNotSame(qconf.isPhononEnabled(), qconf2.isPhononEnabled());
        assertEquals(qconf.isPhononEnabled().getValue(), qconf2.isPhononEnabled().getValue());
        assertNotSame(qconf.isQt3SupportEnabled(), qconf2.isQt3SupportEnabled());
        assertEquals(qconf.isQt3SupportEnabled().getValue(), qconf2.isQt3SupportEnabled().getValue());
        assertNotSame(qconf.isSqlEnabled(), qconf2.isSqlEnabled());
        assertEquals(qconf.isSqlEnabled().getValue(), qconf2.isSqlEnabled().getValue());
        assertNotSame(qconf.isSvgEnabled(), qconf2.isSvgEnabled());
        assertEquals(qconf.isSvgEnabled().getValue(), qconf2.isSvgEnabled().getValue());
        assertNotSame(qconf.isWebkitEnabled(), qconf2.isWebkitEnabled());
        assertEquals(qconf.isWebkitEnabled().getValue(), qconf2.isWebkitEnabled().getValue());
        assertNotSame(qconf.isXmlEnabled(), qconf2.isXmlEnabled());
        assertEquals(qconf.isXmlEnabled().getValue(), qconf2.isXmlEnabled().getValue());
        assertNotSame(qconf.getMocDir(), qconf2.getMocDir());
        assertEquals(qconf.getMocDir().getValue(), qconf2.getMocDir().getValue());
        assertNotSame(qconf.getRccDir(), qconf2.getRccDir());
        assertEquals(qconf.getRccDir().getValue(), qconf2.getRccDir().getValue());
        assertNotSame(qconf.getUiDir(), qconf2.getUiDir());
        assertEquals(qconf.getUiDir().getValue(), qconf2.getUiDir().getValue());
        assertNotSame(qconf.getQmakeSpec(), qconf2.getQmakeSpec());
        assertEquals(qconf.getQmakeSpec().getValue(), qconf2.getQmakeSpec().getValue());
        assertNotSame(qconf.getCustomDefs(), qconf2.getCustomDefs());
        assertEquals(qconf.getCustomDefs().getValue(), qconf2.getCustomDefs().getValue());
    }

    @Test
    public void testAssign() {
        for (int conftype : QT_CONF_TYPES) {
            QmakeConfiguration qconf = newQmakeConfiguration(conftype);
            QmakeConfiguration qconf2 = newQmakeConfiguration(conftype);
            setNonStandardProperties(qconf2);
            qconf.assign(qconf2);
            assertNotSameButEquals(qconf2, qconf);
        }
    }

    @Test
    public void testClone() {
        for (int conftype : QT_CONF_TYPES) {
            QmakeConfiguration qconf = newQmakeConfiguration(conftype);
            setNonStandardProperties(qconf);
            QmakeConfiguration qconf2 = qconf.clone();
            assertNotSameButEquals(qconf, qconf2);
        }
    }

    @Test
    public void testCloneAndAssign() {
        for (int conftype : QT_CONF_TYPES) {
            QmakeConfiguration qconf = newQmakeConfiguration(conftype);
            QmakeConfiguration qconf2 = qconf.clone();
            assertNotSameButEquals(qconf, qconf2);
            setNonStandardProperties(qconf2);
            qconf.assign(qconf2);
            assertNotSameButEquals(qconf2, qconf);
        }
    }
}
