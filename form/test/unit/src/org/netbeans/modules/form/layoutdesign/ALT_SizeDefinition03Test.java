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

public class ALT_SizeDefinition03Test extends LayoutTestCase {

    public ALT_SizeDefinition03Test(String name) {
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
     * Set size of jTextField2 to 87. This shrinks the form and brings jButton1
     * to default size. As a result all resizing components should have the
     * preferred size set to default.
     */
    public void doChanges0() {
        LayoutInterval li = lm.getLayoutComponent("jTextField2").getLayoutInterval(0);
        lm.setUserIntervalSize(li, 0, 87);
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 219, 300));
        compBounds.put("jLabel1", new Rectangle(10, 14, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compBounds.put("jTextField1", new Rectangle(48, 11, 161, 20));
        baselinePosition.put("jTextField1-161-20", new Integer(14));
        compBounds.put("jToggleButton1", new Rectangle(10, 42, 105, 23));
        baselinePosition.put("jToggleButton1-105-23", new Integer(15));
        compBounds.put("jTextField2", new Rectangle(121, 43, 88, 20));
        baselinePosition.put("jTextField2-88-20", new Integer(14));
        compBounds.put("jCheckBox1", new Rectangle(10, 71, 81, 23));
        baselinePosition.put("jCheckBox1-81-23", new Integer(15));
        compBounds.put("jButton1", new Rectangle(93, 71, 116, 23));
        baselinePosition.put("jButton1-116-23", new Integer(15));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compPrefSize.put("jTextField2", new Dimension(59, 20));
        compPrefSize.put("jButton1", new Dimension(115, 23));
        prefPaddingInParent.put("Form-jCheckBox1-1-1", new Integer(7)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        contInterior.put("Form", new Rectangle(0, 0, 219, 300));
        compBounds.put("jLabel1", new Rectangle(10, 14, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compBounds.put("jTextField1", new Rectangle(48, 11, 161, 20));
        baselinePosition.put("jTextField1-161-20", new Integer(14));
        compBounds.put("jToggleButton1", new Rectangle(10, 42, 105, 23));
        baselinePosition.put("jToggleButton1-105-23", new Integer(15));
        compBounds.put("jTextField2", new Rectangle(121, 43, 88, 20));
        baselinePosition.put("jTextField2-88-20", new Integer(14));
        compBounds.put("jCheckBox1", new Rectangle(10, 71, 81, 23));
        baselinePosition.put("jCheckBox1-81-23", new Integer(15));
        compBounds.put("jButton1", new Rectangle(93, 71, 116, 23));
        baselinePosition.put("jButton1-116-23", new Integer(15));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 218, 300));
        compBounds.put("jLabel1", new Rectangle(10, 11, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compBounds.put("jTextField1", new Rectangle(48, 11, 160, 20));
        baselinePosition.put("jTextField1-160-20", new Integer(14));
        compBounds.put("jToggleButton1", new Rectangle(10, 42, 105, 23));
        baselinePosition.put("jToggleButton1-105-23", new Integer(15));
        compBounds.put("jTextField2", new Rectangle(121, 43, 87, 20));
        baselinePosition.put("jTextField2-87-20", new Integer(14));
        compBounds.put("jCheckBox1", new Rectangle(10, 71, 81, 23));
        baselinePosition.put("jCheckBox1-81-23", new Integer(15));
        compBounds.put("jButton1", new Rectangle(93, 71, 115, 23));
        baselinePosition.put("jButton1-115-23", new Integer(15));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compPrefSize.put("jTextField2", new Dimension(59, 20));
        compPrefSize.put("jButton1", new Dimension(115, 23));
        prefPaddingInParent.put("Form-jCheckBox1-1-1", new Integer(7)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        contInterior.put("Form", new Rectangle(0, 0, 218, 300));
        compBounds.put("jLabel1", new Rectangle(10, 14, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compBounds.put("jTextField1", new Rectangle(48, 11, 160, 20));
        baselinePosition.put("jTextField1-160-20", new Integer(14));
        compBounds.put("jToggleButton1", new Rectangle(10, 42, 105, 23));
        baselinePosition.put("jToggleButton1-105-23", new Integer(15));
        compBounds.put("jTextField2", new Rectangle(121, 43, 87, 20));
        baselinePosition.put("jTextField2-87-20", new Integer(14));
        compBounds.put("jCheckBox1", new Rectangle(10, 71, 81, 23));
        baselinePosition.put("jCheckBox1-81-23", new Integer(15));
        compBounds.put("jButton1", new Rectangle(93, 71, 115, 23));
        baselinePosition.put("jButton1-115-23", new Integer(15));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }

    /**
     * Resize whole designer slightly to the right. This will make whole container
     * bigger than default size, but only jTextField1 should have explicit pref.
     * size set (other resizing components should stay at default).
     */
    public void doChanges1() {
        // > START RESIZING
        baselinePosition.put("Form-218-300", new Integer(-1));
        compMinSize.put("Form", new Dimension(218, 105));
        prefPaddingInParent.put("Form-jTextField1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jTextField2-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        // parentId-compId-dimension-compAlignment
        // parentId-compId-dimension-compAlignment
        {
            String[] compIds = new String[]{"Form"};
            Rectangle[] bounds = new Rectangle[]{new Rectangle(0, 0, 218, 300)};
            Point hotspot = new Point(217, 190);
            int[] resizeEdges = new int[]{1, -1};
            boolean inLayout = false;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
        // < START RESIZING
        // > MOVE
        // < START RESIZING
        // > MOVE
        {
            Point p = new Point(286, 196);
            String containerId = null;
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(0, 0, 287, 300)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > MOVE
        // < MOVE
        // > MOVE
        {
            Point p = new Point(289, 196);
            String containerId = null;
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(0, 0, 290, 300)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        contInterior.put("Form", new Rectangle(0, 0, 290, 300));
        compBounds.put("jLabel1", new Rectangle(10, 14, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compBounds.put("jTextField1", new Rectangle(48, 11, 232, 20));
        baselinePosition.put("jTextField1-232-20", new Integer(14));
        compBounds.put("jToggleButton1", new Rectangle(10, 42, 105, 23));
        baselinePosition.put("jToggleButton1-105-23", new Integer(15));
        compBounds.put("jTextField2", new Rectangle(121, 43, 159, 20));
        baselinePosition.put("jTextField2-159-20", new Integer(14));
        compBounds.put("jCheckBox1", new Rectangle(10, 71, 81, 23));
        baselinePosition.put("jCheckBox1-81-23", new Integer(15));
        compBounds.put("jButton1", new Rectangle(93, 71, 187, 23));
        baselinePosition.put("jButton1-187-23", new Integer(15));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compPrefSize.put("jTextField2", new Dimension(59, 20));
        compPrefSize.put("jButton1", new Dimension(115, 23));
        prefPaddingInParent.put("Form-jCheckBox1-1-1", new Integer(7)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        // parentId-compId-dimension-compAlignment
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 290, 300));
        compBounds.put("jLabel1", new Rectangle(10, 14, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compBounds.put("jTextField1", new Rectangle(48, 11, 232, 20));
        baselinePosition.put("jTextField1-232-20", new Integer(14));
        compBounds.put("jToggleButton1", new Rectangle(10, 42, 105, 23));
        baselinePosition.put("jToggleButton1-105-23", new Integer(15));
        compBounds.put("jTextField2", new Rectangle(121, 43, 159, 20));
        baselinePosition.put("jTextField2-159-20", new Integer(14));
        compBounds.put("jCheckBox1", new Rectangle(10, 71, 81, 23));
        baselinePosition.put("jCheckBox1-81-23", new Integer(15));
        compBounds.put("jButton1", new Rectangle(93, 71, 187, 23));
        baselinePosition.put("jButton1-187-23", new Integer(15));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compPrefSize.put("jTextField2", new Dimension(59, 20));
        compPrefSize.put("jButton1", new Dimension(115, 23));
        prefPaddingInParent.put("Form-jCheckBox1-1-1", new Integer(7)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        contInterior.put("Form", new Rectangle(0, 0, 290, 300));
        compBounds.put("jLabel1", new Rectangle(10, 14, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compBounds.put("jTextField1", new Rectangle(48, 11, 232, 20));
        baselinePosition.put("jTextField1-232-20", new Integer(14));
        compBounds.put("jToggleButton1", new Rectangle(10, 42, 105, 23));
        baselinePosition.put("jToggleButton1-105-23", new Integer(15));
        compBounds.put("jTextField2", new Rectangle(121, 43, 159, 20));
        baselinePosition.put("jTextField2-159-20", new Integer(14));
        compBounds.put("jCheckBox1", new Rectangle(10, 71, 81, 23));
        baselinePosition.put("jCheckBox1-81-23", new Integer(15));
        compBounds.put("jButton1", new Rectangle(93, 71, 187, 23));
        baselinePosition.put("jButton1-187-23", new Integer(15));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }

}
