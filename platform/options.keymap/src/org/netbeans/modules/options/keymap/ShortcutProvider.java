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

package org.netbeans.modules.options.keymap;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.swing.KeyStroke;
import org.netbeans.core.options.keymap.api.KeyStrokeUtils;
import org.openide.util.Utilities;

/**
 * Provides set of meaningful shortcuts
 * @author Max Sauer
 */
public class ShortcutProvider {

    private static final int[] letters = new int[]{
        KeyEvent.VK_A,
        KeyEvent.VK_B,
        KeyEvent.VK_C,
        KeyEvent.VK_D,
        KeyEvent.VK_E,
        KeyEvent.VK_F,
        KeyEvent.VK_G,
        KeyEvent.VK_H,
        KeyEvent.VK_I,
        KeyEvent.VK_J,
        KeyEvent.VK_K,
        KeyEvent.VK_L,
        KeyEvent.VK_M,
        KeyEvent.VK_N,
        KeyEvent.VK_O,
        KeyEvent.VK_P,
        KeyEvent.VK_Q,
        KeyEvent.VK_R,
        KeyEvent.VK_S,
        KeyEvent.VK_T,
        KeyEvent.VK_U,
        KeyEvent.VK_V,
        KeyEvent.VK_W,
        KeyEvent.VK_X,
        KeyEvent.VK_Y,
        KeyEvent.VK_Z,

        KeyEvent.VK_TAB,
        KeyEvent.VK_F1,
        KeyEvent.VK_F2,
        KeyEvent.VK_F3,
        KeyEvent.VK_F4,
        KeyEvent.VK_F5,
        KeyEvent.VK_F6,
        KeyEvent.VK_F7,
        KeyEvent.VK_F8,
        KeyEvent.VK_F9,
        KeyEvent.VK_F10,

        KeyEvent.VK_BACK_SPACE,
        KeyEvent.VK_BACK_SLASH,
        KeyEvent.VK_QUOTE,
        KeyEvent.VK_BACK_QUOTE,
        KeyEvent.VK_ENTER,
        KeyEvent.VK_ESCAPE,
        KeyEvent.VK_OPEN_BRACKET,
        KeyEvent.VK_CLOSE_BRACKET,
        KeyEvent.VK_SEMICOLON,
        KeyEvent.VK_COMMA,
        KeyEvent.VK_PERIOD,
        KeyEvent.VK_SLASH,
        KeyEvent.VK_MINUS,
        KeyEvent.VK_EQUALS,
        KeyEvent.VK_SPACE,

        Utilities.isMac() ? KeyEvent.VK_HELP : KeyEvent.VK_INSERT,
        KeyEvent.VK_HOME,
        KeyEvent.VK_PAGE_UP,
        KeyEvent.VK_PAGE_DOWN,

        KeyEvent.VK_UP,
        KeyEvent.VK_DOWN,
        KeyEvent.VK_LEFT,
        KeyEvent.VK_RIGHT
    };

    private static LinkedHashSet<String> shortcutSet;

    public static Set<String> getSet() {
        if (shortcutSet == null) {
            shortcutSet = new LinkedHashSet<String>();

            //CTRL
            for (int i = 0; i < letters.length; i++) {
                shortcutSet.add(KeyStrokeUtils.getKeyStrokeAsText(KeyStroke.getKeyStroke(letters[i], InputEvent.CTRL_MASK)));
            }

            if (Utilities.isMac())
                //META
                for (int i = 0; i < letters.length; i++) {
                    shortcutSet.add(KeyStrokeUtils.getKeyStrokeAsText(KeyStroke.getKeyStroke(letters[i], InputEvent.META_MASK)));
                }
            else
                //ALT
                for (int i = 0; i < letters.length; i++) {
                    shortcutSet.add(KeyStrokeUtils.getKeyStrokeAsText(KeyStroke.getKeyStroke(letters[i], InputEvent.ALT_MASK)));
                }

            //CTRL+SHIFT
            for (int i = 0; i < letters.length; i++) {
                shortcutSet.add(KeyStrokeUtils.getKeyStrokeAsText(KeyStroke.getKeyStroke(letters[i], InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK)));

            }

            if (Utilities.isMac())
                //SHIFT+META
                for (int i = 0; i < letters.length; i++) {
                    shortcutSet.add(KeyStrokeUtils.getKeyStrokeAsText(KeyStroke.getKeyStroke(letters[i], InputEvent.SHIFT_MASK | InputEvent.META_MASK)));
                }
            else
                //SHIFT+ALT
                for (int i = 0; i < letters.length; i++) {
                    shortcutSet.add(KeyStrokeUtils.getKeyStrokeAsText(KeyStroke.getKeyStroke(letters[i], InputEvent.SHIFT_MASK | InputEvent.ALT_MASK)));
                }
        }
        return shortcutSet;
    }
}
