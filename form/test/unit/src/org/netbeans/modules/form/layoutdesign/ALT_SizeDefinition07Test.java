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

/**
 * Tests setting the supporting size of the GUI form to the right component.
 */
public class ALT_SizeDefinition07Test extends LayoutTestCase {

    public ALT_SizeDefinition07Test(String name) {
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
     * Invoke Set to Default Size on the form. This also resets all resizing
     * intervals to be flexibly assignable with default or explicit size in
     * order to define the form size.
     */
    public void doChanges0() {
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 400, 300));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jTextField1", new Rectangle(0, 0, 400, 20));
        baselinePosition.put("jTextField1-400-20", new Integer(14));
        compBounds.put("jCheckBox1", new Rectangle(10, 22, 81, 23));
        baselinePosition.put("jCheckBox1-81-23", new Integer(15));
        compBounds.put("jCheckBox2", new Rectangle(10, 45, 81, 23));
        baselinePosition.put("jCheckBox2-81-23", new Integer(15));
        compBounds.put("jScrollPane1", new Rectangle(10, 80, 35, 86));
        baselinePosition.put("jScrollPane1-35-86", new Integer(0));
        compBounds.put("jToggleButton1", new Rectangle(78, 143, 271, 23));
        baselinePosition.put("jToggleButton1-271-23", new Integer(15));
        compBounds.put("jButton1", new Rectangle(78, 114, 271, 23));
        baselinePosition.put("jButton1-271-23", new Integer(15));
        compBounds.put("jTextField2", new Rectangle(78, 83, 271, 20));
        baselinePosition.put("jTextField2-271-20", new Integer(14));
        compBounds.put("jScrollPane2", new Rectangle(355, 116, 35, 50));
        baselinePosition.put("jScrollPane2-35-50", new Integer(0));
        compMinSize.put("Form", new Dimension(234, 177));
        compBounds.put("Form", new Rectangle(0, 0, 400, 300));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compPrefSize.put("jToggleButton1", new Dimension(105, 23));
        compPrefSize.put("jButton1", new Dimension(73, 23));
        compPrefSize.put("jTextField2", new Dimension(59, 20));
        prefPaddingInParent.put("Form-jScrollPane2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollPane1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jToggleButton1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
// > SET DEFAULT SIZE
        ld.setDefaultSize("Form");
// < SET DEFAULT SIZE
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 234, 177));
        contInterior.put("Form", new Rectangle(0, 0, 234, 177));
        compBounds.put("jTextField1", new Rectangle(0, 0, 234, 20));
        baselinePosition.put("jTextField1-234-20", new Integer(14));
        compBounds.put("jCheckBox1", new Rectangle(10, 22, 81, 23));
        baselinePosition.put("jCheckBox1-81-23", new Integer(15));
        compBounds.put("jCheckBox2", new Rectangle(10, 45, 81, 23));
        baselinePosition.put("jCheckBox2-81-23", new Integer(15));
        compBounds.put("jScrollPane1", new Rectangle(10, 80, 35, 86));
        baselinePosition.put("jScrollPane1-35-86", new Integer(0));
        compBounds.put("jToggleButton1", new Rectangle(78, 143, 105, 23));
        baselinePosition.put("jToggleButton1-105-23", new Integer(15));
        compBounds.put("jButton1", new Rectangle(78, 114, 105, 23));
        baselinePosition.put("jButton1-105-23", new Integer(15));
        compBounds.put("jTextField2", new Rectangle(78, 83, 105, 20));
        baselinePosition.put("jTextField2-105-20", new Integer(14));
        compBounds.put("jScrollPane2", new Rectangle(189, 116, 35, 50));
        baselinePosition.put("jScrollPane2-35-50", new Integer(0));
        compMinSize.put("Form", new Dimension(234, 177));
        compBounds.put("Form", new Rectangle(0, 0, 234, 177));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compPrefSize.put("jToggleButton1", new Dimension(105, 23));
        compPrefSize.put("jButton1", new Dimension(73, 23));
        compPrefSize.put("jTextField2", new Dimension(59, 20));
        prefPaddingInParent.put("Form-jScrollPane2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollPane1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jToggleButton1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
    }

    /**
     * Resize the entire form horizontally, make it bigger than the initial
     * state when opened. The supporting size should be set to jTextField2.
     */
    public void doChanges1() {
// > START RESIZING
        baselinePosition.put("Form-234-177", new Integer(0));
        compMinSize.put("Form", new Dimension(234, 177));
        prefPaddingInParent.put("Form-jScrollPane2-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        {
            String[] compIds = new String[]{
                "Form"
            };
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(0, 0, 234, 177)
            };
            Point hotspot = new Point(237, 81);
            int[] resizeEdges = new int[]{
                1,
                -1
            };
            boolean inLayout = false;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
// < START RESIZING
// > MOVE
        {
            Point p = new Point(461, 95);
            String containerId = null;
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(0, 0, 458, 177)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
// > MOVE
        {
            Point p = new Point(462, 95);
            String containerId = null;
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(0, 0, 459, 177)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
// > END MOVING
        contInterior.put("Form", new Rectangle(0, 0, 459, 177));
        compBounds.put("jTextField1", new Rectangle(0, 0, 459, 20));
        baselinePosition.put("jTextField1-459-20", new Integer(14));
        compBounds.put("jCheckBox1", new Rectangle(10, 22, 81, 23));
        baselinePosition.put("jCheckBox1-81-23", new Integer(15));
        compBounds.put("jCheckBox2", new Rectangle(10, 45, 81, 23));
        baselinePosition.put("jCheckBox2-81-23", new Integer(15));
        compBounds.put("jScrollPane1", new Rectangle(10, 80, 35, 86));
        baselinePosition.put("jScrollPane1-35-86", new Integer(0));
        compBounds.put("jToggleButton1", new Rectangle(78, 143, 330, 23));
        baselinePosition.put("jToggleButton1-330-23", new Integer(15));
        compBounds.put("jButton1", new Rectangle(78, 114, 330, 23));
        baselinePosition.put("jButton1-330-23", new Integer(15));
        compBounds.put("jTextField2", new Rectangle(78, 83, 330, 20));
        baselinePosition.put("jTextField2-330-20", new Integer(14));
        compBounds.put("jScrollPane2", new Rectangle(414, 116, 35, 50));
        baselinePosition.put("jScrollPane2-35-50", new Integer(0));
        ld.endMoving(true);
// < END MOVING
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 459, 177));
        contInterior.put("Form", new Rectangle(0, 0, 459, 177));
        compBounds.put("jTextField1", new Rectangle(0, 0, 459, 20));
        baselinePosition.put("jTextField1-459-20", new Integer(14));
        compBounds.put("jCheckBox1", new Rectangle(10, 22, 81, 23));
        baselinePosition.put("jCheckBox1-81-23", new Integer(15));
        compBounds.put("jCheckBox2", new Rectangle(10, 45, 81, 23));
        baselinePosition.put("jCheckBox2-81-23", new Integer(15));
        compBounds.put("jScrollPane1", new Rectangle(10, 80, 35, 86));
        baselinePosition.put("jScrollPane1-35-86", new Integer(0));
        compBounds.put("jToggleButton1", new Rectangle(78, 143, 330, 23));
        baselinePosition.put("jToggleButton1-330-23", new Integer(15));
        compBounds.put("jButton1", new Rectangle(78, 114, 330, 23));
        baselinePosition.put("jButton1-330-23", new Integer(15));
        compBounds.put("jTextField2", new Rectangle(78, 83, 330, 20));
        baselinePosition.put("jTextField2-330-20", new Integer(14));
        compBounds.put("jScrollPane2", new Rectangle(414, 116, 35, 50));
        baselinePosition.put("jScrollPane2-35-50", new Integer(0));
        compMinSize.put("Form", new Dimension(234, 177));
        compBounds.put("Form", new Rectangle(0, 0, 459, 177));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compPrefSize.put("jToggleButton1", new Dimension(105, 23));
        compPrefSize.put("jButton1", new Dimension(73, 23));
        compPrefSize.put("jTextField2", new Dimension(59, 20));
        prefPaddingInParent.put("Form-jScrollPane2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollPane1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jToggleButton1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        compBounds.put("Form", new Rectangle(0, 0, 459, 177));
        contInterior.put("Form", new Rectangle(0, 0, 459, 177));
        compBounds.put("jTextField1", new Rectangle(0, 0, 459, 20));
        baselinePosition.put("jTextField1-459-20", new Integer(14));
        compBounds.put("jCheckBox1", new Rectangle(10, 22, 81, 23));
        baselinePosition.put("jCheckBox1-81-23", new Integer(15));
        compBounds.put("jCheckBox2", new Rectangle(10, 45, 81, 23));
        baselinePosition.put("jCheckBox2-81-23", new Integer(15));
        compBounds.put("jScrollPane1", new Rectangle(10, 80, 35, 86));
        baselinePosition.put("jScrollPane1-35-86", new Integer(0));
        compBounds.put("jToggleButton1", new Rectangle(78, 143, 330, 23));
        baselinePosition.put("jToggleButton1-330-23", new Integer(15));
        compBounds.put("jButton1", new Rectangle(78, 114, 330, 23));
        baselinePosition.put("jButton1-330-23", new Integer(15));
        compBounds.put("jTextField2", new Rectangle(78, 83, 330, 20));
        baselinePosition.put("jTextField2-330-20", new Integer(14));
        compBounds.put("jScrollPane2", new Rectangle(414, 116, 35, 50));
        baselinePosition.put("jScrollPane2-35-50", new Integer(0));
        compMinSize.put("Form", new Dimension(234, 177));
        compBounds.put("Form", new Rectangle(0, 0, 459, 177));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compPrefSize.put("jToggleButton1", new Dimension(105, 23));
        compPrefSize.put("jButton1", new Dimension(73, 23));
        compPrefSize.put("jTextField2", new Dimension(59, 20));
        prefPaddingInParent.put("Form-jScrollPane2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollPane1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jToggleButton1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
    }

    /**
     * Set size of jtextField1 explicitly to 400 (i.e. smaller than current
     * size). The form should shrink, default size should be set to jTextField2
     * which defined the size before.
     */
    public void doChanges2() {
        LayoutInterval compInt = lm.getLayoutComponent("jTextField1").getLayoutInterval(0);
        lm.setUserIntervalSize(compInt, 0, 400);
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 400, 177));
        contInterior.put("Form", new Rectangle(0, 0, 400, 177));
        compBounds.put("jTextField1", new Rectangle(0, 0, 400, 20));
        baselinePosition.put("jTextField1-400-20", new Integer(14));
        compBounds.put("jCheckBox1", new Rectangle(10, 22, 81, 23));
        baselinePosition.put("jCheckBox1-81-23", new Integer(15));
        compBounds.put("jCheckBox2", new Rectangle(10, 45, 81, 23));
        baselinePosition.put("jCheckBox2-81-23", new Integer(15));
        compBounds.put("jScrollPane1", new Rectangle(10, 80, 35, 86));
        baselinePosition.put("jScrollPane1-35-86", new Integer(0));
        compBounds.put("jToggleButton1", new Rectangle(78, 143, 271, 23));
        baselinePosition.put("jToggleButton1-271-23", new Integer(15));
        compBounds.put("jButton1", new Rectangle(78, 114, 271, 23));
        baselinePosition.put("jButton1-271-23", new Integer(15));
        compBounds.put("jTextField2", new Rectangle(78, 83, 271, 20));
        baselinePosition.put("jTextField2-271-20", new Integer(14));
        compBounds.put("jScrollPane2", new Rectangle(355, 116, 35, 50));
        baselinePosition.put("jScrollPane2-35-50", new Integer(0));
        compMinSize.put("Form", new Dimension(142, 177));
        compBounds.put("Form", new Rectangle(0, 0, 400, 177));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compPrefSize.put("jToggleButton1", new Dimension(105, 23));
        compPrefSize.put("jButton1", new Dimension(73, 23));
        compPrefSize.put("jTextField2", new Dimension(59, 20));
        prefPaddingInParent.put("Form-jScrollPane2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollPane1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jToggleButton1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        compBounds.put("Form", new Rectangle(0, 0, 400, 177));
        contInterior.put("Form", new Rectangle(0, 0, 400, 177));
        compBounds.put("jTextField1", new Rectangle(0, 0, 400, 20));
        baselinePosition.put("jTextField1-400-20", new Integer(14));
        compBounds.put("jCheckBox1", new Rectangle(10, 22, 81, 23));
        baselinePosition.put("jCheckBox1-81-23", new Integer(15));
        compBounds.put("jCheckBox2", new Rectangle(10, 45, 81, 23));
        baselinePosition.put("jCheckBox2-81-23", new Integer(15));
        compBounds.put("jScrollPane1", new Rectangle(10, 80, 35, 86));
        baselinePosition.put("jScrollPane1-35-86", new Integer(0));
        compBounds.put("jToggleButton1", new Rectangle(78, 143, 271, 23));
        baselinePosition.put("jToggleButton1-271-23", new Integer(15));
        compBounds.put("jButton1", new Rectangle(78, 114, 271, 23));
        baselinePosition.put("jButton1-271-23", new Integer(15));
        compBounds.put("jTextField2", new Rectangle(78, 83, 271, 20));
        baselinePosition.put("jTextField2-271-20", new Integer(14));
        compBounds.put("jScrollPane2", new Rectangle(355, 116, 35, 50));
        baselinePosition.put("jScrollPane2-35-50", new Integer(0));
        compMinSize.put("Form", new Dimension(234, 177));
        compBounds.put("Form", new Rectangle(0, 0, 400, 177));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compPrefSize.put("jToggleButton1", new Dimension(105, 23));
        compPrefSize.put("jButton1", new Dimension(73, 23));
        compPrefSize.put("jTextField2", new Dimension(59, 20));
        prefPaddingInParent.put("Form-jScrollPane2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollPane1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jToggleButton1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
    }
}
