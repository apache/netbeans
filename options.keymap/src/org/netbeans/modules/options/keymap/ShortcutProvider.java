/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
