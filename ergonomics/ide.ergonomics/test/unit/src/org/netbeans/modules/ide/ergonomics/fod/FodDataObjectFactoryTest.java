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
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.logging.Level;
import junit.framework.Test;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.cookies.EditCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class FodDataObjectFactoryTest extends NbTestCase {
    private static final InstanceContent ic = new InstanceContent();

    static {
        FeatureManager.assignFeatureTypesLookup(new AbstractLookup(ic));
    }
    private ModuleInfo au;
    private ModuleInfo fav;

    public FodDataObjectFactoryTest(String n) {
        super(n);
    }

    public static Test suite() {
        System.setProperty("org.netbeans.core.startup.level", "OFF");

        return NbModuleSuite.create(
            NbModuleSuite.emptyConfiguration().
            addTest(FodDataObjectFactoryTest.class).
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
        for (ModuleInfo info : Lookup.getDefault().lookupAll(ModuleInfo.class)) {
            boolean disable = false;
            if (
                info.getCodeNameBase().equals("org.netbeans.modules.subversion")
            ) {
                disable = true;
                au = info;
            }
            if (
                info.getCodeNameBase().equals("org.netbeans.modules.favorites")
            ) {
                disable = true;
                fav = info;
            }
            if (disable) {
             Method m = null;
                Class<?> c = info.getClass();
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
                m.invoke(info, false);
                assertFalse("Module is disabled", info.isEnabled());
                found++;
            }
            sb.append(info.getCodeNameBase()).append('\n');
        }
        if (found != 2) {
            fail("Two shall be found, was " + found + ":\n" + sb);
        }
        FeatureInfo info = FeatureInfo.create(
            "TestFactory",
            ParseXMLContentTest.class.getResource("FeatureInfoTest.xml"),
            ParseXMLContentTest.class.getResource("TestBundle.properties")
        );
        ic.add(info);
        FoDLayersProvider.getInstance().refreshForce();
    }



    @RandomlyFails // ergonomics #3485
    public void testCreateRecognize() throws Exception {
        assertFalse("Autoupdate is disabled", au.isEnabled());
        FileUtil.setMIMEType("huh", "text/x-huh");
        FileObject fo = FileUtil.createData(FileUtil.getConfigRoot(), "test/my.huh");
        DataObject obj = DataObject.find(fo);
        FileObject fo2 = FileUtil.createData(FileUtil.getConfigRoot(), "test/subdir/my.huh");
        DataObject obj2 = DataObject.find(fo2);
        CharSequence log = Log.enable("org.openide.loaders", Level.WARNING);
        OpenCookie oc = obj.getLookup().lookup(OpenCookie.class);
        assertNotNull("Open cookie found", oc);
        assertEquals("Cookie is OK too", oc, obj.getCookie(OpenCookie.class));
        assertEquals("Node is OK too", oc, obj.getNodeDelegate().getCookie(OpenCookie.class));
        assertEquals("Node lookup is OK too", oc, obj.getNodeDelegate().getLookup().lookup(OpenCookie.class));
        assertTrue("It is our cookie: " + oc, oc.getClass().getName().contains("ergonomics"));
        assertEquals("No warnings: " + log, 0, log.length());

        EditCookie ec = obj.getLookup().lookup(EditCookie.class);
        assertEquals("Edit cookie is available and same as open one", oc, ec);
        ec.edit();
        assertTrue("Autoupdate is enabled", au.isEnabled());
        for (int i = 0; ; i++) {
            DataObject newObj = DataObject.find(fo);
            if (obj == newObj) {
                if (i < 50) {
                    Thread.sleep(1000);
                    continue;
                }
                fail("New object shall be created: " + newObj);
            }
            break;
        }
        assertFalse("Old is no longer valid", obj.isValid());

        DataObject newObj2 = DataObject.find(fo2);
        if (obj2 == newObj2) {
            fail("New object shall be created for all objects: " + newObj2);
        }

        DataObject folder = FodDataObjectFactory.create(fo).findDataObject(fo.getParent(), new HashSet<FileObject>());
        assertNull("Folders are not recognized", folder);
    }

}
