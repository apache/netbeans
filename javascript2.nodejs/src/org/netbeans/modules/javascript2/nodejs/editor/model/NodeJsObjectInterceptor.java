/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
            List<JsObject> properties = new ArrayList(global.getProperties().values());
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
            List<? extends DeclarationScope> childrenScopesCopy = new ArrayList(globalScope.getChildrenScopes());
            
            for(DeclarationScope movedScope: childrenScopesCopy) {
                if (!movedScope.equals(module)) {
                    ModelUtils.changeDeclarationScope((JsObject)movedScope, module);
                }
            }
        }
    }
}
