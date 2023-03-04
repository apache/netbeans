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
