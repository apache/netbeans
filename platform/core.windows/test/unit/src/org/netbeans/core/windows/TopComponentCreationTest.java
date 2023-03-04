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

package org.netbeans.core.windows;

import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.netbeans.core.windows.persistence.PersistenceManager;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/** 
 * Test saving of TopComponent of different persistence type.
 * TopComponent is saved according to persistence type and TC state opened/closed.
 * 
 * @author Marek Slama
 * 
 */
public class TopComponentCreationTest extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(TopComponentCreationTest.class);
    }

    private static boolean loaded = false;
    
    public TopComponentCreationTest (String name) {
        super (name);
    }
    
    @Override
    protected void setUp () throws Exception {
        if (!loaded) {
            //Load just once for all tests in this class
            Lookup.getDefault().lookup(ModuleInfo.class);
            PersistenceHandler.getDefault().load();
            loaded = true;
        }
    }
    
    @Override
    protected boolean runInEQ () {
        return true;
    }
    
    /**
     * Test saving of TopComponent with persistence type
     * TopComponent.PERSISTENCE_ALWAYS.
     */
    public void testSavePersistentTopComponent () throws Exception {
        WindowManager wm = WindowManager.getDefault();
        
        Mode m = wm.findMode("explorer");
        assertNotNull("Mode explorer must be present", m);
        
        TopComponent tc = Component00.getDefault();
        m.dockInto(tc);
        tc.open();
        
        String res = "Windows2Local/Modes/explorer/"
        + wm.findTopComponentID(tc) + ".wstcref";
        //Check that persistent, opened TC is saved ie. wstcref file is created
        PersistenceHandler.getDefault().save();
        //Check wstcref file was created
        assertNotNull(FileUtil.getConfigFile(res));
        deleteLocalData();
        //Check wstcref file was deleted
        assertNull(FileUtil.getConfigFile(res));
        
        //Check that persistent, closed TC is saved ie. wstcref file is created
        tc.close();
        PersistenceHandler.getDefault().save();        
        //Check wstcref file was created
        assertNotNull(FileUtil.getConfigFile(res));
        deleteLocalData();
        //Check wstcref file was deleted
        assertNull(FileUtil.getConfigFile(res));
    }
    
    /**
     * Test saving of TopComponent with persistence type
     * TopComponent.PERSISTENCE_ONLY_OPENED.
     */
    public void testSavePersistentOnlyOpenedTopComponent () throws Exception {
        WindowManager wm = WindowManager.getDefault();
        
        Mode m = wm.findMode("explorer");
        assertNotNull("Mode explorer must be present", m);
        
        TopComponent tc = new Component01();
        m.dockInto(tc);
        tc.open();
        
        String res = "Windows2Local/Modes/explorer/"
        + wm.findTopComponentID(tc) + ".wstcref";
        
        //Check that persistent only opened, opened TC is saved ie. wstcref file is created
        PersistenceHandler.getDefault().save();
        //Check wstcref file was created
        assertNotNull(FileUtil.getConfigFile(res));
        deleteLocalData();
        //Check wstcref file was deleted
        assertNull(FileUtil.getConfigFile(res));
        
        //Check that persistent only opened, closed TC is NOT saved ie. wstcref file is NOT created
        tc.close();
        PersistenceHandler.getDefault().save();        
        //Check wstcref file was not created
        assertNull(FileUtil.getConfigFile(res));
        deleteLocalData();
    }
    /**
     * Test saving of TopComponent with persistence type
     * TopComponent.PERSISTENCE_NEVER.
     */
    public void testSavePersistentNeverTopComponent () throws Exception {
        WindowManager wm = WindowManager.getDefault();
        
        Mode m = wm.findMode("explorer");
        assertNotNull("Mode explorer must be present", m);
        
        TopComponent tc = new Component02();
        m.dockInto(tc);
        tc.open();
        
        String res = "Windows2Local/Modes/explorer/"
        + wm.findTopComponentID(tc) + ".wstcref";
        
        //Check that non persistent, opened TC is NOT saved ie. wstcref file is NOT created
        PersistenceHandler.getDefault().save();
        //Check wstcref file was not created
        assertNull(FileUtil.getConfigFile(res));
        deleteLocalData();
        
        //Check that non persistent, closed TC is NOT saved ie. wstcref file is NOT created
        tc.close();
        PersistenceHandler.getDefault().save();        
        //Check wstcref file was not created
        assertNull(FileUtil.getConfigFile(res));
        deleteLocalData();
    }
    
    /** 
     * Clean folder Windows2Local with custom data created when winsys is saved.
     */
    private void deleteLocalData () {
        try {
            FileObject rootFolder = PersistenceManager.getDefault().getRootLocalFolder();
            if (rootFolder != null) {
                for (FileObject fo : rootFolder.getChildren()) {
                    if (PersistenceManager.COMPS_FOLDER.equals(fo.getName())) {
                        continue; //do not delete settings files
                    }
                    fo.delete();
                }
            }
        } catch (IOException exc) {
            Logger.getLogger(this.getClass().getName()).log
            (Level.INFO, "Cannot delete local data:", exc);
        }
    }
    
}

