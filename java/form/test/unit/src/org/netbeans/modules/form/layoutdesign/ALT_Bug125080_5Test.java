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

public class ALT_Bug125080_5Test extends LayoutTestCase {

    public ALT_Bug125080_5Test(String name) {
        super(name);
        try {
            className = this.getClass().getName();
            className = className.substring(className.lastIndexOf('.') + 1);
            startingFormFile = FileUtil.toFileObject(new File(url.getFile() + goldenFilesPath + className + "-StartingForm.form").getCanonicalFile());
        } catch (IOException ioe) {
            fail(ioe.toString());
        }
    }

    /**
     * Resize jTextField2 in both dimensions at the same time. Left to
     * left-align with jTextField1 and slightly down over the table.
     */
    public void doChanges0() {
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 400, 434));
        contInterior.put("Form", new Rectangle(0, 0, 400, 434));
        compBounds.put("jPanel1", new Rectangle(53, 11, 234, 67));
        baselinePosition.put("jPanel1-234-67", new Integer(0));
        contInterior.put("jPanel1", new Rectangle(55, 13, 230, 63));
        compBounds.put("jLabel1", new Rectangle(98, 33, 83, 14));
        baselinePosition.put("jLabel1-83-14", new Integer(11));
        compMinSize.put("jPanel1", new Dimension(140, 49));
        compBounds.put("jPanel1", new Rectangle(53, 11, 234, 67));
        compPrefSize.put("jPanel1", new Dimension(234, 67));
        prefPaddingInParent.put("jPanel1-jLabel1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        prefPaddingInParent.put("jPanel1-jLabel1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        compBounds.put("jLabel2", new Rectangle(53, 99, 34, 14));
        baselinePosition.put("jLabel2-34-14", new Integer(11));
        compBounds.put("jTextField1", new Rectangle(105, 96, 182, 20));
        baselinePosition.put("jTextField1-182-20", new Integer(14));
        compBounds.put("jScrollPane1", new Rectangle(10, 148, 329, 275));
        baselinePosition.put("jScrollPane1-329-275", new Integer(0));
        compBounds.put("jButton1", new Rectangle(345, 148, 45, 23));
        baselinePosition.put("jButton1-45-23", new Integer(15));
        compBounds.put("jButton2", new Rectangle(345, 177, 45, 23));
        baselinePosition.put("jButton2-45-23", new Integer(15));
        compBounds.put("jButton3", new Rectangle(345, 206, 45, 23));
        baselinePosition.put("jButton3-45-23", new Integer(15));
        compBounds.put("jButton4", new Rectangle(345, 247, 45, 23));
        baselinePosition.put("jButton4-45-23", new Integer(15));
        compBounds.put("jButton5", new Rectangle(345, 276, 45, 23));
        baselinePosition.put("jButton5-45-23", new Integer(15));
        compBounds.put("jTextField2", new Rectangle(146, 122, 59, 20));
        baselinePosition.put("jTextField2-59-20", new Integer(14));
        compMinSize.put("Form", new Dimension(348, 434));
        compBounds.put("Form", new Rectangle(0, 0, 400, 434));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compPrefSize.put("jScrollPane1", new Dimension(452, 427));
        prefPaddingInParent.put("Form-jScrollPane1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton5-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
// > START RESIZING
        baselinePosition.put("jTextField2-59-20", new Integer(14));
        compPrefSize.put("jTextField2", new Dimension(59, 20));
        {
            String[] compIds = new String[]{
                "jTextField2"
            };
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(146, 122, 59, 20)
            };
            Point hotspot = new Point(144, 144);
            int[] resizeEdges = new int[]{
                0,
                1
            };
            boolean inLayout = true;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
// < START RESIZING
        prefPadding.put("jTextField2-jScrollPane1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jScrollPane1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jScrollPane1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jScrollPane1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jTextField2-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jLabel2-jTextField2-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jTextField2-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
// > MOVE
        {
            Point p = new Point(109, 154);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(111, 122, 94, 30)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
        prefPadding.put("jTextField2-jScrollPane1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jScrollPane1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jScrollPane1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jScrollPane1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jTextField2-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jLabel2-jTextField2-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jTextField2-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
// > MOVE
        {
            Point p = new Point(108, 154);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(105, 122, 100, 30)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
// > END MOVING
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compPrefSize.put("jScrollPane1", new Dimension(452, 427));
        prefPaddingInParent.put("Form-jScrollPane1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton5-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        ld.endMoving(true);
// < END MOVING
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 400, 444));
        contInterior.put("Form", new Rectangle(0, 0, 400, 444));
        compBounds.put("jPanel1", new Rectangle(53, 11, 234, 67));
        baselinePosition.put("jPanel1-234-67", new Integer(0));
        contInterior.put("jPanel1", new Rectangle(55, 13, 230, 63));
        compBounds.put("jLabel1", new Rectangle(98, 33, 83, 14));
        baselinePosition.put("jLabel1-83-14", new Integer(11));
        compMinSize.put("jPanel1", new Dimension(140, 49));
        compBounds.put("jPanel1", new Rectangle(53, 11, 234, 67));
        compPrefSize.put("jPanel1", new Dimension(234, 67));
        prefPaddingInParent.put("jPanel1-jLabel1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        prefPaddingInParent.put("jPanel1-jLabel1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        compBounds.put("jLabel2", new Rectangle(53, 99, 34, 14));
        baselinePosition.put("jLabel2-34-14", new Integer(11));
        compBounds.put("jTextField1", new Rectangle(105, 96, 182, 20));
        baselinePosition.put("jTextField1-182-20", new Integer(14));
        compBounds.put("jScrollPane1", new Rectangle(10, 158, 329, 275));
        baselinePosition.put("jScrollPane1-329-275", new Integer(0));
        compBounds.put("jButton1", new Rectangle(345, 158, 45, 23));
        baselinePosition.put("jButton1-45-23", new Integer(15));
        compBounds.put("jButton2", new Rectangle(345, 187, 45, 23));
        baselinePosition.put("jButton2-45-23", new Integer(15));
        compBounds.put("jButton3", new Rectangle(345, 216, 45, 23));
        baselinePosition.put("jButton3-45-23", new Integer(15));
        compBounds.put("jButton4", new Rectangle(345, 257, 45, 23));
        baselinePosition.put("jButton4-45-23", new Integer(15));
        compBounds.put("jButton5", new Rectangle(345, 286, 45, 23));
        baselinePosition.put("jButton5-45-23", new Integer(15));
        compBounds.put("jTextField2", new Rectangle(105, 122, 100, 30));
        baselinePosition.put("jTextField2-100-30", new Integer(19));
        compMinSize.put("Form", new Dimension(348, 444));
        compBounds.put("Form", new Rectangle(0, 0, 400, 444));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compPrefSize.put("jScrollPane1", new Dimension(452, 427));
        prefPaddingInParent.put("Form-jScrollPane1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton5-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
    }
}
