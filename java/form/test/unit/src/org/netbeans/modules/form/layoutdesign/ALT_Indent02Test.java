/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.form.layoutdesign;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.*;
import org.openide.filesystems.FileUtil;

// Test simulating indented alignment when the whole group is aligned, but only
// one component (in the group) is indented. The whole group needs to be aligned
// (would be S-layout otherwise). As the group moves, the leading gap before it
// needs to be shortened.
public class ALT_Indent02Test extends LayoutTestCase {
        
    public ALT_Indent02Test(String name) {
        super(name);
        try {
	    className = this.getClass().getName();
	    className = className.substring(className.lastIndexOf('.') + 1, className.length());	    
            startingFormFile = FileUtil.toFileObject(new File(url.getFile() + goldenFilesPath + className + "-StartingForm.form").getCanonicalFile());
        } catch (IOException ioe) {
            fail(ioe.toString());
        }
    }

    // Move jLabel20 ("Show text in icons:") to indent it from the label above
    // it (so virtually on the same place where it is).
    public void doChanges0() {
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("jPanel5", new Rectangle(0, 0, 400, 308));
        compBounds.put("jPanel5", new Rectangle(0, 0, 400, 308));
        compPrefSize.put("jPanel5", new Dimension(400, 308));
        compBounds.put("jLabel20", new Rectangle(30, 35, 91, 14));
        baselinePosition.put("jLabel20-91-14", new Integer(11));
        compPrefSize.put("jLabel20", new Dimension(91, 14));
        compBounds.put("jLabel23", new Rectangle(30, 107, 84, 14));
        baselinePosition.put("jLabel23-84-14", new Integer(11));
        compPrefSize.put("jLabel23", new Dimension(84, 14));
        compBounds.put("jLabel27", new Rectangle(30, 135, 126, 14));
        baselinePosition.put("jLabel27-126-14", new Integer(11));
        compPrefSize.put("jLabel27", new Dimension(126, 14));
        compBounds.put("jComboBox9", new Rectangle(160, 31, 230, 22));
        baselinePosition.put("jComboBox9-230-22", new Integer(15));
        compPrefSize.put("jComboBox9", new Dimension(57, 22));
        compBounds.put("jComboBox11", new Rectangle(160, 103, 230, 22));
        baselinePosition.put("jComboBox11-230-22", new Integer(15));
        compPrefSize.put("jComboBox11", new Dimension(57, 22));
        compBounds.put("jComboBox13", new Rectangle(160, 131, 230, 22));
        baselinePosition.put("jComboBox13-230-22", new Integer(15));
        compPrefSize.put("jComboBox13", new Dimension(57, 22));
        compBounds.put("jLabel22", new Rectangle(20, 83, 113, 14));
        baselinePosition.put("jLabel22-113-14", new Integer(11));
        compPrefSize.put("jLabel22", new Dimension(113, 14));
        compBounds.put("jLabel19", new Rectangle(20, 11, 46, 14));
        baselinePosition.put("jLabel19-46-14", new Integer(11));
        compPrefSize.put("jLabel19", new Dimension(46, 14));
        contInterior.put("jPanel5", new Rectangle(0, 0, 400, 308));
        compBounds.put("jLabel20", new Rectangle(30, 35, 91, 14));
        baselinePosition.put("jLabel20-91-14", new Integer(11));
        compBounds.put("jLabel23", new Rectangle(30, 107, 84, 14));
        baselinePosition.put("jLabel23-84-14", new Integer(11));
        compBounds.put("jLabel27", new Rectangle(30, 135, 126, 14));
        baselinePosition.put("jLabel27-126-14", new Integer(11));
        compBounds.put("jComboBox9", new Rectangle(160, 31, 230, 22));
        baselinePosition.put("jComboBox9-230-22", new Integer(15));
        compBounds.put("jComboBox11", new Rectangle(160, 103, 230, 22));
        baselinePosition.put("jComboBox11-230-22", new Integer(15));
        compBounds.put("jComboBox13", new Rectangle(160, 131, 230, 22));
        baselinePosition.put("jComboBox13-230-22", new Integer(15));
        compBounds.put("jLabel22", new Rectangle(20, 83, 113, 14));
        baselinePosition.put("jLabel22-113-14", new Integer(11));
        compBounds.put("jLabel19", new Rectangle(20, 11, 46, 14));
        baselinePosition.put("jLabel19-46-14", new Integer(11));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
        // > START MOVING
        baselinePosition.put("jLabel20-91-14", new Integer(11));
        {
            String[] compIds = new String[] {
                "jLabel20"
                };
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(30, 35, 91, 14)
                };
            Point hotspot = new Point(46,41);
            ld.startMoving(compIds, bounds, hotspot);
        }
        // < START MOVING
        prefPaddingInParent.put("jPanel5-jLabel20-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel5-jLabel20-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jLabel19-jLabel20-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel20-jLabel19-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel22-jLabel20-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel20-jLabel22-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel23-jLabel20-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jComboBox11-jLabel20-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel20-jLabel23-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel20-jComboBox11-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel27-jLabel20-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jComboBox13-jLabel20-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel20-jLabel27-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel20-jComboBox13-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("jPanel5-jLabel20-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel5-jLabel20-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jLabel20-jComboBox9-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel19-jLabel20-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        {
            Point p = new Point(45,46);
            String containerId= "jPanel5";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(30, 35, 91, 14)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPaddingInParent.put("jPanel5-jLabel20-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel5-jLabel20-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jLabel19-jLabel20-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel20-jLabel19-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel22-jLabel20-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel20-jLabel22-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel23-jLabel20-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jComboBox11-jLabel20-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel20-jLabel23-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel20-jComboBox11-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel27-jLabel20-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jComboBox13-jLabel20-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel20-jLabel27-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel20-jComboBox13-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("jPanel5-jLabel20-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel5-jLabel20-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jLabel20-jComboBox9-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel19-jLabel20-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        {
            Point p = new Point(44,46);
            String containerId= "jPanel5";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(30, 35, 91, 14)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        prefPadding.put("jLabel23-jComboBox9-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel23-jComboBox11-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel23-jComboBox13-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel27-jComboBox9-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel27-jComboBox11-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel27-jComboBox13-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel20-jComboBox9-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel20-jComboBox11-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel20-jComboBox13-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("jPanel5", new Rectangle(0, 0, 400, 308));
        compBounds.put("jLabel23", new Rectangle(30, 107, 84, 14));
        baselinePosition.put("jLabel23-84-14", new Integer(11));
        compPrefSize.put("jLabel23", new Dimension(84, 14));
        compBounds.put("jLabel27", new Rectangle(30, 135, 126, 14));
        baselinePosition.put("jLabel27-126-14", new Integer(11));
        compPrefSize.put("jLabel27", new Dimension(126, 14));
        compBounds.put("jComboBox9", new Rectangle(160, 31, 230, 22));
        baselinePosition.put("jComboBox9-230-22", new Integer(15));
        compPrefSize.put("jComboBox9", new Dimension(57, 22));
        compBounds.put("jComboBox11", new Rectangle(160, 103, 230, 22));
        baselinePosition.put("jComboBox11-230-22", new Integer(15));
        compPrefSize.put("jComboBox11", new Dimension(57, 22));
        compBounds.put("jComboBox13", new Rectangle(160, 131, 230, 22));
        baselinePosition.put("jComboBox13-230-22", new Integer(15));
        compPrefSize.put("jComboBox13", new Dimension(57, 22));
        compBounds.put("jLabel22", new Rectangle(20, 83, 113, 14));
        baselinePosition.put("jLabel22-113-14", new Integer(11));
        compPrefSize.put("jLabel22", new Dimension(113, 14));
        compBounds.put("jLabel19", new Rectangle(20, 11, 46, 14));
        baselinePosition.put("jLabel19-46-14", new Integer(11));
        compPrefSize.put("jLabel19", new Dimension(46, 14));
        compBounds.put("jLabel20", new Rectangle(30, 35, 91, 14));
        baselinePosition.put("jLabel20-91-14", new Integer(11));
        compPrefSize.put("jLabel20", new Dimension(91, 14));
        contInterior.put("jPanel5", new Rectangle(0, 0, 400, 308));
        compBounds.put("jLabel23", new Rectangle(30, 107, 84, 14));
        baselinePosition.put("jLabel23-84-14", new Integer(11));
        compBounds.put("jLabel27", new Rectangle(30, 135, 126, 14));
        baselinePosition.put("jLabel27-126-14", new Integer(11));
        compBounds.put("jComboBox9", new Rectangle(160, 31, 230, 22));
        baselinePosition.put("jComboBox9-230-22", new Integer(15));
        compBounds.put("jComboBox11", new Rectangle(160, 103, 230, 22));
        baselinePosition.put("jComboBox11-230-22", new Integer(15));
        compBounds.put("jComboBox13", new Rectangle(160, 131, 230, 22));
        baselinePosition.put("jComboBox13-230-22", new Integer(15));
        compBounds.put("jLabel22", new Rectangle(20, 83, 113, 14));
        baselinePosition.put("jLabel22-113-14", new Integer(11));
        compBounds.put("jLabel19", new Rectangle(20, 11, 46, 14));
        baselinePosition.put("jLabel19-46-14", new Integer(11));
        compBounds.put("jLabel20", new Rectangle(30, 35, 91, 14));
        baselinePosition.put("jLabel20-91-14", new Integer(11));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }
    
}
