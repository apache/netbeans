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
package org.netbeans.modules.javascript2.model;

import org.netbeans.modules.javascript2.model.api.ModelUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.javascript2.doc.spi.JsDocumentationHolder;
import org.netbeans.modules.javascript2.model.api.JsArray;
import org.netbeans.modules.javascript2.model.api.JsObject;
import org.netbeans.modules.javascript2.types.api.Identifier;
import org.netbeans.modules.javascript2.types.api.Type;
import org.netbeans.modules.javascript2.types.api.TypeUsage;

/**
 *
 * @author Petr Pisl
 */
public class JsArrayImpl extends JsObjectImpl implements JsArray {

    private List<TypeUsage> typesInArray = new ArrayList<TypeUsage>();

    public JsArrayImpl(JsObject parent, Identifier name, OffsetRange offsetRange, String mimeType, String sourceLabel) {
        super(parent, name, offsetRange, mimeType, sourceLabel);
    }

    public JsArrayImpl(JsObject parent, String name, boolean isDeclared, OffsetRange offsetRange, Set<Modifier> modifiers, String mimeType, String sourceLabel) {
        super(parent, name, isDeclared, offsetRange, modifiers, mimeType, sourceLabel);
    }
    
    public JsArrayImpl(JsObject parent, Identifier name, OffsetRange offsetRange, boolean isDeclared, Set<Modifier> modifiers, String mimeType, String sourceLabel) {
        super(parent, name, offsetRange, isDeclared, modifiers, mimeType, sourceLabel);
    }

    public Collection<? extends TypeUsage> getTypesInArray() {
        List<TypeUsage> values;
        values = new ArrayList<TypeUsage>();
        for(TypeUsage type : typesInArray) {
            values.add(type);
        }
        return Collections.unmodifiableCollection(values);
    }
   
    public void addTypeInArray(TypeUsage type) {
        boolean isHere = false;
        for (TypeUsage typeUsage : typesInArray) {
            if (typeUsage.getType().equals(type.getType())) {
                isHere = true;
                break;
            }
        }
        if (!isHere) {
            typesInArray.add(type);
        }
    }
    
    public void addTypesInArray(Collection<TypeUsage> types) {
        for (TypeUsage type : types) {
            addTypeInArray(type);
        }
    }

    @Override
    public void resolveTypes(JsDocumentationHolder jsDocHolder) {
        super.resolveTypes(jsDocHolder); 
        HashSet<String> nameTypesInArray = new HashSet<String>();
        Collection<TypeUsage> resolved = new ArrayList<>();
        Collection<? extends TypeUsage> typesIA = getTypesInArray();
        for (TypeUsage type : typesIA) {
            if (!(type.getType().equals(Type.UNRESOLVED) && typesIA.size() > 1)) {
                if (!type.isResolved()) {
                    for (TypeUsage rType : ModelUtils.resolveTypeFromSemiType(this, type)) {
                        if (!nameTypesInArray.contains(rType.getType())) {
                            if ("@this;".equals(type.getType())) { // NOI18N
                                rType = new TypeUsage(rType.getType(), -1, rType.isResolved());
                            }
                            resolved.add(rType);
                            nameTypesInArray.add(rType.getType());
                        }
                    }
                } else {
                    if (!nameTypesInArray.contains(type.getType())) {
                        resolved.add(type);
                        nameTypesInArray.add(type.getType());
                    }
                }
            }
        }
        
        for (TypeUsage type : resolved) {
            if (type.getOffset() > 0) {
                JsObject jsObject = ModelUtils.findJsObjectByName(this, type.getType());
                if (jsObject == null) {
                    JsObject global = ModelUtils.getGlobalObject(this);
                    jsObject = ModelUtils.findJsObjectByName(global, type.getType());
                }
                if (jsObject != null) {
                    int index = type.getType().lastIndexOf('.');
                    int typeLength = (index > -1) ? type.getType().length() - index - 1 : type.getType().length();
                    ((JsObjectImpl)jsObject).addOccurrence(new OffsetRange(type.getOffset(), type.getOffset() + typeLength));
                }
            }
        }
        typesInArray.clear();
        typesInArray.addAll(resolved);
                
    }
    
    
    
}
