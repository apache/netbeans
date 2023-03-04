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
