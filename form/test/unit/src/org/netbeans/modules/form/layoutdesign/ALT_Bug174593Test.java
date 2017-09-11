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

public class ALT_Bug174593Test extends LayoutTestCase {

    public ALT_Bug174593Test(String name) {
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
     * Resize jPanel2 to align at bottom with jPanel1. Used to cause AE due to
     * two consecutive gaps appearing in layout before eliminated later.
     */
    public void doChanges0() {
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("jPanel1", new Rectangle(11, 12, 227, 226));
        compBounds.put("jPanel1", new Rectangle(10, 11, 229, 228));
        baselinePosition.put("jPanel1-229-228", new Integer(0));
        compMinSize.put("jPanel1", new Dimension(2, 2));
        compPrefSize.put("jPanel1", new Dimension(229, 228));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        contInterior.put("Form", new Rectangle(0, 0, 433, 292));
        compBounds.put("jComboBox1", new Rectangle(10, 250, 229, 20));
        baselinePosition.put("jComboBox1-229-20", new Integer(14));
        compPrefSize.put("jComboBox1", new Dimension(56, 20));
        compBounds.put("jPanel1", new Rectangle(10, 11, 229, 228));
        baselinePosition.put("jPanel1-229-228", new Integer(0));
        compBounds.put("jLabel1", new Rectangle(249, 11, 72, 64));
        baselinePosition.put("jLabel1-72-64", new Integer(36));
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        compBounds.put("jPanel2", new Rectangle(245, 81, 102, 116));
        baselinePosition.put("jPanel2-102-116", new Integer(0));
        contInterior.put("jPanel2", new Rectangle(246, 82, 100, 114));
        compBounds.put("jPanel2", new Rectangle(245, 81, 102, 116));
        baselinePosition.put("jPanel2-102-116", new Integer(0));
        compMinSize.put("jPanel2", new Dimension(2, 2));
        compPrefSize.put("jPanel2", new Dimension(102, 116));
        hasExplicitPrefSize.put("jPanel2", new Boolean(false));
        hasExplicitPrefSize.put("jPanel2", new Boolean(false));
        contInterior.put("jPanel1", new Rectangle(11, 12, 227, 226));
        contInterior.put("Form", new Rectangle(0, 0, 433, 292));
        compBounds.put("jComboBox1", new Rectangle(10, 250, 229, 20));
        baselinePosition.put("jComboBox1-229-20", new Integer(14));
        compBounds.put("jPanel1", new Rectangle(10, 11, 229, 228));
        baselinePosition.put("jPanel1-229-228", new Integer(0));
        compBounds.put("jLabel1", new Rectangle(249, 11, 72, 64));
        baselinePosition.put("jLabel1-72-64", new Integer(36));
        compBounds.put("jPanel2", new Rectangle(245, 81, 102, 116));
        baselinePosition.put("jPanel2-102-116", new Integer(0));
        contInterior.put("jPanel2", new Rectangle(246, 82, 100, 114));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
        // > START RESIZING
        baselinePosition.put("jPanel2-102-116", new Integer(0));
        compMinSize.put("jPanel2", new Dimension(2, 2));
        {
            String[] compIds = new String[]{"jPanel2"};
            Rectangle[] bounds = new Rectangle[]{new Rectangle(245, 81, 102, 116)};
            Point hotspot = new Point(285, 198);
            int[] resizeEdges = new int[]{-1, 1};
            boolean inLayout = true;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
        // < START RESIZING
        prefPaddingInParent.put("Form-jPanel2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        // > MOVE
        // > MOVE
        {
            Point p = new Point(269, 237);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(245, 81, 102, 158)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPaddingInParent.put("Form-jPanel2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        // > MOVE
        // > MOVE
        {
            Point p = new Point(269, 238);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(245, 81, 102, 158)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        prefPaddingInParent.put("Form-jPanel2-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        contInterior.put("jPanel2", new Rectangle(-32522, -32686, 100, 156));
        compBounds.put("jPanel2", new Rectangle(-32523, -32687, 102, 158));
        baselinePosition.put("jPanel2-102-158", new Integer(0));
        compMinSize.put("jPanel2", new Dimension(2, 2));
        compPrefSize.put("jPanel2", new Dimension(102, 116));
        hasExplicitPrefSize.put("jPanel2", new Boolean(false));
        hasExplicitPrefSize.put("jPanel2", new Boolean(false));
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("jPanel1", new Rectangle(11, 12, 227, 226));
        compBounds.put("jPanel1", new Rectangle(10, 11, 229, 228));
        baselinePosition.put("jPanel1-229-228", new Integer(0));
        compMinSize.put("jPanel1", new Dimension(2, 2));
        compPrefSize.put("jPanel1", new Dimension(229, 228));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        contInterior.put("Form", new Rectangle(0, 0, 433, 292));
        compBounds.put("jComboBox1", new Rectangle(10, 250, 229, 20));
        baselinePosition.put("jComboBox1-229-20", new Integer(14));
        compPrefSize.put("jComboBox1", new Dimension(56, 20));
        compBounds.put("jPanel1", new Rectangle(10, 11, 229, 228));
        baselinePosition.put("jPanel1-229-228", new Integer(0));
        compBounds.put("jLabel1", new Rectangle(249, 11, 72, 64));
        baselinePosition.put("jLabel1-72-64", new Integer(36));
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        compBounds.put("jPanel2", new Rectangle(245, 81, 102, 158));
        baselinePosition.put("jPanel2-102-158", new Integer(0));
        contInterior.put("jPanel2", new Rectangle(246, 82, 100, 156));
        compBounds.put("jPanel2", new Rectangle(245, 81, 102, 158));
        baselinePosition.put("jPanel2-102-158", new Integer(0));
        compMinSize.put("jPanel2", new Dimension(2, 2));
        compPrefSize.put("jPanel2", new Dimension(102, 158));
        hasExplicitPrefSize.put("jPanel2", new Boolean(false));
        hasExplicitPrefSize.put("jPanel2", new Boolean(false));
        contInterior.put("jPanel1", new Rectangle(11, 12, 227, 226));
        contInterior.put("Form", new Rectangle(0, 0, 433, 292));
        compBounds.put("jComboBox1", new Rectangle(10, 250, 229, 20));
        baselinePosition.put("jComboBox1-229-20", new Integer(14));
        compBounds.put("jPanel1", new Rectangle(10, 11, 229, 228));
        baselinePosition.put("jPanel1-229-228", new Integer(0));
        compBounds.put("jLabel1", new Rectangle(249, 11, 72, 64));
        baselinePosition.put("jLabel1-72-64", new Integer(36));
        compBounds.put("jPanel2", new Rectangle(245, 81, 102, 158));
        baselinePosition.put("jPanel2-102-158", new Integer(0));
        contInterior.put("jPanel2", new Rectangle(246, 82, 100, 156));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }

}
