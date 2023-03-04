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

package org.netbeans.core.windows.view.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.JPanel;
import junit.framework.TestCase;
import org.netbeans.core.windows.Constants;
import org.openide.windows.TopComponent;

/**
 *
 * @author S. Aubrecht
 */
public class DesktopImplTest extends TestCase {
    
    public DesktopImplTest(String testName) {
        super(testName);
    }

    /**
     * Test of computeSlideInBounds method, of class DesktopImpl.
     */
    public void testComputeSlideInBounds() {
        int ideWidth = 1000;
        int ideHeight = 1000;
        Rectangle splitRootRect = new Rectangle(0,0,ideWidth, ideHeight);
        String side = Constants.LEFT;
        Component slideComponent = new JPanel();
        slideComponent.setBounds(new Rectangle(0,0,100,ideHeight));
        
        Rectangle slideBounds = new Rectangle(0,0,ideWidth/2,ideHeight);
        TopComponent selTc = new TopComponent();
        selTc.setPreferredSize(new Dimension(200,200) );
        
        DesktopImpl instance = new DesktopImpl();
        
        //test default behavior
        Rectangle result = instance.computeSlideInBounds(splitRootRect, side, slideComponent, slideBounds, selTc);
        assertNotNull(result);
        assertEquals( result.width, slideBounds.width);
        assertEquals(result.height, splitRootRect.height);
        
        //now let's test that preferred size is respected
        selTc.putClientProperty(Constants.KEEP_PREFERRED_SIZE_WHEN_SLIDED_IN, Boolean.TRUE);
        result = instance.computeSlideInBounds(splitRootRect, side, slideComponent, slideBounds, selTc);
        assertNotNull(result);
        assertEquals( result.getSize(), selTc.getPreferredSize());
        
        //turn the flag off and test again
        selTc.putClientProperty(Constants.KEEP_PREFERRED_SIZE_WHEN_SLIDED_IN, Boolean.FALSE);
        result = instance.computeSlideInBounds(splitRootRect, side, slideComponent, slideBounds, selTc);
        assertNotNull(result);
        assertEquals( result.width, slideBounds.width);
        assertEquals(result.height, splitRootRect.height);
    }

}
