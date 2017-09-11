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

public class ALT_Bug253745Test extends LayoutTestCase {

    public ALT_Bug253745Test(String name) {
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
     * Move jLabel1 and jButton4 together out of jPanel1 - somewhere above it into the root form.
     */
    public void doChanges0() {
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 541, 409));
        contInterior.put("Form", new Rectangle(0, 0, 541, 409));
        compBounds.put("jPanel1", new Rectangle(77, 157, 315, 218));
        baselinePosition.put("jPanel1-315-218", new Integer(0));
        compMinSize.put("Form", new Dimension(402, 386));
        compBounds.put("Form", new Rectangle(0, 0, 541, 409));
        compBounds.put("jPanel1", new Rectangle(77, 157, 315, 218));
        compBounds.put("Form", new Rectangle(0, 0, 541, 409));
        contInterior.put("jPanel1", new Rectangle(77, 157, 315, 218));
        compBounds.put("jButton3", new Rectangle(77, 272, 73, 23));
        baselinePosition.put("jButton3-73-23", new Integer(15));
        compBounds.put("jButton4", new Rectangle(156, 292, 73, 23));
        baselinePosition.put("jButton4-73-23", new Integer(15));
        compBounds.put("jButton1", new Rectangle(87, 214, 73, 23));
        baselinePosition.put("jButton1-73-23", new Integer(15));
        compBounds.put("jLabel1", new Rectangle(178, 223, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compBounds.put("jButton2", new Rectangle(87, 243, 73, 23));
        baselinePosition.put("jButton2-73-23", new Integer(15));
        compBounds.put("jButton6", new Rectangle(166, 243, 73, 23));
        baselinePosition.put("jButton6-73-23", new Integer(15));
        compBounds.put("jScrollPane1", new Rectangle(284, 168, 35, 130));
        baselinePosition.put("jScrollPane1-35-130", new Integer(0));
        compMinSize.put("jPanel1", new Dimension(252, 169));
        compBounds.put("jPanel1", new Rectangle(77, 157, 315, 218));
        prefPaddingInParent.put("jPanel1-jScrollPane1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jButton2-jButton3-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton6-jButton3-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("jPanel1-jScrollPane1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel1-jButton4-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel1-jButton3-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        compPrefSize.put("jPanel1", new Dimension(315, 218));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        prefPaddingInParent.put("Form-jPanel1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jPanel1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
        prefPaddingInParent.put("Form-jPanel1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jPanel1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        compBounds.put("jPanel1", new Rectangle(77, 157, 315, 218));
        prefPaddingInParent.put("jPanel1-jScrollPane1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel1-jButton4-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel1-jButton3-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        compBounds.put("jScrollPane1", new Rectangle(284, 168, 35, 130));
        compBounds.put("jScrollPane1", new Rectangle(284, 168, 35, 130));
// > START MOVING
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        baselinePosition.put("jButton4-73-23", new Integer(15));
        {
            String[] compIds = new String[]{
                "jLabel1",
                "jButton4"
            };
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(178, 223, 34, 14),
                new Rectangle(156, 292, 73, 23)
            };
            Point hotspot = new Point(200, 299);
            ld.startMoving(compIds, bounds, hotspot);
        }
// < START MOVING
        prefPaddingInParent.put("Form-jLabel1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jPanel1-jLabel1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPanel1-jLabel1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPanel1-jLabel1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPanel1-jLabel1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jPanel1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jPanel1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jPanel1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jPanel1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jLabel1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
// > MOVE
        {
            Point p = new Point(204, 105);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(182, 29, 34, 14),
                new Rectangle(160, 98, 73, 23)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
        prefPaddingInParent.put("Form-jLabel1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jPanel1-jLabel1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPanel1-jLabel1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPanel1-jLabel1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPanel1-jLabel1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jPanel1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jPanel1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jPanel1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jPanel1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jLabel1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
// > MOVE
        {
            Point p = new Point(204, 104);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(182, 28, 34, 14),
                new Rectangle(160, 97, 73, 23)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
// > END MOVING
        prefPaddingInParent.put("jPanel1-jScrollPane1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel1-jButton3-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel1-jScrollPane1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton4-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton4-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jButton4-jPanel1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        ld.endMoving(true);
// < END MOVING
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 541, 409));
        contInterior.put("Form", new Rectangle(0, 0, 541, 409));
        compBounds.put("jPanel1", new Rectangle(77, 157, 315, 218));
        baselinePosition.put("jPanel1-315-218", new Integer(0));
        compBounds.put("jLabel1", new Rectangle(182, 28, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compBounds.put("jButton4", new Rectangle(160, 97, 73, 23));
        baselinePosition.put("jButton4-73-23", new Integer(15));
        compMinSize.put("Form", new Dimension(402, 386));
        compBounds.put("Form", new Rectangle(0, 0, 541, 409));
        contInterior.put("jPanel1", new Rectangle(77, 157, 315, 218));
        compBounds.put("jButton3", new Rectangle(77, 272, 73, 23));
        baselinePosition.put("jButton3-73-23", new Integer(15));
        compBounds.put("jButton1", new Rectangle(87, 214, 73, 23));
        baselinePosition.put("jButton1-73-23", new Integer(15));
        compBounds.put("jButton2", new Rectangle(87, 243, 73, 23));
        baselinePosition.put("jButton2-73-23", new Integer(15));
        compBounds.put("jButton6", new Rectangle(166, 243, 73, 23));
        baselinePosition.put("jButton6-73-23", new Integer(15));
        compBounds.put("jScrollPane1", new Rectangle(284, 168, 35, 130));
        baselinePosition.put("jScrollPane1-35-130", new Integer(0));
        compMinSize.put("jPanel1", new Dimension(252, 152));
        compBounds.put("jPanel1", new Rectangle(77, 157, 315, 218));
        prefPaddingInParent.put("jPanel1-jScrollPane1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel1-jButton3-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel1-jScrollPane1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        compPrefSize.put("jPanel1", new Dimension(315, 218));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        prefPaddingInParent.put("Form-jPanel1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton4-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jPanel1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 541, 409));
        prefPaddingInParent.put("Form-jPanel1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton4-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        compBounds.put("Form", new Rectangle(0, 0, 541, 409));
        prefPaddingInParent.put("Form-jPanel1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton4-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
    }

    /**
     * Move jLabel1 and jButton4 slightly to the right.
     * This used to cause AE due to 2 consecutive gaps left from step 0.
     */
    public void doChanges1() {
// > START MOVING
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        baselinePosition.put("jButton4-73-23", new Integer(15));
        {
            String[] compIds = new String[]{
                "jLabel1",
                "jButton4"
            };
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(182, 28, 34, 14),
                new Rectangle(160, 97, 73, 23)
            };
            Point hotspot = new Point(202, 111);
            ld.startMoving(compIds, bounds, hotspot);
        }
// < START MOVING
        prefPaddingInParent.put("Form-jLabel1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jPanel1-jLabel1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPanel1-jLabel1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPanel1-jLabel1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPanel1-jLabel1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jPanel1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jPanel1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jPanel1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jPanel1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jLabel1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
// > MOVE
        {
            Point p = new Point(332, 108);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(312, 25, 34, 14),
                new Rectangle(290, 94, 73, 23)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
        prefPaddingInParent.put("Form-jLabel1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jPanel1-jLabel1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPanel1-jLabel1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPanel1-jLabel1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPanel1-jLabel1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jPanel1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jPanel1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jPanel1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jPanel1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jLabel1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
// > MOVE
        {
            Point p = new Point(333, 108);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(313, 25, 34, 14),
                new Rectangle(291, 94, 73, 23)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
// > END MOVING
        prefPaddingInParent.put("Form-jPanel1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jPanel1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton4-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton4-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jButton4-jPanel1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        ld.endMoving(true);
// < END MOVING
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 541, 409));
        contInterior.put("Form", new Rectangle(0, 0, 541, 409));
        compBounds.put("jPanel1", new Rectangle(77, 157, 315, 218));
        baselinePosition.put("jPanel1-315-218", new Integer(0));
        compBounds.put("jLabel1", new Rectangle(313, 25, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compBounds.put("jButton4", new Rectangle(291, 94, 73, 23));
        baselinePosition.put("jButton4-73-23", new Integer(15));
        compMinSize.put("Form", new Dimension(402, 386));
        compBounds.put("Form", new Rectangle(0, 0, 541, 409));
        contInterior.put("jPanel1", new Rectangle(77, 157, 315, 218));
        compBounds.put("jButton3", new Rectangle(77, 272, 73, 23));
        baselinePosition.put("jButton3-73-23", new Integer(15));
        compBounds.put("jButton1", new Rectangle(87, 214, 73, 23));
        baselinePosition.put("jButton1-73-23", new Integer(15));
        compBounds.put("jButton2", new Rectangle(87, 243, 73, 23));
        baselinePosition.put("jButton2-73-23", new Integer(15));
        compBounds.put("jButton6", new Rectangle(166, 243, 73, 23));
        baselinePosition.put("jButton6-73-23", new Integer(15));
        compBounds.put("jScrollPane1", new Rectangle(284, 168, 35, 130));
        baselinePosition.put("jScrollPane1-35-130", new Integer(0));
        compMinSize.put("jPanel1", new Dimension(252, 152));
        compBounds.put("jPanel1", new Rectangle(77, 157, 315, 218));
        prefPaddingInParent.put("jPanel1-jScrollPane1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel1-jButton3-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel1-jScrollPane1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        compPrefSize.put("jPanel1", new Dimension(315, 218));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        prefPaddingInParent.put("Form-jPanel1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton4-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jPanel1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        compBounds.put("Form", new Rectangle(0, 0, 541, 409));
        contInterior.put("Form", new Rectangle(0, 0, 541, 409));
        compBounds.put("jPanel1", new Rectangle(77, 157, 315, 218));
        baselinePosition.put("jPanel1-315-218", new Integer(0));
        compBounds.put("jLabel1", new Rectangle(313, 25, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compBounds.put("jButton4", new Rectangle(291, 94, 73, 23));
        baselinePosition.put("jButton4-73-23", new Integer(15));
        compMinSize.put("Form", new Dimension(402, 386));
        compBounds.put("Form", new Rectangle(0, 0, 541, 409));
        contInterior.put("jPanel1", new Rectangle(77, 157, 315, 218));
        compBounds.put("jButton3", new Rectangle(77, 272, 73, 23));
        baselinePosition.put("jButton3-73-23", new Integer(15));
        compBounds.put("jButton1", new Rectangle(87, 214, 73, 23));
        baselinePosition.put("jButton1-73-23", new Integer(15));
        compBounds.put("jButton2", new Rectangle(87, 243, 73, 23));
        baselinePosition.put("jButton2-73-23", new Integer(15));
        compBounds.put("jButton6", new Rectangle(166, 243, 73, 23));
        baselinePosition.put("jButton6-73-23", new Integer(15));
        compBounds.put("jScrollPane1", new Rectangle(284, 168, 35, 130));
        baselinePosition.put("jScrollPane1-35-130", new Integer(0));
        compMinSize.put("jPanel1", new Dimension(252, 152));
        compBounds.put("jPanel1", new Rectangle(77, 157, 315, 218));
        prefPaddingInParent.put("jPanel1-jScrollPane1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel1-jButton3-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel1-jScrollPane1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jPanel1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton4-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jPanel1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 541, 409));
        compBounds.put("Form", new Rectangle(0, 0, 541, 409));
        prefPaddingInParent.put("Form-jButton4-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
    }

}
