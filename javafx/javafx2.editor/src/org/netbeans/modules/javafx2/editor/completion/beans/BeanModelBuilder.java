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
package org.netbeans.modules.javafx2.editor.completion.beans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.AbstractTypeVisitor7;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;

import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.modules.javafx2.editor.JavaFXEditorUtils;
import org.netbeans.modules.javafx2.editor.completion.model.FxClassUtils;

/**
 * Builds a model for a single class. Needs a {@link FxBeanProvider}, so that
 * a BeanInfo for the superclass can be obtained.
 *
 * @author sdedic
 */
public final class BeanModelBuilder {
    /**
     * Environment, which should be used for introspection
     */
    private final CompilationInfo compilationInfo;
    
    /**
     * Fully qualified class name
     */
    private final String  className;
    
    private Set<String> dependencies = Collections.emptySet();
    
    /**
     * Properties found on the Bean
     */
    private Map<String, FxProperty>   allProperties = Collections.emptyMap();
    
    private Map<String, FxProperty>   staticProperties = Collections.emptyMap();
    
    /**
     * List of simple properties in the class
     */
    private Map<String, FxProperty> simpleProperties = Collections.emptyMap();
    
    /**
     * Names of factory methods usable to create the bean instance
     */
    private Map<String, TypeMirrorHandle> factoryMethods = Collections.emptyMap();
    
    private FxBean  resultInfo;
    
    private Set<String> constants = Collections.emptySet();
    
    /**
     * Type element for the class.
     */
    @NullAllowed
    private TypeElement classElement;
    
    private FxBeanProvider  provider;
    
    /**
     * True, if analyzing a Builder. Naming patterns are different.
     */
    private boolean builder;

    public BeanModelBuilder(FxBeanProvider provider, CompilationInfo compilationInfo, String className) {
        this.compilationInfo = compilationInfo;
        this.className = className;
        this.provider = provider;
    }
    
    private void addDependency(TypeMirror tm) {
        if (tm.getKind() == TypeKind.ARRAY) {
            addDependency(((ArrayType)tm).getComponentType());
        } else if (tm.getKind() == TypeKind.WILDCARD) {
            WildcardType wt = (WildcardType)tm;
            TypeMirror bound = wt.getSuperBound();
            if (bound == null) {
                bound = wt.getExtendsBound();
            }
            addDependency(bound);
        } else if (tm.getKind() == TypeKind.DECLARED) {
            addDependency(
                ((TypeElement)compilationInfo.getTypes().asElement(tm)).getQualifiedName().toString()
            );
        }
    }
    
    private void addDependency(String name) {
        if (dependencies.isEmpty()) {
            dependencies = new HashSet<String>();
        }
        dependencies.add(name);
    }
    
    FxBean process() {
        classElement = compilationInfo.getElements().getTypeElement(className);
        TypeElement builderEl = compilationInfo.getElements().getTypeElement("javafx.util.Builder"); // NOI18N
        TypeElement mapEl = compilationInfo.getElements().getTypeElement("java.util.Map"); // NOI18N
        TypeElement collectionEl = compilationInfo.getElements().getTypeElement("java.util.Collection"); // NOI18N
        
        if (classElement == null) {
            return resultInfo = null;
        }
        if (builderEl != null) {
            Types t = compilationInfo.getTypes();
            builder = t.isAssignable(
                    t.erasure(classElement.asType()), t.erasure(builderEl.asType())
            );
        }
        boolean fxInstance = true;
        boolean valueOf = FxClassUtils.findValueOf(classElement, compilationInfo) != null;
        
        if (!classElement.getModifiers().contains(Modifier.PUBLIC)) {
            fxInstance = false;
        } else {
            boolean found = false;
            
            for (ExecutableElement c : ElementFilter.constructorsIn(classElement.getEnclosedElements())) {
                if (c.getParameters().isEmpty() &&
                    (FxClassUtils.isFxmlAccessible(c)) ) {
                    found = true;
                    break;
                }
            }
            
            if (found) {
                fxInstance = true;
            } else {
                fxInstance = valueOf;
            }
        }
        
        FxBean declared = resultInfo = new FxBean(className);
        resultInfo.setJavaType(ElementHandle.create(classElement));
        resultInfo.setFxInstance(fxInstance);
        resultInfo.setValueOf(valueOf);

        if (mapEl != null) {
            Types t = compilationInfo.getTypes();
            if (t.isAssignable(
                    t.erasure(classElement.asType()), t.erasure(mapEl.asType()))) {
                resultInfo.makeMap();
            }
        }
        if (collectionEl != null) {
            Types t = compilationInfo.getTypes();
            if (t.isAssignable(
                    t.erasure(classElement.asType()), t.erasure(collectionEl.asType()))) {
                resultInfo.makeCollection();
            }
        }
        inspectMembers();
        
        // try to find default property
        resultInfo.setProperties(allProperties);
        resultInfo.setSimpleProperties(simpleProperties);
        resultInfo.setAttachedProperties(staticProperties);
        resultInfo.setEvents(events);
        resultInfo.setFactories(factoryMethods);
        resultInfo.setConstants(constants);
        String defaultProperty = FxClassUtils.getDefaultProperty(classElement);
        resultInfo.setDefaultPropertyName(defaultProperty);
        
        FxBean merge = new FxBean(className);
        merge.setJavaType(resultInfo.getJavaType());
        merge.setValueOf(resultInfo.hasValueOf());
        merge.setFxInstance(resultInfo.isFxInstance());
        merge.setDeclaredInfo(resultInfo);
        merge.setFactories(factoryMethods);
        resultInfo = merge;

        // try to find the builder
        findBuilder();

        if (classElement.getKind() == ElementKind.CLASS) {
            collectSuperClass(classElement.getSuperclass());
        }
        resultInfo.setParentBeanInfo(superBi);
        resultInfo.merge(declared);
        // constants are not merged, apply to just the single type
        resultInfo.setConstants(constants);

        
        // add to the bean cache:
        if (beanCache != null) {
            beanCache.addBeanInfo(compilationInfo.getClasspathInfo(), resultInfo, dependencies);
        }
        return resultInfo;
    }
    
    private void findBuilder() {
        if (classElement.getNestingKind() != NestingKind.TOP_LEVEL || builder) {
            return;
        }
        Collection<? extends BuilderResolver> resolvers = MimeLookup.getLookup(JavaFXEditorUtils.FXML_MIME_TYPE).lookupAll(BuilderResolver.class);
        for (BuilderResolver r : resolvers) {
            String builderName = r.findBuilderClass(compilationInfo, null, className);
            if (builderName != null) {
                FxBean builderBean = provider.getBeanInfo(builderName);
                if (builderBean != null) {
                    resultInfo.setBuilder(builderBean);
                    builderBean.makeBuilder(resultInfo);
                    return;
                }
            }
        }
    }
    
    public FxBean getBeanInfo() {
        if (resultInfo == null) {
            process();
        }
        return resultInfo;
    }
    
    TypeElement getClassElement() {
        return classElement;
    }
    
    private static final String SET_NAME_PREFIX = "set";
    private static final int SET_NAME_PREFIX_LEN = 3;
    private static final String GET_NAME_PREFIX = "get";
    private static final int GET_NAME_PREFIX_LEN = 3;
    
    private String getPropertyName(String setterName) {
        return Character.toLowerCase(setterName.charAt(SET_NAME_PREFIX_LEN)) + setterName.substring(SET_NAME_PREFIX_LEN + 1);
    }
    
    private boolean consumed;
    
    private Collection<ExecutableElement> getters = new ArrayList<ExecutableElement>();
    
    private void addCandidateROProperty(ExecutableElement m) {
        if (consumed) {
            return;
        }
        String name = m.getSimpleName().toString();
        if (name.length() > GET_NAME_PREFIX_LEN && name.startsWith(GET_NAME_PREFIX)) {
            String n = getPropertyName(name);
            if (m.getParameters().isEmpty()) {
                getters.add(m);
            }
        }
    }
    
    private static final String LIST_CLASS = "java.util.List"; // NOI18N
    
    private static final String MAP_CLASS = "java.util.Map"; // NOI18N
    
    
    private void processGetters() {
        // Check presence !
        TypeMirror listType = compilationInfo.getElements().getTypeElement(LIST_CLASS).asType();
        TypeMirror mapType = compilationInfo.getElements().getTypeElement(MAP_CLASS).asType();
        for (ExecutableElement m : getters) {
            String n = getPropertyName(m.getSimpleName().toString());
            if (allProperties.containsKey(n)) {
                continue;
            }
            TypeMirror retType = m.getReturnType();
            TypeMirror erasure = compilationInfo.getTypes().erasure(retType);
            if (compilationInfo.getTypes().isAssignable(erasure, listType)) {
                addListProperty(m, n);
            } else if (compilationInfo.getTypes().isAssignable(erasure, mapType)) {
                addMapProperty(m, n);
            }
        }
        if (allProperties.isEmpty() && !resultInfo.isFxInstance()) {
            processGettersCheckForImmutables();
        }
    }

    private static final String NAMED_ARG = "javafx.beans.NamedArg";

    /** Some javafx classes, such as Insets, are immutable and do not have
     * no argument constructors or setters; so they are not found.
     * Accept a property if there is a getter with a corresponding
     * constructor param declared with NamedArg annotation; use constructor
     * with the most NamedArg parameters.
     * <p/>
     * One alternate strategy would be to provide a document with lines like:
     * "Insets: top bottom left right" and use this info.
     */
    private void processGettersCheckForImmutables() {
        Set<String> propsConstructor = Collections.emptySet();
        Set<String> props1 = new HashSet<>();
        CHECK_CONSTR: for (ExecutableElement c : ElementFilter.constructorsIn(classElement.getEnclosedElements())) {
            props1.clear();
            CHECK_PARAMS: for (VariableElement p : c.getParameters()) {
                for (AnnotationMirror am : p.getAnnotationMirrors()) {
                    if (am.getAnnotationType().asElement().equals(
                            compilationInfo.getElements().getTypeElement(NAMED_ARG))) {
                        for (Entry<? extends ExecutableElement, ? extends AnnotationValue> entry
                                : am.getElementValues().entrySet()) {
                            if (entry.getKey().getSimpleName().toString().equals("value")) { // NOI18N
                                props1.add((String)entry.getValue().getValue());
                                continue CHECK_PARAMS;
                            }
                        }
                    }
                }
                // a parameters wasn't NAMED_ARG; skip this constructor.
                continue CHECK_CONSTR;
            }
            if (propsConstructor.size() < props1.size()) {
                propsConstructor = new HashSet<>(props1);
            }
        }

        if (propsConstructor.isEmpty()) {
            return;
        }

        // problem if not all constructor args are covered?
        boolean fxInstance = false;
        for (ExecutableElement m : getters) {
            String n = getPropertyName(m.getSimpleName().toString());
            if (propsConstructor.contains(n)) {
                addGetterOnlyProperty(m, n);
                fxInstance = true;
            }
        }
        resultInfo.setFxInstance(fxInstance);
    }

    private void addGetterOnlyProperty(ExecutableElement m, String propName) {
        TypeMirror returnType = m.getReturnType();
        boolean simple = FxClassUtils.isSimpleType(returnType, compilationInfo);

        // Could only accept simple?

        FxProperty pi = new FxProperty(propName, FxDefinitionKind.GETTER);
        pi.setAccessor(ElementHandle.create(m));
        pi.setSimple(simple);
        pi.setType(TypeMirrorHandle.create(returnType));
        pi.setObservableAccessors(pi.getAccessor());
        
        registerProperty(pi);
    }
    
    private static final String EVENT_TYPE_NAME = "javafx.event.Event"; // NOI18N
    
    private ElementHandle<TypeElement>  eventHandle;
    
    private ElementHandle<TypeElement> getPropertyChangeHandle() {
        if (eventHandle == null) {
            eventHandle = ElementHandle.create(compilationInfo.getElements().getTypeElement(EVENT_TYPE_NAME));
        }
        return eventHandle;
    }
    
    private void generatePropertyChanges() {
        for (FxProperty p : allProperties.values()) {
            if (p.getObservableAccessor() != null) {
                String evName = p.getName() + "Change"; // NOI18N

                FxEvent ev = new FxEvent(evName);
                ev.setPropertyChange(true);
                ev.setEventClassName(EVENT_TYPE_NAME);
                ev.setEventType(getPropertyChangeHandle());
                addEvent(ev);
            }
       }
    }
    
    private void addAttachedProperty(ExecutableElement m) {
        if (consumed) {
            return;
        }
        String name = m.getSimpleName().toString();
        if (!name.startsWith(SET_NAME_PREFIX) || name.length() == SET_NAME_PREFIX_LEN ||
             !Character.isUpperCase(name.charAt(SET_NAME_PREFIX_LEN))) {
            return;
        }
        if (!isStatic(m)) {
            return;
        }
        if (m.getParameters().size() != 2) {
            return;
        }

        // setWhateverProperty(attachedObject, value)
        TypeMirror objectType = m.getParameters().get(0).asType();
        TypeMirror paramType = m.getParameters().get(1).asType();
        
        boolean simple = FxClassUtils.isSimpleType(paramType, compilationInfo);
        // analysis depends ont he paramType contents:
        addDependency(paramType);
        FxProperty pi = new FxProperty(getPropertyName(name), FxDefinitionKind.ATTACHED);
        pi.setSimple(simple);
        pi.setType(TypeMirrorHandle.create(paramType));
        pi.setAccessor(ElementHandle.create(m));
        
        // setup the discovered object type
        pi.setObjectType(TypeMirrorHandle.create(objectType));
        
        if (staticProperties.isEmpty()) {
            staticProperties = new HashMap<String, FxProperty>();
        }
        staticProperties.put(pi.getName(), pi);
        
        consumed = true;
    }

    private ExecutableElement mapGetMethod = null;
    private ExecutableElement listGetMethod = null;
    
    private ExecutableType findMapGetMethod(DeclaredType inType) {
        TypeElement mapClass = compilationInfo.getElements().getTypeElement(MAP_CLASS);

        if (mapGetMethod == null) {
            for (ExecutableElement mm : ElementFilter.methodsIn(mapClass.getEnclosedElements())) {
                if (mm.getSimpleName().toString().equals("get")) { // NOI18N
                    mapGetMethod = mm;
                }
            }
        }
        return (ExecutableType)compilationInfo.getTypes().asMemberOf(inType, mapGetMethod);
    }

    private ExecutableType findListGetMethod(DeclaredType inType) {
        TypeElement mapClass = compilationInfo.getElements().getTypeElement(LIST_CLASS);

        if (listGetMethod == null) {
            for (ExecutableElement mm : ElementFilter.methodsIn(mapClass.getEnclosedElements())) {
                if (mm.getSimpleName().toString().equals("get")) { // NOI18N
                    listGetMethod = mm;
                }
            }
        }
        return (ExecutableType)compilationInfo.getTypes().asMemberOf(inType, listGetMethod);
    }
    
    private void addMapProperty(ExecutableElement m, String propName) {
        FxProperty pi = new FxProperty(propName, FxDefinitionKind.MAP);
        pi.setSimple(false);
        pi.setAccessor(ElementHandle.create(m));
        
        // must extract type arguments; assume there's a DeclaredType
        DeclaredType t = ((DeclaredType)m.getReturnType());
        ExecutableType getterType = findMapGetMethod(t);
        
        pi.setType(TypeMirrorHandle.create(getterType.getReturnType()));
        pi.setObservableAccessors(pi.getAccessor());
        
        registerProperty(pi);
    }
    
    private void addListProperty(ExecutableElement m, String propName) {
        FxProperty pi = new FxProperty(propName, FxDefinitionKind.LIST);
        pi.setSimple(false);
        pi.setAccessor(ElementHandle.create(m));
        
        // must extract type arguments; assume there's a DeclaredType
        DeclaredType t = ((DeclaredType)m.getReturnType());
        ExecutableType getterType = findListGetMethod(t);
        
        pi.setType(TypeMirrorHandle.create(getterType.getReturnType()));
        pi.setObservableAccessors(pi.getAccessor());
        
        registerProperty(pi);
    }
    
    private void addObservableAccessor(ExecutableElement m) {
        if (consumed) {
            return;
        }
        String mName = m.getSimpleName().toString();
        if (!mName.endsWith("Property")) {
            return;
        }
        mName = mName.substring(0, mName.length() - 8); // Property suffix
        observableAccessors.put(mName, ElementHandle.create(m));
    }
    
    private void markObservableProperties() {
        for (FxProperty prop : allProperties.values()) {
            String n = prop.getName();
            ElementHandle<ExecutableElement> m = observableAccessors.get(n);
            prop.setObservableAccessors(m);
        }
    }
    
    protected String findPropertyName(ExecutableElement m) {
        String name = m.getSimpleName().toString();
        if (!builder) {
            if (!name.startsWith(SET_NAME_PREFIX) || name.length() == SET_NAME_PREFIX_LEN ||
                 !Character.isUpperCase(name.charAt(SET_NAME_PREFIX_LEN))) {
                return null;
            }
        }

        // check number of parameters:
        if (m.getParameters().size() != 1) {
            return null;
        }
        
        if (builder) {
            StringBuilder sb = new StringBuilder();
            sb.append(Character.toLowerCase(name.charAt(0)));
            if (name.length() > 1) {
                sb.append(name.substring(1));
            }
            return compilationInfo.getTypes().isAssignable(m.getReturnType(), m.getEnclosingElement().asType()) ?
                    // #223293: some builder methods start with uppercase, which is not a permitted name for a property
                    sb.toString() : null;
        } else {
            return  m.getReturnType().getKind() == TypeKind.VOID ?
                    getPropertyName(name.toString()) : null;
            
        }
    }

    /**
     * Checks if the method represents a simple setter property. If so, it creates
     * and registers the appropriate FxProperty
     * @param m 
     */
    private void addProperty(ExecutableElement m) {
        if (consumed) {
            return;
        }
        String name = findPropertyName(m);
        if (name == null) {
            return;
        }
        registerProperty(m, name);
    }
    
   private void registerProperty(ExecutableElement m, String name) {
        TypeMirror paramType = m.getParameters().get(0).asType();
        boolean simple = FxClassUtils.isSimpleType(paramType, compilationInfo);
        addDependency(paramType);
        FxProperty pi = new FxProperty(name, FxDefinitionKind.SETTER);
        pi.setSimple(simple);
        pi.setType(TypeMirrorHandle.create(paramType));
        pi.setAccessor(ElementHandle.create(m));
        
        registerProperty(pi);
        if (simple) {
            if (simpleProperties.isEmpty()) {
                simpleProperties = new HashMap<String, FxProperty>();
            }
            simpleProperties.put(pi.getName(), pi);
        }
        consumed = true;
    }
    
    private void registerProperty(FxProperty pi) {
        if (allProperties.isEmpty()) {
            allProperties = new HashMap<String, FxProperty>();
        }
        allProperties.put(pi.getName(), pi);
    }
    
    /**
     * Checks whether the method is a factory method for the class,
     * and if so, adds its name to the list
     */
    private void addFactoryMethod(ExecutableElement m) {
        if (consumed) {
            return;
        }
        if (!isStatic(m)) {
            return;
        }
        if (!m.getParameters().isEmpty()) {
            return;
        }
        TypeMirrorHandle returnType = TypeMirrorHandle.create(m.getReturnType());
        
        if (factoryMethods.isEmpty()) {
            factoryMethods = new HashMap<>();
        }
        factoryMethods.put(m.getSimpleName().toString(), returnType);
        consumed = true;
    }
    
    private static final String EVENT_PREFIX = "setOn";
    private static final int EVENT_PREFIX_LEN = 5;
    private static final String ANNOTATION_TYPE_FXML = "javax.fxml.beans.FXML";
    
    
    private boolean isStatic(ExecutableElement m) {
         return m.getModifiers().contains(Modifier.STATIC);
    }
    
    private boolean isAccessible(Element m) {
        if (!m.getModifiers().contains(Modifier.PUBLIC)) {
            for (AnnotationMirror am : m.getAnnotationMirrors()) {
                String atype = ((TypeElement)am.getAnnotationType().asElement()).getQualifiedName().toString();
                if (ANNOTATION_TYPE_FXML.equals(atype)) {
                    return true;
                }
            }
            return false;
        } else {
            return true;
        }
    }
    /** 
     * Accessible symbols are either public, or annotated with FXML.
     */
    private boolean isAccessible(ExecutableElement m, boolean classMethod) {
        return isAccessible(m) && m.getModifiers().contains(Modifier.STATIC) == classMethod;
    }
    
    private static final String JAVAFX_EVENT_BASE = "javafx.event.EventHandler"; // NOI18N
    
    private TypeMirror  eventHandlerBase;
    
    private TypeMirror  getHandlerBaseType() {
        if (eventHandlerBase == null) {
            TypeElement el = compilationInfo.getElements().getTypeElement(JAVAFX_EVENT_BASE);
            if (el == null) {
                // FIXME - better exception, must be catched & reported outside
                throw new IllegalStateException();
            }
            eventHandlerBase = el.asType();
        }
        return eventHandlerBase;
    }
    
    private void addEventSource(ExecutableElement m) {
        if (consumed) {
            return;
            
        }
        String sn = m.getSimpleName().toString();
        
        if (!isAccessible(m, false)) {
            return;
        }
        
        if (!sn.startsWith(EVENT_PREFIX) || sn.length() == EVENT_PREFIX_LEN) {
            return;
        }
        
        if (m.getParameters().size() != 1) {
            return;
        }
        VariableElement param = m.getParameters().get(0);
        TypeMirror varType = param.asType();
        
        // the type must be assignable to the event handler
        if (compilationInfo.getTypes().isAssignable(varType, getHandlerBaseType())) {
            return;
        }
        ElementHandle<TypeElement> eventHandle = null;
        String eventClassName = null;
        
        if (varType.getKind() == TypeKind.DECLARED) {
            // extract event type as the type of event / argument for the event handler method
            DeclaredType dt = (DeclaredType)varType;
            List<? extends TypeMirror> tParams = dt.getTypeArguments();
            if (tParams.size() != 1) {
                // something very wrong, the event handler has just 1 type parameter
                //throw new IllegalStateException();
                return;
            }
            TypeMirror eventType = tParams.get(0);
            if (eventType.getKind() == TypeKind.WILDCARD) {
                TypeMirror t = ((WildcardType)eventType).getSuperBound();
                if (t == null) {
                    t = ((WildcardType)eventType).getExtendsBound();
                }
                eventType = t;
            }
            if (eventType.getKind() != TypeKind.DECLARED) {
                throw new IllegalStateException();
            }
            TypeElement te = (TypeElement)compilationInfo.getTypes().asElement(eventType);
            eventClassName = te.getQualifiedName().toString();
            eventHandle = ElementHandle.create(te);
            addDependency(eventType);
        }

        String eventName = Character.toLowerCase(sn.charAt(EVENT_PREFIX_LEN)) + sn.substring(EVENT_PREFIX_LEN + 1);
        FxEvent ei = new FxEvent(eventName);
        ei.setEventClassName(eventClassName);
        ei.setEventType(eventHandle);
        
        addEvent(ei);
        
        consumed = true;
    }
    
    private void addEvent(FxEvent ei) {
        if (events.isEmpty()) {
            events = new HashMap<String, FxEvent>();
        }
        events.put(ei.getName(), ei);
    }
    
    private Map<String, FxEvent>    events = Collections.emptyMap();
    
    private FxBeanCache beanCache;
    
    private Map<String, ElementHandle<ExecutableElement>> observableAccessors = new HashMap<String, ElementHandle<ExecutableElement>>();
    
    private void inspectMembers() {
        List<ExecutableElement> methods = ElementFilter.methodsIn(classElement.getEnclosedElements());
        
        for (ExecutableElement m :methods) {
            if (!isAccessible(m)) {
                continue;
            }
            consumed = false;

            // event sources (except for on* property types)
            addEventSource(m);

            // instance properties
            addProperty(m);
            // factory methods
            addFactoryMethod(m);
            // attached properties
            addAttachedProperty(m);
            
            addCandidateROProperty(m);
            
            addObservableAccessor(m);
        }
        // add list and map properties, which have no corresponding setter and are r/o
        processGetters();

        markObservableProperties();
        
        // generate property changes from existing properties
        generatePropertyChanges();
        
        List<VariableElement> vars =  ElementFilter.fieldsIn(classElement.getEnclosedElements());
        for (VariableElement v : vars) {
            if (!isAccessible(v)) {
                continue;
            }
            consumed = false;
            addConstant(v);
        }
    }

   void setBeanCache(FxBeanCache beanCache) {
        this.beanCache = beanCache;
    }
    
    private static final String JAVA_LANG_OBJECT = "java.lang.Object"; // NOI18N
    
    private FxBean superBi;
    
    /**
     * Collects information from superclasses. Creates an additional instance of
     * BeanModelBuilder to get all the information.
     */
    private void collectSuperClass(TypeMirror superT) {
        if (superT == null) {
            return;
        }
        TypeElement elem = (TypeElement)compilationInfo.getTypes().asElement(superT);
        String fqn = elem.getQualifiedName().toString();
        if (JAVA_LANG_OBJECT.equals(fqn)) {
            return;
        }
        addDependency(fqn);
        superBi = null;
        if (beanCache != null) {
            superBi = beanCache.getBeanInfo(compilationInfo.getClasspathInfo(), fqn);
        }
        if (superBi == null) {
            superBi = provider.getBeanInfo(fqn);
        }
        resultInfo.merge(superBi);
    }
    
    private void addConstant(VariableElement v) {
        Set<Modifier> mods = v.getModifiers();
        if (!(mods.contains(Modifier.FINAL) && mods.contains(Modifier.STATIC))) {
            return;
        }
        
        boolean ok = false;
        
        // check that the return type is the same as this class' type
        if (!compilationInfo.getTypes().isSameType(
                v.asType(), classElement.asType())) {
            // the constant may be primitive & our type the wrapper
            TypeMirror t = v.asType();
            if (t instanceof PrimitiveType) {
                PrimitiveType p = (PrimitiveType)t;
                if (compilationInfo.getTypes().isSameType(
                        compilationInfo.getTypes().boxedClass(p).asType(),
                        classElement.asType())) {
                    ok = true;
                }
            } 
            if (!ok) {
                return;
            }
        }
        
        addConstant(v.getSimpleName().toString());
    }
    
    private void addConstant(String s) {
        if (constants.isEmpty()) {
            constants = new HashSet<String>();
        }
        constants.add(s);
    }
}
