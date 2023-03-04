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
 * set the height of textfields to explicit size (36) although they are resizing
 * and the actual built size does not match the default preferred size (is bigger).
 */
public class ALT_SizeDefinition10Test extends LayoutTestCase {

    public ALT_SizeDefinition10Test(String name) {
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
        compBounds.put("Form", new Rectangle(0, 0, 451, 108));
        contInterior.put("Form", new Rectangle(0, 0, 451, 108));
        compBounds.put("tabbedPane", new Rectangle(10, 11, 431, 86));
        baselinePosition.put("tabbedPane-431-86", new Integer(0));
        compMinSize.put("Form", new Dimension(63, 108));
        compBounds.put("Form", new Rectangle(0, 0, 451, 108));
        compBounds.put("tab2", new Rectangle(12, 36, 426, 58));
        compBounds.put("tab1", new Rectangle(12, 36, 426, 58));
        compBounds.put("Form", new Rectangle(0, 0, 451, 108));
        compPrefSize.put("tabbedPane", new Dimension(431, 70));
        compPrefSize.put("tabbedPane", new Dimension(431, 70));
        compBounds.put("tab2", new Rectangle(12, 36, 426, 58));
        contInterior.put("tab2", new Rectangle(12, 36, 426, 58));
        compBounds.put("textField2", new Rectangle(22, 47, 406, 36));
        baselinePosition.put("textField2-406-36", new Integer(22));
        compMinSize.put("tab2", new Dimension(26, 42));
        compBounds.put("tab2", new Rectangle(12, 36, 426, 58));
        compPrefSize.put("textField2", new Dimension(162, 20));
        compBounds.put("tab1", new Rectangle(12, 36, 426, 58));
        contInterior.put("tab1", new Rectangle(12, 36, 426, 58));
        compBounds.put("textField1", new Rectangle(22, 47, 406, 36));
        baselinePosition.put("textField1-406-36", new Integer(22));
        compMinSize.put("tab1", new Dimension(26, 42));
        compBounds.put("tab1", new Rectangle(12, 36, 426, 58));
        compPrefSize.put("textField1", new Dimension(406, 20));
        compPrefSize.put("textField1", new Dimension(406, 20));
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
        prefPaddingInParent.put("Form-tabbedPane-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-tabbedPane-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-tabbedPane-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-tabbedPane-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
    }

}
