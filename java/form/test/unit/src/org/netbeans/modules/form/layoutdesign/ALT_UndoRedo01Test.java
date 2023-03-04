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

public class ALT_UndoRedo01Test extends LayoutTestCase {

    public ALT_UndoRedo01Test(String name) {
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
     * Move scrollbar slightly to the right (snapped from above at medium
     * preferred distance). Then do undo. Tests that bunch of operations done
     * for the move is correctly undone.
     */
    public void doChanges0() {
        lm.setChangeRecording(true);
        Object changeMark = lm.getChangeMark();
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 451, 291));
        contInterior.put("Form", new Rectangle(0, 0, 451, 291));
        compBounds.put("jButton1", new Rectangle(370, 47, 73, 23));
        baselinePosition.put("jButton1-73-23", new Integer(15));
        compBounds.put("jCheckBox1", new Rectangle(370, 75, 81, 23));
        baselinePosition.put("jCheckBox1-81-23", new Integer(15));
        compBounds.put("jPanel1", new Rectangle(28, 103, 310, 100));
        baselinePosition.put("jPanel1-310-100", new Integer(0));
        contInterior.put("jPanel1", new Rectangle(28, 103, 310, 100));
        compBounds.put("jSplitPane1", new Rectangle(38, 114, 179, 25));
        baselinePosition.put("jSplitPane1-179-25", new Integer(-1));
        compMinSize.put("jPanel1", new Dimension(199, 47));
        compBounds.put("jPanel1", new Rectangle(28, 103, 310, 100));
        compPrefSize.put("jPanel1", new Dimension(310, 100));
        prefPaddingInParent.put("jPanel1-jSplitPane1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        prefPaddingInParent.put("jPanel1-jSplitPane1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        compBounds.put("jScrollPane1", new Rectangle(343, 103, 108, 130));
        baselinePosition.put("jScrollPane1-108-130", new Integer(0));
        compBounds.put("jToggleButton1", new Rectangle(28, 243, 105, 23));
        baselinePosition.put("jToggleButton1-105-23", new Integer(15));
        compBounds.put("jScrollBar1", new Rectangle(348, 243, 17, 48));
        baselinePosition.put("jScrollBar1-17-48", new Integer(-1));
        compMinSize.put("Form", new Dimension(451, 291));
        compBounds.put("Form", new Rectangle(0, 0, 451, 291));
        compPrefSize.put("jButton1", new Dimension(73, 23));
        compPrefSize.put("jCheckBox1", new Dimension(81, 23));
        compPrefSize.put("jPanel1", new Dimension(310, 100));
        compPrefSize.put("jToggleButton1", new Dimension(105, 23));
        compPrefSize.put("jScrollBar1", new Dimension(17, 48));
        compPrefSize.put("jButton1", new Dimension(73, 23));
        compPrefSize.put("jCheckBox1", new Dimension(81, 23));
        compPrefSize.put("jPanel1", new Dimension(310, 100));
        compPrefSize.put("jScrollPane1", new Dimension(35, 130));
        compPrefSize.put("jToggleButton1", new Dimension(105, 23));
        compPrefSize.put("jScrollBar1", new Dimension(17, 48));
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
// > START MOVING
        baselinePosition.put("jScrollBar1-17-48", new Integer(-1));
        {
            String[] compIds = new String[]{
                "jScrollBar1"
            };
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(348, 243, 17, 48)
            };
            Point hotspot = new Point(358, 264);
            ld.startMoving(compIds, bounds, hotspot);
        }
// < START MOVING
        prefPaddingInParent.put("Form-jScrollBar1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollBar1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jButton1-jScrollBar1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jScrollBar1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jScrollBar1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jScrollBar1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jButton1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jButton1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jButton1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jButton1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox1-jScrollBar1-1-0-0", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox1-jScrollBar1-1-0-1", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox1-jScrollBar1-1-0-2", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox1-jScrollBar1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jCheckBox1-1-0-0", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jCheckBox1-1-0-1", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jCheckBox1-1-0-2", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jCheckBox1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jScrollBar1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jScrollBar1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jScrollBar1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jScrollBar1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jScrollBar1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollBar1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jToggleButton1-jScrollBar1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jScrollBar1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jScrollBar1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jScrollBar1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jToggleButton1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jToggleButton1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jToggleButton1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jToggleButton1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jScrollBar1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
// > MOVE
        {
            Point p = new Point(404, 266);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(394, 244, 17, 48)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
        prefPaddingInParent.put("Form-jScrollBar1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollBar1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jButton1-jScrollBar1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jScrollBar1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jScrollBar1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jScrollBar1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jButton1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jButton1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jButton1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jButton1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox1-jScrollBar1-1-0-0", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox1-jScrollBar1-1-0-1", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox1-jScrollBar1-1-0-2", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox1-jScrollBar1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jCheckBox1-1-0-0", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jCheckBox1-1-0-1", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jCheckBox1-1-0-2", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jCheckBox1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jScrollBar1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jScrollBar1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jScrollBar1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jScrollBar1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jScrollBar1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollBar1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jToggleButton1-jScrollBar1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jScrollBar1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jScrollBar1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jScrollBar1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jToggleButton1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jToggleButton1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jToggleButton1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jToggleButton1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jScrollBar1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
// > MOVE
        {
            Point p = new Point(405, 266);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(395, 244, 17, 48)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
// > END MOVING
        prefPadding.put("jToggleButton1-jScrollBar1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jScrollBar1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollBar1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        ld.endMoving(true);
// < END MOVING
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 451, 303));
        contInterior.put("Form", new Rectangle(0, 0, 451, 303));
        compBounds.put("jButton1", new Rectangle(370, 47, 73, 23));
        baselinePosition.put("jButton1-73-23", new Integer(15));
        compBounds.put("jCheckBox1", new Rectangle(370, 75, 81, 23));
        baselinePosition.put("jCheckBox1-81-23", new Integer(15));
        compBounds.put("jPanel1", new Rectangle(28, 103, 310, 100));
        baselinePosition.put("jPanel1-310-100", new Integer(0));
        contInterior.put("jPanel1", new Rectangle(28, 103, 310, 100));
        compBounds.put("jSplitPane1", new Rectangle(38, 114, 179, 25));
        baselinePosition.put("jSplitPane1-179-25", new Integer(-1));
        compMinSize.put("jPanel1", new Dimension(199, 47));
        compBounds.put("jPanel1", new Rectangle(28, 103, 310, 100));
        compPrefSize.put("jPanel1", new Dimension(310, 100));
        prefPaddingInParent.put("jPanel1-jSplitPane1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        prefPaddingInParent.put("jPanel1-jSplitPane1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        compBounds.put("jScrollPane1", new Rectangle(343, 103, 108, 130));
        baselinePosition.put("jScrollPane1-108-130", new Integer(0));
        compBounds.put("jToggleButton1", new Rectangle(28, 243, 105, 23));
        baselinePosition.put("jToggleButton1-105-23", new Integer(15));
        compBounds.put("jScrollBar1", new Rectangle(395, 244, 17, 48));
        baselinePosition.put("jScrollBar1-17-48", new Integer(-1));
        compMinSize.put("Form", new Dimension(451, 303));
        compBounds.put("Form", new Rectangle(0, 0, 451, 303));
        prefPadding.put("jToggleButton1-jScrollBar1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jScrollBar1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jScrollBar1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jScrollBar1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPanel1-jScrollBar1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jScrollBar1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jToggleButton1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollBar1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        compPrefSize.put("jButton1", new Dimension(73, 23));
        compPrefSize.put("jCheckBox1", new Dimension(81, 23));
        compPrefSize.put("jPanel1", new Dimension(310, 100));
        compPrefSize.put("jScrollPane1", new Dimension(35, 130));
        compPrefSize.put("jToggleButton1", new Dimension(105, 23));
        prefPadding.put("jPanel1-jScrollBar1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jScrollBar1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        compPrefSize.put("jScrollBar1", new Dimension(17, 48));
        prefPaddingInParent.put("Form-jToggleButton1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollBar1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        compBounds.put("Form", new Rectangle(0, 0, 451, 303));
        contInterior.put("Form", new Rectangle(0, 0, 451, 303));
        compBounds.put("jButton1", new Rectangle(370, 47, 73, 23));
        baselinePosition.put("jButton1-73-23", new Integer(15));
        compBounds.put("jCheckBox1", new Rectangle(370, 75, 81, 23));
        baselinePosition.put("jCheckBox1-81-23", new Integer(15));
        compBounds.put("jPanel1", new Rectangle(28, 103, 310, 100));
        baselinePosition.put("jPanel1-310-100", new Integer(0));
        contInterior.put("jPanel1", new Rectangle(28, 103, 310, 100));
        compBounds.put("jSplitPane1", new Rectangle(38, 114, 179, 25));
        baselinePosition.put("jSplitPane1-179-25", new Integer(-1));
        compMinSize.put("jPanel1", new Dimension(199, 47));
        compBounds.put("jPanel1", new Rectangle(28, 103, 310, 100));
        prefPaddingInParent.put("jPanel1-jSplitPane1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel1-jSplitPane1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        compBounds.put("jScrollPane1", new Rectangle(343, 103, 108, 130));
        baselinePosition.put("jScrollPane1-108-130", new Integer(0));
        compBounds.put("jToggleButton1", new Rectangle(28, 243, 105, 23));
        baselinePosition.put("jToggleButton1-105-23", new Integer(15));
        compBounds.put("jScrollBar1", new Rectangle(395, 244, 17, 48));
        baselinePosition.put("jScrollBar1-17-48", new Integer(-1));
        compMinSize.put("Form", new Dimension(451, 303));
        compBounds.put("Form", new Rectangle(0, 0, 451, 303));
        prefPadding.put("jToggleButton1-jScrollBar1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jScrollBar1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jScrollBar1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jScrollBar1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPanel1-jScrollBar1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jScrollBar1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jToggleButton1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollBar1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
        lm.undo(changeMark, lm.getChangeMark());
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 451, 291));
        contInterior.put("Form", new Rectangle(0, 0, 451, 291));
        compBounds.put("jButton1", new Rectangle(370, 47, 73, 23));
        baselinePosition.put("jButton1-73-23", new Integer(15));
        compBounds.put("jCheckBox1", new Rectangle(370, 75, 81, 23));
        baselinePosition.put("jCheckBox1-81-23", new Integer(15));
        compBounds.put("jPanel1", new Rectangle(28, 103, 310, 100));
        baselinePosition.put("jPanel1-310-100", new Integer(0));
        contInterior.put("jPanel1", new Rectangle(28, 103, 310, 100));
        compBounds.put("jSplitPane1", new Rectangle(38, 114, 179, 25));
        baselinePosition.put("jSplitPane1-179-25", new Integer(-1));
        compMinSize.put("jPanel1", new Dimension(199, 47));
        compBounds.put("jPanel1", new Rectangle(28, 103, 310, 100));
        prefPaddingInParent.put("jPanel1-jSplitPane1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel1-jSplitPane1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        compBounds.put("jScrollPane1", new Rectangle(343, 103, 108, 130));
        baselinePosition.put("jScrollPane1-108-130", new Integer(0));
        compBounds.put("jToggleButton1", new Rectangle(28, 243, 105, 23));
        baselinePosition.put("jToggleButton1-105-23", new Integer(15));
        compBounds.put("jScrollBar1", new Rectangle(348, 243, 17, 48));
        baselinePosition.put("jScrollBar1-17-48", new Integer(-1));
        compMinSize.put("Form", new Dimension(451, 291));
        compBounds.put("Form", new Rectangle(0, 0, 451, 291));
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
    }
}
