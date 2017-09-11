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

public class ALT_MaintainSize01Test extends LayoutTestCase {

    public ALT_MaintainSize01Test(String name) {
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
     * Delete jTextField1.
     */
    public void doChanges0() {
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jButton1", new Rectangle(52, 11, 300, 23));
        baselinePosition.put("jButton1-300-23", new Integer(15));
        compBounds.put("jLabel1", new Rectangle(356, 15, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compBounds.put("jButton3", new Rectangle(317, 40, 73, 23));
        baselinePosition.put("jButton3-73-23", new Integer(15));
        compBounds.put("jToggleButton1", new Rectangle(10, 68, 105, 23));
        baselinePosition.put("jToggleButton1-105-23", new Integer(15));
        compBounds.put("jButton2", new Rectangle(10, 97, 73, 23));
        baselinePosition.put("jButton2-73-23", new Integer(15));
        compBounds.put("jTextField1", new Rectangle(10, 138, 380, 20));
        baselinePosition.put("jTextField1-380-20", new Integer(14));
        compPrefSize.put("jButton1", new Dimension(73, 23));
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        compPrefSize.put("jButton3", new Dimension(73, 23));
        compPrefSize.put("jToggleButton1", new Dimension(105, 23));
        compPrefSize.put("jButton2", new Dimension(73, 23));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jButton1", new Rectangle(52, 11, 300, 23));
        baselinePosition.put("jButton1-300-23", new Integer(15));
        compBounds.put("jLabel1", new Rectangle(356, 15, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compBounds.put("jButton3", new Rectangle(317, 40, 73, 23));
        baselinePosition.put("jButton3-73-23", new Integer(15));
        compBounds.put("jToggleButton1", new Rectangle(10, 68, 105, 23));
        baselinePosition.put("jToggleButton1-105-23", new Integer(15));
        compBounds.put("jButton2", new Rectangle(10, 97, 73, 23));
        baselinePosition.put("jButton2-73-23", new Integer(15));
        compBounds.put("jTextField1", new Rectangle(10, 138, 380, 20));
        baselinePosition.put("jTextField1-380-20", new Integer(14));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
        lm.removeComponent("jTextField1", true);
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jButton1", new Rectangle(52, 11, 300, 23));
        baselinePosition.put("jButton1-300-23", new Integer(15));
        compBounds.put("jLabel1", new Rectangle(356, 15, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compBounds.put("jButton3", new Rectangle(317, 40, 73, 23));
        baselinePosition.put("jButton3-73-23", new Integer(15));
        compBounds.put("jToggleButton1", new Rectangle(10, 68, 105, 23));
        baselinePosition.put("jToggleButton1-105-23", new Integer(15));
        compBounds.put("jButton2", new Rectangle(10, 97, 73, 23));
        baselinePosition.put("jButton2-73-23", new Integer(15));
        compPrefSize.put("jButton1", new Dimension(73, 23));
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        compPrefSize.put("jButton3", new Dimension(73, 23));
        compPrefSize.put("jToggleButton1", new Dimension(105, 23));
        compPrefSize.put("jButton2", new Dimension(73, 23));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jButton1", new Rectangle(52, 11, 300, 23));
        baselinePosition.put("jButton1-300-23", new Integer(15));
        compBounds.put("jLabel1", new Rectangle(356, 15, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compBounds.put("jButton3", new Rectangle(317, 40, 73, 23));
        baselinePosition.put("jButton3-73-23", new Integer(15));
        compBounds.put("jToggleButton1", new Rectangle(10, 68, 105, 23));
        baselinePosition.put("jToggleButton1-105-23", new Integer(15));
        compBounds.put("jButton2", new Rectangle(10, 97, 73, 23));
        baselinePosition.put("jButton2-73-23", new Integer(15));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }

    /**
     * Delete jLabel1.
     */
    public void doChanges1() {
        lm.removeComponent("jLabel1", true);
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jButton1", new Rectangle(52, 11, 300, 23));
        baselinePosition.put("jButton1-300-23", new Integer(15));
        compBounds.put("jButton3", new Rectangle(317, 40, 73, 23));
        baselinePosition.put("jButton3-73-23", new Integer(15));
        compBounds.put("jToggleButton1", new Rectangle(10, 68, 105, 23));
        baselinePosition.put("jToggleButton1-105-23", new Integer(15));
        compBounds.put("jButton2", new Rectangle(10, 97, 73, 23));
        baselinePosition.put("jButton2-73-23", new Integer(15));
        compPrefSize.put("jButton1", new Dimension(73, 23));
        compPrefSize.put("jButton3", new Dimension(73, 23));
        compPrefSize.put("jToggleButton1", new Dimension(105, 23));
        compPrefSize.put("jButton2", new Dimension(73, 23));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jButton1", new Rectangle(52, 11, 300, 23));
        baselinePosition.put("jButton1-300-23", new Integer(15));
        compBounds.put("jButton3", new Rectangle(317, 40, 73, 23));
        baselinePosition.put("jButton3-73-23", new Integer(15));
        compBounds.put("jToggleButton1", new Rectangle(10, 68, 105, 23));
        baselinePosition.put("jToggleButton1-105-23", new Integer(15));
        compBounds.put("jButton2", new Rectangle(10, 97, 73, 23));
        baselinePosition.put("jButton2-73-23", new Integer(15));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }

    /**
     * Delete jButton3.
     */
    public void doChanges2() {
        lm.removeComponent("jButton3", true);
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jButton1", new Rectangle(52, 11, 300, 23));
        baselinePosition.put("jButton1-300-23", new Integer(15));
        compBounds.put("jToggleButton1", new Rectangle(10, 68, 105, 23));
        baselinePosition.put("jToggleButton1-105-23", new Integer(15));
        compBounds.put("jButton2", new Rectangle(10, 97, 73, 23));
        baselinePosition.put("jButton2-73-23", new Integer(15));
        prefPaddingInParent.put("Form-jToggleButton1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton2-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        compPrefSize.put("jButton1", new Dimension(73, 23));
        compPrefSize.put("jToggleButton1", new Dimension(105, 23));
        compPrefSize.put("jButton2", new Dimension(73, 23));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jButton1", new Rectangle(52, 11, 300, 23));
        baselinePosition.put("jButton1-300-23", new Integer(15));
        compBounds.put("jToggleButton1", new Rectangle(10, 68, 105, 23));
        baselinePosition.put("jToggleButton1-105-23", new Integer(15));
        compBounds.put("jButton2", new Rectangle(10, 97, 73, 23));
        baselinePosition.put("jButton2-73-23", new Integer(15));
        prefPaddingInParent.put("Form-jToggleButton1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton2-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        // parentId-compId-dimension-compAlignment
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }

    /**
     * Delete jButton1.
     */
    public void doChanges3() {
        lm.removeComponent("jButton1", true);
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jToggleButton1", new Rectangle(10, 68, 105, 23));
        baselinePosition.put("jToggleButton1-105-23", new Integer(15));
        compBounds.put("jButton2", new Rectangle(10, 97, 73, 23));
        baselinePosition.put("jButton2-73-23", new Integer(15));
        prefPaddingInParent.put("Form-jToggleButton1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton2-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        compPrefSize.put("jToggleButton1", new Dimension(105, 23));
        compPrefSize.put("jButton2", new Dimension(73, 23));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jToggleButton1", new Rectangle(10, 68, 105, 23));
        baselinePosition.put("jToggleButton1-105-23", new Integer(15));
        compBounds.put("jButton2", new Rectangle(10, 97, 73, 23));
        baselinePosition.put("jButton2-73-23", new Integer(15));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }

    /**
     * Delete jToggleButton1.
     */
    public void doChanges4() {
        lm.removeComponent("jToggleButton1", true);
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jButton2", new Rectangle(10, 97, 73, 23));
        baselinePosition.put("jButton2-73-23", new Integer(15));
        compPrefSize.put("jButton2", new Dimension(73, 23));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jButton2", new Rectangle(10, 97, 73, 23));
        baselinePosition.put("jButton2-73-23", new Integer(15));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }

}
