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
public class ALT_AddingToColumn1Test extends LayoutTestCase {

    public ALT_AddingToColumn1Test(String name) {
        super(name);
        try {
	    className = this.getClass().getName();
	    className = className.substring(className.lastIndexOf('.') + 1, className.length());
            startingFormFile = FileUtil.toFileObject(new File(url.getFile() + goldenFilesPath + className + "-StartingForm.form").getCanonicalFile());
        } catch (IOException ioe) {
            fail(ioe.toString());
        }
    }

    // Add new label below the labels, left-aligned with the longest label.
    public void doChanges0() {
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jLabel3", new Rectangle(106, 90, 69, 14));
        baselinePosition.put("jLabel3-69-14", new Integer(11));
        compPrefSize.put("jLabel3", new Dimension(69, 14));
        compBounds.put("jLabel2", new Rectangle(94, 70, 81, 14));
        baselinePosition.put("jLabel2-81-14", new Integer(11));
        compPrefSize.put("jLabel2", new Dimension(81, 14));
        compBounds.put("jLabel1", new Rectangle(108, 50, 67, 14));
        baselinePosition.put("jLabel1-67-14", new Integer(11));
        compPrefSize.put("jLabel1", new Dimension(67, 14));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jLabel3", new Rectangle(106, 90, 69, 14));
        baselinePosition.put("jLabel3-69-14", new Integer(11));
        compBounds.put("jLabel2", new Rectangle(94, 70, 81, 14));
        baselinePosition.put("jLabel2-81-14", new Integer(11));
        compBounds.put("jLabel1", new Rectangle(108, 50, 67, 14));
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
        prefPadding.put("jLabel3-jLabel4-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        // > MOVE
        // > MOVE
        {
            Point p = new Point(104, 123);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(94, 115, 34, 14)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPaddingInParent.put("Form-jLabel4-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel4-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
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
        prefPadding.put("jLabel3-jLabel4-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        // > MOVE
        // > MOVE
        {
            Point p = new Point(105, 123);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(94, 115, 34, 14)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        compPrefSize.put("jLabel4", new Dimension(34, 14));
        prefPaddingInParent.put("Form-jLabel4-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel4-1-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        // parentId-compId-dimension-compAlignment
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jLabel3", new Rectangle(106, 90, 69, 14));
        baselinePosition.put("jLabel3-69-14", new Integer(11));
        compPrefSize.put("jLabel3", new Dimension(69, 14));
        compBounds.put("jLabel2", new Rectangle(94, 70, 81, 14));
        baselinePosition.put("jLabel2-81-14", new Integer(11));
        compPrefSize.put("jLabel2", new Dimension(81, 14));
        compBounds.put("jLabel1", new Rectangle(108, 50, 67, 14));
        baselinePosition.put("jLabel1-67-14", new Integer(11));
        compPrefSize.put("jLabel1", new Dimension(67, 14));
        compBounds.put("jLabel4", new Rectangle(94, 115, 34, 14));
        baselinePosition.put("jLabel4-34-14", new Integer(11));
        compPrefSize.put("jLabel4", new Dimension(34, 14));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jLabel3", new Rectangle(106, 90, 69, 14));
        baselinePosition.put("jLabel3-69-14", new Integer(11));
        compBounds.put("jLabel2", new Rectangle(94, 70, 81, 14));
        baselinePosition.put("jLabel2-81-14", new Integer(11));
        compBounds.put("jLabel1", new Rectangle(108, 50, 67, 14));
        baselinePosition.put("jLabel1-67-14", new Integer(11));
        compBounds.put("jLabel4", new Rectangle(94, 115, 34, 14));
        baselinePosition.put("jLabel4-34-14", new Integer(11));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }

    /**
     * Remove the previously added label. Then add a new label below the labels,
     * now right-aligned with the labels.
     */
    public void doChanges1() {
        lm.removeComponent("jLabel4", true);
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jLabel3", new Rectangle(106, 90, 69, 14));
        baselinePosition.put("jLabel3-69-14", new Integer(11));
        compPrefSize.put("jLabel3", new Dimension(69, 14));
        compBounds.put("jLabel2", new Rectangle(94, 70, 81, 14));
        baselinePosition.put("jLabel2-81-14", new Integer(11));
        compPrefSize.put("jLabel2", new Dimension(81, 14));
        compBounds.put("jLabel1", new Rectangle(108, 50, 67, 14));
        baselinePosition.put("jLabel1-67-14", new Integer(11));
        compPrefSize.put("jLabel1", new Dimension(67, 14));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jLabel3", new Rectangle(106, 90, 69, 14));
        baselinePosition.put("jLabel3-69-14", new Integer(11));
        compBounds.put("jLabel2", new Rectangle(94, 70, 81, 14));
        baselinePosition.put("jLabel2-81-14", new Integer(11));
        compBounds.put("jLabel1", new Rectangle(108, 50, 67, 14));
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
        prefPadding.put("jLabel3-jLabel4-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        // > MOVE
        // > MOVE
        {
            Point p = new Point(156, 120);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(141, 115, 34, 14)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPaddingInParent.put("Form-jLabel4-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel4-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
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
        prefPadding.put("jLabel3-jLabel4-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        // > MOVE
        // > MOVE
        {
            Point p = new Point(156, 121);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(141, 115, 34, 14)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        compPrefSize.put("jLabel4", new Dimension(34, 14));
        prefPaddingInParent.put("Form-jLabel4-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel4-1-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        // parentId-compId-dimension-compAlignment
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jLabel3", new Rectangle(106, 90, 69, 14));
        baselinePosition.put("jLabel3-69-14", new Integer(11));
        compPrefSize.put("jLabel3", new Dimension(69, 14));
        compBounds.put("jLabel2", new Rectangle(94, 70, 81, 14));
        baselinePosition.put("jLabel2-81-14", new Integer(11));
        compPrefSize.put("jLabel2", new Dimension(81, 14));
        compBounds.put("jLabel1", new Rectangle(108, 50, 67, 14));
        baselinePosition.put("jLabel1-67-14", new Integer(11));
        compPrefSize.put("jLabel1", new Dimension(67, 14));
        compBounds.put("jLabel4", new Rectangle(141, 115, 34, 14));
        baselinePosition.put("jLabel4-34-14", new Integer(11));
        compPrefSize.put("jLabel4", new Dimension(34, 14));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jLabel3", new Rectangle(106, 90, 69, 14));
        baselinePosition.put("jLabel3-69-14", new Integer(11));
        compBounds.put("jLabel2", new Rectangle(94, 70, 81, 14));
        baselinePosition.put("jLabel2-81-14", new Integer(11));
        compBounds.put("jLabel1", new Rectangle(108, 50, 67, 14));
        baselinePosition.put("jLabel1-67-14", new Integer(11));
        compBounds.put("jLabel4", new Rectangle(141, 115, 34, 14));
        baselinePosition.put("jLabel4-34-14", new Integer(11));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }

    /**
     * Remove the previously added label. Add new label below the labels,
     * left-aligned with the lowest label.
     */
    public void doChanges2() {
        lm.removeComponent("jLabel4", true);
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jLabel3", new Rectangle(106, 90, 69, 14));
        baselinePosition.put("jLabel3-69-14", new Integer(11));
        compPrefSize.put("jLabel3", new Dimension(69, 14));
        compBounds.put("jLabel2", new Rectangle(94, 70, 81, 14));
        baselinePosition.put("jLabel2-81-14", new Integer(11));
        compPrefSize.put("jLabel2", new Dimension(81, 14));
        compBounds.put("jLabel1", new Rectangle(108, 50, 67, 14));
        baselinePosition.put("jLabel1-67-14", new Integer(11));
        compPrefSize.put("jLabel1", new Dimension(67, 14));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jLabel3", new Rectangle(106, 90, 69, 14));
        baselinePosition.put("jLabel3-69-14", new Integer(11));
        compBounds.put("jLabel2", new Rectangle(94, 70, 81, 14));
        baselinePosition.put("jLabel2-81-14", new Integer(11));
        compBounds.put("jLabel1", new Rectangle(108, 50, 67, 14));
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
        prefPadding.put("jLabel3-jLabel4-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        // > MOVE
        // > MOVE
        {
            Point p = new Point(113, 121);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(106, 115, 34, 14)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPaddingInParent.put("Form-jLabel4-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel4-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
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
        prefPadding.put("jLabel3-jLabel4-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        // > MOVE
        // > MOVE
        {
            Point p = new Point(113, 122);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(106, 115, 34, 14)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        compPrefSize.put("jLabel4", new Dimension(34, 14));
        prefPaddingInParent.put("Form-jLabel4-1-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        // parentId-compId-dimension-compAlignment
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jLabel3", new Rectangle(106, 90, 69, 14));
        baselinePosition.put("jLabel3-69-14", new Integer(11));
        compPrefSize.put("jLabel3", new Dimension(69, 14));
        compBounds.put("jLabel2", new Rectangle(94, 70, 81, 14));
        baselinePosition.put("jLabel2-81-14", new Integer(11));
        compPrefSize.put("jLabel2", new Dimension(81, 14));
        compBounds.put("jLabel1", new Rectangle(108, 50, 67, 14));
        baselinePosition.put("jLabel1-67-14", new Integer(11));
        compPrefSize.put("jLabel1", new Dimension(67, 14));
        compBounds.put("jLabel4", new Rectangle(106, 115, 34, 14));
        baselinePosition.put("jLabel4-34-14", new Integer(11));
        compPrefSize.put("jLabel4", new Dimension(34, 14));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jLabel3", new Rectangle(106, 90, 69, 14));
        baselinePosition.put("jLabel3-69-14", new Integer(11));
        compBounds.put("jLabel2", new Rectangle(94, 70, 81, 14));
        baselinePosition.put("jLabel2-81-14", new Integer(11));
        compBounds.put("jLabel1", new Rectangle(108, 50, 67, 14));
        baselinePosition.put("jLabel1-67-14", new Integer(11));
        compBounds.put("jLabel4", new Rectangle(106, 115, 34, 14));
        baselinePosition.put("jLabel4-34-14", new Integer(11));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }

    /**
     * Remove the previously added label. Make the longest label resizable.
     * Then add a new label below the labels, right-aligned with the labels.
     */
    public void doChanges3() {
        lm.removeComponent("jLabel4", true);
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jLabel3", new Rectangle(106, 90, 69, 14));
        baselinePosition.put("jLabel3-69-14", new Integer(11));
        compPrefSize.put("jLabel3", new Dimension(69, 14));
        compBounds.put("jLabel2", new Rectangle(94, 70, 81, 14));
        baselinePosition.put("jLabel2-81-14", new Integer(11));
        compPrefSize.put("jLabel2", new Dimension(81, 14));
        compBounds.put("jLabel1", new Rectangle(108, 50, 67, 14));
        baselinePosition.put("jLabel1-67-14", new Integer(11));
        compPrefSize.put("jLabel1", new Dimension(67, 14));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jLabel3", new Rectangle(106, 90, 69, 14));
        baselinePosition.put("jLabel3-69-14", new Integer(11));
        compBounds.put("jLabel2", new Rectangle(94, 70, 81, 14));
        baselinePosition.put("jLabel2-81-14", new Integer(11));
        compBounds.put("jLabel1", new Rectangle(108, 50, 67, 14));
        baselinePosition.put("jLabel1-67-14", new Integer(11));
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
        // < SET COMPONENT RESIZING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jLabel3", new Rectangle(106, 90, 69, 14));
        baselinePosition.put("jLabel3-69-14", new Integer(11));
        compPrefSize.put("jLabel3", new Dimension(69, 14));
        compBounds.put("jLabel2", new Rectangle(94, 70, 81, 14));
        baselinePosition.put("jLabel2-81-14", new Integer(11));
        compPrefSize.put("jLabel2", new Dimension(81, 14));
        compBounds.put("jLabel1", new Rectangle(108, 50, 67, 14));
        baselinePosition.put("jLabel1-67-14", new Integer(11));
        compPrefSize.put("jLabel1", new Dimension(67, 14));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jLabel3", new Rectangle(106, 90, 69, 14));
        baselinePosition.put("jLabel3-69-14", new Integer(11));
        compBounds.put("jLabel2", new Rectangle(94, 70, 81, 14));
        baselinePosition.put("jLabel2-81-14", new Integer(11));
        compBounds.put("jLabel1", new Rectangle(108, 50, 67, 14));
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
        prefPadding.put("jLabel3-jLabel4-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        // > MOVE
        // > MOVE
        {
            Point p = new Point(148, 122);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(141, 115, 34, 14)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPaddingInParent.put("Form-jLabel4-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel4-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
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
        prefPadding.put("jLabel3-jLabel4-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        // > MOVE
        // > MOVE
        {
            Point p = new Point(148, 121);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(141, 115, 34, 14)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        compPrefSize.put("jLabel4", new Dimension(34, 14));
        prefPaddingInParent.put("Form-jLabel4-1-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        // parentId-compId-dimension-compAlignment
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jLabel3", new Rectangle(106, 90, 69, 14));
        baselinePosition.put("jLabel3-69-14", new Integer(11));
        compPrefSize.put("jLabel3", new Dimension(69, 14));
        compBounds.put("jLabel2", new Rectangle(94, 70, 81, 14));
        baselinePosition.put("jLabel2-81-14", new Integer(11));
        compPrefSize.put("jLabel2", new Dimension(81, 14));
        compBounds.put("jLabel1", new Rectangle(108, 50, 67, 14));
        baselinePosition.put("jLabel1-67-14", new Integer(11));
        compPrefSize.put("jLabel1", new Dimension(67, 14));
        compBounds.put("jLabel4", new Rectangle(141, 115, 34, 14));
        baselinePosition.put("jLabel4-34-14", new Integer(11));
        compPrefSize.put("jLabel4", new Dimension(34, 14));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jLabel3", new Rectangle(106, 90, 69, 14));
        baselinePosition.put("jLabel3-69-14", new Integer(11));
        compBounds.put("jLabel2", new Rectangle(94, 70, 81, 14));
        baselinePosition.put("jLabel2-81-14", new Integer(11));
        compBounds.put("jLabel1", new Rectangle(108, 50, 67, 14));
        baselinePosition.put("jLabel1-67-14", new Integer(11));
        compBounds.put("jLabel4", new Rectangle(141, 115, 34, 14));
        baselinePosition.put("jLabel4-34-14", new Integer(11));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }

    // With the longest label still resizable, add a new label below the labels,
    // left-aligned with the longest label.
    public void doChanges4() {
        lm.removeComponent("jLabel4", true);
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jLabel3", new Rectangle(106, 90, 69, 14));
        baselinePosition.put("jLabel3-69-14", new Integer(11));
        compPrefSize.put("jLabel3", new Dimension(69, 14));
        compBounds.put("jLabel2", new Rectangle(94, 70, 81, 14));
        baselinePosition.put("jLabel2-81-14", new Integer(11));
        compPrefSize.put("jLabel2", new Dimension(81, 14));
        compBounds.put("jLabel1", new Rectangle(108, 50, 67, 14));
        baselinePosition.put("jLabel1-67-14", new Integer(11));
        compPrefSize.put("jLabel1", new Dimension(67, 14));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jLabel3", new Rectangle(106, 90, 69, 14));
        baselinePosition.put("jLabel3-69-14", new Integer(11));
        compBounds.put("jLabel2", new Rectangle(94, 70, 81, 14));
        baselinePosition.put("jLabel2-81-14", new Integer(11));
        compBounds.put("jLabel1", new Rectangle(108, 50, 67, 14));
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
        prefPadding.put("jLabel3-jLabel4-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        // > MOVE
        // > MOVE
        {
            Point p = new Point(105, 125);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(94, 115, 34, 14)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPaddingInParent.put("Form-jLabel4-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel4-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
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
        prefPadding.put("jLabel3-jLabel4-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        // > MOVE
        // > MOVE
        {
            Point p = new Point(105, 124);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(94, 115, 34, 14)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        compPrefSize.put("jLabel4", new Dimension(34, 14));
        prefPaddingInParent.put("Form-jLabel4-1-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        // parentId-compId-dimension-compAlignment
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jLabel3", new Rectangle(106, 90, 69, 14));
        baselinePosition.put("jLabel3-69-14", new Integer(11));
        compPrefSize.put("jLabel3", new Dimension(69, 14));
        compBounds.put("jLabel2", new Rectangle(94, 70, 81, 14));
        baselinePosition.put("jLabel2-81-14", new Integer(11));
        compPrefSize.put("jLabel2", new Dimension(81, 14));
        compBounds.put("jLabel1", new Rectangle(108, 50, 67, 14));
        baselinePosition.put("jLabel1-67-14", new Integer(11));
        compPrefSize.put("jLabel1", new Dimension(67, 14));
        compBounds.put("jLabel4", new Rectangle(94, 115, 34, 14));
        baselinePosition.put("jLabel4-34-14", new Integer(11));
        compPrefSize.put("jLabel4", new Dimension(34, 14));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jLabel3", new Rectangle(106, 90, 69, 14));
        baselinePosition.put("jLabel3-69-14", new Integer(11));
        compBounds.put("jLabel2", new Rectangle(94, 70, 81, 14));
        baselinePosition.put("jLabel2-81-14", new Integer(11));
        compBounds.put("jLabel1", new Rectangle(108, 50, 67, 14));
        baselinePosition.put("jLabel1-67-14", new Integer(11));
        compBounds.put("jLabel4", new Rectangle(94, 115, 34, 14));
        baselinePosition.put("jLabel4-34-14", new Integer(11));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }

}
