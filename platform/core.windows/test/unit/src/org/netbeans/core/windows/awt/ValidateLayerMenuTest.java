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

package org.netbeans.core.windows.awt;

import java.net.URL;
import java.util.ArrayList;
import javax.swing.Box;
import junit.framework.*;
import org.netbeans.junit.*;
import org.openide.cookies.InstanceCookie;

import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;

/** Checks the consistence of Menu folder.
 *
 * @author Jaroslav Tulach
 */
public class ValidateLayerMenuTest extends NbTestCase {

    static {
        System.setProperty("java.awt.headless", "true");
    }

    /** Creates a new instance of SFSTest */
    public ValidateLayerMenuTest(String name) {
        super (name);
    }

    public static Test suite() {
        return NbModuleSuite.createConfiguration(ValidateLayerMenuTest.class)
                .clusters(".*")
                .enableModules(".*")
                .gui(false)
                .suite();
    }

    //
    // override in subclasses
    //
    
    protected String rootName () {
        return "Menu";
    }
    
    /** Allowes to skip filest that are know to be broken */
    protected boolean skipFile (FileObject fo) {
        if (fo.getPath().equals("Menu/Help/org-netbeans-modules-usersguide-master.xml")) {
            return true;
        } else if(fo.getPath().equals("Menu/Help/master-help.xml")) {
            return true;
        }
        // ignore these files, there are helper for implementation of 
        // View/filesystems, View/Runtime, View/Projects, etc.
        return fo.getPath().startsWith ("Menu/Window/oldRoots") && fo.hasExt ("txt");
    }
    
    protected boolean correctInstance (Object obj) {
        if (obj instanceof javax.swing.Action) {
            return true;
        }
        if (obj instanceof org.openide.util.actions.Presenter.Menu) {
            return true;
        }
        if (obj instanceof javax.swing.JSeparator) {
            return true;
        }
        if (obj instanceof javax.swing.JMenuItem) {
            return true;
        }
        if (obj instanceof Box.Filler) {
            return true;
        }
        
        return false;
    }
    
    
    //
    // the test
    // 
    
    public void testContentCorrect () throws Exception {
        // This magic call will load modules and fill content of default file system
        // where xml layers live - uaah sometimes I think I just live in another world
        Lookup.getDefault().lookup(ModuleInfo.class);
        
        ArrayList<String> errors = new ArrayList<>();

        DataFolder dataFolder = DataFolder.findFolder(FileUtil.getConfigFile(rootName()));
        
        assertNotNull(dataFolder);
        verifyMenu (dataFolder, errors);
        
        if (!errors.isEmpty()) {
            fail ("Some files do not provide valid menu elements" + errors);
        }
    }
    
    private void verifyMenu (DataFolder f, ArrayList<String> errors) throws Exception {
        DataObject[] arr = f.getChildren();
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] instanceof DataFolder) {
                verifyMenu ((DataFolder)arr[i], errors);
                continue;
            } 
            FileObject file = arr[i].getPrimaryFile ();
                                    
            if (skipFile (file)) {
                continue;
            }
            
            URL url = file.toURL();
            
            InstanceCookie ic = (InstanceCookie)arr[i].getCookie(InstanceCookie.class);
            if (ic == null) {
                errors.add ("\n    File " + file + " does not have instance cookie, url: " + url);
                continue;
            }
            
            try {
                Object obj = ic.instanceCreate();
                if (correctInstance (obj)) {
                    continue;
                }
                errors.add ("\n    File " + arr[i].getPrimaryFile () + " does not provide correct instance: " + obj + " url: " + url);
            } catch (Exception ex) {
                errors.add ("\n    File " + arr[i].getPrimaryFile () + " cannot be read " + ex + " url: " + url);
            }
        }
    }
    
}

