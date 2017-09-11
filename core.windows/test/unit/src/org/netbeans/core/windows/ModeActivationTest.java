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
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;

/** 
 * Test Mode activation behavior.
 * 
 * @author Marek Slama
 * 
 */
public class ModeActivationTest extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(ModeActivationTest.class);
    }

    public ModeActivationTest (String name) {
        super (name);
    }
    
    protected boolean runInEQ () {
        return true;
    }
    
    /**
     * Test basic behavior when Mode is activated. TC is docked into Mode, opened, activated,
     * closed. During this activation state of Mode is tested.
     */
    public void testActivate () throws Exception {
        Lookup.getDefault().lookup(ModuleInfo.class);
        
        PersistenceHandler.getDefault().load();
        
        //This must be unit test as we need minimum winsys config
        //if default minimum winsys config is changed this test must be changed too.
        WindowManagerImpl wmi = WindowManagerImpl.getInstance();
        Mode activeMode = wmi.getActiveMode();
        assertNull("No mode is activated ie. active mode must be null",activeMode);
        
        //Mode cannot be activated when it is empty
        Mode editor = wmi.getDefaultEditorMode();
        wmi.setActiveMode((ModeImpl) editor);
        activeMode = wmi.getActiveMode();
        assertNull("Ignore mode activation when mode is empty",activeMode);
        
        //Editor mode must be empty
        TopComponent [] tcs = editor.getTopComponents();
        assertEquals("Mode editor must be empty",tcs.length,0);

        //Dock TC into mode
        TopComponent tc = new TopComponent();
        
        //As tc is not yet docked into any mode this must return null
        Mode m = wmi.findMode(tc);
        assertNull("No mode for TC",m);
        
        editor.dockInto(tc);
        //Editor mode must contain one TC
        tcs = editor.getTopComponents();
        assertEquals("Mode editor must contain one TC", 1, tcs.length);
        
        //Mode cannot be activated when it does not contain opened TC
        wmi.setActiveMode((ModeImpl) editor);
        activeMode = wmi.getActiveMode();
        assertNull("Mode cannot be activated when it does not contain opened TC",activeMode);
        
        m = wmi.findMode(tc);
        assertEquals("Mode editor must be found for TC", editor, m);
        
        //TC is closed
        assertFalse("TC is closed",tc.isOpened());
        
        tc.open();
        //TC is opened
        assertTrue("TC is opened",tc.isOpened());
        tc.requestActive();
        
        //Editor mode is now activated
        activeMode = wmi.getActiveMode();
        assertEquals("Editor mode is now activated",editor,activeMode);
        
        //Check active tc
        TopComponent activeTC = wmi.getRegistry().getActivated();
        assertEquals("TC is now active",tc,activeTC);
        
        tc.close();
        //TC is closed
        assertFalse("TC is closed",tc.isOpened());
        
        //No mode is now activated
        activeMode = wmi.getActiveMode();
        assertNull("No mode is activated ie. active mode must be null", activeMode);
    }
    
}
