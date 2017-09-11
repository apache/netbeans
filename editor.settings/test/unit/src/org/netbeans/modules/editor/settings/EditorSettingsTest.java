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

package org.netbeans.modules.editor.settings;

import java.awt.event.KeyEvent;
import java.io.IOException;
import javax.swing.KeyStroke;
import org.netbeans.api.editor.settings.MultiKeyBinding;
import org.netbeans.junit.NbTestCase;

/** Testing basic functionality of Editor Settings API
 *
 *  @author Martin Roskanin
 */
public class EditorSettingsTest extends NbTestCase {

    public EditorSettingsTest(String testName) {
        super(testName);
    }

    public void testMultiKeyBindingsEquality() throws IOException{
        // simple equals test
        MultiKeyBinding one = new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.CTRL_DOWN_MASK), "actionCTRL+M");
        MultiKeyBinding two = new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.CTRL_DOWN_MASK), "actionCTRL+M");
        testEquality(one, two, true);
        
        // test keys with one KeyStroke versus key
        KeyStroke ks [] = new KeyStroke[]{KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.CTRL_DOWN_MASK)};
        one = new MultiKeyBinding(ks, "actionCTRL+M");
        two = new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.CTRL_DOWN_MASK), "actionCTRL+M");        
        testEquality(one, two, true);
        
        // different action bound, should fail
        one = new MultiKeyBinding(ks, "actionCTRL+M");
        two = new MultiKeyBinding(
                KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.CTRL_DOWN_MASK),
                "actionCTRL+M_differentAction");
        testEquality(one, two, false);
        
        // simple equals for multikeybings
        KeyStroke ks2 [] = new KeyStroke[]{KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.CTRL_DOWN_MASK),
                KeyStroke.getKeyStroke(KeyEvent.VK_F, 0),
                KeyStroke.getKeyStroke(KeyEvent.VK_2, 0),
                KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.ALT_DOWN_MASK)
        };
        KeyStroke ks3 [] = new KeyStroke[]{KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.CTRL_DOWN_MASK),
                KeyStroke.getKeyStroke(KeyEvent.VK_F, 0),
                KeyStroke.getKeyStroke(KeyEvent.VK_2, 0),
                KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.ALT_DOWN_MASK)
        };
        
        one = new MultiKeyBinding(ks2, "multiOne");
        two = new MultiKeyBinding(ks3, "multiOne");
        testEquality(one, two, true);
        
        // testing different sequence (swapping VK_F and VK_2), should fail
        KeyStroke ks4 [] = new KeyStroke[]{KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.CTRL_DOWN_MASK),
                KeyStroke.getKeyStroke(KeyEvent.VK_2, 0),
                KeyStroke.getKeyStroke(KeyEvent.VK_F, 0),                
                KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.ALT_DOWN_MASK)
        };
        one = new MultiKeyBinding(ks3, "multiOne");
        two = new MultiKeyBinding(ks4, "multiOne");
        testEquality(one, two, false);
        
        // testing different modifier, should fail
        KeyStroke ks5 [] = new KeyStroke[]{KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.CTRL_DOWN_MASK),
                KeyStroke.getKeyStroke(KeyEvent.VK_2, 0),
                KeyStroke.getKeyStroke(KeyEvent.VK_F, 0),                
                KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.SHIFT_DOWN_MASK)
        };
        one = new MultiKeyBinding(ks4, "multiOne");
        two = new MultiKeyBinding(ks5, "multiOne");
        testEquality(one, two, false);
        
        // testing different action, should fail
        one = new MultiKeyBinding(ks5, "multiOne");
        two = new MultiKeyBinding(ks5, "multiOne_different");
        testEquality(one, two, false);
        
        // testing action null, should fail
        boolean failed = false;
        try {
            one = new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.CTRL_DOWN_MASK), null);
        } catch (NullPointerException npe){
            failed = true;
        }
        assertTrue(failed);
        
        // testing key null, should fail
        failed = false;
        try {
            one = new MultiKeyBinding((KeyStroke) null, "actionName");
        } catch (NullPointerException npe){
            failed = true;
        }
        assertTrue(failed);

        // testing keys null, should fail
        failed = false;
        try {
            one = new MultiKeyBinding((KeyStroke[]) null, "actionName");
        } catch (NullPointerException npe){
            failed = true;
        }
        assertTrue(failed);
        
    }

    private void testEquality(MultiKeyBinding m1, MultiKeyBinding m2, boolean equals){
        assertTrue(equals == m2.equals(m1));
        assertTrue(equals == m1.equals(m2));
    }
}
