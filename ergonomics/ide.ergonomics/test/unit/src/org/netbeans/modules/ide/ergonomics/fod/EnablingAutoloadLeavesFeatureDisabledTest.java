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

package org.netbeans.modules.ide.ergonomics.fod;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Collections;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.logging.Level;
import junit.framework.Test;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class EnablingAutoloadLeavesFeatureDisabledTest extends NbTestCase {
    private static final InstanceContent ic = new InstanceContent();

    static {
        FeatureManager.assignFeatureTypesLookup(new AbstractLookup(ic));
    }
    private ModuleInfo subversion;
    private ModuleInfo services;
    private FeatureInfo info;

    public EnablingAutoloadLeavesFeatureDisabledTest(String n) {
        super(n);
    }

    public static Test suite() {
        System.setProperty("org.netbeans.core.startup.level", "OFF");

        return NbModuleSuite.create(
            NbModuleSuite.emptyConfiguration().
            addTest(EnablingAutoloadLeavesFeatureDisabledTest.class).
            gui(false)
        );
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();

        ic.set(Collections.emptyList(), null);

        URI uri = ModuleInfo.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        File jar = new File(uri);
        System.setProperty("netbeans.home", jar.getParentFile().getParent());
        System.setProperty("netbeans.user", getWorkDirPath());
        StringBuffer sb = new StringBuffer();
        int found = 0;
        Exception ex2 = null;
        for (ModuleInfo mi : Lookup.getDefault().lookupAll(ModuleInfo.class)) {
            boolean disable = false;
            if (
                mi.getCodeNameBase().equals("org.netbeans.modules.autoupdate.services")
            ) {
                disable = true;
                services = mi;
            }
            if (
                mi.getCodeNameBase().equals("org.netbeans.modules.subversion")
            ) {
                disable = true;
                subversion = mi;
            }
            if (disable) {
             Method m = null;
                Class<?> c = mi.getClass();
                for (;;) {
                    if (c == null) {
                        throw ex2;
                    }
                    try {
                        m = c.getDeclaredMethod("setEnabled", Boolean.TYPE);
                    } catch (Exception ex) {
                        ex2 = ex;
                    }
                    if (m != null) {
                        break;
                    }
                    c = c.getSuperclass();
                }
                m.setAccessible(true);
                m.invoke(mi, false);
                assertFalse("Module is disabled", mi.isEnabled());
                found++;
            }
            sb.append(mi.getCodeNameBase()).append('\n');
        }
        if (found != 2) {
            fail("Two shall be found, was " + found + ":\n" + sb);
        }

        info = FeatureInfo.create(
            "TestFactory",
            ParseXMLContentTest.class.getResource("FeatureInfoTest.xml"),
            ParseXMLContentTest.class.getResource("TestBundle4.properties")
        );
        ic.add(info);
        FoDLayersProvider.getInstance().refreshForce();
    }



    public void testFeatureRemainsDisabled() throws Exception {
        assertFalse("Subversion is disabled", subversion.isEnabled());
        assertFalse("AU services is disabled", services.isEnabled());

        assertFalse("Feature reports itself disabled", info.isEnabled());

        ModuleManager man = org.netbeans.core.startup.Main.getModuleSystem().getManager();
        try {
            man.mutexPrivileged().enterWriteAccess();
            man.enable((Module)services);
        } finally {
            man.mutexPrivileged().exitWriteAccess();
        }

        assertTrue("AU services is active too", services.isEnabled());
        assertFalse("Feature still reports itself disabled", info.isEnabled());

        try {
            man.mutexPrivileged().enterWriteAccess();
            man.enable((Module)subversion);
        } finally {
            man.mutexPrivileged().exitWriteAccess();
        }
        assertTrue("Subversion is active", subversion.isEnabled());

        assertTrue("Feature is enabled now", info.isEnabled());

    }
}
