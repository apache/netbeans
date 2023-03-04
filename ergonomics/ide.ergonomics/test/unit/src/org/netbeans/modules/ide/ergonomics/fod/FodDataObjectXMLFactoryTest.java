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

import java.awt.event.ActionEvent;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Collections;
import java.util.logging.Level;
import javax.swing.Action;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.XMLDataObject;
import org.openide.modules.ModuleInfo;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class FodDataObjectXMLFactoryTest extends NbTestCase {
    private static final InstanceContent ic = new InstanceContent();

    static {
        FeatureManager.assignFeatureTypesLookup(new AbstractLookup(ic));
    }
    private ModuleInfo au;
    private ModuleInfo fav;

    public FodDataObjectXMLFactoryTest(String n) {
        super(n);
    }

    public static Test suite() {
        System.setProperty("org.netbeans.core.startup.level", "OFF");

        return NbModuleSuite.create(
            NbModuleSuite.emptyConfiguration().
            addTest(FodDataObjectXMLFactoryTest.class).
            gui(false)
        );
    }

    @Override
    protected boolean runInEQ() {
        return true;
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



    public void testCreateRecognize() throws Exception {
        assertFalse("Autoupdate is disabled", au.isEnabled());
        FileUtil.setMIMEType("huh", "text/huh+xml");
        FileObject fo = FileUtil.createData(FileUtil.getConfigRoot(), "test/my.huh");
        DataObject obj = DataObject.find(fo);
        FileObject fo2 = FileUtil.createData(FileUtil.getConfigRoot(), "test/my.xml");
        DataObject obj2 = DataObject.find(fo2);

        assertTrue("It is kind of XML", obj instanceof XMLDataObject);
        assertTrue("It is kind of XML2", obj2 instanceof XMLDataObject);

        Action[] arr = obj.getNodeDelegate().getActions(true);
        Action a = assertAction("Enabled in huh object", true, arr, obj.getLookup());
        assertAction("Disabled in real xml object", false, arr, obj2.getLookup());

        a.actionPerformed(new ActionEvent(this, 0, ""));

        
    }

    private static Action assertAction(String msg, boolean enabled, Action[] arr, Lookup context) {
        for (int i = 0; i < arr.length; i++) {
            Action action = arr[i];
            if (action instanceof OpenAdvancedAction) {
                Action clone = ((ContextAwareAction)action).createContextAwareInstance(context);
                assertEquals("Correctly enabled. " + msg, enabled, clone.isEnabled());
                return clone;
            }
        }
        fail("No open advanced action found!");
        return null;
    }

}
