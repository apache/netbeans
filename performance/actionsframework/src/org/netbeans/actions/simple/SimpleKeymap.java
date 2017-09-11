/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004, 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
/*
 * SimpleKeymap.java
 *
 * Created on January 27, 2004, 1:29 AM
 */

package org.netbeans.actions.simple;

import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.text.Keymap;

/**
 *
 * @author  tim
 */
public class SimpleKeymap implements Keymap {
    private Interpreter interp;
    private SimpleEngine engine;
    /** Creates a new instance of SimpleKeymap */
    SimpleKeymap(SimpleEngine engine, Interpreter interp) {
        this.interp = interp;
        this.engine = engine;
    }

    public void addActionForKeyStroke(javax.swing.KeyStroke key, javax.swing.Action a) {
        //XXX probably want to proxy some standard keymap to support this
        throw new UnsupportedOperationException();
    }

    public javax.swing.Action getAction(javax.swing.KeyStroke key) {
        String action = interp.getActionForKeystroke(key);
        if (action != null) {
            return engine.getAction(action);
        } else {
            return null;
        }
    }

    public javax.swing.Action[] getBoundActions() {
        String[] s = interp.getAllActionsBoundToKeystrokes();
        Action[] result = new Action[s.length];
        for (int i=0; i < s.length; i++) {
            result[i] = engine.getAction(s[i]);
        }
        return result;
    }
    
    public javax.swing.KeyStroke[] getBoundKeyStrokes() {
        return interp.getAllKeystrokes();
    }
    
    public javax.swing.Action getDefaultAction() {
        return null; //XXX
    }
    
    public javax.swing.KeyStroke[] getKeyStrokesForAction(javax.swing.Action a) {
        //heck with multiple keystroke support for now
        return new KeyStroke[] {interp.getKeyStrokeForAction((String) a.getValue(a.ACTION_COMMAND_KEY))}; 
    }
    
    public String getName() {
        return "George";
    }
    
    public Keymap getResolveParent() {
        return parent;
    }
    
    public boolean isLocallyDefined(javax.swing.KeyStroke key) {
        return true;
    }
    
    public void removeBindings() {
        throw new UnsupportedOperationException();
    }
    
    public void removeKeyStrokeBinding(javax.swing.KeyStroke keys) {
        throw new UnsupportedOperationException();
    }
    
    public void setDefaultAction(javax.swing.Action a) {
        throw new UnsupportedOperationException();
    }
    
    Keymap parent = null;
    public void setResolveParent(Keymap parent) {
        this.parent = parent;
    }
    
}
