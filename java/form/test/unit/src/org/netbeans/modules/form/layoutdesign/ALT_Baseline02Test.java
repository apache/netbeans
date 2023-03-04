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
 * Tests that large buttons (that don't have vertical text alignment on CENTER)
 * don't align on baseline, but can L/T align with whole baseline group. If such
 * a baslined button is removed from an existing (old) layout, the layout should
 * not collapse with other components in parallel that might not overlap only
 * thanks to the height of the button.
 *
 * The layout structure in the example is rather ugly (could be less nested with
 * less gaps) - to ensure the parallel collapse prevention works also in
 * non-optimal cases.
 */
public class ALT_Baseline02Test extends LayoutTestCase {

    public ALT_Baseline02Test(String name) {
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
     * Move jButton2 between the big button and the other components, align at
     * bottom with the group (button and textfield).
     */
    public void doChanges0() {
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 316));
        compBounds.put("jTextField2", new Rectangle(10, 11, 380, 19));
        baselinePosition.put("jTextField2-380-19", new Integer(13));
        compPrefSize.put("jTextField2", new Dimension(64, 19));
        compBounds.put("jLabel2", new Rectangle(10, 81, 58, 14));
        baselinePosition.put("jLabel2-58-14", new Integer(11));
        compPrefSize.put("jLabel2", new Dimension(58, 14));
        compBounds.put("jTextField1", new Rectangle(72, 79, 140, 19));
        baselinePosition.put("jTextField1-140-19", new Integer(13));
        compPrefSize.put("jTextField1", new Dimension(11, 19));
        compBounds.put("jLabel1", new Rectangle(10, 45, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        compBounds.put("jComboBox1", new Rectangle(48, 41, 55, 22));
        baselinePosition.put("jComboBox1-55-22", new Integer(15));
        compPrefSize.put("jComboBox1", new Dimension(55, 22));
        compBounds.put("jButton1", new Rectangle(317, 41, 73, 59));
        baselinePosition.put("jButton1-73-59", new Integer(0));
        compPrefSize.put("jButton1", new Dimension(73, 59));
        compBounds.put("jButton2", new Rectangle(120, 254, 65, 41));
        baselinePosition.put("jButton2-65-41", new Integer(0));
        compPrefSize.put("jButton2", new Dimension(65, 41));
        contInterior.put("Form", new Rectangle(0, 0, 400, 316));
        compBounds.put("jTextField2", new Rectangle(10, 11, 380, 19));
        baselinePosition.put("jTextField2-380-19", new Integer(13));
        compBounds.put("jLabel2", new Rectangle(10, 81, 58, 14));
        baselinePosition.put("jLabel2-58-14", new Integer(11));
        compBounds.put("jTextField1", new Rectangle(72, 79, 140, 19));
        baselinePosition.put("jTextField1-140-19", new Integer(13));
        compBounds.put("jLabel1", new Rectangle(10, 45, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compBounds.put("jComboBox1", new Rectangle(48, 41, 55, 22));
        baselinePosition.put("jComboBox1-55-22", new Integer(15));
        compBounds.put("jButton1", new Rectangle(317, 41, 73, 59));
        baselinePosition.put("jButton1-73-59", new Integer(0));
        compBounds.put("jButton2", new Rectangle(120, 254, 65, 41));
        baselinePosition.put("jButton2-65-41", new Integer(0));
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
// > START MOVING
        baselinePosition.put("jButton2-65-41", new Integer(0));
        {
            String[] compIds = new String[]{"jButton2"};
            Rectangle[] bounds = new Rectangle[]{new Rectangle(120, 254, 65, 41)};
            Point hotspot = new Point(153, 278);
            ld.startMoving(compIds, bounds, hotspot);
        }
// < START MOVING
        prefPaddingInParent.put("Form-jButton2-1-0", new Integer(11));
        // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton2-1-1", new Integer(11));
        // parentId-compId-dimension-compAlignment
        prefPadding.put("jTextField2-jButton2-1-0-0", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jButton2-1-0-1", new Integer(11));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jButton2-1-0-2", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jButton2-1-0-3", new Integer(18));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jTextField2-1-0-0", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jTextField2-1-0-1", new Integer(11));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jTextField2-1-0-2", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jTextField2-1-0-3", new Integer(18));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jButton2-1-0-0", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jButton2-1-0-1", new Integer(11));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jButton2-1-0-2", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jButton2-1-0-3", new Integer(18));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jButton2-1-0-0", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jButton2-1-0-1", new Integer(11));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jButton2-1-0-2", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jButton2-1-0-3", new Integer(18));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jButton2-1-0-0", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jButton2-1-0-1", new Integer(11));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jButton2-1-0-2", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jButton2-1-0-3", new Integer(18));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jTextField1-1-0-0", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jTextField1-1-0-1", new Integer(11));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jTextField1-1-0-2", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jTextField1-1-0-3", new Integer(18));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jLabel2-1-0-0", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jLabel2-1-0-1", new Integer(11));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jLabel2-1-0-2", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jLabel2-1-0-3", new Integer(18));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jButton1-1-0-0", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jButton1-1-0-1", new Integer(11));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jButton1-1-0-2", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jButton1-1-0-3", new Integer(18));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jButton2-0-0", new Integer(10));
        // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton2-0-1", new Integer(10));
        // parentId-compId-dimension-compAlignment
        prefPadding.put("jLabel2-jButton2-0-0-0", new Integer(4));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jButton2-0-0-0", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jButton2-0-0-1", new Integer(10));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jButton2-0-0-2", new Integer(10));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jButton2-0-0-3", new Integer(18));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jComboBox1-jButton2-0-0-0", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jComboBox1-jButton2-0-0-1", new Integer(10));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jComboBox1-jButton2-0-0-2", new Integer(10));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jComboBox1-jButton2-0-0-3", new Integer(18));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jButton2-0-0-0", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jButton2-0-0-1", new Integer(10));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jButton2-0-0-2", new Integer(10));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jButton2-0-0-3", new Integer(18));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jButton1-0-0-0", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jButton1-0-0-1", new Integer(10));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jButton1-0-0-2", new Integer(10));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jButton1-0-0-3", new Integer(18));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
// > MOVE
        {
            Point p = new Point(255, 81);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(222, 59, 65, 41)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
        prefPaddingInParent.put("Form-jButton2-1-0", new Integer(11));
        // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton2-1-1", new Integer(11));
        // parentId-compId-dimension-compAlignment
        prefPadding.put("jTextField2-jButton2-1-0-0", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jButton2-1-0-1", new Integer(11));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jButton2-1-0-2", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jButton2-1-0-3", new Integer(18));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jTextField2-1-0-0", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jTextField2-1-0-1", new Integer(11));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jTextField2-1-0-2", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jTextField2-1-0-3", new Integer(18));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jButton2-1-0-0", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jButton2-1-0-1", new Integer(11));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jButton2-1-0-2", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jButton2-1-0-3", new Integer(18));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jButton2-1-0-0", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jButton2-1-0-1", new Integer(11));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jButton2-1-0-2", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jButton2-1-0-3", new Integer(18));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jButton2-1-0-0", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jButton2-1-0-1", new Integer(11));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jButton2-1-0-2", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jButton2-1-0-3", new Integer(18));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jTextField1-1-0-0", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jTextField1-1-0-1", new Integer(11));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jTextField1-1-0-2", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jTextField1-1-0-3", new Integer(18));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jLabel2-1-0-0", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jLabel2-1-0-1", new Integer(11));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jLabel2-1-0-2", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jLabel2-1-0-3", new Integer(18));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jButton1-1-0-0", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jButton1-1-0-1", new Integer(11));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jButton1-1-0-2", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jButton1-1-0-3", new Integer(18));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jButton2-0-0", new Integer(10));
        // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton2-0-1", new Integer(10));
        // parentId-compId-dimension-compAlignment
        prefPadding.put("jLabel2-jButton2-0-0-0", new Integer(4));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jButton2-0-0-0", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jButton2-0-0-1", new Integer(10));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jButton2-0-0-2", new Integer(10));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jButton2-0-0-3", new Integer(18));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jComboBox1-jButton2-0-0-0", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jComboBox1-jButton2-0-0-1", new Integer(10));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jComboBox1-jButton2-0-0-2", new Integer(10));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jComboBox1-jButton2-0-0-3", new Integer(18));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jButton2-0-0-0", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jButton2-0-0-1", new Integer(10));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jButton2-0-0-2", new Integer(10));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jButton2-0-0-3", new Integer(18));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jButton1-0-0-0", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jButton1-0-0-1", new Integer(10));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jButton1-0-0-2", new Integer(10));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jButton1-0-0-3", new Integer(18));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
// > MOVE
        {
            Point p = new Point(254, 81);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(222, 59, 65, 41)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
// > END MOVING
        prefPadding.put("jButton2-jButton1-0-0-0", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jButton2-1-0-0", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jButton2-1-0-1", new Integer(11));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jButton2-1-0-2", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jButton2-1-0-3", new Integer(18));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        ld.endMoving(true);
// < END MOVING
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 316));
        compBounds.put("jTextField2", new Rectangle(10, 11, 380, 19));
        baselinePosition.put("jTextField2-380-19", new Integer(13));
        compPrefSize.put("jTextField2", new Dimension(64, 19));
        compBounds.put("jLabel2", new Rectangle(10, 81, 58, 14));
        baselinePosition.put("jLabel2-58-14", new Integer(11));
        compPrefSize.put("jLabel2", new Dimension(58, 14));
        compBounds.put("jTextField1", new Rectangle(72, 79, 140, 19));
        baselinePosition.put("jTextField1-140-19", new Integer(13));
        compPrefSize.put("jTextField1", new Dimension(11, 19));
        compBounds.put("jLabel1", new Rectangle(10, 45, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        compBounds.put("jComboBox1", new Rectangle(48, 41, 55, 22));
        baselinePosition.put("jComboBox1-55-22", new Integer(15));
        compPrefSize.put("jComboBox1", new Dimension(55, 22));
        compBounds.put("jButton1", new Rectangle(317, 41, 73, 59));
        baselinePosition.put("jButton1-73-59", new Integer(0));
        compPrefSize.put("jButton1", new Dimension(73, 59));
        compBounds.put("jButton2", new Rectangle(222, 59, 65, 41));
        baselinePosition.put("jButton2-65-41", new Integer(0));
        compPrefSize.put("jButton2", new Dimension(65, 41));
        contInterior.put("Form", new Rectangle(0, 0, 400, 316));
        compBounds.put("jTextField2", new Rectangle(10, 11, 380, 19));
        baselinePosition.put("jTextField2-380-19", new Integer(13));
        compBounds.put("jLabel2", new Rectangle(10, 81, 58, 14));
        baselinePosition.put("jLabel2-58-14", new Integer(11));
        compBounds.put("jTextField1", new Rectangle(72, 79, 140, 19));
        baselinePosition.put("jTextField1-140-19", new Integer(13));
        compBounds.put("jLabel1", new Rectangle(10, 45, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compBounds.put("jComboBox1", new Rectangle(48, 41, 55, 22));
        baselinePosition.put("jComboBox1-55-22", new Integer(15));
        compBounds.put("jButton1", new Rectangle(317, 41, 73, 59));
        baselinePosition.put("jButton1-73-59", new Integer(0));
        compBounds.put("jButton2", new Rectangle(222, 59, 65, 41));
        baselinePosition.put("jButton2-65-41", new Integer(0));
        ld.updateCurrentState();
    }

    /**
     * Move jButton1 away from it's position in baseline group with textfield
     * and label. The vertical gap between the combobox and the textfield below
     * it should be preserved.
     */
    public void doChanges1() {
// > START MOVING
        baselinePosition.put("jButton1-73-59", new Integer(0));
        {
            String[] compIds = new String[]{"jButton1"};
            Rectangle[] bounds = new Rectangle[]{new Rectangle(317, 41, 73, 59)};
            Point hotspot = new Point(347, 66);
            ld.startMoving(compIds, bounds, hotspot);
        }
// < START MOVING
        prefPaddingInParent.put("Form-jButton1-1-0", new Integer(11));
        // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton1-1-1", new Integer(11));
        // parentId-compId-dimension-compAlignment
        prefPadding.put("jTextField2-jButton1-1-0-0", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jButton1-1-0-1", new Integer(11));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jButton1-1-0-2", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jButton1-1-0-3", new Integer(18));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jTextField2-1-0-0", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jTextField2-1-0-1", new Integer(11));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jTextField2-1-0-2", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jTextField2-1-0-3", new Integer(18));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jButton1-1-0-0", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jButton1-1-0-1", new Integer(11));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jButton1-1-0-2", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jButton1-1-0-3", new Integer(18));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jButton1-1-0-0", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jButton1-1-0-1", new Integer(11));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jButton1-1-0-2", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jButton1-1-0-3", new Integer(18));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jButton1-1-0-0", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jButton1-1-0-1", new Integer(11));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jButton1-1-0-2", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jButton1-1-0-3", new Integer(18));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jButton1-0-0", new Integer(10));
        // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton1-0-1", new Integer(10));
        // parentId-compId-dimension-compAlignment
// > MOVE
        {
            Point p = new Point(351, 269);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(317, 246, 73, 59)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
        prefPaddingInParent.put("Form-jButton1-1-0", new Integer(11));
        // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton1-1-1", new Integer(11));
        // parentId-compId-dimension-compAlignment
        prefPadding.put("jTextField2-jButton1-1-0-0", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jButton1-1-0-1", new Integer(11));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jButton1-1-0-2", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jButton1-1-0-3", new Integer(18));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jTextField2-1-0-0", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jTextField2-1-0-1", new Integer(11));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jTextField2-1-0-2", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jTextField2-1-0-3", new Integer(18));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jButton1-1-0-0", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jButton1-1-0-1", new Integer(11));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jButton1-1-0-2", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jButton1-1-0-3", new Integer(18));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jButton1-1-0-0", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jButton1-1-0-1", new Integer(11));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jButton1-1-0-2", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jButton1-1-0-3", new Integer(18));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jButton1-1-0-0", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jButton1-1-0-1", new Integer(11));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jButton1-1-0-2", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jButton1-1-0-3", new Integer(18));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jButton1-0-0", new Integer(10));
        // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton1-0-1", new Integer(10));
        // parentId-compId-dimension-compAlignment
// > MOVE
        {
            Point p = new Point(351, 268);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(317, 246, 73, 59)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
// > END MOVING
        prefPaddingInParent.put("Form-jButton1-0-0", new Integer(10));
        // parentId-compId-dimension-compAlignment
        prefPadding.put("jButton2-jButton1-1-0-0", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jButton1-1-0-0", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jButton1-1-0-0", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        ld.endMoving(true);
// < END MOVING
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 316));
        compBounds.put("jTextField2", new Rectangle(10, 11, 380, 19));
        baselinePosition.put("jTextField2-380-19", new Integer(13));
        compPrefSize.put("jTextField2", new Dimension(64, 19));
        compBounds.put("jLabel2", new Rectangle(10, 81, 58, 14));
        baselinePosition.put("jLabel2-58-14", new Integer(11));
        compPrefSize.put("jLabel2", new Dimension(58, 14));
        compBounds.put("jTextField1", new Rectangle(72, 79, 140, 19));
        baselinePosition.put("jTextField1-140-19", new Integer(13));
        compPrefSize.put("jTextField1", new Dimension(11, 19));
        compBounds.put("jLabel1", new Rectangle(10, 45, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        compBounds.put("jComboBox1", new Rectangle(48, 41, 55, 22));
        baselinePosition.put("jComboBox1-55-22", new Integer(15));
        compPrefSize.put("jComboBox1", new Dimension(55, 22));
        compBounds.put("jButton2", new Rectangle(222, 57, 65, 41));
        baselinePosition.put("jButton2-65-41", new Integer(0));
        compPrefSize.put("jButton2", new Dimension(65, 41));
        compBounds.put("jButton1", new Rectangle(317, 246, 73, 59));
        baselinePosition.put("jButton1-73-59", new Integer(0));
        compPrefSize.put("jButton1", new Dimension(73, 59));
        prefPadding.put("jButton2-jButton1-1-0-0", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jButton1-1-0-1", new Integer(11));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jButton1-1-0-2", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jButton1-1-0-3", new Integer(18));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jButton1-1-0-0", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jButton1-1-0-1", new Integer(11));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jButton1-1-0-2", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jButton1-1-0-3", new Integer(18));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jButton1-1-0-0", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jButton1-1-0-1", new Integer(11));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jButton1-1-0-2", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jButton1-1-0-3", new Integer(18));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        contInterior.put("Form", new Rectangle(0, 0, 400, 316));
        compBounds.put("jTextField2", new Rectangle(10, 11, 380, 19));
        baselinePosition.put("jTextField2-380-19", new Integer(13));
        compBounds.put("jLabel2", new Rectangle(10, 81, 58, 14));
        baselinePosition.put("jLabel2-58-14", new Integer(11));
        compBounds.put("jTextField1", new Rectangle(72, 79, 140, 19));
        baselinePosition.put("jTextField1-140-19", new Integer(13));
        compBounds.put("jLabel1", new Rectangle(10, 45, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compBounds.put("jComboBox1", new Rectangle(48, 41, 55, 22));
        baselinePosition.put("jComboBox1-55-22", new Integer(15));
        compBounds.put("jButton2", new Rectangle(222, 57, 65, 41));
        baselinePosition.put("jButton2-65-41", new Integer(0));
        compBounds.put("jButton1", new Rectangle(317, 246, 73, 59));
        baselinePosition.put("jButton1-73-59", new Integer(0));
        ld.updateCurrentState();
    }

}
