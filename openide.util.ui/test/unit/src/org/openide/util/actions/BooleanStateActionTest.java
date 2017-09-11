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

package org.openide.util.actions;

import org.netbeans.junit.NbTestCase;
import org.openide.util.HelpCtx;
import org.openide.util.test.MockPropertyChangeListener;

/** Test that boolean actions are in fact toggled.
 * @author Jesse Glick
 */
public class BooleanStateActionTest extends NbTestCase {

    public BooleanStateActionTest(String name) {
        super(name);
    }

    /** Self-explanatory, hopefully. */
    public void testToggle() throws Exception {
        BooleanStateAction a1 = (BooleanStateAction)SystemAction.get(SimpleBooleanAction1.class);
        assertTrue(a1.isEnabled());
        BooleanStateAction a2 = (BooleanStateAction)SystemAction.get(SimpleBooleanAction2.class);
        assertTrue(a1.getBooleanState());
        assertFalse(a2.getBooleanState());
        MockPropertyChangeListener l = new MockPropertyChangeListener();
        a1.addPropertyChangeListener(l);
        a1.actionPerformed(null);
        l.expectEvent(BooleanStateAction.PROP_BOOLEAN_STATE, 1500);
        assertFalse(a1.getBooleanState());
        a1.removePropertyChangeListener(l);
        l.reset();//l.gotit = 0;
        a2.addPropertyChangeListener(l);
        a2.actionPerformed(null);
        l.expectEvent(BooleanStateAction.PROP_BOOLEAN_STATE, 1500);
        assertTrue(a2.getBooleanState());
        a2.removePropertyChangeListener(l);
    }
    
    public static final class SimpleBooleanAction1 extends BooleanStateAction {
        public String getName() {
            return "SimpleBooleanAction1";
        }
        public HelpCtx getHelpCtx() {
            return null;
        }
        protected boolean asynchronous() {
            return false;
        }
    }
    
    public static final class SimpleBooleanAction2 extends BooleanStateAction {
        protected void initialize() {
            super.initialize();
            setBooleanState(false);
        }
        public String getName() {
            return "SimpleBooleanAction2";
        }
        public HelpCtx getHelpCtx() {
            return null;
        }
        protected boolean asynchronous() {
            return false;
        }
    }
    
}
