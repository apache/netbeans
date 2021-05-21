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

package org.netbeans.debugger.registry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import org.netbeans.api.debugger.LazyActionsManagerListener;
import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerEngineProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.DebuggerServiceRegistrations;
import org.netbeans.spi.debugger.SessionProvider;
import org.openide.filesystems.annotations.LayerBuilder;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.openide.util.lookup.ServiceProvider;

/** Processor to hide all the complexities of settings layer registration.
 *
 * @author Martin Entlicher
 */
@ServiceProvider(service=Processor.class)
public class DebuggerProcessor extends LayerGeneratingProcessor {

    public @Override Set<String> getSupportedAnnotationTypes() {
        return new HashSet<String>(Arrays.asList(
            ActionsProvider.Registration.class.getCanonicalName(),
            DebuggerEngineProvider.Registration.class.getCanonicalName(),
            SessionProvider.Registration.class.getCanonicalName(),
            LazyActionsManagerListener.Registration.class.getCanonicalName(),
            DebuggerServiceRegistration.class.getCanonicalName(),
            DebuggerServiceRegistrations.class.getCanonicalName()
        ));
    }

    @Override
    protected boolean handleProcess(
        Set<? extends TypeElement> annotations,
        RoundEnvironment env
    ) throws LayerGenerationException {
        if (env.processingOver()) {
            return false;
        }

        int cnt = 0;
        for (Element e : env.getElementsAnnotatedWith(ActionsProvider.Registration.class)) {
            ActionsProvider.Registration reg = e.getAnnotation(ActionsProvider.Registration.class);

            final String path = reg.path();
            final String[] actions = reg.actions();
            final String[] mimeTypes = reg.activateForMIMETypes();
            handleProviderRegistrationInner(e, ActionsProvider.class, path, actions, mimeTypes);
            //layer(e).instanceFile("Debugger/"+path, null, ActionsProvider.class).
            //        stringvalue("serviceName", instantiableClassOrMethod(e)).
            //        stringvalue("serviceClass", ActionsProvider.class.getName()).
            //        methodvalue("instanceCreate", "org.netbeans.debugger.registry.ActionsProviderContextAware", "createService").
            //        write();
            cnt++;
        }
        for (Element e : env.getElementsAnnotatedWith(ActionsProvider.Registrations.class)) {
            ActionsProvider.Registrations regs = e.getAnnotation(ActionsProvider.Registrations.class);
            for (ActionsProvider.Registration reg : regs.value()) {
                final String path = reg.path();
                final String[] actions = reg.actions();
                final String[] mimeTypes = reg.activateForMIMETypes();
                handleProviderRegistrationInner(e, ActionsProvider.class, path, actions, mimeTypes);
            }
            cnt++;
        }
        for (Element e : env.getElementsAnnotatedWith(LazyActionsManagerListener.Registration.class)) {
            LazyActionsManagerListener.Registration reg = e.getAnnotation(LazyActionsManagerListener.Registration.class);

            final String path = reg.path();
            handleProviderRegistrationInner(e, LazyActionsManagerListener.class, path);
            cnt++;
        }
        for (Element e : env.getElementsAnnotatedWith(DebuggerEngineProvider.Registration.class)) {
            DebuggerEngineProvider.Registration reg = e.getAnnotation(DebuggerEngineProvider.Registration.class);

            final String path = reg.path();
            handleProviderRegistrationInner(e, DebuggerEngineProvider.class, path);
            cnt++;
        }
        for (Element e : env.getElementsAnnotatedWith(SessionProvider.Registration.class)) {
            SessionProvider.Registration reg = e.getAnnotation(SessionProvider.Registration.class);

            final String path = reg.path();
            handleProviderRegistrationInner(e, SessionProvider.class, path);
            cnt++;
        }
        for (Element e : env.getElementsAnnotatedWith(DebuggerServiceRegistration.class)) {
            DebuggerServiceRegistration reg = e.getAnnotation(DebuggerServiceRegistration.class);
            // TODO: Get rid of AnnotationMirror after http://bugs.sun.com/view_bug.do?bug_id=6519115 is fixed.
            AnnotationMirror am = null;
            for (AnnotationMirror am_ : e.getAnnotationMirrors()) {
                if (am_.getAnnotationType().toString().equals(DebuggerServiceRegistration.class.getName())) {
                    am = am_;
                    break;
                }
            }

            handleServiceRegistration(e, reg, am);
            cnt++;
        }
        for (Element e : env.getElementsAnnotatedWith(DebuggerServiceRegistrations.class)) {
            DebuggerServiceRegistrations regs = e.getAnnotation(DebuggerServiceRegistrations.class);
            // TODO: Get rid of AnnotationMirror after http://bugs.sun.com/view_bug.do?bug_id=6519115 is fixed.
            AnnotationMirror amregs = null;
            for (AnnotationMirror am_ : e.getAnnotationMirrors()) {
                if (am_.getAnnotationType().toString().equals(DebuggerServiceRegistrations.class.getName())) {
                    amregs = am_;
                    break;
                }
            }

            DebuggerServiceRegistration[] regsv = regs.value();
            if (regsv == null || regsv.length == 0) {
                throw new IllegalArgumentException("No service registration for element "+e);
            }
            AnnotationMirror[] ams = new AnnotationMirror[regsv.length];
            Map<? extends ExecutableElement, ? extends AnnotationValue> annElementValues = amregs.getElementValues();
            for (AnnotationValue av : annElementValues.values()) {
                Object value = av.getValue();
                if (value instanceof Collection) {
                    ams = (AnnotationMirror[]) ((Collection) value).toArray(ams);
                }
            }
            for (int i = 0; i < regsv.length; i++) {
                handleServiceRegistration(e, regsv[i], ams[i]);
            }
            cnt++;
        }
        return cnt == annotations.size();
    }

    private List<? extends TypeMirror> getTypeMirrors(DebuggerServiceRegistration reg, AnnotationMirror am) {

        /* TODO uncomment this after http://bugs.sun.com/view_bug.do?bug_id=6519115 is fixed.
        List<? extends TypeMirror> typeMirrors = null;
        try {
            reg.types();
            throw new IllegalStateException("No type mirros obtained from element "+e);
        } catch (MirroredTypeException mte) {
            typeMirrors = Collections.singletonList(mte.getTypeMirror());
            System.err.println("Have one "+typeMirrors.get(0)+" type mirror for element "+e);
        } catch (MirroredTypesException mte) {
            typeMirrors = mte.getTypeMirrors();
            System.err.println("Have "+typeMirrors.size()+" type mirrors for element "+e);
            System.err.println("    mirrors = "+typeMirrors);
        }
         */

        // TODO: Delete the rest of this method after http://bugs.sun.com/view_bug.do?bug_id=6519115 is fixed.
        List<TypeMirror> typeMirrors = null;
        Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues =
                am.getElementValues();
        //System.err.println("am:\n elementValues = "+elementValues);
        String classNames = null;
        for (ExecutableElement ee : elementValues.keySet()) {
            if (ee.getSimpleName().contentEquals("types")) { // NOI18N
                classNames = elementValues.get(ee).getValue().toString();
            }
        }
        if (classNames == null) {
            throw new IllegalArgumentException("Annotation "+am+" does not provide types");
        }
        //System.err.println("classNames before translation = "+classNames);
        typeMirrors = new ArrayList<TypeMirror>();
        int i1 = 0;
        int i2;
        while ((i2 = classNames.indexOf(',', i1)) > 0 || i1 < classNames.length()) {
            if (i2 < 0) i2 = classNames.length();
            String className = classNames.substring(i1, i2).trim();
            if (className.endsWith(".class")) {
                className = className.substring(0, className.length() - ".class".length());
            }
            TypeElement type = processingEnv.getElementUtils().getTypeElement(className);
            typeMirrors.add(type.asType());
            i1 = i2 + 1;
        }
        //System.err.println("=> type mirrors = "+typeMirrors);

        return typeMirrors;
    }

    private void handleServiceRegistration(Element e, DebuggerServiceRegistration reg, AnnotationMirror am) throws LayerGenerationException {
        // Class[] classes = reg.types(); - Cant NOT do that, classes are not created at compile time.
        // e.getAnnotationMirrors() - use this not to generate MirroredTypeException
        List<? extends TypeMirror> typeMirrors = getTypeMirrors(reg, am);

        List<? extends TypeMirror> notImplTypeMirrors = implementsInterfaces(e, typeMirrors);
        if (!notImplTypeMirrors.isEmpty()) {
            throw new IllegalArgumentException("Annotated element "+e+" does not implement all interfaces " + notImplTypeMirrors);
        }

        String path = reg.path();
        LayerBuilder lb = layer(e);
        String className = instantiableClassOrMethod(e);
        //System.err.println("icm = "+className);

        boolean isActionThere = false;
        StringBuilder classNamesBuilder = new StringBuilder();
        for (TypeMirror tm : typeMirrors) {
            TypeElement te = (TypeElement) processingEnv.getTypeUtils().asElement(tm);
            String cn;
            if (te != null) {
                cn = processingEnv.getElementUtils().getBinaryName(te).toString();
            } else {
                cn = tm.toString();
            }
            if (classNamesBuilder.length() > 0) {
                classNamesBuilder.append(", ");
            }
            classNamesBuilder.append(cn);
            if ("javax.swing.Action".equals(cn)) {
                isActionThere = true;
            }
        }

        if (path != null && path.length() > 0) {
            path = "Debugger/"+path;
        } else {
            path = "Debugger";
        }
        //System.err.println("path = "+path);
        String basename = className.replace('.', '-');
        LayerBuilder.File f = lb.file(path + "/" + basename + ".instance");
        //LayerBuilder.File f = lb.instanceFile(path, null, Evaluator.class);
        if (isActionThere) {
            f.boolvalue("misplaced.action.allowed", true);
        }
        f.stringvalue(ContextAwareServiceHandler.SERVICE_NAME, className).
          //stringvalue("serviceClass", Evaluator.class.getName()).
          stringvalue("instanceOf", classNamesBuilder.toString()).
          methodvalue("instanceCreate", "org.netbeans.spi.debugger.ContextAwareSupport", "createService").
          position(reg.position()).
          write();
    }

    private void handleProviderRegistrationInner(Element e, Class providerClass, String path) throws IllegalArgumentException, LayerGenerationException {
        handleProviderRegistrationInner(e, providerClass, path, null, null);
    }

    private void handleProviderRegistrationInner(Element e, Class providerClass, String path,
                                                 String[] actions, String[] enabledOnMIMETypes
                                                 ) throws IllegalArgumentException, LayerGenerationException {
        String className = instantiableClassOrMethod(e);
        if (!isClassOf(e, providerClass)) {
            throw new IllegalArgumentException("Annotated element "+e+" is not an instance of " + providerClass);
        }
        if (path != null && path.length() > 0) {
            path = "Debugger/"+path;
        } else {
            path = "Debugger";
        }
        LayerBuilder lb = layer(e);
        String basename = className.replace('.', '-');
        LayerBuilder.File f = lb.file(path + "/" + basename + ".instance");
        f.stringvalue(ContextAwareServiceHandler.SERVICE_NAME, className).
          stringvalue("serviceClass", providerClass.getName());
        if (actions != null && actions.length > 0) {
            f.stringvalue(ContextAwareServiceHandler.SERVICE_ACTIONS, Arrays.toString(actions));
        }
        if (enabledOnMIMETypes != null && enabledOnMIMETypes.length > 0) {
            f.stringvalue(ContextAwareServiceHandler.SERVICE_ENABLED_MIMETYPES, Arrays.toString(enabledOnMIMETypes));
        }
        f.stringvalue("instanceOf", providerClass.getName());
        f.methodvalue("instanceCreate", providerClass.getName()+"$ContextAware", "createService");
        f.write();
    }

    private boolean isClassOf(Element e, Class providerClass) {
        switch (e.getKind()) {
            case CLASS: {
                TypeElement te = (TypeElement) e;
                TypeMirror superType = te.getSuperclass();
                if (superType.getKind().equals(TypeKind.NONE)) {
                    return false;
                } else {
                    e = ((DeclaredType) superType).asElement();
                    String clazz = processingEnv.getElementUtils().getBinaryName((TypeElement) e).toString();
                    if (clazz.equals(providerClass.getName())) {
                        return true;
                    } else {
                        return isClassOf(e, providerClass);
                    }
                }
            }
            case METHOD: {
                TypeMirror retType = ((ExecutableElement) e).getReturnType();
                if (retType.getKind().equals(TypeKind.NONE)) {
                    return false;
                } else {
                    e = ((DeclaredType) retType).asElement();
                    String clazz = processingEnv.getElementUtils().getBinaryName((TypeElement) e).toString();
                    if (clazz.equals(providerClass.getName())) {
                        return true;
                    } else {
                        return isClassOf(e, providerClass);
                    }
                }
            }
            default:
                throw new IllegalArgumentException("Annotated element is not loadable as an instance: " + e);
        }
    }

    private boolean implementsInterfaces(Element e, String classNames) {
        Set<String> interfaces = new HashSet<>(Arrays.asList(classNames.split("[, ]+")));
        return implementsInterfaces(e, interfaces);
    }

    private boolean implementsInterfaces(Element e, Set<String> interfaces) {
        switch (e.getKind()) {
            case CLASS:
            case INTERFACE: {
                TypeElement te = (TypeElement) e;
                removeImplementingInterfaces(te.asType(), interfaces);
                /*
                List<? extends TypeMirror> interfs = te.getInterfaces();
                for (TypeMirror tm : interfs) {
                    e = ((DeclaredType) tm).asElement();
                    String clazz = processingEnv.getElementUtils().getBinaryName((TypeElement) e).toString();
                    boolean contains = interfaces.remove(clazz);
                    if (!contains) {
                        implementsInterfaces(e, interfaces);
                    }
                }
                 */
                break;
            }
            case METHOD: {
                TypeMirror retType = ((ExecutableElement) e).getReturnType();
                if (retType.getKind().equals(TypeKind.NONE)) {
                    return false;
                } else {
                    TypeElement te = (TypeElement) ((DeclaredType) retType).asElement();
                    List<? extends TypeMirror> interfs = te.getInterfaces();
                    for (TypeMirror tm : interfs) {
                        e = ((DeclaredType) tm).asElement();
                        String clazz = processingEnv.getElementUtils().getBinaryName((TypeElement) e).toString();
                        interfaces.remove(clazz);
                    }
                }
                break;
            }
            default:
                throw new IllegalArgumentException("Annotated element is not loadable as an instance: " + e);
        }
        return interfaces.isEmpty();
    }

    private void removeImplementingInterfaces(TypeMirror tm, Set<String> interfaces) {
        for (Iterator<String> i = interfaces.iterator(); i.hasNext(); ) {
            String type = i.next();
            TypeMirror typeMirror =
                processingEnv.getTypeUtils().getDeclaredType(
                    processingEnv.getElementUtils().getTypeElement(type.replace('$', '.')));

            if (processingEnv.getTypeUtils().isAssignable(tm, typeMirror)) {
                i.remove();
            }
        }
    }

    private List<? extends TypeMirror> implementsInterfaces(Element e, List<? extends TypeMirror> typeMirrors) {
        List<TypeMirror> notImplementedMirrors = Collections.emptyList();
        switch (e.getKind()) {
            case CLASS:
            case INTERFACE: {
                TypeElement te = (TypeElement) e;
                TypeMirror tm = te.asType();
                for (TypeMirror typeMirror : typeMirrors) {
                    if (!processingEnv.getTypeUtils().isAssignable(tm, typeMirror)) {
                        if (notImplementedMirrors == Collections.EMPTY_LIST) {
                            notImplementedMirrors = new ArrayList<TypeMirror>();
                        }
                        notImplementedMirrors.add(typeMirror);
                    }
                }
                /*
                TypeMirror typeMirror = type != null ?
                    processingEnv.getTypeUtils().getDeclaredType(
                        processingEnv.getElementUtils().getTypeElement(type.getName().replace('$', '.'))) :
                    null;

                        processingEnv.getTypeUtils().isAssignable(te.asType(), null);
                 */
                //removeImplementingInterfaces(te.asType(), interfaces);
                /*
                List<? extends TypeMirror> interfs = te.getInterfaces();
                for (TypeMirror tm : interfs) {
                    e = ((DeclaredType) tm).asElement();
                    String clazz = processingEnv.getElementUtils().getBinaryName((TypeElement) e).toString();
                    boolean contains = interfaces.remove(clazz);
                    if (!contains) {
                        implementsInterfaces(e, interfaces);
                    }
                }
                 */
                break;
            }
            case METHOD: {
                TypeMirror retType = ((ExecutableElement) e).getReturnType();
                if (retType.getKind().equals(TypeKind.NONE)) {
                    return typeMirrors;
                } else {
                    TypeElement te = (TypeElement) ((DeclaredType) retType).asElement();
                    TypeMirror tm = te.asType();
                    for (TypeMirror typeMirror : typeMirrors) {
                        if (!processingEnv.getTypeUtils().isAssignable(tm, typeMirror)) {
                            if (notImplementedMirrors == Collections.EMPTY_LIST) {
                                notImplementedMirrors = new ArrayList<TypeMirror>();
                            }
                            notImplementedMirrors.add(typeMirror);
                        }
                    }

                    /*
                    List<? extends TypeMirror> interfs = te.getInterfaces();
                    for (TypeMirror tm : interfs) {
                        e = ((DeclaredType) tm).asElement();
                        String clazz = processingEnv.getElementUtils().getBinaryName((TypeElement) e).toString();
                        interfaces.remove(clazz);
                    }
                     */
                }
                break;
            }
            default:
                throw new IllegalArgumentException("Annotated element is not loadable as an instance: " + e);
        }
        return notImplementedMirrors;
    }

    private String instantiableClassOrMethod(Element e) throws IllegalArgumentException, LayerGenerationException {
        switch (e.getKind()) {
            case CLASS: {
                String clazz = processingEnv.getElementUtils().getBinaryName((TypeElement) e).toString();
                //System.err.println("instantiableClassOrMethod(): class = "+clazz);
                if (e.getModifiers().contains(Modifier.ABSTRACT)) {
                    throw new LayerGenerationException(clazz + " must not be abstract", e);
                }
                {
                    boolean hasDefaultCtor = false;
                    boolean hasContextCtor = false;
                    for (ExecutableElement constructor : ElementFilter.constructorsIn(e.getEnclosedElements())) {
                        List<? extends VariableElement> parameters = constructor.getParameters();
                        //System.err.println("  parameters = "+parameters+", size = "+parameters.size());
                        if (parameters.isEmpty()) {
                            hasDefaultCtor = true;
                            break;
                        }
                        if (parameters.size() == 1) {
                            String type = parameters.get(0).asType().toString();
                            //System.err.println("Param type = "+type);
                            if (ContextProvider.class.getName().equals(type)) {
                                hasContextCtor = true;
                                break;
                            }
                        }
                    }
                    if (!(hasDefaultCtor || hasContextCtor)) {
                        throw new LayerGenerationException(clazz + " must have a no-argument constructor or constuctor taking "+ContextProvider.class.getName()+" as a parameter.", e);
                    }
                }
                /*propType = processingEnv.getElementUtils().getTypeElement("java.util.Properties").asType();
                        if (
                            m.getParameters().size() == 1 &&
                            m.getSimpleName().contentEquals("writeProperties") &&
                            m.getParameters().get(0).asType().equals(propType) &&
                            m.getReturnType().getKind() == TypeKind.VOID
                        ) {
                            hasWrite = true;
                        }
                }
                 * */
                return clazz;
            }
            case METHOD: {
                ExecutableElement ee = (ExecutableElement) e;
                String methodName = ee.getSimpleName().toString();
                String clazz = processingEnv.getElementUtils().getBinaryName((TypeElement) ee.getEnclosingElement()).toString();
                if (!e.getModifiers().contains(Modifier.STATIC)) {
                    throw new LayerGenerationException(ee + " must be static", e);
                }
                if (ee.getParameters().size() > 0) {
                    throw new LayerGenerationException(ee + " must not have any parameters", e);
                }
                return clazz+"."+methodName+"()";
            }
            default:
                throw new IllegalArgumentException("Annotated element is not loadable as an instance: " + e);
        }
    }

    /**
     * Translates "org.MyClass1.class, org.MyClass2.class, ... " to
     * "org.MyClass1, org.MyClass2, ..."
     * @param classNames
     * @return comma-separated class names
     */
    private String translateClassNames(String classNames) {
        StringBuilder builder = new StringBuilder();
        int i1 = 0;
        int i2;
        while ((i2 = classNames.indexOf(',', i1)) > 0) {
            if (i1 > 0) builder.append(',');
            builder.append(translateClass(classNames.substring(i1, i2).trim()));
            i1 = i2 + 1;
        }
        if (i1 > 0) builder.append(',');
        builder.append(translateClass(classNames.substring(i1).trim()));

        return builder.toString();
    }

    private String translateClass(String className) {
        if (className.endsWith(".class")) {
            className = className.substring(0, className.length() - ".class".length());
        }
        TypeElement type = processingEnv.getElementUtils().getTypeElement(className);
        //System.err.println("translateClass("+className+") type = "+type);
        return processingEnv.getElementUtils().getBinaryName(type).toString();
    }
}
