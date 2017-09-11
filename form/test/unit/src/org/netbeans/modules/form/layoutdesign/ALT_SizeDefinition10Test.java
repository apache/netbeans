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
