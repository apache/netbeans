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
import org.openide.filesystems.FileUtil;

public class ALT_SeqResizing04bTest extends LayoutTestCase {

    public ALT_SeqResizing04bTest(String name) {
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
     * Resize the toggle button slightly to the right (so it does not go under
     * the combobox). It shuld stay in the sequence with the label and combobox
     * even though it visually does not belong there (arranged artificially).
     */
    public void doChanges0() {
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
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
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        compPrefSize.put("jToggleButton2", new Dimension(105, 23));
        compPrefSize.put("jComboBox1", new Dimension(56, 20));
        compPrefSize.put("jCheckBox1", new Dimension(81, 23));
        compPrefSize.put("jCheckBox2", new Dimension(81, 23));
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
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
        // > START RESIZING
        baselinePosition.put("jToggleButton2-105-23", new Integer(15));
        compPrefSize.put("jToggleButton2", new Dimension(105, 23));
        {
            String[] compIds = new String[]{"jToggleButton2"};
            Rectangle[] bounds = new Rectangle[]{new Rectangle(91, 58, 105, 23)};
            Point hotspot = new Point(196, 71);
            int[] resizeEdges = new int[]{1, -1};
            boolean inLayout = true;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
        // < START RESIZING
        prefPaddingInParent.put("Form-jToggleButton2-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        // > MOVE
        // > MOVE
        // > MOVE
        {
            Point p = new Point(235, 79);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(91, 58, 144, 23)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPaddingInParent.put("Form-jToggleButton2-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        // > MOVE
        // > MOVE
        // > MOVE
        {
            Point p = new Point(236, 79);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(91, 58, 145, 23)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        prefPadding.put("jLabel1-jToggleButton2-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton2-jComboBox1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 370, 300));
        compBounds.put("jLabel1", new Rectangle(27, 30, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compBounds.put("jToggleButton2", new Rectangle(91, 58, 145, 23));
        baselinePosition.put("jToggleButton2-145-23", new Integer(15));
        compBounds.put("jComboBox1", new Rectangle(255, 27, 56, 20));
        baselinePosition.put("jComboBox1-56-20", new Integer(14));
        compBounds.put("jCheckBox1", new Rectangle(47, 99, 81, 23));
        baselinePosition.put("jCheckBox1-81-23", new Integer(15));
        compBounds.put("jCheckBox2", new Rectangle(184, 99, 81, 23));
        baselinePosition.put("jCheckBox2-81-23", new Integer(15));
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        compPrefSize.put("jToggleButton2", new Dimension(105, 23));
        compPrefSize.put("jComboBox1", new Dimension(56, 20));
        compPrefSize.put("jCheckBox1", new Dimension(81, 23));
        compPrefSize.put("jCheckBox2", new Dimension(81, 23));
        contInterior.put("Form", new Rectangle(0, 0, 370, 300));
        compBounds.put("jLabel1", new Rectangle(27, 30, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compBounds.put("jToggleButton2", new Rectangle(91, 58, 145, 23));
        baselinePosition.put("jToggleButton2-145-23", new Integer(15));
        compBounds.put("jComboBox1", new Rectangle(255, 27, 56, 20));
        baselinePosition.put("jComboBox1-56-20", new Integer(14));
        compBounds.put("jCheckBox1", new Rectangle(47, 99, 81, 23));
        baselinePosition.put("jCheckBox1-81-23", new Integer(15));
        compBounds.put("jCheckBox2", new Rectangle(184, 99, 81, 23));
        baselinePosition.put("jCheckBox2-81-23", new Integer(15));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }

    /**
     * Resize the toggle button slightly to the left so it goes under the label
     * but does not snap anywhere. It should now be placed in parallel with the
     * label, but remain in sequence with the combobox on the other side.
     * (A variant of ALT_SeqResizing04Test, just without snapping.)
     */
    public void doChanges1() {
        // > START RESIZING
        baselinePosition.put("jToggleButton2-145-23", new Integer(15));
        compPrefSize.put("jToggleButton2", new Dimension(105, 23));
        {
            String[] compIds = new String[]{"jToggleButton2"};
            Rectangle[] bounds = new Rectangle[]{new Rectangle(91, 58, 145, 23)};
            Point hotspot = new Point(89, 72);
            int[] resizeEdges = new int[]{0, -1};
            boolean inLayout = true;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
        // < START RESIZING
        prefPaddingInParent.put("Form-jToggleButton2-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jLabel1-jToggleButton2-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        // > MOVE
        // > MOVE
        {
            Point p = new Point(54, 74);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(56, 58, 180, 23)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPaddingInParent.put("Form-jToggleButton2-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jLabel1-jToggleButton2-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        // > MOVE
        // > MOVE
        {
            Point p = new Point(53, 74);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(55, 58, 181, 23)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        prefPaddingInParent.put("Form-jToggleButton2-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        // parentId-compId-dimension-compAlignment
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 370, 300));
        compBounds.put("jLabel1", new Rectangle(27, 30, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compBounds.put("jToggleButton2", new Rectangle(55, 58, 181, 23));
        baselinePosition.put("jToggleButton2-181-23", new Integer(15));
        compBounds.put("jComboBox1", new Rectangle(255, 27, 56, 20));
        baselinePosition.put("jComboBox1-56-20", new Integer(14));
        compBounds.put("jCheckBox1", new Rectangle(47, 99, 81, 23));
        baselinePosition.put("jCheckBox1-81-23", new Integer(15));
        compBounds.put("jCheckBox2", new Rectangle(184, 99, 81, 23));
        baselinePosition.put("jCheckBox2-81-23", new Integer(15));
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        compPrefSize.put("jToggleButton2", new Dimension(105, 23));
        compPrefSize.put("jComboBox1", new Dimension(56, 20));
        compPrefSize.put("jCheckBox1", new Dimension(81, 23));
        compPrefSize.put("jCheckBox2", new Dimension(81, 23));
        contInterior.put("Form", new Rectangle(0, 0, 370, 300));
        compBounds.put("jLabel1", new Rectangle(27, 30, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compBounds.put("jToggleButton2", new Rectangle(55, 58, 181, 23));
        baselinePosition.put("jToggleButton2-181-23", new Integer(15));
        compBounds.put("jComboBox1", new Rectangle(255, 27, 56, 20));
        baselinePosition.put("jComboBox1-56-20", new Integer(14));
        compBounds.put("jCheckBox1", new Rectangle(47, 99, 81, 23));
        baselinePosition.put("jCheckBox1-81-23", new Integer(15));
        compBounds.put("jCheckBox2", new Rectangle(184, 99, 81, 23));
        baselinePosition.put("jCheckBox2-81-23", new Integer(15));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }

}
