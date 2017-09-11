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

public class ALT_MultiResizable02Test extends LayoutTestCase {

    public ALT_MultiResizable02Test(String name) {
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
     * Resize the panel with button to the left, then to the right, at default
     * gap from container border. The resizing gaps next to the buttons should
     * keep default size.
     */
    public void doChanges0() {
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 458, 300));
        contInterior.put("Form", new Rectangle(0, 0, 458, 300));
        compBounds.put("jToggleButton1", new Rectangle(10, 11, 105, 23));
        baselinePosition.put("jToggleButton1-105-23", new Integer(15));
        compBounds.put("jToggleButton2", new Rectangle(121, 11, 105, 23));
        baselinePosition.put("jToggleButton2-105-23", new Integer(15));
        compBounds.put("jToggleButton3", new Rectangle(232, 11, 105, 23));
        baselinePosition.put("jToggleButton3-105-23", new Integer(15));
        compBounds.put("jToggleButton4", new Rectangle(343, 11, 105, 23));
        baselinePosition.put("jToggleButton4-105-23", new Integer(15));
        compBounds.put("jPanel1", new Rectangle(180, 73, 93, 45));
        baselinePosition.put("jPanel1-93-45", new Integer(0));
        compMinSize.put("Form", new Dimension(458, 129));
        compBounds.put("Form", new Rectangle(0, 0, 458, 300));
        compBounds.put("Form", new Rectangle(0, 0, 458, 300));
        compBounds.put("Form", new Rectangle(0, 0, 458, 300));
        compBounds.put("jPanel1", new Rectangle(180, 73, 93, 45));
        contInterior.put("jPanel1", new Rectangle(180, 73, 93, 45));
        compBounds.put("jButton1", new Rectangle(190, 84, 73, 23));
        baselinePosition.put("jButton1-73-23", new Integer(15));
        compMinSize.put("jPanel1", new Dimension(93, 45));
        compBounds.put("jPanel1", new Rectangle(180, 73, 93, 45));
        prefPaddingInParent.put("jPanel1-jButton1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel1-jButton1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel1-jButton1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel1-jButton1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        compPrefSize.put("jButton1", new Dimension(73, 23));
        prefPaddingInParent.put("jPanel1-jButton1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        compPrefSize.put("jPanel1", new Dimension(93, 45));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        prefPaddingInParent.put("Form-jToggleButton4-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jPanel1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jPanel1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
        prefPaddingInParent.put("Form-jToggleButton1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jToggleButton2-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jToggleButton3-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jToggleButton4-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jPanel1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jToggleButton1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jToggleButton4-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jPanel1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel1-jButton1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel1-jButton1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel1-jButton1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel1-jButton1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jPanel1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jPanel1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
// > START RESIZING
        baselinePosition.put("jPanel1-93-45", new Integer(0));
        compMinSize.put("jPanel1", new Dimension(93, 45));
        prefPaddingInParent.put("jPanel1-jButton1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        {
            String[] compIds = new String[]{
                "jPanel1"
            };
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(180, 73, 93, 45)
            };
            Point hotspot = new Point(175, 99);
            int[] resizeEdges = new int[]{
                0,
                -1
            };
            boolean inLayout = true;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
// < START RESIZING
        prefPaddingInParent.put("Form-jPanel1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
// > MOVE
        {
            Point p = new Point(13, 96);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(18, 73, 255, 45)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
        prefPaddingInParent.put("Form-jPanel1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
// > MOVE
        {
            Point p = new Point(12, 96);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(10, 73, 263, 45)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
// > END MOVING
        prefPaddingInParent.put("Form-jToggleButton4-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jToggleButton1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jToggleButton2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jToggleButton3-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jToggleButton4-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jPanel1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        contInterior.put("jPanel1", new Rectangle(-32758, -32695, 263, 45));
        compBounds.put("jButton1", new Rectangle(-32663, -32684, 73, 23));
        baselinePosition.put("jButton1-73-23", new Integer(15));
        prefPaddingInParent.put("jPanel1-jButton1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel1-jButton1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel1-jButton1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        ld.endMoving(true);
// < END MOVING
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 458, 300));
        contInterior.put("Form", new Rectangle(0, 0, 458, 300));
        compBounds.put("jToggleButton1", new Rectangle(10, 11, 105, 23));
        baselinePosition.put("jToggleButton1-105-23", new Integer(15));
        compBounds.put("jToggleButton2", new Rectangle(121, 11, 105, 23));
        baselinePosition.put("jToggleButton2-105-23", new Integer(15));
        compBounds.put("jToggleButton3", new Rectangle(232, 11, 105, 23));
        baselinePosition.put("jToggleButton3-105-23", new Integer(15));
        compBounds.put("jToggleButton4", new Rectangle(343, 11, 105, 23));
        baselinePosition.put("jToggleButton4-105-23", new Integer(15));
        compBounds.put("jPanel1", new Rectangle(10, 73, 263, 45));
        baselinePosition.put("jPanel1-263-45", new Integer(0));
        compMinSize.put("Form", new Dimension(458, 129));
        compBounds.put("Form", new Rectangle(0, 0, 458, 300));
        contInterior.put("jPanel1", new Rectangle(10, 73, 263, 45));
        compBounds.put("jButton1", new Rectangle(105, 84, 73, 23));
        baselinePosition.put("jButton1-73-23", new Integer(15));
        compMinSize.put("jPanel1", new Dimension(93, 45));
        compBounds.put("jPanel1", new Rectangle(10, 73, 263, 45));
        prefPaddingInParent.put("jPanel1-jButton1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel1-jButton1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel1-jButton1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel1-jButton1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        compPrefSize.put("jButton1", new Dimension(73, 23));
        prefPaddingInParent.put("jPanel1-jButton1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        compPrefSize.put("jPanel1", new Dimension(263, 45));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        prefPaddingInParent.put("Form-jToggleButton4-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        compPrefSize.put("jPanel1", new Dimension(263, 45));
        prefPaddingInParent.put("Form-jPanel1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        compBounds.put("Form", new Rectangle(0, 0, 458, 300));
        contInterior.put("Form", new Rectangle(0, 0, 458, 300));
        compBounds.put("jToggleButton1", new Rectangle(10, 11, 105, 23));
        baselinePosition.put("jToggleButton1-105-23", new Integer(15));
        compBounds.put("jToggleButton2", new Rectangle(121, 11, 105, 23));
        baselinePosition.put("jToggleButton2-105-23", new Integer(15));
        compBounds.put("jToggleButton3", new Rectangle(232, 11, 105, 23));
        baselinePosition.put("jToggleButton3-105-23", new Integer(15));
        compBounds.put("jToggleButton4", new Rectangle(343, 11, 105, 23));
        baselinePosition.put("jToggleButton4-105-23", new Integer(15));
        compBounds.put("jPanel1", new Rectangle(10, 73, 263, 45));
        baselinePosition.put("jPanel1-263-45", new Integer(0));
        compMinSize.put("Form", new Dimension(458, 129));
        compBounds.put("Form", new Rectangle(0, 0, 458, 300));
        contInterior.put("jPanel1", new Rectangle(10, 73, 263, 45));
        compBounds.put("jButton1", new Rectangle(105, 84, 73, 23));
        baselinePosition.put("jButton1-73-23", new Integer(15));
        compMinSize.put("jPanel1", new Dimension(93, 45));
        compBounds.put("jPanel1", new Rectangle(10, 73, 263, 45));
        prefPaddingInParent.put("jPanel1-jButton1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel1-jButton1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel1-jButton1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jToggleButton4-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        compPrefSize.put("jPanel1", new Dimension(93, 45));
        prefPaddingInParent.put("Form-jPanel1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 458, 300));
        prefPaddingInParent.put("Form-jPanel1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jToggleButton1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jPanel1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        compBounds.put("jPanel1", new Rectangle(10, 73, 263, 45));
        prefPaddingInParent.put("jPanel1-jButton1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel1-jButton1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel1-jButton1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel1-jButton1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
// > START RESIZING
        baselinePosition.put("jPanel1-263-45", new Integer(0));
        compMinSize.put("jPanel1", new Dimension(93, 45));
        prefPaddingInParent.put("jPanel1-jButton1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        {
            String[] compIds = new String[]{
                "jPanel1"
            };
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(10, 73, 263, 45)
            };
            Point hotspot = new Point(275, 98);
            int[] resizeEdges = new int[]{
                1,
                -1
            };
            boolean inLayout = true;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
// < START RESIZING
        prefPaddingInParent.put("Form-jPanel1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
// > MOVE
        {
            Point p = new Point(443, 100);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(10, 73, 438, 45)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
        prefPaddingInParent.put("Form-jPanel1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
// > MOVE
        {
            Point p = new Point(444, 100);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(10, 73, 438, 45)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
// > END MOVING
        prefPaddingInParent.put("Form-jToggleButton4-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jToggleButton1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jToggleButton2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jToggleButton3-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jToggleButton4-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        contInterior.put("jPanel1", new Rectangle(-32758, -32695, 438, 45));
        compBounds.put("jButton1", new Rectangle(-32576, -32684, 73, 23));
        baselinePosition.put("jButton1-73-23", new Integer(15));
        prefPaddingInParent.put("jPanel1-jButton1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel1-jButton1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel1-jButton1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        ld.endMoving(true);
// < END MOVING
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 458, 300));
        contInterior.put("Form", new Rectangle(0, 0, 458, 300));
        compBounds.put("jToggleButton1", new Rectangle(10, 11, 105, 23));
        baselinePosition.put("jToggleButton1-105-23", new Integer(15));
        compBounds.put("jToggleButton2", new Rectangle(121, 11, 105, 23));
        baselinePosition.put("jToggleButton2-105-23", new Integer(15));
        compBounds.put("jToggleButton3", new Rectangle(232, 11, 105, 23));
        baselinePosition.put("jToggleButton3-105-23", new Integer(15));
        compBounds.put("jToggleButton4", new Rectangle(343, 11, 105, 23));
        baselinePosition.put("jToggleButton4-105-23", new Integer(15));
        compBounds.put("jPanel1", new Rectangle(10, 73, 438, 45));
        baselinePosition.put("jPanel1-438-45", new Integer(0));
        compMinSize.put("Form", new Dimension(458, 129));
        compBounds.put("Form", new Rectangle(0, 0, 458, 300));
        contInterior.put("jPanel1", new Rectangle(10, 73, 438, 45));
        compBounds.put("jButton1", new Rectangle(192, 84, 73, 23));
        baselinePosition.put("jButton1-73-23", new Integer(15));
        compMinSize.put("jPanel1", new Dimension(93, 45));
        compBounds.put("jPanel1", new Rectangle(10, 73, 438, 45));
        prefPaddingInParent.put("jPanel1-jButton1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel1-jButton1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel1-jButton1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel1-jButton1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        compPrefSize.put("jButton1", new Dimension(73, 23));
        prefPaddingInParent.put("jPanel1-jButton1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        compPrefSize.put("jPanel1", new Dimension(438, 45));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        compPrefSize.put("jPanel1", new Dimension(438, 45));
        prefPaddingInParent.put("Form-jPanel1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        compBounds.put("Form", new Rectangle(0, 0, 458, 300));
        contInterior.put("Form", new Rectangle(0, 0, 458, 300));
        compBounds.put("jToggleButton1", new Rectangle(10, 11, 105, 23));
        baselinePosition.put("jToggleButton1-105-23", new Integer(15));
        compBounds.put("jToggleButton2", new Rectangle(121, 11, 105, 23));
        baselinePosition.put("jToggleButton2-105-23", new Integer(15));
        compBounds.put("jToggleButton3", new Rectangle(232, 11, 105, 23));
        baselinePosition.put("jToggleButton3-105-23", new Integer(15));
        compBounds.put("jToggleButton4", new Rectangle(343, 11, 105, 23));
        baselinePosition.put("jToggleButton4-105-23", new Integer(15));
        compBounds.put("jPanel1", new Rectangle(10, 73, 438, 45));
        baselinePosition.put("jPanel1-438-45", new Integer(0));
        compMinSize.put("Form", new Dimension(458, 129));
        compBounds.put("Form", new Rectangle(0, 0, 458, 300));
        contInterior.put("jPanel1", new Rectangle(10, 73, 438, 45));
        compBounds.put("jButton1", new Rectangle(192, 84, 73, 23));
        baselinePosition.put("jButton1-73-23", new Integer(15));
        compMinSize.put("jPanel1", new Dimension(93, 45));
        compBounds.put("jPanel1", new Rectangle(10, 73, 438, 45));
        prefPaddingInParent.put("jPanel1-jButton1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel1-jButton1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel1-jButton1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        compPrefSize.put("jPanel1", new Dimension(93, 45));
        prefPaddingInParent.put("Form-jPanel1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 458, 300));
        prefPaddingInParent.put("Form-jPanel1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jPanel1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jToggleButton1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jPanel1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        compBounds.put("jPanel1", new Rectangle(10, 73, 438, 45));
        prefPaddingInParent.put("jPanel1-jButton1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel1-jButton1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel1-jButton1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel1-jButton1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel1-jButton1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel1-jButton1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
    }
}
