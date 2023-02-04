/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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

public class ALT_Positioning09bTest extends LayoutTestCase {

    public ALT_Positioning09bTest(String name) {
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
     * Add toggle button snapped next to the list (right of), on the line with
     * the second button (which gets moved as a result).
     */
    public void doChanges0() {
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 334, 300));
        compBounds.put("jScrollPane1", new Rectangle(176, 65, 35, 130));
        baselinePosition.put("jScrollPane1-35-130", 0);
        compBounds.put("jButton1", new Rectangle(241, 79, 73, 23));
        baselinePosition.put("jButton1-73-23", 15);
        compBounds.put("jButton2", new Rectangle(261, 132, 73, 23));
        baselinePosition.put("jButton2-73-23", 15);
        prefPaddingInParent.put("Form-jButton1-0-1", 10); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollPane1-1-1", 11); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton2-1-1", 11); // parentId-compId-dimension-compAlignment
        contInterior.put("Form", new Rectangle(0, 0, 334, 300));
        compBounds.put("jScrollPane1", new Rectangle(176, 65, 35, 130));
        baselinePosition.put("jScrollPane1-35-130", 0);
        compBounds.put("jButton1", new Rectangle(241, 79, 73, 23));
        baselinePosition.put("jButton1-73-23", 15);
        compBounds.put("jButton2", new Rectangle(261, 132, 73, 23));
        baselinePosition.put("jButton2-73-23", 15);
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
        lc = new LayoutComponent("jToggleButton1", false);
        // > START ADDING
        baselinePosition.put("jToggleButton1-105-23", 15);
        {
            LayoutComponent[] comps = new LayoutComponent[]{lc};
            Rectangle[] bounds = new Rectangle[]{new Rectangle(0, 0, 105, 23)};
            String defaultContId = null;
            Point hotspot = new Point(48, 11);
            ld.startAdding(comps, bounds, hotspot, defaultContId);
        }
        // < START ADDING
        prefPaddingInParent.put("Form-jToggleButton1-1-0", 11); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jToggleButton1-1-1", 11); // parentId-compId-dimension-compAlignment
        prefPadding.put("jButton1-jToggleButton1-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jToggleButton1-1-0-1", 11); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jToggleButton1-1-0-2", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jToggleButton1-1-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jButton1-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jButton1-1-0-1", 11); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jButton1-1-0-2", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jButton1-1-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jToggleButton1-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jToggleButton1-1-0-1", 11); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jToggleButton1-1-0-2", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jToggleButton1-1-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jButton2-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jButton2-1-0-1", 11); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jButton2-1-0-2", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jButton2-1-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jToggleButton1-0-0", 10); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jToggleButton1-0-1", 10); // parentId-compId-dimension-compAlignment
        prefPadding.put("jScrollPane1-jToggleButton1-0-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jToggleButton1-0-0-1", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jToggleButton1-0-0-2", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jToggleButton1-0-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jScrollPane1-0-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jScrollPane1-0-0-1", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jScrollPane1-0-0-2", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jScrollPane1-0-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jButton2-0-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jButton2-0-0-1", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jButton2-0-0-2", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jButton2-0-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        // > MOVE
        // > MOVE
        {
            Point p = new Point(264, 145);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(217, 132, 105, 23)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPaddingInParent.put("Form-jToggleButton1-1-0", 11); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jToggleButton1-1-1", 11); // parentId-compId-dimension-compAlignment
        prefPadding.put("jButton1-jToggleButton1-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jToggleButton1-1-0-1", 11); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jToggleButton1-1-0-2", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jToggleButton1-1-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jButton1-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jButton1-1-0-1", 11); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jButton1-1-0-2", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jButton1-1-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jToggleButton1-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jToggleButton1-1-0-1", 11); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jToggleButton1-1-0-2", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jToggleButton1-1-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jButton2-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jButton2-1-0-1", 11); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jButton2-1-0-2", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jButton2-1-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jToggleButton1-0-0", 10); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jToggleButton1-0-1", 10); // parentId-compId-dimension-compAlignment
        prefPadding.put("jScrollPane1-jToggleButton1-0-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jToggleButton1-0-0-1", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jToggleButton1-0-0-2", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jToggleButton1-0-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jScrollPane1-0-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jScrollPane1-0-0-1", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jScrollPane1-0-0-2", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jScrollPane1-0-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jButton2-0-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jButton2-0-0-1", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jButton2-0-0-2", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jButton2-0-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        // > MOVE
        // > MOVE
        {
            Point p = new Point(264, 146);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(217, 132, 105, 23)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        compPrefSize.put("jToggleButton1", new Dimension(105, 23));
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 401, 300));
        compBounds.put("jScrollPane1", new Rectangle(176, 65, 35, 130));
        baselinePosition.put("jScrollPane1-35-130", 0);
        compBounds.put("jButton1", new Rectangle(241, 79, 73, 23));
        baselinePosition.put("jButton1-73-23", 15);
        compBounds.put("jButton2", new Rectangle(328, 132, 73, 23));
        baselinePosition.put("jButton2-73-23", 15);
        compBounds.put("jToggleButton1", new Rectangle(217, 132, 105, 23));
        baselinePosition.put("jToggleButton1-105-23", 15);
        prefPadding.put("jScrollPane1-jToggleButton1-0-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jButton1-0-1", 10); // parentId-compId-dimension-compAlignment
        prefPadding.put("jToggleButton1-jButton2-0-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jButton2-0-0-1", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jButton2-0-0-2", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jButton2-0-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jScrollPane1-1-1", 11); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton2-1-1", 11); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jToggleButton1-1-1", 11); // parentId-compId-dimension-compAlignment
        contInterior.put("Form", new Rectangle(0, 0, 401, 300));
        compBounds.put("jScrollPane1", new Rectangle(176, 65, 35, 130));
        baselinePosition.put("jScrollPane1-35-130", 0);
        compBounds.put("jButton1", new Rectangle(241, 79, 73, 23));
        baselinePosition.put("jButton1-73-23", 15);
        compBounds.put("jButton2", new Rectangle(328, 132, 73, 23));
        baselinePosition.put("jButton2-73-23", 15);
        compBounds.put("jToggleButton1", new Rectangle(217, 132, 105, 23));
        baselinePosition.put("jToggleButton1-105-23", 15);
        prefPadding.put("jScrollPane1-jToggleButton1-0-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }

}
