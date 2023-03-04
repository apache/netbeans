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
 * Tests LayoutOperations.eliminateEndingGaps for special situation revealed in
 * http://statistics.netbeans.org/exceptions/exception.do?id=637356.
 * The tested parallel group has outer gaps on both sides and all intervals of
 * the group are evalueted as need edge gap optimization on either side, but
 * they are not all on one side, and are not meeting the "independent edges"
 * condition either. This special case where the original group was emptied was
 * not considered before and let to a failure.
 */
public class ALT_GapsOptimization10Test extends LayoutTestCase {

    public ALT_GapsOptimization10Test(String name) {
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
     * Delete jLabel2.
     */
    public void doChanges0() {
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 427, 300));
        contInterior.put("Form", new Rectangle(0, 0, 427, 300));
        compBounds.put("jLabel2", new Rectangle(167, 70, 81, 14));
        baselinePosition.put("jLabel2-81-14", new Integer(11));
        compBounds.put("jLabel1", new Rectangle(167, 50, 91, 14));
        baselinePosition.put("jLabel1-91-14", new Integer(11));
        compBounds.put("jButton1", new Rectangle(224, 90, 73, 23));
        baselinePosition.put("jButton1-73-23", new Integer(15));
        compBounds.put("jTextField1", new Rectangle(157, 119, 150, 20));
        baselinePosition.put("jTextField1-150-20", new Integer(14));
        compMinSize.put("Form", new Dimension(373, 150));
        compBounds.put("Form", new Rectangle(0, 0, 427, 300));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        prefPaddingInParent.put("Form-jTextField1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
        lm.removeComponent("jLabel2", true);
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        prefPaddingInParent.put("Form-jTextField1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 427, 300));
        contInterior.put("Form", new Rectangle(0, 0, 427, 300));
        compBounds.put("jLabel1", new Rectangle(167, 50, 91, 14));
        baselinePosition.put("jLabel1-91-14", new Integer(11));
        compBounds.put("jButton1", new Rectangle(224, 90, 73, 23));
        baselinePosition.put("jButton1-73-23", new Integer(15));
        compBounds.put("jTextField1", new Rectangle(157, 119, 150, 20));
        baselinePosition.put("jTextField1-150-20", new Integer(14));
        compMinSize.put("Form", new Dimension(283, 150));
        compBounds.put("Form", new Rectangle(0, 0, 427, 300));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        prefPaddingInParent.put("Form-jTextField1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 427, 300));
        prefPaddingInParent.put("Form-jTextField1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
    }
}
