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


/**
 * Tests correct behaviour of TopComponent.openAtTabPosition and TopComponent.getTabPosition.
 * 
 * @author Dafe Simonek
 */
public class OpenAtTabPositionTest extends NbTestCase {

    public OpenAtTabPositionTest (String name) {
        super (name);
    }

    protected boolean runInEQ () {
        return true;
    }
     
    public void testIsOpenedAtRightPosition () throws Exception {
        Mode mode = WindowManagerImpl.getInstance().createMode("testIsOpenedAtRightPositionMode",
                Constants.MODE_KIND_EDITOR, Constants.MODE_STATE_JOINED, false, new SplitConstraint[0] );
        
        TopComponent firstTC = new TopComponent();
        mode.dockInto(firstTC);
        TopComponent tc1 = new TopComponent();
        mode.dockInto(tc1);
        TopComponent tc2 = new TopComponent();
        mode.dockInto(tc2);
        TopComponent tc3 = new TopComponent();
        mode.dockInto(tc3);
        
        System.out.println("Checking getTabPosition on closed TopComponent...");
        assertTrue("Expected TC position -1, but got " + tc1.getTabPosition(), tc1.getTabPosition() == -1);
                
        System.out.println("Checking open both on impossible and possible positions...");
        
        firstTC.open();
        
        tc1.openAtTabPosition(2);
        assertTrue(tc1.isOpened());
        assertTrue("Expected TC position 1, but got " + tc1.getTabPosition(), tc1.getTabPosition() == 1);
        
        tc2.openAtTabPosition(-2);
        assertTrue(tc2.isOpened());
        assertTrue("Expected TC position 0, but got " + tc2.getTabPosition(), tc2.getTabPosition() == 0);
        
        tc3.openAtTabPosition(1);
        assertTrue(tc3.isOpened());
        assertTrue("Expected TC position 1, but got " + tc3.getTabPosition(), tc3.getTabPosition() == 1);
        assertTrue("Expected TC position 3, but got " + tc1.getTabPosition(), tc1.getTabPosition() == 3);
        assertTrue("Expected TC position 0, but got " + tc2.getTabPosition(), tc2.getTabPosition() == 0);
        
        tc3.close();
        assertTrue("Expected TC position -1, but got " + tc3.getTabPosition(), tc3.getTabPosition() == -1);
        
    }
    
}
