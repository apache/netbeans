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

// Testing correct parallel merging in vertical dimension. The layout is
// "screwed" by jLabel1 which is in parallel to everything. Tests thoroughly
// mergeParallelInclusions method, all indices must match, including the case
// when superfluous gap is eliminated.
public class ALT_Bug69497Test extends LayoutTestCase {
        
    public ALT_Bug69497Test(String name) {
        super(name);
        try {
	    className = this.getClass().getName();
	    className = className.substring(className.lastIndexOf('.') + 1, className.length());	    
            startingFormFile = FileUtil.toFileObject(new File(url.getFile() + goldenFilesPath + className + "-StartingForm.form").getCanonicalFile());
        } catch (IOException ioe) {
            fail(ioe.toString());
        }
    }

    // Call Date (jLabel7) originally on baseline with couponField textfield is
    // moved left and up to top-align with couponField.
    public void doChanges0() {
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("myJPanel", new Rectangle(0, 0, 203, 183));
        compBounds.put("myJPanel", new Rectangle(0, 0, 203, 183));
        compPrefSize.put("myJPanel", new Dimension(203,183));
        compBounds.put("jLabel7", new Rectangle(27, 104, 62, 15));
        baselinePosition.put("jLabel7-62-15", new Integer(11));
        compPrefSize.put("jLabel7", new Dimension(43, 14));
        compBounds.put("jLabel1", new Rectangle(10, 11, 93, 14));
        baselinePosition.put("jLabel1-93-14", new Integer(11));
        compPrefSize.put("jLabel1", new Dimension(70, 14));
        compBounds.put("jLabel3", new Rectangle(10, 35, 93, 14));
        baselinePosition.put("jLabel3-93-14", new Integer(11));
        compPrefSize.put("jLabel3", new Dimension(70, 14));
        compBounds.put("jLabel5", new Rectangle(10, 55, 90, 14));
        baselinePosition.put("jLabel5-90-14", new Integer(11));
        compPrefSize.put("jLabel5", new Dimension(66, 14));
        compBounds.put("jLabel2", new Rectangle(10, 78, 54, 14));
        baselinePosition.put("jLabel2-54-14", new Integer(11));
        compPrefSize.put("jLabel2", new Dimension(37, 14));
        compBounds.put("jLabel6", new Rectangle(10, 155, 62, 14));
        baselinePosition.put("jLabel6-62-14", new Integer(11));
        compPrefSize.put("jLabel6", new Dimension(43, 14));
        compBounds.put("callPriceField", new Rectangle(107, 152, 86, 20));
        baselinePosition.put("callPriceField-86-20", new Integer(14));
        compPrefSize.put("callPriceField", new Dimension(86, 20));
        compBounds.put("callDateField", new Rectangle(107, 130, 86, 20));
        baselinePosition.put("callDateField-86-20", new Integer(14));
        compPrefSize.put("callDateField", new Dimension(86, 20));
        compBounds.put("maturityDateField", new Rectangle(107, 75, 86, 20));
        baselinePosition.put("maturityDateField-86-20", new Integer(14));
        compPrefSize.put("maturityDateField", new Dimension(86, 20));
        compBounds.put("couponField", new Rectangle(107, 101, 86, 20));
        baselinePosition.put("couponField-86-20", new Integer(14));
        compPrefSize.put("couponField", new Dimension(86, 20));
        compBounds.put("purchasePriceField", new Rectangle(107, 45, 86, 20));
        baselinePosition.put("purchasePriceField-86-20", new Integer(14));
        compPrefSize.put("purchasePriceField", new Dimension(86, 20));
        compBounds.put("purchaseDateField", new Rectangle(107, 19, 86, 20));
        baselinePosition.put("purchaseDateField-86-20", new Integer(14));
        compPrefSize.put("purchaseDateField", new Dimension(86, 20));
        prefPaddingInParent.put("myJPanel-callPriceField-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("myJPanel-callDateField-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("myJPanel-maturityDateField-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("myJPanel-couponField-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("myJPanel-purchasePriceField-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("myJPanel-purchaseDateField-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("myJPanel-jLabel1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("myJPanel-callPriceField-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("myJPanel-jLabel6-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        contInterior.put("myJPanel", new Rectangle(0, 0, 203, 183));
        compBounds.put("jLabel7", new Rectangle(27, 104, 62, 15));
        baselinePosition.put("jLabel7-62-15", new Integer(11));
        compBounds.put("jLabel1", new Rectangle(10, 11, 93, 14));
        baselinePosition.put("jLabel1-93-14", new Integer(11));
        compBounds.put("jLabel3", new Rectangle(10, 35, 93, 14));
        baselinePosition.put("jLabel3-93-14", new Integer(11));
        compBounds.put("jLabel5", new Rectangle(10, 55, 90, 14));
        baselinePosition.put("jLabel5-90-14", new Integer(11));
        compBounds.put("jLabel2", new Rectangle(10, 78, 54, 14));
        baselinePosition.put("jLabel2-54-14", new Integer(11));
        compBounds.put("jLabel6", new Rectangle(10, 155, 62, 14));
        baselinePosition.put("jLabel6-62-14", new Integer(11));
        compBounds.put("callPriceField", new Rectangle(107, 152, 86, 20));
        baselinePosition.put("callPriceField-86-20", new Integer(14));
        compBounds.put("callDateField", new Rectangle(107, 130, 86, 20));
        baselinePosition.put("callDateField-86-20", new Integer(14));
        compBounds.put("maturityDateField", new Rectangle(107, 75, 86, 20));
        baselinePosition.put("maturityDateField-86-20", new Integer(14));
        compBounds.put("couponField", new Rectangle(107, 101, 86, 20));
        baselinePosition.put("couponField-86-20", new Integer(14));
        compBounds.put("purchasePriceField", new Rectangle(107, 45, 86, 20));
        baselinePosition.put("purchasePriceField-86-20", new Integer(14));
        compBounds.put("purchaseDateField", new Rectangle(107, 19, 86, 20));
        baselinePosition.put("purchaseDateField-86-20", new Integer(14));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
        // > START MOVING
        baselinePosition.put("jLabel7-62-15", new Integer(11));
        {
            String[] compIds = new String[] {
                "jLabel7"
                };
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(27, 104, 62, 15)
                };
            Point hotspot = new Point(50,114);
            ld.startMoving(compIds, bounds, hotspot);
        }
        // < START MOVING
        prefPaddingInParent.put("myJPanel-jLabel7-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("myJPanel-jLabel7-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jLabel3-jLabel7-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel5-jLabel7-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("maturityDateField-jLabel7-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel7-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("callPriceField-jLabel7-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel6-jLabel7-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel7-callPriceField-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel7-jLabel6-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jLabel7-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel7-jLabel1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("myJPanel-jLabel7-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("myJPanel-jLabel7-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jLabel7-couponField-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel7-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        {
            Point p = new Point(35,113);
            String containerId= "myJPanel";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(10, 104, 62, 15)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPaddingInParent.put("myJPanel-jLabel7-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("myJPanel-jLabel7-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jLabel3-jLabel7-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel5-jLabel7-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("maturityDateField-jLabel7-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel7-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("callPriceField-jLabel7-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel6-jLabel7-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel7-callPriceField-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel7-jLabel6-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jLabel7-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel7-jLabel1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("myJPanel-jLabel7-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("myJPanel-jLabel7-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jLabel7-couponField-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel7-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        {
            Point p = new Point(35,112);
            String containerId= "myJPanel";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(10, 101, 62, 15)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        prefPadding.put("jLabel1-callPriceField-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-callDateField-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-maturityDateField-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-couponField-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-purchasePriceField-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-purchaseDateField-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-callPriceField-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-callDateField-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-maturityDateField-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-couponField-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-purchasePriceField-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-purchaseDateField-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel5-callPriceField-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel5-callDateField-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel5-maturityDateField-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel5-couponField-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel5-purchasePriceField-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel5-purchaseDateField-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-callPriceField-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-callDateField-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-maturityDateField-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-couponField-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-purchasePriceField-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-purchaseDateField-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel6-callPriceField-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel6-callDateField-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel6-maturityDateField-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel6-couponField-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel6-purchasePriceField-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel6-purchaseDateField-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel7-callPriceField-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel7-callDateField-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel7-maturityDateField-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel7-couponField-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel7-purchasePriceField-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel7-purchaseDateField-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jLabel7-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel7-callPriceField-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel7-jLabel6-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("myJPanel", new Rectangle(0, 0, 203, 183));
        compBounds.put("jLabel1", new Rectangle(10, 11, 93, 14));
        baselinePosition.put("jLabel1-93-14", new Integer(11));
        compPrefSize.put("jLabel1", new Dimension(70, 14));
        compBounds.put("jLabel3", new Rectangle(10, 35, 93, 14));
        baselinePosition.put("jLabel3-93-14", new Integer(11));
        compPrefSize.put("jLabel3", new Dimension(70, 14));
        compBounds.put("jLabel5", new Rectangle(10, 55, 90, 14));
        baselinePosition.put("jLabel5-90-14", new Integer(11));
        compPrefSize.put("jLabel5", new Dimension(66, 14));
        compBounds.put("jLabel2", new Rectangle(10, 78, 54, 14));
        baselinePosition.put("jLabel2-54-14", new Integer(11));
        compPrefSize.put("jLabel2", new Dimension(37, 14));
        compBounds.put("jLabel6", new Rectangle(10, 155, 62, 14));
        baselinePosition.put("jLabel6-62-14", new Integer(11));
        compPrefSize.put("jLabel6", new Dimension(43, 14));
        compBounds.put("callPriceField", new Rectangle(107, 152, 86, 20));
        baselinePosition.put("callPriceField-86-20", new Integer(14));
        compPrefSize.put("callPriceField", new Dimension(86, 20));
        compBounds.put("callDateField", new Rectangle(107, 130, 86, 20));
        baselinePosition.put("callDateField-86-20", new Integer(14));
        compPrefSize.put("callDateField", new Dimension(86, 20));
        compBounds.put("maturityDateField", new Rectangle(107, 75, 86, 20));
        baselinePosition.put("maturityDateField-86-20", new Integer(14));
        compPrefSize.put("maturityDateField", new Dimension(86, 20));
        compBounds.put("couponField", new Rectangle(107, 101, 86, 20));
        baselinePosition.put("couponField-86-20", new Integer(14));
        compPrefSize.put("couponField", new Dimension(86, 20));
        compBounds.put("purchasePriceField", new Rectangle(107, 45, 86, 20));
        baselinePosition.put("purchasePriceField-86-20", new Integer(14));
        compPrefSize.put("purchasePriceField", new Dimension(86, 20));
        compBounds.put("purchaseDateField", new Rectangle(107, 19, 86, 20));
        baselinePosition.put("purchaseDateField-86-20", new Integer(14));
        compPrefSize.put("purchaseDateField", new Dimension(86, 20));
        compBounds.put("jLabel7", new Rectangle(10, 101, 62, 15));
        baselinePosition.put("jLabel7-62-15", new Integer(11));
        compPrefSize.put("jLabel7", new Dimension(43, 14));
        prefPaddingInParent.put("myJPanel-callPriceField-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("myJPanel-callDateField-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("myJPanel-maturityDateField-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("myJPanel-couponField-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("myJPanel-purchasePriceField-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("myJPanel-purchaseDateField-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("myJPanel-callPriceField-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("myJPanel-jLabel6-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        contInterior.put("myJPanel", new Rectangle(0, 0, 203, 183));
        compBounds.put("jLabel1", new Rectangle(10, 11, 93, 14));
        baselinePosition.put("jLabel1-93-14", new Integer(11));
        compBounds.put("jLabel3", new Rectangle(10, 35, 93, 14));
        baselinePosition.put("jLabel3-93-14", new Integer(11));
        compBounds.put("jLabel5", new Rectangle(10, 55, 90, 14));
        baselinePosition.put("jLabel5-90-14", new Integer(11));
        compBounds.put("jLabel2", new Rectangle(10, 78, 54, 14));
        baselinePosition.put("jLabel2-54-14", new Integer(11));
        compBounds.put("jLabel6", new Rectangle(10, 155, 62, 14));
        baselinePosition.put("jLabel6-62-14", new Integer(11));
        compBounds.put("callPriceField", new Rectangle(107, 152, 86, 20));
        baselinePosition.put("callPriceField-86-20", new Integer(14));
        compBounds.put("callDateField", new Rectangle(107, 130, 86, 20));
        baselinePosition.put("callDateField-86-20", new Integer(14));
        compBounds.put("maturityDateField", new Rectangle(107, 75, 86, 20));
        baselinePosition.put("maturityDateField-86-20", new Integer(14));
        compBounds.put("couponField", new Rectangle(107, 101, 86, 20));
        baselinePosition.put("couponField-86-20", new Integer(14));
        compBounds.put("purchasePriceField", new Rectangle(107, 45, 86, 20));
        baselinePosition.put("purchasePriceField-86-20", new Integer(14));
        compBounds.put("purchaseDateField", new Rectangle(107, 19, 86, 20));
        baselinePosition.put("purchaseDateField-86-20", new Integer(14));
        compBounds.put("jLabel7", new Rectangle(10, 101, 62, 15));
        baselinePosition.put("jLabel7-62-15", new Integer(11));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }
    
}
