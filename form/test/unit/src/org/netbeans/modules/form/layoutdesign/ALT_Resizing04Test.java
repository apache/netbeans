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
import java.util.HashMap;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.form.RADComponent;

public class ALT_Resizing04Test extends LayoutTestCase {

    public ALT_Resizing04Test(String name) {
        super(name);
        try {
	    className = this.getClass().getName();
	    className = className.substring(className.lastIndexOf('.') + 1, className.length());	    
            startingFormFile = FileUtil.toFileObject(new File(url.getFile() + goldenFilesPath + className + "-StartingForm.form").getCanonicalFile());
        } catch (IOException ioe) {
            fail(ioe.toString());
        }
    }

    // Resize jTree1/jScrollPane1 slightly to the right.
    public void doChanges0() {
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("jPanel1", new Rectangle(93, 84, 100, 100));
        compBounds.put("jPanel1", new Rectangle(93, 84, 100, 100));
        baselinePosition.put("jPanel1-100-100", new Integer(0));
        compMinSize.put("jPanel1", new Dimension(0, 0));
        compPrefSize.put("jPanel1", new Dimension(100, 100));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        contInterior.put("Form", new Rectangle(0, 0, 404, 400));
        compBounds.put("jScrollPane1", new Rectangle(10, 11, 77, 291));
        baselinePosition.put("jScrollPane1-77-291", new Integer(0));
        compPrefSize.put("jScrollPane1", new Dimension(77, 324));
        compBounds.put("jTextField1", new Rectangle(93, 11, 209, 20));
        baselinePosition.put("jTextField1-209-20", new Integer(14));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compBounds.put("jPanel1", new Rectangle(93, 84, 100, 100));
        baselinePosition.put("jPanel1-100-100", new Integer(0));
        compBounds.put("jButton1", new Rectangle(10, 308, 75, 23));
        baselinePosition.put("jButton1-75-23", new Integer(15));
        compPrefSize.put("jButton1", new Dimension(75, 23));
        compBounds.put("jButton2", new Rectangle(10, 337, 75, 23));
        baselinePosition.put("jButton2-75-23", new Integer(15));
        compPrefSize.put("jButton2", new Dimension(75, 23));
        compBounds.put("jButton3", new Rectangle(10, 366, 75, 23));
        baselinePosition.put("jButton3-75-23", new Integer(15));
        compPrefSize.put("jButton3", new Dimension(75, 23));
        prefPadding.put("jScrollPane1-jTextField1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jPanel1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jButton1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton2-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton3-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollPane1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollPane1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jTextField1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jScrollPane1-jButton1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPanel1-jButton1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jButton2-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jButton3-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jButton3-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
        // > START RESIZING
        {
            String[] compIds = new String[] {
                "jScrollPane1"
                };
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(10, 11, 77, 291)
                };
            Point hotspot = new Point(84,154);
            int[] resizeEdges = new int[] {
                1
                    ,-1
                };
            boolean inLayout = true;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
        baselinePosition.put("jScrollPane1-77-291", new Integer(0));
        compPrefSize.put("jScrollPane1", new Dimension(77, 324));
        // < START RESIZING
        // > MOVE
        {
            Point p = new Point(111,156);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(10, 11, 104, 291)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > MOVE
        {
            Point p = new Point(112,156);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(10, 11, 105, 291)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        prefPadding.put("jScrollPane1-jButton1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("jPanel1", new Rectangle(121, 84, 100, 100));
        compBounds.put("jPanel1", new Rectangle(121, 84, 100, 100));
        baselinePosition.put("jPanel1-100-100", new Integer(0));
        compMinSize.put("jPanel1", new Dimension(0, 0));
        compPrefSize.put("jPanel1", new Dimension(100, 100));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        contInterior.put("Form", new Rectangle(0, 0, 404, 400));
        compBounds.put("jTextField1", new Rectangle(121, 11, 209, 20));
        baselinePosition.put("jTextField1-209-20", new Integer(14));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compBounds.put("jPanel1", new Rectangle(121, 84, 100, 100));
        baselinePosition.put("jPanel1-100-100", new Integer(0));
        compBounds.put("jButton1", new Rectangle(10, 308, 75, 23));
        baselinePosition.put("jButton1-75-23", new Integer(15));
        compPrefSize.put("jButton1", new Dimension(75, 23));
        compBounds.put("jButton2", new Rectangle(10, 337, 75, 23));
        baselinePosition.put("jButton2-75-23", new Integer(15));
        compPrefSize.put("jButton2", new Dimension(75, 23));
        compBounds.put("jButton3", new Rectangle(10, 366, 75, 23));
        baselinePosition.put("jButton3-75-23", new Integer(15));
        compPrefSize.put("jButton3", new Dimension(75, 23));
        compBounds.put("jScrollPane1", new Rectangle(10, 11, 105, 291));
        baselinePosition.put("jScrollPane1-105-291", new Integer(0));
        compPrefSize.put("jScrollPane1", new Dimension(77, 324));
        prefPadding.put("jScrollPane1-jTextField1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jPanel1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jButton1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton2-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton3-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollPane1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton2-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton3-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jTextField1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jPanel1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollPane1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jTextField1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jScrollPane1-jButton1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPanel1-jButton1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jButton2-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jButton3-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jButton3-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }

    // Resize JPanel down to snap to container border.
    public void doChanges1() {
        // > START RESIZING
        {
            String[] compIds = new String[] {
                "jPanel1"
                };
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(121, 84, 100, 100)
                };
            Point hotspot = new Point(164,181);
            int[] resizeEdges = new int[] {
                -1
                    ,1
                };
            boolean inLayout = true;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
        baselinePosition.put("jPanel1-100-100", new Integer(0));
        compMinSize.put("jPanel1", new Dimension(0, 0));
        // < START RESIZING
        prefPaddingInParent.put("Form-jPanel1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        // > MOVE
        {
            Point p = new Point(165,384);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(121, 84, 100, 305)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPaddingInParent.put("Form-jPanel1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        // > MOVE
        {
            Point p = new Point(166,384);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(121, 84, 100, 305)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        prefPadding.put("jTextField1-jPanel1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jButton3-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jButton1-jPanel1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jPanel1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton3-jPanel1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jPanel1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        contInterior.put("jPanel1", new Rectangle(121, 84, 100, 305));
        compBounds.put("jPanel1", new Rectangle(121, 84, 100, 305));
        baselinePosition.put("jPanel1-100-305", new Integer(0));
        compMinSize.put("jPanel1", new Dimension(0, 0));
        compPrefSize.put("jPanel1", new Dimension(100, 100));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("jPanel1", new Rectangle(121, 84, 100, 305));
        compBounds.put("jPanel1", new Rectangle(121, 84, 100, 305));
        baselinePosition.put("jPanel1-100-305", new Integer(0));
        compMinSize.put("jPanel1", new Dimension(0, 0));
        compPrefSize.put("jPanel1", new Dimension(100, 305));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        contInterior.put("Form", new Rectangle(0, 0, 404, 400));
        compBounds.put("jTextField1", new Rectangle(121, 11, 209, 20));
        baselinePosition.put("jTextField1-209-20", new Integer(14));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compBounds.put("jButton1", new Rectangle(10, 308, 75, 23));
        baselinePosition.put("jButton1-75-23", new Integer(15));
        compPrefSize.put("jButton1", new Dimension(75, 23));
        compBounds.put("jButton2", new Rectangle(10, 337, 75, 23));
        baselinePosition.put("jButton2-75-23", new Integer(15));
        compPrefSize.put("jButton2", new Dimension(75, 23));
        compBounds.put("jButton3", new Rectangle(10, 366, 75, 23));
        baselinePosition.put("jButton3-75-23", new Integer(15));
        compPrefSize.put("jButton3", new Dimension(75, 23));
        compBounds.put("jScrollPane1", new Rectangle(10, 11, 105, 291));
        baselinePosition.put("jScrollPane1-105-291", new Integer(0));
        compPrefSize.put("jScrollPane1", new Dimension(77, 324));
        compBounds.put("jPanel1", new Rectangle(121, 84, 100, 305));
        baselinePosition.put("jPanel1-100-305", new Integer(0));
        prefPaddingInParent.put("Form-jScrollPane1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton2-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton3-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jScrollPane1-jTextField1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jPanel1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jTextField1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jPanel1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jTextField1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jPanel1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton3-jTextField1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton3-jPanel1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jButton1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jButton2-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jButton3-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jScrollPane1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jTextField1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton3-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jPanel1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }

    // Resize JPanel's bottom edge to align with bottom edge of jButton2.
    public void doChanges2() {
        // > START RESIZING
        {
            String[] compIds = new String[] {
                "jPanel1"
                };
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(121, 84, 100, 305)
                };
            Point hotspot = new Point(169,388);
            int[] resizeEdges = new int[] {
                -1
                    ,1
                };
            boolean inLayout = true;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
        baselinePosition.put("jPanel1-100-305", new Integer(0));
        compMinSize.put("jPanel1", new Dimension(0, 0));
        // < START RESIZING
        prefPaddingInParent.put("Form-jPanel1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        // > MOVE
        {
            Point p = new Point(168,367);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(121, 84, 100, 284)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPaddingInParent.put("Form-jPanel1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        // > MOVE
        {
            Point p = new Point(168,366);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(121, 84, 100, 276)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        prefPadding.put("jTextField1-jPanel1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jPanel1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        contInterior.put("jPanel1", new Rectangle(121, 84, 100, 276));
        compBounds.put("jPanel1", new Rectangle(121, 84, 100, 276));
        baselinePosition.put("jPanel1-100-276", new Integer(0));
        compMinSize.put("jPanel1", new Dimension(0, 0));
        compPrefSize.put("jPanel1", new Dimension(100, 305));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        prefPaddingInParent.put("Form-jScrollPane1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jTextField1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jScrollPane1-jButton1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        compPrefSize.put("jButton1", new Dimension(75, 23));
        prefPadding.put("jButton1-jButton2-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        compPrefSize.put("jButton2", new Dimension(75, 23));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compPrefSize.put("jPanel1", new Dimension(100, 305));
        prefPadding.put("jButton2-jButton3-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPanel1-jButton3-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        compPrefSize.put("jButton3", new Dimension(75, 23));
        prefPaddingInParent.put("Form-jButton3-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("jPanel1", new Rectangle(121, 84, 100, 276));
        compBounds.put("jPanel1", new Rectangle(121, 84, 100, 276));
        baselinePosition.put("jPanel1-100-276", new Integer(0));
        compMinSize.put("jPanel1", new Dimension(0, 0));
        compPrefSize.put("jPanel1", new Dimension(100, 276));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        contInterior.put("Form", new Rectangle(0, 0, 404, 400));
        compBounds.put("jTextField1", new Rectangle(121, 11, 209, 20));
        baselinePosition.put("jTextField1-209-20", new Integer(14));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compBounds.put("jButton1", new Rectangle(10, 308, 75, 23));
        baselinePosition.put("jButton1-75-23", new Integer(15));
        compPrefSize.put("jButton1", new Dimension(75, 23));
        compBounds.put("jButton2", new Rectangle(10, 337, 75, 23));
        baselinePosition.put("jButton2-75-23", new Integer(15));
        compPrefSize.put("jButton2", new Dimension(75, 23));
        compBounds.put("jButton3", new Rectangle(10, 366, 75, 23));
        baselinePosition.put("jButton3-75-23", new Integer(15));
        compPrefSize.put("jButton3", new Dimension(75, 23));
        compBounds.put("jScrollPane1", new Rectangle(10, 11, 105, 291));
        baselinePosition.put("jScrollPane1-105-291", new Integer(0));
        compPrefSize.put("jScrollPane1", new Dimension(77, 324));
        compBounds.put("jPanel1", new Rectangle(121, 84, 100, 276));
        baselinePosition.put("jPanel1-100-276", new Integer(0));
        prefPaddingInParent.put("Form-jScrollPane1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton2-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton3-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jScrollPane1-jTextField1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jPanel1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jTextField1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jPanel1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jTextField1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jPanel1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton3-jTextField1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton3-jPanel1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jButton1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jButton2-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jScrollPane1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jTextField1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jButton2-jButton3-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPanel1-jButton3-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jButton3-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }

    // Resize JPanel's bottom edge to align with bottom edge of jButton1.
    public void doChanges3() {
        // > START RESIZING
        {
            String[] compIds = new String[] {
                "jPanel1"
                };
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(121, 84, 100, 276)
                };
            Point hotspot = new Point(168,364);
            int[] resizeEdges = new int[] {
                -1
                    ,1
                };
            boolean inLayout = true;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
        baselinePosition.put("jPanel1-100-276", new Integer(0));
        compMinSize.put("jPanel1", new Dimension(0, 0));
        // < START RESIZING
        prefPaddingInParent.put("Form-jPanel1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        // > MOVE
        {
            Point p = new Point(164,341);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(121, 84, 100, 247)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPaddingInParent.put("Form-jPanel1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        // > MOVE
        {
            Point p = new Point(164,340);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(121, 84, 100, 247)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        prefPadding.put("jTextField1-jPanel1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jPanel1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        contInterior.put("jPanel1", new Rectangle(121, 84, 100, 247));
        compBounds.put("jPanel1", new Rectangle(121, 84, 100, 247));
        baselinePosition.put("jPanel1-100-247", new Integer(0));
        compMinSize.put("jPanel1", new Dimension(0, 0));
        compPrefSize.put("jPanel1", new Dimension(100, 276));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        prefPaddingInParent.put("Form-jScrollPane1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jTextField1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jScrollPane1-jButton1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        compPrefSize.put("jButton1", new Dimension(75, 23));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compPrefSize.put("jPanel1", new Dimension(100, 276));
        prefPadding.put("jButton1-jButton2-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPanel1-jButton2-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        compPrefSize.put("jButton2", new Dimension(75, 23));
        prefPadding.put("jButton2-jButton3-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        compPrefSize.put("jButton3", new Dimension(75, 23));
        prefPaddingInParent.put("Form-jButton3-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("jPanel1", new Rectangle(121, 84, 100, 247));
        compBounds.put("jPanel1", new Rectangle(121, 84, 100, 247));
        baselinePosition.put("jPanel1-100-247", new Integer(0));
        compMinSize.put("jPanel1", new Dimension(0, 0));
        compPrefSize.put("jPanel1", new Dimension(100, 247));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        contInterior.put("Form", new Rectangle(0, 0, 404, 400));
        compBounds.put("jTextField1", new Rectangle(121, 11, 209, 20));
        baselinePosition.put("jTextField1-209-20", new Integer(14));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compBounds.put("jButton1", new Rectangle(10, 308, 75, 23));
        baselinePosition.put("jButton1-75-23", new Integer(15));
        compPrefSize.put("jButton1", new Dimension(75, 23));
        compBounds.put("jButton2", new Rectangle(10, 337, 75, 23));
        baselinePosition.put("jButton2-75-23", new Integer(15));
        compPrefSize.put("jButton2", new Dimension(75, 23));
        compBounds.put("jButton3", new Rectangle(10, 366, 75, 23));
        baselinePosition.put("jButton3-75-23", new Integer(15));
        compPrefSize.put("jButton3", new Dimension(75, 23));
        compBounds.put("jScrollPane1", new Rectangle(10, 11, 105, 291));
        baselinePosition.put("jScrollPane1-105-291", new Integer(0));
        compPrefSize.put("jScrollPane1", new Dimension(77, 324));
        compBounds.put("jPanel1", new Rectangle(121, 84, 100, 247));
        baselinePosition.put("jPanel1-100-247", new Integer(0));
        prefPaddingInParent.put("Form-jScrollPane1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton2-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton3-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jScrollPane1-jTextField1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jPanel1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jTextField1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jPanel1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jTextField1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jPanel1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton3-jTextField1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton3-jPanel1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jButton1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jScrollPane1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jTextField1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jButton1-jButton2-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPanel1-jButton2-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jButton3-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jButton3-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }

    // Resize JPanel's bottom edge to align with bottom edge of jTree1/jScrollPane1.
    public void doChanges4() {
        // > START RESIZING
        {
            String[] compIds = new String[] {
                "jPanel1"
                };
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(121, 84, 100, 247)
                };
            Point hotspot = new Point(169,335);
            int[] resizeEdges = new int[] {
                -1
                    ,1
                };
            boolean inLayout = true;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
        baselinePosition.put("jPanel1-100-247", new Integer(0));
        compMinSize.put("jPanel1", new Dimension(0, 0));
        // < START RESIZING
        prefPaddingInParent.put("Form-jPanel1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        // > MOVE
        {
            Point p = new Point(160,311);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(121, 84, 100, 218)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPaddingInParent.put("Form-jPanel1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        // > MOVE
        {
            Point p = new Point(160,310);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(121, 84, 100, 218)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        prefPadding.put("jTextField1-jPanel1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jPanel1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        contInterior.put("jPanel1", new Rectangle(121, 84, 100, 218));
        compBounds.put("jPanel1", new Rectangle(121, 84, 100, 218));
        baselinePosition.put("jPanel1-100-218", new Integer(0));
        compMinSize.put("jPanel1", new Dimension(0, 0));
        compPrefSize.put("jPanel1", new Dimension(100, 247));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        prefPaddingInParent.put("Form-jScrollPane1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jTextField1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compPrefSize.put("jPanel1", new Dimension(100, 247));
        prefPadding.put("jScrollPane1-jButton1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPanel1-jButton1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        compPrefSize.put("jButton1", new Dimension(75, 23));
        prefPadding.put("jButton1-jButton2-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        compPrefSize.put("jButton2", new Dimension(75, 23));
        prefPadding.put("jButton2-jButton3-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        compPrefSize.put("jButton3", new Dimension(75, 23));
        prefPaddingInParent.put("Form-jButton3-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("jPanel1", new Rectangle(121, 84, 100, 218));
        compBounds.put("jPanel1", new Rectangle(121, 84, 100, 218));
        baselinePosition.put("jPanel1-100-218", new Integer(0));
        compMinSize.put("jPanel1", new Dimension(0, 0));
        compPrefSize.put("jPanel1", new Dimension(100, 218));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        contInterior.put("Form", new Rectangle(0, 0, 404, 400));
        compBounds.put("jTextField1", new Rectangle(121, 11, 209, 20));
        baselinePosition.put("jTextField1-209-20", new Integer(14));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compBounds.put("jButton1", new Rectangle(10, 308, 75, 23));
        baselinePosition.put("jButton1-75-23", new Integer(15));
        compPrefSize.put("jButton1", new Dimension(75, 23));
        compBounds.put("jButton2", new Rectangle(10, 337, 75, 23));
        baselinePosition.put("jButton2-75-23", new Integer(15));
        compPrefSize.put("jButton2", new Dimension(75, 23));
        compBounds.put("jButton3", new Rectangle(10, 366, 75, 23));
        baselinePosition.put("jButton3-75-23", new Integer(15));
        compPrefSize.put("jButton3", new Dimension(75, 23));
        compBounds.put("jScrollPane1", new Rectangle(10, 11, 105, 291));
        baselinePosition.put("jScrollPane1-105-291", new Integer(0));
        compPrefSize.put("jScrollPane1", new Dimension(77, 324));
        compBounds.put("jPanel1", new Rectangle(121, 84, 100, 218));
        baselinePosition.put("jPanel1-100-218", new Integer(0));
        prefPaddingInParent.put("Form-jScrollPane1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton2-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton3-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jScrollPane1-jTextField1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jPanel1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jTextField1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jPanel1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jTextField1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jPanel1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton3-jTextField1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton3-jPanel1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jScrollPane1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jTextField1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jScrollPane1-jButton1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPanel1-jButton1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jButton2-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jButton3-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jButton3-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }

    // Resize JPanel down and right to snap to container border at the bottom
    // and to align with jTextField1 at the right edge.
    public void doChanges5() {
        // > START RESIZING
        {
            String[] compIds = new String[] {
                "jPanel1"
                };
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(121, 84, 100, 218)
                };
            Point hotspot = new Point(220,305);
            int[] resizeEdges = new int[] {
                1
                    ,1
                };
            boolean inLayout = true;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
        baselinePosition.put("jPanel1-100-218", new Integer(0));
        compMinSize.put("jPanel1", new Dimension(0, 0));
        // < START RESIZING
        prefPaddingInParent.put("Form-jPanel1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jPanel1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        // > MOVE
        {
            Point p = new Point(326,385);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(121, 84, 209, 305)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPaddingInParent.put("Form-jPanel1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jPanel1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        // > MOVE
        {
            Point p = new Point(327,385);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(121, 84, 209, 305)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        compPrefSize.put("jPanel1", new Dimension(100, 218));
        prefPadding.put("jTextField1-jPanel1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jButton3-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        contInterior.put("jPanel1", new Rectangle(121, 84, 209, 305));
        compBounds.put("jPanel1", new Rectangle(121, 84, 209, 305));
        baselinePosition.put("jPanel1-209-305", new Integer(0));
        compMinSize.put("jPanel1", new Dimension(0, 0));
        compPrefSize.put("jPanel1", new Dimension(100, 218));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("jPanel1", new Rectangle(121, 84, 209, 305));
        compBounds.put("jPanel1", new Rectangle(121, 84, 209, 305));
        baselinePosition.put("jPanel1-209-305", new Integer(0));
        compMinSize.put("jPanel1", new Dimension(0, 0));
        compPrefSize.put("jPanel1", new Dimension(209, 305));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        contInterior.put("Form", new Rectangle(0, 0, 404, 400));
        compBounds.put("jTextField1", new Rectangle(121, 11, 209, 20));
        baselinePosition.put("jTextField1-209-20", new Integer(14));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compBounds.put("jButton1", new Rectangle(10, 308, 75, 23));
        baselinePosition.put("jButton1-75-23", new Integer(15));
        compPrefSize.put("jButton1", new Dimension(75, 23));
        compBounds.put("jButton2", new Rectangle(10, 337, 75, 23));
        baselinePosition.put("jButton2-75-23", new Integer(15));
        compPrefSize.put("jButton2", new Dimension(75, 23));
        compBounds.put("jButton3", new Rectangle(10, 366, 75, 23));
        baselinePosition.put("jButton3-75-23", new Integer(15));
        compPrefSize.put("jButton3", new Dimension(75, 23));
        compBounds.put("jScrollPane1", new Rectangle(10, 11, 105, 291));
        baselinePosition.put("jScrollPane1-105-291", new Integer(0));
        compPrefSize.put("jScrollPane1", new Dimension(77, 324));
        compBounds.put("jPanel1", new Rectangle(121, 84, 209, 305));
        baselinePosition.put("jPanel1-209-305", new Integer(0));
        prefPaddingInParent.put("Form-jScrollPane1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton2-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton3-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jScrollPane1-jPanel1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jTextField1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jPanel1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jTextField1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jPanel1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jTextField1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton3-jPanel1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton3-jTextField1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jButton1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jButton2-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton2-jButton3-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jScrollPane1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jTextField1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton3-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jPanel1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }
    
}
