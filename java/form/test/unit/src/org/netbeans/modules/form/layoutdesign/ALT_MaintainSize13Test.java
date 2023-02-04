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

public class ALT_MaintainSize13Test extends LayoutTestCase {

    public ALT_MaintainSize13Test(String name) {
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
     * Delete jButton2.
     */
    public void doChanges0() {
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jScrollPane1", new Rectangle(25, 52, 35, 191));
        baselinePosition.put("jScrollPane1-35-191", 0);
        compBounds.put("jButton1", new Rectangle(66, 52, 73, 23));
        baselinePosition.put("jButton1-73-23", 15);
        compBounds.put("jButton2", new Rectangle(66, 145, 73, 23));
        baselinePosition.put("jButton2-73-23", 15);
        compBounds.put("jLabel1", new Rectangle(143, 149, 34, 14));
        baselinePosition.put("jLabel1-34-14", 11);
        compBounds.put("jButton3", new Rectangle(66, 174, 73, 23));
        baselinePosition.put("jButton3-73-23", 15);
        compBounds.put("jButton4", new Rectangle(258, 174, 73, 23));
        baselinePosition.put("jButton4-73-23", 15);
        prefPaddingInParent.put("Form-jButton1-0-1", 10); // parentId-compId-dimension-compAlignment
        prefPadding.put("jButton3-jButton4-0-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton3-jButton4-0-0-1", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton3-jButton4-0-0-2", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton3-jButton4-0-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jButton4-0-0-0", 4); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jButton4-0-0-1", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jButton4-0-0-2", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jButton4-0-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jScrollPane1-1-1", 11); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton4-1-1", 11); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton3-1-1", 11); // parentId-compId-dimension-compAlignment
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jScrollPane1", new Rectangle(25, 52, 35, 191));
        baselinePosition.put("jScrollPane1-35-191", 0);
        compBounds.put("jButton1", new Rectangle(66, 52, 73, 23));
        baselinePosition.put("jButton1-73-23", 15);
        compBounds.put("jButton2", new Rectangle(66, 145, 73, 23));
        baselinePosition.put("jButton2-73-23", 15);
        compBounds.put("jLabel1", new Rectangle(143, 149, 34, 14));
        baselinePosition.put("jLabel1-34-14", 11);
        compBounds.put("jButton3", new Rectangle(66, 174, 73, 23));
        baselinePosition.put("jButton3-73-23", 15);
        compBounds.put("jButton4", new Rectangle(258, 174, 73, 23));
        baselinePosition.put("jButton4-73-23", 15);
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
        lm.removeComponent("jButton2", true);
        prefPaddingInParent.put("Form-jButton1-0-1", 10); // parentId-compId-dimension-compAlignment
        prefPadding.put("jButton3-jButton4-0-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton3-jButton4-0-0-1", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton3-jButton4-0-0-2", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton3-jButton4-0-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jButton4-0-0-0", 4); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jButton4-0-0-1", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jButton4-0-0-2", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jButton4-0-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jScrollPane1-1-1", 11); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton4-1-1", 11); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton3-1-1", 11); // parentId-compId-dimension-compAlignment
        // parentId-compId-dimension-compAlignment
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jScrollPane1", new Rectangle(25, 52, 35, 191));
        baselinePosition.put("jScrollPane1-35-191", 0);
        compBounds.put("jButton1", new Rectangle(66, 52, 73, 23));
        baselinePosition.put("jButton1-73-23", 15);
        compBounds.put("jLabel1", new Rectangle(143, 149, 34, 14));
        baselinePosition.put("jLabel1-34-14", 11);
        compBounds.put("jButton3", new Rectangle(66, 169, 73, 23));
        baselinePosition.put("jButton3-73-23", 15);
        compBounds.put("jButton4", new Rectangle(258, 169, 73, 23));
        baselinePosition.put("jButton4-73-23", 15);
        prefPaddingInParent.put("Form-jButton1-0-1", 10); // parentId-compId-dimension-compAlignment
        prefPadding.put("jButton3-jButton4-0-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton3-jButton4-0-0-1", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton3-jButton4-0-0-2", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton3-jButton4-0-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jButton4-0-0-0", 4); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jButton4-0-0-1", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jButton4-0-0-2", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jButton4-0-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jScrollPane1-1-1", 11); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton4-1-1", 11); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton3-1-1", 11); // parentId-compId-dimension-compAlignment
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jScrollPane1", new Rectangle(25, 52, 35, 191));
        baselinePosition.put("jScrollPane1-35-191", 0);
        compBounds.put("jButton1", new Rectangle(66, 52, 73, 23));
        baselinePosition.put("jButton1-73-23", 15);
        compBounds.put("jLabel1", new Rectangle(143, 149, 34, 14));
        baselinePosition.put("jLabel1-34-14", 11);
        compBounds.put("jButton3", new Rectangle(66, 169, 73, 23));
        baselinePosition.put("jButton3-73-23", 15);
        compBounds.put("jButton4", new Rectangle(258, 169, 73, 23));
        baselinePosition.put("jButton4-73-23", 15);
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }

    /**
     * Add jButton2 back left of the label (from palette).
     */
    public void doChanges1() {
        lc = new LayoutComponent("jButton2", false);
        // > START ADDING
        baselinePosition.put("jButton2-73-23", 15);
        {
            LayoutComponent[] comps = new LayoutComponent[]{lc};
            Rectangle[] bounds = new Rectangle[]{new Rectangle(0, 0, 73, 23)};
            String defaultContId = null;
            Point hotspot = new Point(32, 11);
            ld.startAdding(comps, bounds, hotspot, defaultContId);
        }
        // < START ADDING
        prefPaddingInParent.put("Form-jButton2-1-0", 11); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton2-1-1", 11); // parentId-compId-dimension-compAlignment
        prefPadding.put("jButton1-jButton2-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jButton2-1-0-1", 11); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jButton2-1-0-2", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jButton2-1-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jButton1-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jButton1-1-0-1", 11); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jButton1-1-0-2", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jButton1-1-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton3-jButton2-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton3-jButton2-1-0-1", 11); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton3-jButton2-1-0-2", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton3-jButton2-1-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jButton3-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jButton2-0-0", 10); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton2-0-1", 10); // parentId-compId-dimension-compAlignment
        prefPadding.put("jScrollPane1-jButton2-0-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jScrollPane1-0-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jScrollPane1-0-0-1", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jScrollPane1-0-0-2", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jScrollPane1-0-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jButton2-0-0-0", 4); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jButton2-0-0-1", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jButton2-0-0-2", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jButton2-0-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jLabel1-0-0-0", 4); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jLabel1-0-0-1", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jLabel1-0-0-2", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jLabel1-0-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        // > MOVE
        // > MOVE
        {
            Point p = new Point(102, 152);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(66, 145, 73, 23)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPaddingInParent.put("Form-jButton2-1-0", 11); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton2-1-1", 11); // parentId-compId-dimension-compAlignment
        prefPadding.put("jButton1-jButton2-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jButton2-1-0-1", 11); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jButton2-1-0-2", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jButton2-1-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jButton1-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jButton1-1-0-1", 11); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jButton1-1-0-2", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jButton1-1-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton3-jButton2-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton3-jButton2-1-0-1", 11); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton3-jButton2-1-0-2", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton3-jButton2-1-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jButton3-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jButton2-0-0", 10); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton2-0-1", 10); // parentId-compId-dimension-compAlignment
        prefPadding.put("jScrollPane1-jButton2-0-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jScrollPane1-0-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jScrollPane1-0-0-1", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jScrollPane1-0-0-2", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jScrollPane1-0-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jButton2-0-0-0", 4); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jButton2-0-0-1", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jButton2-0-0-2", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jButton2-0-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jLabel1-0-0-0", 4); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jLabel1-0-0-1", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jLabel1-0-0-2", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jLabel1-0-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        // > MOVE
        // > MOVE
        {
            Point p = new Point(101, 152);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(66, 145, 73, 23)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        compPrefSize.put("jButton2", new Dimension(73, 23));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jScrollPane1", new Rectangle(25, 52, 35, 191));
        baselinePosition.put("jScrollPane1-35-191", 0);
        compBounds.put("jButton1", new Rectangle(66, 52, 73, 23));
        baselinePosition.put("jButton1-73-23", 15);
        compBounds.put("jLabel1", new Rectangle(143, 149, 34, 14));
        baselinePosition.put("jLabel1-34-14", 11);
        compBounds.put("jButton3", new Rectangle(66, 174, 73, 23));
        baselinePosition.put("jButton3-73-23", 15);
        compBounds.put("jButton4", new Rectangle(258, 174, 73, 23));
        baselinePosition.put("jButton4-73-23", 15);
        compBounds.put("jButton2", new Rectangle(66, 145, 73, 23));
        baselinePosition.put("jButton2-73-23", 15);
        prefPaddingInParent.put("Form-jButton1-0-1", 10); // parentId-compId-dimension-compAlignment
        prefPadding.put("jButton3-jButton4-0-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton3-jButton4-0-0-1", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton3-jButton4-0-0-2", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton3-jButton4-0-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jButton4-0-0-0", 4); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jButton4-0-0-1", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jButton4-0-0-2", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jButton4-0-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jScrollPane1-1-1", 11); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton4-1-1", 11); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton3-1-1", 11); // parentId-compId-dimension-compAlignment
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jScrollPane1", new Rectangle(25, 52, 35, 191));
        baselinePosition.put("jScrollPane1-35-191", 0);
        compBounds.put("jButton1", new Rectangle(66, 52, 73, 23));
        baselinePosition.put("jButton1-73-23", 15);
        compBounds.put("jLabel1", new Rectangle(143, 149, 34, 14));
        baselinePosition.put("jLabel1-34-14", 11);
        compBounds.put("jButton3", new Rectangle(66, 174, 73, 23));
        baselinePosition.put("jButton3-73-23", 15);
        compBounds.put("jButton4", new Rectangle(258, 174, 73, 23));
        baselinePosition.put("jButton4-73-23", 15);
        compBounds.put("jButton2", new Rectangle(66, 145, 73, 23));
        baselinePosition.put("jButton2-73-23", 15);
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }

}
