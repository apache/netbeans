/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.openide.awt;

import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.processing.Completion;
import javax.annotation.processing.Completions;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
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
@SupportedSourceVersion(SourceVersion.RELEASE_7)
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
                            COMPLETIONS = (Processor)Class.forName(pathCompletions, true, l).newInstance();
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
                        generateContext(e, f, ar);
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

    private TypeMirror type(Class<?> type) {
        final TypeElement e = processingEnv.getElementUtils().getTypeElement(type.getCanonicalName());
        return e == null ? null : e.asType();
    }

    private void generateContext(Element e, File f, ActionRegistration ar) throws LayerGenerationException {
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
            return;
        }
        if (!dt.getTypeArguments().isEmpty()) {
            throw new LayerGenerationException("No type parameters allowed in ", ee);
        }

        f.stringvalue("type", binaryName(ctorType));
        f.methodvalue("delegate", "org.openide.awt.Actions", "inject");
        f.stringvalue("injectable", processingEnv.getElementUtils().getBinaryName((TypeElement)e).toString());
        f.stringvalue("selectionType", "EXACTLY_ONE");
        f.methodvalue("instanceCreate", "org.openide.awt.Actions", "context");
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
