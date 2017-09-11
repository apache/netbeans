/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.debugger.jpda.js.vars;

import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.LocalVariable;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Variable;

/**
 *
 * @author Martin
 */
public class JSVariable {
    
    private final JPDADebugger debugger;
    private final Variable valueInfoDesc;
    private final String key;
    private final String value;
    private final boolean expandable;
    private final ObjectVariable valueObject;
    
    protected JSVariable(JPDADebugger debugger, Variable valueInfoDesc) {
        this.debugger = debugger;
        this.valueInfoDesc = valueInfoDesc;
        value = getStringValue(debugger, valueInfoDesc);
        key = DebuggerSupport.getDescriptionKey(valueInfoDesc);
        expandable = DebuggerSupport.isDescriptionExpandable(valueInfoDesc);
        ObjectVariable valueObject = null;
        if (!expandable) {
            // Check if it's a script object:
            Variable valueObjectVar = DebuggerSupport.getDescriptionValueObject(valueInfoDesc);
            if (valueObjectVar instanceof ObjectVariable) {
                JPDAClassType classType = ((ObjectVariable) valueObjectVar).getClassType();
                if (classType != null) {
                    String className = classType.getName();
                    if (!className.startsWith("jdk.nashorn") && !String.class.getName().equals(className)) {   // NOI18N
                        // Not a Nashorn's script class
                        valueObject = (ObjectVariable) valueObjectVar;
                    }
                }
            }
        }
        this.valueObject = valueObject;
    }
    
    private static String getStringValue(JPDADebugger debugger, Variable valueInfoDesc) {
        String value = DebuggerSupport.getDescriptionValue(valueInfoDesc);
        if ("{}".equals(value)) { // an object
            Variable valueObject = DebuggerSupport.getDescriptionValueObject(valueInfoDesc);
            value = DebuggerSupport.getVarValue(debugger, valueObject);
        }
        return value;
    }
    
    public static JSVariable[] createScopeVars(JPDADebugger debugger, Variable scope) {
        String value = scope.getValue();
        if ("null".equals(value)) {
            return new JSVariable[] {};
        }
        Variable[] valueInfos = DebuggerSupport.getValueInfos(debugger, scope, false);
        if (valueInfos == null) {
            return new JSVariable[] {};
        }
        int n = valueInfos.length;
        JSVariable[] jVars = new JSVariable[n];
        for (int i = 0; i < n; i++) {
            jVars[i] = new JSVariable(debugger, valueInfos[i]);
        }
        return jVars;
    }
    
    public static JSVariable create(JPDADebugger debugger, LocalVariable lv) {
        Variable valueInfoDesc = DebuggerSupport.getValueInfoDesc(debugger, lv.getName(), lv, false); // NOI18N
        if (valueInfoDesc == null) {
            return null;
        }
        return new JSVariable(debugger, valueInfoDesc);
    }
    
    public static JSVariable createIfScriptObject(JPDADebugger debugger, ObjectVariable ov, String name) {
        JPDAClassType classType = ov.getClassType();
        if (classType == null) {
            return null;
        }
        boolean isScript = classType.isInstanceOf("jdk.nashorn.internal.runtime.ScriptObject"); // NOI18N
        if (!isScript) {
            return null;
        }
        Variable valueInfoDesc = DebuggerSupport.getValueInfoDesc(debugger, name, ov, false);
        if (valueInfoDesc == null) {
            return null;
        }
        return new JSVariable(debugger, valueInfoDesc);
    }

    public String getKey() {
        return key;
    }
    
    public String getValue() {
        return value;
    }
    
    /**
     * @return value object for non-JavaScript values.
     */
    public ObjectVariable getValueObject() {
        return valueObject;
    }
    
    public boolean isExpandable() {
        return expandable;
    }
    
    public JSVariable[] getChildren() {
        Variable descriptionValueObject = DebuggerSupport.getDescriptionValueObject(valueInfoDesc);
        return createScopeVars(debugger, descriptionValueObject);
    }
    
}
