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

package org.netbeans.modules.python.debugger.gui;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;
import org.openide.util.RequestProcessor;

/**
 * A keymap that filters ENTER, ESC and TAB, which have special meaning in dialogs
 *
 * @author Martin Entlicher
 */
public class FilteredKeymap implements Keymap {

    private final javax.swing.KeyStroke enter = javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0);
    private final javax.swing.KeyStroke esc = javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0);
    private final javax.swing.KeyStroke tab = javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_TAB, 0);
    private final Keymap keyMap; // The original keymap
    
    /** Creates a new instance of FilteredKeymap */
    public FilteredKeymap(final JTextComponent component) {
        
        class KeymapUpdater implements Runnable {
            @Override
            public void run() {
                component.setKeymap(new FilteredKeymap(component));
            }
        }
        
        this.keyMap = component.getKeymap();
        component.addPropertyChangeListener("keymap", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (!(evt.getNewValue() instanceof FilteredKeymap)) {
                    // We have to do that lazily, because the property change
                    // is fired *before* the keymap is actually changed!
                    component.removePropertyChangeListener("keymap", this);
                    if (EventQueue.isDispatchThread()) {
                        EventQueue.invokeLater(new KeymapUpdater());
                    } else {
                        RequestProcessor.getDefault().post(new KeymapUpdater(), 100);
                    }
                }
            }
        });
    }
    
    @Override
    public void addActionForKeyStroke(KeyStroke key, Action a) {
        keyMap.addActionForKeyStroke(key, a);
    }
    @Override
    public Action getAction(KeyStroke key) {
        if (enter.equals(key) ||
            esc.equals(key) ||
            tab.equals(key)) {

            return null;
        } else {
            return keyMap.getAction(key);
        }
    }
    @Override
    public Action[] getBoundActions() {
        return keyMap.getBoundActions();
    }
    @Override
    public KeyStroke[] getBoundKeyStrokes() {
        return keyMap.getBoundKeyStrokes();
    }
    @Override
    public Action getDefaultAction() {
        return keyMap.getDefaultAction();
    }
    @Override
    public KeyStroke[] getKeyStrokesForAction(Action a) {
        return keyMap.getKeyStrokesForAction(a);
    }
    @Override
    public String getName() {
        return keyMap.getName()+"_Filtered"; //NOI18N
    }
    @Override
    public javax.swing.text.Keymap getResolveParent() {
        return keyMap.getResolveParent();
    }
    @Override
    public boolean isLocallyDefined(KeyStroke key) {
        if (enter.equals(key) ||
            esc.equals(key) ||
            tab.equals(key)) {
            
            return false;
        } else {
            return keyMap.isLocallyDefined(key);
        }
    }
    @Override
    public void removeBindings() {
        keyMap.removeBindings();
    }
    @Override
    public void removeKeyStrokeBinding(KeyStroke keys) {
        keyMap.removeKeyStrokeBinding(keys);
    }
    @Override
    public void setDefaultAction(Action a) {
        keyMap.setDefaultAction(a);
    }
    @Override
    public void setResolveParent(javax.swing.text.Keymap parent) {
        keyMap.setResolveParent(parent);
    }
    
}
