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

// Tests that resizing snaps next to a parallel group.
public class ALT_Resizing08Test extends LayoutTestCase {

    public ALT_Resizing08Test(String name) {
        super(name);
        try {
	    className = this.getClass().getName();
	    className = className.substring(className.lastIndexOf('.') + 1, className.length());	    
            startingFormFile = FileUtil.toFileObject(new File(url.getFile() + goldenFilesPath + className + "-StartingForm.form").getCanonicalFile());
        } catch (IOException ioe) {
            fail(ioe.toString());
        }
    }

    // Resize text field to the right to snap next to the group of labels and button.
    public void doChanges0() {
        lm.setChangeRecording(true);
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jLabel1", new Rectangle(10, 14, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        compBounds.put("jTextField1", new Rectangle(48, 11, 59, 20));
        baselinePosition.put("jTextField1-59-20", new Integer(14));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compBounds.put("jButton1", new Rectangle(315, 37, 75, 23));
        baselinePosition.put("jButton1-75-23", new Integer(15));
        compPrefSize.put("jButton1", new Dimension(75, 23));
        compBounds.put("jLabel3", new Rectangle(315, 14, 34, 14));
        baselinePosition.put("jLabel3-34-14", new Integer(11));
        compPrefSize.put("jLabel3", new Dimension(34, 14));
        compBounds.put("jLabel2", new Rectangle(355, 14, 34, 14));
        baselinePosition.put("jLabel2-34-14", new Integer(11));
        compPrefSize.put("jLabel2", new Dimension(34, 14));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jLabel1", new Rectangle(10, 14, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compBounds.put("jTextField1", new Rectangle(48, 11, 59, 20));
        baselinePosition.put("jTextField1-59-20", new Integer(14));
        compBounds.put("jButton1", new Rectangle(315, 37, 75, 23));
        baselinePosition.put("jButton1-75-23", new Integer(15));
        compBounds.put("jLabel3", new Rectangle(315, 14, 34, 14));
        baselinePosition.put("jLabel3-34-14", new Integer(11));
        compBounds.put("jLabel2", new Rectangle(355, 14, 34, 14));
        baselinePosition.put("jLabel2-34-14", new Integer(11));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
        // > START RESIZING
        baselinePosition.put("jTextField1-59-20", new Integer(14));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        {
            String[] compIds = new String[] {
                "jTextField1"
                };
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(48, 11, 59, 20)
                };
            Point hotspot = new Point(105,21);
            int[] resizeEdges = new int[] {
                1,
                    -1
                };
            boolean inLayout = true;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
        // < START RESIZING
        prefPadding.put("jTextField1-jLabel3-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel2-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jButton1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel3-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        {
            Point p = new Point(306,31);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(48, 11, 261, 20)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPadding.put("jTextField1-jLabel3-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel2-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jButton1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel3-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        {
            Point p = new Point(307,31);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(48, 11, 261, 20)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jLabel1", new Rectangle(10, 14, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        compBounds.put("jButton1", new Rectangle(315, 37, 75, 23));
        baselinePosition.put("jButton1-75-23", new Integer(15));
        compPrefSize.put("jButton1", new Dimension(75, 23));
        compBounds.put("jLabel3", new Rectangle(315, 14, 34, 14));
        baselinePosition.put("jLabel3-34-14", new Integer(11));
        compPrefSize.put("jLabel3", new Dimension(34, 14));
        compBounds.put("jLabel2", new Rectangle(355, 14, 34, 14));
        baselinePosition.put("jLabel2-34-14", new Integer(11));
        compPrefSize.put("jLabel2", new Dimension(34, 14));
        compBounds.put("jTextField1", new Rectangle(48, 11, 261, 20));
        baselinePosition.put("jTextField1-261-20", new Integer(14));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jLabel1", new Rectangle(10, 14, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compBounds.put("jButton1", new Rectangle(315, 37, 75, 23));
        baselinePosition.put("jButton1-75-23", new Integer(15));
        compBounds.put("jLabel3", new Rectangle(315, 14, 34, 14));
        baselinePosition.put("jLabel3-34-14", new Integer(11));
        compBounds.put("jLabel2", new Rectangle(355, 14, 34, 14));
        baselinePosition.put("jLabel2-34-14", new Integer(11));
        compBounds.put("jTextField1", new Rectangle(48, 11, 261, 20));
        baselinePosition.put("jTextField1-261-20", new Integer(14));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }
    
}
