/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of the
 * License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include the
 * License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by Oracle
 * in the GPL Version 2 section of the License file that accompanied this code.
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL or only
 * the GPL Version 2, indicate your decision by adding "[Contributor] elects to
 * include this software in this distribution under the [CDDL or GPL Version 2]
 * license." If you do not indicate a single choice of license, a recipient has
 * the option to distribute your version of this file under either the CDDL, the
 * GPL Version 2 or to extend the choice of license to its licensees as provided
 * above. However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is made
 * subject to such option by the copyright holder.
 */
package org.netbeans.modules.form.layoutdesign;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.*;
import org.openide.filesystems.FileUtil;

public class ALT_Bug203129Test extends LayoutTestCase {

    public ALT_Bug203129Test(String name) {
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
     * Move jButton3 to the left to snap at small pref. distance from the
     * neighbor button.
     */
    public void doChanges0() {
        lm.setChangeRecording(true);
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 548, 565));
        contInterior.put("Form", new Rectangle(0, 0, 548, 565));
        compBounds.put("jPanel2", new Rectangle(10, 11, 528, 543));
        baselinePosition.put("jPanel2-528-543", new Integer(0));
        contInterior.put("jPanel2", new Rectangle(10, 11, 528, 543));
        compBounds.put("jScrollPane1", new Rectangle(20, 22, 166, 413));
        baselinePosition.put("jScrollPane1-166-413", new Integer(0));
        compBounds.put("jComboBox2", new Rectangle(204, 22, 56, 20));
        baselinePosition.put("jComboBox2-56-20", new Integer(14));
        compBounds.put("jButton4", new Rectangle(204, 48, 154, 387));
        baselinePosition.put("jButton4-154-387", new Integer(197));
        compBounds.put("jButton2", new Rectangle(364, 48, 73, 23));
        baselinePosition.put("jButton2-73-23", new Integer(15));
        compBounds.put("jButton3", new Rectangle(455, 48, 73, 23));
        baselinePosition.put("jButton3-73-23", new Integer(15));
        compBounds.put("jRadioButton3", new Rectangle(20, 524, 93, 23));
        baselinePosition.put("jRadioButton3-93-23", new Integer(15));
        compBounds.put("jRadioButton2", new Rectangle(20, 501, 93, 23));
        baselinePosition.put("jRadioButton2-93-23", new Integer(15));
        compBounds.put("jSlider1", new Rectangle(20, 453, 200, 23));
        baselinePosition.put("jSlider1-200-23", new Integer(0));
        compBounds.put("jRadioButton1", new Rectangle(20, 478, 93, 23));
        baselinePosition.put("jRadioButton1-93-23", new Integer(15));
        compMinSize.put("jPanel2", new Dimension(516, 543));
        compBounds.put("jPanel2", new Rectangle(10, 11, 528, 543));
        compPrefSize.put("jPanel2", new Dimension(528, 543));
        prefPadding.put("jButton2-jButton3-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jButton3-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jButton3-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jButton3-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        hasExplicitPrefSize.put("jPanel2", new Boolean(false));
        compPrefSize.put("jButton3", new Dimension(73, 23));
        compPrefSize.put("jButton2", new Dimension(73, 23));
        compPrefSize.put("jButton4", new Dimension(73, 23));
        hasExplicitPrefSize.put("jPanel2", new Boolean(false));
        compMinSize.put("Form", new Dimension(548, 565));
        compBounds.put("Form", new Rectangle(0, 0, 548, 565));
        prefPaddingInParent.put("Form-jPanel2-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jPanel2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
// > START MOVING
        baselinePosition.put("jButton3-73-23", new Integer(15));
        {
            String[] compIds = new String[]{
                "jButton3"
            };
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(455, 48, 73, 23)
            };
            Point hotspot = new Point(493, 59);
            ld.startMoving(compIds, bounds, hotspot);
        }
// < START MOVING
        prefPaddingInParent.put("jPanel2-jButton3-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel2-jButton3-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel2-jButton3-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel2-jButton3-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jScrollPane1-jButton3-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton3-jScrollPane1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton3-jScrollPane1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton3-jScrollPane1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton3-jScrollPane1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton4-jButton3-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton3-jButton4-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jButton3-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jButton3-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jButton3-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jButton3-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton3-jButton2-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jComboBox2-jButton3-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
// > MOVE
        {
            Point p = new Point(478, 59);
            String containerId = "jPanel2";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(443, 48, 73, 23)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
        prefPaddingInParent.put("jPanel2-jButton3-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel2-jButton3-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel2-jButton3-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel2-jButton3-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jScrollPane1-jButton3-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton3-jScrollPane1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton3-jScrollPane1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton3-jScrollPane1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton3-jScrollPane1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton4-jButton3-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton3-jButton4-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jButton3-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jButton3-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jButton3-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jButton3-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton3-jButton2-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jComboBox2-jButton3-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
// > MOVE
        {
            Point p = new Point(477, 59);
            String containerId = "jPanel2";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(443, 48, 73, 23)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
// > END MOVING
        prefPaddingInParent.put("jPanel2-jRadioButton3-0-1", new Integer(6)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel2-jRadioButton2-0-1", new Integer(6)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel2-jSlider1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel2-jRadioButton1-0-1", new Integer(6)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel2-jComboBox2-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel2-jButton2-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        compPrefSize.put("jButton4", new Dimension(73, 23));
        prefPaddingInParent.put("jPanel2-jRadioButton3-0-1", new Integer(6)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel2-jRadioButton2-0-1", new Integer(6)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel2-jSlider1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel2-jRadioButton1-0-1", new Integer(6)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel2-jComboBox2-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel2-jButton3-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        ld.endMoving(true);
// < END MOVING
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 548, 565));
        contInterior.put("Form", new Rectangle(0, 0, 548, 565));
        compBounds.put("jPanel2", new Rectangle(10, 11, 528, 543));
        baselinePosition.put("jPanel2-528-543", new Integer(0));
        contInterior.put("jPanel2", new Rectangle(10, 11, 528, 543));
        compBounds.put("jScrollPane1", new Rectangle(20, 22, 166, 413));
        baselinePosition.put("jScrollPane1-166-413", new Integer(0));
        compBounds.put("jComboBox2", new Rectangle(204, 22, 56, 20));
        baselinePosition.put("jComboBox2-56-20", new Integer(14));
        compBounds.put("jButton4", new Rectangle(204, 48, 154, 387));
        baselinePosition.put("jButton4-154-387", new Integer(197));
        compBounds.put("jButton2", new Rectangle(364, 48, 73, 23));
        baselinePosition.put("jButton2-73-23", new Integer(15));
        compBounds.put("jButton3", new Rectangle(443, 48, 73, 23));
        baselinePosition.put("jButton3-73-23", new Integer(15));
        compBounds.put("jRadioButton3", new Rectangle(20, 524, 93, 23));
        baselinePosition.put("jRadioButton3-93-23", new Integer(15));
        compBounds.put("jRadioButton2", new Rectangle(20, 501, 93, 23));
        baselinePosition.put("jRadioButton2-93-23", new Integer(15));
        compBounds.put("jSlider1", new Rectangle(20, 453, 200, 23));
        baselinePosition.put("jSlider1-200-23", new Integer(0));
        compBounds.put("jRadioButton1", new Rectangle(20, 478, 93, 23));
        baselinePosition.put("jRadioButton1-93-23", new Integer(15));
        compMinSize.put("jPanel2", new Dimension(516, 543));
        compBounds.put("jPanel2", new Rectangle(10, 11, 528, 543));
        compPrefSize.put("jPanel2", new Dimension(528, 543));
        prefPaddingInParent.put("jPanel2-jRadioButton3-0-1", new Integer(6)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel2-jRadioButton2-0-1", new Integer(6)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel2-jSlider1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel2-jRadioButton1-0-1", new Integer(6)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel2-jComboBox2-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel2-jButton3-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        hasExplicitPrefSize.put("jPanel2", new Boolean(false));
        compPrefSize.put("jButton3", new Dimension(73, 23));
        compPrefSize.put("jButton2", new Dimension(73, 23));
        compPrefSize.put("jButton4", new Dimension(73, 23));
        hasExplicitPrefSize.put("jPanel2", new Boolean(false));
        compMinSize.put("Form", new Dimension(548, 565));
        compBounds.put("Form", new Rectangle(0, 0, 548, 565));
        prefPaddingInParent.put("Form-jPanel2-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jPanel2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
    }

    /**
     * Delete jButton3.
     */
    public void doChanges1() {
        lm.removeComponent("jButton3", true);
        prefPaddingInParent.put("jPanel2-jRadioButton3-0-1", new Integer(6)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel2-jRadioButton2-0-1", new Integer(6)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel2-jSlider1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel2-jRadioButton1-0-1", new Integer(6)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel2-jComboBox2-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel2-jButton2-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        compPrefSize.put("jButton4", new Dimension(73, 23));
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 548, 565));
        contInterior.put("Form", new Rectangle(0, 0, 548, 565));
        compBounds.put("jPanel2", new Rectangle(10, 11, 528, 543));
        baselinePosition.put("jPanel2-528-543", new Integer(0));
        contInterior.put("jPanel2", new Rectangle(10, 11, 528, 543));
        compBounds.put("jScrollPane1", new Rectangle(20, 22, 166, 413));
        baselinePosition.put("jScrollPane1-166-413", new Integer(0));
        compBounds.put("jComboBox2", new Rectangle(204, 22, 56, 20));
        baselinePosition.put("jComboBox2-56-20", new Integer(14));
        compBounds.put("jButton4", new Rectangle(204, 48, 154, 387));
        baselinePosition.put("jButton4-154-387", new Integer(197));
        compBounds.put("jButton2", new Rectangle(364, 48, 73, 23));
        baselinePosition.put("jButton2-73-23", new Integer(15));
        compBounds.put("jRadioButton3", new Rectangle(20, 524, 93, 23));
        baselinePosition.put("jRadioButton3-93-23", new Integer(15));
        compBounds.put("jRadioButton2", new Rectangle(20, 501, 93, 23));
        baselinePosition.put("jRadioButton2-93-23", new Integer(15));
        compBounds.put("jSlider1", new Rectangle(20, 453, 200, 23));
        baselinePosition.put("jSlider1-200-23", new Integer(0));
        compBounds.put("jRadioButton1", new Rectangle(20, 478, 93, 23));
        baselinePosition.put("jRadioButton1-93-23", new Integer(15));
        compMinSize.put("jPanel2", new Dimension(437, 543));
        compBounds.put("jPanel2", new Rectangle(10, 11, 528, 543));
        compPrefSize.put("jPanel2", new Dimension(528, 543));
        prefPaddingInParent.put("jPanel2-jRadioButton3-0-1", new Integer(6)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel2-jRadioButton2-0-1", new Integer(6)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel2-jSlider1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel2-jRadioButton1-0-1", new Integer(6)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel2-jComboBox2-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel2-jButton2-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        hasExplicitPrefSize.put("jPanel2", new Boolean(false));
        compPrefSize.put("jButton4", new Dimension(73, 23));
        hasExplicitPrefSize.put("jPanel2", new Boolean(false));
        compMinSize.put("Form", new Dimension(548, 565));
        compBounds.put("Form", new Rectangle(0, 0, 548, 565));
        prefPaddingInParent.put("Form-jPanel2-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jPanel2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
    }
}
