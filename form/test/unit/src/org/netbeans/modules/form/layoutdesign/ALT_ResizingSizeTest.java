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

public class ALT_ResizingSizeTest extends LayoutTestCase {

    public ALT_ResizingSizeTest(String name) {
        super(name);
        try {
	    className = this.getClass().getName();
	    className = className.substring(className.lastIndexOf('.') + 1, className.length());
            startingFormFile = FileUtil.toFileObject(new File(url.getFile() + goldenFilesPath + className + "-StartingForm.form").getCanonicalFile());
        } catch (IOException ioe) {
            fail(ioe.toString());
        }
    }

    // Resize whole designer to the right. Textfields should grow, not the gap.
    public void doChanges0() {
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 301, 143));
        compBounds.put("jTextField1", new Rectangle(10, 37, 266, 20));
        baselinePosition.put("jTextField1-266-20", new Integer(14));
        compBounds.put("jCheckBox1", new Rectangle(10, 7, 81, 23));
        baselinePosition.put("jCheckBox1-81-23", new Integer(15));
        compBounds.put("jCheckBox2", new Rectangle(93, 7, 81, 23));
        baselinePosition.put("jCheckBox2-81-23", new Integer(15));
        compBounds.put("jLabel1", new Rectangle(10, 89, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compBounds.put("jComboBox1", new Rectangle(54, 86, 56, 20));
        baselinePosition.put("jComboBox1-56-20", new Integer(14));
        compBounds.put("jLabel2", new Rectangle(10, 115, 34, 14));
        baselinePosition.put("jLabel2-34-14", new Integer(11));
        compBounds.put("jTextField2", new Rectangle(48, 112, 228, 20));
        baselinePosition.put("jTextField2-228-20", new Integer(14));
        prefPaddingInParent.put("Form-jLabel2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jTextField2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compPrefSize.put("jCheckBox1", new Dimension(81, 23));
        compPrefSize.put("jCheckBox2", new Dimension(81, 23));
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        compPrefSize.put("jComboBox1", new Dimension(56, 20));
        compPrefSize.put("jLabel2", new Dimension(34, 14));
        compPrefSize.put("jTextField2", new Dimension(59, 20));
        contInterior.put("Form", new Rectangle(0, 0, 301, 143));
        compBounds.put("jTextField1", new Rectangle(10, 37, 266, 20));
        baselinePosition.put("jTextField1-266-20", new Integer(14));
        compBounds.put("jCheckBox1", new Rectangle(10, 7, 81, 23));
        baselinePosition.put("jCheckBox1-81-23", new Integer(15));
        compBounds.put("jCheckBox2", new Rectangle(93, 7, 81, 23));
        baselinePosition.put("jCheckBox2-81-23", new Integer(15));
        compBounds.put("jLabel1", new Rectangle(10, 89, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compBounds.put("jComboBox1", new Rectangle(54, 86, 56, 20));
        baselinePosition.put("jComboBox1-56-20", new Integer(14));
        compBounds.put("jLabel2", new Rectangle(10, 115, 34, 14));
        baselinePosition.put("jLabel2-34-14", new Integer(11));
        compBounds.put("jTextField2", new Rectangle(48, 112, 228, 20));
        baselinePosition.put("jTextField2-228-20", new Integer(14));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
        // > START RESIZING
        baselinePosition.put("Form-301-143", new Integer(0));
        compMinSize.put("Form", new Dimension(199, 143));
        prefPaddingInParent.put("Form-jTextField1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jCheckBox2-0-1", new Integer(6)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jComboBox1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jTextField2-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        // parentId-compId-dimension-compAlignment
        // parentId-compId-dimension-compAlignment
        {
            String[] compIds = new String[]{"Form"};
            Rectangle[] bounds = new Rectangle[]{new Rectangle(0, 0, 301, 143)};
            Point hotspot = new Point(302, 86);
            int[] resizeEdges = new int[]{1, -1};
            boolean inLayout = false;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
        // < START RESIZING
        // > MOVE
        // < START RESIZING
        // > MOVE
        {
            Point p = new Point(350, 86);
            String containerId = null;
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(0, 0, 349, 143)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > MOVE
        // < MOVE
        // > MOVE
        {
            Point p = new Point(351, 86);
            String containerId = null;
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(0, 0, 350, 143)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        contInterior.put("Form", new Rectangle(0, 0, 350, 143));
        compBounds.put("jTextField1", new Rectangle(10, 37, 315, 20));
        baselinePosition.put("jTextField1-315-20", new Integer(14));
        compBounds.put("jCheckBox1", new Rectangle(10, 7, 81, 23));
        baselinePosition.put("jCheckBox1-81-23", new Integer(15));
        compBounds.put("jCheckBox2", new Rectangle(93, 7, 81, 23));
        baselinePosition.put("jCheckBox2-81-23", new Integer(15));
        compBounds.put("jLabel1", new Rectangle(10, 89, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compBounds.put("jComboBox1", new Rectangle(54, 86, 56, 20));
        baselinePosition.put("jComboBox1-56-20", new Integer(14));
        compBounds.put("jLabel2", new Rectangle(10, 115, 34, 14));
        baselinePosition.put("jLabel2-34-14", new Integer(11));
        compBounds.put("jTextField2", new Rectangle(48, 112, 277, 20));
        baselinePosition.put("jTextField2-277-20", new Integer(14));
        prefPaddingInParent.put("Form-jLabel2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jTextField2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compPrefSize.put("jCheckBox1", new Dimension(81, 23));
        compPrefSize.put("jCheckBox2", new Dimension(81, 23));
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        compPrefSize.put("jComboBox1", new Dimension(56, 20));
        compPrefSize.put("jLabel2", new Dimension(34, 14));
        compPrefSize.put("jTextField2", new Dimension(59, 20));
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 350, 143));
        compBounds.put("jTextField1", new Rectangle(10, 37, 315, 20));
        baselinePosition.put("jTextField1-315-20", new Integer(14));
        compBounds.put("jCheckBox1", new Rectangle(10, 7, 81, 23));
        baselinePosition.put("jCheckBox1-81-23", new Integer(15));
        compBounds.put("jCheckBox2", new Rectangle(93, 7, 81, 23));
        baselinePosition.put("jCheckBox2-81-23", new Integer(15));
        compBounds.put("jLabel1", new Rectangle(10, 89, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compBounds.put("jComboBox1", new Rectangle(54, 86, 56, 20));
        baselinePosition.put("jComboBox1-56-20", new Integer(14));
        compBounds.put("jLabel2", new Rectangle(10, 115, 34, 14));
        baselinePosition.put("jLabel2-34-14", new Integer(11));
        compBounds.put("jTextField2", new Rectangle(48, 112, 277, 20));
        baselinePosition.put("jTextField2-277-20", new Integer(14));
        prefPaddingInParent.put("Form-jLabel2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jTextField2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compPrefSize.put("jCheckBox1", new Dimension(81, 23));
        compPrefSize.put("jCheckBox2", new Dimension(81, 23));
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        compPrefSize.put("jComboBox1", new Dimension(56, 20));
        compPrefSize.put("jLabel2", new Dimension(34, 14));
        compPrefSize.put("jTextField2", new Dimension(59, 20));
        contInterior.put("Form", new Rectangle(0, 0, 350, 143));
        compBounds.put("jTextField1", new Rectangle(10, 37, 315, 20));
        baselinePosition.put("jTextField1-315-20", new Integer(14));
        compBounds.put("jCheckBox1", new Rectangle(10, 7, 81, 23));
        baselinePosition.put("jCheckBox1-81-23", new Integer(15));
        compBounds.put("jCheckBox2", new Rectangle(93, 7, 81, 23));
        baselinePosition.put("jCheckBox2-81-23", new Integer(15));
        compBounds.put("jLabel1", new Rectangle(10, 89, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compBounds.put("jComboBox1", new Rectangle(54, 86, 56, 20));
        baselinePosition.put("jComboBox1-56-20", new Integer(14));
        compBounds.put("jLabel2", new Rectangle(10, 115, 34, 14));
        baselinePosition.put("jLabel2-34-14", new Integer(11));
        compBounds.put("jTextField2", new Rectangle(48, 112, 277, 20));
        baselinePosition.put("jTextField2-277-20", new Integer(14));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }

    // Invoke "Set Default Size" on the container. Both resizing textfields
    // should end up with default pref. size set.
    public void doChanges1() {
        // > SET DEFAULT SIZE
        ld.setDefaultSize("Form");
        // < SET DEFAULT SIZE
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 199, 143));
        compBounds.put("jTextField1", new Rectangle(10, 37, 164, 20));
        baselinePosition.put("jTextField1-164-20", new Integer(14));
        compBounds.put("jCheckBox1", new Rectangle(10, 7, 81, 23));
        baselinePosition.put("jCheckBox1-81-23", new Integer(15));
        compBounds.put("jCheckBox2", new Rectangle(93, 7, 81, 23));
        baselinePosition.put("jCheckBox2-81-23", new Integer(15));
        compBounds.put("jLabel1", new Rectangle(10, 89, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compBounds.put("jComboBox1", new Rectangle(54, 86, 56, 20));
        baselinePosition.put("jComboBox1-56-20", new Integer(14));
        compBounds.put("jLabel2", new Rectangle(10, 115, 34, 14));
        baselinePosition.put("jLabel2-34-14", new Integer(11));
        compBounds.put("jTextField2", new Rectangle(48, 112, 126, 20));
        baselinePosition.put("jTextField2-126-20", new Integer(14));
        prefPaddingInParent.put("Form-jLabel2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jTextField2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compPrefSize.put("jCheckBox1", new Dimension(81, 23));
        compPrefSize.put("jCheckBox2", new Dimension(81, 23));
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        compPrefSize.put("jComboBox1", new Dimension(56, 20));
        compPrefSize.put("jLabel2", new Dimension(34, 14));
        compPrefSize.put("jTextField2", new Dimension(59, 20));
        contInterior.put("Form", new Rectangle(0, 0, 199, 143));
        compBounds.put("jTextField1", new Rectangle(10, 37, 164, 20));
        baselinePosition.put("jTextField1-164-20", new Integer(14));
        compBounds.put("jCheckBox1", new Rectangle(10, 7, 81, 23));
        baselinePosition.put("jCheckBox1-81-23", new Integer(15));
        compBounds.put("jCheckBox2", new Rectangle(93, 7, 81, 23));
        baselinePosition.put("jCheckBox2-81-23", new Integer(15));
        compBounds.put("jLabel1", new Rectangle(10, 89, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compBounds.put("jComboBox1", new Rectangle(54, 86, 56, 20));
        baselinePosition.put("jComboBox1-56-20", new Integer(14));
        compBounds.put("jLabel2", new Rectangle(10, 115, 34, 14));
        baselinePosition.put("jLabel2-34-14", new Integer(11));
        compBounds.put("jTextField2", new Rectangle(48, 112, 126, 20));
        baselinePosition.put("jTextField2-126-20", new Integer(14));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }

    // Add a new label between the checkboxes. Textfields will grow, but both
    // should keep default size set.
    public void doChanges2() {
        lc = new LayoutComponent("jLabel3", false);
        // > START ADDING
        baselinePosition.put("jLabel3-34-14", new Integer(11));
        {
            LayoutComponent[] comps = new LayoutComponent[]{lc};
            Rectangle[] bounds = new Rectangle[]{new Rectangle(0, 0, 34, 14)};
            String defaultContId = null;
            Point hotspot = new Point(13, 7);
            ld.startAdding(comps, bounds, hotspot, defaultContId);
        }
        // < START ADDING
        prefPaddingInParent.put("Form-jLabel3-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel3-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jCheckBox1-jLabel3-1-0-1", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox2-jLabel3-1-0-1", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jCheckBox1-1-0-0", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jCheckBox1-1-0-1", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jCheckBox1-1-0-2", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jCheckBox1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jCheckBox2-1-0-0", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jCheckBox2-1-0-1", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jCheckBox2-1-0-2", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jCheckBox2-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel3-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel3-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel3-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel3-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jTextField1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jLabel3-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jComboBox1-jLabel3-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jComboBox1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jComboBox1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jComboBox1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jComboBox1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel3-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel3-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel3-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel3-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jLabel3-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jLabel3-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jLabel3-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jLabel3-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel2-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jTextField2-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jLabel3-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel3-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jCheckBox1-jLabel3-0-0-1", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jCheckBox1-0-0-0", new Integer(0)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jCheckBox1-0-0-1", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jCheckBox1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jCheckBox1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox2-jLabel3-0-0-0", new Integer(0)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox2-jLabel3-0-0-1", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox2-jLabel3-0-0-2", new Integer(21)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox2-jLabel3-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jCheckBox2-0-0-1", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        // > MOVE
        // > MOVE
        {
            Point p = new Point(85, 23);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(72, 11, 34, 14)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPaddingInParent.put("Form-jLabel3-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel3-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jCheckBox1-jLabel3-1-0-1", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox2-jLabel3-1-0-1", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jCheckBox1-1-0-0", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jCheckBox1-1-0-1", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jCheckBox1-1-0-2", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jCheckBox1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jCheckBox2-1-0-0", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jCheckBox2-1-0-1", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jCheckBox2-1-0-2", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jCheckBox2-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel3-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel3-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel3-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel3-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jTextField1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jLabel3-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jComboBox1-jLabel3-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jComboBox1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jComboBox1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jComboBox1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jComboBox1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel3-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel3-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel3-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel3-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jLabel3-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jLabel3-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jLabel3-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField2-jLabel3-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel2-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jTextField2-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jLabel3-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel3-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jCheckBox1-jLabel3-0-0-1", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jCheckBox1-0-0-0", new Integer(0)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jCheckBox1-0-0-1", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jCheckBox1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jCheckBox1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox2-jLabel3-0-0-0", new Integer(0)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox2-jLabel3-0-0-1", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox2-jLabel3-0-0-2", new Integer(21)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox2-jLabel3-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jCheckBox2-0-0-1", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        // > MOVE
        // > MOVE
        {
            Point p = new Point(85, 22);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(72, 11, 34, 14)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        compPrefSize.put("jLabel3", new Dimension(34, 14));
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 231, 143));
        compBounds.put("jTextField1", new Rectangle(10, 37, 196, 20));
        baselinePosition.put("jTextField1-196-20", new Integer(14));
        compBounds.put("jCheckBox1", new Rectangle(10, 7, 81, 23));
        baselinePosition.put("jCheckBox1-81-23", new Integer(15));
        compBounds.put("jCheckBox2", new Rectangle(125, 7, 81, 23));
        baselinePosition.put("jCheckBox2-81-23", new Integer(15));
        compBounds.put("jLabel1", new Rectangle(10, 89, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compBounds.put("jComboBox1", new Rectangle(54, 86, 56, 20));
        baselinePosition.put("jComboBox1-56-20", new Integer(14));
        compBounds.put("jLabel2", new Rectangle(10, 115, 34, 14));
        baselinePosition.put("jLabel2-34-14", new Integer(11));
        compBounds.put("jTextField2", new Rectangle(48, 112, 158, 20));
        baselinePosition.put("jTextField2-158-20", new Integer(14));
        compBounds.put("jLabel3", new Rectangle(91, 11, 34, 14));
        baselinePosition.put("jLabel3-34-14", new Integer(11));
        prefPaddingInParent.put("Form-jLabel2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jTextField2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compPrefSize.put("jCheckBox1", new Dimension(81, 23));
        compPrefSize.put("jCheckBox2", new Dimension(81, 23));
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        compPrefSize.put("jComboBox1", new Dimension(56, 20));
        compPrefSize.put("jLabel2", new Dimension(34, 14));
        compPrefSize.put("jTextField2", new Dimension(59, 20));
        compPrefSize.put("jLabel3", new Dimension(34, 14));
        contInterior.put("Form", new Rectangle(0, 0, 231, 143));
        compBounds.put("jTextField1", new Rectangle(10, 37, 196, 20));
        baselinePosition.put("jTextField1-196-20", new Integer(14));
        compBounds.put("jCheckBox1", new Rectangle(10, 7, 81, 23));
        baselinePosition.put("jCheckBox1-81-23", new Integer(15));
        compBounds.put("jCheckBox2", new Rectangle(125, 7, 81, 23));
        baselinePosition.put("jCheckBox2-81-23", new Integer(15));
        compBounds.put("jLabel1", new Rectangle(10, 89, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compBounds.put("jComboBox1", new Rectangle(54, 86, 56, 20));
        baselinePosition.put("jComboBox1-56-20", new Integer(14));
        compBounds.put("jLabel2", new Rectangle(10, 115, 34, 14));
        baselinePosition.put("jLabel2-34-14", new Integer(11));
        compBounds.put("jTextField2", new Rectangle(48, 112, 158, 20));
        baselinePosition.put("jTextField2-158-20", new Integer(14));
        compBounds.put("jLabel3", new Rectangle(91, 11, 34, 14));
        baselinePosition.put("jLabel3-34-14", new Integer(11));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }

    // Resize whole designer to the right. The size of textfields must be set
    // explicitly as they are no longer supported by fixed components.
    public void doChanges3() {
        {
            String[] compIds = new String[]{"Form"};
            Rectangle[] bounds = new Rectangle[]{new Rectangle(0, 0, 301, 143)};
            Point hotspot = new Point(302, 86);
            int[] resizeEdges = new int[]{1, -1};
            boolean inLayout = false;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
        // < START RESIZING
        // > MOVE
        // < START RESIZING
        // > MOVE
        {
            Point p = new Point(350, 86);
            String containerId = null;
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(0, 0, 349, 143)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > MOVE
        // < MOVE
        // > MOVE
        {
            Point p = new Point(351, 86);
            String containerId = null;
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(0, 0, 350, 143)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        contInterior.put("Form", new Rectangle(0, 0, 350, 143));
        compBounds.put("jTextField1", new Rectangle(10, 37, 315, 20));
        baselinePosition.put("jTextField1-315-20", new Integer(14));
        compBounds.put("jTextField2", new Rectangle(48, 112, 277, 20));
        baselinePosition.put("jTextField2-277-20", new Integer(14));
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 350, 143));
        compBounds.put("jTextField1", new Rectangle(10, 37, 315, 20));
        baselinePosition.put("jTextField1-315-20", new Integer(14));
        compBounds.put("jTextField2", new Rectangle(48, 112, 277, 20));
        baselinePosition.put("jTextField2-277-20", new Integer(14));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE

    }
}
