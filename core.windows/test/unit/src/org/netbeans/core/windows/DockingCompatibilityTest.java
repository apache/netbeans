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
 * Software is Sun Microsystems, Inc. Portions Copyright 2002 Sun
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

import org.netbeans.junit.*;

import org.openide.windows.*;


/** Test to guarantee that the compatibility for docking operations is
 * preserved for components written against release 3.5 and later and
 * that such components can be docked.
 *
 * @author Jaroslav Tulach
 */
public class DockingCompatibilityTest extends NbTestCase {
    private Mode mode;

    public DockingCompatibilityTest (String name) {
        super (name);
    }

    @Override
    protected void setUp() throws Exception {
        mode = WindowManager.getDefault().getCurrentWorkspace().createMode("OwnMode", "displayName", null);
    }

    @Override
    protected boolean runInEQ () {
        return true;
    }
    
    
    public void testSimplyOpenedComponentCanBeDockedWhereeverItWants () throws Exception {
        TopComponent tc = new TopComponent ();
        tc.open ();
        
        assertCanBeDocked (tc, Boolean.TRUE);
    }
    
    public void testComponentPutIntoOwnModeCanBeDockedAsWell () {
        TopComponent tc = new TopComponent ();
        mode.dockInto (tc);
        tc.open ();
        
        assertCanBeDocked (tc, Boolean.TRUE);
    }

    public void testComponentPlacedDirectlyIntoEditorModeHasToStayThere () {
        Mode editor = WindowManager.getDefault ().findMode ("editor");
        assertNotNull ("Shall not be null", editor);
        TopComponent tc = new TopComponent ();
        editor.dockInto (tc);
        assertCanBeDocked (tc, null);
    }
    
    
    private static void assertCanBeDocked (TopComponent tc, Boolean expectedValue) {
        assertEquals (
            expectedValue,  
            tc.getClientProperty (Constants.TOPCOMPONENT_ALLOW_DOCK_ANYWHERE)
        );
    }
}

