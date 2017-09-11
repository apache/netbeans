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

public class ALT_Bug204704Test extends LayoutTestCase {

    public ALT_Bug204704Test(String name) {
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
     * Delete jSpinner1.
     */
    public void doChanges0() {
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 500, 329));
        contInterior.put("Form", new Rectangle(0, 0, 500, 329));
        compBounds.put("jScrollPane1", new Rectangle(10, 11, 69, 89));
        baselinePosition.put("jScrollPane1-69-89", new Integer(0));
        compBounds.put("jRadioButton3", new Rectangle(397, 77, 93, 23));
        baselinePosition.put("jRadioButton3-93-23", new Integer(15));
        compBounds.put("jRadioButton1", new Rectangle(397, 28, 93, 23));
        baselinePosition.put("jRadioButton1-93-23", new Integer(15));
        compBounds.put("jRadioButton2", new Rectangle(397, 51, 93, 23));
        baselinePosition.put("jRadioButton2-93-23", new Integer(15));
        compBounds.put("jLabel1", new Rectangle(89, 11, 401, 14));
        baselinePosition.put("jLabel1-401-14", new Integer(11));
        compBounds.put("jSpinner1", new Rectangle(97, 298, 104, 20));
        baselinePosition.put("jSpinner1-104-20", new Integer(14));
        compBounds.put("jComboBox1", new Rectangle(386, 293, 104, 20));
        baselinePosition.put("jComboBox1-104-20", new Integer(14));
        compBounds.put("jButton2", new Rectangle(10, 295, 73, 23));
        baselinePosition.put("jButton2-73-23", new Integer(15));
        compBounds.put("jTextField2", new Rectangle(93, 296, 186, 20));
        baselinePosition.put("jTextField2-186-20", new Integer(14));
        compBounds.put("jButton1", new Rectangle(10, 254, 73, 23));
        baselinePosition.put("jButton1-73-23", new Integer(15));
        compBounds.put("jTextField1", new Rectangle(93, 255, 186, 20));
        baselinePosition.put("jTextField1-186-20", new Integer(14));
        compMinSize.put("Form", new Dimension(321, 312));
        compBounds.put("Form", new Rectangle(0, 0, 500, 329));
        prefPadding.put("jScrollPane1-jLabel1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        prefPadding.put("jSpinner1-jComboBox1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jSpinner1-jComboBox1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jSpinner1-jComboBox1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jSpinner1-jComboBox1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        compPrefSize.put("jScrollPane1", new Dimension(35, 130));
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
        lm.removeComponent("jSpinner1", true);
        prefPadding.put("jScrollPane1-jLabel1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        prefPaddingInParent.put("Form-jButton2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jTextField2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        compPrefSize.put("jScrollPane1", new Dimension(35, 130));
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 500, 329));
        contInterior.put("Form", new Rectangle(0, 0, 500, 329));
        compBounds.put("jScrollPane1", new Rectangle(10, 11, 69, 89));
        baselinePosition.put("jScrollPane1-69-89", new Integer(0));
        compBounds.put("jRadioButton3", new Rectangle(397, 77, 93, 23));
        baselinePosition.put("jRadioButton3-93-23", new Integer(15));
        compBounds.put("jRadioButton1", new Rectangle(397, 28, 93, 23));
        baselinePosition.put("jRadioButton1-93-23", new Integer(15));
        compBounds.put("jRadioButton2", new Rectangle(397, 51, 93, 23));
        baselinePosition.put("jRadioButton2-93-23", new Integer(15));
        compBounds.put("jLabel1", new Rectangle(89, 11, 401, 14));
        baselinePosition.put("jLabel1-401-14", new Integer(11));
        compBounds.put("jComboBox1", new Rectangle(386, 293, 104, 20));
        baselinePosition.put("jComboBox1-104-20", new Integer(14));
        compBounds.put("jButton2", new Rectangle(10, 295, 73, 23));
        baselinePosition.put("jButton2-73-23", new Integer(15));
        compBounds.put("jTextField2", new Rectangle(93, 296, 186, 20));
        baselinePosition.put("jTextField2-186-20", new Integer(14));
        compBounds.put("jButton1", new Rectangle(10, 254, 73, 23));
        baselinePosition.put("jButton1-73-23", new Integer(15));
        compBounds.put("jTextField1", new Rectangle(93, 255, 186, 20));
        baselinePosition.put("jTextField1-186-20", new Integer(14));
        compMinSize.put("Form", new Dimension(289, 312));
        compBounds.put("Form", new Rectangle(0, 0, 500, 329));
        prefPadding.put("jScrollPane1-jLabel1-0-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        prefPaddingInParent.put("Form-jButton2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jTextField2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        compPrefSize.put("jScrollPane1", new Dimension(35, 130));
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
    }
}
