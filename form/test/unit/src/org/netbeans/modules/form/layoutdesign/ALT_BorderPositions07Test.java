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

public class ALT_BorderPositions07Test extends LayoutTestCase {

    private Object changeMark;

    public ALT_BorderPositions07Test(String name) {
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
     * Move jScrolBar1 so it snaps at default distances from top-left corner of
     * the container. Bug 213124.
     */
    public void doChanges0() {
        lm.setChangeRecording(true);
        changeMark = lm.getChangeMark(); 
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 504, 395));
        contInterior.put("Form", new Rectangle(0, 0, 504, 395));
        compBounds.put("jScrollBar1", new Rectangle(0, 29, 17, 341));
        baselinePosition.put("jScrollBar1-17-341", new Integer(0));
        compBounds.put("jComboBox1", new Rectangle(145, 60, 185, 20));
        baselinePosition.put("jComboBox1-185-20", new Integer(14));
        compBounds.put("jButton1", new Rectangle(162, 156, 126, 150));
        baselinePosition.put("jButton1-126-150", new Integer(79));
        compBounds.put("jLabel1", new Rectangle(355, 149, 34, 122));
        baselinePosition.put("jLabel1-34-122", new Integer(65));
        compBounds.put("jCheckBox1", new Rectangle(326, 273, 178, 23));
        baselinePosition.put("jCheckBox1-178-23", new Integer(15));
        compMinSize.put("Form", new Dimension(504, 395));
        compBounds.put("Form", new Rectangle(0, 0, 504, 395));
        compBounds.put("Form", new Rectangle(0, 0, 504, 395));
        compBounds.put("Form", new Rectangle(0, 0, 504, 395));
        compPrefSize.put("jScrollBar1", new Dimension(17, 48));
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        compPrefSize.put("jComboBox1", new Dimension(56, 20));
        prefPadding.put("jComboBox1-jButton1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jComboBox1-jLabel1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jCheckBox1-1-0-0", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        compPrefSize.put("jCheckBox1", new Dimension(81, 23));
        prefPadding.put("jComboBox1-jButton1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jComboBox1-jLabel1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
        prefPadding.put("jComboBox1-jButton1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jComboBox1-jLabel1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
// > START MOVING
        baselinePosition.put("jScrollBar1-17-341", new Integer(0));
        {
            String[] compIds = new String[]{
                "jScrollBar1"
            };
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(0, 29, 17, 341)
            };
            Point hotspot = new Point(8, 127);
            ld.startMoving(compIds, bounds, hotspot);
        }
// < START MOVING
        prefPaddingInParent.put("Form-jScrollBar1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollBar1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollBar1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollBar1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jScrollBar1-jComboBox1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jComboBox1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jComboBox1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jComboBox1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jButton1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jButton1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jButton1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jButton1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jLabel1-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jLabel1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jLabel1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jLabel1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jCheckBox1-0-0-0", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jCheckBox1-0-0-1", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jCheckBox1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jCheckBox1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
// > MOVE
        {
            Point p = new Point(16, 116);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(10, 11, 17, 341)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
        prefPaddingInParent.put("Form-jScrollBar1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollBar1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollBar1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollBar1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jScrollBar1-jComboBox1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jComboBox1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jComboBox1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jComboBox1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jButton1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jButton1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jButton1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jButton1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jLabel1-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jLabel1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jLabel1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jLabel1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jCheckBox1-0-0-0", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jCheckBox1-0-0-1", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jCheckBox1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jCheckBox1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
// > MOVE
        {
            Point p = new Point(16, 115);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(10, 11, 17, 341)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
// > END MOVING
        prefPadding.put("jScrollBar1-jComboBox1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jButton1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        ld.endMoving(true);
// < END MOVING
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 504, 395));
        contInterior.put("Form", new Rectangle(0, 0, 504, 395));
        compBounds.put("jScrollBar1", new Rectangle(10, 11, 17, 341));
        baselinePosition.put("jScrollBar1-17-341", new Integer(0));
        compBounds.put("jComboBox1", new Rectangle(145, 60, 185, 20));
        baselinePosition.put("jComboBox1-185-20", new Integer(14));
        compBounds.put("jButton1", new Rectangle(162, 156, 126, 150));
        baselinePosition.put("jButton1-126-150", new Integer(79));
        compBounds.put("jLabel1", new Rectangle(355, 149, 34, 122));
        baselinePosition.put("jLabel1-34-122", new Integer(65));
        compBounds.put("jCheckBox1", new Rectangle(326, 273, 178, 23));
        baselinePosition.put("jCheckBox1-178-23", new Integer(15));
        compMinSize.put("Form", new Dimension(504, 395));
        compBounds.put("Form", new Rectangle(0, 0, 504, 395));
        prefPaddingInParent.put("Form-jScrollBar1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        compPrefSize.put("jScrollBar1", new Dimension(17, 48));
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        compPrefSize.put("jComboBox1", new Dimension(56, 20));
        prefPadding.put("jLabel1-jCheckBox1-1-0-0", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        compPrefSize.put("jCheckBox1", new Dimension(81, 23));
        prefPaddingInParent.put("Form-jScrollBar1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 504, 395));
        prefPaddingInParent.put("Form-jScrollBar1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollBar1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
    }

    /**
     * Undo previous change. Resize jScrollBar1 slightly to the right.
     */
    public void doChanges1() {
        lm.undo(changeMark, lm.getChangeMark());
        compBounds.put("Form", new Rectangle(0, 0, 504, 395));
        prefPaddingInParent.put("Form-jScrollBar1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollBar1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 504, 395));
        contInterior.put("Form", new Rectangle(0, 0, 504, 395));
        compBounds.put("jScrollBar1", new Rectangle(0, 29, 17, 341));
        baselinePosition.put("jScrollBar1-17-341", new Integer(0));
        compBounds.put("jComboBox1", new Rectangle(145, 60, 185, 20));
        baselinePosition.put("jComboBox1-185-20", new Integer(14));
        compBounds.put("jButton1", new Rectangle(162, 156, 126, 150));
        baselinePosition.put("jButton1-126-150", new Integer(79));
        compBounds.put("jLabel1", new Rectangle(355, 149, 34, 122));
        baselinePosition.put("jLabel1-34-122", new Integer(65));
        compBounds.put("jCheckBox1", new Rectangle(326, 273, 178, 23));
        baselinePosition.put("jCheckBox1-178-23", new Integer(15));
        compMinSize.put("Form", new Dimension(504, 395));
        compBounds.put("Form", new Rectangle(0, 0, 504, 395));
        prefPadding.put("jComboBox1-jButton1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jComboBox1-jLabel1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 504, 395));
        prefPadding.put("jComboBox1-jButton1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jComboBox1-jLabel1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        compBounds.put("Form", new Rectangle(0, 0, 504, 395));
// > START RESIZING
        baselinePosition.put("jScrollBar1-17-341", new Integer(0));
        compPrefSize.put("jScrollBar1", new Dimension(17, 48));
        {
            String[] compIds = new String[]{
                "jScrollBar1"
            };
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(0, 29, 17, 341)
            };
            Point hotspot = new Point(17, 128);
            int[] resizeEdges = new int[]{
                1,
                -1
            };
            boolean inLayout = true;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
// < START RESIZING
// > MOVE
        {
            Point p = new Point(49, 132);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(0, 29, 49, 341)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
// > MOVE
        {
            Point p = new Point(50, 132);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(0, 29, 50, 341)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
// > END MOVING
        prefPadding.put("jScrollBar1-jComboBox1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollBar1-jButton1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        ld.endMoving(true);
// < END MOVING
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 504, 395));
        contInterior.put("Form", new Rectangle(0, 0, 504, 395));
        compBounds.put("jScrollBar1", new Rectangle(0, 29, 50, 341));
        baselinePosition.put("jScrollBar1-50-341", new Integer(0));
        compBounds.put("jComboBox1", new Rectangle(145, 60, 185, 20));
        baselinePosition.put("jComboBox1-185-20", new Integer(14));
        compBounds.put("jButton1", new Rectangle(162, 156, 126, 150));
        baselinePosition.put("jButton1-126-150", new Integer(79));
        compBounds.put("jLabel1", new Rectangle(355, 149, 34, 122));
        baselinePosition.put("jLabel1-34-122", new Integer(65));
        compBounds.put("jCheckBox1", new Rectangle(326, 273, 178, 23));
        baselinePosition.put("jCheckBox1-178-23", new Integer(15));
        compMinSize.put("Form", new Dimension(504, 395));
        compBounds.put("Form", new Rectangle(0, 0, 504, 395));
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        compPrefSize.put("jComboBox1", new Dimension(56, 20));
        prefPadding.put("jComboBox1-jButton1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jComboBox1-jLabel1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jCheckBox1-1-0-0", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        compPrefSize.put("jCheckBox1", new Dimension(81, 23));
        prefPadding.put("jComboBox1-jButton1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jComboBox1-jLabel1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 504, 395));
        compBounds.put("Form", new Rectangle(0, 0, 504, 395));
        prefPadding.put("jComboBox1-jButton1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jComboBox1-jLabel1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        compBounds.put("Form", new Rectangle(0, 0, 504, 395));
        prefPadding.put("jComboBox1-jButton1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jComboBox1-jLabel1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
    }
}
