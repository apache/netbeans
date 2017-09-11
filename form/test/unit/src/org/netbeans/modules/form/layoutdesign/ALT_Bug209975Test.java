/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.form.layoutdesign;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.*;
import org.openide.filesystems.FileUtil;

public class ALT_Bug209975Test extends LayoutTestCase {

    public ALT_Bug209975Test(String name) {
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
     * Resize jEditorPane1 (in jScrollPane1) to the left to snap at default
     * small distance from jList1 (in jScrollPane4).
     */
    public void doChanges0() {
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 526, 238));
        contInterior.put("Form", new Rectangle(0, 0, 526, 238));
        compBounds.put("jScrollPane4", new Rectangle(10, 11, 35, 130));
        baselinePosition.put("jScrollPane4-35-130", new Integer(0));
        compBounds.put("jScrollPane1", new Rectangle(63, 11, 195, 130));
        baselinePosition.put("jScrollPane1-195-130", new Integer(0));
        compBounds.put("jToggleButton1", new Rectangle(276, 147, 105, 23));
        baselinePosition.put("jToggleButton1-105-23", new Integer(15));
        compBounds.put("jSpinner1", new Rectangle(399, 148, 54, 20));
        baselinePosition.put("jSpinner1-54-20", new Integer(14));
        compBounds.put("jSlider1", new Rectangle(276, 118, 200, 23));
        baselinePosition.put("jSlider1-200-23", new Integer(0));
        compBounds.put("jComboBox2", new Rectangle(27, 176, 56, 20));
        baselinePosition.put("jComboBox2-56-20", new Integer(14));
        compBounds.put("jButton4", new Rectangle(112, 176, 73, 23));
        baselinePosition.put("jButton4-73-23", new Integer(15));
        compBounds.put("jSeparator1", new Rectangle(10, 217, 506, 10));
        baselinePosition.put("jSeparator1-506-10", new Integer(0));
        compMinSize.put("Form", new Dimension(526, 238));
        compBounds.put("Form", new Rectangle(0, 0, 526, 238));
        compBounds.put("Form", new Rectangle(0, 0, 526, 238));
        compBounds.put("Form", new Rectangle(0, 0, 526, 238));
        prefPaddingInParent.put("Form-jButton4-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jSeparator1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jSlider1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jSpinner1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jSlider1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        compPrefSize.put("jScrollPane4", new Dimension(35, 130));
        compPrefSize.put("jScrollPane1", new Dimension(195, 22));
        prefPadding.put("jToggleButton1-jButton4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jComboBox2-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jSpinner1-jButton4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jSpinner1-jComboBox2-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
        prefPadding.put("jToggleButton1-jButton4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jComboBox2-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jSpinner1-jButton4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jSpinner1-jComboBox2-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jSeparator1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jSlider1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollPane4-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollPane1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton4-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jSeparator1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jSlider1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jSpinner1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollPane4-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jSeparator1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollPane4-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollPane1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
// > START RESIZING
        baselinePosition.put("jScrollPane1-195-130", new Integer(0));
        compPrefSize.put("jScrollPane1", new Dimension(195, 22));
        {
            String[] compIds = new String[]{
                "jScrollPane1"
            };
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(63, 11, 195, 130)
            };
            Point hotspot = new Point(60, 75);
            int[] resizeEdges = new int[]{
                0,
                -1
            };
            boolean inLayout = true;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
// < START RESIZING
        prefPadding.put("jScrollPane4-jScrollPane1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane4-jScrollPane1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane4-jScrollPane1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane4-jScrollPane1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
// > MOVE
        {
            Point p = new Point(48, 77);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(51, 11, 207, 130)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
        prefPadding.put("jScrollPane4-jScrollPane1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane4-jScrollPane1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane4-jScrollPane1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane4-jScrollPane1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
// > MOVE
        {
            Point p = new Point(47, 77);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(51, 11, 207, 130)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
// > END MOVING
        prefPaddingInParent.put("Form-jButton4-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jSeparator1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jSlider1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jSpinner1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jSlider1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jToggleButton1-jButton4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jComboBox2-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jSpinner1-jButton4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jSpinner1-jComboBox2-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        ld.endMoving(true);
// < END MOVING
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 526, 238));
        contInterior.put("Form", new Rectangle(0, 0, 526, 238));
        compBounds.put("jScrollPane4", new Rectangle(10, 11, 35, 130));
        baselinePosition.put("jScrollPane4-35-130", new Integer(0));
        compBounds.put("jScrollPane1", new Rectangle(51, 11, 207, 130));
        baselinePosition.put("jScrollPane1-207-130", new Integer(0));
        compBounds.put("jToggleButton1", new Rectangle(276, 147, 105, 23));
        baselinePosition.put("jToggleButton1-105-23", new Integer(15));
        compBounds.put("jSpinner1", new Rectangle(399, 148, 54, 20));
        baselinePosition.put("jSpinner1-54-20", new Integer(14));
        compBounds.put("jSlider1", new Rectangle(276, 118, 200, 23));
        baselinePosition.put("jSlider1-200-23", new Integer(0));
        compBounds.put("jComboBox2", new Rectangle(27, 176, 56, 20));
        baselinePosition.put("jComboBox2-56-20", new Integer(14));
        compBounds.put("jButton4", new Rectangle(112, 176, 73, 23));
        baselinePosition.put("jButton4-73-23", new Integer(15));
        compBounds.put("jSeparator1", new Rectangle(10, 217, 506, 10));
        baselinePosition.put("jSeparator1-506-10", new Integer(0));
        compMinSize.put("Form", new Dimension(526, 238));
        compBounds.put("Form", new Rectangle(0, 0, 526, 238));
        prefPaddingInParent.put("Form-jButton4-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jSeparator1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jSlider1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jSpinner1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jSlider1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        compPrefSize.put("jScrollPane4", new Dimension(35, 130));
        compPrefSize.put("jScrollPane1", new Dimension(207, 22));
        prefPadding.put("jToggleButton1-jButton4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jComboBox2-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jSpinner1-jButton4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jSpinner1-jComboBox2-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 526, 238));
        prefPaddingInParent.put("Form-jScrollPane4-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollPane1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
    }
}
