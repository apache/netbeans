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

package org.netbeans.modules.debugger.jpda.jsui.vars.models;

import java.beans.Customizer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAWatch;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.modules.debugger.jpda.js.vars.JSVariable;

/**
 *
 * @author Martin
 */
final class JSWatchVar implements PropertyChangeListener {
    
    private final JPDADebugger debugger;
    private final JPDAWatch watch;
    private JSVariable jsVar;
    private boolean hasJSVar;
    
    JSWatchVar(JPDADebugger debugger, JPDAWatch watch) {
        this.debugger = debugger;
        this.watch = watch;
        //((Refreshable) watch).isCurrent();
        ((Customizer) watch).addPropertyChangeListener(this);
    }
    
    static boolean is(Object var) {
        return var instanceof ObjectVariable && var instanceof JPDAWatch;
    }
    
    JPDAWatch getWatch() {
        return watch;
    }
    
    synchronized JSVariable getJSVar() {
        if (!hasJSVar && jsVar == null) {
            jsVar = JSVariable.createIfScriptObject(debugger, (ObjectVariable) watch, watch.getExpression());
            hasJSVar = true;
        }
        return jsVar;
    }
    
    JSVariable getJSVarIfExists() {
        return jsVar;
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("value".equals(evt.getPropertyName())) {
            synchronized (this) {
                jsVar = null;
                hasJSVar = false;
            }
        }
    }
    
    
}
