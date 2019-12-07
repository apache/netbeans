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

import org.netbeans.modules.javascript2.model.api.Occurrence;
import org.netbeans.modules.javascript2.model.api.ModelUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import org.netbeans.modules.csl.api.Documentation;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.javascript2.doc.spi.JsDocumentationHolder;
import org.netbeans.modules.javascript2.model.api.JsElement;
import org.netbeans.modules.javascript2.model.api.JsFunction;
import org.netbeans.modules.javascript2.model.api.JsObject;
import org.netbeans.modules.javascript2.model.api.Occurrence;
import org.netbeans.modules.javascript2.types.api.DeclarationScope;
import org.netbeans.modules.javascript2.types.api.Identifier;
import org.netbeans.modules.javascript2.types.api.Type;
import org.netbeans.modules.javascript2.types.api.TypeUsage;

/**
 *
 * @author Petr Pisl
 */
public class JsObjectImpl extends JsElementImpl implements JsObject {

//    final protected HashMap<String, JsObject> properties = new HashMap<String, JsObject>();
    final protected LinkedHashMap<String, JsObject> properties = new LinkedHashMap<String, JsObject>();
    private Identifier declarationName;
    private JsObject parent;
    final private List<Occurrence> occurrences = new ArrayList<Occurrence>();
    final private NavigableMap<Integer, Collection<TypeUsage>> assignments = new TreeMap<>();
    final private Map<String, Integer>assignmentsReverse = new HashMap<>();
    private int countOfAssignments = 0;
    private boolean hasName;
    private Documentation documentation;
    protected JsElement.Kind kind;
    private boolean isVirtual;
    private boolean isAnonymous;

    public JsObjectImpl(JsObject parent, Identifier name, OffsetRange offsetRange,
            String mimeType, String sourceLabel) {
        super((parent != null ? parent.getFileObject() : null), name.getName(),
                ModelUtils.PROTOTYPE.equals(name.getName()), offsetRange, EnumSet.of(Modifier.PUBLIC), mimeType, sourceLabel);
        this.declarationName = name;
        this.parent = parent;
        this.hasName = name.getOffsetRange().getStart() != name.getOffsetRange().getEnd();
        this.kind = null;
        this.isVirtual = false;
        this.isAnonymous = false;
    }

    public JsObjectImpl(JsObject parent, Identifier name, OffsetRange offsetRange,
            boolean isDeclared, Set<Modifier> modifiers, String mimeType, String sourceLabel) {
        super((parent != null ? parent.getFileObject() : null), name.getName(),
                isDeclared, offsetRange, modifiers, mimeType, sourceLabel);
        this.declarationName = name;
        this.parent = parent;
        this.hasName = !OffsetRange.NONE.equals(name.getOffsetRange()) && (name.getOffsetRange().getStart() != name.getOffsetRange().getEnd());
        this.kind = null;
        this.isVirtual = false;
    }

    public JsObjectImpl(JsObject parent, Identifier name, OffsetRange offsetRange,
            boolean isDeclared, String mimeType, String sourceLabel) {
        this(parent, name, offsetRange, isDeclared, EnumSet.of(Modifier.PUBLIC), mimeType, sourceLabel);
    }

    protected JsObjectImpl(JsObject parent, String name, boolean isDeclared,
            OffsetRange offsetRange, Set<Modifier> modifiers, String mimeType, String sourceLabel) {
        super((parent != null ? parent.getFileObject() : null), name, isDeclared,
                offsetRange, modifiers, mimeType, sourceLabel);
        this.declarationName = null;
        this.parent = parent;
        this.hasName = false;
    }

    @Override
    public Identifier getDeclarationName() {
        return declarationName;
    }

    @Override
    public String getName() {
        return declarationName != null ? declarationName.getName() : super.getName();
    }
    
    public void setDeclarationName(Identifier declaration) {
        declarationName = declaration;
        hasName = declaration.getOffsetRange().getLength() > 0;
    }

    @Override
    public Kind getJSKind() {
        if (kind != null) {
            return kind;
        }
        if (parent == null) {
            // global object
            return Kind.FILE;
        }
        if (ModelUtils.PROTOTYPE.equals(getName())) {
            return Kind.OBJECT;
        }
        if (isDeclared()) {
            if (ModelUtils.ARGUMENTS.equals(getName())) {
                // special variable object of every function
                return Kind.VARIABLE;
            }
            if (!getAssignmentForOffset(getDeclarationName().getOffsetRange().getEnd()).isEmpty()
                    && hasOnlyVirtualProperties()) {
                if (getParent().getParent() == null || getModifiers().contains(Modifier.PRIVATE)) {
                    return Kind.VARIABLE;
                } else {
                    return Kind.PROPERTY;
                }
            }
        } else {
            if (!getProperties().isEmpty()) {
                return Kind.OBJECT;
            }
        }
        if (getProperties().isEmpty()) {
            if (getParent().isAnonymous() && (getParent() instanceof AnonymousObject)) {
                return Kind.PROPERTY;
            }
            if (getParent().getParent() == null || getModifiers().contains(Modifier.PRIVATE)) {
                // variable or the global object
                return Kind.VARIABLE;
            }
            if (getParent() instanceof JsFunction) {
                if (isDeclared()) {
                    return getModifiers().contains(Modifier.PRIVATE) ? Kind.VARIABLE : Kind.PROPERTY;
                }
            }
            return Kind.PROPERTY;
        }
        return Kind.OBJECT;
    }

    private boolean hasOnlyVirtualProperties() {
        for (JsObject property : getProperties().values()) {
            if (property.isDeclared() || ModelUtils.PROTOTYPE.equals(property.getName())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Map<String, ? extends JsObject> getProperties() {
        return properties;
    }

    @Override
    public void addProperty(String name, JsObject property) {
        properties.put(name, property);
    }

    @Override
    public JsObject getProperty(String name) {
        return properties.get(name);
    }

    @Override
    public JsObject getParent() {
        return parent;
    }

    public void setParent(JsObject newParent) {
        this.parent = newParent;
    }

    @Override
    public boolean isVirtual() {
        return isVirtual;
    }

    public void setVirtual(boolean isVirtual) {
        this.isVirtual = isVirtual;
    }

    @Override
    public int getOffset() {
        return declarationName == null ? -1 : declarationName.getOffsetRange().getStart();
    }

    @Override
    public List<Occurrence> getOccurrences() {
        return occurrences;
    }

    @Override
    public void addOccurrence(OffsetRange offsetRange) {
//        boolean isThere = false;
//        for (Occurrence occurrence : occurrences) {
//            if (occurrence.getOffsetRange().equals(offsetRange)) {
//                isThere = true;
//                break;
//            }
//        }
//        if (!isThere) {
//            occurrences.add(new OccurrenceImpl(offsetRange, this));
//        }
        Occurrence occurrence = new Occurrence(offsetRange, this);
        if (!occurrences.contains(occurrence)) {
            occurrences.add(occurrence);
        }
    }

    public void addAssignment(Collection<TypeUsage> typeNames, int offset) {
        for(TypeUsage type: typeNames) {
            addAssignment(type, offset);
        }
    }

    @Override
    public void clearAssignments() {
        assignments.clear();
    }

    @Override
    public void addAssignment(TypeUsage typeName, int offset) {
        if (typeName == null || (Type.UNDEFINED.equals(typeName.getType()) && assignments.size() > 0)) {
            // don't add undefined type, if there are already some types
            return;
        }
        Collection<TypeUsage> types = assignments.get(offset);
        if (types == null) {
            // create always empty list, need to be counted for number of assignments.
            types = new ArrayList<TypeUsage>();
            assignments.put(offset, types);
        }
        
        Integer alreadyDefinedOffset = assignmentsReverse.get(typeName.getType());
        if (alreadyDefinedOffset != null) {
            // there is already assignment of this type. It's enough to store the
            // assignment with the min offset
            if(alreadyDefinedOffset <= offset) {
                // do nothing, just remember the previous one
                return;
            } else {
                // we need to replace the assignment with bigger offset
                Collection<TypeUsage> typesToRemove = assignments.get(alreadyDefinedOffset);
                for (TypeUsage type : typesToRemove) {
                    if (type.getType().equals(typeName.getType())) {
                        typesToRemove.remove(type);
                        break;
                    }
                }
            }
        }
        assignmentsReverse.put(typeName.getType(), offset);
        types.add(typeName);
    }

    @Override
    public Collection<? extends TypeUsage> getAssignmentForOffset(int offset) {
        List<? extends TypeUsage> result = new ArrayList<>();
        Map.Entry<Integer, Collection<TypeUsage>> found = assignments.floorEntry(offset);
        int tmpOffset = offset;
        while (found != null) {
            result.addAll((Collection)found.getValue());
            tmpOffset = found.getKey() - 1;
            found = assignments.floorEntry(tmpOffset);
        }
//        if (result.isEmpty()) {
//            Collection<TypeUsage> resolved = new HashSet();
//            for(TypeUsage item : result) {
//                TypeUsage type = (TypeUsage)item;
//                if (type.isResolved()) {
//                    resolved.add(type);
//                } else {
//                    JsObject jsObject = ModelUtils.findJsObjectByName(ModelUtils.getGlobalObject(this), type.getType());
//                    if(jsObject != null) {
//                        resolved.addAll(resolveAssignments(jsObject, offset));
//                    }
//                }
//            }
//            if(resolved.isEmpty()) {
//                // keep somthink in the assignments. 
//                resolved.add(new TypeUsage("Object", offset, true));
//            }
//            Collection<TypeUsage> resolved = new HashSet();
//            //resolved.add(new TypeUsage("Object", offset, true));
//            result = resolved;
//        }

        return result;
    }

    public int getAssignmentCount() {
        return assignments.size();
    }

    @Override
    public Collection<? extends TypeUsage> getAssignments() {
        List<TypeUsage> values;
        values = new ArrayList<TypeUsage>();
        for (Collection<? extends TypeUsage> types : assignments.values()) {
            values.addAll(types);
        }
        return Collections.unmodifiableCollection(values);
    }

    @Override
    public String getFullyQualifiedName() {
        if (getParent() == null) {
            return getName();
        }
        StringBuilder result = new StringBuilder();
        JsObject pObject = this;
        result.append(getName());

        while ((pObject = pObject.getParent()).getParent() != null) {
            result.insert(0, ".");
            result.insert(0, pObject.getName());
        }
        return result.toString();
    }

    @Override
    public boolean isAnonymous() {
        return isAnonymous;
    }

    @Override
    public void setAnonymous(boolean value) {
        this.isAnonymous = value;
    }

    @Override
    public boolean containsOffset(int offset) {
        if (getOffsetRange().containsInclusive(offset)) {
            return true;
        }
        // some methods can be declared outside the main object
        for (JsObject property : getProperties().values()) {
            if (property.getOffsetRange().containsInclusive(offset)) {
                return true;
            }
            if (ModelUtils.PROTOTYPE.equals(property.getName())) {
                if (property.containsOffset(offset)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean hasExactName() {
        return hasName;
    }

    public final void setJsKind(JsElement.Kind kind) {
        this.kind = kind;
    }

    protected Collection<TypeUsage> resolveAssignments(JsObject jsObject, int offset) {        Collection<String> visited = new HashSet();  // for preventing infinited loops
        return resolveAssignments(jsObject, offset, visited);
    }

    protected Collection<TypeUsage> resolveAssignments(JsObject jsObject, int offset, Collection<String> visited) {
        Collection<TypeUsage> result = new HashSet<>();
        String fqn = jsObject.getFullyQualifiedName();
        if (visited.contains(fqn)) {
            return result;
        }
        visited.add(fqn);
        Collection<? extends TypeUsage> offsetAssignments = Collections.emptyList();
        Map.Entry<Integer, Collection<TypeUsage>> found = ((JsObjectImpl) jsObject).assignments.floorEntry(offset);
        if (found != null) {
            offsetAssignments = found.getValue();
        }
        if (offsetAssignments.isEmpty() && !jsObject.getProperties().isEmpty()) {
            result.add(new TypeUsage(jsObject.getFullyQualifiedName(), jsObject.getOffset(), true));
        } else {
            for (TypeUsage assignment : offsetAssignments) {
                if (!visited.contains(assignment.getType())) {
                    if (assignment.isResolved()) {
                        result.add(assignment);
                    } else {
                        if (assignment.getType().startsWith("@")) {
                            result.addAll(ModelUtils.resolveTypeFromSemiType(jsObject, assignment));
                        } else {
                            DeclarationScope scope = ModelUtils.getDeclarationScope(jsObject);
                            JsObject object = ModelUtils.getJsObjectByName(scope, assignment.getType());
                            if (object == null) {
                                JsObject gloal = ModelUtils.getGlobalObject(jsObject);
                                object = ModelUtils.findJsObjectByName(gloal, assignment.getType());
                            }
                            if (object != null) {
                                Collection<TypeUsage> resolvedFromObject = resolveAssignments(object, found != null ? found.getKey() : -1, visited);
                                if (resolvedFromObject.isEmpty()) {
                                    result.add(new TypeUsage(object.getFullyQualifiedName(), assignment.getOffset(), true));
                                } else {
                                    result.addAll(resolvedFromObject);
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    public void resolveTypes(JsDocumentationHolder jsDocHolder) {
        if (parent == null 
                || (parent != null && parent.getOffset() == getOffset() && ModelUtils.ARGUMENTS.equals(getName())) ) {
            return;
        }
        Collection<TypeUsage> resolved = new ArrayList<>();
        for (Collection<TypeUsage> unresolved : assignments.values()) {
            resolved.clear();
            JsObject global = ModelUtils.getGlobalObject(parent);
            for (TypeUsage type : unresolved) {
                Collection<TypeUsage> resolvedHere = new ArrayList<TypeUsage>();
                if (!type.isResolved()) {
                    resolvedHere.addAll(ModelUtils.resolveTypeFromSemiType(this, type));
                } else {
                    resolvedHere.add(type);
                }
                if (!type.getType().contains(ModelUtils.THIS)) {
                    for (TypeUsage typeHere : resolvedHere) {
                        if (typeHere.getOffset() > 0) {
                            TypeUsage newType = typeHere;
                            if (!typeHere.isResolved() && (typeHere.getType().startsWith(SemiTypeResolverVisitor.ST_PRO))) {
                                newType = ModelUtils.createResolvedType(global, typeHere);
                            }
                            String rType = ModelUtils.getFQNFromType(newType);
                            JsObject jsObject = ModelUtils.findJsObjectByName(global, rType);
                            
                            if (jsObject == null && rType.indexOf('.') == -1 && global instanceof DeclarationScope) {
                                DeclarationScope declarationScope = ModelUtils.getDeclarationScope((DeclarationScope) global, typeHere.getOffset());
                                jsObject = ModelUtils.getJsObjectByName(declarationScope, rType);
                                if (jsObject == null) {
                                    JsObject decParent = (this.parent.getJSKind() != JsElement.Kind.ANONYMOUS_OBJECT
                                            && this.parent.getJSKind() != JsElement.Kind.OBJECT_LITERAL)
                                            ? this.parent : this.parent.getParent();
                                    while (jsObject == null && decParent != null) {
                                        jsObject = decParent.getProperty(rType);
                                        decParent = decParent.getParent();
                                    }
                                }
                            }
                            if (jsObject != null) {
//                                if (typeHere.isResolved() && !jsObject.isAnonymous()) {
                                if (typeHere.isResolved()) {
                                    int index = rType.lastIndexOf('.');
                                    int typeLength = (index > -1) ? rType.length() - index - 1 : rType.length();
                                    int offset = typeHere.getOffset();
                                    ((JsObjectImpl) jsObject).addOccurrence(new OffsetRange(offset, jsObject.isAnonymous() ? offset : offset + typeLength));
                                }
                                moveOccurrenceOfProperties((JsObjectImpl) jsObject, this);
                                JsObject parent = jsObject.getParent();
                                if (parent != null && "window".equals(parent.getName())) {
                                    for (JsObject property : getProperties().values()) {
                                        if (property.isDeclared()) {
                                            JsObject gwProp = jsObject.getProperty(property.getName());
                                            if (gwProp == null) {
                                                jsObject.addProperty(property.getName(), property);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (type.getType().equals("@this;") && resolvedHere.size() == 1) {
                    // we expect something like self = this, so all properties of the object should be assigned to the this.
                    TypeUsage originalType = resolvedHere.iterator().next();
                    JsObject originalObject = ModelUtils.findJsObjectByName(global, originalType.getType());
                    if (originalObject != null) {
                        // move all properties to the original type.
                        // create copy of the new object, but without the properties
                        // the new object is needed for setting new assignment.
                        JsObject newObject = new JsObjectImpl(this.parent, this.declarationName,
                                this.getOffsetRange(), this.isDeclared(), this.getModifiers(), this.getMimeType(), this.getSourceLabel());
                        // replace the object with object without the properties 
                        parent.addProperty(this.getName(), newObject);
                        // copy all the properties to the original object that represents this
                        List <JsObject> propertiesCopy = new ArrayList<JsObject>(this.properties.values());
                        for (JsObject property : propertiesCopy) {
                            ModelUtils.moveProperty(originalObject, property);
                        }
                        for (Occurrence occurrence : this.occurrences) {
                            newObject.addOccurrence(occurrence.getOffsetRange());
                        }
                        newObject.addAssignment(new TypeUsage(originalObject.getFullyQualifiedName(), originalObject.getOffset(), true), assignments.keySet().iterator().next().intValue());
                    }
                }
                resolved.addAll(resolvedHere);
            }

            unresolved.clear();
            unresolved.addAll(resolved);
        }

        if (!isAnonymous()) {
            List<OffsetRange> docOccurrences = jsDocHolder.getOccurencesMap().get(getFullyQualifiedName());
            if (docOccurrences != null) {
                for (OffsetRange offsetRange : docOccurrences) {
                    addOccurrence(offsetRange);
                }
            }
        }
        
        if (!isAnonymous() && assignments.isEmpty()) {
            // try to recount occurrences
            JsObject global = ModelUtils.getGlobalObject(parent);
            List<Occurrence> correctedOccurrences = new ArrayList<Occurrence>();

            JsObjectImpl obAssignment = findRightTypeAssignment(getDeclarationName().getOffsetRange().getStart(), global);
            if (obAssignment != null && !obAssignment.getModifiers().contains(Modifier.PRIVATE)) {
                obAssignment.addOccurrence(getDeclarationName().getOffsetRange());
            }

            for (Occurrence occurrence : new ArrayList<Occurrence>(occurrences)) {
                obAssignment = findRightTypeAssignment(occurrence.getOffsetRange().getStart(), global);
                if (obAssignment != null && !obAssignment.getModifiers().contains(Modifier.PRIVATE)) {
                    obAssignment.addOccurrence(occurrence.getOffsetRange());
                } else {
                    correctedOccurrences.add(occurrence);
                }
            }

            for(Occurrence occurrence : correctedOccurrences){
                addOccurrence(occurrence.getOffsetRange());
            }
        }

        // resolving prototype types
        JsObject prototype = getProperty(ModelUtils.PROTOTYPE);
        if (prototype != null) {
            Collection<? extends TypeUsage> protoAssignments = prototype.getAssignments();
            if (protoAssignments != null && !protoAssignments.isEmpty()) {
                protoAssignments = new ArrayList<>(protoAssignments);
                Collection<? extends JsObject> variables = ModelUtils.getVariables(ModelUtils.getDeclarationScope(this));
                for (TypeUsage typeUsage : protoAssignments) {
                    for (JsObject variable : variables) {
                        if (typeUsage.getType().equals(variable.getName())) {
                            if (!typeUsage.getType().equals(variable.getFullyQualifiedName())) {
                                prototype.addAssignment(new TypeUsage(variable.getFullyQualifiedName(), typeUsage.getOffset(), true), typeUsage.getOffset());
                            }
                        }
                    }
                }
            }
        }

        // Try to find, whether this object is not also property of parent prototype.
        if (!isDeclared() && getParent() != null) {
            prototype = getParent().getProperty(ModelUtils.PROTOTYPE);
            if (prototype != null) {
                JsObject prototypeProperty = prototype.getProperty(getName());
                if (prototypeProperty != null && prototypeProperty.isDeclared()) {
                    // if there is also a property of parent prototype with the same name
                    // and is declared, move all the occurrences to the declared property
                    // and this property remove from parent.
                    for (Occurrence occurrence : getOccurrences()) {
                        prototypeProperty.addOccurrence(occurrence.getOffsetRange());
                    }
                    getParent().getProperties().remove(getName());
                }
            }
        }
    }

    protected void clearOccurrences() {
        occurrences.clear();
    }

    public static void moveOccurrenceOfProperties(JsObjectImpl original, JsObject created) {
        if (original.equals(created)) {
            return;
        }
        Collection<JsObject> prototypeChains = findPrototypeChain(original);
        for (JsObject jsObject : prototypeChains) {
            for (JsObject origProperty : jsObject.getProperties().values()) {
                if (origProperty.getModifiers().contains(Modifier.PUBLIC)
                        || origProperty.getModifiers().contains(Modifier.PROTECTED)) {
                    JsObjectImpl usedProperty = (JsObjectImpl) created.getProperty(origProperty.getName());
                    if (usedProperty != null) {
                        moveOccurrence((JsObjectImpl) origProperty, usedProperty);
                        moveOccurrenceOfProperties((JsObjectImpl) origProperty, usedProperty);
                    }
                }
            }
            JsObject prototype = jsObject.getProperty(ModelUtils.PROTOTYPE);
            if (prototype != null) {
                moveOccurrenceOfProperties((JsObjectImpl) prototype, created);
            }
        }
        
        if (!original.getAssignments().isEmpty() && created.getAssignments().isEmpty()) {
            // we are add type to help resolve other properties. 
            for(TypeUsage type : original.getAssignments()) {
                created.addAssignment(type, -1);
            }
        }

    }

    public static void moveOccurrence(JsObjectImpl original, JsObject created) {
        original.addOccurrence(created.getDeclarationName() != null ? created.getDeclarationName().getOffsetRange(): OffsetRange.NONE);
        for (Occurrence occur : created.getOccurrences()) {
            original.addOccurrence(occur.getOffsetRange());
        }
        ((JsObjectImpl) created).clearOccurrences();
        if (original.isDeclared() && created.isDeclared()) {
            ((JsObjectImpl) created).setDeclared(false); // the property is not declared here
        }
    }

    /**
     * Create prototype chain only from objects in the file
     *
     * @param object
     * @return
     */
    public static Collection<JsObject> findPrototypeChain(JsObject object) {
        List<JsObject> chain = new ArrayList<JsObject>();
        chain.add(object);
        chain.addAll(findPrototypeChain(object, new HashSet<String>()));
        return chain;
    }

    private static List<JsObject> findPrototypeChain(JsObject object, Set<String> alreadyCheck) {
        List<JsObject> result = new ArrayList<JsObject>();
        String fqn = object.getFullyQualifiedName();
        if (!alreadyCheck.contains(fqn)) {
            alreadyCheck.add(fqn);
            JsObject prototype = object.getProperty(ModelUtils.PROTOTYPE);
            if (prototype != null && !prototype.getAssignments().isEmpty()) {
                JsObject global = ModelUtils.getGlobalObject(object);
                for (TypeUsage type : prototype.getAssignments()) {
                    if (!type.isResolved()) {
                        Collection<TypeUsage> resolved = ModelUtils.resolveTypeFromSemiType(object, type);
                        for (TypeUsage rType : resolved) {
                            if (rType.isResolved()) {
                                JsObject fObject = ModelUtils.findJsObjectByName(global, rType.getType());
                                if (fObject != null) {
                                    result.add(fObject);
                                    result.addAll(findPrototypeChain(fObject, alreadyCheck));
                                }
                            }
                        }
                    } else {
                        JsObject fObject = ModelUtils.findJsObjectByName(global, type.getType());
                        if (fObject != null) {
                            result.add(fObject);
                            result.addAll(findPrototypeChain(fObject, alreadyCheck));
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * This methods returns JsObject that represents a type for an assignment.
     *
     * @param offset
     * @return return the object
     */
    private JsObjectImpl findRightTypeAssignment(int offset, JsObject global) {
        Collection<? extends TypeUsage> findedAssignments;
        JsObject current;
        JsObject currentParent = this;
        // save the properties in a list to reuse it later
        List<String> propertyPath = new ArrayList<String>();
        do {
            current = currentParent;
            findedAssignments = current.getAssignmentForOffset(offset);
            propertyPath.add(current.getName());
            currentParent = current.getParent();
        } while (findedAssignments.isEmpty() && currentParent != null);

        for (TypeUsage type : findedAssignments) {
            // find the appropriate object for the type in the model
            current = ModelUtils.findJsObjectByName(global, type.getType());

            // map back the properties from the propertyPath to get the right object
            for (int i = propertyPath.size() - 2; i > -1 && current != null; i--) {
                current = current.getProperty(propertyPath.get(i));
            }
            if (current != null) {
                return (JsObjectImpl) current;
            }
        }

        return null;
    }

    @Override
    public Documentation getDocumentation() {
        return documentation;
    }

    @Override
    public void setDocumentation(Documentation doc) {
        this.documentation = doc;
    }

    @Override
    public boolean isDeprecated() {
        return getModifiers().contains(Modifier.DEPRECATED);
    }

    public void setDeprecated(boolean depreceted) {
        if (depreceted) {
            getModifiers().add(Modifier.DEPRECATED);
        } else {
            getModifiers().remove(Modifier.DEPRECATED);
        }
    }
    
    

//    @Override
//    public String toString() {
//        return "JsObjectImpl{" + "declarationName=" + declarationName + ", parent=" + parent + ", kind=" + kind + '}';
//    }

    @Override
    public boolean moveProperty(String name, JsObject newParent) {
        JsObject property = getProperty(name);
        if (property == null) {
            return false;
        }
        if (property instanceof JsObjectImpl) {
            String oldFQN = property.getFullyQualifiedName();
            ((JsObjectImpl)property).setParent(newParent);
            newParent.addProperty(name, property);
            String newFQN = property.getFullyQualifiedName();
            JsObject global = ModelUtils.getGlobalObject(this);
            if (global instanceof JsObjectImpl) {
                ((JsObjectImpl)global).correctAssignmentsInModel(oldFQN, newFQN, new HashSet<String>());
            }
            return properties.remove(name) != null;
        }
        return false;
    }
    
    private void correctAssignmentsInModel (String fromType, String toType, Set<String> done) {
        if (!done.contains(getFullyQualifiedName())) {
            done.add(getFullyQualifiedName());
            correctTypes(fromType, toType);
            for (JsObject property: getProperties().values()) {
                if (property instanceof JsObjectImpl) {
                    ((JsObjectImpl)property).correctAssignmentsInModel(fromType, toType, done);
                }
            }
        }
    }
    
    protected void correctTypes(String fromType, String toType) {
        for (Integer offset: assignments.keySet()) {
            Collection<TypeUsage> types = assignments.get(offset);
            List<TypeUsage> copy = new ArrayList<>(types);
            String typeR = null;
            for (TypeUsage type : copy) {
                typeR = replaceTypeInFQN(type.getType(), fromType, toType);
                if (typeR != null) {
                    types.remove(type);
                    types.add(new TypeUsage(typeR, type.getOffset(), type.isResolved()));
                }
            }
        }
    }
    
    /**
     * 
     * @param typeFQN type that where should be changed
     * @param fromType  the old type or part of a type
     * @param toType    the new type or part of a type
     * @return null, if it's not possible to replace or the result FQN
     */
    protected String replaceTypeInFQN(String typeFQN, String fromType, String toType) {
        String typeR = null;
        if (typeFQN.isEmpty()) {
            return null;
        }
        if (typeFQN.equals(fromType)) {
            typeR = toType;
        } else {
            int index = typeFQN.indexOf(fromType);
            if (typeFQN.startsWith(SemiTypeResolverVisitor.ST_START_DELIMITER)) {
                // it's semitype. we need to mask the semitypes
                int delEndIndex = typeFQN.indexOf(';');
                if (delEndIndex > 0 && index < delEndIndex) {
                    index = typeFQN.indexOf(fromType, delEndIndex);
                } 
                 
            }
            if (index > -1 && !typeFQN.contains(toType)
                    && (index == 0 || typeFQN.charAt(index - 1) == '.' || typeFQN.charAt(index - 1) == ';')
                    && ((index + fromType.length()) == typeFQN.length() || typeFQN.charAt(index + fromType.length()) == '.')) {
                boolean replace = (index == 0 || typeFQN.startsWith(SemiTypeResolverVisitor.ST_START_DELIMITER));
                if (!replace && index > 0) {
                    String typePrefix = typeFQN.substring(0, index - 1);
                    JsObject global = ModelUtils.getGlobalObject(this);
                    replace = ModelUtils.findJsObjectByName(global, typePrefix) != null;
                }
                if (replace) {
                    typeR = typeFQN.substring(0, index) + toType + typeFQN.substring(index + fromType.length());
                }
            }
        }
        return typeR;
    }
}
