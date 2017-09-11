/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of the
 * License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include the
 * License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by Oracle
 * in the GPL Version 2 section of the License file that accompanied this code.
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL or only
 * the GPL Version 2, indicate your decision by adding "[Contributor] elects to
 * include this software in this distribution under the [CDDL or GPL Version 2]
 * license." If you do not indicate a single choice of license, a recipient has
 * the option to distribute your version of this file under either the CDDL, the
 * GPL Version 2 or to extend the choice of license to its licensees as provided
 * above. However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is made
 * subject to such option by the copyright holder.
 */
package org.netbeans.modules.form.layoutdesign;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.*;
import org.openide.filesystems.FileUtil;

public class ALT_Bug203486Test extends LayoutTestCase {

    public ALT_Bug203486Test(String name) {
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
     * Move jScrollPane4 (with jTextArea2) to the right under jScrollPane3
     * (with jList2), snapped at preferred distance from top/right.
     */
    public void doChanges0() {
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 618, 586));
        contInterior.put("Form", new Rectangle(0, 0, 618, 586));
        compBounds.put("jPanel2", new Rectangle(10, 11, 598, 564));
        baselinePosition.put("jPanel2-598-564", new Integer(0));
        contInterior.put("jPanel2", new Rectangle(11, 12, 596, 562));
        compBounds.put("jSeparator1", new Rectangle(11, 48, 596, 18));
        baselinePosition.put("jSeparator1-596-18", new Integer(-1));
        compBounds.put("jProgressBar1", new Rectangle(11, 549, 596, 14));
        baselinePosition.put("jProgressBar1-596-14", new Integer(-1));
        compBounds.put("jButton2", new Rectangle(270, 473, 73, 23));
        baselinePosition.put("jButton2-73-23", new Integer(15));
        compBounds.put("jScrollPane2", new Rectangle(21, 72, 231, 233));
        baselinePosition.put("jScrollPane2-231-233", new Integer(0));
        compBounds.put("jScrollPane3", new Rectangle(475, 72, 122, 167));
        baselinePosition.put("jScrollPane3-122-167", new Integer(0));
        compBounds.put("jScrollPane4", new Rectangle(21, 323, 231, 84));
        baselinePosition.put("jScrollPane4-231-84", new Integer(0));
        compMinSize.put("jPanel2", new Dimension(598, 428));
        compBounds.put("jPanel2", new Rectangle(10, 11, 598, 564));
        compPrefSize.put("jPanel2", new Dimension(598, 564));
        compPrefSize.put("jSeparator1", new Dimension(0, 2));
        compPrefSize.put("jProgressBar1", new Dimension(146, 14));
        prefPaddingInParent.put("jPanel2-jScrollPane4-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel2-jScrollPane3-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        hasExplicitPrefSize.put("jPanel2", new Boolean(false));
        prefPadding.put("jScrollPane3-jProgressBar1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane3-jProgressBar1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane3-jProgressBar1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane3-jProgressBar1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane4-jProgressBar1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane4-jProgressBar1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane4-jProgressBar1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane4-jProgressBar1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        hasExplicitPrefSize.put("jPanel2", new Boolean(false));
        compMinSize.put("Form", new Dimension(618, 450));
        compBounds.put("Form", new Rectangle(0, 0, 618, 586));
        prefPaddingInParent.put("Form-jPanel2-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        compPrefSize.put("jPanel2", new Dimension(598, 564));
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
// > START MOVING
        baselinePosition.put("jScrollPane4-231-84", new Integer(0));
        {
            String[] compIds = new String[]{
                "jScrollPane4"
            };
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(21, 323, 231, 84)
            };
            Point hotspot = new Point(185, 355);
            ld.startMoving(compIds, bounds, hotspot);
        }
// < START MOVING
        prefPaddingInParent.put("jPanel2-jScrollPane4-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel2-jScrollPane4-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jSeparator1-jScrollPane4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane4-jSeparator1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane4-jSeparator1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane4-jSeparator1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane4-jSeparator1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane3-jScrollPane4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane3-jScrollPane4-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane3-jScrollPane4-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane3-jScrollPane4-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane4-jScrollPane3-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jProgressBar1-jScrollPane4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jProgressBar1-jScrollPane4-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jProgressBar1-jScrollPane4-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jProgressBar1-jScrollPane4-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane4-jProgressBar1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane4-jProgressBar1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane4-jProgressBar1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane4-jProgressBar1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("jPanel2-jScrollPane4-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel2-jScrollPane4-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jScrollPane2-jScrollPane4-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane2-jScrollPane4-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane2-jScrollPane4-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane2-jScrollPane4-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane4-jScrollPane2-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane4-jScrollPane2-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane4-jScrollPane2-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane4-jScrollPane2-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane3-jScrollPane4-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
// > MOVE
        {
            Point p = new Point(531, 275);
            String containerId = "jPanel2";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(366, 245, 231, 84)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
        prefPaddingInParent.put("jPanel2-jScrollPane4-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel2-jScrollPane4-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jSeparator1-jScrollPane4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane4-jSeparator1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane4-jSeparator1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane4-jSeparator1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane4-jSeparator1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane3-jScrollPane4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane3-jScrollPane4-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane3-jScrollPane4-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane3-jScrollPane4-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane4-jScrollPane3-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jProgressBar1-jScrollPane4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jProgressBar1-jScrollPane4-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jProgressBar1-jScrollPane4-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jProgressBar1-jScrollPane4-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane4-jProgressBar1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane4-jProgressBar1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane4-jProgressBar1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane4-jProgressBar1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("jPanel2-jScrollPane4-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel2-jScrollPane4-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jScrollPane2-jScrollPane4-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane2-jScrollPane4-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane2-jScrollPane4-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane2-jScrollPane4-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane4-jScrollPane2-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane4-jScrollPane2-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane4-jScrollPane2-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane4-jScrollPane2-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane3-jScrollPane4-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
// > MOVE
        {
            Point p = new Point(531, 274);
            String containerId = "jPanel2";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(366, 245, 231, 84)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
// > END MOVING
        compPrefSize.put("jSeparator1", new Dimension(0, 2));
        compPrefSize.put("jProgressBar1", new Dimension(146, 14));
        prefPaddingInParent.put("jPanel2-jScrollPane3-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jScrollPane2-jProgressBar1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane2-jProgressBar1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane2-jProgressBar1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane2-jProgressBar1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane3-jProgressBar1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane3-jProgressBar1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane3-jProgressBar1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane3-jProgressBar1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane2-jProgressBar1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane4-jProgressBar1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        ld.endMoving(true);
// < END MOVING
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 618, 586));
        contInterior.put("Form", new Rectangle(0, 0, 618, 586));
        compBounds.put("jPanel2", new Rectangle(10, 11, 598, 564));
        baselinePosition.put("jPanel2-598-564", new Integer(0));
        contInterior.put("jPanel2", new Rectangle(11, 12, 596, 562));
        compBounds.put("jSeparator1", new Rectangle(11, 48, 596, 18));
        baselinePosition.put("jSeparator1-596-18", new Integer(-1));
        compBounds.put("jProgressBar1", new Rectangle(11, 549, 596, 14));
        baselinePosition.put("jProgressBar1-596-14", new Integer(-1));
        compBounds.put("jButton2", new Rectangle(270, 473, 73, 23));
        baselinePosition.put("jButton2-73-23", new Integer(15));
        compBounds.put("jScrollPane2", new Rectangle(21, 72, 231, 233));
        baselinePosition.put("jScrollPane2-231-233", new Integer(0));
        compBounds.put("jScrollPane3", new Rectangle(475, 72, 122, 167));
        baselinePosition.put("jScrollPane3-122-167", new Integer(0));
        compBounds.put("jScrollPane4", new Rectangle(366, 245, 231, 84));
        baselinePosition.put("jScrollPane4-231-84", new Integer(0));
        compMinSize.put("jPanel2", new Dimension(598, 350));
        compBounds.put("jPanel2", new Rectangle(10, 11, 598, 564));
        compPrefSize.put("jPanel2", new Dimension(598, 564));
        compPrefSize.put("jSeparator1", new Dimension(0, 2));
        compPrefSize.put("jProgressBar1", new Dimension(146, 14));
        prefPadding.put("jScrollPane2-jScrollPane4-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane2-jScrollPane4-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane2-jScrollPane4-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane2-jScrollPane4-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        hasExplicitPrefSize.put("jPanel2", new Boolean(false));
        prefPadding.put("jScrollPane2-jProgressBar1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane2-jProgressBar1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane2-jProgressBar1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane2-jProgressBar1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane4-jProgressBar1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane4-jProgressBar1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane4-jProgressBar1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane4-jProgressBar1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        hasExplicitPrefSize.put("jPanel2", new Boolean(false));
        compMinSize.put("Form", new Dimension(618, 372));
        compBounds.put("Form", new Rectangle(0, 0, 618, 586));
        prefPaddingInParent.put("Form-jPanel2-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        compPrefSize.put("jPanel2", new Dimension(598, 564));
        compBounds.put("Form", new Rectangle(0, 0, 618, 586));
        contInterior.put("Form", new Rectangle(0, 0, 618, 586));
        compBounds.put("jPanel2", new Rectangle(10, 11, 598, 564));
        baselinePosition.put("jPanel2-598-564", new Integer(0));
        contInterior.put("jPanel2", new Rectangle(11, 12, 596, 562));
        compBounds.put("jSeparator1", new Rectangle(11, 48, 596, 18));
        baselinePosition.put("jSeparator1-596-18", new Integer(-1));
        compBounds.put("jProgressBar1", new Rectangle(11, 549, 596, 14));
        baselinePosition.put("jProgressBar1-596-14", new Integer(-1));
        compBounds.put("jButton2", new Rectangle(270, 473, 73, 23));
        baselinePosition.put("jButton2-73-23", new Integer(15));
        compBounds.put("jScrollPane2", new Rectangle(21, 72, 231, 233));
        baselinePosition.put("jScrollPane2-231-233", new Integer(0));
        compBounds.put("jScrollPane3", new Rectangle(475, 72, 122, 167));
        baselinePosition.put("jScrollPane3-122-167", new Integer(0));
        compBounds.put("jScrollPane4", new Rectangle(366, 245, 231, 84));
        baselinePosition.put("jScrollPane4-231-84", new Integer(0));
        compMinSize.put("jPanel2", new Dimension(598, 350));
        compBounds.put("jPanel2", new Rectangle(10, 11, 598, 564));
        compPrefSize.put("jSeparator1", new Dimension(0, 2));
        compPrefSize.put("jProgressBar1", new Dimension(146, 14));
        prefPadding.put("jScrollPane2-jScrollPane4-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane2-jScrollPane4-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane2-jScrollPane4-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane2-jScrollPane4-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane2-jProgressBar1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane2-jProgressBar1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane2-jProgressBar1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane2-jProgressBar1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane4-jProgressBar1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane4-jProgressBar1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane4-jProgressBar1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane4-jProgressBar1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        compMinSize.put("Form", new Dimension(618, 372));
        compBounds.put("Form", new Rectangle(0, 0, 618, 586));
        prefPaddingInParent.put("Form-jPanel2-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        compPrefSize.put("jPanel2", new Dimension(598, 564));
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
    }
}
