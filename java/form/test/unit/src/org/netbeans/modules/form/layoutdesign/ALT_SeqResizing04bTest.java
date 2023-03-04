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
import org.openide.filesystems.FileUtil;

public class ALT_SeqResizing04bTest extends LayoutTestCase {

    public ALT_SeqResizing04bTest(String name) {
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
     * Resize the toggle button slightly to the right (so it does not go under
     * the combobox). It shuld stay in the sequence with the label and combobox
     * even though it visually does not belong there (arranged artificially).
     */
    public void doChanges0() {
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 370, 300));
        compBounds.put("jLabel1", new Rectangle(27, 30, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compBounds.put("jToggleButton2", new Rectangle(91, 58, 105, 23));
        baselinePosition.put("jToggleButton2-105-23", new Integer(15));
        compBounds.put("jComboBox1", new Rectangle(255, 27, 56, 20));
        baselinePosition.put("jComboBox1-56-20", new Integer(14));
        compBounds.put("jCheckBox1", new Rectangle(47, 99, 81, 23));
        baselinePosition.put("jCheckBox1-81-23", new Integer(15));
        compBounds.put("jCheckBox2", new Rectangle(184, 99, 81, 23));
        baselinePosition.put("jCheckBox2-81-23", new Integer(15));
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        compPrefSize.put("jToggleButton2", new Dimension(105, 23));
        compPrefSize.put("jComboBox1", new Dimension(56, 20));
        compPrefSize.put("jCheckBox1", new Dimension(81, 23));
        compPrefSize.put("jCheckBox2", new Dimension(81, 23));
        contInterior.put("Form", new Rectangle(0, 0, 370, 300));
        compBounds.put("jLabel1", new Rectangle(27, 30, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compBounds.put("jToggleButton2", new Rectangle(91, 58, 105, 23));
        baselinePosition.put("jToggleButton2-105-23", new Integer(15));
        compBounds.put("jComboBox1", new Rectangle(255, 27, 56, 20));
        baselinePosition.put("jComboBox1-56-20", new Integer(14));
        compBounds.put("jCheckBox1", new Rectangle(47, 99, 81, 23));
        baselinePosition.put("jCheckBox1-81-23", new Integer(15));
        compBounds.put("jCheckBox2", new Rectangle(184, 99, 81, 23));
        baselinePosition.put("jCheckBox2-81-23", new Integer(15));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
        // > START RESIZING
        baselinePosition.put("jToggleButton2-105-23", new Integer(15));
        compPrefSize.put("jToggleButton2", new Dimension(105, 23));
        {
            String[] compIds = new String[]{"jToggleButton2"};
            Rectangle[] bounds = new Rectangle[]{new Rectangle(91, 58, 105, 23)};
            Point hotspot = new Point(196, 71);
            int[] resizeEdges = new int[]{1, -1};
            boolean inLayout = true;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
        // < START RESIZING
        prefPaddingInParent.put("Form-jToggleButton2-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        // > MOVE
        // > MOVE
        // > MOVE
        {
            Point p = new Point(235, 79);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(91, 58, 144, 23)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPaddingInParent.put("Form-jToggleButton2-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        // > MOVE
        // > MOVE
        // > MOVE
        {
            Point p = new Point(236, 79);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(91, 58, 145, 23)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        prefPadding.put("jLabel1-jToggleButton2-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton2-jComboBox1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 370, 300));
        compBounds.put("jLabel1", new Rectangle(27, 30, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compBounds.put("jToggleButton2", new Rectangle(91, 58, 145, 23));
        baselinePosition.put("jToggleButton2-145-23", new Integer(15));
        compBounds.put("jComboBox1", new Rectangle(255, 27, 56, 20));
        baselinePosition.put("jComboBox1-56-20", new Integer(14));
        compBounds.put("jCheckBox1", new Rectangle(47, 99, 81, 23));
        baselinePosition.put("jCheckBox1-81-23", new Integer(15));
        compBounds.put("jCheckBox2", new Rectangle(184, 99, 81, 23));
        baselinePosition.put("jCheckBox2-81-23", new Integer(15));
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        compPrefSize.put("jToggleButton2", new Dimension(105, 23));
        compPrefSize.put("jComboBox1", new Dimension(56, 20));
        compPrefSize.put("jCheckBox1", new Dimension(81, 23));
        compPrefSize.put("jCheckBox2", new Dimension(81, 23));
        contInterior.put("Form", new Rectangle(0, 0, 370, 300));
        compBounds.put("jLabel1", new Rectangle(27, 30, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compBounds.put("jToggleButton2", new Rectangle(91, 58, 145, 23));
        baselinePosition.put("jToggleButton2-145-23", new Integer(15));
        compBounds.put("jComboBox1", new Rectangle(255, 27, 56, 20));
        baselinePosition.put("jComboBox1-56-20", new Integer(14));
        compBounds.put("jCheckBox1", new Rectangle(47, 99, 81, 23));
        baselinePosition.put("jCheckBox1-81-23", new Integer(15));
        compBounds.put("jCheckBox2", new Rectangle(184, 99, 81, 23));
        baselinePosition.put("jCheckBox2-81-23", new Integer(15));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }

    /**
     * Resize the toggle button slightly to the left so it goes under the label
     * but does not snap anywhere. It should now be placed in parallel with the
     * label, but remain in sequence with the combobox on the other side.
     * (A variant of ALT_SeqResizing04Test, just without snapping.)
     */
    public void doChanges1() {
        // > START RESIZING
        baselinePosition.put("jToggleButton2-145-23", new Integer(15));
        compPrefSize.put("jToggleButton2", new Dimension(105, 23));
        {
            String[] compIds = new String[]{"jToggleButton2"};
            Rectangle[] bounds = new Rectangle[]{new Rectangle(91, 58, 145, 23)};
            Point hotspot = new Point(89, 72);
            int[] resizeEdges = new int[]{0, -1};
            boolean inLayout = true;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
        // < START RESIZING
        prefPaddingInParent.put("Form-jToggleButton2-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jLabel1-jToggleButton2-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        // > MOVE
        // > MOVE
        {
            Point p = new Point(54, 74);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(56, 58, 180, 23)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPaddingInParent.put("Form-jToggleButton2-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jLabel1-jToggleButton2-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        // > MOVE
        // > MOVE
        {
            Point p = new Point(53, 74);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(55, 58, 181, 23)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        prefPaddingInParent.put("Form-jToggleButton2-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        // parentId-compId-dimension-compAlignment
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 370, 300));
        compBounds.put("jLabel1", new Rectangle(27, 30, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compBounds.put("jToggleButton2", new Rectangle(55, 58, 181, 23));
        baselinePosition.put("jToggleButton2-181-23", new Integer(15));
        compBounds.put("jComboBox1", new Rectangle(255, 27, 56, 20));
        baselinePosition.put("jComboBox1-56-20", new Integer(14));
        compBounds.put("jCheckBox1", new Rectangle(47, 99, 81, 23));
        baselinePosition.put("jCheckBox1-81-23", new Integer(15));
        compBounds.put("jCheckBox2", new Rectangle(184, 99, 81, 23));
        baselinePosition.put("jCheckBox2-81-23", new Integer(15));
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        compPrefSize.put("jToggleButton2", new Dimension(105, 23));
        compPrefSize.put("jComboBox1", new Dimension(56, 20));
        compPrefSize.put("jCheckBox1", new Dimension(81, 23));
        compPrefSize.put("jCheckBox2", new Dimension(81, 23));
        contInterior.put("Form", new Rectangle(0, 0, 370, 300));
        compBounds.put("jLabel1", new Rectangle(27, 30, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compBounds.put("jToggleButton2", new Rectangle(55, 58, 181, 23));
        baselinePosition.put("jToggleButton2-181-23", new Integer(15));
        compBounds.put("jComboBox1", new Rectangle(255, 27, 56, 20));
        baselinePosition.put("jComboBox1-56-20", new Integer(14));
        compBounds.put("jCheckBox1", new Rectangle(47, 99, 81, 23));
        baselinePosition.put("jCheckBox1-81-23", new Integer(15));
        compBounds.put("jCheckBox2", new Rectangle(184, 99, 81, 23));
        baselinePosition.put("jCheckBox2-81-23", new Integer(15));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }

}
