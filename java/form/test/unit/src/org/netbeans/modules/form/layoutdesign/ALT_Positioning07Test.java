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

public class ALT_Positioning07Test extends LayoutTestCase {

    public ALT_Positioning07Test(String name) {
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
     * Move jButton2 on the line with the label so it just snaps next to the list.
     */
    public void doChanges0() {
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 479, 300));
        compBounds.put("jScrollPane1", new Rectangle(54, 57, 35, 130));
        baselinePosition.put("jScrollPane1-35-130", 0);
        compBounds.put("jLabel1", new Rectangle(387, 136, 34, 14));
        baselinePosition.put("jLabel1-34-14", 11);
        compBounds.put("jButton1", new Rectangle(137, 73, 73, 23));
        baselinePosition.put("jButton1-73-23", 15);
        compBounds.put("jButton2", new Rectangle(137, 102, 73, 23));
        baselinePosition.put("jButton2-73-23", 15);
        prefPaddingInParent.put("Form-jButton1-0-1", 10); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton2-0-1", 10); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollPane1-1-1", 11); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel1-1-1", 11); // parentId-compId-dimension-compAlignment
        contInterior.put("Form", new Rectangle(0, 0, 479, 300));
        compBounds.put("jScrollPane1", new Rectangle(54, 57, 35, 130));
        baselinePosition.put("jScrollPane1-35-130", 0);
        compBounds.put("jLabel1", new Rectangle(387, 136, 34, 14));
        baselinePosition.put("jLabel1-34-14", 11);
        compBounds.put("jButton1", new Rectangle(137, 73, 73, 23));
        baselinePosition.put("jButton1-73-23", 15);
        compBounds.put("jButton2", new Rectangle(137, 102, 73, 23));
        baselinePosition.put("jButton2-73-23", 15);
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
        // > START MOVING
        baselinePosition.put("jButton2-73-23", 15);
        {
            String[] compIds = new String[]{"jButton2"};
            Rectangle[] bounds = new Rectangle[]{new Rectangle(137, 102, 73, 23)};
            Point hotspot = new Point(179, 113);
            ld.startMoving(compIds, bounds, hotspot);
        }
        // < START MOVING
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
        prefPaddingInParent.put("Form-jButton2-0-0", 10); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton2-0-1", 10); // parentId-compId-dimension-compAlignment
        prefPadding.put("jScrollPane1-jButton2-0-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jButton2-0-0-1", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jButton2-0-0-2", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jButton2-0-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jScrollPane1-0-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jScrollPane1-0-0-1", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jScrollPane1-0-0-2", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jScrollPane1-0-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jLabel1-0-0-0", 4); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jLabel1-0-0-1", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jLabel1-0-0-2", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jLabel1-0-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        // > MOVE
        // > MOVE
        {
            Point p = new Point(135, 140);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(95, 132, 73, 23)};
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
        prefPaddingInParent.put("Form-jButton2-0-0", 10); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton2-0-1", 10); // parentId-compId-dimension-compAlignment
        prefPadding.put("jScrollPane1-jButton2-0-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jButton2-0-0-1", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jButton2-0-0-2", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jButton2-0-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jScrollPane1-0-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jScrollPane1-0-0-1", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jScrollPane1-0-0-2", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jScrollPane1-0-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jLabel1-0-0-0", 4); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jLabel1-0-0-1", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jLabel1-0-0-2", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jLabel1-0-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        // > MOVE
        // > MOVE
        {
            Point p = new Point(133, 140);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(95, 132, 73, 23)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        prefPaddingInParent.put("Form-jButton1-0-1", 10); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollPane1-1-1", 11); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel1-1-1", 11); // parentId-compId-dimension-compAlignment
        prefPadding.put("jButton2-jLabel1-0-0-0", 4); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 479, 300));
        compBounds.put("jScrollPane1", new Rectangle(54, 57, 35, 130));
        baselinePosition.put("jScrollPane1-35-130", 0);
        compBounds.put("jLabel1", new Rectangle(387, 136, 34, 14));
        baselinePosition.put("jLabel1-34-14", 11);
        compBounds.put("jButton1", new Rectangle(137, 73, 73, 23));
        baselinePosition.put("jButton1-73-23", 15);
        compBounds.put("jButton2", new Rectangle(95, 132, 73, 23));
        baselinePosition.put("jButton2-73-23", 15);
        prefPadding.put("jScrollPane1-jButton2-0-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jButton1-0-1", 10); // parentId-compId-dimension-compAlignment
        prefPadding.put("jButton2-jLabel1-0-0-0", 4); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jLabel1-0-0-1", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jLabel1-0-0-2", 10); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jLabel1-0-0-3", 18); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jScrollPane1-1-1", 11); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel1-1-1", 11); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton2-1-1", 11); // parentId-compId-dimension-compAlignment
        contInterior.put("Form", new Rectangle(0, 0, 479, 300));
        compBounds.put("jScrollPane1", new Rectangle(54, 57, 35, 130));
        baselinePosition.put("jScrollPane1-35-130", 0);
        compBounds.put("jLabel1", new Rectangle(387, 136, 34, 14));
        baselinePosition.put("jLabel1-34-14", 11);
        compBounds.put("jButton1", new Rectangle(137, 73, 73, 23));
        baselinePosition.put("jButton1-73-23", 15);
        compBounds.put("jButton2", new Rectangle(95, 132, 73, 23));
        baselinePosition.put("jButton2-73-23", 15);
        prefPadding.put("jScrollPane1-jButton2-0-0-0", 6); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }

}
