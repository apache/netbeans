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
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.javascript2.doc.spi.JsDocumentationHolder;
import org.netbeans.modules.javascript2.types.api.TypeUsage;
import org.netbeans.modules.javascript2.model.api.JsElement;
import org.netbeans.modules.javascript2.model.api.JsObject;
import org.netbeans.modules.javascript2.model.api.JsWith;
import org.netbeans.modules.javascript2.model.api.Occurrence;

/**
 *
 * @author Petr Pisl
 */
public class JsWithObjectImpl extends JsObjectImpl implements JsWith {

    private final Collection<TypeUsage> withTypes;
    private final JsWith outerWith;
    private final Collection<JsWith> innerWith = new ArrayList<JsWith>();
    private final OffsetRange expressionRange;
    private final Set<JsObject> assignedIn = new HashSet<JsObject>();
    
    public JsWithObjectImpl(JsObject parent, String name, Collection<TypeUsage> withTypes,
            OffsetRange offsetRange, OffsetRange expressionRange, JsWith outer, String mimeType, String sourceLabel) {
        super(parent, name, false, offsetRange, EnumSet.of(Modifier.PUBLIC), mimeType, sourceLabel);
        this.withTypes = withTypes;
//        while (parent != null && !(parent instanceof JsWithObjectImpl)) {
//            parent = parent.getParent();
//        }
        this.outerWith = outer;
        if (this.outerWith != null) {
            ((JsWithObjectImpl)outerWith).addInnerWith(this);
        }
        this.expressionRange = expressionRange;
    }

    @Override
    public Collection<TypeUsage> getTypes() {
        return withTypes;
    }
    
    protected void addInnerWith(JsWith inner) {
        innerWith.add(inner);
    }
    
    
    @Override
    public Collection<JsWith> getInnerWiths() {
        Collection<JsWith> result = innerWith.isEmpty() ? Collections.emptyList(): new ArrayList<JsWith>(innerWith);
        return result;
    }
    
    @Override
    public JsWith getOuterWith() {
        return outerWith;
    }
    
    @Override
    public void resolveTypes(JsDocumentationHolder jsDocHolder) {
        Collection<JsObject> withProperties = new ArrayList<JsObject>(getProperties().values());
        for (JsObject withProperty: withProperties) {
            if (resolveWith(this, withProperty)) {
                properties.remove(withProperty.getName());
            }
        }
    }

    public Collection<JsObject> getObjectWithAssignment() {
        return this.assignedIn;
    }
    
    public void addObjectWithAssignment(JsObject object) {
        this.assignedIn.add(object);
    }
    
    private boolean resolveWith(JsWithObjectImpl withObject, JsObject property) {
        JsObject global = ModelUtils.getGlobalObject(withObject.getParent());
        for (TypeUsage typeUsage : withObject.getTypes()) {
            for (TypeUsage rType : ModelUtils.resolveTypeFromSemiType(withObject, typeUsage)) {
                JsObject fromType = ModelUtils.findJsObjectByName(global, rType.getType());
                if (fromType != null) {
                    JsObject propertyFromType = fromType.getProperty(property.getName());
                    if (propertyFromType == null && withObject.getOuterWith() == null) {
                        propertyFromType = global.getProperty(property.getName());
                    }
                    if (propertyFromType != null) {
                        moveOccurrenceOfObject((JsObjectImpl)propertyFromType, (JsObjectImpl)property);
                        moveFromWith((JsObjectImpl)propertyFromType, (JsObjectImpl)property);
                        return true;
                    } else {
                        JsWith outer = withObject.getOuterWith();
                        if (outer != null) {
                            return resolveWith((JsWithObjectImpl)outer, property);
                        }
                    }
                } else {
                    JsWith outer = withObject.getOuterWith();
                    if (outer != null) {
                        return resolveWith((JsWithObjectImpl)outer, property);
                    }
                }
            }
        }
        return false;
    }
    
    @Override
    public Kind getJSKind() {
        return JsElement.Kind.WITH_OBJECT;
    }
    
    @Override
    public int getOffset() {
        return getOffsetRange().getStart();
    }

    @Override
    public boolean isAnonymous() {
        return true;
    }

    public OffsetRange getExpressionRange() {
        return expressionRange;
    }

    private void moveOccurrenceOfObject(JsObjectImpl original, JsObjectImpl copy) {
        for (Occurrence occurrence: copy.getOccurrences()) {
            original.addOccurrence(occurrence.getOffsetRange());
        }
        copy.clearOccurrences();
    }
    
    protected void moveFromWith(JsObjectImpl original, JsObjectImpl inWith) {
        if (original.equals(inWith)) {
            return;
        }
        if (!original.isDeclared() && inWith.isDeclared()) {
            moveOccurrenceOfObject(inWith, original);
            moveOccurrenceOfProperties(inWith, original);
            inWith.setParent(original.getParent());
            original.getParent().addProperty(original.getName(), inWith);
            return;
        }
        
        Collection<JsObject> prototypeChains = findPrototypeChain(original);
        Collection<JsObject> propertiesCopy = new ArrayList<JsObject>(inWith.getProperties().values());
        for (JsObject withProperty : propertiesCopy) {
            if (withProperty.isDeclared()) {
                boolean accessible = false;
                for (JsObject jsObject : prototypeChains) {
                    JsObject originalProperty = jsObject.getProperty(withProperty.getName());
                    if (originalProperty != null) {
                        accessible = true;
                        break;
                    }
                }
                if (!accessible) {
                    ((JsObjectImpl)withProperty).setParent(original);
                    original.addProperty(withProperty.getName(), withProperty);
                    inWith.properties.remove(withProperty.getName());
                }
            }
        }
        
        for (JsObject jsObject : prototypeChains) {
            for (JsObject origProperty : jsObject.getProperties().values()) {
                if(origProperty.getModifiers().contains(Modifier.PUBLIC)
                        || origProperty.getModifiers().contains(Modifier.PROTECTED)) {
                    JsObjectImpl usedProperty = (JsObjectImpl)inWith.getProperty(origProperty.getName());
                    if (usedProperty != null) {
                        ((JsObjectImpl)origProperty).addOccurrence(usedProperty.getDeclarationName().getOffsetRange());
                        for(Occurrence occur : usedProperty.getOccurrences()) {
                            ((JsObjectImpl)origProperty).addOccurrence(occur.getOffsetRange());
                        }
                        usedProperty.clearOccurrences();
                        if (origProperty.isDeclared() && usedProperty.isDeclared()){
                            usedProperty.setDeclared(false); // the property is not declared here
                        }
                        moveFromWith((JsObjectImpl)origProperty, usedProperty);
                    }
                }
            }
            JsObject prototype = jsObject.getProperty(ModelUtils.PROTOTYPE);
            if (prototype != null) {
                moveFromWith((JsObjectImpl)prototype, inWith);
            }
        }
        
        
    }
    
    
}
