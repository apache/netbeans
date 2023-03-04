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

public class ALT_Bug203112Test extends LayoutTestCase {

    public ALT_Bug203112Test(String name) {
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
     * Delete jSlider2.
     */
    public void doChanges0() {
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 706, 468));
        contInterior.put("Form", new Rectangle(0, 0, 706, 468));
        compBounds.put("jPanel1", new Rectangle(10, 11, 686, 446));
        baselinePosition.put("jPanel1-686-446", new Integer(0));
        contInterior.put("jPanel1", new Rectangle(10, 11, 686, 446));
        compBounds.put("jButton1", new Rectangle(218, 377, 73, 23));
        baselinePosition.put("jButton1-73-23", new Integer(15));
        compBounds.put("jTextField1", new Rectangle(309, 378, 59, 20));
        baselinePosition.put("jTextField1-59-20", new Integer(14));
        compBounds.put("jScrollPane2", new Rectangle(20, 22, 348, 306));
        baselinePosition.put("jScrollPane2-348-306", new Integer(0));
        compBounds.put("jCheckBox2", new Rectangle(386, 22, 81, 23));
        baselinePosition.put("jCheckBox2-81-23", new Integer(15));
        compBounds.put("jCheckBox3", new Rectangle(386, 63, 81, 23));
        baselinePosition.put("jCheckBox3-81-23", new Integer(15));
        compBounds.put("jComboBox1", new Rectangle(386, 104, 56, 20));
        baselinePosition.put("jComboBox1-56-20", new Integer(14));
        compBounds.put("jSlider2", new Rectangle(386, 273, 200, 23));
        baselinePosition.put("jSlider2-200-23", new Integer(0));
        compMinSize.put("jPanel1", new Dimension(686, 446));
        compBounds.put("jPanel1", new Rectangle(10, 11, 686, 446));
        compPrefSize.put("jPanel1", new Dimension(686, 446));
        prefPadding.put("jScrollPane2-jCheckBox2-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane2-jCheckBox3-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane2-jComboBox1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane2-jSlider2-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jCheckBox2-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jCheckBox3-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jComboBox1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jSlider2-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        prefPadding.put("jCheckBox2-jCheckBox3-1-0-0", new Integer(0)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox2-jCheckBox3-1-0-1", new Integer(3)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox2-jCheckBox3-1-0-2", new Integer(0)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox2-jCheckBox3-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        compMinSize.put("Form", new Dimension(706, 468));
        compBounds.put("Form", new Rectangle(0, 0, 706, 468));
        compPrefSize.put("jPanel1", new Dimension(686, 446));
        compPrefSize.put("jPanel1", new Dimension(686, 446));
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
        lm.removeComponent("jSlider2", true);
        prefPadding.put("jScrollPane2-jCheckBox2-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane2-jCheckBox3-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane2-jComboBox1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jCheckBox2-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jCheckBox3-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jComboBox1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox2-jCheckBox3-1-0-0", new Integer(0)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox2-jCheckBox3-1-0-1", new Integer(3)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox2-jCheckBox3-1-0-2", new Integer(0)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox2-jCheckBox3-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 706, 468));
        contInterior.put("Form", new Rectangle(0, 0, 706, 468));
        compBounds.put("jPanel1", new Rectangle(10, 11, 686, 446));
        baselinePosition.put("jPanel1-686-446", new Integer(0));
        contInterior.put("jPanel1", new Rectangle(10, 11, 686, 446));
        compBounds.put("jButton1", new Rectangle(218, 377, 73, 23));
        baselinePosition.put("jButton1-73-23", new Integer(15));
        compBounds.put("jTextField1", new Rectangle(309, 378, 59, 20));
        baselinePosition.put("jTextField1-59-20", new Integer(14));
        compBounds.put("jScrollPane2", new Rectangle(20, 22, 348, 306));
        baselinePosition.put("jScrollPane2-348-306", new Integer(0));
        compBounds.put("jCheckBox2", new Rectangle(386, 22, 81, 23));
        baselinePosition.put("jCheckBox2-81-23", new Integer(15));
        compBounds.put("jCheckBox3", new Rectangle(386, 63, 81, 23));
        baselinePosition.put("jCheckBox3-81-23", new Integer(15));
        compBounds.put("jComboBox1", new Rectangle(386, 104, 56, 20));
        baselinePosition.put("jComboBox1-56-20", new Integer(14));
        compMinSize.put("jPanel1", new Dimension(686, 446));
        compBounds.put("jPanel1", new Rectangle(10, 11, 686, 446));
        compPrefSize.put("jPanel1", new Dimension(686, 446));
        prefPadding.put("jScrollPane2-jCheckBox2-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane2-jCheckBox3-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane2-jComboBox1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jCheckBox2-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jCheckBox3-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jComboBox1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        prefPadding.put("jCheckBox2-jCheckBox3-1-0-0", new Integer(0)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox2-jCheckBox3-1-0-1", new Integer(3)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox2-jCheckBox3-1-0-2", new Integer(0)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox2-jCheckBox3-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        compMinSize.put("Form", new Dimension(706, 468));
        compBounds.put("Form", new Rectangle(0, 0, 706, 468));
        compPrefSize.put("jPanel1", new Dimension(686, 446));
        compPrefSize.put("jPanel1", new Dimension(686, 446));
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
    }
}
