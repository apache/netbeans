/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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

