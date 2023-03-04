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

import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInfo;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;

/**
 *
 * @author Jaroslav Tulach
 */
public class GeneralActionTest extends NbTestCase {
    private FileObject actionFile;
    private FileObject root;
    
    public GeneralActionTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        if (true) return; // disabled right now

        clearWorkDir();
        
        Logger.getLogger("org.netbeans.core.startup").setLevel(Level.OFF);

        URI uri = ModuleInfo.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        File jar = new File(uri);
        System.setProperty("netbeans.home", jar.getParentFile().getParent());
        System.setProperty("netbeans.user", getWorkDirPath());
        StringBuffer sb = new StringBuffer();
        boolean found = false;
        for (ModuleInfo info : Lookup.getDefault().lookupAll(ModuleInfo.class)) {
            if (info.getCodeNameBase().equals("org.netbeans.modules.subversion")) {
                Method m = null;
                Class<?> c = info.getClass();
                Exception ex = null;
                for (;;) {
                    if (c == null) {
                        throw ex;
                    }
                    try {
                        m = c.getDeclaredMethod("setEnabled", Boolean.TYPE);
                    } catch (Exception ex2) {
                        ex = ex2;
                    }
                    if (m != null) {
                        break;
                    }
                    c = c.getSuperclass();
                }
                m.setAccessible(true);
                m.invoke(info, false);
                assertFalse("Module is now disabled", info.isEnabled());
                found = true;
            }
            sb.append(info.getCodeNameBase()).append('\n');
        }
        if (!found) {
            fail("No module found:\n" + sb);
        }

        File dbp = new File(getWorkDir(), "dbproject");
        File db = new File(dbp, "project.properties");
        dbp.mkdirs();
        db.createNewFile();

        root = FileUtil.toFileObject(getWorkDir());
        assertNotNull("fileobject found", root);
        
        FoDLayersProvider.getInstance().refreshForce();

        actionFile = FileUtil.getConfigFile("Actions/System/org-netbeans-modules-autoupdate-ui-actions-PluginManagerAction.instance");
        assertNotNull("testing layer is loaded: ", actionFile);
    }
    
    @Override
    protected void tearDown() throws Exception {
    }
    
    @Override
    protected boolean runInEQ() {
        return true;
    }
    
    public void testIconIsCorrect() throws Exception {
        if (true) return; // disabled right now
        
        myListenerCounter = 0;
        myIconResourceCounter = 0;
        Action a = readAction();
        
        assertTrue("Always enabled", a.isEnabled());
        a.setEnabled(false);
        assertTrue("Still Always enabled", a.isEnabled());
        
        
        assertEquals("No icon in menu", Boolean.TRUE, a.getValue("noIconInMenu"));
        
        if (a instanceof ContextAwareAction) {
            fail("Should not be context sensitive, otherwise it would have to implement equal correctly: " + a);
        }
        
        a.actionPerformed(null);
    }
    
    private static int myListenerCounter;
    private static ActionListener myListener() {
        myListenerCounter++;
        return null;
    }
    private static int myIconResourceCounter;
    private static String myIconResource() {
        myIconResourceCounter++;
        return "/org/netbeans/modules/ide/ergonomics/api/TestIcon.png";
    }
    
    
    private Action readAction() throws Exception {
        FileObject fo = this.actionFile;
        assertNotNull("file " + actionFile, fo);
        
        Object obj = fo.getAttribute("instanceCreate");
        assertNotNull("File object has not null instanceCreate attribute", obj);
        
        if (!(obj instanceof Action)) {
            fail("Object needs to be action: " + obj);
        }
        
        return (Action)obj;
    }
}
