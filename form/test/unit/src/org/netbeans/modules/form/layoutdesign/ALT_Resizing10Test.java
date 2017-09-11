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

public class ALT_Resizing10Test extends LayoutTestCase {

    public ALT_Resizing10Test(String name) {
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
     * Resize jButton1 slightly to the right. There used to be a bug that cut
     * the last gap to compensate.
     */
    public void doChanges0() {
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 420, 300));
        compBounds.put("jToggleButton1", new Rectangle(57, 71, 105, 23));
        baselinePosition.put("jToggleButton1-105-23", new Integer(15));
        compBounds.put("jButton1", new Rectangle(57, 100, 105, 23));
        baselinePosition.put("jButton1-105-23", new Integer(15));
        compBounds.put("jButton2", new Rectangle(57, 129, 105, 23));
        baselinePosition.put("jButton2-105-23", new Integer(15));
        compBounds.put("jScrollPane1", new Rectangle(168, 50, 35, 130));
        baselinePosition.put("jScrollPane1-35-130", new Integer(0));
        compBounds.put("jTextField1", new Rectangle(209, 81, 181, 20));
        baselinePosition.put("jTextField1-181-20", new Integer(14));
        compBounds.put("jTextField2", new Rectangle(209, 112, 181, 20));
        baselinePosition.put("jTextField2-181-20", new Integer(14));
        compPrefSize.put("jToggleButton1", new Dimension(105, 23));
        compPrefSize.put("jButton1", new Dimension(73, 23));
        compPrefSize.put("jButton2", new Dimension(73, 23));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compPrefSize.put("jTextField2", new Dimension(59, 20));
        prefPaddingInParent.put("Form-jButton2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollPane1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jTextField2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        contInterior.put("Form", new Rectangle(0, 0, 420, 300));
        compBounds.put("jToggleButton1", new Rectangle(57, 71, 105, 23));
        baselinePosition.put("jToggleButton1-105-23", new Integer(15));
        compBounds.put("jButton1", new Rectangle(57, 100, 105, 23));
        baselinePosition.put("jButton1-105-23", new Integer(15));
        compBounds.put("jButton2", new Rectangle(57, 129, 105, 23));
        baselinePosition.put("jButton2-105-23", new Integer(15));
        compBounds.put("jScrollPane1", new Rectangle(168, 50, 35, 130));
        baselinePosition.put("jScrollPane1-35-130", new Integer(0));
        compBounds.put("jTextField1", new Rectangle(209, 81, 181, 20));
        baselinePosition.put("jTextField1-181-20", new Integer(14));
        compBounds.put("jTextField2", new Rectangle(209, 112, 181, 20));
        baselinePosition.put("jTextField2-181-20", new Integer(14));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
        // > START RESIZING
        baselinePosition.put("jButton1-105-23", new Integer(15));
        compPrefSize.put("jButton1", new Dimension(73, 23));
        {
            String[] compIds = new String[]{"jButton1"};
            Rectangle[] bounds = new Rectangle[]{new Rectangle(57, 100, 105, 23)};
            Point hotspot = new Point(163, 116);
            int[] resizeEdges = new int[]{1, -1};
            boolean inLayout = true;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
        // < START RESIZING
        prefPadding.put("jButton1-jScrollPane1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        // > MOVE
        // > MOVE
        {
            Point p = new Point(180, 119);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(57, 100, 122, 23)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPadding.put("jButton1-jScrollPane1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        // > MOVE
        // > MOVE
        {
            Point p = new Point(181, 119);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(57, 100, 123, 23)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 420, 300));
        compBounds.put("jToggleButton1", new Rectangle(57, 71, 105, 23));
        baselinePosition.put("jToggleButton1-123-23", new Integer(15));
        compBounds.put("jButton1", new Rectangle(57, 100, 123, 23));
        baselinePosition.put("jButton1-123-23", new Integer(15));
        compBounds.put("jButton2", new Rectangle(57, 129, 105, 23));
        baselinePosition.put("jButton2-123-23", new Integer(15));
        compBounds.put("jScrollPane1", new Rectangle(186, 50, 35, 130));
        baselinePosition.put("jScrollPane1-35-130", new Integer(0));
        compBounds.put("jTextField1", new Rectangle(227, 81, 163, 20));
        baselinePosition.put("jTextField1-163-20", new Integer(14));
        compBounds.put("jTextField2", new Rectangle(227, 112, 163, 20));
        baselinePosition.put("jTextField2-163-20", new Integer(14));
        compPrefSize.put("jToggleButton1", new Dimension(105, 23));
        compPrefSize.put("jButton2", new Dimension(73, 23));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compPrefSize.put("jTextField2", new Dimension(59, 20));
        prefPaddingInParent.put("Form-jButton2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollPane1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jTextField2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        contInterior.put("Form", new Rectangle(0, 0, 420, 300));
        compBounds.put("jScrollPane1", new Rectangle(186, 50, 35, 130));
        baselinePosition.put("jScrollPane1-35-130", new Integer(0));
        compBounds.put("jTextField1", new Rectangle(227, 81, 163, 20));
        baselinePosition.put("jTextField1-163-20", new Integer(14));
        compBounds.put("jTextField2", new Rectangle(227, 112, 163, 20));
        baselinePosition.put("jTextField2-163-20", new Integer(14));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }

}
