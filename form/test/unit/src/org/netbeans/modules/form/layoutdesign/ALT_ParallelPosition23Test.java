/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 */
package org.netbeans.modules.form.layoutdesign;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.*;
import org.openide.filesystems.FileUtil;

public class ALT_ParallelPosition23Test extends LayoutTestCase {

    public ALT_ParallelPosition23Test(String name) {
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
     * Move the scrollbar above the panel (snap at preferred distance) and right
     * align with it. Tests that no resizing gap (for supp. res. group) should
     * be created even though the scrollbar is aligned at right - because the
     * left checkbox is not aligned with the panel (this is a difference from
     * from ALT_ParallelPosition15Test where OTOH a resizing gap must be created).
     */
    public void doChanges0() {
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 503, 345));
        contInterior.put("Form", new Rectangle(0, 0, 503, 345));
        compBounds.put("jCheckBox1", new Rectangle(370, 75, 81, 23));
        baselinePosition.put("jCheckBox1-81-23", new Integer(15));
        compBounds.put("jPanel1", new Rectangle(28, 103, 310, 100));
        baselinePosition.put("jPanel1-310-100", new Integer(0));
        contInterior.put("jPanel1", new Rectangle(28, 103, 310, 100));
        compBounds.put("jSplitPane1", new Rectangle(38, 114, 179, 25));
        baselinePosition.put("jSplitPane1-179-25", new Integer(-1));
        compMinSize.put("jPanel1", new Dimension(199, 47));
        compBounds.put("jPanel1", new Rectangle(28, 103, 310, 100));
        compPrefSize.put("jPanel1", new Dimension(310, 100));
        prefPaddingInParent.put("jPanel1-jSplitPane1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        prefPaddingInParent.put("jPanel1-jSplitPane1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        compBounds.put("jScrollPane1", new Rectangle(343, 103, 108, 130));
        baselinePosition.put("jScrollPane1-108-130", new Integer(0));
        compBounds.put("jToggleButton1", new Rectangle(28, 243, 105, 23));
        baselinePosition.put("jToggleButton1-105-23", new Integer(15));
        compBounds.put("jScrollBar1", new Rectangle(348, 243, 17, 48));
        baselinePosition.put("jScrollBar1-17-48", new Integer(-1));
        compBounds.put("jCheckBox2", new Rectangle(64, 75, 81, 23));
        baselinePosition.put("jCheckBox2-81-23", new Integer(15));
        compMinSize.put("Form", new Dimension(461, 302));
        compBounds.put("Form", new Rectangle(0, 0, 503, 345));
        prefPaddingInParent.put("Form-jCheckBox1-0-1", new Integer(6)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollPane1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollBar1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jToggleButton1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollBar1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
// > START MOVING
        baselinePosition.put("jScrollBar1-17-48", new Integer(-1));
        {
            String[] compIds = new String[]{
                "jScrollBar1"
            };
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(348, 243, 17, 48)
            };
            Point hotspot = new Point(360, 276);
            ld.startMoving(compIds, bounds, hotspot);
        }
// < START MOVING
        prefPaddingInParent.put("Form-jScrollBar1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollBar1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jCheckBox1-jScrollBar1-1-0-0", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox1-jScrollBar1-1-0-1", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox1-jScrollBar1-1-0-2", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox1-jScrollBar1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox2-jScrollBar1-1-0-0", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox2-jScrollBar1-1-0-1", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox2-jScrollBar1-1-0-2", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox2-jScrollBar1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jCheckBox1-1-0-0", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jCheckBox1-1-0-1", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jCheckBox1-1-0-2", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jCheckBox1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jCheckBox2-1-0-0", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jCheckBox2-1-0-1", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jCheckBox2-1-0-2", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jCheckBox2-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jPanel1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jPanel1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jPanel1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jPanel1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jPanel1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jPanel1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jPanel1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jPanel1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jScrollPane1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jScrollPane1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jScrollPane1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jScrollPane1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jScrollBar1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollBar1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jCheckBox2-jScrollBar1-0-0-0", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox2-jScrollBar1-0-0-1", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox2-jScrollBar1-0-0-2", new Integer(21)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox2-jScrollBar1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jCheckBox2-0-0-0", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jCheckBox2-0-0-1", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jCheckBox2-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jCheckBox2-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox1-jScrollBar1-0-0-0", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox1-jScrollBar1-0-0-1", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox1-jScrollBar1-0-0-2", new Integer(21)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox1-jScrollBar1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jCheckBox1-0-0-0", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jCheckBox1-0-0-1", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jCheckBox1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jCheckBox1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
// > MOVE
        {
            Point p = new Point(330, 82);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(321, 50, 17, 48)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
        prefPaddingInParent.put("Form-jScrollBar1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollBar1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jCheckBox1-jScrollBar1-1-0-0", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox1-jScrollBar1-1-0-1", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox1-jScrollBar1-1-0-2", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox1-jScrollBar1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox2-jScrollBar1-1-0-0", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox2-jScrollBar1-1-0-1", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox2-jScrollBar1-1-0-2", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox2-jScrollBar1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jCheckBox1-1-0-0", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jCheckBox1-1-0-1", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jCheckBox1-1-0-2", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jCheckBox1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jCheckBox2-1-0-0", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jCheckBox2-1-0-1", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jCheckBox2-1-0-2", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jCheckBox2-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jPanel1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jPanel1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jPanel1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jPanel1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jPanel1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jPanel1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jPanel1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jPanel1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jScrollPane1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jScrollPane1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jScrollPane1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jScrollPane1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jScrollBar1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollBar1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jCheckBox2-jScrollBar1-0-0-0", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox2-jScrollBar1-0-0-1", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox2-jScrollBar1-0-0-2", new Integer(21)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox2-jScrollBar1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jCheckBox2-0-0-0", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jCheckBox2-0-0-1", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jCheckBox2-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jCheckBox2-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox1-jScrollBar1-0-0-0", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox1-jScrollBar1-0-0-1", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox1-jScrollBar1-0-0-2", new Integer(21)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox1-jScrollBar1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jCheckBox1-0-0-0", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jCheckBox1-0-0-1", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jCheckBox1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jCheckBox1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
// > MOVE
        {
            Point p = new Point(331, 82);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(321, 50, 17, 48)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
// > END MOVING
        prefPaddingInParent.put("Form-jCheckBox1-0-1", new Integer(6)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollPane1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jToggleButton1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jToggleButton1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jCheckBox2-jScrollBar1-0-0-0", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jCheckBox1-0-0-0", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jScrollBar1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        ld.endMoving(true);
// < END MOVING
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 503, 345));
        contInterior.put("Form", new Rectangle(0, 0, 503, 345));
        compBounds.put("jCheckBox1", new Rectangle(370, 75, 81, 23));
        baselinePosition.put("jCheckBox1-81-23", new Integer(15));
        compBounds.put("jPanel1", new Rectangle(28, 103, 310, 100));
        baselinePosition.put("jPanel1-310-100", new Integer(0));
        contInterior.put("jPanel1", new Rectangle(28, 103, 310, 100));
        compBounds.put("jSplitPane1", new Rectangle(38, 114, 179, 25));
        baselinePosition.put("jSplitPane1-179-25", new Integer(-1));
        compMinSize.put("jPanel1", new Dimension(199, 47));
        compBounds.put("jPanel1", new Rectangle(28, 103, 310, 100));
        compPrefSize.put("jPanel1", new Dimension(310, 100));
        prefPaddingInParent.put("jPanel1-jSplitPane1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        prefPaddingInParent.put("jPanel1-jSplitPane1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        compBounds.put("jScrollPane1", new Rectangle(343, 103, 108, 130));
        baselinePosition.put("jScrollPane1-108-130", new Integer(0));
        compBounds.put("jToggleButton1", new Rectangle(28, 243, 105, 23));
        baselinePosition.put("jToggleButton1-105-23", new Integer(15));
        compBounds.put("jScrollBar1", new Rectangle(321, 50, 17, 48));
        baselinePosition.put("jScrollBar1-17-48", new Integer(-1));
        compBounds.put("jCheckBox2", new Rectangle(64, 75, 81, 23));
        baselinePosition.put("jCheckBox2-81-23", new Integer(15));
        compMinSize.put("Form", new Dimension(461, 277));
        compBounds.put("Form", new Rectangle(0, 0, 503, 345));
        prefPaddingInParent.put("Form-jToggleButton1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollPane1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jCheckBox1-0-1", new Integer(6)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jToggleButton1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
    }
}
