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

public class ALT_MaintainSize07Test extends LayoutTestCase {

    public ALT_MaintainSize07Test(String name) {
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
     * Delete jButton1. One of the other buttons should be set to explicit size.
     */
    public void doChanges0() {
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jButton3", new Rectangle(43, 100, 141, 23));
        baselinePosition.put("jButton3-141-23", new Integer(15));
        compBounds.put("jButton2", new Rectangle(43, 71, 141, 23));
        baselinePosition.put("jButton2-141-23", new Integer(15));
        compBounds.put("jButton1", new Rectangle(43, 42, 141, 23));
        baselinePosition.put("jButton1-141-23", new Integer(15));
        compBounds.put("jTextField1", new Rectangle(190, 43, 143, 20));
        baselinePosition.put("jTextField1-143-20", new Integer(14));
        compBounds.put("jTextField2", new Rectangle(190, 72, 143, 20));
        baselinePosition.put("jTextField2-143-20", new Integer(14));
        compBounds.put("jTextField3", new Rectangle(190, 101, 143, 20));
        baselinePosition.put("jTextField3-143-20", new Integer(14));
        compPrefSize.put("jButton3", new Dimension(73, 23));
        compPrefSize.put("jButton2", new Dimension(73, 23));
        compPrefSize.put("jButton1", new Dimension(73, 23));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compPrefSize.put("jTextField2", new Dimension(59, 20));
        compPrefSize.put("jTextField3", new Dimension(59, 20));
        prefPaddingInParent.put("Form-jButton3-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jTextField3-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jButton3", new Rectangle(43, 100, 141, 23));
        baselinePosition.put("jButton3-141-23", new Integer(15));
        compBounds.put("jButton2", new Rectangle(43, 71, 141, 23));
        baselinePosition.put("jButton2-141-23", new Integer(15));
        compBounds.put("jButton1", new Rectangle(43, 42, 141, 23));
        baselinePosition.put("jButton1-141-23", new Integer(15));
        compBounds.put("jTextField1", new Rectangle(190, 43, 143, 20));
        baselinePosition.put("jTextField1-143-20", new Integer(14));
        compBounds.put("jTextField2", new Rectangle(190, 72, 143, 20));
        baselinePosition.put("jTextField2-143-20", new Integer(14));
        compBounds.put("jTextField3", new Rectangle(190, 101, 143, 20));
        baselinePosition.put("jTextField3-143-20", new Integer(14));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
        compPrefSize.put("jButton3", new Dimension(73, 23));
        compPrefSize.put("jButton2", new Dimension(73, 23));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compPrefSize.put("jTextField2", new Dimension(59, 20));
        compPrefSize.put("jTextField3", new Dimension(59, 20));
        prefPaddingInParent.put("Form-jButton3-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jTextField3-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        // parentId-compId-dimension-compAlignment
        lm.removeComponent("jButton1", true);
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jButton3", new Rectangle(43, 97, 141, 23));
        baselinePosition.put("jButton3-141-23", new Integer(15));
        compBounds.put("jButton2", new Rectangle(43, 68, 141, 23));
        baselinePosition.put("jButton2-141-23", new Integer(15));
        compBounds.put("jTextField1", new Rectangle(190, 42, 143, 20));
        baselinePosition.put("jTextField1-143-20", new Integer(14));
        compBounds.put("jTextField2", new Rectangle(190, 69, 143, 20));
        baselinePosition.put("jTextField2-143-20", new Integer(14));
        compBounds.put("jTextField3", new Rectangle(190, 98, 143, 20));
        baselinePosition.put("jTextField3-143-20", new Integer(14));
        compPrefSize.put("jButton3", new Dimension(73, 23));
        compPrefSize.put("jButton2", new Dimension(73, 23));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compPrefSize.put("jTextField2", new Dimension(59, 20));
        compPrefSize.put("jTextField3", new Dimension(59, 20));
        prefPaddingInParent.put("Form-jButton3-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jTextField3-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jButton3", new Rectangle(43, 97, 141, 23));
        baselinePosition.put("jButton3-141-23", new Integer(15));
        compBounds.put("jButton2", new Rectangle(43, 68, 141, 23));
        baselinePosition.put("jButton2-141-23", new Integer(15));
        compBounds.put("jTextField1", new Rectangle(190, 42, 143, 20));
        baselinePosition.put("jTextField1-143-20", new Integer(14));
        compBounds.put("jTextField2", new Rectangle(190, 69, 143, 20));
        baselinePosition.put("jTextField2-143-20", new Integer(14));
        compBounds.put("jTextField3", new Rectangle(190, 98, 143, 20));
        baselinePosition.put("jTextField3-143-20", new Integer(14));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }

}
