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

package org.netbeans.modules.openide.awt;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import org.openide.awt.Actions;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.actions.BooleanStateAction;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.actions.ActionPresenterProvider;

/** Default implementation of presenters for various action types.
 */
@ServiceProvider(service=ActionPresenterProvider.class)
public final class DefaultAWTBridge extends ActionPresenterProvider {
    public JMenuItem createMenuPresenter (Action action) {
        if (action instanceof BooleanStateAction) {
            BooleanStateAction b = (BooleanStateAction)action;
            return new Actions.CheckboxMenuItem (b, true);
        }
        if (action instanceof SystemAction) {
            SystemAction s = (SystemAction)action;
            return new Actions.MenuItem (s, true);
        }
            
        return new Actions.MenuItem (action, true);
    }
    
    public @Override JMenuItem createPopupPresenter(Action action) {
        JMenuItem item;
        if (action instanceof BooleanStateAction) {
            BooleanStateAction b = (BooleanStateAction)action;
            item = new Actions.CheckboxMenuItem (b, false);
        } else if (action instanceof SystemAction) {
            SystemAction s = (SystemAction)action;
            item = new Actions.MenuItem (s, false);
        } else {
            item = new Actions.MenuItem (action, false);
        }
        return item;
    }
    
    public Component createToolbarPresenter(Action action) {
        AbstractButton btn;
        if (action instanceof BooleanStateAction) {
            btn = new JToggleButton();
            Actions.connect(btn, (BooleanStateAction) action);
        } else {
            btn = new JButton();
            Actions.connect(btn, action);
        }
        return btn;
    }
    
    public JPopupMenu createEmptyPopup() {
        return new JPopupMenu();
    }  
    
    public @Override Component[] convertComponents(Component comp) {
        if (comp instanceof JMenuItem) {
            JMenuItem item = (JMenuItem) comp;
            if (Boolean.TRUE.equals(item.getClientProperty(DynamicMenuContent.HIDE_WHEN_DISABLED)) && !item.isEnabled()) {
                return new Component[0];
            }
        }
         if (comp instanceof DynamicMenuContent) {
            Component[] toRet = ((DynamicMenuContent)comp).getMenuPresenters();
            boolean atLeastOne = false;
            Collection<Component> col = new ArrayList<Component>();
            for (int i = 0; i < toRet.length; i++) {
                if (toRet[i] instanceof DynamicMenuContent && toRet[i] != comp) {
                    col.addAll(Arrays.asList(convertComponents(toRet[i])));
                    atLeastOne = true;
                } else {
                    if (toRet[i] == null) {
                        toRet[i] = new JSeparator();
                    }
                    col.add(toRet[i]);
                }
            }
            if (atLeastOne) {
                return col.toArray(new Component[col.size()]);
            } else {
                return toRet;
            }
         }
         return new Component[] {comp};
    }
    
}
