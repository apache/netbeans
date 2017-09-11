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

// Tests adding a component in parallel with existing group (column).
// After each test method, the added component is removed, and added again.
public class ALT_AddingToColumn2Test extends LayoutTestCase {

    public ALT_AddingToColumn2Test(String name) {
        super(name);
        try {
	    className = this.getClass().getName();
	    className = className.substring(className.lastIndexOf('.') + 1, className.length());
            startingFormFile = FileUtil.toFileObject(new File(url.getFile() + goldenFilesPath + className + "-StartingForm.form").getCanonicalFile());
        } catch (IOException ioe) {
            fail(ioe.toString());
        }
    }

    // Add a new label below the labels but above the list's bottom edge,
    // left-aligned with the longest label.
    public void doChanges0() {
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jScrollPane1", new Rectangle(28, 44, 35, 157));
        baselinePosition.put("jScrollPane1-35-157", new Integer(0));
        compPrefSize.put("jScrollPane1", new Dimension(35, 130));
        compBounds.put("jTextField1", new Rectangle(69, 44, 321, 20));
        baselinePosition.put("jTextField1-321-20", new Integer(14));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compBounds.put("jLabel3", new Rectangle(145, 132, 69, 14));
        baselinePosition.put("jLabel3-69-14", new Integer(11));
        compPrefSize.put("jLabel3", new Dimension(69, 14));
        compBounds.put("jLabel2", new Rectangle(133, 112, 81, 14));
        baselinePosition.put("jLabel2-81-14", new Integer(11));
        compPrefSize.put("jLabel2", new Dimension(81, 14));
        compBounds.put("jLabel1", new Rectangle(147, 92, 67, 14));
        baselinePosition.put("jLabel1-67-14", new Integer(11));
        compPrefSize.put("jLabel1", new Dimension(67, 14));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jScrollPane1", new Rectangle(28, 44, 35, 157));
        baselinePosition.put("jScrollPane1-35-157", new Integer(0));
        compBounds.put("jTextField1", new Rectangle(69, 44, 321, 20));
        baselinePosition.put("jTextField1-321-20", new Integer(14));
        compBounds.put("jLabel3", new Rectangle(145, 132, 69, 14));
        baselinePosition.put("jLabel3-69-14", new Integer(11));
        compBounds.put("jLabel2", new Rectangle(133, 112, 81, 14));
        baselinePosition.put("jLabel2-81-14", new Integer(11));
        compBounds.put("jLabel1", new Rectangle(147, 92, 67, 14));
        baselinePosition.put("jLabel1-67-14", new Integer(11));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
        lc = new LayoutComponent("jLabel4", false);
        // > START ADDING
        baselinePosition.put("jLabel4-34-14", new Integer(11));
        {
            LayoutComponent[] comps = new LayoutComponent[]{lc};
            Rectangle[] bounds = new Rectangle[]{new Rectangle(0, 0, 34, 14)};
            String defaultContId = null;
            Point hotspot = new Point(13, 7);
            ld.startAdding(comps, bounds, hotspot, defaultContId);
        }
        // < START ADDING
        prefPaddingInParent.put("Form-jLabel4-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel4-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jTextField1-jLabel4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel4-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel4-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel4-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jTextField1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jTextField1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jTextField1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jTextField1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jLabel4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel2-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel4-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel4-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel4-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel3-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jLabel4-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel4-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jScrollPane1-jLabel4-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jLabel4-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jLabel4-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jLabel4-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jScrollPane1-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jScrollPane1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jScrollPane1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jScrollPane1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel4-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        // > MOVE
        // > MOVE
        {
            Point p = new Point(141, 164);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(133, 157, 34, 14)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPaddingInParent.put("Form-jLabel4-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel4-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jTextField1-jLabel4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel4-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel4-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel4-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jTextField1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jTextField1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jTextField1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jTextField1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jLabel4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel2-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel4-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel4-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel4-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel3-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jLabel4-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel4-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jScrollPane1-jLabel4-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jLabel4-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jLabel4-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jLabel4-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jScrollPane1-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jScrollPane1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jScrollPane1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jScrollPane1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel4-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        // > MOVE
        // > MOVE
        {
            Point p = new Point(142, 164);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(133, 157, 34, 14)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        compPrefSize.put("jLabel4", new Dimension(34, 14));
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jScrollPane1", new Rectangle(28, 44, 35, 157));
        baselinePosition.put("jScrollPane1-35-157", new Integer(0));
        compPrefSize.put("jScrollPane1", new Dimension(35, 130));
        compBounds.put("jTextField1", new Rectangle(69, 44, 321, 20));
        baselinePosition.put("jTextField1-321-20", new Integer(14));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compBounds.put("jLabel3", new Rectangle(145, 132, 69, 14));
        baselinePosition.put("jLabel3-69-14", new Integer(11));
        compPrefSize.put("jLabel3", new Dimension(69, 14));
        compBounds.put("jLabel2", new Rectangle(133, 112, 81, 14));
        baselinePosition.put("jLabel2-81-14", new Integer(11));
        compPrefSize.put("jLabel2", new Dimension(81, 14));
        compBounds.put("jLabel1", new Rectangle(147, 92, 67, 14));
        baselinePosition.put("jLabel1-67-14", new Integer(11));
        compPrefSize.put("jLabel1", new Dimension(67, 14));
        compBounds.put("jLabel4", new Rectangle(133, 157, 34, 14));
        baselinePosition.put("jLabel4-34-14", new Integer(11));
        compPrefSize.put("jLabel4", new Dimension(34, 14));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jScrollPane1", new Rectangle(28, 44, 35, 157));
        baselinePosition.put("jScrollPane1-35-157", new Integer(0));
        compBounds.put("jTextField1", new Rectangle(69, 44, 321, 20));
        baselinePosition.put("jTextField1-321-20", new Integer(14));
        compBounds.put("jLabel3", new Rectangle(145, 132, 69, 14));
        baselinePosition.put("jLabel3-69-14", new Integer(11));
        compBounds.put("jLabel2", new Rectangle(133, 112, 81, 14));
        baselinePosition.put("jLabel2-81-14", new Integer(11));
        compBounds.put("jLabel1", new Rectangle(147, 92, 67, 14));
        baselinePosition.put("jLabel1-67-14", new Integer(11));
        compBounds.put("jLabel4", new Rectangle(133, 157, 34, 14));
        baselinePosition.put("jLabel4-34-14", new Integer(11));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }

    // Remove the previously added label. Add a new label below the labels but
    // above the list's bottom edge, right-aligned with the labels.
    public void doChanges1() {
        lm.removeComponent("jLabel4", true);
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jScrollPane1", new Rectangle(28, 44, 35, 157));
        baselinePosition.put("jScrollPane1-35-157", new Integer(0));
        compPrefSize.put("jScrollPane1", new Dimension(35, 130));
        compBounds.put("jTextField1", new Rectangle(69, 44, 321, 20));
        baselinePosition.put("jTextField1-321-20", new Integer(14));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compBounds.put("jLabel3", new Rectangle(145, 132, 69, 14));
        baselinePosition.put("jLabel3-69-14", new Integer(11));
        compPrefSize.put("jLabel3", new Dimension(69, 14));
        compBounds.put("jLabel2", new Rectangle(133, 112, 81, 14));
        baselinePosition.put("jLabel2-81-14", new Integer(11));
        compPrefSize.put("jLabel2", new Dimension(81, 14));
        compBounds.put("jLabel1", new Rectangle(147, 92, 67, 14));
        baselinePosition.put("jLabel1-67-14", new Integer(11));
        compPrefSize.put("jLabel1", new Dimension(67, 14));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jScrollPane1", new Rectangle(28, 44, 35, 157));
        baselinePosition.put("jScrollPane1-35-157", new Integer(0));
        compBounds.put("jTextField1", new Rectangle(69, 44, 321, 20));
        baselinePosition.put("jTextField1-321-20", new Integer(14));
        compBounds.put("jLabel3", new Rectangle(145, 132, 69, 14));
        baselinePosition.put("jLabel3-69-14", new Integer(11));
        compBounds.put("jLabel2", new Rectangle(133, 112, 81, 14));
        baselinePosition.put("jLabel2-81-14", new Integer(11));
        compBounds.put("jLabel1", new Rectangle(147, 92, 67, 14));
        baselinePosition.put("jLabel1-67-14", new Integer(11));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
        lc = new LayoutComponent("jLabel4", false);
        // > START ADDING
        baselinePosition.put("jLabel4-34-14", new Integer(11));
        {
            LayoutComponent[] comps = new LayoutComponent[]{lc};
            Rectangle[] bounds = new Rectangle[]{new Rectangle(0, 0, 34, 14)};
            String defaultContId = null;
            Point hotspot = new Point(13, 7);
            ld.startAdding(comps, bounds, hotspot, defaultContId);
        }
        // < START ADDING
        prefPaddingInParent.put("Form-jLabel4-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel4-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jTextField1-jLabel4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel4-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel4-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel4-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jTextField1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jTextField1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jTextField1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jTextField1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jLabel4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel2-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel4-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel4-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel4-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel3-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jLabel4-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel4-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jScrollPane1-jLabel4-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jLabel4-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jLabel4-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jLabel4-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jScrollPane1-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jScrollPane1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jScrollPane1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jScrollPane1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel4-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        // > MOVE
        // > MOVE
        {
            Point p = new Point(187, 164);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(180, 157, 34, 14)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPaddingInParent.put("Form-jLabel4-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel4-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jTextField1-jLabel4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel4-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel4-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel4-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jTextField1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jTextField1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jTextField1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jTextField1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jLabel4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel2-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel4-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel4-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel4-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel3-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jLabel4-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel4-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jScrollPane1-jLabel4-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jLabel4-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jLabel4-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jLabel4-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jScrollPane1-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jScrollPane1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jScrollPane1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jScrollPane1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel4-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        // > MOVE
        // > MOVE
        {
            Point p = new Point(188, 163);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(180, 157, 34, 14)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        compPrefSize.put("jLabel4", new Dimension(34, 14));
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jScrollPane1", new Rectangle(28, 44, 35, 157));
        baselinePosition.put("jScrollPane1-35-157", new Integer(0));
        compPrefSize.put("jScrollPane1", new Dimension(35, 130));
        compBounds.put("jTextField1", new Rectangle(69, 44, 321, 20));
        baselinePosition.put("jTextField1-321-20", new Integer(14));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compBounds.put("jLabel3", new Rectangle(145, 132, 69, 14));
        baselinePosition.put("jLabel3-69-14", new Integer(11));
        compPrefSize.put("jLabel3", new Dimension(69, 14));
        compBounds.put("jLabel2", new Rectangle(133, 112, 81, 14));
        baselinePosition.put("jLabel2-81-14", new Integer(11));
        compPrefSize.put("jLabel2", new Dimension(81, 14));
        compBounds.put("jLabel1", new Rectangle(147, 92, 67, 14));
        baselinePosition.put("jLabel1-67-14", new Integer(11));
        compPrefSize.put("jLabel1", new Dimension(67, 14));
        compBounds.put("jLabel4", new Rectangle(180, 157, 34, 14));
        baselinePosition.put("jLabel4-34-14", new Integer(11));
        compPrefSize.put("jLabel4", new Dimension(34, 14));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jScrollPane1", new Rectangle(28, 44, 35, 157));
        baselinePosition.put("jScrollPane1-35-157", new Integer(0));
        compBounds.put("jTextField1", new Rectangle(69, 44, 321, 20));
        baselinePosition.put("jTextField1-321-20", new Integer(14));
        compBounds.put("jLabel3", new Rectangle(145, 132, 69, 14));
        baselinePosition.put("jLabel3-69-14", new Integer(11));
        compBounds.put("jLabel2", new Rectangle(133, 112, 81, 14));
        baselinePosition.put("jLabel2-81-14", new Integer(11));
        compBounds.put("jLabel1", new Rectangle(147, 92, 67, 14));
        baselinePosition.put("jLabel1-67-14", new Integer(11));
        compBounds.put("jLabel4", new Rectangle(180, 157, 34, 14));
        baselinePosition.put("jLabel4-34-14", new Integer(11));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }

    // Remove the previously added label. Add a new label below the labels and
    // also below the list, left-aligned with the longest label.
    public void doChanges2() {
        lm.removeComponent("jLabel4", true);
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jScrollPane1", new Rectangle(28, 44, 35, 157));
        baselinePosition.put("jScrollPane1-35-157", new Integer(0));
        compPrefSize.put("jScrollPane1", new Dimension(35, 130));
        compBounds.put("jTextField1", new Rectangle(69, 44, 321, 20));
        baselinePosition.put("jTextField1-321-20", new Integer(14));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compBounds.put("jLabel3", new Rectangle(145, 132, 69, 14));
        baselinePosition.put("jLabel3-69-14", new Integer(11));
        compPrefSize.put("jLabel3", new Dimension(69, 14));
        compBounds.put("jLabel2", new Rectangle(133, 112, 81, 14));
        baselinePosition.put("jLabel2-81-14", new Integer(11));
        compPrefSize.put("jLabel2", new Dimension(81, 14));
        compBounds.put("jLabel1", new Rectangle(147, 92, 67, 14));
        baselinePosition.put("jLabel1-67-14", new Integer(11));
        compPrefSize.put("jLabel1", new Dimension(67, 14));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jScrollPane1", new Rectangle(28, 44, 35, 157));
        baselinePosition.put("jScrollPane1-35-157", new Integer(0));
        compBounds.put("jTextField1", new Rectangle(69, 44, 321, 20));
        baselinePosition.put("jTextField1-321-20", new Integer(14));
        compBounds.put("jLabel3", new Rectangle(145, 132, 69, 14));
        baselinePosition.put("jLabel3-69-14", new Integer(11));
        compBounds.put("jLabel2", new Rectangle(133, 112, 81, 14));
        baselinePosition.put("jLabel2-81-14", new Integer(11));
        compBounds.put("jLabel1", new Rectangle(147, 92, 67, 14));
        baselinePosition.put("jLabel1-67-14", new Integer(11));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
        lc = new LayoutComponent("jLabel4", false);
        // > START ADDING
        baselinePosition.put("jLabel4-34-14", new Integer(11));
        {
            LayoutComponent[] comps = new LayoutComponent[]{lc};
            Rectangle[] bounds = new Rectangle[]{new Rectangle(0, 0, 34, 14)};
            String defaultContId = null;
            Point hotspot = new Point(13, 7);
            ld.startAdding(comps, bounds, hotspot, defaultContId);
        }
        // < START ADDING
        prefPaddingInParent.put("Form-jLabel4-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel4-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jTextField1-jLabel4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel4-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel4-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel4-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jLabel4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel4-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel4-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel4-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jLabel4-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel4-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        // > MOVE
        // > MOVE
        // > MOVE
        {
            Point p = new Point(149, 229);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(133, 222, 34, 14)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPaddingInParent.put("Form-jLabel4-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel4-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jTextField1-jLabel4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel4-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel4-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel4-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jLabel4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel4-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel4-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel4-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jLabel4-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel4-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        // > MOVE
        // > MOVE
        // > MOVE
        {
            Point p = new Point(149, 228);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(133, 221, 34, 14)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        compPrefSize.put("jLabel4", new Dimension(34, 14));
        prefPadding.put("jScrollPane1-jLabel4-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel4-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jLabel4-1-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        // parentId-compId-dimension-compAlignment
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jScrollPane1", new Rectangle(28, 44, 35, 157));
        baselinePosition.put("jScrollPane1-35-157", new Integer(0));
        compPrefSize.put("jScrollPane1", new Dimension(35, 130));
        compBounds.put("jTextField1", new Rectangle(69, 44, 321, 20));
        baselinePosition.put("jTextField1-321-20", new Integer(14));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compBounds.put("jLabel3", new Rectangle(145, 132, 69, 14));
        baselinePosition.put("jLabel3-69-14", new Integer(11));
        compPrefSize.put("jLabel3", new Dimension(69, 14));
        compBounds.put("jLabel2", new Rectangle(133, 112, 81, 14));
        baselinePosition.put("jLabel2-81-14", new Integer(11));
        compPrefSize.put("jLabel2", new Dimension(81, 14));
        compBounds.put("jLabel1", new Rectangle(147, 92, 67, 14));
        baselinePosition.put("jLabel1-67-14", new Integer(11));
        compPrefSize.put("jLabel1", new Dimension(67, 14));
        compBounds.put("jLabel4", new Rectangle(133, 221, 34, 14));
        baselinePosition.put("jLabel4-34-14", new Integer(11));
        compPrefSize.put("jLabel4", new Dimension(34, 14));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jScrollPane1", new Rectangle(28, 44, 35, 157));
        baselinePosition.put("jScrollPane1-35-157", new Integer(0));
        compBounds.put("jTextField1", new Rectangle(69, 44, 321, 20));
        baselinePosition.put("jTextField1-321-20", new Integer(14));
        compBounds.put("jLabel3", new Rectangle(145, 132, 69, 14));
        baselinePosition.put("jLabel3-69-14", new Integer(11));
        compBounds.put("jLabel2", new Rectangle(133, 112, 81, 14));
        baselinePosition.put("jLabel2-81-14", new Integer(11));
        compBounds.put("jLabel1", new Rectangle(147, 92, 67, 14));
        baselinePosition.put("jLabel1-67-14", new Integer(11));
        compBounds.put("jLabel4", new Rectangle(133, 221, 34, 14));
        baselinePosition.put("jLabel4-34-14", new Integer(11));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }

    // Remove the previously added label. Add a new label below the labels and
    // also below the list, right-aligned with the labels.
    public void doChanges3() {
        lm.removeComponent("jLabel4", true);
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jScrollPane1", new Rectangle(28, 44, 35, 157));
        baselinePosition.put("jScrollPane1-35-157", new Integer(0));
        compPrefSize.put("jScrollPane1", new Dimension(35, 130));
        compBounds.put("jTextField1", new Rectangle(69, 44, 321, 20));
        baselinePosition.put("jTextField1-321-20", new Integer(14));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compBounds.put("jLabel3", new Rectangle(145, 132, 69, 14));
        baselinePosition.put("jLabel3-69-14", new Integer(11));
        compPrefSize.put("jLabel3", new Dimension(69, 14));
        compBounds.put("jLabel2", new Rectangle(133, 112, 81, 14));
        baselinePosition.put("jLabel2-81-14", new Integer(11));
        compPrefSize.put("jLabel2", new Dimension(81, 14));
        compBounds.put("jLabel1", new Rectangle(147, 92, 67, 14));
        baselinePosition.put("jLabel1-67-14", new Integer(11));
        compPrefSize.put("jLabel1", new Dimension(67, 14));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jScrollPane1", new Rectangle(28, 44, 35, 157));
        baselinePosition.put("jScrollPane1-35-157", new Integer(0));
        compBounds.put("jTextField1", new Rectangle(69, 44, 321, 20));
        baselinePosition.put("jTextField1-321-20", new Integer(14));
        compBounds.put("jLabel3", new Rectangle(145, 132, 69, 14));
        baselinePosition.put("jLabel3-69-14", new Integer(11));
        compBounds.put("jLabel2", new Rectangle(133, 112, 81, 14));
        baselinePosition.put("jLabel2-81-14", new Integer(11));
        compBounds.put("jLabel1", new Rectangle(147, 92, 67, 14));
        baselinePosition.put("jLabel1-67-14", new Integer(11));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
        lc = new LayoutComponent("jLabel4", false);
        // > START ADDING
        baselinePosition.put("jLabel4-34-14", new Integer(11));
        {
            LayoutComponent[] comps = new LayoutComponent[]{lc};
            Rectangle[] bounds = new Rectangle[]{new Rectangle(0, 0, 34, 14)};
            String defaultContId = null;
            Point hotspot = new Point(13, 7);
            ld.startAdding(comps, bounds, hotspot, defaultContId);
        }
        // < START ADDING
        prefPaddingInParent.put("Form-jLabel4-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel4-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jTextField1-jLabel4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel4-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel4-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel4-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jLabel4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel4-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel4-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel4-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jLabel4-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel4-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        // > MOVE
        // > MOVE
        // > MOVE
        {
            Point p = new Point(197, 233);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(180, 226, 34, 14)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPaddingInParent.put("Form-jLabel4-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel4-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jTextField1-jLabel4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel4-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel4-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel4-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jLabel4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel4-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel4-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel4-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jLabel4-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel4-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        // > MOVE
        // > MOVE
        // > MOVE
        {
            Point p = new Point(196, 233);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(180, 226, 34, 14)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        compPrefSize.put("jLabel4", new Dimension(34, 14));
        prefPadding.put("jScrollPane1-jLabel4-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel4-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jLabel4-1-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        // parentId-compId-dimension-compAlignment
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jScrollPane1", new Rectangle(28, 44, 35, 157));
        baselinePosition.put("jScrollPane1-35-157", new Integer(0));
        compPrefSize.put("jScrollPane1", new Dimension(35, 130));
        compBounds.put("jTextField1", new Rectangle(69, 44, 321, 20));
        baselinePosition.put("jTextField1-321-20", new Integer(14));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compBounds.put("jLabel3", new Rectangle(145, 132, 69, 14));
        baselinePosition.put("jLabel3-69-14", new Integer(11));
        compPrefSize.put("jLabel3", new Dimension(69, 14));
        compBounds.put("jLabel2", new Rectangle(133, 112, 81, 14));
        baselinePosition.put("jLabel2-81-14", new Integer(11));
        compPrefSize.put("jLabel2", new Dimension(81, 14));
        compBounds.put("jLabel1", new Rectangle(147, 92, 67, 14));
        baselinePosition.put("jLabel1-67-14", new Integer(11));
        compPrefSize.put("jLabel1", new Dimension(67, 14));
        compBounds.put("jLabel4", new Rectangle(180, 226, 34, 14));
        baselinePosition.put("jLabel4-34-14", new Integer(11));
        compPrefSize.put("jLabel4", new Dimension(34, 14));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jScrollPane1", new Rectangle(28, 44, 35, 157));
        baselinePosition.put("jScrollPane1-35-157", new Integer(0));
        compBounds.put("jTextField1", new Rectangle(69, 44, 321, 20));
        baselinePosition.put("jTextField1-321-20", new Integer(14));
        compBounds.put("jLabel3", new Rectangle(145, 132, 69, 14));
        baselinePosition.put("jLabel3-69-14", new Integer(11));
        compBounds.put("jLabel2", new Rectangle(133, 112, 81, 14));
        baselinePosition.put("jLabel2-81-14", new Integer(11));
        compBounds.put("jLabel1", new Rectangle(147, 92, 67, 14));
        baselinePosition.put("jLabel1-67-14", new Integer(11));
        compBounds.put("jLabel4", new Rectangle(180, 226, 34, 14));
        baselinePosition.put("jLabel4-34-14", new Integer(11));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }

    // Remove the previously added label. Make the longest label resizing.
    // Then add a new label below the labels but above the list's bottom edge,
    // left-aligned with the longest label.
    public void doChanges4() {
        lm.removeComponent("jLabel4", true);
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 400, 300));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jScrollPane1", new Rectangle(28, 44, 35, 157));
        baselinePosition.put("jScrollPane1-35-157", new Integer(0));
        compBounds.put("jTextField1", new Rectangle(69, 44, 321, 20));
        baselinePosition.put("jTextField1-321-20", new Integer(14));
        compBounds.put("jLabel3", new Rectangle(145, 132, 69, 14));
        baselinePosition.put("jLabel3-69-14", new Integer(11));
        compBounds.put("jLabel2", new Rectangle(133, 112, 81, 14));
        baselinePosition.put("jLabel2-81-14", new Integer(11));
        compBounds.put("jLabel1", new Rectangle(147, 92, 67, 14));
        baselinePosition.put("jLabel1-67-14", new Integer(11));
        compMinSize.put("Form", new Dimension(224, 212));
        compBounds.put("Form", new Rectangle(0, 0, 400, 300));
        prefPadding.put("jScrollPane1-jTextField1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jTextField1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jTextField1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jTextField1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        prefPaddingInParent.put("Form-jScrollPane1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel3-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        // parentId-compId-dimension-compAlignment
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
        // > SET COMPONENT RESIZING
        // < UPDATE CURRENT STATE
        // > SET COMPONENT RESIZING
        {
            LayoutComponent comp = lm.getLayoutComponent("jLabel2");
            int dimension = 0;
            boolean resizing = true;
            ld.setComponentResizing(comp, dimension, resizing);
        }
        prefPaddingInParent.put("Form-jTextField1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        // < SET COMPONENT RESIZING
        // < SET COMPONENT RESIZING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 400, 300));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jScrollPane1", new Rectangle(28, 44, 35, 157));
        baselinePosition.put("jScrollPane1-35-157", new Integer(0));
        compBounds.put("jTextField1", new Rectangle(69, 44, 321, 20));
        baselinePosition.put("jTextField1-321-20", new Integer(14));
        compBounds.put("jLabel3", new Rectangle(145, 132, 69, 14));
        baselinePosition.put("jLabel3-69-14", new Integer(11));
        compBounds.put("jLabel2", new Rectangle(133, 112, 81, 14));
        baselinePosition.put("jLabel2-81-14", new Integer(11));
        compBounds.put("jLabel1", new Rectangle(147, 92, 67, 14));
        baselinePosition.put("jLabel1-67-14", new Integer(11));
        compMinSize.put("Form", new Dimension(400, 212));
        compBounds.put("Form", new Rectangle(0, 0, 400, 300));
        prefPadding.put("jScrollPane1-jTextField1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jTextField1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jTextField1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jTextField1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compPrefSize.put("jLabel2", new Dimension(81, 14));
        prefPaddingInParent.put("Form-jScrollPane1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel3-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        compBounds.put("Form", new Rectangle(0, 0, 400, 300));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jScrollPane1", new Rectangle(28, 44, 35, 157));
        baselinePosition.put("jScrollPane1-35-157", new Integer(0));
        compBounds.put("jTextField1", new Rectangle(69, 44, 321, 20));
        baselinePosition.put("jTextField1-321-20", new Integer(14));
        compBounds.put("jLabel3", new Rectangle(145, 132, 69, 14));
        baselinePosition.put("jLabel3-69-14", new Integer(11));
        compBounds.put("jLabel2", new Rectangle(133, 112, 81, 14));
        baselinePosition.put("jLabel2-81-14", new Integer(11));
        compBounds.put("jLabel1", new Rectangle(147, 92, 67, 14));
        baselinePosition.put("jLabel1-67-14", new Integer(11));
        compMinSize.put("Form", new Dimension(400, 212));
        compBounds.put("Form", new Rectangle(0, 0, 400, 300));
        prefPadding.put("jScrollPane1-jTextField1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jTextField1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jTextField1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jTextField1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compPrefSize.put("jLabel2", new Dimension(81, 14));
        prefPaddingInParent.put("Form-jScrollPane1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel3-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        // parentId-compId-dimension-compAlignment
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
        lc = new LayoutComponent("jLabel4", false);
        // > START ADDING
        baselinePosition.put("jLabel4-34-14", new Integer(11));
        {
            LayoutComponent[] comps = new LayoutComponent[]{lc};
            Rectangle[] bounds = new Rectangle[]{new Rectangle(0, 0, 34, 14)};
            String defaultContId = null;
            Point hotspot = new Point(13, 7);
            ld.startAdding(comps, bounds, hotspot, defaultContId);
        }
        // < START ADDING
        prefPaddingInParent.put("Form-jLabel4-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel4-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jTextField1-jLabel4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel4-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel4-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel4-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jTextField1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jTextField1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jTextField1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jTextField1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jLabel4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel2-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel4-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel4-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel4-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel3-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jLabel4-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel4-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jScrollPane1-jLabel4-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jLabel4-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jLabel4-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jLabel4-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jScrollPane1-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jScrollPane1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jScrollPane1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jScrollPane1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel4-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        // > MOVE
        // > MOVE
        {
            Point p = new Point(141, 163);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(133, 155, 34, 14)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPaddingInParent.put("Form-jLabel4-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel4-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jTextField1-jLabel4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel4-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel4-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel4-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jTextField1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jTextField1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jTextField1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jTextField1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jLabel4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel2-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel4-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel4-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel4-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel3-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jLabel4-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel4-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jScrollPane1-jLabel4-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jLabel4-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jLabel4-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jLabel4-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jScrollPane1-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jScrollPane1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jScrollPane1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jScrollPane1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel4-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        // > MOVE
        // > MOVE
        {
            Point p = new Point(142, 163);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(133, 155, 34, 14)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        compPrefSize.put("jLabel4", new Dimension(34, 14));
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 400, 300));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jScrollPane1", new Rectangle(28, 44, 35, 157));
        baselinePosition.put("jScrollPane1-35-157", new Integer(0));
        compBounds.put("jTextField1", new Rectangle(69, 44, 321, 20));
        baselinePosition.put("jTextField1-321-20", new Integer(14));
        compBounds.put("jLabel3", new Rectangle(145, 132, 69, 14));
        baselinePosition.put("jLabel3-69-14", new Integer(11));
        compBounds.put("jLabel2", new Rectangle(133, 112, 81, 14));
        baselinePosition.put("jLabel2-81-14", new Integer(11));
        compBounds.put("jLabel1", new Rectangle(147, 92, 67, 14));
        baselinePosition.put("jLabel1-67-14", new Integer(11));
        compBounds.put("jLabel4", new Rectangle(133, 152, 34, 14));
        baselinePosition.put("jLabel4-34-14", new Integer(11));
        compMinSize.put("Form", new Dimension(400, 212));
        compBounds.put("Form", new Rectangle(0, 0, 400, 300));
        prefPadding.put("jScrollPane1-jTextField1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jTextField1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jTextField1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jTextField1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compPrefSize.put("jLabel2", new Dimension(81, 14));
        prefPaddingInParent.put("Form-jScrollPane1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel4-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        compBounds.put("Form", new Rectangle(0, 0, 400, 300));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jScrollPane1", new Rectangle(28, 44, 35, 157));
        baselinePosition.put("jScrollPane1-35-157", new Integer(0));
        compBounds.put("jTextField1", new Rectangle(69, 44, 321, 20));
        baselinePosition.put("jTextField1-321-20", new Integer(14));
        compBounds.put("jLabel3", new Rectangle(145, 132, 69, 14));
        baselinePosition.put("jLabel3-69-14", new Integer(11));
        compBounds.put("jLabel2", new Rectangle(133, 112, 81, 14));
        baselinePosition.put("jLabel2-81-14", new Integer(11));
        compBounds.put("jLabel1", new Rectangle(147, 92, 67, 14));
        baselinePosition.put("jLabel1-67-14", new Integer(11));
        compBounds.put("jLabel4", new Rectangle(133, 152, 34, 14));
        baselinePosition.put("jLabel4-34-14", new Integer(11));
        compMinSize.put("Form", new Dimension(400, 212));
        compBounds.put("Form", new Rectangle(0, 0, 400, 300));
        prefPadding.put("jScrollPane1-jTextField1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jTextField1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jTextField1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jTextField1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compPrefSize.put("jLabel2", new Dimension(81, 14));
        prefPaddingInParent.put("Form-jScrollPane1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel4-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        // parentId-compId-dimension-compAlignment
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
//        ld.externalSizeChangeHappened();
//        // > UPDATE CURRENT STATE
//        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
//        compBounds.put("jScrollPane1", new Rectangle(28, 44, 35, 157));
//        baselinePosition.put("jScrollPane1-35-157", new Integer(0));
//        compPrefSize.put("jScrollPane1", new Dimension(35, 130));
//        compBounds.put("jTextField1", new Rectangle(69, 44, 321, 20));
//        baselinePosition.put("jTextField1-321-20", new Integer(14));
//        compPrefSize.put("jTextField1", new Dimension(59, 20));
//        compBounds.put("jLabel3", new Rectangle(145, 132, 69, 14));
//        baselinePosition.put("jLabel3-69-14", new Integer(11));
//        compPrefSize.put("jLabel3", new Dimension(69, 14));
//        compBounds.put("jLabel2", new Rectangle(133, 112, 81, 14));
//        baselinePosition.put("jLabel2-81-14", new Integer(11));
//        compPrefSize.put("jLabel2", new Dimension(81, 14));
//        compBounds.put("jLabel1", new Rectangle(147, 92, 67, 14));
//        baselinePosition.put("jLabel1-67-14", new Integer(11));
//        compPrefSize.put("jLabel1", new Dimension(67, 14));
//        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
//        compBounds.put("jScrollPane1", new Rectangle(28, 44, 35, 157));
//        baselinePosition.put("jScrollPane1-35-157", new Integer(0));
//        compBounds.put("jTextField1", new Rectangle(69, 44, 321, 20));
//        baselinePosition.put("jTextField1-321-20", new Integer(14));
//        compBounds.put("jLabel3", new Rectangle(145, 132, 69, 14));
//        baselinePosition.put("jLabel3-69-14", new Integer(11));
//        compBounds.put("jLabel2", new Rectangle(133, 112, 81, 14));
//        baselinePosition.put("jLabel2-81-14", new Integer(11));
//        compBounds.put("jLabel1", new Rectangle(147, 92, 67, 14));
//        baselinePosition.put("jLabel1-67-14", new Integer(11));
//        ld.updateCurrentState();
//        // < UPDATE CURRENT STATE
//        // > SET COMPONENT RESIZING
//        // < UPDATE CURRENT STATE
//        // > SET COMPONENT RESIZING
//        {
//            LayoutComponent comp = lm.getLayoutComponent("jLabel2");
//            int dimension = 0;
//            boolean resizing = true;
//            ld.setComponentResizing(comp, dimension, resizing);
//        }
//        prefPaddingInParent.put("Form-jTextField1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
//        // < SET COMPONENT RESIZING
//        // < SET COMPONENT RESIZING
//        ld.externalSizeChangeHappened();
//        // > UPDATE CURRENT STATE
//        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
//        compBounds.put("jScrollPane1", new Rectangle(28, 44, 35, 157));
//        baselinePosition.put("jScrollPane1-35-157", new Integer(0));
//        compPrefSize.put("jScrollPane1", new Dimension(35, 130));
//        compBounds.put("jTextField1", new Rectangle(69, 44, 321, 20));
//        baselinePosition.put("jTextField1-321-20", new Integer(14));
//        compPrefSize.put("jTextField1", new Dimension(59, 20));
//        compBounds.put("jLabel3", new Rectangle(145, 132, 69, 14));
//        baselinePosition.put("jLabel3-69-14", new Integer(11));
//        compPrefSize.put("jLabel3", new Dimension(69, 14));
//        compBounds.put("jLabel2", new Rectangle(133, 112, 81, 14));
//        baselinePosition.put("jLabel2-81-14", new Integer(11));
//        compPrefSize.put("jLabel2", new Dimension(81, 14));
//        compBounds.put("jLabel1", new Rectangle(147, 92, 67, 14));
//        baselinePosition.put("jLabel1-67-14", new Integer(11));
//        compPrefSize.put("jLabel1", new Dimension(67, 14));
//        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
//        compBounds.put("jScrollPane1", new Rectangle(28, 44, 35, 157));
//        baselinePosition.put("jScrollPane1-35-157", new Integer(0));
//        compBounds.put("jTextField1", new Rectangle(69, 44, 321, 20));
//        baselinePosition.put("jTextField1-321-20", new Integer(14));
//        compBounds.put("jLabel3", new Rectangle(145, 132, 69, 14));
//        baselinePosition.put("jLabel3-69-14", new Integer(11));
//        compBounds.put("jLabel2", new Rectangle(133, 112, 81, 14));
//        baselinePosition.put("jLabel2-81-14", new Integer(11));
//        compBounds.put("jLabel1", new Rectangle(147, 92, 67, 14));
//        baselinePosition.put("jLabel1-67-14", new Integer(11));
//        ld.updateCurrentState();
//        // < UPDATE CURRENT STATE
//        lc = new LayoutComponent("jLabel4", false);
//        // > START ADDING
//        baselinePosition.put("jLabel4-34-14", new Integer(11));
//        {
//            LayoutComponent[] comps = new LayoutComponent[]{lc};
//            Rectangle[] bounds = new Rectangle[]{new Rectangle(0, 0, 34, 14)};
//            String defaultContId = null;
//            Point hotspot = new Point(13, 7);
//            ld.startAdding(comps, bounds, hotspot, defaultContId);
//        }
//        // < START ADDING
//        prefPaddingInParent.put("Form-jLabel4-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
//        prefPaddingInParent.put("Form-jLabel4-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
//        prefPadding.put("jTextField1-jLabel4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
//        prefPadding.put("jTextField1-jLabel4-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
//        prefPadding.put("jTextField1-jLabel4-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
//        prefPadding.put("jTextField1-jLabel4-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
//        prefPadding.put("jLabel4-jTextField1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
//        prefPadding.put("jLabel4-jTextField1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
//        prefPadding.put("jLabel4-jTextField1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
//        prefPadding.put("jLabel4-jTextField1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
//        prefPadding.put("jLabel1-jLabel4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
//        prefPadding.put("jLabel4-jLabel1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
//        prefPadding.put("jLabel4-jLabel1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
//        prefPadding.put("jLabel4-jLabel1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
//        prefPadding.put("jLabel4-jLabel1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
//        prefPadding.put("jLabel2-jLabel4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
//        prefPadding.put("jLabel4-jLabel2-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
//        prefPadding.put("jLabel3-jLabel4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
//        prefPadding.put("jLabel3-jLabel4-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
//        prefPadding.put("jLabel3-jLabel4-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
//        prefPadding.put("jLabel3-jLabel4-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
//        prefPadding.put("jLabel4-jLabel3-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
//        prefPaddingInParent.put("Form-jLabel4-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
//        prefPaddingInParent.put("Form-jLabel4-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
//        prefPadding.put("jScrollPane1-jLabel4-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
//        prefPadding.put("jScrollPane1-jLabel4-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
//        prefPadding.put("jScrollPane1-jLabel4-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
//        prefPadding.put("jScrollPane1-jLabel4-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
//        prefPadding.put("jLabel4-jScrollPane1-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
//        prefPadding.put("jLabel4-jScrollPane1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
//        prefPadding.put("jLabel4-jScrollPane1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
//        prefPadding.put("jLabel4-jScrollPane1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
//        prefPadding.put("jLabel3-jLabel4-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
//        // > MOVE
//        // > MOVE
//        // > MOVE
//        {
//            Point p = new Point(143, 164);
//            String containerId = "Form";
//            boolean autoPositioning = true;
//            boolean lockDimension = false;
//            Rectangle[] bounds = new Rectangle[]{new Rectangle(133, 157, 34, 14)};
//            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
//        }
//        // < MOVE
//        prefPaddingInParent.put("Form-jLabel4-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
//        prefPaddingInParent.put("Form-jLabel4-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
//        prefPadding.put("jTextField1-jLabel4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
//        prefPadding.put("jTextField1-jLabel4-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
//        prefPadding.put("jTextField1-jLabel4-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
//        prefPadding.put("jTextField1-jLabel4-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
//        prefPadding.put("jLabel4-jTextField1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
//        prefPadding.put("jLabel4-jTextField1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
//        prefPadding.put("jLabel4-jTextField1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
//        prefPadding.put("jLabel4-jTextField1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
//        prefPadding.put("jLabel1-jLabel4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
//        prefPadding.put("jLabel4-jLabel1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
//        prefPadding.put("jLabel4-jLabel1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
//        prefPadding.put("jLabel4-jLabel1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
//        prefPadding.put("jLabel4-jLabel1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
//        prefPadding.put("jLabel2-jLabel4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
//        prefPadding.put("jLabel4-jLabel2-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
//        prefPadding.put("jLabel3-jLabel4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
//        prefPadding.put("jLabel3-jLabel4-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
//        prefPadding.put("jLabel3-jLabel4-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
//        prefPadding.put("jLabel3-jLabel4-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
//        prefPadding.put("jLabel4-jLabel3-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
//        prefPaddingInParent.put("Form-jLabel4-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
//        prefPaddingInParent.put("Form-jLabel4-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
//        prefPadding.put("jScrollPane1-jLabel4-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
//        prefPadding.put("jScrollPane1-jLabel4-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
//        prefPadding.put("jScrollPane1-jLabel4-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
//        prefPadding.put("jScrollPane1-jLabel4-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
//        prefPadding.put("jLabel4-jScrollPane1-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
//        prefPadding.put("jLabel4-jScrollPane1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
//        prefPadding.put("jLabel4-jScrollPane1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
//        prefPadding.put("jLabel4-jScrollPane1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
//        prefPadding.put("jLabel3-jLabel4-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
//        // > MOVE
//        // > MOVE
//        // > MOVE
//        {
//            Point p = new Point(143, 163);
//            String containerId = "Form";
//            boolean autoPositioning = true;
//            boolean lockDimension = false;
//            Rectangle[] bounds = new Rectangle[]{new Rectangle(133, 157, 34, 14)};
//            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
//        }
//        // < MOVE
//        // > END MOVING
//        compPrefSize.put("jLabel4", new Dimension(34, 14));
//        ld.endMoving(true);
//        // < END MOVING
//        ld.externalSizeChangeHappened();
//        // > UPDATE CURRENT STATE
//        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
//        compBounds.put("jScrollPane1", new Rectangle(28, 44, 35, 157));
//        baselinePosition.put("jScrollPane1-35-157", new Integer(0));
//        compPrefSize.put("jScrollPane1", new Dimension(35, 130));
//        compBounds.put("jTextField1", new Rectangle(69, 44, 321, 20));
//        baselinePosition.put("jTextField1-321-20", new Integer(14));
//        compPrefSize.put("jTextField1", new Dimension(59, 20));
//        compBounds.put("jLabel3", new Rectangle(145, 132, 69, 14));
//        baselinePosition.put("jLabel3-69-14", new Integer(11));
//        compPrefSize.put("jLabel3", new Dimension(69, 14));
//        compBounds.put("jLabel2", new Rectangle(133, 112, 81, 14));
//        baselinePosition.put("jLabel2-81-14", new Integer(11));
//        compPrefSize.put("jLabel2", new Dimension(81, 14));
//        compBounds.put("jLabel1", new Rectangle(147, 92, 67, 14));
//        baselinePosition.put("jLabel1-67-14", new Integer(11));
//        compPrefSize.put("jLabel1", new Dimension(67, 14));
//        compBounds.put("jLabel4", new Rectangle(133, 157, 34, 14));
//        baselinePosition.put("jLabel4-34-14", new Integer(11));
//        compPrefSize.put("jLabel4", new Dimension(34, 14));
//        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
//        compBounds.put("jScrollPane1", new Rectangle(28, 44, 35, 157));
//        baselinePosition.put("jScrollPane1-35-157", new Integer(0));
//        compBounds.put("jTextField1", new Rectangle(69, 44, 321, 20));
//        baselinePosition.put("jTextField1-321-20", new Integer(14));
//        compBounds.put("jLabel3", new Rectangle(145, 132, 69, 14));
//        baselinePosition.put("jLabel3-69-14", new Integer(11));
//        compBounds.put("jLabel2", new Rectangle(133, 112, 81, 14));
//        baselinePosition.put("jLabel2-81-14", new Integer(11));
//        compBounds.put("jLabel1", new Rectangle(147, 92, 67, 14));
//        baselinePosition.put("jLabel1-67-14", new Integer(11));
//        compBounds.put("jLabel4", new Rectangle(133, 157, 34, 14));
//        baselinePosition.put("jLabel4-34-14", new Integer(11));
//        ld.updateCurrentState();
//        // < UPDATE CURRENT STATE
    }

    // Remove the previously added label. With the longest label still resizing,
    // add a new label below the labels but above the list's bottom edge,
    // right-aligned with the labels.
    public void doChanges5() {
        lm.removeComponent("jLabel4", true);
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jScrollPane1", new Rectangle(28, 44, 35, 157));
        baselinePosition.put("jScrollPane1-35-157", new Integer(0));
        compPrefSize.put("jScrollPane1", new Dimension(35, 130));
        compBounds.put("jTextField1", new Rectangle(69, 44, 321, 20));
        baselinePosition.put("jTextField1-321-20", new Integer(14));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compBounds.put("jLabel3", new Rectangle(145, 132, 69, 14));
        baselinePosition.put("jLabel3-69-14", new Integer(11));
        compPrefSize.put("jLabel3", new Dimension(69, 14));
        compBounds.put("jLabel2", new Rectangle(133, 112, 81, 14));
        baselinePosition.put("jLabel2-81-14", new Integer(11));
        compPrefSize.put("jLabel2", new Dimension(81, 14));
        compBounds.put("jLabel1", new Rectangle(147, 92, 67, 14));
        baselinePosition.put("jLabel1-67-14", new Integer(11));
        compPrefSize.put("jLabel1", new Dimension(67, 14));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jScrollPane1", new Rectangle(28, 44, 35, 157));
        baselinePosition.put("jScrollPane1-35-157", new Integer(0));
        compBounds.put("jTextField1", new Rectangle(69, 44, 321, 20));
        baselinePosition.put("jTextField1-321-20", new Integer(14));
        compBounds.put("jLabel3", new Rectangle(145, 132, 69, 14));
        baselinePosition.put("jLabel3-69-14", new Integer(11));
        compBounds.put("jLabel2", new Rectangle(133, 112, 81, 14));
        baselinePosition.put("jLabel2-81-14", new Integer(11));
        compBounds.put("jLabel1", new Rectangle(147, 92, 67, 14));
        baselinePosition.put("jLabel1-67-14", new Integer(11));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
        lc = new LayoutComponent("jLabel4", false);
        // > START ADDING
        baselinePosition.put("jLabel4-34-14", new Integer(11));
        {
            LayoutComponent[] comps = new LayoutComponent[]{lc};
            Rectangle[] bounds = new Rectangle[]{new Rectangle(0, 0, 34, 14)};
            String defaultContId = null;
            Point hotspot = new Point(13, 7);
            ld.startAdding(comps, bounds, hotspot, defaultContId);
        }
        // < START ADDING
        prefPaddingInParent.put("Form-jLabel4-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel4-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jTextField1-jLabel4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel4-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel4-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel4-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jTextField1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jTextField1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jTextField1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jTextField1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jLabel4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel2-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel4-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel4-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel4-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel3-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jLabel4-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel4-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jScrollPane1-jLabel4-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jLabel4-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jLabel4-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jLabel4-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jScrollPane1-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jScrollPane1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jScrollPane1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jScrollPane1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        // > MOVE
        // > MOVE
        {
            Point p = new Point(188, 168);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(180, 164, 34, 14)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPaddingInParent.put("Form-jLabel4-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel4-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jTextField1-jLabel4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel4-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel4-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel4-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jTextField1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jTextField1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jTextField1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jTextField1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jLabel4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel2-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel4-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel4-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel4-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel3-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jLabel4-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel4-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jScrollPane1-jLabel4-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jLabel4-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jLabel4-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jLabel4-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jScrollPane1-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jScrollPane1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jScrollPane1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jScrollPane1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel4-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        // > MOVE
        // > MOVE
        {
            Point p = new Point(188, 167);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(180, 157, 34, 14)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        compPrefSize.put("jLabel4", new Dimension(34, 14));
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jScrollPane1", new Rectangle(28, 44, 35, 157));
        baselinePosition.put("jScrollPane1-35-157", new Integer(0));
        compPrefSize.put("jScrollPane1", new Dimension(35, 130));
        compBounds.put("jTextField1", new Rectangle(69, 44, 321, 20));
        baselinePosition.put("jTextField1-321-20", new Integer(14));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compBounds.put("jLabel3", new Rectangle(145, 132, 69, 14));
        baselinePosition.put("jLabel3-69-14", new Integer(11));
        compPrefSize.put("jLabel3", new Dimension(69, 14));
        compBounds.put("jLabel2", new Rectangle(133, 112, 81, 14));
        baselinePosition.put("jLabel2-81-14", new Integer(11));
        compPrefSize.put("jLabel2", new Dimension(81, 14));
        compBounds.put("jLabel1", new Rectangle(147, 92, 67, 14));
        baselinePosition.put("jLabel1-67-14", new Integer(11));
        compPrefSize.put("jLabel1", new Dimension(67, 14));
        compBounds.put("jLabel4", new Rectangle(180, 157, 34, 14));
        baselinePosition.put("jLabel4-34-14", new Integer(11));
        compPrefSize.put("jLabel4", new Dimension(34, 14));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jScrollPane1", new Rectangle(28, 44, 35, 157));
        baselinePosition.put("jScrollPane1-35-157", new Integer(0));
        compBounds.put("jTextField1", new Rectangle(69, 44, 321, 20));
        baselinePosition.put("jTextField1-321-20", new Integer(14));
        compBounds.put("jLabel3", new Rectangle(145, 132, 69, 14));
        baselinePosition.put("jLabel3-69-14", new Integer(11));
        compBounds.put("jLabel2", new Rectangle(133, 112, 81, 14));
        baselinePosition.put("jLabel2-81-14", new Integer(11));
        compBounds.put("jLabel1", new Rectangle(147, 92, 67, 14));
        baselinePosition.put("jLabel1-67-14", new Integer(11));
        compBounds.put("jLabel4", new Rectangle(180, 157, 34, 14));
        baselinePosition.put("jLabel4-34-14", new Integer(11));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }

}
