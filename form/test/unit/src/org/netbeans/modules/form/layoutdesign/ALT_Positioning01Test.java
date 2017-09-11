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

// Tests two special positioning cases.
public class ALT_Positioning01Test extends LayoutTestCase {

    public ALT_Positioning01Test(String name) {
        super(name);
        try {
	    className = this.getClass().getName();
	    className = className.substring(className.lastIndexOf('.') + 1, className.length());	    
            startingFormFile = FileUtil.toFileObject(new File(url.getFile() + goldenFilesPath + className + "-StartingForm.form").getCanonicalFile());
        } catch (IOException ioe) {
            fail(ioe.toString());
        }
    }

    // Add a new button below jLabel5 (snapped). The label is (in vertical
    // dimension) 3 pixels from the edge of the group with jTextField4.
    // The button needs to be added in sequence with this group. (Was problem
    // when the vertical space considered was extended by 4 pixels in
    // LayoutFeeder.addToGroup.)
    public void doChanges0() {
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jLabel5", new Rectangle(10, 134, 34, 14));
        baselinePosition.put("jLabel5-34-14", new Integer(11));
        compPrefSize.put("jLabel5", new Dimension(34, 14));
        compBounds.put("jTextField4", new Rectangle(331, 133, 59, 20));
        baselinePosition.put("jTextField4-59-20", new Integer(14));
        compPrefSize.put("jTextField4", new Dimension(59, 20));
        compBounds.put("jLabel3", new Rectangle(10, 69, 34, 14));
        baselinePosition.put("jLabel3-34-14", new Integer(11));
        compPrefSize.put("jLabel3", new Dimension(34, 14));
        compBounds.put("jLabel4", new Rectangle(10, 98, 34, 14));
        baselinePosition.put("jLabel4-34-14", new Integer(11));
        compPrefSize.put("jLabel4", new Dimension(34, 14));
        compBounds.put("jTextField1", new Rectangle(331, 55, 59, 20));
        baselinePosition.put("jTextField1-59-20", new Integer(14));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compBounds.put("jTextField2", new Rectangle(331, 81, 59, 20));
        baselinePosition.put("jTextField2-59-20", new Integer(14));
        compPrefSize.put("jTextField2", new Dimension(59, 20));
        compBounds.put("jTextField3", new Rectangle(331, 107, 59, 20));
        baselinePosition.put("jTextField3-59-20", new Integer(14));
        compPrefSize.put("jTextField3", new Dimension(59, 20));
        compBounds.put("jTextField5", new Rectangle(331, 159, 59, 20));
        baselinePosition.put("jTextField5-59-20", new Integer(14));
        compPrefSize.put("jTextField5", new Dimension(59, 20));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jLabel5", new Rectangle(10, 134, 34, 14));
        baselinePosition.put("jLabel5-34-14", new Integer(11));
        compBounds.put("jTextField4", new Rectangle(331, 133, 59, 20));
        baselinePosition.put("jTextField4-59-20", new Integer(14));
        compBounds.put("jLabel3", new Rectangle(10, 69, 34, 14));
        baselinePosition.put("jLabel3-34-14", new Integer(11));
        compBounds.put("jLabel4", new Rectangle(10, 98, 34, 14));
        baselinePosition.put("jLabel4-34-14", new Integer(11));
        compBounds.put("jTextField1", new Rectangle(331, 55, 59, 20));
        baselinePosition.put("jTextField1-59-20", new Integer(14));
        compBounds.put("jTextField2", new Rectangle(331, 81, 59, 20));
        baselinePosition.put("jTextField2-59-20", new Integer(14));
        compBounds.put("jTextField3", new Rectangle(331, 107, 59, 20));
        baselinePosition.put("jTextField3-59-20", new Integer(14));
        compBounds.put("jTextField5", new Rectangle(331, 159, 59, 20));
        baselinePosition.put("jTextField5-59-20", new Integer(14));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
        lc = new LayoutComponent("jButton1", false);
        // > START ADDING
        baselinePosition.put("jButton1-75-23", new Integer(15));
        {
            LayoutComponent[] comps = new LayoutComponent[] { lc };
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(0, 0, 75, 23)
                };
            String defaultContId = null;
            Point hotspot = new Point(33,11);
            ld.startAdding(comps, bounds, hotspot, defaultContId);
        }
        // < START ADDING
        prefPaddingInParent.put("Form-jButton1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jLabel3-jButton1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jButton1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel5-jButton1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jButton1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jTextField5-jButton1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jTextField5-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel5-jButton1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField4-jButton1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        {
            Point p = new Point(64,173);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(31, 158, 75, 23)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPaddingInParent.put("Form-jButton1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jLabel3-jButton1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jButton1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel5-jButton1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jButton1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jTextField5-jButton1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jTextField5-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel5-jButton1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField4-jButton1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        {
            Point p = new Point(64,172);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(31, 154, 75, 23)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        compPrefSize.put("jButton1", new Dimension(75, 23));
        compPrefSize.put("jButton1", new Dimension(75, 23));
        prefPaddingInParent.put("Form-jButton1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jButton1-jTextField5-0-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jButton1-1-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jLabel5", new Rectangle(10, 134, 34, 14));
        baselinePosition.put("jLabel5-34-14", new Integer(11));
        compPrefSize.put("jLabel5", new Dimension(34, 14));
        compBounds.put("jTextField4", new Rectangle(331, 133, 59, 20));
        baselinePosition.put("jTextField4-59-20", new Integer(14));
        compPrefSize.put("jTextField4", new Dimension(59, 20));
        compBounds.put("jLabel3", new Rectangle(10, 69, 34, 14));
        baselinePosition.put("jLabel3-34-14", new Integer(11));
        compPrefSize.put("jLabel3", new Dimension(34, 14));
        compBounds.put("jLabel4", new Rectangle(10, 98, 34, 14));
        baselinePosition.put("jLabel4-34-14", new Integer(11));
        compPrefSize.put("jLabel4", new Dimension(34, 14));
        compBounds.put("jTextField1", new Rectangle(331, 55, 59, 20));
        baselinePosition.put("jTextField1-59-20", new Integer(14));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compBounds.put("jTextField2", new Rectangle(331, 81, 59, 20));
        baselinePosition.put("jTextField2-59-20", new Integer(14));
        compPrefSize.put("jTextField2", new Dimension(59, 20));
        compBounds.put("jTextField3", new Rectangle(331, 107, 59, 20));
        baselinePosition.put("jTextField3-59-20", new Integer(14));
        compPrefSize.put("jTextField3", new Dimension(59, 20));
        compBounds.put("jTextField5", new Rectangle(331, 159, 59, 20));
        baselinePosition.put("jTextField5-59-20", new Integer(14));
        compPrefSize.put("jTextField5", new Dimension(59, 20));
        compBounds.put("jButton1", new Rectangle(31, 159, 75, 23));
        baselinePosition.put("jButton1-75-23", new Integer(15));
        compPrefSize.put("jButton1", new Dimension(75, 23));
        prefPaddingInParent.put("Form-jTextField5-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jLabel5", new Rectangle(10, 134, 34, 14));
        baselinePosition.put("jLabel5-34-14", new Integer(11));
        compBounds.put("jTextField4", new Rectangle(331, 133, 59, 20));
        baselinePosition.put("jTextField4-59-20", new Integer(14));
        compBounds.put("jLabel3", new Rectangle(10, 69, 34, 14));
        baselinePosition.put("jLabel3-34-14", new Integer(11));
        compBounds.put("jLabel4", new Rectangle(10, 98, 34, 14));
        baselinePosition.put("jLabel4-34-14", new Integer(11));
        compBounds.put("jTextField1", new Rectangle(331, 55, 59, 20));
        baselinePosition.put("jTextField1-59-20", new Integer(14));
        compBounds.put("jTextField2", new Rectangle(331, 81, 59, 20));
        baselinePosition.put("jTextField2-59-20", new Integer(14));
        compBounds.put("jTextField3", new Rectangle(331, 107, 59, 20));
        baselinePosition.put("jTextField3-59-20", new Integer(14));
        compBounds.put("jTextField5", new Rectangle(331, 159, 59, 20));
        baselinePosition.put("jTextField5-59-20", new Integer(14));
        compBounds.put("jButton1", new Rectangle(31, 159, 75, 23));
        baselinePosition.put("jButton1-75-23", new Integer(15));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }

    // Add a new button left of jTextField1 so that it does not overlap jLabel3
    // in vertical dimension; no snapping.
    // In horizontal dimension a new sequence with explicit trailing alignment
    // should be created (with fixed trailing gap following the button).
    // In vertical dimension the button should be put on a closed row with
    // jTextField1, as a result of preference of creating rows in vertical dimension.
    // (In previous version it used to be added in sequence with jLabel3 - but that
    // should happen only if the button is added horizontally closer to it,
    // which is not the case here, the button is added closer to jTextField1.)
    public void doChanges1() {
        lc = new LayoutComponent("jButton2", false);
        // > START ADDING
        baselinePosition.put("jButton2-75-23", new Integer(15));
        {
            LayoutComponent[] comps = new LayoutComponent[] { lc };
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(0, 0, 75, 23)
                };
            String defaultContId = null;
            Point hotspot = new Point(33,11);
            ld.startAdding(comps, bounds, hotspot, defaultContId);
        }
        // < START ADDING
        prefPaddingInParent.put("Form-jButton2-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton2-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton2-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jButton2-jTextField1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        {
            Point p = new Point(263,52);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(230, 41, 75, 23)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPaddingInParent.put("Form-jButton2-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton2-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton2-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jButton2-jTextField1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        {
            Point p = new Point(263,53);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(230, 42, 75, 23)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        compPrefSize.put("jButton2", new Dimension(75, 23));
        compPrefSize.put("jButton2", new Dimension(75, 23));
        prefPaddingInParent.put("Form-jButton2-1-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jButton2-jLabel3-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jLabel5", new Rectangle(10, 134, 34, 14));
        baselinePosition.put("jLabel5-34-14", new Integer(11));
        compPrefSize.put("jLabel5", new Dimension(34, 14));
        compBounds.put("jTextField4", new Rectangle(331, 133, 59, 20));
        baselinePosition.put("jTextField4-59-20", new Integer(14));
        compPrefSize.put("jTextField4", new Dimension(59, 20));
        compBounds.put("jLabel3", new Rectangle(10, 69, 34, 14));
        baselinePosition.put("jLabel3-34-14", new Integer(11));
        compPrefSize.put("jLabel3", new Dimension(34, 14));
        compBounds.put("jLabel4", new Rectangle(10, 98, 34, 14));
        baselinePosition.put("jLabel4-34-14", new Integer(11));
        compPrefSize.put("jLabel4", new Dimension(34, 14));
        compBounds.put("jTextField1", new Rectangle(331, 55, 59, 20));
        baselinePosition.put("jTextField1-59-20", new Integer(14));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compBounds.put("jTextField2", new Rectangle(331, 81, 59, 20));
        baselinePosition.put("jTextField2-59-20", new Integer(14));
        compPrefSize.put("jTextField2", new Dimension(59, 20));
        compBounds.put("jTextField3", new Rectangle(331, 107, 59, 20));
        baselinePosition.put("jTextField3-59-20", new Integer(14));
        compPrefSize.put("jTextField3", new Dimension(59, 20));
        compBounds.put("jTextField5", new Rectangle(331, 159, 59, 20));
        baselinePosition.put("jTextField5-59-20", new Integer(14));
        compPrefSize.put("jTextField5", new Dimension(59, 20));
        compBounds.put("jButton1", new Rectangle(31, 159, 75, 23));
        baselinePosition.put("jButton1-75-23", new Integer(15));
        compPrefSize.put("jButton1", new Dimension(75, 23));
        compBounds.put("jButton2", new Rectangle(230, 42, 75, 23));
        baselinePosition.put("jButton2-75-23", new Integer(15));
        compPrefSize.put("jButton2", new Dimension(75, 23));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jLabel5", new Rectangle(10, 134, 34, 14));
        baselinePosition.put("jLabel5-34-14", new Integer(11));
        compBounds.put("jTextField4", new Rectangle(331, 133, 59, 20));
        baselinePosition.put("jTextField4-59-20", new Integer(14));
        compBounds.put("jLabel3", new Rectangle(10, 69, 34, 14));
        baselinePosition.put("jLabel3-34-14", new Integer(11));
        compBounds.put("jLabel4", new Rectangle(10, 98, 34, 14));
        baselinePosition.put("jLabel4-34-14", new Integer(11));
        compBounds.put("jTextField1", new Rectangle(331, 55, 59, 20));
        baselinePosition.put("jTextField1-59-20", new Integer(14));
        compBounds.put("jTextField2", new Rectangle(331, 81, 59, 20));
        baselinePosition.put("jTextField2-59-20", new Integer(14));
        compBounds.put("jTextField3", new Rectangle(331, 107, 59, 20));
        baselinePosition.put("jTextField3-59-20", new Integer(14));
        compBounds.put("jTextField5", new Rectangle(331, 159, 59, 20));
        baselinePosition.put("jTextField5-59-20", new Integer(14));
        compBounds.put("jButton1", new Rectangle(31, 159, 75, 23));
        baselinePosition.put("jButton1-75-23", new Integer(15));
        compBounds.put("jButton2", new Rectangle(230, 42, 75, 23));
        baselinePosition.put("jButton2-75-23", new Integer(15));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }
    
}
