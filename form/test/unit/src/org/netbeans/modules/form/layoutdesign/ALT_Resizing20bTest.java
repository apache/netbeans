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

/**
 * Inspired from the bug 213119. The form differes from ALT_Resizing20aTest in
 * that scrollPane1 was deleted.
 * Each tested change is undone before next one starts, i.e. always starting
 * from the initial state.
 */
public class ALT_Resizing20bTest extends LayoutTestCase {

    private Object changeMark;

    public ALT_Resizing20bTest(String name) {
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
     * Resize choice1 slightly to the right.
     * No change should happen in either dimension, except changing the
     * horizontal size of the resized component. I.e. the horizontal layout
     * should not be fixed.
     */
    public void doChanges0() {
        lm.setChangeRecording(true);
        changeMark = lm.getChangeMark(); 
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 1338, 376));
        contInterior.put("Form", new Rectangle(0, 0, 1338, 376));
        compBounds.put("panel1", new Rectangle(0, 0, 0, 0));
        baselinePosition.put("panel1-0-0", new Integer(0));
        compBounds.put("canvas1", new Rectangle(0, 0, 0, 0));
        baselinePosition.put("canvas1-0-0", new Integer(0));
        compBounds.put("list1", new Rectangle(53, 26, 40, 200));
        baselinePosition.put("list1-40-200", new Integer(0));
        compBounds.put("checkbox1", new Rectangle(10, 346, 109, 20));
        baselinePosition.put("checkbox1-109-20", new Integer(0));
        compBounds.put("button1", new Rectangle(129, 304, 98, 62));
        baselinePosition.put("button1-98-62", new Integer(0));
        compBounds.put("label1", new Rectangle(169, 26, 38, 20));
        baselinePosition.put("label1-38-20", new Integer(0));
        compBounds.put("scrollbar1", new Rectangle(191, 101, 16, 48));
        baselinePosition.put("scrollbar1-16-48", new Integer(0));
        compBounds.put("scrollbar2", new Rectangle(279, 26, 48, 16));
        baselinePosition.put("scrollbar2-48-16", new Integer(0));
        compBounds.put("choice1", new Rectangle(492, 80, 28, 200));
        baselinePosition.put("choice1-28-200", new Integer(0));
        compBounds.put("textArea1", new Rectangle(129, 200, 245, 80));
        baselinePosition.put("textArea1-245-80", new Integer(0));
        compMinSize.put("Form", new Dimension(1231, 362));
        compBounds.put("Form", new Rectangle(0, 0, 1338, 376));
        compBounds.put("Form", new Rectangle(0, 0, 1338, 376));
        compBounds.put("Form", new Rectangle(0, 0, 1338, 376));
        contInterior.put("panel1", new Rectangle(0, 0, 0, 0));
        compMinSize.put("panel1", new Dimension(0, 0));
        compBounds.put("panel1", new Rectangle(0, 0, 0, 0));
        compPrefSize.put("panel1", new Dimension(0, 0));
        hasExplicitPrefSize.put("panel1", new Boolean(false));
        hasExplicitPrefSize.put("panel1", new Boolean(false));
        prefPadding.put("scrollbar2-textArea1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("scrollbar1-textArea1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("choice1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("choice1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
        prefPadding.put("choice1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("choice1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-checkbox1-1-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-button1-1-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("scrollbar2-textArea1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("scrollbar1-textArea1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-checkbox1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        compBounds.put("panel1", new Rectangle(0, 0, 0, 0));
        compBounds.put("canvas1", new Rectangle(0, 0, 0, 0));
        compBounds.put("textArea1", new Rectangle(129, 200, 245, 80));
        prefPadding.put("choice1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("choice1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        compBounds.put("textArea1", new Rectangle(129, 200, 245, 80));
// > START RESIZING
        baselinePosition.put("choice1-28-200", new Integer(0));
        compPrefSize.put("choice1", new Dimension(28, 20));
        {
            String[] compIds = new String[]{
                "choice1"
            };
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(492, 80, 28, 200)
            };
            Point hotspot = new Point(521, 177);
            int[] resizeEdges = new int[]{
                1,
                -1
            };
            boolean inLayout = true;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
// < START RESIZING
        prefPaddingInParent.put("Form-choice1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
// > MOVE
        {
            Point p = new Point(536, 179);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(492, 80, 43, 200)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
        prefPaddingInParent.put("Form-choice1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
// > MOVE
        {
            Point p = new Point(537, 179);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(492, 80, 44, 200)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
// > END MOVING
        prefPadding.put("list1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("scrollbar2-choice1-0-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-panel1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-canvas1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-button1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-textArea1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-choice1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        ld.endMoving(true);
// < END MOVING
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 1338, 376));
        contInterior.put("Form", new Rectangle(0, 0, 1338, 376));
        compBounds.put("panel1", new Rectangle(0, 0, 0, 0));
        baselinePosition.put("panel1-0-0", new Integer(0));
        compBounds.put("canvas1", new Rectangle(0, 0, 0, 0));
        baselinePosition.put("canvas1-0-0", new Integer(0));
        compBounds.put("list1", new Rectangle(53, 26, 40, 200));
        baselinePosition.put("list1-40-200", new Integer(0));
        compBounds.put("checkbox1", new Rectangle(10, 346, 109, 20));
        baselinePosition.put("checkbox1-109-20", new Integer(0));
        compBounds.put("button1", new Rectangle(129, 304, 98, 62));
        baselinePosition.put("button1-98-62", new Integer(0));
        compBounds.put("label1", new Rectangle(169, 26, 38, 20));
        baselinePosition.put("label1-38-20", new Integer(0));
        compBounds.put("scrollbar1", new Rectangle(191, 101, 16, 48));
        baselinePosition.put("scrollbar1-16-48", new Integer(0));
        compBounds.put("scrollbar2", new Rectangle(279, 26, 48, 16));
        baselinePosition.put("scrollbar2-48-16", new Integer(0));
        compBounds.put("choice1", new Rectangle(492, 80, 44, 200));
        baselinePosition.put("choice1-44-200", new Integer(0));
        compBounds.put("textArea1", new Rectangle(129, 200, 245, 80));
        baselinePosition.put("textArea1-245-80", new Integer(0));
        compMinSize.put("Form", new Dimension(1338, 362));
        compBounds.put("Form", new Rectangle(0, 0, 1338, 376));
        contInterior.put("panel1", new Rectangle(0, 0, 0, 0));
        compMinSize.put("panel1", new Dimension(0, 0));
        compBounds.put("panel1", new Rectangle(0, 0, 0, 0));
        compPrefSize.put("panel1", new Dimension(0, 0));
        hasExplicitPrefSize.put("panel1", new Boolean(false));
        hasExplicitPrefSize.put("panel1", new Boolean(false));
        prefPadding.put("scrollbar2-textArea1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("scrollbar1-textArea1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("choice1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("choice1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 1338, 376));
        prefPadding.put("choice1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("choice1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        compBounds.put("textArea1", new Rectangle(129, 200, 245, 80));
        compBounds.put("textArea1", new Rectangle(129, 200, 245, 80));
        compBounds.put("textArea1", new Rectangle(129, 200, 245, 80));
    }

    /**
     * Undo previous change. Resize choice1 slightly top-right, but not getting
     * vertically over scrollBar2.
     * In vertical dimension choice1 should get to sequence with panel1 and
     * canvas1, and also with scrollBar2, as a result of preference of creating
     * rows in vertical dimension. (Originally thought it could end up parallel
     * with scrollBar2 as a result of detecting it in sequence in horizontal
     * dimension that is processed first in this resizing. But it would be
     * inconsistent with situation where just positioning or resizing only
     * vertically (doChanges2) where the horizontal result could not be anticipated.)
     * Horizontal layout should not change, except the width of the component.
     * (Unlike doChanges3 where the horizontal layout is repaired to have
     * textArea1 in sequence where it visually belongs.)
     */
    public void doChanges1() {
        lm.undo(changeMark, lm.getChangeMark());
        changeMark = lm.getChangeMark(); 
        compBounds.put("textArea1", new Rectangle(129, 200, 245, 80));
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 1338, 376));
        contInterior.put("Form", new Rectangle(0, 0, 1338, 376));
        compBounds.put("panel1", new Rectangle(0, 0, 0, 0));
        baselinePosition.put("panel1-0-0", new Integer(0));
        compBounds.put("canvas1", new Rectangle(0, 0, 0, 0));
        baselinePosition.put("canvas1-0-0", new Integer(0));
        compBounds.put("list1", new Rectangle(53, 26, 40, 200));
        baselinePosition.put("list1-40-200", new Integer(0));
        compBounds.put("checkbox1", new Rectangle(10, 346, 109, 20));
        baselinePosition.put("checkbox1-109-20", new Integer(0));
        compBounds.put("button1", new Rectangle(129, 304, 98, 62));
        baselinePosition.put("button1-98-62", new Integer(0));
        compBounds.put("label1", new Rectangle(169, 26, 38, 20));
        baselinePosition.put("label1-38-20", new Integer(0));
        compBounds.put("scrollbar1", new Rectangle(191, 101, 16, 48));
        baselinePosition.put("scrollbar1-16-48", new Integer(0));
        compBounds.put("scrollbar2", new Rectangle(279, 26, 48, 16));
        baselinePosition.put("scrollbar2-48-16", new Integer(0));
        compBounds.put("choice1", new Rectangle(492, 80, 28, 200));
        baselinePosition.put("choice1-28-200", new Integer(0));
        compBounds.put("textArea1", new Rectangle(129, 200, 245, 80));
        baselinePosition.put("textArea1-245-80", new Integer(0));
        compMinSize.put("Form", new Dimension(1231, 362));
        compBounds.put("Form", new Rectangle(0, 0, 1338, 376));
        contInterior.put("panel1", new Rectangle(0, 0, 0, 0));
        compMinSize.put("panel1", new Dimension(0, 0));
        compBounds.put("panel1", new Rectangle(0, 0, 0, 0));
        prefPadding.put("scrollbar2-textArea1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("scrollbar1-textArea1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("choice1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("choice1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 1338, 376));
        prefPadding.put("choice1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("choice1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        compBounds.put("textArea1", new Rectangle(129, 200, 245, 80));
        compBounds.put("textArea1", new Rectangle(129, 200, 245, 80));
// > START RESIZING
        baselinePosition.put("choice1-28-200", new Integer(0));
        compPrefSize.put("choice1", new Dimension(28, 20));
        {
            String[] compIds = new String[]{
                "choice1"
            };
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(492, 80, 28, 200)
            };
            Point hotspot = new Point(524, 79);
            int[] resizeEdges = new int[]{
                1,
                0
            };
            boolean inLayout = true;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
// < START RESIZING
        prefPaddingInParent.put("Form-choice1-1-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-choice1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
// > MOVE
        {
            Point p = new Point(540, 71);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(492, 72, 44, 208)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
        prefPaddingInParent.put("Form-choice1-1-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-choice1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
// > MOVE
        {
            Point p = new Point(541, 70);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(492, 71, 45, 209)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
// > END MOVING
        prefPadding.put("list1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("scrollbar2-choice1-0-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-panel1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-canvas1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-button1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-textArea1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-choice1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        ld.endMoving(true);
// < END MOVING
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 1338, 376));
        contInterior.put("Form", new Rectangle(0, 0, 1338, 376));
        compBounds.put("panel1", new Rectangle(0, 0, 0, 0));
        baselinePosition.put("panel1-0-0", new Integer(0));
        compBounds.put("canvas1", new Rectangle(0, 0, 0, 0));
        baselinePosition.put("canvas1-0-0", new Integer(0));
        compBounds.put("list1", new Rectangle(53, 26, 40, 200));
        baselinePosition.put("list1-40-200", new Integer(0));
        compBounds.put("checkbox1", new Rectangle(10, 346, 109, 20));
        baselinePosition.put("checkbox1-109-20", new Integer(0));
        compBounds.put("button1", new Rectangle(129, 304, 98, 62));
        baselinePosition.put("button1-98-62", new Integer(0));
        compBounds.put("label1", new Rectangle(169, 26, 38, 20));
        baselinePosition.put("label1-38-20", new Integer(0));
        compBounds.put("scrollbar1", new Rectangle(191, 101, 16, 48));
        baselinePosition.put("scrollbar1-16-48", new Integer(0));
        compBounds.put("scrollbar2", new Rectangle(279, 26, 48, 16));
        baselinePosition.put("scrollbar2-48-16", new Integer(0));
        compBounds.put("choice1", new Rectangle(492, 71, 45, 209));
        baselinePosition.put("choice1-45-209", new Integer(0));
        compBounds.put("textArea1", new Rectangle(129, 200, 245, 80));
        baselinePosition.put("textArea1-245-80", new Integer(0));
        compMinSize.put("Form", new Dimension(1338, 362));
        compBounds.put("Form", new Rectangle(0, 0, 1338, 376));
        contInterior.put("panel1", new Rectangle(0, 0, 0, 0));
        compMinSize.put("panel1", new Dimension(0, 0));
        compBounds.put("panel1", new Rectangle(0, 0, 0, 0));
        compPrefSize.put("panel1", new Dimension(0, 0));
        hasExplicitPrefSize.put("panel1", new Boolean(false));
        hasExplicitPrefSize.put("panel1", new Boolean(false));
        prefPadding.put("choice1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("choice1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 1338, 376));
        prefPadding.put("choice1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("choice1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        compBounds.put("textArea1", new Rectangle(129, 200, 245, 80));
        compBounds.put("textArea1", new Rectangle(129, 200, 245, 80));
        compBounds.put("textArea1", new Rectangle(129, 200, 245, 80));
        compBounds.put("textArea1", new Rectangle(129, 200, 245, 80));
    }

    /**
     * Undo previous change. Resize choice1 slightly upwards.
     * In vertical layout choice1 should get to sequence with scrollBar2, as a
     * result of preference of creating rows in vertical dimension. This does
     * not happen in doChanges1 nor doChanges3 which also resize upwards, but
     * they also resize horizontally where the horizontal layout is done first
     * so vertical analysis then finds that scrollBar2 is already in sequence
     * horizontally and so does not apply the row preference rule. This is
     * probably more correct, however in plain vertical resizing where vertical
     * dimension goes first we don't have the detection of sequence in
     * horizontal dimension.
     * Horizontal layout should not change.
     */
    public void doChanges2() {
        lm.undo(changeMark, lm.getChangeMark());
        changeMark = lm.getChangeMark(); 
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 1338, 376));
        contInterior.put("Form", new Rectangle(0, 0, 1338, 376));
        compBounds.put("panel1", new Rectangle(0, 0, 0, 0));
        baselinePosition.put("panel1-0-0", new Integer(0));
        compBounds.put("canvas1", new Rectangle(0, 0, 0, 0));
        baselinePosition.put("canvas1-0-0", new Integer(0));
        compBounds.put("list1", new Rectangle(53, 26, 40, 200));
        baselinePosition.put("list1-40-200", new Integer(0));
        compBounds.put("checkbox1", new Rectangle(10, 346, 109, 20));
        baselinePosition.put("checkbox1-109-20", new Integer(0));
        compBounds.put("button1", new Rectangle(129, 304, 98, 62));
        baselinePosition.put("button1-98-62", new Integer(0));
        compBounds.put("label1", new Rectangle(169, 26, 38, 20));
        baselinePosition.put("label1-38-20", new Integer(0));
        compBounds.put("scrollbar1", new Rectangle(191, 101, 16, 48));
        baselinePosition.put("scrollbar1-16-48", new Integer(0));
        compBounds.put("scrollbar2", new Rectangle(279, 26, 48, 16));
        baselinePosition.put("scrollbar2-48-16", new Integer(0));
        compBounds.put("choice1", new Rectangle(492, 80, 28, 200));
        baselinePosition.put("choice1-28-200", new Integer(0));
        compBounds.put("textArea1", new Rectangle(129, 200, 245, 80));
        baselinePosition.put("textArea1-245-80", new Integer(0));
        compMinSize.put("Form", new Dimension(1231, 362));
        compBounds.put("Form", new Rectangle(0, 0, 1338, 376));
        contInterior.put("panel1", new Rectangle(0, 0, 0, 0));
        compMinSize.put("panel1", new Dimension(0, 0));
        compBounds.put("panel1", new Rectangle(0, 0, 0, 0));
        prefPadding.put("scrollbar2-textArea1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("scrollbar1-textArea1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("choice1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("choice1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 1338, 376));
        prefPadding.put("choice1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("choice1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        compBounds.put("textArea1", new Rectangle(129, 200, 245, 80));
        compBounds.put("textArea1", new Rectangle(129, 200, 245, 80));
// > START RESIZING
        baselinePosition.put("choice1-28-200", new Integer(0));
        compPrefSize.put("choice1", new Dimension(28, 20));
        {
            String[] compIds = new String[]{
                "choice1"
            };
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(492, 80, 28, 200)
            };
            Point hotspot = new Point(506, 77);
            int[] resizeEdges = new int[]{
                -1,
                0
            };
            boolean inLayout = true;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
// < START RESIZING
        prefPaddingInParent.put("Form-choice1-1-0", new Integer(10)); // parentId-compId-dimension-compAlignment
// > MOVE
        {
            Point p = new Point(507, 61);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(492, 64, 28, 216)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
        prefPaddingInParent.put("Form-choice1-1-0", new Integer(10)); // parentId-compId-dimension-compAlignment
// > MOVE
        {
            Point p = new Point(508, 61);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(492, 64, 28, 216)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
// > END MOVING
        prefPadding.put("list1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("scrollbar2-choice1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        ld.endMoving(true);
// < END MOVING
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 1338, 376));
        contInterior.put("Form", new Rectangle(0, 0, 1338, 376));
        compBounds.put("panel1", new Rectangle(0, 0, 0, 0));
        baselinePosition.put("panel1-0-0", new Integer(0));
        compBounds.put("canvas1", new Rectangle(0, 0, 0, 0));
        baselinePosition.put("canvas1-0-0", new Integer(0));
        compBounds.put("list1", new Rectangle(53, 26, 40, 200));
        baselinePosition.put("list1-40-200", new Integer(0));
        compBounds.put("checkbox1", new Rectangle(10, 346, 109, 20));
        baselinePosition.put("checkbox1-109-20", new Integer(0));
        compBounds.put("button1", new Rectangle(129, 304, 98, 62));
        baselinePosition.put("button1-98-62", new Integer(0));
        compBounds.put("label1", new Rectangle(169, 26, 38, 20));
        baselinePosition.put("label1-38-20", new Integer(0));
        compBounds.put("scrollbar1", new Rectangle(191, 101, 16, 48));
        baselinePosition.put("scrollbar1-16-48", new Integer(0));
        compBounds.put("scrollbar2", new Rectangle(279, 26, 48, 16));
        baselinePosition.put("scrollbar2-48-16", new Integer(0));
        compBounds.put("choice1", new Rectangle(492, 64, 28, 216));
        baselinePosition.put("choice1-28-216", new Integer(0));
        compBounds.put("textArea1", new Rectangle(129, 200, 245, 80));
        baselinePosition.put("textArea1-245-80", new Integer(0));
        compMinSize.put("Form", new Dimension(1231, 362));
        compBounds.put("Form", new Rectangle(0, 0, 1338, 376));
        contInterior.put("panel1", new Rectangle(0, 0, 0, 0));
        compMinSize.put("panel1", new Dimension(0, 0));
        compBounds.put("panel1", new Rectangle(0, 0, 0, 0));
        compPrefSize.put("panel1", new Dimension(0, 0));
        hasExplicitPrefSize.put("panel1", new Boolean(false));
        hasExplicitPrefSize.put("panel1", new Boolean(false));
        prefPadding.put("list1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("choice1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("choice1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 1338, 376));
        prefPadding.put("list1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("choice1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("choice1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        compBounds.put("textArea1", new Rectangle(129, 200, 245, 80));
        compBounds.put("textArea1", new Rectangle(129, 200, 245, 80));
        compBounds.put("textArea1", new Rectangle(129, 200, 245, 80));
        compBounds.put("textArea1", new Rectangle(129, 200, 245, 80));
    }

    /**
     * Undo previous change. Resize choice1 slightly top-left, but not getting
     * vertically over scrollBar2.
     * In vertical dimension choice1 should get to sequence with panel1 and
     * canvas1, and also with scrollBar2, as a result of preference of creating
     * rows in vertical dimension. (Same situation as in doChanges1.)
     * In horizontal dimension the layout should be fixed and textArea1 also
     * become in sequence with choice1.
     */
    public void doChanges3() {
        lm.undo(changeMark, lm.getChangeMark());
        changeMark = lm.getChangeMark(); 
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 1338, 376));
        contInterior.put("Form", new Rectangle(0, 0, 1338, 376));
        compBounds.put("panel1", new Rectangle(0, 0, 0, 0));
        baselinePosition.put("panel1-0-0", new Integer(0));
        compBounds.put("canvas1", new Rectangle(0, 0, 0, 0));
        baselinePosition.put("canvas1-0-0", new Integer(0));
        compBounds.put("list1", new Rectangle(53, 26, 40, 200));
        baselinePosition.put("list1-40-200", new Integer(0));
        compBounds.put("checkbox1", new Rectangle(10, 346, 109, 20));
        baselinePosition.put("checkbox1-109-20", new Integer(0));
        compBounds.put("button1", new Rectangle(129, 304, 98, 62));
        baselinePosition.put("button1-98-62", new Integer(0));
        compBounds.put("label1", new Rectangle(169, 26, 38, 20));
        baselinePosition.put("label1-38-20", new Integer(0));
        compBounds.put("scrollbar1", new Rectangle(191, 101, 16, 48));
        baselinePosition.put("scrollbar1-16-48", new Integer(0));
        compBounds.put("scrollbar2", new Rectangle(279, 26, 48, 16));
        baselinePosition.put("scrollbar2-48-16", new Integer(0));
        compBounds.put("choice1", new Rectangle(492, 80, 28, 200));
        baselinePosition.put("choice1-28-200", new Integer(0));
        compBounds.put("textArea1", new Rectangle(129, 200, 245, 80));
        baselinePosition.put("textArea1-245-80", new Integer(0));
        compMinSize.put("Form", new Dimension(1231, 362));
        compBounds.put("Form", new Rectangle(0, 0, 1338, 376));
        contInterior.put("panel1", new Rectangle(0, 0, 0, 0));
        compMinSize.put("panel1", new Dimension(0, 0));
        compBounds.put("panel1", new Rectangle(0, 0, 0, 0));
        prefPadding.put("scrollbar2-textArea1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("scrollbar1-textArea1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("choice1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("choice1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 1338, 376));
        prefPadding.put("choice1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("choice1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        compBounds.put("textArea1", new Rectangle(129, 200, 245, 80));
        compBounds.put("textArea1", new Rectangle(129, 200, 245, 80));
// > START RESIZING
        baselinePosition.put("choice1-28-200", new Integer(0));
        compPrefSize.put("choice1", new Dimension(28, 20));
        {
            String[] compIds = new String[]{
                "choice1"
            };
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(492, 80, 28, 200)
            };
            Point hotspot = new Point(489, 79);
            int[] resizeEdges = new int[]{
                0,
                0
            };
            boolean inLayout = true;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
// < START RESIZING
        prefPaddingInParent.put("Form-choice1-1-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("scrollbar1-choice1-0-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("scrollbar1-choice1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("scrollbar1-choice1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("scrollbar1-choice1-0-0-3", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
// > MOVE
        {
            Point p = new Point(468, 69);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(471, 70, 49, 210)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
        prefPaddingInParent.put("Form-choice1-1-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("scrollbar1-choice1-0-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("scrollbar1-choice1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("scrollbar1-choice1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("scrollbar1-choice1-0-0-3", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
// > MOVE
        {
            Point p = new Point(468, 68);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(471, 69, 49, 211)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
// > END MOVING
        prefPadding.put("list1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("scrollbar2-choice1-0-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-choice1-0-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-choice1-0-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("scrollbar2-choice1-0-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-panel1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-canvas1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-button1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-choice1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        ld.endMoving(true);
// < END MOVING
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 1338, 376));
        contInterior.put("Form", new Rectangle(0, 0, 1338, 376));
        compBounds.put("panel1", new Rectangle(0, 0, 0, 0));
        baselinePosition.put("panel1-0-0", new Integer(0));
        compBounds.put("canvas1", new Rectangle(0, 0, 0, 0));
        baselinePosition.put("canvas1-0-0", new Integer(0));
        compBounds.put("list1", new Rectangle(53, 26, 40, 200));
        baselinePosition.put("list1-40-200", new Integer(0));
        compBounds.put("checkbox1", new Rectangle(10, 346, 109, 20));
        baselinePosition.put("checkbox1-109-20", new Integer(0));
        compBounds.put("button1", new Rectangle(129, 304, 98, 62));
        baselinePosition.put("button1-98-62", new Integer(0));
        compBounds.put("label1", new Rectangle(169, 26, 38, 20));
        baselinePosition.put("label1-38-20", new Integer(0));
        compBounds.put("scrollbar1", new Rectangle(191, 101, 16, 48));
        baselinePosition.put("scrollbar1-16-48", new Integer(0));
        compBounds.put("scrollbar2", new Rectangle(279, 26, 48, 16));
        baselinePosition.put("scrollbar2-48-16", new Integer(0));
        compBounds.put("choice1", new Rectangle(471, 69, 49, 211));
        baselinePosition.put("choice1-49-211", new Integer(0));
        compBounds.put("textArea1", new Rectangle(129, 200, 245, 80));
        baselinePosition.put("textArea1-245-80", new Integer(0));
        compMinSize.put("Form", new Dimension(1338, 362));
        compBounds.put("Form", new Rectangle(0, 0, 1338, 376));
        contInterior.put("panel1", new Rectangle(0, 0, 0, 0));
        compMinSize.put("panel1", new Dimension(0, 0));
        compBounds.put("panel1", new Rectangle(0, 0, 0, 0));
        compPrefSize.put("panel1", new Dimension(0, 0));
        hasExplicitPrefSize.put("panel1", new Boolean(false));
        hasExplicitPrefSize.put("panel1", new Boolean(false));
        prefPadding.put("choice1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("choice1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 1338, 376));
        prefPadding.put("choice1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("choice1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
    }

    /**
     * Undo previous change. Resize choice1 slightly to the left.
     * In horizontal dimension the textArea1 should also become in sequence with
     * choice1. Vertical layout should not change.
     */
    public void doChanges4() {
        lm.undo(changeMark, lm.getChangeMark());
        changeMark = lm.getChangeMark(); 
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 1338, 376));
        contInterior.put("Form", new Rectangle(0, 0, 1338, 376));
        compBounds.put("panel1", new Rectangle(0, 0, 0, 0));
        baselinePosition.put("panel1-0-0", new Integer(0));
        compBounds.put("canvas1", new Rectangle(0, 0, 0, 0));
        baselinePosition.put("canvas1-0-0", new Integer(0));
        compBounds.put("list1", new Rectangle(53, 26, 40, 200));
        baselinePosition.put("list1-40-200", new Integer(0));
        compBounds.put("checkbox1", new Rectangle(10, 346, 109, 20));
        baselinePosition.put("checkbox1-109-20", new Integer(0));
        compBounds.put("button1", new Rectangle(129, 304, 98, 62));
        baselinePosition.put("button1-98-62", new Integer(0));
        compBounds.put("label1", new Rectangle(169, 26, 38, 20));
        baselinePosition.put("label1-38-20", new Integer(0));
        compBounds.put("scrollbar1", new Rectangle(191, 101, 16, 48));
        baselinePosition.put("scrollbar1-16-48", new Integer(0));
        compBounds.put("scrollbar2", new Rectangle(279, 26, 48, 16));
        baselinePosition.put("scrollbar2-48-16", new Integer(0));
        compBounds.put("choice1", new Rectangle(492, 80, 28, 200));
        baselinePosition.put("choice1-28-200", new Integer(0));
        compBounds.put("textArea1", new Rectangle(129, 200, 245, 80));
        baselinePosition.put("textArea1-245-80", new Integer(0));
        compMinSize.put("Form", new Dimension(1231, 362));
        compBounds.put("Form", new Rectangle(0, 0, 1338, 376));
        contInterior.put("panel1", new Rectangle(0, 0, 0, 0));
        compMinSize.put("panel1", new Dimension(0, 0));
        compBounds.put("panel1", new Rectangle(0, 0, 0, 0));
        prefPadding.put("scrollbar2-textArea1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("scrollbar1-textArea1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("choice1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("choice1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 1338, 376));
        prefPadding.put("choice1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("choice1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        compBounds.put("textArea1", new Rectangle(129, 200, 245, 80));
        compBounds.put("textArea1", new Rectangle(129, 200, 245, 80));
// > START RESIZING
        baselinePosition.put("choice1-28-200", new Integer(0));
        compPrefSize.put("choice1", new Dimension(28, 20));
        {
            String[] compIds = new String[]{
                "choice1"
            };
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(492, 80, 28, 200)
            };
            Point hotspot = new Point(491, 180);
            int[] resizeEdges = new int[]{
                0,
                -1
            };
            boolean inLayout = true;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
// < START RESIZING
        prefPadding.put("scrollbar1-choice1-0-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("scrollbar1-choice1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("scrollbar1-choice1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("scrollbar1-choice1-0-0-3", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
// > MOVE
        {
            Point p = new Point(474, 184);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(475, 80, 45, 200)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
        prefPadding.put("scrollbar1-choice1-0-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("scrollbar1-choice1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("scrollbar1-choice1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("scrollbar1-choice1-0-0-3", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
// > MOVE
        {
            Point p = new Point(474, 185);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(475, 80, 45, 200)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
// > END MOVING
        prefPadding.put("list1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("scrollbar2-choice1-0-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-choice1-0-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-choice1-0-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("scrollbar2-choice1-0-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-panel1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-canvas1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-button1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-choice1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        ld.endMoving(true);
// < END MOVING
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 1338, 376));
        contInterior.put("Form", new Rectangle(0, 0, 1338, 376));
        compBounds.put("panel1", new Rectangle(0, 0, 0, 0));
        baselinePosition.put("panel1-0-0", new Integer(0));
        compBounds.put("canvas1", new Rectangle(0, 0, 0, 0));
        baselinePosition.put("canvas1-0-0", new Integer(0));
        compBounds.put("list1", new Rectangle(53, 26, 40, 200));
        baselinePosition.put("list1-40-200", new Integer(0));
        compBounds.put("checkbox1", new Rectangle(10, 346, 109, 20));
        baselinePosition.put("checkbox1-109-20", new Integer(0));
        compBounds.put("button1", new Rectangle(129, 304, 98, 62));
        baselinePosition.put("button1-98-62", new Integer(0));
        compBounds.put("label1", new Rectangle(169, 26, 38, 20));
        baselinePosition.put("label1-38-20", new Integer(0));
        compBounds.put("scrollbar1", new Rectangle(191, 101, 16, 48));
        baselinePosition.put("scrollbar1-16-48", new Integer(0));
        compBounds.put("scrollbar2", new Rectangle(279, 26, 48, 16));
        baselinePosition.put("scrollbar2-48-16", new Integer(0));
        compBounds.put("choice1", new Rectangle(475, 80, 45, 200));
        baselinePosition.put("choice1-45-200", new Integer(0));
        compBounds.put("textArea1", new Rectangle(129, 200, 245, 80));
        baselinePosition.put("textArea1-245-80", new Integer(0));
        compMinSize.put("Form", new Dimension(1338, 362));
        compBounds.put("Form", new Rectangle(0, 0, 1338, 376));
        contInterior.put("panel1", new Rectangle(0, 0, 0, 0));
        compMinSize.put("panel1", new Dimension(0, 0));
        compBounds.put("panel1", new Rectangle(0, 0, 0, 0));
        compPrefSize.put("panel1", new Dimension(0, 0));
        hasExplicitPrefSize.put("panel1", new Boolean(false));
        hasExplicitPrefSize.put("panel1", new Boolean(false));
        prefPadding.put("scrollbar2-textArea1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("scrollbar1-textArea1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("choice1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("choice1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 1338, 376));
        prefPadding.put("choice1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("choice1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("list1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-checkbox1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textArea1-button1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
    }
}
