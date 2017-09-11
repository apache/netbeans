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

// Manipulating component at the border - without a gap.
public class ALT_BorderPositions01Test extends LayoutTestCase {

    public ALT_BorderPositions01Test(String name) {
        super(name);
        try {
	    className = this.getClass().getName();
	    className = className.substring(className.lastIndexOf('.') + 1, className.length());	    
            startingFormFile = FileUtil.toFileObject(new File(url.getFile() + goldenFilesPath + className + "-StartingForm.form").getCanonicalFile());
        } catch (IOException ioe) {
            fail(ioe.toString());
        }
    }

    // Position panel in the top left corner (no gap).
    public void doChanges0() {
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
        lc = new LayoutComponent("jPanel1", true, 100, 100);
        // > START ADDING
        baselinePosition.put("jPanel1-100-100", new Integer(0));
        {
            LayoutComponent[] comps = new LayoutComponent[] { lc };
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(0, 0, 100, 100)
                };
            String defaultContId = null;
            Point hotspot = new Point(46,50);
            ld.startAdding(comps, bounds, hotspot, defaultContId);
        }
        // < START ADDING
        prefPaddingInParent.put("Form-jPanel1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jPanel1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jPanel1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jPanel1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        // > MOVE
        {
            Point p = new Point(45,51);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(0, 0, 100, 100)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPaddingInParent.put("Form-jPanel1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jPanel1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jPanel1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jPanel1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        // > MOVE
        {
            Point p = new Point(44,51);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(0, 0, 100, 100)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        compPrefSize.put("jPanel1", new Dimension(100, 100));
        compPrefSize.put("jPanel1", new Dimension(100, 100));
        prefPaddingInParent.put("Form-jPanel1-1-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jPanel1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("jPanel1", new Rectangle(0, 0, 100, 100));
        compBounds.put("jPanel1", new Rectangle(0, 0, 100, 100));
        baselinePosition.put("jPanel1-100-100", new Integer(0));
        compMinSize.put("jPanel1", new Dimension(0, 0));
        compPrefSize.put("jPanel1", new Dimension(100, 100));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jPanel1", new Rectangle(0, 0, 100, 100));
        baselinePosition.put("jPanel1-100-100", new Integer(0));
        contInterior.put("jPanel1", new Rectangle(0, 0, 100, 100));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jPanel1", new Rectangle(0, 0, 100, 100));
        baselinePosition.put("jPanel1-100-100", new Integer(0));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }

    // Resize panel slightly to the right, then down.
    public void doChanges1() {
        // > START RESIZING
        baselinePosition.put("jPanel1-100-100", new Integer(0));
        compMinSize.put("jPanel1", new Dimension(0, 0));
        {
            String[] compIds = new String[] {
                "jPanel1"
                };
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(0, 0, 100, 100)
                };
            Point hotspot = new Point(100,50);
            int[] resizeEdges = new int[] {
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
            Point p = new Point(115,52);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(0, 0, 115, 100)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPaddingInParent.put("Form-jPanel1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        // > MOVE
        {
            Point p = new Point(116,52);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(0, 0, 116, 100)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        prefPaddingInParent.put("Form-jPanel1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jPanel1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        contInterior.put("jPanel1", new Rectangle(-32768, -32768, 116, 100));
        compBounds.put("jPanel1", new Rectangle(-32768, -32768, 116, 100));
        baselinePosition.put("jPanel1-116-100", new Integer(0));
        compMinSize.put("jPanel1", new Dimension(0, 0));
        compPrefSize.put("jPanel1", new Dimension(100, 100));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("jPanel1", new Rectangle(0, 0, 116, 100));
        compBounds.put("jPanel1", new Rectangle(0, 0, 116, 100));
        baselinePosition.put("jPanel1-116-100", new Integer(0));
        compMinSize.put("jPanel1", new Dimension(0, 0));
        compPrefSize.put("jPanel1", new Dimension(116, 100));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jPanel1", new Rectangle(0, 0, 116, 100));
        baselinePosition.put("jPanel1-116-100", new Integer(0));
        contInterior.put("jPanel1", new Rectangle(0, 0, 116, 100));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jPanel1", new Rectangle(0, 0, 116, 100));
        baselinePosition.put("jPanel1-116-100", new Integer(0));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
        // > START RESIZING
        baselinePosition.put("jPanel1-116-100", new Integer(0));
        compMinSize.put("jPanel1", new Dimension(0, 0));
        {
            String[] compIds = new String[] {
                "jPanel1"
                };
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(0, 0, 116, 100)
                };
            Point hotspot = new Point(54,103);
            int[] resizeEdges = new int[] {
                -1,
                    1
                };
            boolean inLayout = true;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
        // < START RESIZING
        prefPaddingInParent.put("Form-jPanel1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        // > MOVE
        {
            Point p = new Point(54,123);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(0, 0, 116, 120)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPaddingInParent.put("Form-jPanel1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        // > MOVE
        {
            Point p = new Point(54,124);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(0, 0, 116, 121)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        prefPaddingInParent.put("Form-jPanel1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jPanel1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        contInterior.put("jPanel1", new Rectangle(-32768, -32768, 116, 121));
        compBounds.put("jPanel1", new Rectangle(-32768, -32768, 116, 121));
        baselinePosition.put("jPanel1-116-121", new Integer(0));
        compMinSize.put("jPanel1", new Dimension(0, 0));
        compPrefSize.put("jPanel1", new Dimension(116, 100));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("jPanel1", new Rectangle(0, 0, 116, 121));
        compBounds.put("jPanel1", new Rectangle(0, 0, 116, 121));
        baselinePosition.put("jPanel1-116-121", new Integer(0));
        compMinSize.put("jPanel1", new Dimension(0, 0));
        compPrefSize.put("jPanel1", new Dimension(116, 121));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jPanel1", new Rectangle(0, 0, 116, 121));
        baselinePosition.put("jPanel1-116-121", new Integer(0));
        contInterior.put("jPanel1", new Rectangle(0, 0, 116, 121));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jPanel1", new Rectangle(0, 0, 116, 121));
        baselinePosition.put("jPanel1-116-121", new Integer(0));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }

    // Resize panel down-right to fill whole frame (no gaps).
    public void doChanges2() {
        // > START RESIZING
        baselinePosition.put("jPanel1-116-121", new Integer(0));
        compMinSize.put("jPanel1", new Dimension(0, 0));
        {
            String[] compIds = new String[] {
                "jPanel1"
                };
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(0, 0, 116, 121)
                };
            Point hotspot = new Point(115,120);
            int[] resizeEdges = new int[] {
                1,
                    1
                };
            boolean inLayout = true;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
        // < START RESIZING
        prefPaddingInParent.put("Form-jPanel1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jPanel1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        // > MOVE
        {
            Point p = new Point(394,296);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(0, 0, 400, 300)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPaddingInParent.put("Form-jPanel1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jPanel1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        // > MOVE
        {
            Point p = new Point(395,296);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(0, 0, 400, 300)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        contInterior.put("jPanel1", new Rectangle(-32768, -32768, 400, 300));
        compBounds.put("jPanel1", new Rectangle(-32768, -32768, 400, 300));
        baselinePosition.put("jPanel1-400-300", new Integer(0));
        compMinSize.put("jPanel1", new Dimension(0, 0));
        compPrefSize.put("jPanel1", new Dimension(116, 121));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("jPanel1", new Rectangle(0, 0, 400, 300));
        compBounds.put("jPanel1", new Rectangle(0, 0, 400, 300));
        baselinePosition.put("jPanel1-400-300", new Integer(0));
        compMinSize.put("jPanel1", new Dimension(0, 0));
        compPrefSize.put("jPanel1", new Dimension(400, 300));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jPanel1", new Rectangle(0, 0, 400, 300));
        baselinePosition.put("jPanel1-400-300", new Integer(0));
        contInterior.put("jPanel1", new Rectangle(0, 0, 400, 300));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jPanel1", new Rectangle(0, 0, 400, 300));
        baselinePosition.put("jPanel1-400-300", new Integer(0));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }

    // Resize (shrink) panel from top-left corner.
    public void doChanges3() {
        // > START RESIZING
        baselinePosition.put("jPanel1-400-300", new Integer(0));
        compMinSize.put("jPanel1", new Dimension(0, 0));
        {
            String[] compIds = new String[] {
                "jPanel1"
                };
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(0, 0, 400, 300)
                };
            Point hotspot = new Point(-3,-2);
            int[] resizeEdges = new int[] {
                0,
                    0
                };
            boolean inLayout = true;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
        // < START RESIZING
        prefPaddingInParent.put("Form-jPanel1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jPanel1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        // > MOVE
        {
            Point p = new Point(279,206);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(282, 208, 118, 92)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPaddingInParent.put("Form-jPanel1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jPanel1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        // > MOVE
        {
            Point p = new Point(280,206);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(283, 208, 117, 92)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        prefPaddingInParent.put("Form-jPanel1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jPanel1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        contInterior.put("jPanel1", new Rectangle(-32485, -32560, 117, 92));
        compBounds.put("jPanel1", new Rectangle(-32485, -32560, 117, 92));
        baselinePosition.put("jPanel1-117-92", new Integer(0));
        compMinSize.put("jPanel1", new Dimension(0, 0));
        compPrefSize.put("jPanel1", new Dimension(400, 300));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("jPanel1", new Rectangle(283, 208, 117, 92));
        compBounds.put("jPanel1", new Rectangle(283, 208, 117, 92));
        baselinePosition.put("jPanel1-117-92", new Integer(0));
        compMinSize.put("jPanel1", new Dimension(0, 0));
        compPrefSize.put("jPanel1", new Dimension(117, 92));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jPanel1", new Rectangle(283, 208, 117, 92));
        baselinePosition.put("jPanel1-117-92", new Integer(0));
        contInterior.put("jPanel1", new Rectangle(283, 208, 117, 92));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jPanel1", new Rectangle(283, 208, 117, 92));
        baselinePosition.put("jPanel1-117-92", new Integer(0));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }

    // Resize bottom-right corner to have preferred gaps.
    public void doChanges4() {
        // > START RESIZING
        baselinePosition.put("jPanel1-117-92", new Integer(0));
        compMinSize.put("jPanel1", new Dimension(0, 0));
        {
            String[] compIds = new String[] {
                "jPanel1"
                };
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(283, 208, 117, 92)
                };
            Point hotspot = new Point(401,302);
            int[] resizeEdges = new int[] {
                1,
                    1
                };
            boolean inLayout = true;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
        // < START RESIZING
        prefPaddingInParent.put("Form-jPanel1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jPanel1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        // > MOVE
        {
            Point p = new Point(392,291);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(283, 208, 107, 81)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPaddingInParent.put("Form-jPanel1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jPanel1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        // > MOVE
        {
            Point p = new Point(391,291);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(283, 208, 107, 81)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        prefPaddingInParent.put("Form-jPanel1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jPanel1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        contInterior.put("jPanel1", new Rectangle(-32485, -32560, 107, 81));
        compBounds.put("jPanel1", new Rectangle(-32485, -32560, 107, 81));
        baselinePosition.put("jPanel1-107-81", new Integer(0));
        compMinSize.put("jPanel1", new Dimension(0, 0));
        compPrefSize.put("jPanel1", new Dimension(117, 92));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("jPanel1", new Rectangle(283, 208, 107, 81));
        compBounds.put("jPanel1", new Rectangle(283, 208, 107, 81));
        baselinePosition.put("jPanel1-107-81", new Integer(0));
        compMinSize.put("jPanel1", new Dimension(0, 0));
        compPrefSize.put("jPanel1", new Dimension(107, 81));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jPanel1", new Rectangle(283, 208, 107, 81));
        baselinePosition.put("jPanel1-107-81", new Integer(0));
        contInterior.put("jPanel1", new Rectangle(283, 208, 107, 81));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jPanel1", new Rectangle(283, 208, 107, 81));
        baselinePosition.put("jPanel1-107-81", new Integer(0));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }
    
}
