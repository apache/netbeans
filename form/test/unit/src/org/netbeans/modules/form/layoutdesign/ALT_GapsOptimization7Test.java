/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of the
 * License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing the
 * software, include this License Header Notice in each file and include the
 * License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by Oracle
 * in the GPL Version 2 section of the License file that accompanied this code.
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL or only
 * the GPL Version 2, indicate your decision by adding "[Contributor] elects to
 * include this software in this distribution under the [CDDL or GPL Version 2]
 * license." If you do not indicate a single choice of license, a recipient has
 * the option to distribute your version of this file under either the CDDL, the
 * GPL Version 2 or to extend the choice of license to its licensees as provided
 * above. However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is made
 * subject to such option by the copyright holder.
 */
package org.netbeans.modules.form.layoutdesign;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.*;
import org.openide.filesystems.FileUtil;

public class ALT_GapsOptimization7Test extends LayoutTestCase {

    public ALT_GapsOptimization7Test(String name) {
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
     * Delete jCheckBox1. According to bug 202707.
     */
    public void doChanges0() {
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 438, 263));
        contInterior.put("Form", new Rectangle(0, 0, 438, 263));
        compBounds.put("jPanel1", new Rectangle(10, 11, 418, 241));
        baselinePosition.put("jPanel1-418-241", new Integer(0));
        contInterior.put("jPanel1", new Rectangle(10, 11, 418, 241));
        compBounds.put("jScrollPane1", new Rectangle(20, 22, 175, 93));
        baselinePosition.put("jScrollPane1-175-93", new Integer(0));
        compBounds.put("jCheckBox1", new Rectangle(341, 22, 81, 23));
        baselinePosition.put("jCheckBox1-81-23", new Integer(15));
        compBounds.put("jRadioButton1", new Rectangle(16, 201, 93, 23));
        baselinePosition.put("jRadioButton1-93-23", new Integer(15));
        compMinSize.put("jPanel1", new Dimension(414, 157));
        compBounds.put("jPanel1", new Rectangle(10, 11, 418, 241));
        compPrefSize.put("jPanel1", new Dimension(418, 241));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        prefPadding.put("jCheckBox1-jRadioButton1-1-0-0", new Integer(0)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox1-jRadioButton1-1-0-1", new Integer(3)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox1-jRadioButton1-1-0-2", new Integer(0)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox1-jRadioButton1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jRadioButton1-1-0-0", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jRadioButton1-1-0-1", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jRadioButton1-1-0-2", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jRadioButton1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        compMinSize.put("Form", new Dimension(434, 179));
        compBounds.put("Form", new Rectangle(0, 0, 438, 263));
        compPrefSize.put("jPanel1", new Dimension(418, 241));
        compPrefSize.put("jPanel1", new Dimension(418, 241));
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
        lm.removeComponent("jCheckBox1", true);
        prefPaddingInParent.put("jPanel1-jRadioButton1-0-1", new Integer(6)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jScrollPane1-jRadioButton1-1-0-0", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jRadioButton1-1-0-1", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jRadioButton1-1-0-2", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jRadioButton1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 438, 263));
        contInterior.put("Form", new Rectangle(0, 0, 438, 263));
        compBounds.put("jPanel1", new Rectangle(10, 11, 418, 241));
        baselinePosition.put("jPanel1-418-241", new Integer(0));
        contInterior.put("jPanel1", new Rectangle(10, 11, 418, 241));
        compBounds.put("jScrollPane1", new Rectangle(20, 22, 175, 93));
        baselinePosition.put("jScrollPane1-175-93", new Integer(0));
        compBounds.put("jRadioButton1", new Rectangle(16, 201, 93, 23));
        baselinePosition.put("jRadioButton1-93-23", new Integer(15));
        compMinSize.put("jPanel1", new Dimension(414, 157));
        compBounds.put("jPanel1", new Rectangle(10, 11, 418, 241));
        compPrefSize.put("jPanel1", new Dimension(418, 241));
        prefPaddingInParent.put("jPanel1-jRadioButton1-0-1", new Integer(6)); // parentId-compId-dimension-compAlignment
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        prefPadding.put("jScrollPane1-jRadioButton1-1-0-0", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jRadioButton1-1-0-1", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jRadioButton1-1-0-2", new Integer(2)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jRadioButton1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        compMinSize.put("Form", new Dimension(434, 179));
        compBounds.put("Form", new Rectangle(0, 0, 438, 263));
        compPrefSize.put("jPanel1", new Dimension(418, 241));
        compPrefSize.put("jPanel1", new Dimension(418, 241));
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
    }
}
