/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
