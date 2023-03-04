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

public class ALT_SizeDefinition09Test extends LayoutTestCase {

    public ALT_SizeDefinition09Test(String name) {
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
     * Enter longer text into jLabel1. It should be less than the the textfield
     * next to it can accommodate (but close).
     * As a result, the size of the textfield should be unchanged. In reality it
     * would than adjust in the live layout with unchanged form size (and shrink
     * from 120 to 90 pixels that would be then applied to the definition), but
     * in the test we can't do real build of the layout, so checking the state
     * before it (i.e. unchanged).
     * (This step would fail in NetBeans 7.2, incorrectly calculating what change
     * can be absorbed without form size change.)
     */
    public void doChanges0() {
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 426, 300));
        contInterior.put("Form", new Rectangle(0, 0, 426, 300));
        compBounds.put("jLabel1", new Rectangle(66, 167, 136, 14));
        baselinePosition.put("jLabel1-136-14", new Integer(11));
        compBounds.put("jTextField1", new Rectangle(206, 164, 120, 20));
        baselinePosition.put("jTextField1-120-20", new Integer(14));
        compBounds.put("jSlider1", new Rectangle(77, 135, 170, 23));
        baselinePosition.put("jSlider1-170-23", new Integer(0));
        compBounds.put("jButton1", new Rectangle(253, 135, 73, 23));
        baselinePosition.put("jButton1-73-23", new Integer(15));
        compBounds.put("jToggleButton1", new Rectangle(147, 106, 179, 23));
        baselinePosition.put("jToggleButton1-179-23", new Integer(15));
        compMinSize.put("Form", new Dimension(415, 195));
        compBounds.put("Form", new Rectangle(0, 0, 426, 300));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        prefPaddingInParent.put("Form-jLabel1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jTextField1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
        prefPaddingInParent.put("Form-jLabel1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jTextField1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jTextField1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        compPrefSize.put("jLabel1", new Dimension(166, 14));
        ld.componentDefaultSizeChanged("jLabel1");
    }

    /**
     * Enter even longer text into jLabel1 so the textfield next to it can't
     * absorb the growth.
     * As a result the textfield size should be set to default. In reality it
     * would also rebuild the form with computing new form size - the form would
     * grow not to shrink textfield under its default size. We can't test the
     * real layout behavior in this test, so at least checking the reset of the
     * textfield to default size.
     * (This step would fail if nothing is done as reaction to component size
     * change as a result of a property change.)
     */
    public void doChanges1() {
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 426, 300));
        contInterior.put("Form", new Rectangle(0, 0, 426, 300));
        compBounds.put("jLabel1", new Rectangle(66, 167, 166, 14));
        baselinePosition.put("jLabel1-166-14", new Integer(11));
        compBounds.put("jTextField1", new Rectangle(236, 164, 90, 20));
        baselinePosition.put("jTextField1-90-20", new Integer(14));
        compBounds.put("jSlider1", new Rectangle(77, 135, 170, 23));
        baselinePosition.put("jSlider1-170-23", new Integer(0));
        compBounds.put("jButton1", new Rectangle(253, 135, 73, 23));
        baselinePosition.put("jButton1-73-23", new Integer(15));
        compBounds.put("jToggleButton1", new Rectangle(147, 106, 179, 23));
        baselinePosition.put("jToggleButton1-179-23", new Integer(15));
        compMinSize.put("Form", new Dimension(415, 195));
        compBounds.put("Form", new Rectangle(0, 0, 426, 300));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        prefPaddingInParent.put("Form-jLabel1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jTextField1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        compBounds.put("Form", new Rectangle(0, 0, 426, 300));
        contInterior.put("Form", new Rectangle(0, 0, 426, 300));
        compBounds.put("jLabel1", new Rectangle(66, 167, 166, 14));
        baselinePosition.put("jLabel1-166-14", new Integer(11));
        compBounds.put("jTextField1", new Rectangle(236, 164, 90, 20));
        baselinePosition.put("jTextField1-90-20", new Integer(14));
        compBounds.put("jSlider1", new Rectangle(77, 135, 170, 23));
        baselinePosition.put("jSlider1-170-23", new Integer(0));
        compBounds.put("jButton1", new Rectangle(253, 135, 73, 23));
        baselinePosition.put("jButton1-73-23", new Integer(15));
        compBounds.put("jToggleButton1", new Rectangle(147, 106, 179, 23));
        baselinePosition.put("jToggleButton1-179-23", new Integer(15));
        compMinSize.put("Form", new Dimension(415, 195));
        compBounds.put("Form", new Rectangle(0, 0, 426, 300));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        prefPaddingInParent.put("Form-jLabel1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jTextField1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
        compPrefSize.put("jLabel1", new Dimension(216, 14));
        ld.componentDefaultSizeChanged("jLabel1");
    }
}
