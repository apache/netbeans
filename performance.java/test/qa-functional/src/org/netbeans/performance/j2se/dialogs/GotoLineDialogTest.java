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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.performance.j2se.dialogs;

import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2se.setup.J2SESetup;
import org.netbeans.modules.performance.utilities.CommonUtilities;

import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;

import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;

/**
 * Test of Go To Line dialog.
 *
 * @author  mmirilovic@netbeans.org
 */
public class GotoLineDialogTest extends PerformanceTestCase {

    private static EditorOperator editor;
    private String TITLE;
    
    /** Creates a new instance of GotoLineDialog */
    public GotoLineDialogTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /** Creates a new instance of GotoLineDialog */
    public GotoLineDialogTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.createConfiguration(J2SESetup.class)
                .addTest(GotoLineDialogTest.class)
                .enableModules(".*").clusters("ide|java|apisupport").suite());
        return suite;
    }

    public void testGotoLineDialog() {
        doMeasurement();
    }
    
    @Override
    public void initialize() {
        TITLE = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.editor.Bundle", "goto-title");
        editor = CommonUtilities.openFile("PerformanceTestData","org.netbeans.test.performance","Main20kB.java", true);
    }
    
    public void prepare() {
   }
    
    public ComponentOperator open(){
        new ActionNoBlock(null, null, KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_MASK)).performShortcut(editor);
        return new NbDialogOperator(TITLE); // NOI18N
    }
    
    @Override
    public void shutdown(){
        if(editor!=null && editor.isShowing()) {
            editor.closeDiscard();
        }
    }
    
}
