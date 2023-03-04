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
 * Vertical resizing of a component (jPanel2) in a sequence (jPanel1, jPanel2,
 * jButton1) to align with a component in another sequence (jButton2).
 * This test differs to ALT_SeqResizing04Test in that here the neighbor in the
 * original sequence (jButton1) is at preferred gap distance.
 */
public class ALT_SeqResizing03Test extends LayoutTestCase {

    public ALT_SeqResizing03Test(String name) {
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
     * Resize the gray panel upwards so it's top edge is aligned with the
     * jButton2's top edge.
     */
    public void doChanges0() {
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        contInterior.put("jPanel1", new Rectangle(26, 30, 100, 100));
        compBounds.put("jPanel1", new Rectangle(26, 30, 100, 100));
        baselinePosition.put("jPanel1-100-100", new Integer(0));
        compMinSize.put("jPanel1", new Dimension(0, 0));
        compPrefSize.put("jPanel1", new Dimension(100, 100));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        contInterior.put("Form", new Rectangle(0, 0, 400, 349));
        compBounds.put("jPanel1", new Rectangle(26, 30, 100, 100));
        baselinePosition.put("jPanel1-100-100", new Integer(0));
        compBounds.put("jButton2", new Rectangle(317, 54, 73, 23));
        baselinePosition.put("jButton2-73-23", new Integer(15));
        compPrefSize.put("jButton2", new Dimension(73, 23));
        compBounds.put("jPanel2", new Rectangle(180, 156, 100, 100));
        baselinePosition.put("jPanel2-100-100", new Integer(0));
        compBounds.put("jButton3", new Rectangle(317, 185, 73, 23));
        baselinePosition.put("jButton3-73-23", new Integer(15));
        compPrefSize.put("jButton3", new Dimension(73, 23));
        compBounds.put("jButton1", new Rectangle(43, 262, 73, 23));
        baselinePosition.put("jButton1-73-23", new Integer(15));
        compPrefSize.put("jButton1", new Dimension(73, 23));
        contInterior.put("jPanel2", new Rectangle(180, 156, 100, 100));
        compBounds.put("jPanel2", new Rectangle(180, 156, 100, 100));
        baselinePosition.put("jPanel2-100-100", new Integer(0));
        compMinSize.put("jPanel2", new Dimension(0, 0));
        compPrefSize.put("jPanel2", new Dimension(100, 100));
        hasExplicitPrefSize.put("jPanel2", new Boolean(false));
        hasExplicitPrefSize.put("jPanel2", new Boolean(false));
        contInterior.put("jPanel1", new Rectangle(26, 30, 100, 100));
        contInterior.put("Form", new Rectangle(0, 0, 400, 349));
        compBounds.put("jPanel1", new Rectangle(26, 30, 100, 100));
        baselinePosition.put("jPanel1-100-100", new Integer(0));
        compBounds.put("jButton2", new Rectangle(317, 54, 73, 23));
        baselinePosition.put("jButton2-73-23", new Integer(15));
        compBounds.put("jPanel2", new Rectangle(180, 156, 100, 100));
        baselinePosition.put("jPanel2-100-100", new Integer(0));
        compBounds.put("jButton3", new Rectangle(317, 185, 73, 23));
        baselinePosition.put("jButton3-73-23", new Integer(15));
        compBounds.put("jButton1", new Rectangle(43, 262, 73, 23));
        baselinePosition.put("jButton1-73-23", new Integer(15));
        contInterior.put("jPanel2", new Rectangle(180, 156, 100, 100));
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
// > START RESIZING
        baselinePosition.put("jPanel2-100-100", new Integer(0));
        compMinSize.put("jPanel2", new Dimension(0, 0));
        {
            String[] compIds = new String[]{"jPanel2"};
            Rectangle[] bounds = new Rectangle[]{new Rectangle(180, 156, 100, 100)};
            Point hotspot = new Point(231, 155);
            int[] resizeEdges = new int[]{-1, 0};
            boolean inLayout = true;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
// < START RESIZING
        prefPaddingInParent.put("Form-jPanel2-1-0", new Integer(11));
        // parentId-compId-dimension-compAlignment
// > MOVE
        {
            Point p = new Point(235, 60);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(180, 54, 100, 202)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
        prefPaddingInParent.put("Form-jPanel2-1-0", new Integer(11));
        // parentId-compId-dimension-compAlignment
// > MOVE
        {
            Point p = new Point(235, 59);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(180, 54, 100, 202)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
// > END MOVING
        prefPadding.put("jPanel1-jPanel2-0-0-0", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPanel2-jButton2-0-0-0", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jPanel2-0-0", new Integer(10));
        // parentId-compId-dimension-compAlignment
        prefPadding.put("jPanel2-jButton3-0-0-0", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPanel1-jPanel2-0-0-0", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPanel2-jButton2-0-0-0", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPanel2-jButton3-0-0-0", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        contInterior.put("jPanel2", new Rectangle(-32588, -32714, 100, 202));
        compBounds.put("jPanel2", new Rectangle(-32588, -32714, 100, 202));
        baselinePosition.put("jPanel2-100-202", new Integer(0));
        compMinSize.put("jPanel2", new Dimension(0, 0));
        compPrefSize.put("jPanel2", new Dimension(100, 100));
        hasExplicitPrefSize.put("jPanel2", new Boolean(false));
        hasExplicitPrefSize.put("jPanel2", new Boolean(false));
        ld.endMoving(true);
// < END MOVING
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        contInterior.put("jPanel1", new Rectangle(26, 30, 100, 100));
        compBounds.put("jPanel1", new Rectangle(26, 30, 100, 100));
        baselinePosition.put("jPanel1-100-100", new Integer(0));
        compMinSize.put("jPanel1", new Dimension(0, 0));
        compPrefSize.put("jPanel1", new Dimension(100, 100));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        contInterior.put("Form", new Rectangle(0, 0, 400, 349));
        compBounds.put("jPanel1", new Rectangle(26, 30, 100, 100));
        baselinePosition.put("jPanel1-100-100", new Integer(0));
        compBounds.put("jButton2", new Rectangle(317, 54, 73, 23));
        baselinePosition.put("jButton2-73-23", new Integer(15));
        compPrefSize.put("jButton2", new Dimension(73, 23));
        compBounds.put("jButton3", new Rectangle(317, 185, 73, 23));
        baselinePosition.put("jButton3-73-23", new Integer(15));
        compPrefSize.put("jButton3", new Dimension(73, 23));
        compBounds.put("jButton1", new Rectangle(43, 262, 73, 23));
        baselinePosition.put("jButton1-73-23", new Integer(15));
        compPrefSize.put("jButton1", new Dimension(73, 23));
        compBounds.put("jPanel2", new Rectangle(180, 54, 100, 202));
        baselinePosition.put("jPanel2-100-202", new Integer(0));
        contInterior.put("jPanel2", new Rectangle(180, 54, 100, 202));
        compBounds.put("jPanel2", new Rectangle(180, 54, 100, 202));
        baselinePosition.put("jPanel2-100-202", new Integer(0));
        compMinSize.put("jPanel2", new Dimension(0, 0));
        compPrefSize.put("jPanel2", new Dimension(100, 202));
        hasExplicitPrefSize.put("jPanel2", new Boolean(false));
        hasExplicitPrefSize.put("jPanel2", new Boolean(false));
        contInterior.put("jPanel1", new Rectangle(26, 30, 100, 100));
        contInterior.put("Form", new Rectangle(0, 0, 400, 349));
        compBounds.put("jPanel1", new Rectangle(26, 30, 100, 100));
        baselinePosition.put("jPanel1-100-100", new Integer(0));
        compBounds.put("jButton2", new Rectangle(317, 54, 73, 23));
        baselinePosition.put("jButton2-73-23", new Integer(15));
        compBounds.put("jButton3", new Rectangle(317, 185, 73, 23));
        baselinePosition.put("jButton3-73-23", new Integer(15));
        compBounds.put("jButton1", new Rectangle(43, 262, 73, 23));
        baselinePosition.put("jButton1-73-23", new Integer(15));
        compBounds.put("jPanel2", new Rectangle(180, 54, 100, 202));
        baselinePosition.put("jPanel2-100-202", new Integer(0));
        contInterior.put("jPanel2", new Rectangle(180, 54, 100, 202));
        ld.updateCurrentState();
    }

}
