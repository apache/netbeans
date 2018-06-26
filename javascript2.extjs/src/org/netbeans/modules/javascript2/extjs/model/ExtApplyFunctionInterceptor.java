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
package org.netbeans.modules.javascript2.extjs.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.regex.Pattern;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.javascript2.types.api.DeclarationScope;
import org.netbeans.modules.javascript2.model.api.JsObject;
import org.netbeans.modules.javascript2.model.api.Occurrence;
import org.netbeans.modules.javascript2.types.api.TypeUsage;
import org.netbeans.modules.javascript2.model.spi.FunctionArgument;
import org.netbeans.modules.javascript2.model.spi.FunctionInterceptor;
import org.netbeans.modules.javascript2.model.spi.ModelElementFactory;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 *
 * @author Petr Pisl
 */
@FunctionInterceptor.Registration(priority = 9)
public class ExtApplyFunctionInterceptor implements FunctionInterceptor {

    @Override
    public Pattern getNamePattern() {
        return Pattern.compile("Ext\\.apply");
    }

    @Override
    public Collection<TypeUsage> intercept(Snapshot snapshot, String functionName, JsObject globalObject,
            DeclarationScope scope, ModelElementFactory factory, Collection<FunctionArgument> args) {
        if (args.size() == 2) {
            Iterator<FunctionArgument> iterator = args.iterator();
            FunctionArgument arg1 = iterator.next();
            FunctionArgument arg2 = iterator.next();
            int offset = arg1.getOffset();
            if (arg1.getKind() == FunctionArgument.Kind.REFERENCE && arg2.getKind() == FunctionArgument.Kind.ANONYMOUS_OBJECT) {
                JsObject parent = globalObject;
                JsObject oldParent = parent;

                if (arg1.getValue() instanceof Collection) {
                    for (String name : (Collection<String>) arg1.getValue()) {
                        JsObject jsObject = oldParent.getProperty(name);
                        OffsetRange offsetRange = new OffsetRange(offset, offset + name.length());
                        if (jsObject == null) {
                            jsObject = factory.newObject(parent, name, offsetRange, true);
                            parent.addProperty(name, jsObject);
                            oldParent = jsObject;
                        } else if (!jsObject.isDeclared()) {
                            JsObject newJsObject = factory.newObject(parent, name, offsetRange, true);
                            parent.addProperty(name, newJsObject);
                            for (Occurrence occurrence : jsObject.getOccurrences()) {
                                newJsObject.addOccurrence(occurrence.getOffsetRange());
                            }
                            newJsObject.addOccurrence(jsObject.getDeclarationName().getOffsetRange());
                            oldParent = jsObject;
                            jsObject = newJsObject;
                        }

                        parent = jsObject;
                        offset += name.length() + 1;
                    }
                }

                JsObject definedObject = (JsObject) arg2.getValue();
                if (definedObject.getModifiers().remove(org.netbeans.modules.csl.api.Modifier.PRIVATE)) {
                    definedObject.getModifiers().add(org.netbeans.modules.csl.api.Modifier.PUBLIC);
                }


                for (JsObject property : definedObject.getProperties().values()) {
                    if (property.isDeclared()) {
                        parent.addProperty(property.getName(), factory.newReference(parent, property.getName(), property.getDeclarationName().getOffsetRange(), property, true, property.getModifiers()));
                    }
                }
            }
        }
        return Collections.emptyList();
    }
}
