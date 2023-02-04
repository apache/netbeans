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

public class ALT_SeqResizing05Test extends LayoutTestCase {

    private Object changeMark;

    public ALT_SeqResizing05Test(String name) {
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
     * Resize jTextField2 to snap at container border.
     */
    public void doChanges0() {
        lm.setChangeRecording(true);
        changeMark = lm.getChangeMark();
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jTextField1", new Rectangle(0, 0, 400, 20));
        baselinePosition.put("jTextField1-400-20", 14);
        compBounds.put("jRadioButton1", new Rectangle(0, 22, 93, 23));
        baselinePosition.put("jRadioButton1-93-23", 15);
        compBounds.put("jRadioButton2", new Rectangle(0, 48, 93, 23));
        baselinePosition.put("jRadioButton2-93-23", 15);
        compBounds.put("jScrollPane1", new Rectangle(0, 73, 35, 130));
        baselinePosition.put("jScrollPane1-35-130", 0);
        compBounds.put("jLabel1", new Rectangle(41, 73, 34, 14));
        baselinePosition.put("jLabel1-34-14", 11);
        compBounds.put("jLabel2", new Rectangle(81, 73, 34, 14));
        baselinePosition.put("jLabel2-34-14", 11);
        compBounds.put("jTextField2", new Rectangle(41, 93, 59, 20));
        baselinePosition.put("jTextField2-59-20", 14);
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compPrefSize.put("jRadioButton1", new Dimension(93, 23));
        compPrefSize.put("jRadioButton2", new Dimension(93, 23));
        compPrefSize.put("jScrollPane1", new Dimension(35, 130));
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        compPrefSize.put("jLabel2", new Dimension(34, 14));
        compPrefSize.put("jTextField2", new Dimension(59, 20));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jTextField1", new Rectangle(0, 0, 400, 20));
        baselinePosition.put("jTextField1-400-20", 14);
        compBounds.put("jRadioButton1", new Rectangle(0, 22, 93, 23));
        baselinePosition.put("jRadioButton1-93-23", 15);
        compBounds.put("jRadioButton2", new Rectangle(0, 48, 93, 23));
        baselinePosition.put("jRadioButton2-93-23", 15);
        compBounds.put("jScrollPane1", new Rectangle(0, 73, 35, 130));
        baselinePosition.put("jScrollPane1-35-130", 0);
        compBounds.put("jLabel1", new Rectangle(41, 73, 34, 14));
        baselinePosition.put("jLabel1-34-14", 11);
        compBounds.put("jLabel2", new Rectangle(81, 73, 34, 14));
        baselinePosition.put("jLabel2-34-14", 11);
        compBounds.put("jTextField2", new Rectangle(41, 93, 59, 20));
        baselinePosition.put("jTextField2-59-20", 14);
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
        // > START RESIZING
        baselinePosition.put("jTextField2-59-20", 14);
        compPrefSize.put("jTextField2", new Dimension(59, 20));
        {
            String[] compIds = new String[]{"jTextField2"};
            Rectangle[] bounds = new Rectangle[]{new Rectangle(41, 93, 59, 20)};
            Point hotspot = new Point(102, 106);
            int[] resizeEdges = new int[]{1, -1};
            boolean inLayout = true;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
        // < START RESIZING
        prefPaddingInParent.put("Form-jTextField2-0-1", 10); // parentId-compId-dimension-compAlignment
        // > MOVE
        // > MOVE
        // > MOVE
        {
            Point p = new Point(399, 128);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(41, 93, 359, 20)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPaddingInParent.put("Form-jTextField2-0-1", 10); // parentId-compId-dimension-compAlignment
        // > MOVE
        // > MOVE
        // > MOVE
        {
            Point p = new Point(400, 128);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(41, 93, 359, 20)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jTextField1", new Rectangle(0, 0, 400, 20));
        baselinePosition.put("jTextField1-400-20", 14);
        compBounds.put("jRadioButton1", new Rectangle(0, 22, 93, 23));
        baselinePosition.put("jRadioButton1-93-23", 15);
        compBounds.put("jRadioButton2", new Rectangle(0, 48, 93, 23));
        baselinePosition.put("jRadioButton2-93-23", 15);
        compBounds.put("jScrollPane1", new Rectangle(0, 73, 35, 130));
        baselinePosition.put("jScrollPane1-35-130", 0);
        compBounds.put("jLabel1", new Rectangle(41, 73, 34, 14));
        baselinePosition.put("jLabel1-34-14", 11);
        compBounds.put("jLabel2", new Rectangle(81, 73, 34, 14));
        baselinePosition.put("jLabel2-34-14", 11);
        compBounds.put("jTextField2", new Rectangle(41, 93, 359, 20));
        baselinePosition.put("jTextField2-359-20", 14);
        prefPaddingInParent.put("Form-jRadioButton1-0-1", 6); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jRadioButton2-0-1", 6); // parentId-compId-dimension-compAlignment
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compPrefSize.put("jRadioButton1", new Dimension(93, 23));
        compPrefSize.put("jRadioButton2", new Dimension(93, 23));
        compPrefSize.put("jScrollPane1", new Dimension(35, 130));
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        compPrefSize.put("jLabel2", new Dimension(34, 14));
        compPrefSize.put("jTextField2", new Dimension(59, 20));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jTextField1", new Rectangle(0, 0, 400, 20));
        baselinePosition.put("jTextField1-400-20", 14);
        compBounds.put("jRadioButton1", new Rectangle(0, 22, 93, 23));
        baselinePosition.put("jRadioButton1-93-23", 15);
        compBounds.put("jRadioButton2", new Rectangle(0, 48, 93, 23));
        baselinePosition.put("jRadioButton2-93-23", 15);
        compBounds.put("jScrollPane1", new Rectangle(0, 73, 35, 130));
        baselinePosition.put("jScrollPane1-35-130", 0);
        compBounds.put("jLabel1", new Rectangle(41, 73, 34, 14));
        baselinePosition.put("jLabel1-34-14", 11);
        compBounds.put("jLabel2", new Rectangle(81, 73, 34, 14));
        baselinePosition.put("jLabel2-34-14", 11);
        compBounds.put("jTextField2", new Rectangle(41, 93, 359, 20));
        baselinePosition.put("jTextField2-359-20", 14);
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }

    /**
     * Undo previous resizing. Add a new label (jLabel3) vertically between
     * jTextField2 and the labels above it. This makes the parallel group
     * survive when jTextField2 is temporarily removed for resizing.
     * Resize jTextField2 to snap at container border.
     */
    public void doChanges1() {
        lm.undo(changeMark, lm.getChangeMark());
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jTextField1", new Rectangle(0, 0, 400, 20));
        baselinePosition.put("jTextField1-400-20", 14);
        compBounds.put("jRadioButton1", new Rectangle(0, 22, 93, 23));
        baselinePosition.put("jRadioButton1-93-23", 15);
        compBounds.put("jRadioButton2", new Rectangle(0, 48, 93, 23));
        baselinePosition.put("jRadioButton2-93-23", 15);
        compBounds.put("jScrollPane1", new Rectangle(0, 73, 35, 130));
        baselinePosition.put("jScrollPane1-35-130", 0);
        compBounds.put("jLabel1", new Rectangle(41, 73, 34, 14));
        baselinePosition.put("jLabel1-34-14", 11);
        compBounds.put("jLabel2", new Rectangle(81, 73, 34, 14));
        baselinePosition.put("jLabel2-34-14", 11);
        compBounds.put("jTextField2", new Rectangle(41, 93, 59, 20));
        baselinePosition.put("jTextField2-59-20", 14);
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
        lc = new LayoutComponent("jLabel3", false);
        // > START ADDING
        baselinePosition.put("jLabel3-34-14", 11);
        {
            LayoutComponent[] comps = new LayoutComponent[]{lc};
            Rectangle[] bounds = new Rectangle[]{new Rectangle(0, 0, 34, 14)};
            String defaultContId = null;
            Point hotspot = new Point(13, 7);
            ld.startAdding(comps, bounds, hotspot, defaultContId);
        }
        // < START ADDING
        prefPaddingInParent.put("Form-jLabel3-1-0", 11); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel3-1-1", 11); // parentId-compId-dimension-compAlignment
        prefPadding.put("jTextField1-jLabel3-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jTextField1-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jTextField1-1-0-1", 11); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jTextField1-1-0-2", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jTextField1-1-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jRadioButton1-jLabel3-1-0-1", 7); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jRadioButton1-1-0-0", 2); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jRadioButton2-jLabel3-1-0-0", 2); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jRadioButton2-1-0-1", 7); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jLabel3-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel3-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel1-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel2-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jLabel3-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jLabel3-1-0-1", 11); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jLabel3-1-0-2", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jLabel3-1-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jTextField2-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jLabel3-0-0", 10); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel3-0-1", 10); // parentId-compId-dimension-compAlignment
        prefPadding.put("jScrollPane1-jLabel3-0-0-0", 4); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jScrollPane1-0-0-0", 4); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jScrollPane1-0-0-1", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jScrollPane1-0-0-2", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jScrollPane1-0-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jLabel3-0-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel1-0-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel3-0-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel3-0-0-1", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel3-0-0-2", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel3-0-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel2-0-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jLabel3-0-0-0", 4); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jLabel3-0-0-1", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jLabel3-0-0-2", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jLabel3-0-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jTextField2-0-0-0", 4); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jRadioButton2-jLabel3-0-0-2", 21); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        // > MOVE
        // > MOVE
        {
            Point p = new Point(53, 91);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(39, 84, 34, 14)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPaddingInParent.put("Form-jLabel3-1-0", 11); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel3-1-1", 11); // parentId-compId-dimension-compAlignment
        prefPadding.put("jTextField1-jLabel3-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jTextField1-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jTextField1-1-0-1", 11); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jTextField1-1-0-2", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jTextField1-1-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jRadioButton1-jLabel3-1-0-1", 7); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jRadioButton1-1-0-0", 2); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jRadioButton2-jLabel3-1-0-0", 2); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jRadioButton2-1-0-1", 7); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jLabel3-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel3-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel1-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel2-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jLabel3-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jLabel3-1-0-1", 11); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jLabel3-1-0-2", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jLabel3-1-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jTextField2-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jLabel3-0-0", 10); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel3-0-1", 10); // parentId-compId-dimension-compAlignment
        prefPadding.put("jScrollPane1-jLabel3-0-0-0", 4); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jScrollPane1-0-0-0", 4); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jScrollPane1-0-0-1", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jScrollPane1-0-0-2", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jScrollPane1-0-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jLabel3-0-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel1-0-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel3-0-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel3-0-0-1", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel3-0-0-2", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel3-0-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel2-0-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jLabel3-0-0-0", 4); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jLabel3-0-0-1", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jLabel3-0-0-2", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jLabel3-0-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jTextField2-0-0-0", 4); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jRadioButton2-jLabel3-0-0-2", 21); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        // > MOVE
        // > MOVE
        {
            Point p = new Point(52, 91);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(39, 84, 34, 14)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        compPrefSize.put("jLabel3", new Dimension(34, 14));
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jTextField1", new Rectangle(0, 0, 400, 20));
        baselinePosition.put("jTextField1-400-20", 14);
        compBounds.put("jRadioButton1", new Rectangle(0, 22, 93, 23));
        baselinePosition.put("jRadioButton1-93-23", 15);
        compBounds.put("jRadioButton2", new Rectangle(0, 48, 93, 23));
        baselinePosition.put("jRadioButton2-93-23", 15);
        compBounds.put("jScrollPane1", new Rectangle(0, 73, 35, 130));
        baselinePosition.put("jScrollPane1-35-130", 0);
        compBounds.put("jLabel1", new Rectangle(41, 73, 34, 14));
        baselinePosition.put("jLabel1-34-14", 11);
        compBounds.put("jLabel2", new Rectangle(81, 73, 34, 14));
        baselinePosition.put("jLabel2-34-14", 11);
        compBounds.put("jTextField2", new Rectangle(41, 113, 59, 20));
        baselinePosition.put("jTextField2-59-20", 14);
        compBounds.put("jLabel3", new Rectangle(41, 93, 34, 14));
        baselinePosition.put("jLabel3-34-14", 11);
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compPrefSize.put("jRadioButton1", new Dimension(93, 23));
        compPrefSize.put("jRadioButton2", new Dimension(93, 23));
        compPrefSize.put("jScrollPane1", new Dimension(35, 130));
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        compPrefSize.put("jLabel2", new Dimension(34, 14));
        compPrefSize.put("jTextField2", new Dimension(59, 20));
        compPrefSize.put("jLabel3", new Dimension(34, 14));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jTextField1", new Rectangle(0, 0, 400, 20));
        baselinePosition.put("jTextField1-400-20", 14);
        compBounds.put("jRadioButton1", new Rectangle(0, 22, 93, 23));
        baselinePosition.put("jRadioButton1-93-23", 15);
        compBounds.put("jRadioButton2", new Rectangle(0, 48, 93, 23));
        baselinePosition.put("jRadioButton2-93-23", 15);
        compBounds.put("jScrollPane1", new Rectangle(0, 73, 35, 130));
        baselinePosition.put("jScrollPane1-35-130", 0);
        compBounds.put("jLabel1", new Rectangle(41, 73, 34, 14));
        baselinePosition.put("jLabel1-34-14", 11);
        compBounds.put("jLabel2", new Rectangle(81, 73, 34, 14));
        baselinePosition.put("jLabel2-34-14", 11);
        compBounds.put("jTextField2", new Rectangle(41, 113, 59, 20));
        baselinePosition.put("jTextField2-59-20", 14);
        compBounds.put("jLabel3", new Rectangle(41, 93, 34, 14));
        baselinePosition.put("jLabel3-34-14", 11);
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
        changeMark = lm.getChangeMark();
        // > START RESIZING
        baselinePosition.put("jTextField2-59-20", 14);
        compPrefSize.put("jTextField2", new Dimension(59, 20));
        {
            String[] compIds = new String[]{"jTextField2"};
            Rectangle[] bounds = new Rectangle[]{new Rectangle(41, 113, 59, 20)};
            Point hotspot = new Point(99, 126);
            int[] resizeEdges = new int[]{1, -1};
            boolean inLayout = true;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
        // < START RESIZING
        prefPaddingInParent.put("Form-jTextField2-0-1", 10); // parentId-compId-dimension-compAlignment
        // > MOVE
        // > MOVE
        // > MOVE
        {
            Point p = new Point(398, 133);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(41, 113, 359, 20)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPaddingInParent.put("Form-jTextField2-0-1", 10); // parentId-compId-dimension-compAlignment
        // > MOVE
        // > MOVE
        // > MOVE
        {
            Point p = new Point(399, 133);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(41, 113, 359, 20)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jTextField1", new Rectangle(0, 0, 400, 20));
        baselinePosition.put("jTextField1-400-20", 14);
        compBounds.put("jRadioButton1", new Rectangle(0, 22, 93, 23));
        baselinePosition.put("jRadioButton1-93-23", 15);
        compBounds.put("jRadioButton2", new Rectangle(0, 48, 93, 23));
        baselinePosition.put("jRadioButton2-93-23", 15);
        compBounds.put("jScrollPane1", new Rectangle(0, 73, 35, 130));
        baselinePosition.put("jScrollPane1-35-130", 0);
        compBounds.put("jLabel1", new Rectangle(41, 73, 34, 14));
        baselinePosition.put("jLabel1-34-14", 11);
        compBounds.put("jLabel2", new Rectangle(81, 73, 34, 14));
        baselinePosition.put("jLabel2-34-14", 11);
        compBounds.put("jTextField2", new Rectangle(41, 113, 359, 20));
        baselinePosition.put("jTextField2-359-20", 14);
        compBounds.put("jLabel3", new Rectangle(41, 93, 34, 14));
        baselinePosition.put("jLabel3-34-14", 11);
        prefPaddingInParent.put("Form-jRadioButton1-0-1", 6); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jRadioButton2-0-1", 6); // parentId-compId-dimension-compAlignment
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compPrefSize.put("jRadioButton1", new Dimension(93, 23));
        compPrefSize.put("jRadioButton2", new Dimension(93, 23));
        compPrefSize.put("jScrollPane1", new Dimension(35, 130));
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        compPrefSize.put("jLabel2", new Dimension(34, 14));
        compPrefSize.put("jTextField2", new Dimension(59, 20));
        compPrefSize.put("jLabel3", new Dimension(34, 14));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jTextField1", new Rectangle(0, 0, 400, 20));
        baselinePosition.put("jTextField1-400-20", 14);
        compBounds.put("jRadioButton1", new Rectangle(0, 22, 93, 23));
        baselinePosition.put("jRadioButton1-93-23", 15);
        compBounds.put("jRadioButton2", new Rectangle(0, 48, 93, 23));
        baselinePosition.put("jRadioButton2-93-23", 15);
        compBounds.put("jScrollPane1", new Rectangle(0, 73, 35, 130));
        baselinePosition.put("jScrollPane1-35-130", 0);
        compBounds.put("jLabel1", new Rectangle(41, 73, 34, 14));
        baselinePosition.put("jLabel1-34-14", 11);
        compBounds.put("jLabel2", new Rectangle(81, 73, 34, 14));
        baselinePosition.put("jLabel2-34-14", 11);
        compBounds.put("jTextField2", new Rectangle(41, 113, 359, 20));
        baselinePosition.put("jTextField2-359-20", 14);
        compBounds.put("jLabel3", new Rectangle(41, 93, 34, 14));
        baselinePosition.put("jLabel3-34-14", 11);
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }

    /**
     * Undo previous resizing. Add another label (jLabel4) on the place of
     * jTextField2 to be left of it in a sequence. This causes that a neighbor
     * must be considered in the initial jTextField2's position when resized.
     * Resize jTextField2 to snap at container border.
     */
    public void doChanges2() {
        lm.undo(changeMark, lm.getChangeMark());
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jTextField1", new Rectangle(0, 0, 400, 20));
        baselinePosition.put("jTextField1-400-20", 14);
        compBounds.put("jRadioButton1", new Rectangle(0, 22, 93, 23));
        baselinePosition.put("jRadioButton1-93-23", 15);
        compBounds.put("jRadioButton2", new Rectangle(0, 48, 93, 23));
        baselinePosition.put("jRadioButton2-93-23", 15);
        compBounds.put("jScrollPane1", new Rectangle(0, 73, 35, 130));
        baselinePosition.put("jScrollPane1-35-130", 0);
        compBounds.put("jLabel1", new Rectangle(41, 73, 34, 14));
        baselinePosition.put("jLabel1-34-14", 11);
        compBounds.put("jLabel2", new Rectangle(81, 73, 34, 14));
        baselinePosition.put("jLabel2-34-14", 11);
        compBounds.put("jTextField2", new Rectangle(41, 113, 59, 20));
        baselinePosition.put("jTextField2-59-20", 14);
        compBounds.put("jLabel3", new Rectangle(41, 93, 34, 14));
        baselinePosition.put("jLabel3-34-14", 11);
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
        lc = new LayoutComponent("jLabel4", false);
        // > START ADDING
        baselinePosition.put("jLabel4-34-14", 11);
        {
            LayoutComponent[] comps = new LayoutComponent[]{lc};
            Rectangle[] bounds = new Rectangle[]{new Rectangle(0, 0, 34, 14)};
            String defaultContId = null;
            Point hotspot = new Point(13, 7);
            ld.startAdding(comps, bounds, hotspot, defaultContId);
        }
        // < START ADDING
        prefPaddingInParent.put("Form-jLabel4-1-0", 11); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel4-1-1", 11); // parentId-compId-dimension-compAlignment
        prefPadding.put("jTextField1-jLabel4-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jTextField1-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jTextField1-1-0-1", 11); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jTextField1-1-0-2", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jTextField1-1-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jRadioButton1-jLabel4-1-0-1", 7); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jRadioButton1-1-0-0", 2); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jRadioButton2-jLabel4-1-0-0", 2); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jRadioButton2-1-0-1", 7); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jLabel4-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel4-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel1-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel2-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel4-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel3-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jLabel4-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jLabel4-1-0-1", 11); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jLabel4-1-0-2", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jLabel4-1-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jTextField2-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jLabel4-0-0", 10); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel4-0-1", 10); // parentId-compId-dimension-compAlignment
        prefPadding.put("jScrollPane1-jLabel4-0-0-0", 4); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jScrollPane1-0-0-0", 4); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jScrollPane1-0-0-1", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jScrollPane1-0-0-2", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jScrollPane1-0-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jLabel4-0-0-0", 4); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jLabel4-0-0-1", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jLabel4-0-0-2", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jLabel4-0-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jTextField2-0-0-0", 4); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel4-0-0-2", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        // > MOVE
        // > MOVE
        {
            Point p = new Point(59, 120);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(39, 116, 34, 14)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPaddingInParent.put("Form-jLabel4-1-0", 11); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel4-1-1", 11); // parentId-compId-dimension-compAlignment
        prefPadding.put("jTextField1-jLabel4-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jTextField1-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jTextField1-1-0-1", 11); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jTextField1-1-0-2", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jTextField1-1-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jRadioButton1-jLabel4-1-0-1", 7); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jRadioButton1-1-0-0", 2); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jRadioButton2-jLabel4-1-0-0", 2); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jRadioButton2-1-0-1", 7); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jLabel4-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel4-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel1-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel2-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel4-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel3-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jLabel4-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jLabel4-1-0-1", 11); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jLabel4-1-0-2", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jLabel4-1-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jTextField2-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jLabel4-0-0", 10); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel4-0-1", 10); // parentId-compId-dimension-compAlignment
        prefPadding.put("jScrollPane1-jLabel4-0-0-0", 4); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jScrollPane1-0-0-0", 4); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jScrollPane1-0-0-1", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jScrollPane1-0-0-2", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jScrollPane1-0-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jLabel4-0-0-0", 4); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jLabel4-0-0-1", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jLabel4-0-0-2", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jLabel4-0-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jTextField2-0-0-0", 4); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel4-0-0-2", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        // > MOVE
        // > MOVE
        {
            Point p = new Point(60, 120);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(41, 116, 34, 14)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        compPrefSize.put("jLabel4", new Dimension(34, 14));
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jTextField1", new Rectangle(0, 0, 400, 20));
        baselinePosition.put("jTextField1-400-20", 14);
        compBounds.put("jRadioButton1", new Rectangle(0, 22, 93, 23));
        baselinePosition.put("jRadioButton1-93-23", 15);
        compBounds.put("jRadioButton2", new Rectangle(0, 48, 93, 23));
        baselinePosition.put("jRadioButton2-93-23", 15);
        compBounds.put("jScrollPane1", new Rectangle(0, 73, 35, 130));
        baselinePosition.put("jScrollPane1-35-130", 0);
        compBounds.put("jLabel1", new Rectangle(39, 73, 34, 14));
        baselinePosition.put("jLabel1-34-14", 11);
        compBounds.put("jLabel2", new Rectangle(79, 73, 34, 14));
        baselinePosition.put("jLabel2-34-14", 11);
        compBounds.put("jTextField2", new Rectangle(77, 113, 59, 20));
        baselinePosition.put("jTextField2-59-20", 14);
        compBounds.put("jLabel3", new Rectangle(39, 93, 34, 14));
        baselinePosition.put("jLabel3-34-14", 11);
        compBounds.put("jLabel4", new Rectangle(39, 116, 34, 14));
        baselinePosition.put("jLabel4-34-14", 11);
        prefPaddingInParent.put("Form-jRadioButton1-0-1", 6); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jRadioButton2-0-1", 6); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel3-0-1", 10); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel2-0-1", 10); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jTextField2-0-1", 10); // parentId-compId-dimension-compAlignment
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compPrefSize.put("jRadioButton1", new Dimension(93, 23));
        compPrefSize.put("jRadioButton2", new Dimension(93, 23));
        compPrefSize.put("jScrollPane1", new Dimension(35, 130));
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        compPrefSize.put("jLabel2", new Dimension(34, 14));
        compPrefSize.put("jTextField2", new Dimension(59, 20));
        compPrefSize.put("jLabel3", new Dimension(34, 14));
        compPrefSize.put("jLabel4", new Dimension(34, 14));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jTextField1", new Rectangle(0, 0, 400, 20));
        baselinePosition.put("jTextField1-400-20", 14);
        compBounds.put("jRadioButton1", new Rectangle(0, 22, 93, 23));
        baselinePosition.put("jRadioButton1-93-23", 15);
        compBounds.put("jRadioButton2", new Rectangle(0, 48, 93, 23));
        baselinePosition.put("jRadioButton2-93-23", 15);
        compBounds.put("jScrollPane1", new Rectangle(0, 73, 35, 130));
        baselinePosition.put("jScrollPane1-35-130", 0);
        compBounds.put("jLabel1", new Rectangle(39, 73, 34, 14));
        baselinePosition.put("jLabel1-34-14", 11);
        compBounds.put("jLabel2", new Rectangle(79, 73, 34, 14));
        baselinePosition.put("jLabel2-34-14", 11);
        compBounds.put("jTextField2", new Rectangle(77, 113, 59, 20));
        baselinePosition.put("jTextField2-59-20", 14);
        compBounds.put("jLabel3", new Rectangle(39, 93, 34, 14));
        baselinePosition.put("jLabel3-34-14", 11);
        compBounds.put("jLabel4", new Rectangle(39, 116, 34, 14));
        baselinePosition.put("jLabel4-34-14", 11);
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
        changeMark = lm.getChangeMark();
        // > START RESIZING
        baselinePosition.put("jTextField2-59-20", 14);
        compPrefSize.put("jTextField2", new Dimension(59, 20));
        {
            String[] compIds = new String[]{"jTextField2"};
            Rectangle[] bounds = new Rectangle[]{new Rectangle(77, 113, 59, 20)};
            Point hotspot = new Point(135, 120);
            int[] resizeEdges = new int[]{1, -1};
            boolean inLayout = true;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
        // < START RESIZING
        prefPaddingInParent.put("Form-jTextField2-0-1", 10); // parentId-compId-dimension-compAlignment
        // > MOVE
        // > MOVE
        // > MOVE
        {
            Point p = new Point(397, 141);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(77, 113, 323, 20)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPaddingInParent.put("Form-jTextField2-0-1", 10); // parentId-compId-dimension-compAlignment
        // > MOVE
        // > MOVE
        // > MOVE
        {
            Point p = new Point(398, 141);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(77, 113, 323, 20)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jTextField1", new Rectangle(0, 0, 400, 20));
        baselinePosition.put("jTextField1-400-20", 14);
        compBounds.put("jRadioButton1", new Rectangle(0, 22, 93, 23));
        baselinePosition.put("jRadioButton1-93-23", 15);
        compBounds.put("jRadioButton2", new Rectangle(0, 48, 93, 23));
        baselinePosition.put("jRadioButton2-93-23", 15);
        compBounds.put("jScrollPane1", new Rectangle(0, 73, 35, 130));
        baselinePosition.put("jScrollPane1-35-130", 0);
        compBounds.put("jLabel1", new Rectangle(39, 73, 34, 14));
        baselinePosition.put("jLabel1-34-14", 11);
        compBounds.put("jLabel2", new Rectangle(79, 73, 34, 14));
        baselinePosition.put("jLabel2-34-14", 11);
        compBounds.put("jTextField2", new Rectangle(77, 113, 323, 20));
        baselinePosition.put("jTextField2-323-20", 14);
        compBounds.put("jLabel3", new Rectangle(39, 93, 34, 14));
        baselinePosition.put("jLabel3-34-14", 11);
        compBounds.put("jLabel4", new Rectangle(39, 116, 34, 14));
        baselinePosition.put("jLabel4-34-14", 11);
        prefPaddingInParent.put("Form-jRadioButton1-0-1", 6); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jRadioButton2-0-1", 6); // parentId-compId-dimension-compAlignment
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compPrefSize.put("jRadioButton1", new Dimension(93, 23));
        compPrefSize.put("jRadioButton2", new Dimension(93, 23));
        compPrefSize.put("jScrollPane1", new Dimension(35, 130));
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        compPrefSize.put("jLabel2", new Dimension(34, 14));
        compPrefSize.put("jTextField2", new Dimension(59, 20));
        compPrefSize.put("jLabel3", new Dimension(34, 14));
        compPrefSize.put("jLabel4", new Dimension(34, 14));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jTextField1", new Rectangle(0, 0, 400, 20));
        baselinePosition.put("jTextField1-400-20", 14);
        compBounds.put("jRadioButton1", new Rectangle(0, 22, 93, 23));
        baselinePosition.put("jRadioButton1-93-23", 15);
        compBounds.put("jRadioButton2", new Rectangle(0, 48, 93, 23));
        baselinePosition.put("jRadioButton2-93-23", 15);
        compBounds.put("jScrollPane1", new Rectangle(0, 73, 35, 130));
        baselinePosition.put("jScrollPane1-35-130", 0);
        compBounds.put("jLabel1", new Rectangle(39, 73, 34, 14));
        baselinePosition.put("jLabel1-34-14", 11);
        compBounds.put("jLabel2", new Rectangle(79, 73, 34, 14));
        baselinePosition.put("jLabel2-34-14", 11);
        compBounds.put("jTextField2", new Rectangle(77, 113, 323, 20));
        baselinePosition.put("jTextField2-323-20", 14);
        compBounds.put("jLabel3", new Rectangle(39, 93, 34, 14));
        baselinePosition.put("jLabel3-34-14", 11);
        compBounds.put("jLabel4", new Rectangle(39, 116, 34, 14));
        baselinePosition.put("jLabel4-34-14", 11);
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }

    /**
     * Undo previous resizing. Add another label (jLabel5) between jLabel4 and
     * jTextField2. This causes that initial position of the jTextField2 for
     * resizing will be inside a sequence.
     * Resize jTextField2 to snap at container border.
     */
    public void doChanges3() {
        lm.undo(changeMark, lm.getChangeMark());
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jTextField1", new Rectangle(0, 0, 400, 20));
        baselinePosition.put("jTextField1-400-20", 14);
        compBounds.put("jRadioButton1", new Rectangle(0, 22, 93, 23));
        baselinePosition.put("jRadioButton1-93-23", 15);
        compBounds.put("jRadioButton2", new Rectangle(0, 48, 93, 23));
        baselinePosition.put("jRadioButton2-93-23", 15);
        compBounds.put("jScrollPane1", new Rectangle(0, 73, 35, 130));
        baselinePosition.put("jScrollPane1-35-130", 0);
        compBounds.put("jLabel1", new Rectangle(39, 73, 34, 14));
        baselinePosition.put("jLabel1-34-14", 11);
        compBounds.put("jLabel2", new Rectangle(79, 73, 34, 14));
        baselinePosition.put("jLabel2-34-14", 11);
        compBounds.put("jTextField2", new Rectangle(77, 113, 59, 20));
        baselinePosition.put("jTextField2-59-20", 14);
        compBounds.put("jLabel3", new Rectangle(39, 93, 34, 14));
        baselinePosition.put("jLabel3-34-14", 11);
        compBounds.put("jLabel4", new Rectangle(39, 116, 34, 14));
        baselinePosition.put("jLabel4-34-14", 11);
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
        lc = new LayoutComponent("jLabel5", false);
        // > START ADDING
        baselinePosition.put("jLabel5-34-14", 11);
        {
            LayoutComponent[] comps = new LayoutComponent[]{lc};
            Rectangle[] bounds = new Rectangle[]{new Rectangle(0, 0, 34, 14)};
            String defaultContId = null;
            Point hotspot = new Point(13, 7);
            ld.startAdding(comps, bounds, hotspot, defaultContId);
        }
        // < START ADDING
        prefPaddingInParent.put("Form-jLabel5-1-0", 11); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel5-1-1", 11); // parentId-compId-dimension-compAlignment
        prefPadding.put("jTextField1-jLabel5-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel5-jTextField1-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel5-jTextField1-1-0-1", 11); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel5-jTextField1-1-0-2", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel5-jTextField1-1-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jRadioButton1-jLabel5-1-0-1", 7); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel5-jRadioButton1-1-0-0", 2); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jRadioButton2-jLabel5-1-0-0", 2); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel5-jRadioButton2-1-0-1", 7); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jLabel5-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel5-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel5-jLabel1-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel5-jLabel2-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel5-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel5-jLabel3-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jLabel5-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jLabel5-1-0-1", 11); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jLabel5-1-0-2", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jLabel5-1-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel5-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel5-1-0-1", 11); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel5-1-0-2", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel5-1-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel5-jTextField2-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel5-jLabel4-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jLabel5-0-0", 10); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel5-0-1", 10); // parentId-compId-dimension-compAlignment
        prefPadding.put("jScrollPane1-jLabel5-0-0-0", 4); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel5-jScrollPane1-0-0-0", 4); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel5-jScrollPane1-0-0-1", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel5-jScrollPane1-0-0-2", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel5-jScrollPane1-0-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel5-0-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel5-jLabel4-0-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jLabel5-0-0-0", 4); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jLabel5-0-0-1", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jLabel5-0-0-2", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jLabel5-0-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel5-jTextField2-0-0-0", 4); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel5-0-0-2", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        // > MOVE
        // > MOVE
        {
            Point p = new Point(72, 125);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(59, 116, 34, 14)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPaddingInParent.put("Form-jLabel5-1-0", 11); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel5-1-1", 11); // parentId-compId-dimension-compAlignment
        prefPadding.put("jTextField1-jLabel5-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel5-jTextField1-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel5-jTextField1-1-0-1", 11); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel5-jTextField1-1-0-2", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel5-jTextField1-1-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jRadioButton1-jLabel5-1-0-1", 7); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel5-jRadioButton1-1-0-0", 2); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jRadioButton2-jLabel5-1-0-0", 2); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel5-jRadioButton2-1-0-1", 7); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jLabel5-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel5-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel5-jLabel1-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel5-jLabel2-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel5-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel5-jLabel3-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jLabel5-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jLabel5-1-0-1", 11); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jLabel5-1-0-2", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jLabel5-1-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel5-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel5-1-0-1", 11); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel5-1-0-2", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel5-1-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel5-jTextField2-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel5-jLabel4-1-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jLabel5-0-0", 10); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel5-0-1", 10); // parentId-compId-dimension-compAlignment
        prefPadding.put("jScrollPane1-jLabel5-0-0-0", 4); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel5-jScrollPane1-0-0-0", 4); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel5-jScrollPane1-0-0-1", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel5-jScrollPane1-0-0-2", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel5-jScrollPane1-0-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel5-0-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel5-jLabel4-0-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jLabel5-0-0-0", 4); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jLabel5-0-0-1", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jLabel5-0-0-2", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jLabel5-0-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel5-jTextField2-0-0-0", 4); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel5-0-0-2", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        // > MOVE
        // > MOVE
        {
            Point p = new Point(73, 125);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(60, 116, 34, 14)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        compPrefSize.put("jLabel5", new Dimension(34, 14));
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jTextField1", new Rectangle(0, 0, 400, 20));
        baselinePosition.put("jTextField1-400-20", 14);
        compBounds.put("jRadioButton1", new Rectangle(0, 22, 93, 23));
        baselinePosition.put("jRadioButton1-93-23", 15);
        compBounds.put("jRadioButton2", new Rectangle(0, 48, 93, 23));
        baselinePosition.put("jRadioButton2-93-23", 15);
        compBounds.put("jScrollPane1", new Rectangle(0, 73, 35, 130));
        baselinePosition.put("jScrollPane1-35-130", 0);
        compBounds.put("jLabel1", new Rectangle(39, 73, 34, 14));
        baselinePosition.put("jLabel1-34-14", 11);
        compBounds.put("jLabel2", new Rectangle(79, 73, 34, 14));
        baselinePosition.put("jLabel2-34-14", 11);
        compBounds.put("jTextField2", new Rectangle(117, 113, 59, 20));
        baselinePosition.put("jTextField2-59-20", 14);
        compBounds.put("jLabel3", new Rectangle(39, 93, 34, 14));
        baselinePosition.put("jLabel3-34-14", 11);
        compBounds.put("jLabel4", new Rectangle(39, 116, 34, 14));
        baselinePosition.put("jLabel4-34-14", 11);
        compBounds.put("jLabel5", new Rectangle(79, 116, 34, 14));
        baselinePosition.put("jLabel5-34-14", 11);
        prefPaddingInParent.put("Form-jRadioButton1-0-1", 6); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jRadioButton2-0-1", 6); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel3-0-1", 10); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel2-0-1", 10); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jTextField2-0-1", 10); // parentId-compId-dimension-compAlignment
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compPrefSize.put("jRadioButton1", new Dimension(93, 23));
        compPrefSize.put("jRadioButton2", new Dimension(93, 23));
        compPrefSize.put("jScrollPane1", new Dimension(35, 130));
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        compPrefSize.put("jLabel2", new Dimension(34, 14));
        compPrefSize.put("jTextField2", new Dimension(59, 20));
        compPrefSize.put("jLabel3", new Dimension(34, 14));
        compPrefSize.put("jLabel4", new Dimension(34, 14));
        compPrefSize.put("jLabel5", new Dimension(34, 14));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jTextField1", new Rectangle(0, 0, 400, 20));
        baselinePosition.put("jTextField1-400-20", 14);
        compBounds.put("jRadioButton1", new Rectangle(0, 22, 93, 23));
        baselinePosition.put("jRadioButton1-93-23", 15);
        compBounds.put("jRadioButton2", new Rectangle(0, 48, 93, 23));
        baselinePosition.put("jRadioButton2-93-23", 15);
        compBounds.put("jScrollPane1", new Rectangle(0, 73, 35, 130));
        baselinePosition.put("jScrollPane1-35-130", 0);
        compBounds.put("jLabel1", new Rectangle(39, 73, 34, 14));
        baselinePosition.put("jLabel1-34-14", 11);
        compBounds.put("jLabel2", new Rectangle(79, 73, 34, 14));
        baselinePosition.put("jLabel2-34-14", 11);
        compBounds.put("jTextField2", new Rectangle(117, 113, 59, 20));
        baselinePosition.put("jTextField2-59-20", 14);
        compBounds.put("jLabel3", new Rectangle(39, 93, 34, 14));
        baselinePosition.put("jLabel3-34-14", 11);
        compBounds.put("jLabel4", new Rectangle(39, 116, 34, 14));
        baselinePosition.put("jLabel4-34-14", 11);
        compBounds.put("jLabel5", new Rectangle(79, 116, 34, 14));
        baselinePosition.put("jLabel5-34-14", 11);
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
        // > START RESIZING
        baselinePosition.put("jTextField2-59-20", 14);
        compPrefSize.put("jTextField2", new Dimension(59, 20));
        {
            String[] compIds = new String[]{"jTextField2"};
            Rectangle[] bounds = new Rectangle[]{new Rectangle(117, 113, 59, 20)};
            Point hotspot = new Point(175, 125);
            int[] resizeEdges = new int[]{1, -1};
            boolean inLayout = true;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
        // < START RESIZING
        prefPaddingInParent.put("Form-jTextField2-0-1", 10); // parentId-compId-dimension-compAlignment
        // > MOVE
        // > MOVE
        // > MOVE
        {
            Point p = new Point(398, 137);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(117, 113, 283, 20)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPaddingInParent.put("Form-jTextField2-0-1", 10); // parentId-compId-dimension-compAlignment
        // > MOVE
        // > MOVE
        // > MOVE
        {
            Point p = new Point(399, 137);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(117, 113, 283, 20)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jTextField1", new Rectangle(0, 0, 400, 20));
        baselinePosition.put("jTextField1-400-20", 14);
        compBounds.put("jRadioButton1", new Rectangle(0, 22, 93, 23));
        baselinePosition.put("jRadioButton1-93-23", 15);
        compBounds.put("jRadioButton2", new Rectangle(0, 48, 93, 23));
        baselinePosition.put("jRadioButton2-93-23", 15);
        compBounds.put("jScrollPane1", new Rectangle(0, 73, 35, 130));
        baselinePosition.put("jScrollPane1-35-130", 0);
        compBounds.put("jLabel1", new Rectangle(39, 73, 34, 14));
        baselinePosition.put("jLabel1-34-14", 11);
        compBounds.put("jLabel2", new Rectangle(79, 73, 34, 14));
        baselinePosition.put("jLabel2-34-14", 11);
        compBounds.put("jTextField2", new Rectangle(117, 113, 283, 20));
        baselinePosition.put("jTextField2-283-20", 14);
        compBounds.put("jLabel3", new Rectangle(39, 93, 34, 14));
        baselinePosition.put("jLabel3-34-14", 11);
        compBounds.put("jLabel4", new Rectangle(39, 116, 34, 14));
        baselinePosition.put("jLabel4-34-14", 11);
        compBounds.put("jLabel5", new Rectangle(79, 116, 34, 14));
        baselinePosition.put("jLabel5-34-14", 11);
        prefPaddingInParent.put("Form-jRadioButton1-0-1", 6); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jRadioButton2-0-1", 6); // parentId-compId-dimension-compAlignment
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compPrefSize.put("jRadioButton1", new Dimension(93, 23));
        compPrefSize.put("jRadioButton2", new Dimension(93, 23));
        compPrefSize.put("jScrollPane1", new Dimension(35, 130));
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        compPrefSize.put("jLabel2", new Dimension(34, 14));
        compPrefSize.put("jTextField2", new Dimension(59, 20));
        compPrefSize.put("jLabel3", new Dimension(34, 14));
        compPrefSize.put("jLabel4", new Dimension(34, 14));
        compPrefSize.put("jLabel5", new Dimension(34, 14));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jTextField1", new Rectangle(0, 0, 400, 20));
        baselinePosition.put("jTextField1-400-20", 14);
        compBounds.put("jRadioButton1", new Rectangle(0, 22, 93, 23));
        baselinePosition.put("jRadioButton1-93-23", 15);
        compBounds.put("jRadioButton2", new Rectangle(0, 48, 93, 23));
        baselinePosition.put("jRadioButton2-93-23", 15);
        compBounds.put("jScrollPane1", new Rectangle(0, 73, 35, 130));
        baselinePosition.put("jScrollPane1-35-130", 0);
        compBounds.put("jLabel1", new Rectangle(39, 73, 34, 14));
        baselinePosition.put("jLabel1-34-14", 11);
        compBounds.put("jLabel2", new Rectangle(79, 73, 34, 14));
        baselinePosition.put("jLabel2-34-14", 11);
        compBounds.put("jTextField2", new Rectangle(117, 113, 283, 20));
        baselinePosition.put("jTextField2-283-20", 14);
        compBounds.put("jLabel3", new Rectangle(39, 93, 34, 14));
        baselinePosition.put("jLabel3-34-14", 11);
        compBounds.put("jLabel4", new Rectangle(39, 116, 34, 14));
        baselinePosition.put("jLabel4-34-14", 11);
        compBounds.put("jLabel5", new Rectangle(79, 116, 34, 14));
        baselinePosition.put("jLabel5-34-14", 11);
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }

}
