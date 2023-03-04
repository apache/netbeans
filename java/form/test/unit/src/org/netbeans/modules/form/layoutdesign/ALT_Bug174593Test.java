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

public class ALT_Bug174593Test extends LayoutTestCase {

    public ALT_Bug174593Test(String name) {
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
     * Resize jPanel2 to align at bottom with jPanel1. Used to cause AE due to
     * two consecutive gaps appearing in layout before eliminated later.
     */
    public void doChanges0() {
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("jPanel1", new Rectangle(11, 12, 227, 226));
        compBounds.put("jPanel1", new Rectangle(10, 11, 229, 228));
        baselinePosition.put("jPanel1-229-228", new Integer(0));
        compMinSize.put("jPanel1", new Dimension(2, 2));
        compPrefSize.put("jPanel1", new Dimension(229, 228));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        contInterior.put("Form", new Rectangle(0, 0, 433, 292));
        compBounds.put("jComboBox1", new Rectangle(10, 250, 229, 20));
        baselinePosition.put("jComboBox1-229-20", new Integer(14));
        compPrefSize.put("jComboBox1", new Dimension(56, 20));
        compBounds.put("jPanel1", new Rectangle(10, 11, 229, 228));
        baselinePosition.put("jPanel1-229-228", new Integer(0));
        compBounds.put("jLabel1", new Rectangle(249, 11, 72, 64));
        baselinePosition.put("jLabel1-72-64", new Integer(36));
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        compBounds.put("jPanel2", new Rectangle(245, 81, 102, 116));
        baselinePosition.put("jPanel2-102-116", new Integer(0));
        contInterior.put("jPanel2", new Rectangle(246, 82, 100, 114));
        compBounds.put("jPanel2", new Rectangle(245, 81, 102, 116));
        baselinePosition.put("jPanel2-102-116", new Integer(0));
        compMinSize.put("jPanel2", new Dimension(2, 2));
        compPrefSize.put("jPanel2", new Dimension(102, 116));
        hasExplicitPrefSize.put("jPanel2", new Boolean(false));
        hasExplicitPrefSize.put("jPanel2", new Boolean(false));
        contInterior.put("jPanel1", new Rectangle(11, 12, 227, 226));
        contInterior.put("Form", new Rectangle(0, 0, 433, 292));
        compBounds.put("jComboBox1", new Rectangle(10, 250, 229, 20));
        baselinePosition.put("jComboBox1-229-20", new Integer(14));
        compBounds.put("jPanel1", new Rectangle(10, 11, 229, 228));
        baselinePosition.put("jPanel1-229-228", new Integer(0));
        compBounds.put("jLabel1", new Rectangle(249, 11, 72, 64));
        baselinePosition.put("jLabel1-72-64", new Integer(36));
        compBounds.put("jPanel2", new Rectangle(245, 81, 102, 116));
        baselinePosition.put("jPanel2-102-116", new Integer(0));
        contInterior.put("jPanel2", new Rectangle(246, 82, 100, 114));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
        // > START RESIZING
        baselinePosition.put("jPanel2-102-116", new Integer(0));
        compMinSize.put("jPanel2", new Dimension(2, 2));
        {
            String[] compIds = new String[]{"jPanel2"};
            Rectangle[] bounds = new Rectangle[]{new Rectangle(245, 81, 102, 116)};
            Point hotspot = new Point(285, 198);
            int[] resizeEdges = new int[]{-1, 1};
            boolean inLayout = true;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
        // < START RESIZING
        prefPaddingInParent.put("Form-jPanel2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        // > MOVE
        // > MOVE
        {
            Point p = new Point(269, 237);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(245, 81, 102, 158)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPaddingInParent.put("Form-jPanel2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        // > MOVE
        // > MOVE
        {
            Point p = new Point(269, 238);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(245, 81, 102, 158)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        prefPaddingInParent.put("Form-jPanel2-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        contInterior.put("jPanel2", new Rectangle(-32522, -32686, 100, 156));
        compBounds.put("jPanel2", new Rectangle(-32523, -32687, 102, 158));
        baselinePosition.put("jPanel2-102-158", new Integer(0));
        compMinSize.put("jPanel2", new Dimension(2, 2));
        compPrefSize.put("jPanel2", new Dimension(102, 116));
        hasExplicitPrefSize.put("jPanel2", new Boolean(false));
        hasExplicitPrefSize.put("jPanel2", new Boolean(false));
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("jPanel1", new Rectangle(11, 12, 227, 226));
        compBounds.put("jPanel1", new Rectangle(10, 11, 229, 228));
        baselinePosition.put("jPanel1-229-228", new Integer(0));
        compMinSize.put("jPanel1", new Dimension(2, 2));
        compPrefSize.put("jPanel1", new Dimension(229, 228));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        contInterior.put("Form", new Rectangle(0, 0, 433, 292));
        compBounds.put("jComboBox1", new Rectangle(10, 250, 229, 20));
        baselinePosition.put("jComboBox1-229-20", new Integer(14));
        compPrefSize.put("jComboBox1", new Dimension(56, 20));
        compBounds.put("jPanel1", new Rectangle(10, 11, 229, 228));
        baselinePosition.put("jPanel1-229-228", new Integer(0));
        compBounds.put("jLabel1", new Rectangle(249, 11, 72, 64));
        baselinePosition.put("jLabel1-72-64", new Integer(36));
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        compBounds.put("jPanel2", new Rectangle(245, 81, 102, 158));
        baselinePosition.put("jPanel2-102-158", new Integer(0));
        contInterior.put("jPanel2", new Rectangle(246, 82, 100, 156));
        compBounds.put("jPanel2", new Rectangle(245, 81, 102, 158));
        baselinePosition.put("jPanel2-102-158", new Integer(0));
        compMinSize.put("jPanel2", new Dimension(2, 2));
        compPrefSize.put("jPanel2", new Dimension(102, 158));
        hasExplicitPrefSize.put("jPanel2", new Boolean(false));
        hasExplicitPrefSize.put("jPanel2", new Boolean(false));
        contInterior.put("jPanel1", new Rectangle(11, 12, 227, 226));
        contInterior.put("Form", new Rectangle(0, 0, 433, 292));
        compBounds.put("jComboBox1", new Rectangle(10, 250, 229, 20));
        baselinePosition.put("jComboBox1-229-20", new Integer(14));
        compBounds.put("jPanel1", new Rectangle(10, 11, 229, 228));
        baselinePosition.put("jPanel1-229-228", new Integer(0));
        compBounds.put("jLabel1", new Rectangle(249, 11, 72, 64));
        baselinePosition.put("jLabel1-72-64", new Integer(36));
        compBounds.put("jPanel2", new Rectangle(245, 81, 102, 158));
        baselinePosition.put("jPanel2-102-158", new Integer(0));
        contInterior.put("jPanel2", new Rectangle(246, 82, 100, 156));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }

}
