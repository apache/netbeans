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
