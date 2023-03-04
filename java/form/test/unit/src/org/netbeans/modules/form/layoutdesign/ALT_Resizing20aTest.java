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

/**
 * Inspired from the bug 213119. The choice1 component is placed incorrectly in
 * the horizontal dimension where it should be in sequence also with textArea1
 * but is not. There are also zero size canvas1 and panel1 in the top left corner.
 * Each tested change is undone before next one starts, i.e. always starting
 * from the initial state.
 */
public class ALT_Resizing20aTest extends LayoutTestCase {

    private Object changeMark;

    public ALT_Resizing20aTest(String name) {
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
     * Resize choice1 slightly to the right.
     * No change should happen in either dimension, except changing the
     * horizontal size of the resized component. I.e. the horizontal layout
     * should not be fixed.
     */
    public void doChanges0() {
        lm.setChangeRecording(true);
        changeMark = lm.getChangeMark(); 
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 1338, 376));
        contInterior.put("Form", new Rectangle(0, 0, 1338, 376));
        compBounds.put("panel1", new Rectangle(0, 0, 0, 0));
        baselinePosition.put("panel1-0-0", new Integer(0));
        compBounds.put("canvas1", new Rectangle(0, 0, 0, 0));
        baselinePosition.put("canvas1-0-0", new Integer(0));
        compBounds.put("list1", new Rectangle(53, 26, 40, 200));
        baselinePosition.put("list1-40-200", new Integer(0));
        compBounds.put("checkbox1", new Rectangle(10, 346, 109, 20));
        baselinePosition.put("checkbox1-109-20", new Integer(0));
        compBounds.put("button1", new Rectangle(129, 304, 98, 62));
        baselinePosition.put("button1-98-62", new Integer(0));
        compBounds.put("label1", new Rectangle(169, 26, 38, 20));
        baselinePosition.put("label1-38-20", new Integer(0));
        compBounds.put("scrollbar1", new Rectangle(191, 101, 16, 48));
        baselinePosition.put("scrollbar1-16-48", new Integer(0));
        compBounds.put("scrollbar2", new Rectangle(279, 26, 48, 16));
        baselinePosition.put("scrollbar2-48-16", new Integer(0));
        compBounds.put("scrollPane1", new Rectangle(382, 26, 100, 100));
        baselinePosition.put("scrollPane1-100-100", new Integer(0));
        compBounds.put("choice1", new Rectangle(492, 80, 28, 200));
        baselinePosition.put("choice1-28-200", new Integer(0));
        compBounds.put("textArea1", new Rectangle(129, 200, 245, 80));
        baselinePosition.put("textArea1-245-80", new Integer(0));
        compMinSize.put("Form", new Dimension(1231, 362));
        compBounds.put("Form", new Rectangle(0, 0, 1338, 376));
        compBounds.put("Form", new Rectangle(0, 0, 1338, 376));
        compBounds.put("Form", new Rectangle(0, 0, 1338, 376));
        contInterior.put("panel1", new Rectangle(0, 0, 0, 0));
        compMinSize.put("panel1", new Dimension(0, 0));
        compBounds.put("panel1", new Rectangle(0, 0, 0, 0));
        compPrefSize.put("panel1", new Dimension(0, 0));
        hasExplicitPrefSize.put("panel1", new Boolean(false));
        hasExplicitPrefSize.put("panel1", new Boolean(false));
        prefPadding.put("scrollbar2-textArea1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("scrollbar1-textArea1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("choice1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("choice1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
        prefPadding.put("choice1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("choice1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-checkbox1-1-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-button1-1-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("scrollbar2-textArea1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("scrollbar1-textArea1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-checkbox1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        compBounds.put("panel1", new Rectangle(0, 0, 0, 0));
        compBounds.put("canvas1", new Rectangle(0, 0, 0, 0));
        prefPadding.put("choice1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("choice1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
// > START RESIZING
        baselinePosition.put("choice1-28-200", new Integer(0));
        compPrefSize.put("choice1", new Dimension(28, 20));
        {
            String[] compIds = new String[]{
                "choice1"
            };
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(492, 80, 28, 200)
            };
            Point hotspot = new Point(525, 182);
            int[] resizeEdges = new int[]{
                1,
                -1
            };
            boolean inLayout = true;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
// < START RESIZING
        prefPaddingInParent.put("Form-choice1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
// > MOVE
        {
            Point p = new Point(548, 179);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(492, 80, 51, 200)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
        prefPaddingInParent.put("Form-choice1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
// > MOVE
        {
            Point p = new Point(549, 179);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(492, 80, 52, 200)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
// > END MOVING
        prefPadding.put("scrollbar2-textArea1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("scrollbar1-textArea1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-panel1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-canvas1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-button1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-textArea1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-choice1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        ld.endMoving(true);
// < END MOVING
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 1338, 376));
        contInterior.put("Form", new Rectangle(0, 0, 1338, 376));
        compBounds.put("panel1", new Rectangle(0, 0, 0, 0));
        baselinePosition.put("panel1-0-0", new Integer(0));
        compBounds.put("canvas1", new Rectangle(0, 0, 0, 0));
        baselinePosition.put("canvas1-0-0", new Integer(0));
        compBounds.put("list1", new Rectangle(53, 26, 40, 200));
        baselinePosition.put("list1-40-200", new Integer(0));
        compBounds.put("checkbox1", new Rectangle(10, 346, 109, 20));
        baselinePosition.put("checkbox1-109-20", new Integer(0));
        compBounds.put("button1", new Rectangle(129, 304, 98, 62));
        baselinePosition.put("button1-98-62", new Integer(0));
        compBounds.put("label1", new Rectangle(169, 26, 38, 20));
        baselinePosition.put("label1-38-20", new Integer(0));
        compBounds.put("scrollbar1", new Rectangle(191, 101, 16, 48));
        baselinePosition.put("scrollbar1-16-48", new Integer(0));
        compBounds.put("scrollbar2", new Rectangle(279, 26, 48, 16));
        baselinePosition.put("scrollbar2-48-16", new Integer(0));
        compBounds.put("scrollPane1", new Rectangle(382, 26, 100, 100));
        baselinePosition.put("scrollPane1-100-100", new Integer(0));
        compBounds.put("choice1", new Rectangle(492, 80, 52, 200));
        baselinePosition.put("choice1-52-200", new Integer(0));
        compBounds.put("textArea1", new Rectangle(129, 200, 245, 80));
        baselinePosition.put("textArea1-245-80", new Integer(0));
        compMinSize.put("Form", new Dimension(1293, 362));
        compBounds.put("Form", new Rectangle(0, 0, 1338, 376));
        contInterior.put("panel1", new Rectangle(0, 0, 0, 0));
        compMinSize.put("panel1", new Dimension(0, 0));
        compBounds.put("panel1", new Rectangle(0, 0, 0, 0));
        compPrefSize.put("panel1", new Dimension(0, 0));
        hasExplicitPrefSize.put("panel1", new Boolean(false));
        hasExplicitPrefSize.put("panel1", new Boolean(false));
        prefPadding.put("scrollbar2-textArea1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("scrollbar1-textArea1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("choice1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("choice1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 1338, 376));
        prefPadding.put("choice1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("choice1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
    }

    /**
     * Undo previous change. Resize choice1 slightly to the left (over scrollPane1).
     * Except changing the size, the horizontal layout should be fixed so choice1
     * is in sequence with both scrollPane1 and textArea1.
     */
    public void doChanges1() {
        lm.undo(changeMark, lm.getChangeMark());
        changeMark = lm.getChangeMark(); 
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 1338, 376));
        contInterior.put("Form", new Rectangle(0, 0, 1338, 376));
        compBounds.put("panel1", new Rectangle(0, 0, 0, 0));
        baselinePosition.put("panel1-0-0", new Integer(0));
        compBounds.put("canvas1", new Rectangle(0, 0, 0, 0));
        baselinePosition.put("canvas1-0-0", new Integer(0));
        compBounds.put("list1", new Rectangle(53, 26, 40, 200));
        baselinePosition.put("list1-40-200", new Integer(0));
        compBounds.put("checkbox1", new Rectangle(10, 346, 109, 20));
        baselinePosition.put("checkbox1-109-20", new Integer(0));
        compBounds.put("button1", new Rectangle(129, 304, 98, 62));
        baselinePosition.put("button1-98-62", new Integer(0));
        compBounds.put("label1", new Rectangle(169, 26, 38, 20));
        baselinePosition.put("label1-38-20", new Integer(0));
        compBounds.put("scrollbar1", new Rectangle(191, 101, 16, 48));
        baselinePosition.put("scrollbar1-16-48", new Integer(0));
        compBounds.put("scrollbar2", new Rectangle(279, 26, 48, 16));
        baselinePosition.put("scrollbar2-48-16", new Integer(0));
        compBounds.put("scrollPane1", new Rectangle(382, 26, 100, 100));
        baselinePosition.put("scrollPane1-100-100", new Integer(0));
        compBounds.put("choice1", new Rectangle(492, 80, 28, 200));
        baselinePosition.put("choice1-28-200", new Integer(0));
        compBounds.put("textArea1", new Rectangle(129, 200, 245, 80));
        baselinePosition.put("textArea1-245-80", new Integer(0));
        compMinSize.put("Form", new Dimension(1231, 362));
        compBounds.put("Form", new Rectangle(0, 0, 1338, 376));
        contInterior.put("panel1", new Rectangle(0, 0, 0, 0));
        compMinSize.put("panel1", new Dimension(0, 0));
        compBounds.put("panel1", new Rectangle(0, 0, 0, 0));
        prefPadding.put("scrollbar2-textArea1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("scrollbar1-textArea1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("choice1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("choice1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 1338, 376));
        prefPadding.put("choice1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("choice1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
// > START RESIZING
        baselinePosition.put("choice1-28-200", new Integer(0));
        compPrefSize.put("choice1", new Dimension(28, 20));
        {
            String[] compIds = new String[]{
                "choice1"
            };
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(492, 80, 28, 200)
            };
            Point hotspot = new Point(494, 180);
            int[] resizeEdges = new int[]{
                0,
                -1
            };
            boolean inLayout = true;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
// < START RESIZING
        prefPadding.put("scrollPane1-choice1-0-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("scrollPane1-choice1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("scrollPane1-choice1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("scrollPane1-choice1-0-0-3", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
// > MOVE
        {
            Point p = new Point(467, 183);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(465, 80, 55, 200)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
        prefPadding.put("scrollPane1-choice1-0-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("scrollPane1-choice1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("scrollPane1-choice1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("scrollPane1-choice1-0-0-3", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
// > MOVE
        {
            Point p = new Point(466, 183);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(464, 80, 56, 200)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
// > END MOVING
        prefPadding.put("scrollbar2-textArea1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("scrollbar1-textArea1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-choice1-0-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-panel1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-canvas1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-button1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-choice1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        ld.endMoving(true);
// < END MOVING
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 1338, 376));
        contInterior.put("Form", new Rectangle(0, 0, 1338, 376));
        compBounds.put("panel1", new Rectangle(0, 0, 0, 0));
        baselinePosition.put("panel1-0-0", new Integer(0));
        compBounds.put("canvas1", new Rectangle(0, 0, 0, 0));
        baselinePosition.put("canvas1-0-0", new Integer(0));
        compBounds.put("list1", new Rectangle(53, 26, 40, 200));
        baselinePosition.put("list1-40-200", new Integer(0));
        compBounds.put("checkbox1", new Rectangle(10, 346, 109, 20));
        baselinePosition.put("checkbox1-109-20", new Integer(0));
        compBounds.put("button1", new Rectangle(129, 304, 98, 62));
        baselinePosition.put("button1-98-62", new Integer(0));
        compBounds.put("label1", new Rectangle(169, 26, 38, 20));
        baselinePosition.put("label1-38-20", new Integer(0));
        compBounds.put("scrollbar1", new Rectangle(191, 101, 16, 48));
        baselinePosition.put("scrollbar1-16-48", new Integer(0));
        compBounds.put("scrollbar2", new Rectangle(279, 26, 48, 16));
        baselinePosition.put("scrollbar2-48-16", new Integer(0));
        compBounds.put("scrollPane1", new Rectangle(382, 26, 100, 100));
        baselinePosition.put("scrollPane1-100-100", new Integer(0));
        compBounds.put("choice1", new Rectangle(492, 80, 56, 200));
        baselinePosition.put("choice1-56-200", new Integer(0));
        compBounds.put("textArea1", new Rectangle(129, 200, 245, 80));
        baselinePosition.put("textArea1-245-80", new Integer(0));
        compMinSize.put("Form", new Dimension(1297, 362));
        compBounds.put("Form", new Rectangle(0, 0, 1338, 376));
        contInterior.put("panel1", new Rectangle(0, 0, 0, 0));
        compMinSize.put("panel1", new Dimension(0, 0));
        compBounds.put("panel1", new Rectangle(0, 0, 0, 0));
        compPrefSize.put("panel1", new Dimension(0, 0));
        hasExplicitPrefSize.put("panel1", new Boolean(false));
        hasExplicitPrefSize.put("panel1", new Boolean(false));
        prefPadding.put("scrollbar2-textArea1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("scrollbar1-textArea1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("choice1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("choice1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        compBounds.put("Form", new Rectangle(0, 0, 1338, 376));
        contInterior.put("Form", new Rectangle(0, 0, 1338, 376));
        compBounds.put("panel1", new Rectangle(0, 0, 0, 0));
        baselinePosition.put("panel1-0-0", new Integer(0));
        compBounds.put("canvas1", new Rectangle(0, 0, 0, 0));
        baselinePosition.put("canvas1-0-0", new Integer(0));
        compBounds.put("list1", new Rectangle(53, 26, 40, 200));
        baselinePosition.put("list1-40-200", new Integer(0));
        compBounds.put("checkbox1", new Rectangle(10, 346, 109, 20));
        baselinePosition.put("checkbox1-109-20", new Integer(0));
        compBounds.put("button1", new Rectangle(129, 304, 98, 62));
        baselinePosition.put("button1-98-62", new Integer(0));
        compBounds.put("label1", new Rectangle(169, 26, 38, 20));
        baselinePosition.put("label1-38-20", new Integer(0));
        compBounds.put("scrollbar1", new Rectangle(191, 101, 16, 48));
        baselinePosition.put("scrollbar1-16-48", new Integer(0));
        compBounds.put("scrollbar2", new Rectangle(279, 26, 48, 16));
        baselinePosition.put("scrollbar2-48-16", new Integer(0));
        compBounds.put("scrollPane1", new Rectangle(382, 26, 100, 100));
        baselinePosition.put("scrollPane1-100-100", new Integer(0));
        compBounds.put("choice1", new Rectangle(492, 80, 56, 200));
        baselinePosition.put("choice1-56-200", new Integer(0));
        compBounds.put("textArea1", new Rectangle(129, 200, 245, 80));
        baselinePosition.put("textArea1-245-80", new Integer(0));
        compMinSize.put("Form", new Dimension(1297, 362));
        compBounds.put("Form", new Rectangle(0, 0, 1338, 376));
        contInterior.put("panel1", new Rectangle(0, 0, 0, 0));
        compMinSize.put("panel1", new Dimension(0, 0));
        compBounds.put("panel1", new Rectangle(0, 0, 0, 0));
        prefPadding.put("scrollbar2-textArea1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("scrollbar1-textArea1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("choice1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("choice1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 1338, 376));
        prefPadding.put("choice1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("choice1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
    }

    /**
     * Undo previos change. Resize choice1 slightly downwards, not to overlap
     * button1.
     * In vertical layout choice1 should stay in sequence with button1 and on
     * the other side in parallel with panel1 and canvas1.
     * Horizontal layout should not change.
     */
    public void doChanges2() {
        lm.undo(changeMark, lm.getChangeMark());
        changeMark = lm.getChangeMark(); 
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 1338, 376));
        contInterior.put("Form", new Rectangle(0, 0, 1338, 376));
        compBounds.put("panel1", new Rectangle(0, 0, 0, 0));
        baselinePosition.put("panel1-0-0", new Integer(0));
        compBounds.put("canvas1", new Rectangle(0, 0, 0, 0));
        baselinePosition.put("canvas1-0-0", new Integer(0));
        compBounds.put("list1", new Rectangle(53, 26, 40, 200));
        baselinePosition.put("list1-40-200", new Integer(0));
        compBounds.put("checkbox1", new Rectangle(10, 346, 109, 20));
        baselinePosition.put("checkbox1-109-20", new Integer(0));
        compBounds.put("button1", new Rectangle(129, 304, 98, 62));
        baselinePosition.put("button1-98-62", new Integer(0));
        compBounds.put("label1", new Rectangle(169, 26, 38, 20));
        baselinePosition.put("label1-38-20", new Integer(0));
        compBounds.put("scrollbar1", new Rectangle(191, 101, 16, 48));
        baselinePosition.put("scrollbar1-16-48", new Integer(0));
        compBounds.put("scrollbar2", new Rectangle(279, 26, 48, 16));
        baselinePosition.put("scrollbar2-48-16", new Integer(0));
        compBounds.put("scrollPane1", new Rectangle(382, 26, 100, 100));
        baselinePosition.put("scrollPane1-100-100", new Integer(0));
        compBounds.put("choice1", new Rectangle(492, 80, 28, 200));
        baselinePosition.put("choice1-28-200", new Integer(0));
        compBounds.put("textArea1", new Rectangle(129, 200, 245, 80));
        baselinePosition.put("textArea1-245-80", new Integer(0));
        compMinSize.put("Form", new Dimension(1231, 362));
        compBounds.put("Form", new Rectangle(0, 0, 1338, 376));
        contInterior.put("panel1", new Rectangle(0, 0, 0, 0));
        compMinSize.put("panel1", new Dimension(0, 0));
        compBounds.put("panel1", new Rectangle(0, 0, 0, 0));
        prefPadding.put("scrollbar2-textArea1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("scrollbar1-textArea1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("choice1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("choice1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 1338, 376));
        prefPadding.put("choice1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("choice1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
// > START RESIZING
        baselinePosition.put("choice1-28-200", new Integer(0));
        compPrefSize.put("choice1", new Dimension(28, 20));
        {
            String[] compIds = new String[]{
                "choice1"
            };
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(492, 80, 28, 200)
            };
            Point hotspot = new Point(503, 282);
            int[] resizeEdges = new int[]{
                -1,
                1
            };
            boolean inLayout = true;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
// < START RESIZING
        prefPaddingInParent.put("Form-choice1-1-1", new Integer(10)); // parentId-compId-dimension-compAlignment
// > MOVE
        {
            Point p = new Point(503, 296);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(492, 80, 28, 214)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
        prefPaddingInParent.put("Form-choice1-1-1", new Integer(10)); // parentId-compId-dimension-compAlignment
// > MOVE
        {
            Point p = new Point(503, 297);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(492, 80, 28, 215)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
// > END MOVING
        prefPadding.put("scrollbar2-textArea1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("scrollbar1-textArea1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-choice1-1-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        ld.endMoving(true);
// < END MOVING
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 1338, 377));
        contInterior.put("Form", new Rectangle(0, 0, 1338, 377));
        compBounds.put("panel1", new Rectangle(0, 0, 0, 0));
        baselinePosition.put("panel1-0-0", new Integer(0));
        compBounds.put("canvas1", new Rectangle(0, 0, 0, 0));
        baselinePosition.put("canvas1-0-0", new Integer(0));
        compBounds.put("list1", new Rectangle(53, 26, 40, 200));
        baselinePosition.put("list1-40-200", new Integer(0));
        compBounds.put("checkbox1", new Rectangle(10, 347, 109, 20));
        baselinePosition.put("checkbox1-109-20", new Integer(0));
        compBounds.put("button1", new Rectangle(129, 305, 98, 62));
        baselinePosition.put("button1-98-62", new Integer(0));
        compBounds.put("label1", new Rectangle(169, 26, 38, 20));
        baselinePosition.put("label1-38-20", new Integer(0));
        compBounds.put("scrollbar1", new Rectangle(191, 101, 16, 48));
        baselinePosition.put("scrollbar1-16-48", new Integer(0));
        compBounds.put("scrollbar2", new Rectangle(279, 26, 48, 16));
        baselinePosition.put("scrollbar2-48-16", new Integer(0));
        compBounds.put("scrollPane1", new Rectangle(382, 26, 100, 100));
        baselinePosition.put("scrollPane1-100-100", new Integer(0));
        compBounds.put("choice1", new Rectangle(492, 80, 28, 215));
        baselinePosition.put("choice1-28-215", new Integer(0));
        compBounds.put("textArea1", new Rectangle(129, 200, 245, 80));
        baselinePosition.put("textArea1-245-80", new Integer(0));
        compMinSize.put("Form", new Dimension(1231, 377));
        compBounds.put("Form", new Rectangle(0, 0, 1338, 377));
        contInterior.put("panel1", new Rectangle(0, 0, 0, 0));
        compMinSize.put("panel1", new Dimension(0, 0));
        compBounds.put("panel1", new Rectangle(0, 0, 0, 0));
        compPrefSize.put("panel1", new Dimension(0, 0));
        hasExplicitPrefSize.put("panel1", new Boolean(false));
        hasExplicitPrefSize.put("panel1", new Boolean(false));
        prefPadding.put("scrollbar2-textArea1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("scrollbar1-textArea1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("choice1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("choice1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        compBounds.put("Form", new Rectangle(0, 0, 1338, 377));
        contInterior.put("Form", new Rectangle(0, 0, 1338, 377));
        compBounds.put("panel1", new Rectangle(0, 0, 0, 0));
        baselinePosition.put("panel1-0-0", new Integer(0));
        compBounds.put("canvas1", new Rectangle(0, 0, 0, 0));
        baselinePosition.put("canvas1-0-0", new Integer(0));
        compBounds.put("list1", new Rectangle(53, 26, 40, 200));
        baselinePosition.put("list1-40-200", new Integer(0));
        compBounds.put("checkbox1", new Rectangle(10, 347, 109, 20));
        baselinePosition.put("checkbox1-109-20", new Integer(0));
        compBounds.put("button1", new Rectangle(129, 305, 98, 62));
        baselinePosition.put("button1-98-62", new Integer(0));
        compBounds.put("label1", new Rectangle(169, 26, 38, 20));
        baselinePosition.put("label1-38-20", new Integer(0));
        compBounds.put("scrollbar1", new Rectangle(191, 101, 16, 48));
        baselinePosition.put("scrollbar1-16-48", new Integer(0));
        compBounds.put("scrollbar2", new Rectangle(279, 26, 48, 16));
        baselinePosition.put("scrollbar2-48-16", new Integer(0));
        compBounds.put("scrollPane1", new Rectangle(382, 26, 100, 100));
        baselinePosition.put("scrollPane1-100-100", new Integer(0));
        compBounds.put("choice1", new Rectangle(492, 80, 28, 215));
        baselinePosition.put("choice1-28-215", new Integer(0));
        compBounds.put("textArea1", new Rectangle(129, 200, 245, 80));
        baselinePosition.put("textArea1-245-80", new Integer(0));
        compMinSize.put("Form", new Dimension(1231, 377));
        compBounds.put("Form", new Rectangle(0, 0, 1338, 377));
        contInterior.put("panel1", new Rectangle(0, 0, 0, 0));
        compMinSize.put("panel1", new Dimension(0, 0));
        compBounds.put("panel1", new Rectangle(0, 0, 0, 0));
        prefPadding.put("scrollbar2-textArea1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("scrollbar1-textArea1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("choice1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("choice1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 1338, 377));
        prefPadding.put("choice1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("choice1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
    }

    /**
     * Undo previous change. Resize choice1 slightly upwards, but not to overlap
     * with scrollBar2.
     * In vertical layout choice1 should get to sequence with canvas1 and panel1,
     * but not with scrollBar2. Nothing should change on the other side, should
     * keep in sequence with button1.
     * Horizontal layout should not change.
     */
    public void doChanges3() {
        lm.undo(changeMark, lm.getChangeMark());
        changeMark = lm.getChangeMark(); 
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 1338, 376));
        contInterior.put("Form", new Rectangle(0, 0, 1338, 376));
        compBounds.put("panel1", new Rectangle(0, 0, 0, 0));
        baselinePosition.put("panel1-0-0", new Integer(0));
        compBounds.put("canvas1", new Rectangle(0, 0, 0, 0));
        baselinePosition.put("canvas1-0-0", new Integer(0));
        compBounds.put("list1", new Rectangle(53, 26, 40, 200));
        baselinePosition.put("list1-40-200", new Integer(0));
        compBounds.put("checkbox1", new Rectangle(10, 346, 109, 20));
        baselinePosition.put("checkbox1-109-20", new Integer(0));
        compBounds.put("button1", new Rectangle(129, 304, 98, 62));
        baselinePosition.put("button1-98-62", new Integer(0));
        compBounds.put("label1", new Rectangle(169, 26, 38, 20));
        baselinePosition.put("label1-38-20", new Integer(0));
        compBounds.put("scrollbar1", new Rectangle(191, 101, 16, 48));
        baselinePosition.put("scrollbar1-16-48", new Integer(0));
        compBounds.put("scrollbar2", new Rectangle(279, 26, 48, 16));
        baselinePosition.put("scrollbar2-48-16", new Integer(0));
        compBounds.put("scrollPane1", new Rectangle(382, 26, 100, 100));
        baselinePosition.put("scrollPane1-100-100", new Integer(0));
        compBounds.put("choice1", new Rectangle(492, 80, 28, 200));
        baselinePosition.put("choice1-28-200", new Integer(0));
        compBounds.put("textArea1", new Rectangle(129, 200, 245, 80));
        baselinePosition.put("textArea1-245-80", new Integer(0));
        compMinSize.put("Form", new Dimension(1231, 362));
        compBounds.put("Form", new Rectangle(0, 0, 1338, 376));
        contInterior.put("panel1", new Rectangle(0, 0, 0, 0));
        compMinSize.put("panel1", new Dimension(0, 0));
        compBounds.put("panel1", new Rectangle(0, 0, 0, 0));
        prefPadding.put("scrollbar2-textArea1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("scrollbar1-textArea1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("choice1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("choice1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 1338, 376));
        prefPadding.put("choice1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("choice1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
// > START RESIZING
        baselinePosition.put("choice1-28-200", new Integer(0));
        compPrefSize.put("choice1", new Dimension(28, 20));
        {
            String[] compIds = new String[]{
                "choice1"
            };
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(492, 80, 28, 200)
            };
            Point hotspot = new Point(506, 76);
            int[] resizeEdges = new int[]{
                -1,
                0
            };
            boolean inLayout = true;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
// < START RESIZING
        prefPaddingInParent.put("Form-choice1-1-0", new Integer(10)); // parentId-compId-dimension-compAlignment
// > MOVE
        {
            Point p = new Point(506, 55);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(492, 59, 28, 221)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
        prefPaddingInParent.put("Form-choice1-1-0", new Integer(10)); // parentId-compId-dimension-compAlignment
// > MOVE
        {
            Point p = new Point(506, 54);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(492, 58, 28, 222)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
// > END MOVING
        prefPadding.put("scrollbar2-textArea1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("scrollbar1-textArea1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        ld.endMoving(true);
// < END MOVING
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 1338, 376));
        contInterior.put("Form", new Rectangle(0, 0, 1338, 376));
        compBounds.put("panel1", new Rectangle(0, 0, 0, 0));
        baselinePosition.put("panel1-0-0", new Integer(0));
        compBounds.put("canvas1", new Rectangle(0, 0, 0, 0));
        baselinePosition.put("canvas1-0-0", new Integer(0));
        compBounds.put("list1", new Rectangle(53, 26, 40, 200));
        baselinePosition.put("list1-40-200", new Integer(0));
        compBounds.put("checkbox1", new Rectangle(10, 346, 109, 20));
        baselinePosition.put("checkbox1-109-20", new Integer(0));
        compBounds.put("button1", new Rectangle(129, 304, 98, 62));
        baselinePosition.put("button1-98-62", new Integer(0));
        compBounds.put("label1", new Rectangle(169, 26, 38, 20));
        baselinePosition.put("label1-38-20", new Integer(0));
        compBounds.put("scrollbar1", new Rectangle(191, 101, 16, 48));
        baselinePosition.put("scrollbar1-16-48", new Integer(0));
        compBounds.put("scrollbar2", new Rectangle(279, 26, 48, 16));
        baselinePosition.put("scrollbar2-48-16", new Integer(0));
        compBounds.put("scrollPane1", new Rectangle(382, 26, 100, 100));
        baselinePosition.put("scrollPane1-100-100", new Integer(0));
        compBounds.put("choice1", new Rectangle(492, 58, 28, 222));
        baselinePosition.put("choice1-28-222", new Integer(0));
        compBounds.put("textArea1", new Rectangle(129, 200, 245, 80));
        baselinePosition.put("textArea1-245-80", new Integer(0));
        compMinSize.put("Form", new Dimension(1231, 362));
        compBounds.put("Form", new Rectangle(0, 0, 1338, 376));
        contInterior.put("panel1", new Rectangle(0, 0, 0, 0));
        compMinSize.put("panel1", new Dimension(0, 0));
        compBounds.put("panel1", new Rectangle(0, 0, 0, 0));
        compPrefSize.put("panel1", new Dimension(0, 0));
        hasExplicitPrefSize.put("panel1", new Boolean(false));
        hasExplicitPrefSize.put("panel1", new Boolean(false));
        prefPadding.put("scrollbar2-textArea1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("scrollbar1-textArea1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("choice1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("choice1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 1338, 376));
        prefPadding.put("choice1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("choice1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
    }
}
