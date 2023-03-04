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

public class ALT_Resizing10Test extends LayoutTestCase {

    public ALT_Resizing10Test(String name) {
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
     * Resize jButton1 slightly to the right. There used to be a bug that cut
     * the last gap to compensate.
     */
    public void doChanges0() {
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 420, 300));
        compBounds.put("jToggleButton1", new Rectangle(57, 71, 105, 23));
        baselinePosition.put("jToggleButton1-105-23", new Integer(15));
        compBounds.put("jButton1", new Rectangle(57, 100, 105, 23));
        baselinePosition.put("jButton1-105-23", new Integer(15));
        compBounds.put("jButton2", new Rectangle(57, 129, 105, 23));
        baselinePosition.put("jButton2-105-23", new Integer(15));
        compBounds.put("jScrollPane1", new Rectangle(168, 50, 35, 130));
        baselinePosition.put("jScrollPane1-35-130", new Integer(0));
        compBounds.put("jTextField1", new Rectangle(209, 81, 181, 20));
        baselinePosition.put("jTextField1-181-20", new Integer(14));
        compBounds.put("jTextField2", new Rectangle(209, 112, 181, 20));
        baselinePosition.put("jTextField2-181-20", new Integer(14));
        compPrefSize.put("jToggleButton1", new Dimension(105, 23));
        compPrefSize.put("jButton1", new Dimension(73, 23));
        compPrefSize.put("jButton2", new Dimension(73, 23));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compPrefSize.put("jTextField2", new Dimension(59, 20));
        prefPaddingInParent.put("Form-jButton2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollPane1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jTextField2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        contInterior.put("Form", new Rectangle(0, 0, 420, 300));
        compBounds.put("jToggleButton1", new Rectangle(57, 71, 105, 23));
        baselinePosition.put("jToggleButton1-105-23", new Integer(15));
        compBounds.put("jButton1", new Rectangle(57, 100, 105, 23));
        baselinePosition.put("jButton1-105-23", new Integer(15));
        compBounds.put("jButton2", new Rectangle(57, 129, 105, 23));
        baselinePosition.put("jButton2-105-23", new Integer(15));
        compBounds.put("jScrollPane1", new Rectangle(168, 50, 35, 130));
        baselinePosition.put("jScrollPane1-35-130", new Integer(0));
        compBounds.put("jTextField1", new Rectangle(209, 81, 181, 20));
        baselinePosition.put("jTextField1-181-20", new Integer(14));
        compBounds.put("jTextField2", new Rectangle(209, 112, 181, 20));
        baselinePosition.put("jTextField2-181-20", new Integer(14));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
        // > START RESIZING
        baselinePosition.put("jButton1-105-23", new Integer(15));
        compPrefSize.put("jButton1", new Dimension(73, 23));
        {
            String[] compIds = new String[]{"jButton1"};
            Rectangle[] bounds = new Rectangle[]{new Rectangle(57, 100, 105, 23)};
            Point hotspot = new Point(163, 116);
            int[] resizeEdges = new int[]{1, -1};
            boolean inLayout = true;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
        // < START RESIZING
        prefPadding.put("jButton1-jScrollPane1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        // > MOVE
        // > MOVE
        {
            Point p = new Point(180, 119);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(57, 100, 122, 23)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPadding.put("jButton1-jScrollPane1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        // > MOVE
        // > MOVE
        {
            Point p = new Point(181, 119);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(57, 100, 123, 23)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 420, 300));
        compBounds.put("jToggleButton1", new Rectangle(57, 71, 105, 23));
        baselinePosition.put("jToggleButton1-123-23", new Integer(15));
        compBounds.put("jButton1", new Rectangle(57, 100, 123, 23));
        baselinePosition.put("jButton1-123-23", new Integer(15));
        compBounds.put("jButton2", new Rectangle(57, 129, 105, 23));
        baselinePosition.put("jButton2-123-23", new Integer(15));
        compBounds.put("jScrollPane1", new Rectangle(186, 50, 35, 130));
        baselinePosition.put("jScrollPane1-35-130", new Integer(0));
        compBounds.put("jTextField1", new Rectangle(227, 81, 163, 20));
        baselinePosition.put("jTextField1-163-20", new Integer(14));
        compBounds.put("jTextField2", new Rectangle(227, 112, 163, 20));
        baselinePosition.put("jTextField2-163-20", new Integer(14));
        compPrefSize.put("jToggleButton1", new Dimension(105, 23));
        compPrefSize.put("jButton2", new Dimension(73, 23));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compPrefSize.put("jTextField2", new Dimension(59, 20));
        prefPaddingInParent.put("Form-jButton2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollPane1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jTextField2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        contInterior.put("Form", new Rectangle(0, 0, 420, 300));
        compBounds.put("jScrollPane1", new Rectangle(186, 50, 35, 130));
        baselinePosition.put("jScrollPane1-35-130", new Integer(0));
        compBounds.put("jTextField1", new Rectangle(227, 81, 163, 20));
        baselinePosition.put("jTextField1-163-20", new Integer(14));
        compBounds.put("jTextField2", new Rectangle(227, 112, 163, 20));
        baselinePosition.put("jTextField2-163-20", new Integer(14));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }

}
