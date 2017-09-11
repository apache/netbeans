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

public class ALT_Resizing03Test extends LayoutTestCase {

    public ALT_Resizing03Test(String name) {
        super(name);
        try {
	    className = this.getClass().getName();
	    className = className.substring(className.lastIndexOf('.') + 1, className.length());	    
            startingFormFile = FileUtil.toFileObject(new File(url.getFile() + goldenFilesPath + className + "-StartingForm.form").getCanonicalFile());
        } catch (IOException ioe) {
            fail(ioe.toString());
        }
    }

    // Resize JInternalFrame downwards to snap to container border.
    public void doChanges0() {
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("jInternalFrame1", new Rectangle(13, 54, 18, 0));
        compBounds.put("jInternalFrame1", new Rectangle(10, 31, 24, 26));
        baselinePosition.put("jInternalFrame1-24-26", new Integer(-1));
        compMinSize.put("jInternalFrame1", new Dimension(30, 26));
        compPrefSize.put("jInternalFrame1", new Dimension(22, 26));
        hasExplicitPrefSize.put("jInternalFrame1", new Boolean(false));
        hasExplicitPrefSize.put("jInternalFrame1", new Boolean(false));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jLabel1", new Rectangle(10, 11, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        compBounds.put("jInternalFrame1", new Rectangle(10, 31, 24, 26));
        baselinePosition.put("jInternalFrame1-24-26", new Integer(-1));
        compBounds.put("jScrollPane1", new Rectangle(286, 11, 104, 64));
        baselinePosition.put("jScrollPane1-104-64", new Integer(0));
        compPrefSize.put("jScrollPane1", new Dimension(104, 64));
        compBounds.put("jButton1", new Rectangle(315, 266, 75, 23));
        baselinePosition.put("jButton1-75-23", new Integer(15));
        compPrefSize.put("jButton1", new Dimension(75, 23));
        compBounds.put("jButton2", new Rectangle(234, 266, 75, 23));
        baselinePosition.put("jButton2-75-23", new Integer(15));
        compPrefSize.put("jButton2", new Dimension(75, 23));
        prefPadding.put("jButton2-jButton1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jButton2-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jInternalFrame1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollPane1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jLabel1-jInternalFrame1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jScrollPane1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
        // > START RESIZING
        {
            String[] compIds = new String[] {
                "jInternalFrame1"
                };
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(10, 31, 24, 26)
                };
            Point hotspot = new Point(24,56);
            int[] resizeEdges = new int[] {
                -1
                    ,1
                };
            boolean inLayout = true;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
        baselinePosition.put("jInternalFrame1-24-26", new Integer(-1));
        compMinSize.put("jInternalFrame1", new Dimension(30, 26));
        // < START RESIZING
        prefPaddingInParent.put("Form-jInternalFrame1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        // > MOVE
        {
            Point p = new Point(33,283);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(10, 31, 24, 258)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPaddingInParent.put("Form-jInternalFrame1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        // > MOVE
        {
            Point p = new Point(32,283);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(10, 31, 24, 258)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        prefPadding.put("jInternalFrame1-jButton2-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jInternalFrame1-jScrollPane1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jInternalFrame1-jButton2-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        contInterior.put("jInternalFrame1", new Rectangle(13, 54, 18, 232));
        compBounds.put("jInternalFrame1", new Rectangle(10, 31, 24, 258));
        baselinePosition.put("jInternalFrame1-24-258", new Integer(-1));
        compMinSize.put("jInternalFrame1", new Dimension(30, 26));
        compPrefSize.put("jInternalFrame1", new Dimension(22, 26));
        hasExplicitPrefSize.put("jInternalFrame1", new Boolean(false));
        hasExplicitPrefSize.put("jInternalFrame1", new Boolean(false));
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("jInternalFrame1", new Rectangle(13, 54, 18, 232));
        compBounds.put("jInternalFrame1", new Rectangle(10, 31, 24, 258));
        baselinePosition.put("jInternalFrame1-24-258", new Integer(-1));
        compMinSize.put("jInternalFrame1", new Dimension(30, 26));
        compPrefSize.put("jInternalFrame1", new Dimension(22, 258));
        hasExplicitPrefSize.put("jInternalFrame1", new Boolean(false));
        hasExplicitPrefSize.put("jInternalFrame1", new Boolean(false));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jLabel1", new Rectangle(10, 11, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        compBounds.put("jScrollPane1", new Rectangle(286, 11, 104, 64));
        baselinePosition.put("jScrollPane1-104-64", new Integer(0));
        compPrefSize.put("jScrollPane1", new Dimension(104, 64));
        compBounds.put("jButton1", new Rectangle(315, 266, 75, 23));
        baselinePosition.put("jButton1-75-23", new Integer(15));
        compPrefSize.put("jButton1", new Dimension(75, 23));
        compBounds.put("jButton2", new Rectangle(234, 266, 75, 23));
        baselinePosition.put("jButton2-75-23", new Integer(15));
        compPrefSize.put("jButton2", new Dimension(75, 23));
        compBounds.put("jInternalFrame1", new Rectangle(10, 31, 24, 258));
        baselinePosition.put("jInternalFrame1-24-258", new Integer(-1));
        prefPaddingInParent.put("Form-jLabel1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jInternalFrame1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jButton2-jButton1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jScrollPane1-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jButton2-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jInternalFrame1-jScrollPane1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jInternalFrame1-jButton2-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jScrollPane1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jLabel1-jInternalFrame1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jInternalFrame1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollPane1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }
    
}
