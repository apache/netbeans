/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.javascript2.nodejs.editor.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.javascript2.editor.api.FrameworksUtils;
import org.netbeans.modules.javascript2.types.api.DeclarationScope;
import org.netbeans.modules.javascript2.model.api.JsFunction;
import org.netbeans.modules.javascript2.model.api.JsObject;
import org.netbeans.modules.javascript2.model.api.ModelUtils;
import org.netbeans.modules.javascript2.types.api.TypeUsage;
import org.netbeans.modules.javascript2.model.spi.ModelElementFactory;
import org.netbeans.modules.javascript2.model.spi.ObjectInterceptor;
import org.netbeans.modules.javascript2.nodejs.editor.NodeJsUtils;

/**
 *
 * @author Petr Pisl
 */
@ObjectInterceptor.Registration(priority=101)
public class NodeJsObjectInterceptor implements ObjectInterceptor {
    
    
    @Override
    public void interceptGlobal(final JsObject global, final ModelElementFactory factory) {
        JsObject exports = global.getProperty(NodeJsUtils.EXPORTS);
        if (exports == null) {
            JsObject module = global.getProperty(NodeJsUtils.MODULE);
            if (module != null) {
                exports = module.getProperty(NodeJsUtils.EXPORTS);
            }
        }
        if (exports != null && exports.getProperties().size() == 0) {
            // probably there is something like var name = exports;
            // used in for example in fs.js
           
            // find the variable, where the exports global object is assigned
            for (JsObject variable : global.getProperties().values()) {
                Collection<? extends TypeUsage> assignments = variable.getAssignments();
                boolean isThis = false;
                for (TypeUsage type : assignments) {
                    if (NodeJsUtils.EXPORTS.equals(type.getType())) {
                        isThis = true;
                        break;
                    }
                }
                if (isThis) {
                    variable.clearAssignments();
                    for (TypeUsage type : assignments) {
                        if (!NodeJsUtils.EXPORTS.equals(type.getType())) {
                            variable.addAssignment(type, type.getOffset());
                        }
                    }
                    exports = factory.newReference(exports.getName(), variable, true, true);
                    exports.getParent().addProperty(exports.getName(), exports);
                    break;
                }
            }
        }
        if (exports != null) {
            JsFunction module = factory.newFunction((DeclarationScope) global, global, NodeJsUtils.FAKE_OBJECT_NAME_PREFIX + global.getName(), Collections.EMPTY_LIST, global.getOffsetRange(), null);
            module.setAnonymous(true);
            global.addProperty(module.getName(), module);
            ((DeclarationScope)global).addDeclaredScope(module);
            List<JsObject> properties = new ArrayList<>(global.getProperties().values());
            for (JsObject property : properties) {
                String propertyName = property.getName();
                if ((property.isDeclared() || NodeJsUtils.MODULE.equals(propertyName)
                        || NodeJsUtils.EXPORTS.equals(propertyName)) && !module.getName().equals(propertyName)) {
                    global.moveProperty(property.getName(), module);
                    if (property.isDeclared()) {
                        Set<Modifier> modifiers = property.getModifiers();
                        modifiers.remove(Modifier.PUBLIC);
                        modifiers.add(Modifier.PRIVATE);
                    }
                } 
            }
            DeclarationScope globalScope = (DeclarationScope)global;
            List<? extends DeclarationScope> childrenScopesCopy = new ArrayList<>(globalScope.getChildrenScopes());
            
            for(DeclarationScope movedScope: childrenScopesCopy) {
                if (!movedScope.equals(module)) {
                    ModelUtils.changeDeclarationScope((JsObject)movedScope, module);
                }
            }
        }
    }
}
