/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
