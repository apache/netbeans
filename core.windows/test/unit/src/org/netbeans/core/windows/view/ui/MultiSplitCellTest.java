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

package org.netbeans.core.windows.view.ui;

import java.util.Locale;

import org.netbeans.junit.NbTestCase;

/** 
 * Test Mode activation behavior.
 * 
 * @author Marek Slama
 * 
 */
public class MultiSplitCellTest extends NbTestCase {

    public MultiSplitCellTest (String name) {
        super (name);
    }
    
    protected boolean runInEQ () {
        return true;
    }

    private Locale defLocale;
    @Override
    protected void setUp() throws Exception {
//        super.setUp();
//        defLocale = Locale.getDefault();
//        Locale.setDefault(new Locale("te_ST"));
    }
    
    @Override
    protected void tearDown() {
//        Locale.setDefault(defLocale);
    }
    
    public void testResizingDisabled() throws Exception {
//        assertFalse(Switches.isTopComponentResizingEnabled());
    }
    
    public void testMinimumSizeRespected() throws Exception {
//        assertTrue(Switches.isSplitterRespectMinimumSizeEnabled());
//        ViewElement ve = new ViewElement(null, 0.0) {
//
//            @Override
//            public Component getComponent() {
//                JPanel panel = new JPanel();
//                panel.setMinimumSize( new Dimension(1024,1024) );
//                return panel;
//            }
//
//            @Override
//            public boolean updateAWTHierarchy(Dimension availableSpace) {
//                throw new UnsupportedOperationException("Not supported yet.");
//            }
//        };
//        MultiSplitCell cell = new MultiSplitCell(ve, 0.5, true);
//        assertTrue( cell.getMinimumSize() == 1024 );
    }
}

