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

package org.netbeans.modules.openide.awt;

import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.processing.Completion;
import javax.annotation.processing.Completions;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.swing.Action;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.tools.Diagnostic;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionState;
import org.openide.awt.Actions;
import org.openide.awt.DynamicMenuContent;
import org.openide.filesystems.annotations.LayerBuilder;
import org.openide.filesystems.annotations.LayerBuilder.File;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.openide.util.ContextAwareAction;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@ServiceProvider(service=Processor.class)
public final class ActionProcessor extends LayerGeneratingProcessor {
    private static final String IDENTIFIER = "(?:\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*)"; // NOI18N
    private static final Pattern FQN = Pattern.compile(IDENTIFIER + "(?:[.]" + IDENTIFIER + ")*"); // NOI18N
    private static final String[] DEFAULT_COMPLETIONS = { "Menu", "Toolbars", "Shortcuts", "Loaders" }; // NOI18N
    private Processor COMPLETIONS;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> hash = new HashSet<String>();
        hash.add(ActionRegistration.class.getCanonicalName());
        hash.add(ActionID.class.getCanonicalName());
        hash.add(ActionReference.class.getCanonicalName());
        hash.add(ActionReferences.class.getCanonicalName());
//        hash.add(ActionState.class.getCanonicalName());
        return hash;
    }
    
    @Override
    public Iterable<? extends Completion> getCompletions(Element element, AnnotationMirror annotation, ExecutableElement member, String userText) {
        /*
        System.err.println("elem: " + element);
        System.err.println("anno: " + annotation.getAnnotationType().asElement().getSimpleName());
        System.err.println("member: " + member.getSimpleName());
        System.err.println("userText: " + userText);
         */
        if (annotation.getAnnotationType().asElement().getSimpleName().toString().contains(ActionReference.class.getSimpleName())) {
            if (member.getSimpleName().contentEquals("path")) { // NOI18N
                if (userText == null) {
                    userText = "";
                }
                if (userText.startsWith("\"")) {
                    userText = userText.substring(1);
                }
                
                Set<Completion> res = new HashSet<Completion>();
                for (String c : DEFAULT_COMPLETIONS) {
                    if (c.startsWith(userText)) {
                        res.add(Completions.of("\"" + c + '/', NbBundle.getMessage(ActionProcessor.class, "HINT_" + c)));
                    }
                }
                if (!res.isEmpty()) {
                    return res;
                }
                
                if (COMPLETIONS == null) {
                    String pathCompletions = System.getProperty(ActionReference.class.getName() + ".completion");
                    if (pathCompletions != null) {
                        ClassLoader l = Lookup.getDefault().lookup(ClassLoader.class);
                        if (l == null) {
                            l = Thread.currentThread().getContextClassLoader();
                        }
                        if (l == null) {
                            l = ActionProcessor.class.getClassLoader();
                        }
                        try {
                            COMPLETIONS = (Processor)Class.forName(pathCompletions, true, l).getDeclaredConstructor().newInstance();
                        } catch (Exception ex) {
                            Exceptions.printStackTrace(ex);
                            // no completions, OK
                            COMPLETIONS = this;
                        }
                    } else {
                        return res;
                    }
                }
                if (COMPLETIONS != null && COMPLETIONS != this) {
                    COMPLETIONS.init(processingEnv);
                    for (Completion completion : COMPLETIONS.getCompletions(element, annotation, member, userText)) {
                        res.add(completion);
                    }
                }
                return res;
            }
        }
        return Collections.emptyList();
    }
    
    @Override
    protected boolean handleProcess(
        Set<? extends TypeElement> annotations, RoundEnvironment roundEnv
    ) throws LayerGenerationException {
        TypeMirror actionListener = type(ActionListener.class);
        TypeMirror p1 = type(Presenter.Menu.class);
        TypeMirror p2 = type(Presenter.Toolbar.class);
        TypeMirror p3 = type(Presenter.Popup.class);
        TypeMirror caa = type(ContextAwareAction.class);
        TypeMirror dmc = type(DynamicMenuContent.class);
        TypeMirror at = type(Action.class);
        TypeMirror ot = type(Object.class);
        TypeMirror lt = type(EventListener.class);
        TypeMirror vt = type(Void.class);
        
        for (Element e : roundEnv.getElementsAnnotatedWith(ActionRegistration.class)) {
            ActionRegistration ar = e.getAnnotation(ActionRegistration.class);
            if (ar == null) {
                continue;
            }
            ActionID aid = e.getAnnotation(ActionID.class);
            if (aid == null) {
                throw new LayerGenerationException("@ActionRegistration can only be used together with @ActionID annotation", e, processingEnv, ar);
            }
            if (aid.id() == null) {
                continue;
            }
            if (aid.category().startsWith("Actions/")) {
                throw new LayerGenerationException("@ActionID category() should not start with Actions/", e, processingEnv, aid, "category");
            }
            if (!FQN.matcher(aid.id()).matches()) {
                throw new LayerGenerationException("@ActionID id() must be valid fully qualified name", e, processingEnv, aid, "id");
            }
            String id = aid.id().replace('.', '-');
            LayerBuilder builder = layer(e);
            File f = builder.file("Actions/" + aid.category() + "/" + id + ".instance");
            f.bundlevalue("displayName", ar.displayName(), ar, "displayName");
            
            String menuText = ar.menuText();
            if(!menuText.isEmpty()) {
                f.bundlevalue("menuText", menuText, ar, "menuText");
            }

            String popupText = ar.popupText();
            if (!popupText.isEmpty()) {
                f.bundlevalue("popupText", popupText, ar, "popupText");
            }
            
            String key;
            boolean createDelegate = true;
            if (e.getKind() == ElementKind.FIELD) {
                VariableElement var = (VariableElement)e;
                TypeMirror stringType = type(String.class);
                if (
                    e.asType() != stringType || 
                    !e.getModifiers().contains(Modifier.PUBLIC) || 
                    !e.getModifiers().contains(Modifier.STATIC) ||
                    !e.getModifiers().contains(Modifier.FINAL)
                ) {
                    throw new LayerGenerationException("Only static string constant fields can be annotated", e, processingEnv, ar);
                }
                if (ar.key().length() != 0) {
                    throw new LayerGenerationException("When annotating field, one cannot define key()", e, processingEnv, ar, "key");
                }
                
                createDelegate = false;
                key = var.getConstantValue().toString();
            } else if (e.getKind() == ElementKind.CLASS) {
                if (!isAssignable(e.asType(), actionListener)) {
                    throw new LayerGenerationException("Class annotated with @ActionRegistration must implement java.awt.event.ActionListener!", e, processingEnv, ar);
                }
                if (!e.getModifiers().contains(Modifier.PUBLIC)) {
                    throw new LayerGenerationException("Class has to be public", e, processingEnv, ar);
                }
                if (e.getEnclosingElement().getKind() == ElementKind.CLASS && !e.getModifiers().contains(Modifier.STATIC)) {
                    throw new LayerGenerationException("Inner class annotated with @ActionRegistration has to be static", e);
                }
                key = ar.key();
            } else {
                assert e.getKind() == ElementKind.METHOD : e;
                builder.instanceFile("dummy", null, ActionListener.class, ar, null);
                key = ar.key();
            }

            Boolean direct = null;
            AnnotationMirror arMirror = null;
            for (AnnotationMirror m : e.getAnnotationMirrors()) {
                if (m.getAnnotationType().toString().equals(ActionRegistration.class.getCanonicalName())) {
                    arMirror = m;
                    for (Map.Entry<? extends ExecutableElement,? extends AnnotationValue> entry : m.getElementValues().entrySet()) {
                        if (entry.getKey().getSimpleName().contentEquals("lazy")) {
                            direct = ! (Boolean) entry.getValue().getValue();
                            assert direct == !ar.lazy();
                            break;
                        }
                    }
                }
            }
            if (direct == null) {
                if (e.getKind() == ElementKind.FIELD) {
                    direct = false;
                } else {
                    TypeMirror type = e.getKind() == ElementKind.CLASS ? e.asType() : ((ExecutableElement) e).getReturnType();
                    direct = isAssignable(type, p1) || isAssignable(type, p2) || isAssignable(type, p3) || isAssignable(type, caa) || isAssignable(type, dmc);
                    if (direct) {
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "Should explicitly specify lazy attribute", e);
                    }
                }
            }
            
            if (direct) {
                if (key.length() != 0) {
                    throw new LayerGenerationException("Cannot specify key and use eager registration", e, processingEnv, ar, "key");
                }
                if (!ar.iconBase().isEmpty()) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "iconBase unused on eager registrations", e, arMirror);
                }
                f.instanceAttribute("instanceCreate", Action.class);
            } else {
                TypeMirror selectType = null;
                if (key.length() == 0) {
                    f.methodvalue("instanceCreate", "org.openide.awt.Actions", "alwaysEnabled");
                } else {
                    f.methodvalue("instanceCreate", "org.openide.awt.Actions", "callback");
                    if (createDelegate) {
                        f.methodvalue("fallback", "org.openide.awt.Actions", "alwaysEnabled");
                    }
                    f.stringvalue("key", key);
                }
                if (createDelegate) {
                    try {
                        f.instanceAttribute("delegate", ActionListener.class, ar, null);
                    } catch (LayerGenerationException ex) {
                        selectType = generateContext(e, f, ar);
                    }
                }
                if (ar.iconBase().length() > 0) {
                    builder.validateResource(ar.iconBase(), e, ar, "iconBase", true);
                    f.stringvalue("iconBase", ar.iconBase());
                }
                f.boolvalue("noIconInMenu", !ar.iconInMenu());
                if (ar.asynchronous()) {
                    f.boolvalue("asynchronous", true);
                }
                if (ar.surviveFocusChange()) {
                    f.boolvalue("surviveFocusChange", true); 
                }
                processActionState(e, ar.enabledOn(), f, selectType, true, at, ot, lt, vt);
                processActionState(e, ar.checkedOn(), f, selectType, false, at, ot, lt, vt);
            }
            f.write();
            
            ActionReference aref = e.getAnnotation(ActionReference.class);
            if (aref != null) {
                processReferences(e, aref, aid);
            }
            ActionReferences refs = e.getAnnotation(ActionReferences.class);
            if (refs != null) {
                for (ActionReference actionReference : refs.value()) {
                    processReferences(e, actionReference, aid);
                }
            }
            
        }
        for (Element e : roundEnv.getElementsAnnotatedWith(ActionReference.class)) {
            if (e.getAnnotation(ActionRegistration.class) != null) {
                continue;
            }
            ActionReference ref = e.getAnnotation(ActionReference.class);
            if (ref == null) {
                continue;
            }
            ActionID id = e.getAnnotation(ActionID.class);
            if (id != null) {
                processReferences(e, ref, id);
                continue;
            }
            throw new LayerGenerationException("Don't use @ActionReference without @ActionID", e, processingEnv, ref);
        }
        for (Element e : roundEnv.getElementsAnnotatedWith(ActionReferences.class)) {
            if (e.getAnnotation(ActionRegistration.class) != null) {
                continue;
            }
            ActionReferences refs = e.getAnnotation(ActionReferences.class);
            if (refs == null) {
                continue;
            }
            ActionID id = e.getAnnotation(ActionID.class);
            if (id != null) {
                for (ActionReference actionReference : refs.value()) {
                    if (!actionReference.id().id().isEmpty() || !actionReference.id().category().isEmpty()) {
                        throw new LayerGenerationException("Don't specify additional id=@ActionID(...) when using @ActionID on the element", e, processingEnv, actionReference.id());
                    }
                    processReferences(e, actionReference, id);
                }
            } else {
                for (ActionReference actionReference : refs.value()) {
                    if (actionReference.id().id().isEmpty() || actionReference.id().category().isEmpty()) {
                        throw new LayerGenerationException("Specify real id=@ActionID(...)", e, processingEnv, actionReference.id());
                    }
                    processReferences(e, actionReference, actionReference.id());
                }
            }
        }
        return true;
    }
    
    private void processActionState(Element e, ActionState as, File f, TypeMirror selectType, boolean enable, 
            TypeMirror actionType, TypeMirror objectType, TypeMirror eventListenerType, TypeMirror voidType) 
        throws LayerGenerationException {
        String property = as.property();
        TypeMirror enabledType = null;
        try {
            as.type();
        } catch (MirroredTypeException mte) {
            enabledType = mte.getTypeMirror();
        }
        if (enabledType == null || enabledType.getKind() != TypeKind.DECLARED) {
            throw new LayerGenerationException("Invalid enabled-on type in @ActionState", e, processingEnv, as, "type");
        }
        if (processingEnv.getTypeUtils().isSameType(enabledType, voidType)) {
            return;
        }
        if (!as.useActionInstance()) {
            if (processingEnv.getTypeUtils().isSameType(enabledType, objectType) && "".equals(as.property())) {
                if (!enable) {
                    throw new LayerGenerationException("Property must be specified", e, processingEnv, as);
                }
            }
        }
        DeclaredType dt = (DeclaredType) enabledType;
        if (processingEnv.getTypeUtils().isSameType(dt, objectType)) {
            if (selectType == null) {
                throw new LayerGenerationException("Property owner type must be specified", e, processingEnv, as);
            }
            dt = (DeclaredType)selectType;
        }
        String dtName = processingEnv.getElementUtils().getBinaryName((TypeElement)dt.asElement()).toString();

        f.stringvalue(enable ? "enableOnType" : "checkedOnType", dtName);
        
        if (!enable) {
            f.boolvalue(Actions.ACTION_VALUE_TOGGLE, true);
        }

        boolean isAction = processingEnv.getTypeUtils().isSameType(dt, actionType);
        switch (property) {
            case "": 
                if (as.useActionInstance()) {
                    property = null;
                    break;
                }
                property = enable ? "enabled" : Action.SELECTED_KEY; break;
            case ActionState.NULL_VALUE: property = null;
        }

        TypeElement tel = (TypeElement)dt.asElement();
        if (property != null && !isAction) {
            ExecutableElement getter = null;
            ExecutableElement invalidGetter = null;

            String capitalizedName = Character.toUpperCase(property.charAt(0)) + property.substring(1);
            String isGetter = "is" + capitalizedName;
            String getGetter = "get" + capitalizedName;
            
            for (ExecutableElement el : ElementFilter.methodsIn(processingEnv.getElementUtils().getAllMembers(tel))) {
                if (el.getSimpleName().contentEquals(isGetter)) {
                    if (!el.getParameters().isEmpty()) {
                        invalidGetter = el;
                    } else {
                        getter = el;
                        break;
                    }
                }
                if (el.getSimpleName().contentEquals(getGetter)) {
                    if (!el.getParameters().isEmpty()) {
                        if (invalidGetter == null) {
                            invalidGetter = el;
                        }
                    } else {
                        getter = el;
                    }
                }
            }

            if (getter == null) {
                if (invalidGetter != null) {
                    throw new LayerGenerationException("Getter " + dtName + "." + invalidGetter.toString() + " must take no parameters", 
                            e, processingEnv, as, "property");
                } else {
                    throw new LayerGenerationException("Property " + property + " not found in " + dtName + ".", 
                            e, processingEnv, as, "property");
                }
            }

            Set<Modifier> mods = getter.getModifiers();
            if (!mods.contains(Modifier.PUBLIC)) {
                    throw new LayerGenerationException("Getter " + dtName + "." + getter.toString() + " must be public", 
                            e, processingEnv, as, "property");
            }
        }
        if (property != null) {
            f.stringvalue(enable ? "enableOnProperty" : "checkedOnProperty", property); // NOI18N
        }
        
        TypeMirror listenType = null;
        try {
            as.listenOn();
            return;
        } catch (MirroredTypeException ex) {
            listenType = ex.getTypeMirror();
        }
        boolean explicitListenerType = !processingEnv.getTypeUtils().isSameType(listenType, eventListenerType);
        
        TypeElement lfaceElement = (TypeElement)((DeclaredType)listenType).asElement();
        String lfaceName = lfaceElement.getSimpleName().toString();
        String lfaceFQN = processingEnv.getElementUtils().getBinaryName(lfaceElement).toString();
        String addName = "add" + lfaceName;
        String removeName = "remove" + lfaceName;

        if (explicitListenerType) {
            if (lfaceElement.getKind() != ElementKind.INTERFACE) {
                throw new LayerGenerationException(lfaceFQN + " is not an interface", e, processingEnv, as, "listenOn");
            }
            if (!lfaceElement.getModifiers().contains(Modifier.PUBLIC)) {
                throw new LayerGenerationException(lfaceFQN + " is not public", e, processingEnv, as, "listenOn");
            }
        }

        ExecutableElement addMethod = null;
        ExecutableElement addCandidate = null;
        ExecutableElement removeMethod = null;
        ExecutableElement removeCandidate = null;
        for (ExecutableElement el : ElementFilter.methodsIn(processingEnv.getElementUtils().getAllMembers(tel))) {
            if (el.getSimpleName().contentEquals(addName)) {
                addCandidate = el;
                if (!el.getModifiers().contains(Modifier.PUBLIC) || el.getModifiers().contains(Modifier.STATIC)) {
                    continue;
                }
                if (el.getParameters().size() == 1 && 
                    processingEnv.getTypeUtils().isSameType(listenType, el.getParameters().get(0).asType())) {
                    addMethod = el;
                }
            } else if (el.getSimpleName().contentEquals(removeName)) {
                removeCandidate = el;
                if (!el.getModifiers().contains(Modifier.PUBLIC) || el.getModifiers().contains(Modifier.STATIC)) {
                    continue;
                }
                if (el.getParameters().size() == 1 && 
                    processingEnv.getTypeUtils().isSameType(listenType, el.getParameters().get(0).asType())) {
                    removeMethod = el;
                }
            }
        }
        if (addMethod == null) {
            if (addCandidate != null) {
                throw new LayerGenerationException("Method add" + 
                        addCandidate.getSimpleName() + " must be public and take exactly one parameter of type " +
                        lfaceName + ".", e, processingEnv, as, "listenOn");
            } else if (explicitListenerType) {
                throw new LayerGenerationException("Method add" + 
                        lfaceName + " not found on " + dtName, e, processingEnv, as, "listenOn");
            }
        }
        if (removeMethod == null) {
            if (removeCandidate != null) {
                throw new LayerGenerationException("Method remove" + 
                        removeCandidate.getSimpleName() + " must be public and take exactly one parameter of type " +
                        lfaceName + ".", e,processingEnv, as, "listenOn");
            } else if (explicitListenerType) {
                throw new LayerGenerationException("Method remove" + 
                        lfaceName + " not found on " + dtName, e, processingEnv, as, "listenOn");
            }
        }
        boolean wantsListen = explicitListenerType || (addMethod != null && removeMethod != null);
        if (wantsListen) {
            f.stringvalue(enable ? "enableOnChangeListener" : "checkedOnChangeListener", lfaceFQN);
        }
        if (!"".equals(as.listenOnMethod())) {
            if (!explicitListenerType) {
                throw new LayerGenerationException("Cannot specify listenOnMethod() without listenOn().", e,processingEnv, as, "listenOnMethod");
            }
            String m = as.listenOnMethod();
            boolean found = false;
            for (ExecutableElement el : ElementFilter.methodsIn(processingEnv.getElementUtils().getAllMembers(lfaceElement))) {
                if (el.getSimpleName().contentEquals(m)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new LayerGenerationException("Interface " + lfaceFQN + " does not contain method " + m,
                    e, processingEnv, as, "listenOnMethod");
            }
            f.stringvalue(enable ? "enableOnMethod" : "checkedOnMethod", m);
        }
        
        if (!"".equals(as.checkedValue())) {
            switch (as.checkedValue()) {
                case ActionState.NULL_VALUE:
                    f.boolvalue(enable ? "enableOnNull" : "checkedOnNull", true);
                    break;
                case ActionState.NON_NULL_VALUE:
                    f.boolvalue(enable ? "enableOnNull" : "checkedOnNull", false);
                    break;
                default:
                    f.stringvalue(enable ? "enableOnValue" : "checkedOnValue", as.checkedValue());
                    break;
            }
        }
        if (as.useActionInstance()) {
            f.stringvalue(enable ? "enableOnActionProperty" : "checkedOnActionProperty", 
                    enable ? "enabled" : Action.SELECTED_KEY);
        }
    }

    private TypeMirror type(Class<?> type) {
        final TypeElement e = processingEnv.getElementUtils().getTypeElement(type.getCanonicalName());
        return e == null ? null : e.asType();
    }

    private TypeMirror generateContext(Element e, File f, ActionRegistration ar) throws LayerGenerationException {
        ExecutableElement ee = null;
        ExecutableElement candidate = null;
        for (ExecutableElement element : ElementFilter.constructorsIn(e.getEnclosedElements())) {
            if (element.getKind() == ElementKind.CONSTRUCTOR) {
                candidate = element;
                if (!element.getModifiers().contains(Modifier.PUBLIC)) {
                    continue;
                }
                if (ee != null) {
                    throw new LayerGenerationException("Only one public constructor allowed", e, processingEnv, ar); // NOI18N
                }
                ee = element;
            }

        }
        
        if (ee == null || ee.getParameters().size() != 1) {
            if (candidate != null) {
                throw new LayerGenerationException("Constructor has to be public with one argument", candidate);
            }
            throw new LayerGenerationException("Constructor must have one argument", ee);
        }

        VariableElement ve = (VariableElement)ee.getParameters().get(0);
        TypeMirror ctorType = ve.asType();
        switch (ctorType.getKind()) {
        case ARRAY:
            String elemType = ((ArrayType) ctorType).getComponentType().toString();
            throw new LayerGenerationException("Use List<" + elemType + "> rather than " + elemType + "[] in constructor", e, processingEnv, ar);
        case DECLARED:
            break; // good
        default:
            throw new LayerGenerationException("Must use SomeType (or List<SomeType>) in constructor, not " + ctorType.getKind());
        }
        DeclaredType dt = (DeclaredType) ctorType;
        String dtName = processingEnv.getElementUtils().getBinaryName((TypeElement)dt.asElement()).toString();
        if ("java.util.List".equals(dtName)) {
            if (dt.getTypeArguments().isEmpty()) {
                throw new LayerGenerationException("Use List<SomeType>", ee);
            }
            f.stringvalue("type", binaryName(dt.getTypeArguments().get(0)));
            f.methodvalue("delegate", "org.openide.awt.Actions", "inject");
            f.stringvalue("injectable", processingEnv.getElementUtils().getBinaryName((TypeElement) e).toString());
            f.stringvalue("selectionType", "ANY");
            f.methodvalue("instanceCreate", "org.openide.awt.Actions", "context");
            return dt.getTypeArguments().get(0);
        }
        if (!dt.getTypeArguments().isEmpty()) {
            throw new LayerGenerationException("No type parameters allowed in ", ee);
        }

        f.stringvalue("type", binaryName(ctorType));
        f.methodvalue("delegate", "org.openide.awt.Actions", "inject");
        f.stringvalue("injectable", processingEnv.getElementUtils().getBinaryName((TypeElement)e).toString());
        f.stringvalue("selectionType", "EXACTLY_ONE");
        f.methodvalue("instanceCreate", "org.openide.awt.Actions", "context");
        return ctorType;
    }
    
    private String binaryName(TypeMirror t) {
        Element e = processingEnv.getTypeUtils().asElement(t);
        if (e != null && (e.getKind().isClass() || e.getKind().isInterface())) {
            return processingEnv.getElementUtils().getBinaryName((TypeElement) e).toString();
        } else {
            return t.toString(); // fallback - might not always be right
        }
    }

    private boolean isAssignable(TypeMirror first, TypeMirror snd) {
        if (snd == null) {
            return false;
        } else {
            return processingEnv.getTypeUtils().isAssignable(first, snd);
        }
    }

    private void processReferences(Element e, ActionReference ref, ActionID aid) throws LayerGenerationException {
        if (!ref.id().category().isEmpty() && !ref.id().id().isEmpty()) {
            if (!aid.id().equals(ref.id().id()) || !aid.category().equals(ref.id().category())) {
                throw new LayerGenerationException("Can't specify id() attribute when @ActionID provided on the element", e, processingEnv, aid);
            }
        }
        String name = ref.name();
        if (name.isEmpty()) {
            name = aid.id().replace('.', '-');
        }
        
        if (ref.path().startsWith("Shortcuts")) {
            KeyStroke[] stroke = Utilities.stringToKeys(name);
            if (stroke == null) {
                throw new LayerGenerationException(
                    "Registrations in Shortcuts folder need to represent a key. "
                    + "Specify value for 'name' attribute.\n"
                    + "See org.openide.util.Utilities.stringToKeys for possible values. Current "
                    + "name=\"" + name + "\" is not valid.\n", e, processingEnv, ref, "path"
                );
            }
        }
        
        File f = layer(e).file(ref.path() + "/" + name + ".shadow");
        f.stringvalue("originalFile", "Actions/" + aid.category() + "/" + aid.id().replace('.', '-') + ".instance");
        f.position(ref.position());
        f.write();
        
        if (ref.separatorAfter() != Integer.MAX_VALUE) {
            if (ref.position() == Integer.MAX_VALUE || ref.position() >= ref.separatorAfter()) {
                throw new LayerGenerationException("separatorAfter() must be greater than position()", e, processingEnv, ref);
            }
            File after = layer(e).file(ref.path() + "/" + name + "-separatorAfter.instance");
            after.newvalue("instanceCreate", JSeparator.class.getName());
            after.position(ref.separatorAfter());
            after.write();
        }
        if (ref.separatorBefore() != Integer.MAX_VALUE) {
            if (ref.position() == Integer.MAX_VALUE || ref.position() <= ref.separatorBefore()) {
                throw new LayerGenerationException("separatorBefore() must be lower than position()", e, processingEnv, ref);
            }
            File before = layer(e).file(ref.path() + "/" + name + "-separatorBefore.instance");
            before.newvalue("instanceCreate", JSeparator.class.getName());
            before.position(ref.separatorBefore());
            before.write();
        }
    }
}
