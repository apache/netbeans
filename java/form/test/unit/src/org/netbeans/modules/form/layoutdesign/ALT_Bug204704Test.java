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

public class ALT_Bug204704Test extends LayoutTestCase {

    public ALT_Bug204704Test(String name) {
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
     * Delete jSpinner1.
     */
    public void doChanges0() {
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 500, 329));
        contInterior.put("Form", new Rectangle(0, 0, 500, 329));
        compBounds.put("jScrollPane1", new Rectangle(10, 11, 69, 89));
        baselinePosition.put("jScrollPane1-69-89", new Integer(0));
        compBounds.put("jRadioButton3", new Rectangle(397, 77, 93, 23));
        baselinePosition.put("jRadioButton3-93-23", new Integer(15));
        compBounds.put("jRadioButton1", new Rectangle(397, 28, 93, 23));
        baselinePosition.put("jRadioButton1-93-23", new Integer(15));
        compBounds.put("jRadioButton2", new Rectangle(397, 51, 93, 23));
        baselinePosition.put("jRadioButton2-93-23", new Integer(15));
        compBounds.put("jLabel1", new Rectangle(89, 11, 401, 14));
        baselinePosition.put("jLabel1-401-14", new Integer(11));
        compBounds.put("jSpinner1", new Rectangle(97, 298, 104, 20));
        baselinePosition.put("jSpinner1-104-20", new Integer(14));
        compBounds.put("jComboBox1", new Rectangle(386, 293, 104, 20));
        baselinePosition.put("jComboBox1-104-20", new Integer(14));
        compBounds.put("jButton2", new Rectangle(10, 295, 73, 23));
        baselinePosition.put("jButton2-73-23", new Integer(15));
        compBounds.put("jTextField2", new Rectangle(93, 296, 186, 20));
        baselinePosition.put("jTextField2-186-20", new Integer(14));
        compBounds.put("jButton1", new Rectangle(10, 254, 73, 23));
        baselinePosition.put("jButton1-73-23", new Integer(15));
        compBounds.put("jTextField1", new Rectangle(93, 255, 186, 20));
        baselinePosition.put("jTextField1-186-20", new Integer(14));
        compMinSize.put("Form", new Dimension(321, 312));
        compBounds.put("Form", new Rectangle(0, 0, 500, 329));
        prefPadding.put("jScrollPane1-jLabel1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        prefPadding.put("jSpinner1-jComboBox1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jSpinner1-jComboBox1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jSpinner1-jComboBox1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jSpinner1-jComboBox1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        compPrefSize.put("jScrollPane1", new Dimension(35, 130));
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
        lm.removeComponent("jSpinner1", true);
        prefPadding.put("jScrollPane1-jLabel1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        prefPaddingInParent.put("Form-jButton2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jTextField2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        compPrefSize.put("jScrollPane1", new Dimension(35, 130));
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 500, 329));
        contInterior.put("Form", new Rectangle(0, 0, 500, 329));
        compBounds.put("jScrollPane1", new Rectangle(10, 11, 69, 89));
        baselinePosition.put("jScrollPane1-69-89", new Integer(0));
        compBounds.put("jRadioButton3", new Rectangle(397, 77, 93, 23));
        baselinePosition.put("jRadioButton3-93-23", new Integer(15));
        compBounds.put("jRadioButton1", new Rectangle(397, 28, 93, 23));
        baselinePosition.put("jRadioButton1-93-23", new Integer(15));
        compBounds.put("jRadioButton2", new Rectangle(397, 51, 93, 23));
        baselinePosition.put("jRadioButton2-93-23", new Integer(15));
        compBounds.put("jLabel1", new Rectangle(89, 11, 401, 14));
        baselinePosition.put("jLabel1-401-14", new Integer(11));
        compBounds.put("jComboBox1", new Rectangle(386, 293, 104, 20));
        baselinePosition.put("jComboBox1-104-20", new Integer(14));
        compBounds.put("jButton2", new Rectangle(10, 295, 73, 23));
        baselinePosition.put("jButton2-73-23", new Integer(15));
        compBounds.put("jTextField2", new Rectangle(93, 296, 186, 20));
        baselinePosition.put("jTextField2-186-20", new Integer(14));
        compBounds.put("jButton1", new Rectangle(10, 254, 73, 23));
        baselinePosition.put("jButton1-73-23", new Integer(15));
        compBounds.put("jTextField1", new Rectangle(93, 255, 186, 20));
        baselinePosition.put("jTextField1-186-20", new Integer(14));
        compMinSize.put("Form", new Dimension(289, 312));
        compBounds.put("Form", new Rectangle(0, 0, 500, 329));
        prefPadding.put("jScrollPane1-jLabel1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        prefPaddingInParent.put("Form-jButton2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jTextField2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        compPrefSize.put("jScrollPane1", new Dimension(35, 130));
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
    }
}
