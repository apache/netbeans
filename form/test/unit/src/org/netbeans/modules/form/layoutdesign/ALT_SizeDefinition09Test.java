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

public class ALT_SizeDefinition09Test extends LayoutTestCase {

    public ALT_SizeDefinition09Test(String name) {
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
     * Enter longer text into jLabel1. It should be less than the the textfield
     * next to it can accommodate (but close).
     * As a result, the size of the textfield should be unchanged. In reality it
     * would than adjust in the live layout with unchanged form size (and shrink
     * from 120 to 90 pixels that would be then applied to the definition), but
     * in the test we can't do real build of the layout, so checking the state
     * before it (i.e. unchanged).
     * (This step would fail in NetBeans 7.2, incorrectly calculating what change
     * can be absorbed without form size change.)
     */
    public void doChanges0() {
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 426, 300));
        contInterior.put("Form", new Rectangle(0, 0, 426, 300));
        compBounds.put("jLabel1", new Rectangle(66, 167, 136, 14));
        baselinePosition.put("jLabel1-136-14", new Integer(11));
        compBounds.put("jTextField1", new Rectangle(206, 164, 120, 20));
        baselinePosition.put("jTextField1-120-20", new Integer(14));
        compBounds.put("jSlider1", new Rectangle(77, 135, 170, 23));
        baselinePosition.put("jSlider1-170-23", new Integer(0));
        compBounds.put("jButton1", new Rectangle(253, 135, 73, 23));
        baselinePosition.put("jButton1-73-23", new Integer(15));
        compBounds.put("jToggleButton1", new Rectangle(147, 106, 179, 23));
        baselinePosition.put("jToggleButton1-179-23", new Integer(15));
        compMinSize.put("Form", new Dimension(415, 195));
        compBounds.put("Form", new Rectangle(0, 0, 426, 300));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        prefPaddingInParent.put("Form-jLabel1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jTextField1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
        prefPaddingInParent.put("Form-jLabel1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jTextField1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jTextField1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        compPrefSize.put("jLabel1", new Dimension(166, 14));
        ld.componentDefaultSizeChanged("jLabel1");
    }

    /**
     * Enter even longer text into jLabel1 so the textfield next to it can't
     * absorb the growth.
     * As a result the textfield size should be set to default. In reality it
     * would also rebuild the form with computing new form size - the form would
     * grow not to shrink textfield under its default size. We can't test the
     * real layout behavior in this test, so at least checking the reset of the
     * textfield to default size.
     * (This step would fail if nothing is done as reaction to component size
     * change as a result of a property change.)
     */
    public void doChanges1() {
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 426, 300));
        contInterior.put("Form", new Rectangle(0, 0, 426, 300));
        compBounds.put("jLabel1", new Rectangle(66, 167, 166, 14));
        baselinePosition.put("jLabel1-166-14", new Integer(11));
        compBounds.put("jTextField1", new Rectangle(236, 164, 90, 20));
        baselinePosition.put("jTextField1-90-20", new Integer(14));
        compBounds.put("jSlider1", new Rectangle(77, 135, 170, 23));
        baselinePosition.put("jSlider1-170-23", new Integer(0));
        compBounds.put("jButton1", new Rectangle(253, 135, 73, 23));
        baselinePosition.put("jButton1-73-23", new Integer(15));
        compBounds.put("jToggleButton1", new Rectangle(147, 106, 179, 23));
        baselinePosition.put("jToggleButton1-179-23", new Integer(15));
        compMinSize.put("Form", new Dimension(415, 195));
        compBounds.put("Form", new Rectangle(0, 0, 426, 300));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        prefPaddingInParent.put("Form-jLabel1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jTextField1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        compBounds.put("Form", new Rectangle(0, 0, 426, 300));
        contInterior.put("Form", new Rectangle(0, 0, 426, 300));
        compBounds.put("jLabel1", new Rectangle(66, 167, 166, 14));
        baselinePosition.put("jLabel1-166-14", new Integer(11));
        compBounds.put("jTextField1", new Rectangle(236, 164, 90, 20));
        baselinePosition.put("jTextField1-90-20", new Integer(14));
        compBounds.put("jSlider1", new Rectangle(77, 135, 170, 23));
        baselinePosition.put("jSlider1-170-23", new Integer(0));
        compBounds.put("jButton1", new Rectangle(253, 135, 73, 23));
        baselinePosition.put("jButton1-73-23", new Integer(15));
        compBounds.put("jToggleButton1", new Rectangle(147, 106, 179, 23));
        baselinePosition.put("jToggleButton1-179-23", new Integer(15));
        compMinSize.put("Form", new Dimension(415, 195));
        compBounds.put("Form", new Rectangle(0, 0, 426, 300));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        prefPaddingInParent.put("Form-jLabel1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jTextField1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
        compPrefSize.put("jLabel1", new Dimension(216, 14));
        ld.componentDefaultSizeChanged("jLabel1");
    }
}
