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

package org.netbeans.api.autoupdate;

import org.netbeans.modules.autoupdate.services.UpdateProblemHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.autoupdate.updateprovider.AutoupdateCatalogProvider;
import org.openide.modules.Dependency;

/**
 *
 * @author Jiri Rechtacek
 */
public class UpdateManagerTest extends DefaultTestCase {
    
    public UpdateManagerTest (String testName) {
        super (testName);
    }
    
    private UpdateUnit independent = null;
    private UpdateUnit depending = null;
    
    public void testGetDefault() {
        UpdateManager result = UpdateManager.getDefault ();

        assertNotNull ("UpdateManager.getDefault () found.", result);
    }

    public void testGetUpdateUnits() {
        List<UpdateUnit> result = UpdateManager.getDefault ().getUpdateUnits (UpdateManager.TYPE.MODULE);
        
        assertNotNull ("List of UpdateUnit found.", result);
        
        List<UpdateUnit> newModules = new ArrayList<UpdateUnit> ();
        for (UpdateUnit unit : result) {
            if (unit.getInstalled () == null) {
                newModules.add (unit);
            }
        }
        
        assertNotNull ("New Modules found.", newModules);
        assertFalse ("New Modules not empty.", newModules.isEmpty ());
        
        for (UpdateUnit unit: newModules) {
            System.out.println ("Unit: " + unit.getCodeName ());
            if (unit.getCodeName ().indexOf ("independent") >= 0) {
                independent = unit;
            }
            if (unit.getCodeName ().indexOf ("depending") >= 0) {
                depending = unit;
            }
        }
    }
    
    public void testGetDependingUpdateUnits () {
        List<UpdateUnit> result = UpdateManager.getDefault ().getUpdateUnits (UpdateManager.TYPE.MODULE);
        
        assertNotNull ("List of UpdateUnit found.", result);
        
        List<UpdateUnit> newModules = new ArrayList<UpdateUnit> ();
        for (UpdateUnit unit : result) {
            if (unit.getInstalled () == null) {
                newModules.add (unit);
            }
        }
        
        assertNotNull ("New Modules found.", newModules);
        assertFalse ("New Modules not empty.", newModules.isEmpty ());
        
        UpdateUnit engine = null;
        
        for (UpdateUnit unit: newModules) {
            if (unit.getCodeName ().indexOf ("org.yourorghere.engine") >= 0) {
                engine = unit;
            }
        }
        
        assertTrue ("There are more depending elements.", engine.getAvailableUpdates ().size () > 1);
    }
    
    public void testInstallIndependentUnit () {
        testGetUpdateUnits ();
        assertNotNull ("I have Independent module.", independent);
        assertNotNull ("Has some UpdateElements to install.", independent.getAvailableUpdates ());
        assertFalse ("Independent has some UpdateElements to install.", independent.getAvailableUpdates ().isEmpty ());
        UpdateElement el = independent.getAvailableUpdates ().get (0);
        assertNotNull ("I have UpdateElement to install.", el);        
    }

    public void testInstallDependingUnit () {
        testGetUpdateUnits ();
        assertNotNull ("I have Depending module.", depending);
        assertNotNull ("Has some UpdateElements to install.", depending.getAvailableUpdates ());
        assertFalse ("Depending has some UpdateElements to install.", depending.getAvailableUpdates ().isEmpty ());
        UpdateElement el = depending.getAvailableUpdates ().get (0);
        assertNotNull ("I have UpdateElement to install.", el);
    }
    
    public static class MyProvider extends AutoupdateCatalogProvider {
        public MyProvider () {
            super ("test-updates-provider", "test-updates-provider", UpdateManagerTest.class.getResource ("data/updates.xml"));
        }
    }
    
    public static class MyProblemHandler extends UpdateProblemHandler {
        
        public boolean ignoreBrokenDependency (Dependency dependency) {
            if (Dependency.TYPE_REQUIRES == dependency.getType ()) {
                return true;
            } else if (Dependency.TYPE_MODULE == dependency.getType ()) {
                return ! dependency.getName ().startsWith ("org.yourorghere");
            }
            
            return false;
        }
    
        public boolean addRequiredElements (Set<UpdateElement> elements) {
            System.out.println ("addRequiredElements(" + elements + ")");
            return true;
        }
    
        public boolean allowUntrustedUpdateElement(String state, UpdateElement element) {
            System.out.println ("allowUntrustedUpdateElement(" + state + ", " + element.getDisplayName () + ")");
            return true;
        }

        public boolean approveLicenseAgreement (String license) {
            return true;
        }
        
        public boolean restartNow () {
            return false;
        }
        
    }
    
}
