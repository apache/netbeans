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

public class ALT_Bug203563Test extends LayoutTestCase {

    public ALT_Bug203563Test(String name) {
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
     * Resize jScrollPane2 (with jTextArea1) down to bottom-align with the
     * tabbed pane.
     */
    public void doChanges0() {
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("jPanel1", new Rectangle(25, 50, 137, 220));
        contInterior.put("jPanel1", new Rectangle(25, 50, 137, 220));
        compMinSize.put("jPanel1", new Dimension(0, 0));
        compBounds.put("jPanel1", new Rectangle(25, 50, 137, 220));
        compBounds.put("Form", new Rectangle(0, 0, 522, 339));
        contInterior.put("Form", new Rectangle(0, 0, 522, 339));
        compBounds.put("jPanel2", new Rectangle(10, 11, 502, 317));
        baselinePosition.put("jPanel2-502-317", new Integer(0));
        contInterior.put("jPanel2", new Rectangle(13, 14, 504, 311));
        compBounds.put("jLabel1", new Rectangle(23, 300, 484, 14));
        baselinePosition.put("jLabel1-484-14", new Integer(11));
        compBounds.put("jTabbedPane1", new Rectangle(23, 25, 142, 248));
        baselinePosition.put("jTabbedPane1-142-248", new Integer(0));
        compBounds.put("jScrollPane2", new Rectangle(284, 25, 195, 153));
        baselinePosition.put("jScrollPane2-195-153", new Integer(0));
        compBounds.put("jButton2", new Rectangle(195, 191, 73, 23));
        baselinePosition.put("jButton2-73-23", new Integer(15));
        compBounds.put("jButton1", new Rectangle(169, 232, 73, 23));
        baselinePosition.put("jButton1-73-23", new Integer(15));
        compMinSize.put("jPanel2", new Dimension(510, 317));
        compBounds.put("jPanel2", new Rectangle(10, 11, 502, 317));
        compPrefSize.put("jPanel2", new Dimension(510, 317));
        prefPaddingInParent.put("jPanel2-jLabel1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jTabbedPane1-jScrollPane2-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTabbedPane1-jScrollPane2-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTabbedPane1-jScrollPane2-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTabbedPane1-jScrollPane2-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        hasExplicitPrefSize.put("jPanel2", new Boolean(false));
        prefPaddingInParent.put("jPanel2-jLabel1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel2-jTabbedPane1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel2-jScrollPane2-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        compPrefSize.put("jButton2", new Dimension(73, 23));
        prefPadding.put("jButton2-jButton1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        compPrefSize.put("jButton1", new Dimension(73, 23));
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        prefPaddingInParent.put("jPanel2-jLabel1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        hasExplicitPrefSize.put("jPanel2", new Boolean(false));
        compMinSize.put("Form", new Dimension(522, 339));
        compBounds.put("Form", new Rectangle(0, 0, 522, 339));
        prefPaddingInParent.put("Form-jPanel2-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jPanel2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jPanel2-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        compPrefSize.put("jPanel2", new Dimension(510, 317));
        prefPaddingInParent.put("Form-jPanel2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        compBounds.put("jPanel3", new Rectangle(25, 50, 137, 220));
        contInterior.put("jPanel3", new Rectangle(25, 50, 137, 220));
        compMinSize.put("jPanel3", new Dimension(0, 0));
        compBounds.put("jPanel3", new Rectangle(25, 50, 137, 220));
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
// > START RESIZING
        baselinePosition.put("jScrollPane2-195-153", new Integer(0));
        compPrefSize.put("jScrollPane2", new Dimension(166, 96));
        {
            String[] compIds = new String[]{
                "jScrollPane2"
            };
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(284, 25, 195, 153)
            };
            Point hotspot = new Point(374, 176);
            int[] resizeEdges = new int[]{
                -1,
                1
            };
            boolean inLayout = true;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
// < START RESIZING
        prefPadding.put("jScrollPane2-jLabel1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane2-jLabel1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane2-jLabel1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane2-jLabel1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
// > MOVE
        {
            Point p = new Point(370, 266);
            String containerId = "jPanel2";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(284, 25, 195, 248)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
        prefPadding.put("jScrollPane2-jLabel1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane2-jLabel1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane2-jLabel1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane2-jLabel1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
// > MOVE
        {
            Point p = new Point(370, 267);
            String containerId = "jPanel2";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(284, 25, 195, 248)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
// > END MOVING
        prefPaddingInParent.put("jPanel2-jLabel1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel2-jLabel1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        compMinSize.put("jTabbedPane1", new Dimension(38, 44));
        prefPadding.put("jTabbedPane1-jScrollPane2-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("jPanel2-jScrollPane2-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jButton2-jScrollPane2-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("jPanel2-jScrollPane2-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jButton1-jScrollPane2-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("jPanel2-jScrollPane2-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jTabbedPane1-jScrollPane2-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jScrollPane2-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jScrollPane2-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("jPanel2-jScrollPane2-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        ld.endMoving(true);
// < END MOVING
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("jPanel1", new Rectangle(25, 50, 137, 220));
        contInterior.put("jPanel1", new Rectangle(25, 50, 137, 220));
        compMinSize.put("jPanel1", new Dimension(0, 0));
        compBounds.put("jPanel1", new Rectangle(25, 50, 137, 220));
        compBounds.put("Form", new Rectangle(0, 0, 522, 339));
        contInterior.put("Form", new Rectangle(0, 0, 522, 339));
        compBounds.put("jPanel2", new Rectangle(10, 11, 502, 317));
        baselinePosition.put("jPanel2-502-317", new Integer(0));
        contInterior.put("jPanel2", new Rectangle(13, 14, 504, 311));
        compBounds.put("jLabel1", new Rectangle(23, 300, 484, 14));
        baselinePosition.put("jLabel1-484-14", new Integer(11));
        compBounds.put("jTabbedPane1", new Rectangle(23, 25, 142, 248));
        baselinePosition.put("jTabbedPane1-142-248", new Integer(0));
        compBounds.put("jScrollPane2", new Rectangle(284, 25, 195, 248));
        baselinePosition.put("jScrollPane2-195-248", new Integer(0));
        compBounds.put("jButton2", new Rectangle(195, 191, 73, 23));
        baselinePosition.put("jButton2-73-23", new Integer(15));
        compBounds.put("jButton1", new Rectangle(169, 232, 73, 23));
        baselinePosition.put("jButton1-73-23", new Integer(15));
        compMinSize.put("jPanel2", new Dimension(510, 317));
        compBounds.put("jPanel2", new Rectangle(10, 11, 502, 317));
        compPrefSize.put("jPanel2", new Dimension(510, 317));
        prefPaddingInParent.put("jPanel2-jLabel1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jTabbedPane1-jScrollPane2-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTabbedPane1-jScrollPane2-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTabbedPane1-jScrollPane2-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTabbedPane1-jScrollPane2-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jScrollPane2-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jScrollPane2-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jScrollPane2-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jScrollPane2-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jScrollPane2-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jScrollPane2-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jScrollPane2-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jScrollPane2-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        hasExplicitPrefSize.put("jPanel2", new Boolean(false));
        prefPaddingInParent.put("jPanel2-jLabel1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel2-jTabbedPane1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel2-jScrollPane2-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        compPrefSize.put("jButton2", new Dimension(73, 23));
        prefPadding.put("jButton2-jButton1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        compPrefSize.put("jButton1", new Dimension(73, 23));
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        prefPaddingInParent.put("jPanel2-jLabel1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        compPrefSize.put("jTabbedPane1", new Dimension(142, 248));
        compPrefSize.put("jScrollPane2", new Dimension(166, 96));
        hasExplicitPrefSize.put("jPanel2", new Boolean(false));
        compMinSize.put("Form", new Dimension(522, 339));
        compBounds.put("Form", new Rectangle(0, 0, 522, 339));
        prefPaddingInParent.put("Form-jPanel2-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jPanel2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jPanel2-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        compPrefSize.put("jPanel2", new Dimension(510, 317));
        prefPaddingInParent.put("Form-jPanel2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        compBounds.put("jPanel3", new Rectangle(25, 50, 137, 220));
        contInterior.put("jPanel3", new Rectangle(25, 50, 137, 220));
        compMinSize.put("jPanel3", new Dimension(0, 0));
        compBounds.put("jPanel3", new Rectangle(25, 50, 137, 220));
        compBounds.put("jPanel1", new Rectangle(25, 50, 137, 220));
        contInterior.put("jPanel1", new Rectangle(25, 50, 137, 220));
        compMinSize.put("jPanel1", new Dimension(0, 0));
        compBounds.put("jPanel1", new Rectangle(25, 50, 137, 220));
        compBounds.put("Form", new Rectangle(0, 0, 522, 339));
        contInterior.put("Form", new Rectangle(0, 0, 522, 339));
        compBounds.put("jPanel2", new Rectangle(10, 11, 502, 317));
        baselinePosition.put("jPanel2-502-317", new Integer(0));
        contInterior.put("jPanel2", new Rectangle(13, 14, 504, 311));
        compBounds.put("jLabel1", new Rectangle(23, 300, 484, 14));
        baselinePosition.put("jLabel1-484-14", new Integer(11));
        compBounds.put("jTabbedPane1", new Rectangle(23, 25, 142, 248));
        baselinePosition.put("jTabbedPane1-142-248", new Integer(0));
        compBounds.put("jScrollPane2", new Rectangle(284, 25, 195, 248));
        baselinePosition.put("jScrollPane2-195-248", new Integer(0));
        compBounds.put("jButton2", new Rectangle(195, 191, 73, 23));
        baselinePosition.put("jButton2-73-23", new Integer(15));
        compBounds.put("jButton1", new Rectangle(169, 232, 73, 23));
        baselinePosition.put("jButton1-73-23", new Integer(15));
        compMinSize.put("jPanel2", new Dimension(510, 317));
        compBounds.put("jPanel2", new Rectangle(10, 11, 502, 317));
        prefPaddingInParent.put("jPanel2-jLabel1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jTabbedPane1-jScrollPane2-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTabbedPane1-jScrollPane2-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTabbedPane1-jScrollPane2-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTabbedPane1-jScrollPane2-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jScrollPane2-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jScrollPane2-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jScrollPane2-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jScrollPane2-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jScrollPane2-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jScrollPane2-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jScrollPane2-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jScrollPane2-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("jPanel2-jLabel1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        compPrefSize.put("jTabbedPane1", new Dimension(142, 248));
        compPrefSize.put("jScrollPane2", new Dimension(166, 96));
        compMinSize.put("Form", new Dimension(522, 339));
        compBounds.put("Form", new Rectangle(0, 0, 522, 339));
        prefPaddingInParent.put("Form-jPanel2-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jPanel2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        compBounds.put("jPanel3", new Rectangle(25, 50, 137, 220));
        contInterior.put("jPanel3", new Rectangle(25, 50, 137, 220));
        compMinSize.put("jPanel3", new Dimension(0, 0));
        compBounds.put("jPanel3", new Rectangle(25, 50, 137, 220));
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
    }
}
