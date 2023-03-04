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
package org.netbeans.modules.javascript2.editor.navigation;

import java.util.IdentityHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.javascript2.model.api.JsElement;
import org.netbeans.modules.javascript2.model.api.JsFunction;
import org.netbeans.modules.javascript2.model.api.JsObject;
import org.netbeans.modules.javascript2.model.api.JsReference;
import org.netbeans.modules.javascript2.model.api.Model;
import org.netbeans.modules.javascript2.model.api.ModelUtils;
import org.netbeans.modules.javascript2.model.api.Occurrence;

/**
 *
 * @author Petr Pisl
 */
public class OccurrencesSupport {

    private static final Logger LOGGER = Logger.getLogger(OccurrencesSupport.class.getName());

    private final Model model;

    public OccurrencesSupport(Model model) {
        this.model = model;
    }

    public Occurrence getOccurrence(int offset) {
        Occurrence result;
        long start = System.currentTimeMillis();
        JsObject object = model.getGlobalObject();
        IdentityHashMap<JsObject,Void> scannedElements = new IdentityHashMap<>();
        result = findOccurrence(object, offset, scannedElements);
        scannedElements.clear();
        if (result == null) {
            result = findDeclaration(object, offset, scannedElements);
        }
        long end = System.currentTimeMillis();
        LOGGER.log(Level.FINE, "Computing getOccurences({0}) took {1}ms. Returns {2}", new Object[]{offset, end - start, result});
        return result;
    }

    private Occurrence findOccurrence(JsObject object, int offset, IdentityHashMap<JsObject,Void> scannedElements) {
        if(scannedElements.containsKey(object)) {
            return null;
        } else {
            scannedElements.put(object, null);
        }
        Occurrence result = null;
        JsElement.Kind kind = object.getJSKind();
        for(Occurrence occurrence: object.getOccurrences()) {
                if (occurrence.getOffsetRange().containsInclusive(offset)) {
                    return occurrence;
                }
            }
            if (kind.isFunction() || kind == JsElement.Kind.CATCH_BLOCK) {
                for(JsObject param : ((JsFunction)object).getParameters()) {
                 result = findOccurrence(param, offset, scannedElements);
                    if (result != null) {
                        break;
                    }
                }
                if (result != null) {
                    return result;
                }
            }
            if (!(object instanceof JsReference && ModelUtils.isDescendant(object, ((JsReference)object).getOriginal()))) {
                for(JsObject property: object.getProperties().values()) {
                    if (!(property instanceof JsReference && !((JsReference)property).getOriginal().isAnonymous())) {
                        result = findOccurrence(property, offset, scannedElements);
                        if (result != null) {
                            break;
                        }
                    } else {
                        for(Occurrence occurrence: property.getOccurrences()) {
                            if (occurrence.getOffsetRange().containsInclusive(offset)) {
                                return occurrence;
                            }
                        }
                    }
                }
            }
        return result;
    }

    private Occurrence findDeclaration (JsObject object, int offset, IdentityHashMap<JsObject,Void> scannedElements) {
        if(scannedElements.containsKey(object)) {
            return null;
        } else {
            scannedElements.put(object, null);
        }
        Occurrence result = null;
        JsElement.Kind kind = object.getJSKind();
        if (kind != JsElement.Kind.ANONYMOUS_OBJECT && kind != JsElement.Kind.WITH_OBJECT
                && object.getDeclarationName() != null && object.getDeclarationName().getOffsetRange().containsInclusive(offset)
                && !ModelUtils.isGlobal(object)) {
            if (kind.isPropertyGetterSetter()) {
                // if it's getter or setter in object literal, return it as occurrence of the property
                String propertyName = object.getName();
                propertyName = propertyName.substring(propertyName.lastIndexOf(' ') + 1);
                JsObject property = object.getParent().getProperty(propertyName);
                if (property != null) {
                    return new Occurrence(property.getDeclarationName().getOffsetRange(), property);
                }
            }

            result = new Occurrence(object.getDeclarationName().getOffsetRange(), object);
        }
        if (result == null && (kind.isFunction() || kind == JsElement.Kind.CATCH_BLOCK)) {
             for(JsObject param : ((JsFunction)object).getParameters()) {
                 if (param.getDeclarationName().getOffsetRange().containsInclusive(offset)) {
                     result = new Occurrence(param.getDeclarationName().getOffsetRange(), object);
                     return result;
                }
            }
        }
        if (result == null) {
            for(JsObject property: object.getProperties().values()) {
                if (!(property instanceof JsReference)) {
                    result = findDeclaration(property, offset, scannedElements);
                    if (result != null) {
                        break;
                    }
                }
            }
        }
        return result;
    }
}
