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

public class ALT_Positioning20Test extends LayoutTestCase {

    public ALT_Positioning20Test(String name) {
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
     * Add a label over jToggleButton2, left-aligned, on baseline. The label
     * should be added just before it.
     */
    public void doChanges0() {
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 370, 300));
        contInterior.put("Form", new Rectangle(0, 0, 370, 300));
        compBounds.put("jLabel1", new Rectangle(27, 30, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compBounds.put("jToggleButton2", new Rectangle(91, 58, 105, 23));
        baselinePosition.put("jToggleButton2-105-23", new Integer(15));
        compBounds.put("jComboBox1", new Rectangle(255, 27, 56, 20));
        baselinePosition.put("jComboBox1-56-20", new Integer(14));
        compBounds.put("jCheckBox1", new Rectangle(47, 99, 81, 23));
        baselinePosition.put("jCheckBox1-81-23", new Integer(15));
        compBounds.put("jCheckBox2", new Rectangle(184, 99, 81, 23));
        baselinePosition.put("jCheckBox2-81-23", new Integer(15));
        compMinSize.put("Form", new Dimension(321, 129));
        compBounds.put("Form", new Rectangle(0, 0, 370, 300));
        compBounds.put("Form", new Rectangle(0, 0, 370, 300));
        compBounds.put("Form", new Rectangle(0, 0, 370, 300));
        prefPaddingInParent.put("Form-jComboBox1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jCheckBox2-0-1", new Integer(6)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jCheckBox1-1-1", new Integer(7)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jCheckBox2-1-1", new Integer(7)); // parentId-compId-dimension-compAlignment
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
        prefPaddingInParent.put("Form-jCheckBox1-1-1", new Integer(7)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jCheckBox2-1-1", new Integer(7)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jComboBox1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jCheckBox2-0-1", new Integer(6)); // parentId-compId-dimension-compAlignment
        lc = new LayoutComponent("jLabel2", false);
// > START ADDING
        baselinePosition.put("jLabel2-34-14", new Integer(11));
        {
            LayoutComponent[] comps = new LayoutComponent[]{lc};
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(0, 0, 34, 14)
            };
            String defaultContId = null;
            Point hotspot = new Point(13, 7);
            ld.startAdding(comps, bounds, hotspot, defaultContId);
        }
// < START ADDING
        prefPaddingInParent.put("Form-jLabel2-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jLabel1-jLabel2-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jComboBox1-jLabel2-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jComboBox1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jComboBox1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jComboBox1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jComboBox1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton2-jLabel2-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jToggleButton2-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox1-jLabel2-1-0-0", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox1-jLabel2-1-0-1", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox1-jLabel2-1-0-2", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox1-jLabel2-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox2-jLabel2-1-0-0", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox2-jLabel2-1-0-1", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox2-jLabel2-1-0-2", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox2-jLabel2-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jCheckBox1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jCheckBox2-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jLabel2-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel2-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jToggleButton2-jLabel2-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton2-jLabel2-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton2-jLabel2-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton2-jLabel2-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jToggleButton2-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jToggleButton2-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jToggleButton2-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jToggleButton2-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jComboBox1-jLabel2-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
// > MOVE
        {
            Point p = new Point(110, 72);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(91, 62, 34, 14)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
        prefPaddingInParent.put("Form-jLabel2-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jLabel1-jLabel2-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jComboBox1-jLabel2-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jComboBox1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jComboBox1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jComboBox1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jComboBox1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton2-jLabel2-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jToggleButton2-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox1-jLabel2-1-0-0", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox1-jLabel2-1-0-1", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox1-jLabel2-1-0-2", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox1-jLabel2-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox2-jLabel2-1-0-0", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox2-jLabel2-1-0-1", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox2-jLabel2-1-0-2", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox2-jLabel2-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jCheckBox1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jCheckBox2-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jLabel2-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel2-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jToggleButton2-jLabel2-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton2-jLabel2-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton2-jLabel2-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton2-jLabel2-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jToggleButton2-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jToggleButton2-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jToggleButton2-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jToggleButton2-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jComboBox1-jLabel2-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
// > MOVE
        {
            Point p = new Point(109, 72);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(91, 62, 34, 14)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
// > END MOVING
        compPrefSize.put("jLabel2", new Dimension(34, 14));
        prefPadding.put("jLabel1-jLabel2-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        ld.endMoving(true);
// < END MOVING
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 370, 300));
        contInterior.put("Form", new Rectangle(0, 0, 370, 300));
        compBounds.put("jLabel1", new Rectangle(27, 30, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compBounds.put("jToggleButton2", new Rectangle(129, 58, 105, 23));
        baselinePosition.put("jToggleButton2-105-23", new Integer(15));
        compBounds.put("jComboBox1", new Rectangle(293, 27, 56, 20));
        baselinePosition.put("jComboBox1-56-20", new Integer(14));
        compBounds.put("jCheckBox1", new Rectangle(47, 99, 81, 23));
        baselinePosition.put("jCheckBox1-81-23", new Integer(15));
        compBounds.put("jCheckBox2", new Rectangle(184, 99, 81, 23));
        baselinePosition.put("jCheckBox2-81-23", new Integer(15));
        compBounds.put("jLabel2", new Rectangle(91, 62, 34, 14));
        baselinePosition.put("jLabel2-34-14", new Integer(11));
        compMinSize.put("Form", new Dimension(359, 129));
        compBounds.put("Form", new Rectangle(0, 0, 370, 300));
        prefPaddingInParent.put("Form-jComboBox1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jCheckBox2-0-1", new Integer(6)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jCheckBox1-1-1", new Integer(7)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jCheckBox2-1-1", new Integer(7)); // parentId-compId-dimension-compAlignment
        compBounds.put("Form", new Rectangle(0, 0, 370, 300));
        contInterior.put("Form", new Rectangle(0, 0, 370, 300));
        compBounds.put("jLabel1", new Rectangle(27, 30, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compBounds.put("jToggleButton2", new Rectangle(129, 58, 105, 23));
        baselinePosition.put("jToggleButton2-105-23", new Integer(15));
        compBounds.put("jComboBox1", new Rectangle(293, 27, 56, 20));
        baselinePosition.put("jComboBox1-56-20", new Integer(14));
        compBounds.put("jCheckBox1", new Rectangle(47, 99, 81, 23));
        baselinePosition.put("jCheckBox1-81-23", new Integer(15));
        compBounds.put("jCheckBox2", new Rectangle(184, 99, 81, 23));
        baselinePosition.put("jCheckBox2-81-23", new Integer(15));
        compBounds.put("jLabel2", new Rectangle(91, 62, 34, 14));
        baselinePosition.put("jLabel2-34-14", new Integer(11));
        compMinSize.put("Form", new Dimension(359, 129));
        compBounds.put("Form", new Rectangle(0, 0, 370, 300));
        prefPaddingInParent.put("Form-jComboBox1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jCheckBox2-0-1", new Integer(6)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jCheckBox1-1-1", new Integer(7)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jCheckBox2-1-1", new Integer(7)); // parentId-compId-dimension-compAlignment
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 370, 300));
    }

    /**
     * Add a label over jToggleButton2, right-aligned, on baseline. The label
     * should be added just after it.
     */
    public void doChanges1() {
        lc = new LayoutComponent("jLabel3", false);
// > START ADDING
        baselinePosition.put("jLabel3-34-14", new Integer(11));
        {
            LayoutComponent[] comps = new LayoutComponent[]{lc};
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(0, 0, 34, 14)
            };
            String defaultContId = null;
            Point hotspot = new Point(13, 7);
            ld.startAdding(comps, bounds, hotspot, defaultContId);
        }
// < START ADDING
        prefPaddingInParent.put("Form-jLabel3-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel3-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jLabel1-jLabel3-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jComboBox1-jLabel3-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jComboBox1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jComboBox1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jComboBox1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jComboBox1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton2-jLabel3-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel3-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jToggleButton2-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel2-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox1-jLabel3-1-0-0", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox1-jLabel3-1-0-1", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox1-jLabel3-1-0-2", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox1-jLabel3-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox2-jLabel3-1-0-0", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox2-jLabel3-1-0-1", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox2-jLabel3-1-0-2", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox2-jLabel3-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jCheckBox1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jCheckBox2-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jLabel3-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel3-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jLabel2-jLabel3-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel2-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel2-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel2-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel2-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton2-jLabel3-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton2-jLabel3-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton2-jLabel3-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton2-jLabel3-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jToggleButton2-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jComboBox1-jLabel3-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
// > MOVE
        {
            Point p = new Point(211, 73);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(200, 62, 34, 14)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
        prefPaddingInParent.put("Form-jLabel3-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel3-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jLabel1-jLabel3-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jComboBox1-jLabel3-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jComboBox1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jComboBox1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jComboBox1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jComboBox1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton2-jLabel3-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel3-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jToggleButton2-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel2-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox1-jLabel3-1-0-0", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox1-jLabel3-1-0-1", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox1-jLabel3-1-0-2", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox1-jLabel3-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox2-jLabel3-1-0-0", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox2-jLabel3-1-0-1", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox2-jLabel3-1-0-2", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox2-jLabel3-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jCheckBox1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jCheckBox2-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jLabel3-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel3-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jLabel2-jLabel3-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel2-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel2-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel2-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel2-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton2-jLabel3-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton2-jLabel3-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton2-jLabel3-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton2-jLabel3-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jToggleButton2-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jComboBox1-jLabel3-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
// > MOVE
        {
            Point p = new Point(212, 73);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(200, 62, 34, 14)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
// > END MOVING
        compPrefSize.put("jLabel3", new Dimension(34, 14));
        prefPadding.put("jLabel3-jComboBox1-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        ld.endMoving(true);
// < END MOVING
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 397, 300));
        contInterior.put("Form", new Rectangle(0, 0, 397, 300));
        compBounds.put("jLabel1", new Rectangle(27, 30, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compBounds.put("jToggleButton2", new Rectangle(129, 58, 105, 23));
        baselinePosition.put("jToggleButton2-105-23", new Integer(15));
        compBounds.put("jComboBox1", new Rectangle(331, 27, 56, 20));
        baselinePosition.put("jComboBox1-56-20", new Integer(14));
        compBounds.put("jCheckBox1", new Rectangle(47, 99, 81, 23));
        baselinePosition.put("jCheckBox1-81-23", new Integer(15));
        compBounds.put("jCheckBox2", new Rectangle(184, 99, 81, 23));
        baselinePosition.put("jCheckBox2-81-23", new Integer(15));
        compBounds.put("jLabel2", new Rectangle(91, 62, 34, 14));
        baselinePosition.put("jLabel2-34-14", new Integer(11));
        compBounds.put("jLabel3", new Rectangle(238, 62, 34, 14));
        baselinePosition.put("jLabel3-34-14", new Integer(11));
        compMinSize.put("Form", new Dimension(397, 129));
        compBounds.put("Form", new Rectangle(0, 0, 397, 300));
        prefPaddingInParent.put("Form-jComboBox1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jCheckBox2-0-1", new Integer(6)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jCheckBox1-1-1", new Integer(7)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jCheckBox2-1-1", new Integer(7)); // parentId-compId-dimension-compAlignment
        compBounds.put("Form", new Rectangle(0, 0, 397, 300));
        contInterior.put("Form", new Rectangle(0, 0, 397, 300));
        compBounds.put("jLabel1", new Rectangle(27, 30, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compBounds.put("jToggleButton2", new Rectangle(129, 58, 105, 23));
        baselinePosition.put("jToggleButton2-105-23", new Integer(15));
        compBounds.put("jComboBox1", new Rectangle(331, 27, 56, 20));
        baselinePosition.put("jComboBox1-56-20", new Integer(14));
        compBounds.put("jCheckBox1", new Rectangle(47, 99, 81, 23));
        baselinePosition.put("jCheckBox1-81-23", new Integer(15));
        compBounds.put("jCheckBox2", new Rectangle(184, 99, 81, 23));
        baselinePosition.put("jCheckBox2-81-23", new Integer(15));
        compBounds.put("jLabel2", new Rectangle(91, 62, 34, 14));
        baselinePosition.put("jLabel2-34-14", new Integer(11));
        compBounds.put("jLabel3", new Rectangle(238, 62, 34, 14));
        baselinePosition.put("jLabel3-34-14", new Integer(11));
        compMinSize.put("Form", new Dimension(397, 129));
        compBounds.put("Form", new Rectangle(0, 0, 397, 300));
        prefPaddingInParent.put("Form-jComboBox1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jCheckBox2-0-1", new Integer(6)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jCheckBox1-1-1", new Integer(7)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jCheckBox2-1-1", new Integer(7)); // parentId-compId-dimension-compAlignment
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 397, 300));
    }
}
