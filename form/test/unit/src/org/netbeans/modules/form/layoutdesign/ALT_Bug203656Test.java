/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of the
 * License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include the
 * License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by Oracle
 * in the GPL Version 2 section of the License file that accompanied this code.
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL or only
 * the GPL Version 2, indicate your decision by adding "[Contributor] elects to
 * include this software in this distribution under the [CDDL or GPL Version 2]
 * license." If you do not indicate a single choice of license, a recipient has
 * the option to distribute your version of this file under either the CDDL, the
 * GPL Version 2 or to extend the choice of license to its licensees as provided
 * above. However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is made
 * subject to such option by the copyright holder.
 */
package org.netbeans.modules.form.layoutdesign;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.*;
import org.openide.filesystems.FileUtil;

public class ALT_Bug203656Test extends LayoutTestCase {

    public ALT_Bug203656Test(String name) {
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
     * Resize jButton3 vertically to top align with the other buttons.
     */
    public void doChanges0() {
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 921, 777));
        contInterior.put("Form", new Rectangle(0, 0, 921, 777));
        compBounds.put("jPanel2", new Rectangle(10, 11, 901, 755));
        baselinePosition.put("jPanel2-901-755", new Integer(0));
        contInterior.put("jPanel2", new Rectangle(10, 11, 901, 755));
        compBounds.put("jScrollPane1", new Rectangle(20, 22, 166, 407));
        baselinePosition.put("jScrollPane1-166-407", new Integer(0));
        compBounds.put("jRadioButton3", new Rectangle(192, 736, 93, 23));
        baselinePosition.put("jRadioButton3-93-23", new Integer(15));
        compBounds.put("jRadioButton2", new Rectangle(192, 713, 93, 23));
        baselinePosition.put("jRadioButton2-93-23", new Integer(15));
        compBounds.put("jRadioButton1", new Rectangle(192, 690, 93, 23));
        baselinePosition.put("jRadioButton1-93-23", new Integer(15));
        compBounds.put("jComboBox2", new Rectangle(192, 42, 56, 20));
        baselinePosition.put("jComboBox2-56-20", new Integer(14));
        compBounds.put("jButton4", new Rectangle(192, 80, 73, 579));
        baselinePosition.put("jButton4-73-579", new Integer(293));
        compBounds.put("filler1", new Rectangle(295, 80, 0, 25));
        baselinePosition.put("filler1-0-25", new Integer(-1));
        compBounds.put("filler2", new Rectangle(313, 80, 0, 25));
        baselinePosition.put("filler2-0-25", new Integer(-1));
        compBounds.put("jButton2", new Rectangle(295, 80, 73, 578));
        baselinePosition.put("jButton2-73-578", new Integer(293));
        compBounds.put("jButton3", new Rectangle(397, 218, 73, 440));
        baselinePosition.put("jButton3-73-440", new Integer(224));
        compBounds.put("jSlider1", new Rectangle(192, 665, 200, 23));
        baselinePosition.put("jSlider1-200-23", new Integer(0));
        compMinSize.put("jPanel2", new Dimension(901, 755));
        compBounds.put("jPanel2", new Rectangle(10, 11, 901, 755));
        compPrefSize.put("jPanel2", new Dimension(901, 755));
        prefPaddingInParent.put("jPanel2-jScrollPane1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel2-jScrollPane1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jScrollPane1-jRadioButton3-0-0-0", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jRadioButton3-0-0-1", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jRadioButton3-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jRadioButton3-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jRadioButton2-0-0-0", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jRadioButton2-0-0-1", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jRadioButton2-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jRadioButton2-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jRadioButton1-0-0-0", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jRadioButton1-0-0-1", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jRadioButton1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jRadioButton1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jSlider1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jSlider1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jSlider1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jSlider1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jComboBox2-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jComboBox2-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jComboBox2-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jComboBox2-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jButton4-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jButton4-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jButton4-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jButton4-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        compPrefSize.put("jRadioButton3", new Dimension(93, 23));
        compPrefSize.put("jRadioButton2", new Dimension(93, 23));
        compPrefSize.put("jRadioButton1", new Dimension(93, 23));
        compPrefSize.put("jComboBox2", new Dimension(56, 20));
        compPrefSize.put("jButton4", new Dimension(73, 23));
        compPrefSize.put("filler1", new Dimension(0, 0));
        prefPadding.put("filler1-filler2-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        compPrefSize.put("filler2", new Dimension(0, 0));
        compPrefSize.put("jButton2", new Dimension(73, 23));
        compPrefSize.put("jButton3", new Dimension(73, 23));
        compPrefSize.put("jSlider1", new Dimension(200, 23));
        hasExplicitPrefSize.put("jPanel2", new Boolean(false));
        compPrefSize.put("jButton4", new Dimension(73, 23));
        prefPadding.put("filler2-jButton3-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("filler2-jButton3-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("filler2-jButton3-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("filler2-jButton3-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        hasExplicitPrefSize.put("jPanel2", new Boolean(false));
        compMinSize.put("Form", new Dimension(921, 777));
        compBounds.put("Form", new Rectangle(0, 0, 921, 777));
        compPrefSize.put("jPanel2", new Dimension(901, 755));
        compPrefSize.put("jPanel2", new Dimension(901, 755));
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
// > START RESIZING
        baselinePosition.put("jButton3-73-440", new Integer(224));
        compPrefSize.put("jButton3", new Dimension(73, 23));
        {
            String[] compIds = new String[]{
                "jButton3"
            };
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(397, 218, 73, 440)
            };
            Point hotspot = new Point(431, 218);
            int[] resizeEdges = new int[]{
                -1,
                0
            };
            boolean inLayout = true;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
// < START RESIZING
        prefPaddingInParent.put("jPanel2-jButton3-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
// > MOVE
        {
            Point p = new Point(428, 83);
            String containerId = "jPanel2";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(397, 80, 73, 578)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
        prefPaddingInParent.put("jPanel2-jButton3-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
// > MOVE
        {
            Point p = new Point(428, 82);
            String containerId = "jPanel2";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(397, 80, 73, 578)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
// > END MOVING
        prefPaddingInParent.put("jPanel2-jScrollPane1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        compPrefSize.put("jButton4", new Dimension(73, 23));
        compMinSize.put("jButton2", new Dimension(73, 23));
        prefPadding.put("jButton2-jButton3-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("filler2-jButton3-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("jPanel2-jButton3-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        ld.endMoving(true);
// < END MOVING
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 921, 777));
        contInterior.put("Form", new Rectangle(0, 0, 921, 777));
        compBounds.put("jPanel2", new Rectangle(10, 11, 901, 755));
        baselinePosition.put("jPanel2-901-755", new Integer(0));
        contInterior.put("jPanel2", new Rectangle(10, 11, 901, 755));
        compBounds.put("jScrollPane1", new Rectangle(20, 22, 166, 407));
        baselinePosition.put("jScrollPane1-166-407", new Integer(0));
        compBounds.put("jRadioButton3", new Rectangle(192, 736, 93, 23));
        baselinePosition.put("jRadioButton3-93-23", new Integer(15));
        compBounds.put("jRadioButton2", new Rectangle(192, 713, 93, 23));
        baselinePosition.put("jRadioButton2-93-23", new Integer(15));
        compBounds.put("jRadioButton1", new Rectangle(192, 690, 93, 23));
        baselinePosition.put("jRadioButton1-93-23", new Integer(15));
        compBounds.put("jComboBox2", new Rectangle(192, 42, 56, 20));
        baselinePosition.put("jComboBox2-56-20", new Integer(14));
        compBounds.put("jButton4", new Rectangle(192, 80, 73, 579));
        baselinePosition.put("jButton4-73-579", new Integer(293));
        compBounds.put("filler1", new Rectangle(295, 80, 0, 25));
        baselinePosition.put("filler1-0-25", new Integer(-1));
        compBounds.put("filler2", new Rectangle(313, 80, 0, 25));
        baselinePosition.put("filler2-0-25", new Integer(-1));
        compBounds.put("jButton2", new Rectangle(295, 80, 73, 578));
        baselinePosition.put("jButton2-73-578", new Integer(293));
        compBounds.put("jButton3", new Rectangle(397, 80, 73, 578));
        baselinePosition.put("jButton3-73-578", new Integer(293));
        compBounds.put("jSlider1", new Rectangle(192, 665, 200, 23));
        baselinePosition.put("jSlider1-200-23", new Integer(0));
        compMinSize.put("jPanel2", new Dimension(901, 755));
        compBounds.put("jPanel2", new Rectangle(10, 11, 901, 755));
        compPrefSize.put("jPanel2", new Dimension(901, 755));
        prefPaddingInParent.put("jPanel2-jScrollPane1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel2-jScrollPane1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jScrollPane1-jRadioButton3-0-0-0", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jRadioButton3-0-0-1", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jRadioButton3-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jRadioButton3-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jRadioButton2-0-0-0", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jRadioButton2-0-0-1", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jRadioButton2-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jRadioButton2-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jRadioButton1-0-0-0", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jRadioButton1-0-0-1", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jRadioButton1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jRadioButton1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jSlider1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jSlider1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jSlider1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jSlider1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jComboBox2-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jComboBox2-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jComboBox2-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jComboBox2-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jButton4-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jButton4-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jButton4-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jButton4-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        compPrefSize.put("jRadioButton3", new Dimension(93, 23));
        compPrefSize.put("jRadioButton2", new Dimension(93, 23));
        compPrefSize.put("jRadioButton1", new Dimension(93, 23));
        compPrefSize.put("jSlider1", new Dimension(200, 23));
        compPrefSize.put("jComboBox2", new Dimension(56, 20));
        compPrefSize.put("jButton4", new Dimension(73, 23));
        compPrefSize.put("filler1", new Dimension(0, 0));
        prefPadding.put("filler1-filler2-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        compPrefSize.put("filler2", new Dimension(0, 0));
        compPrefSize.put("jButton2", new Dimension(73, 23));
        compPrefSize.put("jButton3", new Dimension(73, 23));
        hasExplicitPrefSize.put("jPanel2", new Boolean(false));
        compPrefSize.put("jButton4", new Dimension(73, 23));
        compPrefSize.put("jButton2", new Dimension(73, 23));
        compPrefSize.put("jButton3", new Dimension(73, 23));
        hasExplicitPrefSize.put("jPanel2", new Boolean(false));
        compMinSize.put("Form", new Dimension(921, 777));
        compBounds.put("Form", new Rectangle(0, 0, 921, 777));
        compPrefSize.put("jPanel2", new Dimension(901, 755));
        compPrefSize.put("jPanel2", new Dimension(901, 755));
        compBounds.put("Form", new Rectangle(0, 0, 921, 777));
        contInterior.put("Form", new Rectangle(0, 0, 921, 777));
        compBounds.put("jPanel2", new Rectangle(10, 11, 901, 755));
        baselinePosition.put("jPanel2-901-755", new Integer(0));
        contInterior.put("jPanel2", new Rectangle(10, 11, 901, 755));
        compBounds.put("jScrollPane1", new Rectangle(20, 22, 166, 407));
        baselinePosition.put("jScrollPane1-166-407", new Integer(0));
        compBounds.put("jRadioButton3", new Rectangle(192, 736, 93, 23));
        baselinePosition.put("jRadioButton3-93-23", new Integer(15));
        compBounds.put("jRadioButton2", new Rectangle(192, 713, 93, 23));
        baselinePosition.put("jRadioButton2-93-23", new Integer(15));
        compBounds.put("jRadioButton1", new Rectangle(192, 690, 93, 23));
        baselinePosition.put("jRadioButton1-93-23", new Integer(15));
        compBounds.put("jComboBox2", new Rectangle(192, 42, 56, 20));
        baselinePosition.put("jComboBox2-56-20", new Integer(14));
        compBounds.put("jButton4", new Rectangle(192, 80, 73, 579));
        baselinePosition.put("jButton4-73-579", new Integer(293));
        compBounds.put("filler1", new Rectangle(295, 80, 0, 25));
        baselinePosition.put("filler1-0-25", new Integer(-1));
        compBounds.put("filler2", new Rectangle(313, 80, 0, 25));
        baselinePosition.put("filler2-0-25", new Integer(-1));
        compBounds.put("jButton2", new Rectangle(295, 80, 73, 578));
        baselinePosition.put("jButton2-73-578", new Integer(293));
        compBounds.put("jButton3", new Rectangle(397, 80, 73, 578));
        baselinePosition.put("jButton3-73-578", new Integer(293));
        compBounds.put("jSlider1", new Rectangle(192, 665, 200, 23));
        baselinePosition.put("jSlider1-200-23", new Integer(0));
        compMinSize.put("jPanel2", new Dimension(901, 755));
        compBounds.put("jPanel2", new Rectangle(10, 11, 901, 755));
        prefPaddingInParent.put("jPanel2-jScrollPane1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        compPrefSize.put("jButton4", new Dimension(73, 23));
        compPrefSize.put("jButton2", new Dimension(73, 23));
        compPrefSize.put("jButton3", new Dimension(73, 23));
        compMinSize.put("Form", new Dimension(921, 777));
        compBounds.put("Form", new Rectangle(0, 0, 921, 777));
        compPrefSize.put("jPanel2", new Dimension(901, 755));
        compPrefSize.put("jPanel2", new Dimension(901, 755));
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
    }
}
