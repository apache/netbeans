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

public class ALT_GapsOptimization7Test extends LayoutTestCase {

    public ALT_GapsOptimization7Test(String name) {
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
     * Delete jCheckBox1. According to bug 202707.
     */
    public void doChanges0() {
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 438, 263));
        contInterior.put("Form", new Rectangle(0, 0, 438, 263));
        compBounds.put("jPanel1", new Rectangle(10, 11, 418, 241));
        baselinePosition.put("jPanel1-418-241", new Integer(0));
        contInterior.put("jPanel1", new Rectangle(10, 11, 418, 241));
        compBounds.put("jScrollPane1", new Rectangle(20, 22, 175, 93));
        baselinePosition.put("jScrollPane1-175-93", new Integer(0));
        compBounds.put("jCheckBox1", new Rectangle(341, 22, 81, 23));
        baselinePosition.put("jCheckBox1-81-23", new Integer(15));
        compBounds.put("jRadioButton1", new Rectangle(16, 201, 93, 23));
        baselinePosition.put("jRadioButton1-93-23", new Integer(15));
        compMinSize.put("jPanel1", new Dimension(414, 157));
        compBounds.put("jPanel1", new Rectangle(10, 11, 418, 241));
        compPrefSize.put("jPanel1", new Dimension(418, 241));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        prefPadding.put("jCheckBox1-jRadioButton1-1-0-0", new Integer(0)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox1-jRadioButton1-1-0-1", new Integer(3)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox1-jRadioButton1-1-0-2", new Integer(0)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox1-jRadioButton1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jRadioButton1-1-0-0", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jRadioButton1-1-0-1", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jRadioButton1-1-0-2", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jRadioButton1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        compMinSize.put("Form", new Dimension(434, 179));
        compBounds.put("Form", new Rectangle(0, 0, 438, 263));
        compPrefSize.put("jPanel1", new Dimension(418, 241));
        compPrefSize.put("jPanel1", new Dimension(418, 241));
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
        lm.removeComponent("jCheckBox1", true);
        prefPaddingInParent.put("jPanel1-jRadioButton1-0-1", new Integer(6)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jScrollPane1-jRadioButton1-1-0-0", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jRadioButton1-1-0-1", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jRadioButton1-1-0-2", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jRadioButton1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 438, 263));
        contInterior.put("Form", new Rectangle(0, 0, 438, 263));
        compBounds.put("jPanel1", new Rectangle(10, 11, 418, 241));
        baselinePosition.put("jPanel1-418-241", new Integer(0));
        contInterior.put("jPanel1", new Rectangle(10, 11, 418, 241));
        compBounds.put("jScrollPane1", new Rectangle(20, 22, 175, 93));
        baselinePosition.put("jScrollPane1-175-93", new Integer(0));
        compBounds.put("jRadioButton1", new Rectangle(16, 201, 93, 23));
        baselinePosition.put("jRadioButton1-93-23", new Integer(15));
        compMinSize.put("jPanel1", new Dimension(414, 157));
        compBounds.put("jPanel1", new Rectangle(10, 11, 418, 241));
        compPrefSize.put("jPanel1", new Dimension(418, 241));
        prefPaddingInParent.put("jPanel1-jRadioButton1-0-1", new Integer(6)); // parentId-compId-dimension-compAlignment
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        prefPadding.put("jScrollPane1-jRadioButton1-1-0-0", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jRadioButton1-1-0-1", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jRadioButton1-1-0-2", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jRadioButton1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        compMinSize.put("Form", new Dimension(434, 179));
        compBounds.put("Form", new Rectangle(0, 0, 438, 263));
        compPrefSize.put("jPanel1", new Dimension(418, 241));
        compPrefSize.put("jPanel1", new Dimension(418, 241));
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
    }
}
