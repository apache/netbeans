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

public class ALT_Bug66919Test extends LayoutTestCase {

    public ALT_Bug66919Test(String name) {
        super(name);
        try {
	    className = this.getClass().getName();
	    className = className.substring(className.lastIndexOf('.') + 1, className.length());	
            startingFormFile = FileUtil.toFileObject(new File(url.getFile() + goldenFilesPath + className + "-StartingForm.form").getCanonicalFile());
        } catch (IOException ioe) {
            fail(ioe.toString());
        }
    }

    // Resize 'Save' button slightly to the right. The layout is screwed in
    // vertical dimension so the button overlaps with the panel above it, but
    // resizing in horizontal dimension only should not care about this overlap.
    public void doChanges0() {
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("jPanel1", new Rectangle(115, 31, 274, 157));
        compBounds.put("jPanel1", new Rectangle(109, 11, 286, 183));
        baselinePosition.put("jPanel1-286-183", new Integer(0));
        compMinSize.put("jPanel1", new Dimension(12, 26));
        compPrefSize.put("jPanel1", new Dimension(286, 183));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        contInterior.put("Form", new Rectangle(0, 0, 778, 481));
        compBounds.put("add", new Rectangle(10, 11, 93, 23));
        baselinePosition.put("add-93-23", new Integer(15));
        compPrefSize.put("add", new Dimension(93, 23));
        compBounds.put("jPanel1", new Rectangle(109, 11, 286, 183));
        baselinePosition.put("jPanel1-286-183", new Integer(0));
        compBounds.put("save", new Rectangle(401, 173, 92, 23));
        baselinePosition.put("save-92-23", new Integer(15));
        compPrefSize.put("save", new Dimension(59, 23));
        compBounds.put("delete", new Rectangle(631, 173, 137, 23));
        baselinePosition.put("delete-137-23", new Integer(15));
        compPrefSize.put("delete", new Dimension(65, 23));
        compBounds.put("jPanel2", new Rectangle(401, 11, 348, 164));
        baselinePosition.put("jPanel2-348-164", new Integer(0));
        prefPaddingInParent.put("Form-add-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("add-jPanel1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jPanel2-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-delete-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jPanel2-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jPanel1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-add-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        contInterior.put("jPanel2", new Rectangle(407, 31, 336, 138));
        compBounds.put("jPanel2", new Rectangle(401, 11, 348, 164));
        baselinePosition.put("jPanel2-348-164", new Integer(0));
        compMinSize.put("jPanel2", new Dimension(12, 26));
        compPrefSize.put("jPanel2", new Dimension(348, 164));
        hasExplicitPrefSize.put("jPanel2", new Boolean(false));
        hasExplicitPrefSize.put("jPanel2", new Boolean(false));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
        // > START RESIZING
        {
            String[] compIds = new String[] {
                "save"
                };
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(401, 173, 92, 23)
                };
            Point hotspot = new Point(494,186);
            int[] resizeEdges = new int[] {
                1,
                    -1
                };
            boolean inLayout = true;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
        baselinePosition.put("save-92-23", new Integer(15));
        compPrefSize.put("save", new Dimension(59, 23));
        // < START RESIZING
        prefPadding.put("save-delete-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        {
            Point p = new Point(515,184);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(401, 173, 113, 23)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPadding.put("save-delete-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        {
            Point p = new Point(516,184);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(401, 173, 114, 23)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        prefPadding.put("save-delete-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("jPanel1", new Rectangle(115, 31, 274, 157));
        compBounds.put("jPanel1", new Rectangle(109, 11, 286, 183));
        baselinePosition.put("jPanel1-286-183", new Integer(0));
        compMinSize.put("jPanel1", new Dimension(12, 26));
        compPrefSize.put("jPanel1", new Dimension(286, 183));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        contInterior.put("Form", new Rectangle(0, 0, 778, 481));
        compBounds.put("add", new Rectangle(10, 11, 93, 23));
        baselinePosition.put("add-93-23", new Integer(15));
        compPrefSize.put("add", new Dimension(93, 23));
        compBounds.put("jPanel1", new Rectangle(109, 11, 286, 183));
        baselinePosition.put("jPanel1-286-183", new Integer(0));
        compBounds.put("delete", new Rectangle(631, 173, 137, 23));
        baselinePosition.put("delete-137-23", new Integer(15));
        compPrefSize.put("delete", new Dimension(65, 23));
        compBounds.put("jPanel2", new Rectangle(401, 11, 348, 164));
        baselinePosition.put("jPanel2-348-164", new Integer(0));
        compBounds.put("save", new Rectangle(401, 173, 114, 23));
        baselinePosition.put("save-114-23", new Integer(15));
        compPrefSize.put("save", new Dimension(59, 23));
        prefPaddingInParent.put("Form-add-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("add-jPanel1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jPanel2-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-delete-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jPanel2-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jPanel1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-add-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        contInterior.put("jPanel2", new Rectangle(407, 31, 336, 138));
        compBounds.put("jPanel2", new Rectangle(401, 11, 348, 164));
        baselinePosition.put("jPanel2-348-164", new Integer(0));
        compMinSize.put("jPanel2", new Dimension(12, 26));
        compPrefSize.put("jPanel2", new Dimension(348, 164));
        hasExplicitPrefSize.put("jPanel2", new Boolean(false));
        hasExplicitPrefSize.put("jPanel2", new Boolean(false));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }
    
}
