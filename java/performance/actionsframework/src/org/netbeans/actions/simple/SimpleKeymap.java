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
