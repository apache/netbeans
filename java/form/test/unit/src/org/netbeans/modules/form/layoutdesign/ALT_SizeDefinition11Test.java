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

/**
 * This test just opens the form and does the usual visual update. It should not
 * set the height of the scrollpane with table to explicit size (402) although it
 * is resizing and the actual built size does not match the default preferred
 * size (is smaller).
 */
public class ALT_SizeDefinition11Test extends LayoutTestCase {

    public ALT_SizeDefinition11Test(String name) {
        super(name);
        try {
            className = this.getClass().getName();
            className = className.substring(className.lastIndexOf('.') + 1, className.length());
            startingFormFile = FileUtil.toFileObject(new File(url.getFile() + goldenFilesPath + className + "-StartingForm.form").getCanonicalFile());
        } catch (IOException ioe) {
            fail(ioe.toString());
        }
    }

    public void doChanges0() {
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 492, 446));
        contInterior.put("Form", new Rectangle(0, 0, 492, 446));
        compBounds.put("panel", new Rectangle(10, 11, 472, 424));
        baselinePosition.put("panel-472-424", new Integer(0));
        compMinSize.put("Form", new Dimension(63, 71));
        contInterior.put("panel", new Rectangle(10, 11, 472, 424));
        compBounds.put("scrollPane", new Rectangle(20, 22, 452, 402));
        baselinePosition.put("scrollPane-452-402", new Integer(0));
        compMinSize.put("panel", new Dimension(43, 49));
        compPrefSize.put("scrollPane", new Dimension(452, 427));
        compPrefSize.put("panel", new Dimension(472, 449));
        hasExplicitPrefSize.put("panel", Boolean.FALSE);
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
        prefPaddingInParent.put("Form-panel-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-panel-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-panel-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-panel-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
    }

}
