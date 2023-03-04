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
package org.netbeans.modules.javascript2.knockout.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.javascript2.types.api.DeclarationScope;
import org.netbeans.modules.javascript2.model.api.JsFunction;
import org.netbeans.modules.javascript2.model.api.JsObject;
import org.netbeans.modules.javascript2.types.api.TypeUsage;
import org.netbeans.modules.javascript2.model.spi.FunctionArgument;
import org.netbeans.modules.javascript2.model.spi.FunctionInterceptor;
import org.netbeans.modules.javascript2.model.spi.ModelElementFactory;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 *
 * @author Petr Hejl
 */
@FunctionInterceptor.Registration(priority = 100)
public class KnockoutExtendInterceptor implements FunctionInterceptor {

    private static final Logger LOGGER = Logger.getLogger(KnockoutExtendInterceptor.class.getName());

    private static final Pattern NAME_PATTERN = Pattern.compile("ko\\.utils\\.(extend|setPrototypeOfOrExtend)"); // NOI18N

    @Override
    public Pattern getNamePattern() {
        return NAME_PATTERN;
    }

    @Override
    public Collection<TypeUsage> intercept(Snapshot snapshot, String functionName, JsObject globalObject, DeclarationScope scope,
            ModelElementFactory factory, Collection<FunctionArgument> args) {

        if (args.size() != 2) {
            return Collections.emptyList();
        }

        Iterator<FunctionArgument> iterator = args.iterator();
        FunctionArgument targetArgument = iterator.next();
        FunctionArgument valueArgument = iterator.next();

        int offset = targetArgument.getOffset();

        JsObject object = null;
        if (targetArgument.getKind() == FunctionArgument.Kind.REFERENCE) {
            List<String> identifiers = (List<String>) targetArgument.getValue();
            JsObject ref = getReference(scope, identifiers, false);
            if (ref != null) {
                JsObject found = findJsObjectByAssignment(globalObject, ref, offset);
                if (found != null) {
                    ref = found;
                }
            }
            object = ref;
        }
        JsObject value = null;
        if (valueArgument.getKind() == FunctionArgument.Kind.REFERENCE) {
            List<String> identifiers = (List<String>) valueArgument.getValue();
            JsObject ref = getReference(scope, identifiers, true);
            if (ref != null) {
                JsObject found = findJsObjectByAssignment(globalObject, ref, offset);
                if (found != null) {
                    ref = found;
                }
            }
            value = ref;
        }

        OffsetRange offsetRange = OffsetRange.NONE;
        if (object != null && value != null) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Extending {0} with {1}", new Object[]{object.getFullyQualifiedName(), value.getFullyQualifiedName()});
            }
            for (Map.Entry<String, ? extends JsObject> entry : value.getProperties().entrySet()) {
                if (functionName.equals("ko.utils.extend") || object.getProperty(entry.getKey()) == null) { //NOI18N
                    // if ko.utils.extend has been called, set the property always, in other cases only if object doesn't contain it
                    object.addProperty(entry.getKey(),
                            factory.newReference(object, entry.getKey(), offsetRange, entry.getValue(), true, null));
                }
            }
        }
        return Collections.emptyList();
    }

    private static JsObject getReference(DeclarationScope scope,
            List<String> identifier, boolean searchPrototype) {

        if ("this".equals(identifier.get(0))) { // NOI18N
            // XXX this is not exactly right as it is evaluated at runtime
            return (JsObject) scope;
        }
        DeclarationScope currentScope = scope;
        while (currentScope != null) {
            JsObject ret = getReference((JsObject) currentScope, identifier);
            if (ret != null) {
                return ret;
            }
            currentScope = currentScope.getParentScope();
        }
        if (searchPrototype && identifier.size() > 1) {
            List<String> prototype = new ArrayList<String>(identifier);
            prototype.add(prototype.size() - 1, "prototype"); // NOI18N
            return getReference(scope, prototype, false);
        }
        return null;
    }

    private static JsObject getReference(JsObject object, List<String> identifier) {
        // XXX performance
        if (object == null) {
            return null;
        }
        if (identifier.isEmpty()) {
            return object;
        }
        return getReference(object.getProperty(identifier.get(0)),
                identifier.subList(1, identifier.size()));
    }

    private static JsObject findJsObjectByAssignment(JsObject globalObject,
            JsObject value, int offset) {

        return findJsObjectByAssignment(globalObject, value, offset, true);
    }

    private static JsObject findJsObjectByAssignment(JsObject globalObject, JsObject value,
            int offset, boolean searchPrototype) {

        if (value == null) {
            return null;
        }

        JsObject ret = null;
        Collection<? extends TypeUsage> assigments = value.getAssignmentForOffset(offset);
        if (assigments.size() == 1) {
            ret = findJsObjectByName(globalObject,
                    assigments.iterator().next().getType());
        }
        // XXX multiple assignments

        if (ret == null && searchPrototype) {
            String fqn = value.getFullyQualifiedName();
            int index = fqn.lastIndexOf('.');
            if (index > 0) {
                fqn = fqn.substring(0, index) + ".prototype" + fqn.substring(index);
                JsObject obj = findJsObjectByName(globalObject, fqn);
                if (obj != null) {
                    ret = findJsObjectByAssignment(globalObject, obj, offset, false);
                }
            }
        }
        return ret;
    }

    private static JsObject findJsObjectByName(JsObject global, String fqName) {
        JsObject result = global;
        JsObject property = result;
        for (StringTokenizer stringTokenizer = new StringTokenizer(fqName, "."); stringTokenizer.hasMoreTokens() && result != null;) {
            String token = stringTokenizer.nextToken();
            property = result.getProperty(token);
            if (property == null) {
                result = (result instanceof JsFunction)
                        ? ((JsFunction)result).getParameter(token)
                        : null;
                if (result == null) {
                    break;
                }
            } else {
                result = property;
            }
        }
        return result;
    }
}
