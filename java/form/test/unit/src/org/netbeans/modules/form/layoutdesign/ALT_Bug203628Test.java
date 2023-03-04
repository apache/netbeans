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

public class ALT_Bug203628Test extends LayoutTestCase {

    public ALT_Bug203628Test(String name) {
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
     * Move the three checkboxes together to the upper-right corner to snap at
     * container border at the right and at top with label1.
     *
     * It leads to S-layout in vertical dimension - but that's not interesting here.
     * In horizontal dimension the group with checkboxes has a fixed gap at left
     * of checkbox3 and for some reason the L-pos of checkbox3 minus size of the
     * gap goes before T-pos of label3 which is in sequence before all the
     * chekboxes group. Looks like some bug in GroupLayout. If relying on these
     * positions, a negative size is computed for the resizing gap in between.
     */
    public void doChanges0() {
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 395, 297));
        contInterior.put("Form", new Rectangle(0, 0, 395, 297));
        compBounds.put("panel1", new Rectangle(10, 10, 375, 277));
        baselinePosition.put("panel1-375-277", new Integer(0));
        contInterior.put("panel1", new Rectangle(10, 10, 375, 277));
        compBounds.put("checkbox3", new Rectangle(163, 213, 84, 20));
        baselinePosition.put("checkbox3-84-20", new Integer(0));
        compBounds.put("checkbox1", new Rectangle(68, 153, 84, 20));
        baselinePosition.put("checkbox1-84-20", new Integer(0));
        compBounds.put("button1", new Rectangle(20, 20, 57, 24));
        baselinePosition.put("button1-57-24", new Integer(0));
        compBounds.put("label1", new Rectangle(87, 24, 38, 20));
        baselinePosition.put("label1-38-20", new Integer(0));
        compBounds.put("button2", new Rectangle(55, 58, 57, 24));
        baselinePosition.put("button2-57-24", new Integer(0));
        compBounds.put("label2", new Rectangle(122, 62, 38, 20));
        baselinePosition.put("label2-38-20", new Integer(0));
        compBounds.put("button3", new Rectangle(103, 92, 57, 24));
        baselinePosition.put("button3-57-24", new Integer(0));
        compBounds.put("label3", new Rectangle(170, 96, 38, 20));
        baselinePosition.put("label3-38-20", new Integer(0));
        compBounds.put("checkbox2", new Rectangle(110, 183, 84, 20));
        baselinePosition.put("checkbox2-84-20", new Integer(0));
        compMinSize.put("panel1", new Dimension(232, 233));
        compBounds.put("panel1", new Rectangle(10, 10, 375, 277));
        compPrefSize.put("panel1", new Dimension(375, 277));
        prefPaddingInParent.put("panel1-checkbox3-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("panel1-checkbox1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("panel1-label3-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("panel1-checkbox2-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("panel1-label1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("panel1-label2-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        hasExplicitPrefSize.put("panel1", new Boolean(false));
        prefPaddingInParent.put("panel1-checkbox3-1-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        hasExplicitPrefSize.put("panel1", new Boolean(false));
        compMinSize.put("Form", new Dimension(252, 253));
        compBounds.put("Form", new Rectangle(0, 0, 395, 297));
        compPrefSize.put("panel1", new Dimension(375, 277));
        compPrefSize.put("panel1", new Dimension(375, 277));
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
// > START MOVING
        baselinePosition.put("checkbox1-84-20", new Integer(0));
        baselinePosition.put("checkbox2-84-20", new Integer(0));
        baselinePosition.put("checkbox3-84-20", new Integer(0));
        {
            String[] compIds = new String[]{
                "checkbox1",
                "checkbox2",
                "checkbox3"
            };
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(68, 153, 84, 20),
                new Rectangle(110, 183, 84, 20),
                new Rectangle(163, 213, 84, 20)
            };
            Point hotspot = new Point(132, 168);
            ld.startMoving(compIds, bounds, hotspot);
        }
// < START MOVING
        prefPaddingInParent.put("panel1-checkbox1-1-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("panel1-checkbox1-1-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("label3-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("label3-checkbox1-1-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("label3-checkbox1-1-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("label3-checkbox1-1-0-3", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("checkbox1-label3-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("checkbox1-label3-1-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("checkbox1-label3-1-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("checkbox1-label3-1-0-3", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("panel1-checkbox1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("panel1-checkbox1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("button1-checkbox1-0-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("label1-checkbox1-0-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("label1-checkbox1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("label1-checkbox1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("label1-checkbox1-0-0-3", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("button2-checkbox1-0-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("label2-checkbox1-0-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("label2-checkbox1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("label2-checkbox1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("label2-checkbox1-0-0-3", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("button3-checkbox1-0-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("label3-checkbox1-0-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("label3-checkbox1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("label3-checkbox1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("label3-checkbox1-0-0-3", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
// > MOVE
        {
            Point p = new Point(269, 37);
            String containerId = "panel1";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(206, 24, 84, 20),
                new Rectangle(248, 54, 84, 20),
                new Rectangle(301, 84, 84, 20)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
        prefPaddingInParent.put("panel1-checkbox1-1-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("panel1-checkbox1-1-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("label3-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("label3-checkbox1-1-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("label3-checkbox1-1-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("label3-checkbox1-1-0-3", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("checkbox1-label3-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("checkbox1-label3-1-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("checkbox1-label3-1-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("checkbox1-label3-1-0-3", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("panel1-checkbox1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("panel1-checkbox1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("button1-checkbox1-0-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("label1-checkbox1-0-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("label1-checkbox1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("label1-checkbox1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("label1-checkbox1-0-0-3", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("button2-checkbox1-0-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("label2-checkbox1-0-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("label2-checkbox1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("label2-checkbox1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("label2-checkbox1-0-0-3", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("button3-checkbox1-0-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("label3-checkbox1-0-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("label3-checkbox1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("label3-checkbox1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("label3-checkbox1-0-0-3", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
// > MOVE
        {
            Point p = new Point(269, 38);
            String containerId = "panel1";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(206, 24, 84, 20),
                new Rectangle(248, 54, 84, 20),
                new Rectangle(301, 84, 84, 20)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
// > END MOVING
        prefPaddingInParent.put("panel1-label3-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("panel1-label1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("panel1-label2-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("panel1-label3-1-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("panel1-button3-1-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        ld.endMoving(true);
// < END MOVING
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 395, 297));
        contInterior.put("Form", new Rectangle(0, 0, 395, 297));
        compBounds.put("panel1", new Rectangle(10, 10, 375, 277));
        baselinePosition.put("panel1-375-277", new Integer(0));
        contInterior.put("panel1", new Rectangle(10, 10, 375, 277));
        compBounds.put("checkbox3", new Rectangle(301, 80, 84, 20));
        baselinePosition.put("checkbox3-84-20", new Integer(0));
        compBounds.put("checkbox1", new Rectangle(218, 20, 84, 20));
        baselinePosition.put("checkbox1-84-20", new Integer(0));
        compBounds.put("button1", new Rectangle(20, 76, 57, 24));
        baselinePosition.put("button1-57-24", new Integer(0));
        compBounds.put("label1", new Rectangle(87, 20, 38, 20));
        baselinePosition.put("label1-38-20", new Integer(0));
        compBounds.put("button2", new Rectangle(55, 114, 57, 24));
        baselinePosition.put("button2-57-24", new Integer(0));
        compBounds.put("label2", new Rectangle(122, 118, 38, 20));
        baselinePosition.put("label2-38-20", new Integer(0));
        compBounds.put("button3", new Rectangle(103, 148, 57, 24));
        baselinePosition.put("button3-57-24", new Integer(0));
        compBounds.put("label3", new Rectangle(170, 152, 38, 20));
        baselinePosition.put("label3-38-20", new Integer(0));
        compBounds.put("checkbox2", new Rectangle(260, 50, 84, 20));
        baselinePosition.put("checkbox2-84-20", new Integer(0));
        compMinSize.put("panel1", new Dimension(344, 172));
        compBounds.put("panel1", new Rectangle(10, 10, 375, 277));
        compPrefSize.put("panel1", new Dimension(387, 333));
        prefPadding.put("label3-checkbox1-0-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("label3-checkbox1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("label3-checkbox1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("label3-checkbox1-0-0-3", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("label1-checkbox1-0-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("label1-checkbox1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("label1-checkbox1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("label1-checkbox1-0-0-3", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("label2-checkbox1-0-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("label2-checkbox1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("label2-checkbox1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("label2-checkbox1-0-0-3", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        hasExplicitPrefSize.put("panel1", new Boolean(false));
        prefPaddingInParent.put("panel1-label3-1-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("panel1-button3-1-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        hasExplicitPrefSize.put("panel1", new Boolean(false));
        compMinSize.put("Form", new Dimension(364, 192));
        compBounds.put("Form", new Rectangle(0, 0, 395, 297));
        compPrefSize.put("panel1", new Dimension(387, 333));
        compPrefSize.put("panel1", new Dimension(387, 333));
        compBounds.put("Form", new Rectangle(0, 0, 395, 297));
        contInterior.put("Form", new Rectangle(0, 0, 395, 297));
        compBounds.put("panel1", new Rectangle(10, 10, 375, 277));
        baselinePosition.put("panel1-375-277", new Integer(0));
        contInterior.put("panel1", new Rectangle(10, 10, 375, 277));
        compBounds.put("checkbox3", new Rectangle(301, 80, 84, 20));
        baselinePosition.put("checkbox3-84-20", new Integer(0));
        compBounds.put("checkbox1", new Rectangle(218, 20, 84, 20));
        baselinePosition.put("checkbox1-84-20", new Integer(0));
        compBounds.put("button1", new Rectangle(20, 76, 57, 24));
        baselinePosition.put("button1-57-24", new Integer(0));
        compBounds.put("label1", new Rectangle(87, 20, 38, 20));
        baselinePosition.put("label1-38-20", new Integer(0));
        compBounds.put("button2", new Rectangle(55, 114, 57, 24));
        baselinePosition.put("button2-57-24", new Integer(0));
        compBounds.put("label2", new Rectangle(122, 118, 38, 20));
        baselinePosition.put("label2-38-20", new Integer(0));
        compBounds.put("button3", new Rectangle(103, 148, 57, 24));
        baselinePosition.put("button3-57-24", new Integer(0));
        compBounds.put("label3", new Rectangle(170, 152, 38, 20));
        baselinePosition.put("label3-38-20", new Integer(0));
        compBounds.put("checkbox2", new Rectangle(260, 50, 84, 20));
        baselinePosition.put("checkbox2-84-20", new Integer(0));
        compMinSize.put("panel1", new Dimension(344, 172));
        compBounds.put("panel1", new Rectangle(10, 10, 375, 277));
        prefPadding.put("label3-checkbox1-0-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("label3-checkbox1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("label3-checkbox1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("label3-checkbox1-0-0-3", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("label1-checkbox1-0-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("label1-checkbox1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("label1-checkbox1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("label1-checkbox1-0-0-3", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("label2-checkbox1-0-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("label2-checkbox1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("label2-checkbox1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("label2-checkbox1-0-0-3", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("panel1-label3-1-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("panel1-button3-1-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        compMinSize.put("Form", new Dimension(364, 192));
        compBounds.put("Form", new Rectangle(0, 0, 395, 297));
        compPrefSize.put("panel1", new Dimension(387, 277));
        compPrefSize.put("panel1", new Dimension(387, 277));
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
    }
}
