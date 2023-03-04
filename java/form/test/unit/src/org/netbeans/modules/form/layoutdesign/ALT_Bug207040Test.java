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

public class ALT_Bug207040Test extends LayoutTestCase {

    public ALT_Bug207040Test(String name) {
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
     * Resize jTextField1 by its left edge slightly to the right.
     */
    public void doChanges0() {
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 396, 326));
        contInterior.put("Form", new Rectangle(0, 0, 396, 326));
        compBounds.put("jTextField1", new Rectangle(10, 12, 297, 20));
        baselinePosition.put("jTextField1-297-20", new Integer(14));
        compBounds.put("jButton1", new Rectangle(313, 11, 73, 23));
        baselinePosition.put("jButton1-73-23", new Integer(15));
        compBounds.put("jScrollPane1", new Rectangle(11, 71, 375, 244));
        baselinePosition.put("jScrollPane1-375-244", new Integer(0));
        compBounds.put("jTextField2", new Rectangle(100, 40, 59, 20));
        baselinePosition.put("jTextField2-59-20", new Integer(14));
        compMinSize.put("Form", new Dimension(395, 326));
        compBounds.put("Form", new Rectangle(0, 0, 396, 326));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        prefPaddingInParent.put("Form-jScrollPane1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
// > START RESIZING
        baselinePosition.put("jTextField1-297-20", new Integer(14));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        {
            String[] compIds = new String[]{
                "jTextField1"
            };
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(10, 12, 297, 20)
            };
            Point hotspot = new Point(10, 20);
            int[] resizeEdges = new int[]{
                0,
                -1
            };
            boolean inLayout = true;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
// < START RESIZING
        prefPaddingInParent.put("Form-jTextField1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
// > MOVE
        {
            Point p = new Point(40, 28);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(40, 12, 267, 20)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
        prefPaddingInParent.put("Form-jTextField1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
// > MOVE
        {
            Point p = new Point(42, 28);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(42, 12, 265, 20)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
// > END MOVING
        prefPaddingInParent.put("Form-jScrollPane1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        ld.endMoving(true);
// < END MOVING
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 396, 326));
        contInterior.put("Form", new Rectangle(0, 0, 396, 326));
        compBounds.put("jTextField1", new Rectangle(42, 12, 265, 20));
        baselinePosition.put("jTextField1-265-20", new Integer(14));
        compBounds.put("jButton1", new Rectangle(313, 11, 73, 23));
        baselinePosition.put("jButton1-73-23", new Integer(15));
        compBounds.put("jScrollPane1", new Rectangle(11, 71, 375, 244));
        baselinePosition.put("jScrollPane1-375-244", new Integer(0));
        compBounds.put("jTextField2", new Rectangle(90, 40, 59, 20));
        baselinePosition.put("jTextField2-59-20", new Integer(14));
        compMinSize.put("Form", new Dimension(385, 326));
        compBounds.put("Form", new Rectangle(0, 0, 396, 326));
        prefPaddingInParent.put("Form-jScrollPane1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        compBounds.put("Form", new Rectangle(0, 0, 396, 326));
        contInterior.put("Form", new Rectangle(0, 0, 396, 326));
        compBounds.put("jTextField1", new Rectangle(42, 12, 265, 20));
        baselinePosition.put("jTextField1-265-20", new Integer(14));
        compBounds.put("jButton1", new Rectangle(313, 11, 73, 23));
        baselinePosition.put("jButton1-73-23", new Integer(15));
        compBounds.put("jScrollPane1", new Rectangle(11, 71, 375, 244));
        baselinePosition.put("jScrollPane1-375-244", new Integer(0));
        compBounds.put("jTextField2", new Rectangle(90, 40, 59, 20));
        baselinePosition.put("jTextField2-59-20", new Integer(14));
        compMinSize.put("Form", new Dimension(385, 326));
        compBounds.put("Form", new Rectangle(0, 0, 396, 326));
        prefPaddingInParent.put("Form-jScrollPane1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
    }
}
