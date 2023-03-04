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

public class ALT_Bug129494_2Test extends LayoutTestCase {

    public ALT_Bug129494_2Test(String name) {
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
     * Resize jTextField2 to right align with the buttons.
     */
    public void doChanges0() {
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 525, 309));
        contInterior.put("Form", new Rectangle(0, 0, 525, 309));
        compBounds.put("jLabel6", new Rectangle(10, 11, 107, 17));
        baselinePosition.put("jLabel6-107-17", new Integer(13));
        compBounds.put("jLabel7", new Rectangle(10, 92, 74, 17));
        baselinePosition.put("jLabel7-74-17", new Integer(13));
        compBounds.put("jLabel2", new Rectangle(22, 67, 52, 14));
        baselinePosition.put("jLabel2-52-14", new Integer(11));
        compBounds.put("jLabel1", new Rectangle(22, 38, 31, 14));
        baselinePosition.put("jLabel1-31-14", new Integer(11));
        compBounds.put("jLabel4", new Rectangle(22, 145, 27, 14));
        baselinePosition.put("jLabel4-27-14", new Integer(11));
        compBounds.put("jLabel3", new Rectangle(22, 118, 11, 14));
        baselinePosition.put("jLabel3-11-14", new Integer(11));
        compBounds.put("jLabel5", new Rectangle(22, 174, 40, 14));
        baselinePosition.put("jLabel5-40-14", new Integer(11));
        compBounds.put("jTextField4", new Rectangle(88, 171, 284, 20));
        baselinePosition.put("jTextField4-284-20", new Integer(14));
        compBounds.put("jTextField3", new Rectangle(88, 142, 284, 20));
        baselinePosition.put("jTextField3-284-20", new Integer(14));
        compBounds.put("jTextField2", new Rectangle(88, 115, 270, 20));
        baselinePosition.put("jTextField2-270-20", new Integer(14));
        compBounds.put("jComboBox1", new Rectangle(88, 35, 86, 20));
        baselinePosition.put("jComboBox1-86-20", new Integer(14));
        compBounds.put("jButton1", new Rectangle(253, 34, 63, 23));
        baselinePosition.put("jButton1-63-23", new Integer(15));
        compBounds.put("jTextField1", new Rectangle(88, 64, 151, 20));
        baselinePosition.put("jTextField1-151-20", new Integer(14));
        compBounds.put("jButton5", new Rectangle(448, 141, 67, 23));
        baselinePosition.put("jButton5-67-23", new Integer(15));
        compBounds.put("jButton6", new Rectangle(448, 170, 67, 23));
        baselinePosition.put("jButton6-67-23", new Integer(15));
        compBounds.put("jButton2", new Rectangle(444, 34, 71, 23));
        baselinePosition.put("jButton2-71-23", new Integer(15));
        compBounds.put("jButton3", new Rectangle(436, 63, 79, 23));
        baselinePosition.put("jButton3-79-23", new Integer(15));
        compMinSize.put("Form", new Dimension(511, 204));
        compBounds.put("Form", new Rectangle(0, 0, 525, 309));
        compPrefSize.put("jTextField4", new Dimension(6, 20));
        compPrefSize.put("jTextField3", new Dimension(6, 20));
        prefPadding.put("jComboBox1-jButton1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jComboBox1-jButton1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jComboBox1-jButton1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jComboBox1-jButton1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        compPrefSize.put("jButton5", new Dimension(67, 23));
        compPrefSize.put("jButton6", new Dimension(67, 23));
        compPrefSize.put("jButton2", new Dimension(71, 23));
        prefPaddingInParent.put("Form-jLabel5-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton6-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jTextField4-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
// > START RESIZING
        baselinePosition.put("jTextField2-270-20", new Integer(14));
        compPrefSize.put("jTextField2", new Dimension(164, 20));
        {
            String[] compIds = new String[]{
                "jTextField2"
            };
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(88, 115, 270, 20)
            };
            Point hotspot = new Point(357, 125);
            int[] resizeEdges = new int[]{
                1,
                -1
            };
            boolean inLayout = true;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
// < START RESIZING
// > MOVE
        {
            Point p = new Point(506, 115);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(88, 115, 419, 20)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
// > MOVE
        {
            Point p = new Point(507, 115);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(88, 115, 427, 20)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
// > END MOVING
        compPrefSize.put("jTextField4", new Dimension(6, 20));
        compPrefSize.put("jTextField3", new Dimension(6, 20));
        prefPadding.put("jComboBox1-jButton1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jComboBox1-jButton1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jComboBox1-jButton1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jComboBox1-jButton1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        compPrefSize.put("jButton5", new Dimension(67, 23));
        compPrefSize.put("jButton6", new Dimension(67, 23));
        compPrefSize.put("jButton2", new Dimension(71, 23));
        prefPaddingInParent.put("Form-jLabel5-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton6-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jTextField4-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        ld.endMoving(true);
// < END MOVING
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 525, 309));
        contInterior.put("Form", new Rectangle(0, 0, 525, 309));
        compBounds.put("jLabel6", new Rectangle(10, 11, 107, 17));
        baselinePosition.put("jLabel6-107-17", new Integer(13));
        compBounds.put("jLabel7", new Rectangle(10, 92, 74, 17));
        baselinePosition.put("jLabel7-74-17", new Integer(13));
        compBounds.put("jLabel2", new Rectangle(22, 67, 52, 14));
        baselinePosition.put("jLabel2-52-14", new Integer(11));
        compBounds.put("jLabel1", new Rectangle(22, 38, 31, 14));
        baselinePosition.put("jLabel1-31-14", new Integer(11));
        compBounds.put("jLabel4", new Rectangle(22, 145, 27, 14));
        baselinePosition.put("jLabel4-27-14", new Integer(11));
        compBounds.put("jLabel3", new Rectangle(22, 118, 11, 14));
        baselinePosition.put("jLabel3-11-14", new Integer(11));
        compBounds.put("jLabel5", new Rectangle(22, 174, 40, 14));
        baselinePosition.put("jLabel5-40-14", new Integer(11));
        compBounds.put("jTextField4", new Rectangle(88, 171, 284, 20));
        baselinePosition.put("jTextField4-284-20", new Integer(14));
        compBounds.put("jTextField3", new Rectangle(88, 142, 284, 20));
        baselinePosition.put("jTextField3-284-20", new Integer(14));
        compBounds.put("jTextField2", new Rectangle(88, 115, 427, 20));
        baselinePosition.put("jTextField2-427-20", new Integer(14));
        compBounds.put("jComboBox1", new Rectangle(88, 35, 86, 20));
        baselinePosition.put("jComboBox1-86-20", new Integer(14));
        compBounds.put("jButton1", new Rectangle(253, 34, 63, 23));
        baselinePosition.put("jButton1-63-23", new Integer(15));
        compBounds.put("jTextField1", new Rectangle(88, 64, 151, 20));
        baselinePosition.put("jTextField1-151-20", new Integer(14));
        compBounds.put("jButton5", new Rectangle(448, 141, 67, 23));
        baselinePosition.put("jButton5-67-23", new Integer(15));
        compBounds.put("jButton6", new Rectangle(448, 170, 67, 23));
        baselinePosition.put("jButton6-67-23", new Integer(15));
        compBounds.put("jButton2", new Rectangle(444, 34, 71, 23));
        baselinePosition.put("jButton2-71-23", new Integer(15));
        compBounds.put("jButton3", new Rectangle(436, 63, 79, 23));
        baselinePosition.put("jButton3-79-23", new Integer(15));
        compMinSize.put("Form", new Dimension(452, 204));
        compBounds.put("Form", new Rectangle(0, 0, 525, 309));
        compPrefSize.put("jTextField4", new Dimension(6, 20));
        compPrefSize.put("jTextField3", new Dimension(6, 20));
        prefPadding.put("jComboBox1-jButton1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jComboBox1-jButton1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jComboBox1-jButton1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jComboBox1-jButton1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        compPrefSize.put("jButton5", new Dimension(67, 23));
        compPrefSize.put("jButton6", new Dimension(67, 23));
        compPrefSize.put("jButton2", new Dimension(71, 23));
        compPrefSize.put("jTextField2", new Dimension(164, 20));
        prefPaddingInParent.put("Form-jLabel5-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton6-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jTextField4-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        compBounds.put("Form", new Rectangle(0, 0, 525, 309));
        contInterior.put("Form", new Rectangle(0, 0, 525, 309));
        compBounds.put("jLabel6", new Rectangle(10, 11, 107, 17));
        baselinePosition.put("jLabel6-107-17", new Integer(13));
        compBounds.put("jLabel7", new Rectangle(10, 92, 74, 17));
        baselinePosition.put("jLabel7-74-17", new Integer(13));
        compBounds.put("jLabel2", new Rectangle(22, 67, 52, 14));
        baselinePosition.put("jLabel2-52-14", new Integer(11));
        compBounds.put("jLabel1", new Rectangle(22, 38, 31, 14));
        baselinePosition.put("jLabel1-31-14", new Integer(11));
        compBounds.put("jLabel4", new Rectangle(22, 145, 27, 14));
        baselinePosition.put("jLabel4-27-14", new Integer(11));
        compBounds.put("jLabel3", new Rectangle(22, 118, 11, 14));
        baselinePosition.put("jLabel3-11-14", new Integer(11));
        compBounds.put("jLabel5", new Rectangle(22, 174, 40, 14));
        baselinePosition.put("jLabel5-40-14", new Integer(11));
        compBounds.put("jTextField4", new Rectangle(88, 171, 284, 20));
        baselinePosition.put("jTextField4-284-20", new Integer(14));
        compBounds.put("jTextField3", new Rectangle(88, 142, 284, 20));
        baselinePosition.put("jTextField3-284-20", new Integer(14));
        compBounds.put("jTextField2", new Rectangle(88, 115, 427, 20));
        baselinePosition.put("jTextField2-427-20", new Integer(14));
        compBounds.put("jComboBox1", new Rectangle(88, 35, 86, 20));
        baselinePosition.put("jComboBox1-86-20", new Integer(14));
        compBounds.put("jButton1", new Rectangle(253, 34, 63, 23));
        baselinePosition.put("jButton1-63-23", new Integer(15));
        compBounds.put("jTextField1", new Rectangle(88, 64, 151, 20));
        baselinePosition.put("jTextField1-151-20", new Integer(14));
        compBounds.put("jButton5", new Rectangle(448, 141, 67, 23));
        baselinePosition.put("jButton5-67-23", new Integer(15));
        compBounds.put("jButton6", new Rectangle(448, 170, 67, 23));
        baselinePosition.put("jButton6-67-23", new Integer(15));
        compBounds.put("jButton2", new Rectangle(444, 34, 71, 23));
        baselinePosition.put("jButton2-71-23", new Integer(15));
        compBounds.put("jButton3", new Rectangle(436, 63, 79, 23));
        baselinePosition.put("jButton3-79-23", new Integer(15));
        compMinSize.put("Form", new Dimension(452, 204));
        compBounds.put("Form", new Rectangle(0, 0, 525, 309));
        compPrefSize.put("jTextField4", new Dimension(6, 20));
        compPrefSize.put("jTextField3", new Dimension(6, 20));
        prefPadding.put("jComboBox1-jButton1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jComboBox1-jButton1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jComboBox1-jButton1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jComboBox1-jButton1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        compPrefSize.put("jButton5", new Dimension(67, 23));
        compPrefSize.put("jButton6", new Dimension(67, 23));
        compPrefSize.put("jButton2", new Dimension(71, 23));
        compPrefSize.put("jTextField2", new Dimension(164, 20));
        prefPaddingInParent.put("Form-jLabel5-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton6-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jTextField4-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
    }
}
