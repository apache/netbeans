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

// Tests vertical structure reduced just to one parallel group with baseline
// alignment, no gaps around.
public class ALT_Bug70904Test extends LayoutTestCase {

    public ALT_Bug70904Test(String name) {
        super(name);
        try {
	    className = this.getClass().getName();
	    className = className.substring(className.lastIndexOf('.') + 1, className.length());	    
            startingFormFile = FileUtil.toFileObject(new File(url.getFile() + goldenFilesPath + className + "-StartingForm.form").getCanonicalFile());
        } catch (IOException ioe) {
            fail(ioe.toString());
        }
    }

    // Resize (shrink) whole form both horizontally and vertically so there is
    // no gap.
    public void doChanges0() {
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jLabel1", new Rectangle(0, 3, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        compBounds.put("jTextField1", new Rectangle(38, 0, 59, 20));
        baselinePosition.put("jTextField1-59-20", new Integer(14));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compMinSize.put("Form", new Dimension(97, 20));
        compBounds.put("jLabel1", new Rectangle(0, 3, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compBounds.put("jTextField1", new Rectangle(38, 0, 59, 20));
        baselinePosition.put("jTextField1-59-20", new Integer(14));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
        // > START RESIZING
        baselinePosition.put("Form-400-300", new Integer(-1));
        prefPaddingInParent.put("Form-jTextField1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jTextField1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        {
            String[] compIds = new String[] {
                "Form"
                };
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(0, 0, 400, 300)
                };
            Point hotspot = new Point(400,301);
            int[] resizeEdges = new int[] {
                1,
                    1
                };
            boolean inLayout = false;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
        // < START RESIZING
        // > MOVE
        {
            Point p = new Point(92,24);
            String containerId = null;
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(0, 0, 97, 20)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > MOVE
        {
            Point p = new Point(92,23);
            String containerId = null;
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(0, 0, 97, 20)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        contInterior.put("Form", new Rectangle(0, 0, 97, 20));
        compBounds.put("jLabel1", new Rectangle(0, 3, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        compBounds.put("jTextField1", new Rectangle(38, 0, 59, 20));
        baselinePosition.put("jTextField1-59-20", new Integer(14));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        prefPadding.put("jLabel1-jTextField1-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 97, 20));
        compBounds.put("jLabel1", new Rectangle(0, 3, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        compBounds.put("jTextField1", new Rectangle(38, 0, 59, 20));
        baselinePosition.put("jTextField1-59-20", new Integer(14));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        prefPadding.put("jLabel1-jTextField1-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        contInterior.put("Form", new Rectangle(0, 0, 97, 20));
        compBounds.put("jLabel1", new Rectangle(0, 3, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compBounds.put("jTextField1", new Rectangle(38, 0, 59, 20));
        baselinePosition.put("jTextField1-59-20", new Integer(14));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }

    // Resize textfield to the right (this creates a gap), then down, then to
    // the right to snap to border without gap.
    public void doChanges1() {
        // > START RESIZING
        baselinePosition.put("jTextField1-59-20", new Integer(14));
        compMinSize.put("Form", new Dimension(97, 20));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compMinSize.put("jTextField1", new Dimension(59, 20));
        {
            String[] compIds = new String[] {
                "jTextField1"
                };
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(38, 0, 59, 20)
                };
            Point hotspot = new Point(99,10);
            int[] resizeEdges = new int[] {
                1,
                    -1
                };
            boolean inLayout = true;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
        // < START RESIZING
        prefPaddingInParent.put("Form-jTextField1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        // > MOVE
        {
            Point p = new Point(128,12);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(38, 0, 88, 20)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPaddingInParent.put("Form-jTextField1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        // > MOVE
        {
            Point p = new Point(129,12);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(38, 0, 89, 20)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        prefPadding.put("jLabel1-jTextField1-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jTextField1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 137, 20));
        compBounds.put("jLabel1", new Rectangle(0, 3, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        compBounds.put("jTextField1", new Rectangle(38, 0, 89, 20));
        baselinePosition.put("jTextField1-89-20", new Integer(14));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        prefPadding.put("jLabel1-jTextField1-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jTextField1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        contInterior.put("Form", new Rectangle(0, 0, 137, 20));
        compBounds.put("jLabel1", new Rectangle(0, 3, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compBounds.put("jTextField1", new Rectangle(38, 0, 89, 20));
        baselinePosition.put("jTextField1-89-20", new Integer(14));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
        // > START RESIZING
        baselinePosition.put("jTextField1-89-20", new Integer(14));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        {
            String[] compIds = new String[] {
                "jTextField1"
                };
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(38, 0, 89, 20)
                };
            Point hotspot = new Point(80,20);
            int[] resizeEdges = new int[] {
                -1,
                    1
                };
            boolean inLayout = true;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
        // < START RESIZING
        prefPaddingInParent.put("Form-jTextField1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        // > MOVE
        {
            Point p = new Point(80,38);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(38, 0, 89, 38)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPaddingInParent.put("Form-jTextField1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        // > MOVE
        {
            Point p = new Point(80,39);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(38, 0, 89, 39)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        prefPadding.put("jLabel1-jTextField1-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jTextField1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 137, 39));
        compBounds.put("jLabel1", new Rectangle(0, 12, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        compBounds.put("jTextField1", new Rectangle(38, 0, 89, 39));
        baselinePosition.put("jTextField1-89-39", new Integer(23));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        prefPadding.put("jLabel1-jTextField1-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jTextField1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        contInterior.put("Form", new Rectangle(0, 0, 137, 39));
        compBounds.put("jLabel1", new Rectangle(0, 12, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compBounds.put("jTextField1", new Rectangle(38, 0, 89, 39));
        baselinePosition.put("jTextField1-89-39", new Integer(23));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
        // > START RESIZING
        baselinePosition.put("jTextField1-89-39", new Integer(23));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        {
            String[] compIds = new String[] {
                "jTextField1"
                };
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(38, 0, 89, 39)
                };
            Point hotspot = new Point(126,22);
            int[] resizeEdges = new int[] {
                1,
                    -1
                };
            boolean inLayout = true;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
        // < START RESIZING
        prefPaddingInParent.put("Form-jTextField1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        // > MOVE
        {
            Point p = new Point(133,23);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(38, 0, 99, 39)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPaddingInParent.put("Form-jTextField1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        // > MOVE
        {
            Point p = new Point(134,23);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(38, 0, 99, 39)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 137, 39));
        compBounds.put("jLabel1", new Rectangle(0, 12, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        compBounds.put("jTextField1", new Rectangle(38, 0, 99, 39));
        baselinePosition.put("jTextField1-99-39", new Integer(23));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        contInterior.put("Form", new Rectangle(0, 0, 137, 39));
        compBounds.put("jLabel1", new Rectangle(0, 12, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compBounds.put("jTextField1", new Rectangle(38, 0, 99, 39));
        baselinePosition.put("jTextField1-99-39", new Integer(23));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }
    
}
