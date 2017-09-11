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

public class ALT_Bug124689Test extends LayoutTestCase {

    public ALT_Bug124689Test(String name) {
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
     * Resize jDesktopPane1 down by its bottom edge.
     */
    public void doChanges0() {
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        contInterior.put("jInternalFrame3", new Rectangle(36, 249, 104, 93));
        contInterior.put("jPanel3", new Rectangle(101, 161, 0, 0));
        contInterior.put("jPanel1", new Rectangle(20, 36, 178, 116));
        contInterior.put("Form", new Rectangle(0, 0, 407, 636));
        compBounds.put("jTabbedPane1", new Rectangle(18, 11, 183, 144));
        baselinePosition.put("jTabbedPane1-183-144", new Integer(0));
        compPrefSize.put("jTabbedPane1", new Dimension(183, 144));
        compBounds.put("jDesktopPane1", new Rectangle(18, 215, 183, 208));
        baselinePosition.put("jDesktopPane1-183-208", new Integer(-1));
        compPrefSize.put("jDesktopPane1", new Dimension(1, 1));
        compBounds.put("jToolBar1", new Rectangle(101, 179, 100, 25));
        baselinePosition.put("jToolBar1-100-25", new Integer(-1));
        compPrefSize.put("jToolBar1", new Dimension(92, 23));
        compBounds.put("jSplitPane1", new Rectangle(101, 161, 100, 0));
        baselinePosition.put("jSplitPane1-100-0", new Integer(-1));
        compPrefSize.put("jSplitPane1", new Dimension(80, 25));
        compBounds.put("textArea1", new Rectangle(219, 298, 188, 136));
        baselinePosition.put("textArea1-188-136", new Integer(0));
        compPrefSize.put("textArea1", new Dimension(100, 80));
        compBounds.put("jScrollPane1", new Rectangle(219, 20, 168, 135));
        baselinePosition.put("jScrollPane1-168-135", new Integer(0));
        compPrefSize.put("jScrollPane1", new Dimension(452, 439));
        contInterior.put("jInternalFrame2", new Rectangle(76, 299, 104, 93));
        contInterior.put("jPanel2", new Rectangle(20, 36, 178, 116));
        contInterior.put("jInternalFrame3", new Rectangle(36, 249, 104, 93));
        contInterior.put("jPanel3", new Rectangle(101, 161, 0, 0));
        contInterior.put("jPanel1", new Rectangle(20, 36, 178, 116));
        contInterior.put("Form", new Rectangle(0, 0, 407, 636));
        compBounds.put("jTabbedPane1", new Rectangle(18, 11, 183, 144));
        baselinePosition.put("jTabbedPane1-183-144", new Integer(0));
        compBounds.put("jDesktopPane1", new Rectangle(18, 215, 183, 208));
        baselinePosition.put("jDesktopPane1-183-208", new Integer(-1));
        compBounds.put("jToolBar1", new Rectangle(101, 179, 100, 25));
        baselinePosition.put("jToolBar1-100-25", new Integer(-1));
        compBounds.put("jSplitPane1", new Rectangle(101, 161, 100, 0));
        baselinePosition.put("jSplitPane1-100-0", new Integer(-1));
        compBounds.put("textArea1", new Rectangle(219, 298, 188, 136));
        baselinePosition.put("textArea1-188-136", new Integer(0));
        compBounds.put("jScrollPane1", new Rectangle(219, 20, 168, 135));
        baselinePosition.put("jScrollPane1-168-135", new Integer(0));
        contInterior.put("jInternalFrame2", new Rectangle(76, 299, 104, 93));
        contInterior.put("jPanel2", new Rectangle(20, 36, 178, 116));
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
// > START RESIZING
        baselinePosition.put("jDesktopPane1-183-208", new Integer(-1));
        compPrefSize.put("jDesktopPane1", new Dimension(1, 1));
        {
            String[] compIds = new String[]{"jDesktopPane1"};
            Rectangle[] bounds = new Rectangle[]{new Rectangle(18, 215, 183, 208)};
            Point hotspot = new Point(109, 421);
            int[] resizeEdges = new int[]{-1, 1};
            boolean inLayout = true;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
// < START RESIZING
        prefPaddingInParent.put("Form-jDesktopPane1-1-1", new Integer(11));
        // parentId-compId-dimension-compAlignment
// > MOVE
        {
            Point p = new Point(108, 522);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(18, 215, 183, 309)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
        prefPaddingInParent.put("Form-jDesktopPane1-1-1", new Integer(11));
        // parentId-compId-dimension-compAlignment
// > MOVE
        {
            Point p = new Point(108, 523);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(18, 215, 183, 310)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
// > END MOVING
        prefPaddingInParent.put("Form-jDesktopPane1-1-1", new Integer(11));
        // parentId-compId-dimension-compAlignment
        ld.endMoving(true);
// < END MOVING
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        contInterior.put("jInternalFrame3", new Rectangle(36, 249, 104, 93));
        contInterior.put("jPanel3", new Rectangle(18, 161, 0, 0));
        contInterior.put("jPanel1", new Rectangle(20, 36, 178, 116));
        contInterior.put("Form", new Rectangle(0, 0, 407, 636));
        compBounds.put("jTabbedPane1", new Rectangle(18, 11, 183, 144));
        baselinePosition.put("jTabbedPane1-183-144", new Integer(0));
        compPrefSize.put("jTabbedPane1", new Dimension(183, 144));
        compBounds.put("jDesktopPane1", new Rectangle(18, 215, 183, 310));
        baselinePosition.put("jDesktopPane1-183-310", new Integer(-1));
        compPrefSize.put("jDesktopPane1", new Dimension(1, 1));
        compBounds.put("jToolBar1", new Rectangle(101, 179, 100, 25));
        baselinePosition.put("jToolBar1-100-25", new Integer(-1));
        compPrefSize.put("jToolBar1", new Dimension(92, 23));
        compBounds.put("jSplitPane1", new Rectangle(101, 161, 100, 0));
        baselinePosition.put("jSplitPane1-183-0", new Integer(-1));
        compPrefSize.put("jSplitPane1", new Dimension(80, 25));
        compBounds.put("textArea1", new Rectangle(219, 298, 188, 136));
        baselinePosition.put("textArea1-188-136", new Integer(0));
        compPrefSize.put("textArea1", new Dimension(100, 80));
        compBounds.put("jScrollPane1", new Rectangle(219, 20, 168, 135));
        baselinePosition.put("jScrollPane1-168-135", new Integer(0));
        compPrefSize.put("jScrollPane1", new Dimension(452, 439));
        contInterior.put("jInternalFrame2", new Rectangle(76, 299, 104, 93));
        contInterior.put("jPanel2", new Rectangle(20, 36, 178, 116));
        contInterior.put("jInternalFrame3", new Rectangle(36, 249, 104, 93));
        contInterior.put("jPanel3", new Rectangle(18, 161, 0, 0));
        contInterior.put("jPanel1", new Rectangle(20, 36, 178, 116));
        contInterior.put("Form", new Rectangle(0, 0, 407, 636));
        compBounds.put("jTabbedPane1", new Rectangle(18, 11, 183, 144));
        baselinePosition.put("jTabbedPane1-183-144", new Integer(0));
        compBounds.put("jDesktopPane1", new Rectangle(18, 215, 183, 310));
        baselinePosition.put("jDesktopPane1-183-310", new Integer(-1));
        compBounds.put("jToolBar1", new Rectangle(101, 179, 100, 25));
        baselinePosition.put("jToolBar1-100-25", new Integer(-1));
        compBounds.put("jSplitPane1", new Rectangle(101, 161, 100, 0));
        baselinePosition.put("jSplitPane1-183-0", new Integer(-1));
        compBounds.put("textArea1", new Rectangle(219, 298, 188, 136));
        baselinePosition.put("textArea1-188-136", new Integer(0));
        compBounds.put("jScrollPane1", new Rectangle(219, 20, 168, 135));
        baselinePosition.put("jScrollPane1-168-135", new Integer(0));
        contInterior.put("jInternalFrame2", new Rectangle(76, 299, 104, 93));
        contInterior.put("jPanel2", new Rectangle(20, 36, 178, 116));
        ld.updateCurrentState();
    }

}
