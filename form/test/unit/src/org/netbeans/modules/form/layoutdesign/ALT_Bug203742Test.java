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

public class ALT_Bug203742Test extends LayoutTestCase {

    public ALT_Bug203742Test(String name) {
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
     * Add a new JLabel below jLabel1 (at default small distance) with indent.
     */
    public void doChanges0() {
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 419, 451));
        contInterior.put("Form", new Rectangle(0, 0, 419, 451));
        compBounds.put("panel1", new Rectangle(10, 10, 399, 431));
        baselinePosition.put("panel1-399-431", new Integer(0));
        contInterior.put("panel1", new Rectangle(10, 10, 399, 431));
        compBounds.put("button1", new Rectangle(49, 171, 57, 24));
        baselinePosition.put("button1-57-24", new Integer(0));
        compBounds.put("textField1", new Rectangle(116, 171, 60, 20));
        baselinePosition.put("textField1-60-20", new Integer(0));
        compBounds.put("jLabel1", new Rectangle(220, 171, 41, 16));
        baselinePosition.put("jLabel1-41-16", new Integer(13));
        compBounds.put("label1", new Rectangle(49, 319, 38, 20));
        baselinePosition.put("label1-38-20", new Integer(0));
        compBounds.put("button2", new Rectangle(59, 205, 57, 24));
        baselinePosition.put("button2-57-24", new Integer(0));
        compBounds.put("textField2", new Rectangle(126, 205, 60, 20));
        baselinePosition.put("textField2-60-20", new Integer(0));
        compBounds.put("label2", new Rectangle(59, 349, 38, 20));
        baselinePosition.put("label2-38-20", new Integer(0));
        compBounds.put("button3", new Rectangle(69, 239, 57, 24));
        baselinePosition.put("button3-57-24", new Integer(0));
        compBounds.put("textField3", new Rectangle(136, 239, 60, 20));
        baselinePosition.put("textField3-60-20", new Integer(0));
        compBounds.put("label3", new Rectangle(69, 379, 38, 20));
        baselinePosition.put("label3-38-20", new Integer(0));
        compBounds.put("jButton1", new Rectangle(196, 314, 79, 25));
        baselinePosition.put("jButton1-79-25", new Integer(17));
        compBounds.put("jButton3", new Rectangle(216, 381, 79, 25));
        baselinePosition.put("jButton3-79-25", new Integer(17));
        compBounds.put("jButton2", new Rectangle(206, 349, 79, 25));
        baselinePosition.put("jButton2-79-25", new Integer(17));
        compMinSize.put("panel1", new Dimension(297, 283));
        compBounds.put("panel1", new Rectangle(10, 10, 399, 431));
        compPrefSize.put("panel1", new Dimension(399, 431));
        prefPaddingInParent.put("panel1-jLabel1-0-1", new Integer(12)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("panel1-jButton1-0-1", new Integer(12)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("panel1-jButton2-0-1", new Integer(12)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("panel1-jButton3-0-1", new Integer(12)); // parentId-compId-dimension-compAlignment
        hasExplicitPrefSize.put("panel1", new Boolean(false));
        prefPaddingInParent.put("panel1-jLabel1-1-0", new Integer(13)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("panel1-button1-1-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("panel1-textField1-1-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jButton2-jButton3-1-0-0", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jButton3-1-0-1", new Integer(13)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jButton3-1-0-2", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jButton3-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        hasExplicitPrefSize.put("panel1", new Boolean(false));
        compMinSize.put("Form", new Dimension(419, 451));
        compBounds.put("Form", new Rectangle(0, 0, 419, 451));
        prefPaddingInParent.put("Form-panel1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-panel1-1-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
        lc = new LayoutComponent("jLabel2", false);
// > START ADDING
        baselinePosition.put("jLabel2-41-16", new Integer(13));
        {
            LayoutComponent[] comps = new LayoutComponent[]{lc};
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(0, 0, 41, 16)
            };
            String defaultContId = null;
            Point hotspot = new Point(16, 8);
            ld.startAdding(comps, bounds, hotspot, defaultContId);
        }
// < START ADDING
        prefPaddingInParent.put("panel1-jLabel2-1-0", new Integer(13)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("panel1-jLabel2-1-1", new Integer(13)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jLabel1-jLabel2-1-0-0", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jLabel2-1-0-1", new Integer(13)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jLabel2-1-0-2", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jLabel2-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel1-1-0-0", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel1-1-0-1", new Integer(13)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel1-1-0-2", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jButton1-1-0-0", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jButton1-1-0-1", new Integer(13)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jButton1-1-0-2", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jButton1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jButton2-1-0-0", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jButton3-1-0-0", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jButton3-1-0-1", new Integer(13)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jButton3-1-0-2", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jButton3-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("panel1-jLabel2-0-0", new Integer(12)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("panel1-jLabel2-0-1", new Integer(12)); // parentId-compId-dimension-compAlignment
        prefPadding.put("button1-jLabel2-0-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-button1-0-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-button1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-button1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-button1-0-0-3", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("button2-jLabel2-0-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textField2-jLabel2-0-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textField2-jLabel2-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textField2-jLabel2-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textField2-jLabel2-0-0-3", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textField1-jLabel2-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jLabel2-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
// > MOVE
        {
            Point p = new Point(242, 198);
            String containerId = "panel1";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(230, 194, 41, 16)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
        prefPaddingInParent.put("panel1-jLabel2-1-0", new Integer(13)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("panel1-jLabel2-1-1", new Integer(13)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jLabel1-jLabel2-1-0-0", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jLabel2-1-0-1", new Integer(13)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jLabel2-1-0-2", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jLabel2-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel1-1-0-0", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel1-1-0-1", new Integer(13)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel1-1-0-2", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jButton1-1-0-0", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jButton1-1-0-1", new Integer(13)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jButton1-1-0-2", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jButton1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jButton2-1-0-0", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jButton3-1-0-0", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jButton3-1-0-1", new Integer(13)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jButton3-1-0-2", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jButton3-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("panel1-jLabel2-0-0", new Integer(12)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("panel1-jLabel2-0-1", new Integer(12)); // parentId-compId-dimension-compAlignment
        prefPadding.put("button1-jLabel2-0-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-button1-0-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-button1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-button1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-button1-0-0-3", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("button2-jLabel2-0-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textField2-jLabel2-0-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textField2-jLabel2-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textField2-jLabel2-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textField2-jLabel2-0-0-3", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("textField1-jLabel2-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jLabel2-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
// > MOVE
        {
            Point p = new Point(243, 198);
            String containerId = "panel1";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(230, 194, 41, 16)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
// > END MOVING
        compPrefSize.put("jLabel2", new Dimension(41, 16));
        ld.endMoving(true);
// < END MOVING
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 419, 451));
        contInterior.put("Form", new Rectangle(0, 0, 419, 451));
        compBounds.put("panel1", new Rectangle(10, 10, 399, 431));
        baselinePosition.put("panel1-399-431", new Integer(0));
        contInterior.put("panel1", new Rectangle(10, 10, 399, 431));
        compBounds.put("button1", new Rectangle(49, 171, 57, 24));
        baselinePosition.put("button1-57-24", new Integer(0));
        compBounds.put("textField1", new Rectangle(116, 171, 60, 20));
        baselinePosition.put("textField1-60-20", new Integer(0));
        compBounds.put("jLabel1", new Rectangle(220, 171, 41, 16));
        baselinePosition.put("jLabel1-41-16", new Integer(13));
        compBounds.put("label1", new Rectangle(49, 319, 38, 20));
        baselinePosition.put("label1-38-20", new Integer(0));
        compBounds.put("button2", new Rectangle(59, 205, 57, 24));
        baselinePosition.put("button2-57-24", new Integer(0));
        compBounds.put("textField2", new Rectangle(126, 205, 60, 20));
        baselinePosition.put("textField2-60-20", new Integer(0));
        compBounds.put("label2", new Rectangle(59, 349, 38, 20));
        baselinePosition.put("label2-38-20", new Integer(0));
        compBounds.put("button3", new Rectangle(69, 239, 57, 24));
        baselinePosition.put("button3-57-24", new Integer(0));
        compBounds.put("textField3", new Rectangle(136, 239, 60, 20));
        baselinePosition.put("textField3-60-20", new Integer(0));
        compBounds.put("label3", new Rectangle(69, 379, 38, 20));
        baselinePosition.put("label3-38-20", new Integer(0));
        compBounds.put("jButton1", new Rectangle(196, 314, 79, 25));
        baselinePosition.put("jButton1-79-25", new Integer(17));
        compBounds.put("jButton3", new Rectangle(216, 381, 79, 25));
        baselinePosition.put("jButton3-79-25", new Integer(17));
        compBounds.put("jButton2", new Rectangle(206, 349, 79, 25));
        baselinePosition.put("jButton2-79-25", new Integer(17));
        compBounds.put("jLabel2", new Rectangle(230, 194, 41, 16));
        baselinePosition.put("jLabel2-41-16", new Integer(13));
        compMinSize.put("panel1", new Dimension(297, 283));
        compBounds.put("panel1", new Rectangle(10, 10, 399, 431));
        compPrefSize.put("panel1", new Dimension(399, 431));
        prefPaddingInParent.put("panel1-jButton1-0-1", new Integer(12)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("panel1-jButton2-0-1", new Integer(12)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("panel1-jLabel1-0-1", new Integer(12)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("panel1-jButton3-0-1", new Integer(12)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("panel1-jLabel2-0-1", new Integer(12)); // parentId-compId-dimension-compAlignment
        hasExplicitPrefSize.put("panel1", new Boolean(false));
        prefPaddingInParent.put("panel1-jLabel1-1-0", new Integer(13)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("panel1-button1-1-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("panel1-textField1-1-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jButton2-jButton3-1-0-0", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jButton3-1-0-1", new Integer(13)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jButton3-1-0-2", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jButton3-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        hasExplicitPrefSize.put("panel1", new Boolean(false));
        compMinSize.put("Form", new Dimension(419, 451));
        compBounds.put("Form", new Rectangle(0, 0, 419, 451));
        prefPaddingInParent.put("Form-panel1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-panel1-1-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
    }
}
