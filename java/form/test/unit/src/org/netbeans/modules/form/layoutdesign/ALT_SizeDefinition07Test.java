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

/**
 * Tests setting the supporting size of the GUI form to the right component.
 */
public class ALT_SizeDefinition07Test extends LayoutTestCase {

    public ALT_SizeDefinition07Test(String name) {
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
     * Invoke Set to Default Size on the form. This also resets all resizing
     * intervals to be flexibly assignable with default or explicit size in
     * order to define the form size.
     */
    public void doChanges0() {
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 400, 300));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jTextField1", new Rectangle(0, 0, 400, 20));
        baselinePosition.put("jTextField1-400-20", new Integer(14));
        compBounds.put("jCheckBox1", new Rectangle(10, 22, 81, 23));
        baselinePosition.put("jCheckBox1-81-23", new Integer(15));
        compBounds.put("jCheckBox2", new Rectangle(10, 45, 81, 23));
        baselinePosition.put("jCheckBox2-81-23", new Integer(15));
        compBounds.put("jScrollPane1", new Rectangle(10, 80, 35, 86));
        baselinePosition.put("jScrollPane1-35-86", new Integer(0));
        compBounds.put("jToggleButton1", new Rectangle(78, 143, 271, 23));
        baselinePosition.put("jToggleButton1-271-23", new Integer(15));
        compBounds.put("jButton1", new Rectangle(78, 114, 271, 23));
        baselinePosition.put("jButton1-271-23", new Integer(15));
        compBounds.put("jTextField2", new Rectangle(78, 83, 271, 20));
        baselinePosition.put("jTextField2-271-20", new Integer(14));
        compBounds.put("jScrollPane2", new Rectangle(355, 116, 35, 50));
        baselinePosition.put("jScrollPane2-35-50", new Integer(0));
        compMinSize.put("Form", new Dimension(234, 177));
        compBounds.put("Form", new Rectangle(0, 0, 400, 300));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compPrefSize.put("jToggleButton1", new Dimension(105, 23));
        compPrefSize.put("jButton1", new Dimension(73, 23));
        compPrefSize.put("jTextField2", new Dimension(59, 20));
        prefPaddingInParent.put("Form-jScrollPane2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollPane1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jToggleButton1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
// > SET DEFAULT SIZE
        ld.setDefaultSize("Form");
// < SET DEFAULT SIZE
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 234, 177));
        contInterior.put("Form", new Rectangle(0, 0, 234, 177));
        compBounds.put("jTextField1", new Rectangle(0, 0, 234, 20));
        baselinePosition.put("jTextField1-234-20", new Integer(14));
        compBounds.put("jCheckBox1", new Rectangle(10, 22, 81, 23));
        baselinePosition.put("jCheckBox1-81-23", new Integer(15));
        compBounds.put("jCheckBox2", new Rectangle(10, 45, 81, 23));
        baselinePosition.put("jCheckBox2-81-23", new Integer(15));
        compBounds.put("jScrollPane1", new Rectangle(10, 80, 35, 86));
        baselinePosition.put("jScrollPane1-35-86", new Integer(0));
        compBounds.put("jToggleButton1", new Rectangle(78, 143, 105, 23));
        baselinePosition.put("jToggleButton1-105-23", new Integer(15));
        compBounds.put("jButton1", new Rectangle(78, 114, 105, 23));
        baselinePosition.put("jButton1-105-23", new Integer(15));
        compBounds.put("jTextField2", new Rectangle(78, 83, 105, 20));
        baselinePosition.put("jTextField2-105-20", new Integer(14));
        compBounds.put("jScrollPane2", new Rectangle(189, 116, 35, 50));
        baselinePosition.put("jScrollPane2-35-50", new Integer(0));
        compMinSize.put("Form", new Dimension(234, 177));
        compBounds.put("Form", new Rectangle(0, 0, 234, 177));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compPrefSize.put("jToggleButton1", new Dimension(105, 23));
        compPrefSize.put("jButton1", new Dimension(73, 23));
        compPrefSize.put("jTextField2", new Dimension(59, 20));
        prefPaddingInParent.put("Form-jScrollPane2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollPane1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jToggleButton1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
    }

    /**
     * Resize the entire form horizontally, make it bigger than the initial
     * state when opened. The supporting size should be set to jTextField2.
     */
    public void doChanges1() {
// > START RESIZING
        baselinePosition.put("Form-234-177", new Integer(0));
        compMinSize.put("Form", new Dimension(234, 177));
        prefPaddingInParent.put("Form-jScrollPane2-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        {
            String[] compIds = new String[]{
                "Form"
            };
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(0, 0, 234, 177)
            };
            Point hotspot = new Point(237, 81);
            int[] resizeEdges = new int[]{
                1,
                -1
            };
            boolean inLayout = false;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
// < START RESIZING
// > MOVE
        {
            Point p = new Point(461, 95);
            String containerId = null;
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(0, 0, 458, 177)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
// > MOVE
        {
            Point p = new Point(462, 95);
            String containerId = null;
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(0, 0, 459, 177)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
// > END MOVING
        contInterior.put("Form", new Rectangle(0, 0, 459, 177));
        compBounds.put("jTextField1", new Rectangle(0, 0, 459, 20));
        baselinePosition.put("jTextField1-459-20", new Integer(14));
        compBounds.put("jCheckBox1", new Rectangle(10, 22, 81, 23));
        baselinePosition.put("jCheckBox1-81-23", new Integer(15));
        compBounds.put("jCheckBox2", new Rectangle(10, 45, 81, 23));
        baselinePosition.put("jCheckBox2-81-23", new Integer(15));
        compBounds.put("jScrollPane1", new Rectangle(10, 80, 35, 86));
        baselinePosition.put("jScrollPane1-35-86", new Integer(0));
        compBounds.put("jToggleButton1", new Rectangle(78, 143, 330, 23));
        baselinePosition.put("jToggleButton1-330-23", new Integer(15));
        compBounds.put("jButton1", new Rectangle(78, 114, 330, 23));
        baselinePosition.put("jButton1-330-23", new Integer(15));
        compBounds.put("jTextField2", new Rectangle(78, 83, 330, 20));
        baselinePosition.put("jTextField2-330-20", new Integer(14));
        compBounds.put("jScrollPane2", new Rectangle(414, 116, 35, 50));
        baselinePosition.put("jScrollPane2-35-50", new Integer(0));
        ld.endMoving(true);
// < END MOVING
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 459, 177));
        contInterior.put("Form", new Rectangle(0, 0, 459, 177));
        compBounds.put("jTextField1", new Rectangle(0, 0, 459, 20));
        baselinePosition.put("jTextField1-459-20", new Integer(14));
        compBounds.put("jCheckBox1", new Rectangle(10, 22, 81, 23));
        baselinePosition.put("jCheckBox1-81-23", new Integer(15));
        compBounds.put("jCheckBox2", new Rectangle(10, 45, 81, 23));
        baselinePosition.put("jCheckBox2-81-23", new Integer(15));
        compBounds.put("jScrollPane1", new Rectangle(10, 80, 35, 86));
        baselinePosition.put("jScrollPane1-35-86", new Integer(0));
        compBounds.put("jToggleButton1", new Rectangle(78, 143, 330, 23));
        baselinePosition.put("jToggleButton1-330-23", new Integer(15));
        compBounds.put("jButton1", new Rectangle(78, 114, 330, 23));
        baselinePosition.put("jButton1-330-23", new Integer(15));
        compBounds.put("jTextField2", new Rectangle(78, 83, 330, 20));
        baselinePosition.put("jTextField2-330-20", new Integer(14));
        compBounds.put("jScrollPane2", new Rectangle(414, 116, 35, 50));
        baselinePosition.put("jScrollPane2-35-50", new Integer(0));
        compMinSize.put("Form", new Dimension(234, 177));
        compBounds.put("Form", new Rectangle(0, 0, 459, 177));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compPrefSize.put("jToggleButton1", new Dimension(105, 23));
        compPrefSize.put("jButton1", new Dimension(73, 23));
        compPrefSize.put("jTextField2", new Dimension(59, 20));
        prefPaddingInParent.put("Form-jScrollPane2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollPane1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jToggleButton1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        compBounds.put("Form", new Rectangle(0, 0, 459, 177));
        contInterior.put("Form", new Rectangle(0, 0, 459, 177));
        compBounds.put("jTextField1", new Rectangle(0, 0, 459, 20));
        baselinePosition.put("jTextField1-459-20", new Integer(14));
        compBounds.put("jCheckBox1", new Rectangle(10, 22, 81, 23));
        baselinePosition.put("jCheckBox1-81-23", new Integer(15));
        compBounds.put("jCheckBox2", new Rectangle(10, 45, 81, 23));
        baselinePosition.put("jCheckBox2-81-23", new Integer(15));
        compBounds.put("jScrollPane1", new Rectangle(10, 80, 35, 86));
        baselinePosition.put("jScrollPane1-35-86", new Integer(0));
        compBounds.put("jToggleButton1", new Rectangle(78, 143, 330, 23));
        baselinePosition.put("jToggleButton1-330-23", new Integer(15));
        compBounds.put("jButton1", new Rectangle(78, 114, 330, 23));
        baselinePosition.put("jButton1-330-23", new Integer(15));
        compBounds.put("jTextField2", new Rectangle(78, 83, 330, 20));
        baselinePosition.put("jTextField2-330-20", new Integer(14));
        compBounds.put("jScrollPane2", new Rectangle(414, 116, 35, 50));
        baselinePosition.put("jScrollPane2-35-50", new Integer(0));
        compMinSize.put("Form", new Dimension(234, 177));
        compBounds.put("Form", new Rectangle(0, 0, 459, 177));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compPrefSize.put("jToggleButton1", new Dimension(105, 23));
        compPrefSize.put("jButton1", new Dimension(73, 23));
        compPrefSize.put("jTextField2", new Dimension(59, 20));
        prefPaddingInParent.put("Form-jScrollPane2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollPane1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jToggleButton1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
    }

    /**
     * Set size of jtextField1 explicitly to 400 (i.e. smaller than current
     * size). The form should shrink, default size should be set to jTextField2
     * which defined the size before.
     */
    public void doChanges2() {
        LayoutInterval compInt = lm.getLayoutComponent("jTextField1").getLayoutInterval(0);
        lm.setUserIntervalSize(compInt, 0, 400);
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 400, 177));
        contInterior.put("Form", new Rectangle(0, 0, 400, 177));
        compBounds.put("jTextField1", new Rectangle(0, 0, 400, 20));
        baselinePosition.put("jTextField1-400-20", new Integer(14));
        compBounds.put("jCheckBox1", new Rectangle(10, 22, 81, 23));
        baselinePosition.put("jCheckBox1-81-23", new Integer(15));
        compBounds.put("jCheckBox2", new Rectangle(10, 45, 81, 23));
        baselinePosition.put("jCheckBox2-81-23", new Integer(15));
        compBounds.put("jScrollPane1", new Rectangle(10, 80, 35, 86));
        baselinePosition.put("jScrollPane1-35-86", new Integer(0));
        compBounds.put("jToggleButton1", new Rectangle(78, 143, 271, 23));
        baselinePosition.put("jToggleButton1-271-23", new Integer(15));
        compBounds.put("jButton1", new Rectangle(78, 114, 271, 23));
        baselinePosition.put("jButton1-271-23", new Integer(15));
        compBounds.put("jTextField2", new Rectangle(78, 83, 271, 20));
        baselinePosition.put("jTextField2-271-20", new Integer(14));
        compBounds.put("jScrollPane2", new Rectangle(355, 116, 35, 50));
        baselinePosition.put("jScrollPane2-35-50", new Integer(0));
        compMinSize.put("Form", new Dimension(142, 177));
        compBounds.put("Form", new Rectangle(0, 0, 400, 177));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compPrefSize.put("jToggleButton1", new Dimension(105, 23));
        compPrefSize.put("jButton1", new Dimension(73, 23));
        compPrefSize.put("jTextField2", new Dimension(59, 20));
        prefPaddingInParent.put("Form-jScrollPane2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollPane1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jToggleButton1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        compBounds.put("Form", new Rectangle(0, 0, 400, 177));
        contInterior.put("Form", new Rectangle(0, 0, 400, 177));
        compBounds.put("jTextField1", new Rectangle(0, 0, 400, 20));
        baselinePosition.put("jTextField1-400-20", new Integer(14));
        compBounds.put("jCheckBox1", new Rectangle(10, 22, 81, 23));
        baselinePosition.put("jCheckBox1-81-23", new Integer(15));
        compBounds.put("jCheckBox2", new Rectangle(10, 45, 81, 23));
        baselinePosition.put("jCheckBox2-81-23", new Integer(15));
        compBounds.put("jScrollPane1", new Rectangle(10, 80, 35, 86));
        baselinePosition.put("jScrollPane1-35-86", new Integer(0));
        compBounds.put("jToggleButton1", new Rectangle(78, 143, 271, 23));
        baselinePosition.put("jToggleButton1-271-23", new Integer(15));
        compBounds.put("jButton1", new Rectangle(78, 114, 271, 23));
        baselinePosition.put("jButton1-271-23", new Integer(15));
        compBounds.put("jTextField2", new Rectangle(78, 83, 271, 20));
        baselinePosition.put("jTextField2-271-20", new Integer(14));
        compBounds.put("jScrollPane2", new Rectangle(355, 116, 35, 50));
        baselinePosition.put("jScrollPane2-35-50", new Integer(0));
        compMinSize.put("Form", new Dimension(234, 177));
        compBounds.put("Form", new Rectangle(0, 0, 400, 177));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compPrefSize.put("jToggleButton1", new Dimension(105, 23));
        compPrefSize.put("jButton1", new Dimension(73, 23));
        compPrefSize.put("jTextField2", new Dimension(59, 20));
        prefPaddingInParent.put("Form-jScrollPane2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollPane1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jToggleButton1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
    }
}
