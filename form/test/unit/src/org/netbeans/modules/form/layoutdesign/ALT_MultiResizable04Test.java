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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.form.layoutdesign;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.*;
import org.openide.filesystems.FileUtil;

public class ALT_MultiResizable04Test extends LayoutTestCase {

    public ALT_MultiResizable04Test(String name) {
        super(name);
        try {
            className = this.getClass().getName();
            className = className.substring(className.lastIndexOf('.') + 1, className.length());
            startingFormFile = FileUtil.toFileObject(new File(url.getFile() + goldenFilesPath + className + "-StartingForm.form").getCanonicalFile());
        } catch (IOException ioe) {
            fail(ioe.toString());
        }
    }

    /**
     * Set the gap size right of the button to 170. Also the gap left of it
     * should be set to its actual size (so far unset - as 0).
     */
    public void doChanges0() {
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 421, 300));
        contInterior.put("Form", new Rectangle(0, 0, 421, 300));
        compBounds.put("jToggleButton1", new Rectangle(10, 11, 105, 23));
        baselinePosition.put("jToggleButton1-105-23", new Integer(15));
        compBounds.put("jToggleButton2", new Rectangle(121, 11, 179, 23));
        baselinePosition.put("jToggleButton2-179-23", new Integer(15));
        compBounds.put("jToggleButton3", new Rectangle(306, 11, 105, 23));
        baselinePosition.put("jToggleButton3-105-23", new Integer(15));
        compBounds.put("jButton1", new Rectangle(174, 138, 73, 23));
        baselinePosition.put("jButton1-73-23", new Integer(15));
        compMinSize.put("Form", new Dimension(421, 172));
        compBounds.put("Form", new Rectangle(0, 0, 421, 300));
        compBounds.put("Form", new Rectangle(0, 0, 421, 300));
        compBounds.put("Form", new Rectangle(0, 0, 421, 300));
        compPrefSize.put("jToggleButton2", new Dimension(179, 23));
        prefPaddingInParent.put("Form-jButton1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jToggleButton1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jToggleButton2-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jToggleButton3-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jToggleButton1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jToggleButton3-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
        ld.externalSizeChangeHappened();

        LayoutInterval leftGap = LayoutInterval.getDirectNeighbor(lm.getLayoutComponent("jButton1").getLayoutInterval(0), 0, false);
        assertEquals(0, leftGap.getPreferredSize());
        // set gap size next to the button to 170 as from the Edit Layout Space dialog
        LayoutInterval rightGap = LayoutInterval.getDirectNeighbor(lm.getLayoutComponent("jButton1").getLayoutInterval(0), 1, false);
        lm.setUserIntervalSize(rightGap, 0, 170, true);

// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 427, 300));
        contInterior.put("Form", new Rectangle(0, 0, 427, 300));
        compBounds.put("jToggleButton1", new Rectangle(10, 11, 105, 23));
        baselinePosition.put("jToggleButton1-105-23", new Integer(15));
        compBounds.put("jToggleButton2", new Rectangle(121, 11, 185, 23));
        baselinePosition.put("jToggleButton2-185-23", new Integer(15));
        compBounds.put("jToggleButton3", new Rectangle(312, 11, 105, 23));
        baselinePosition.put("jToggleButton3-105-23", new Integer(15));
        compBounds.put("jButton1", new Rectangle(174, 138, 73, 23));
        baselinePosition.put("jButton1-73-23", new Integer(15));
        compMinSize.put("Form", new Dimension(421, 172));
        compBounds.put("Form", new Rectangle(0, 0, 427, 300));
        compBounds.put("Form", new Rectangle(0, 0, 427, 300));
        compPrefSize.put("jToggleButton2", new Dimension(179, 23));
        prefPaddingInParent.put("Form-jButton1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
    }
}
