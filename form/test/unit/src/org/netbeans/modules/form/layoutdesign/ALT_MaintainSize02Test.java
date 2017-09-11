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
 * Starts with a form that has the opposite style of using zero gaps than the
 * default preference - to make sure it can handle such case as well. The same
 * style is used in ALT_MaintainSize03Test where the initial state is also
 * built during the test.
 */
public class ALT_MaintainSize02Test extends LayoutTestCase {

    private Object changeMark;

    public ALT_MaintainSize02Test(String name) {
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
        lm.setChangeRecording(true);
        changeMark = lm.getChangeMark();
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jButton1", new Rectangle(317, 11, 73, 23));
        baselinePosition.put("jButton1-73-23", new Integer(15));
        compBounds.put("jTextField1", new Rectangle(10, 40, 380, 20));
        baselinePosition.put("jTextField1-380-20", new Integer(14));
        compBounds.put("jButton2", new Rectangle(10, 66, 73, 23));
        baselinePosition.put("jButton2-73-23", new Integer(15));
        compPrefSize.put("jButton1", new Dimension(73, 23));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compPrefSize.put("jButton2", new Dimension(73, 23));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jButton1", new Rectangle(317, 11, 73, 23));
        baselinePosition.put("jButton1-73-23", new Integer(15));
        compBounds.put("jTextField1", new Rectangle(10, 40, 380, 20));
        baselinePosition.put("jTextField1-380-20", new Integer(14));
        compBounds.put("jButton2", new Rectangle(10, 66, 73, 23));
        baselinePosition.put("jButton2-73-23", new Integer(15));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
        lm.removeComponent("jTextField1", true);
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jButton1", new Rectangle(317, 11, 73, 23));
        baselinePosition.put("jButton1-73-23", new Integer(15));
        compBounds.put("jButton2", new Rectangle(10, 66, 73, 23));
        baselinePosition.put("jButton2-73-23", new Integer(15));
        compPrefSize.put("jButton1", new Dimension(73, 23));
        compPrefSize.put("jButton2", new Dimension(73, 23));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jButton1", new Rectangle(317, 11, 73, 23));
        baselinePosition.put("jButton1-73-23", new Integer(15));
        compBounds.put("jButton2", new Rectangle(10, 66, 73, 23));
        baselinePosition.put("jButton2-73-23", new Integer(15));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }

    /**
     * Undo text field's deletion. Delete jButton2.
     */
    public void doChanges1() {
        lm.undo(changeMark, lm.getChangeMark());
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jButton1", new Rectangle(317, 11, 73, 23));
        baselinePosition.put("jButton1-73-23", new Integer(15));
        compBounds.put("jTextField1", new Rectangle(10, 40, 380, 20));
        baselinePosition.put("jTextField1-380-20", new Integer(14));
        compBounds.put("jButton2", new Rectangle(10, 66, 73, 23));
        baselinePosition.put("jButton2-73-23", new Integer(15));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
        lm.removeComponent("jButton2", true);
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jButton1", new Rectangle(317, 11, 73, 23));
        baselinePosition.put("jButton1-73-23", new Integer(15));
        compBounds.put("jTextField1", new Rectangle(10, 40, 380, 20));
        baselinePosition.put("jTextField1-380-20", new Integer(14));
        compPrefSize.put("jButton1", new Dimension(73, 23));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jButton1", new Rectangle(317, 11, 73, 23));
        baselinePosition.put("jButton1-73-23", new Integer(15));
        compBounds.put("jTextField1", new Rectangle(10, 40, 380, 20));
        baselinePosition.put("jTextField1-380-20", new Integer(14));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }

    /**
     * Delete jTextField1.
     */
    public void doChanges2() {
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jButton1", new Rectangle(317, 11, 73, 23));
        baselinePosition.put("jButton1-73-23", new Integer(15));
        compPrefSize.put("jButton1", new Dimension(73, 23));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jButton1", new Rectangle(317, 11, 73, 23));
        baselinePosition.put("jButton1-73-23", new Integer(15));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
        lm.removeComponent("jTextField1", true);
    }

}
