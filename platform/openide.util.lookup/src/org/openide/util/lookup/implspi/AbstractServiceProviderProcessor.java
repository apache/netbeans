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

package org.openide.util.lookup.implspi;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.WeakHashMap;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
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
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

/**
 * Infrastructure for generating {@code META-INF/services/*} and
 * {@code META-INF/namedservices/*} registrations from annotations. From version
 * 8.40, it is not necessary (and is not recommended) to declare
 * @{@link SupportedSourceVersion} on subclasses: the default implementation
 * declares support for {@link SourceVersion#latest()}. Declare specific
 * {@link SourceVersion} limits only when necessary.
 *
 * @since 8.1
 */
public abstract class AbstractServiceProviderProcessor extends AbstractProcessor {

    private final Map<Filer,Map<String,SortedSet<ServiceLoaderLine>>> outputFilesByProcessor =
            new WeakHashMap<Filer,Map<String,SortedSet<ServiceLoaderLine>>>();
    private final Map<Filer,Map<String,List<Element>>> originatingElementsByProcessor =
            new WeakHashMap<Filer,Map<String,List<Element>>>();
    private final Map<TypeElement,Boolean> verifiedClasses = new WeakHashMap<TypeElement,Boolean>();

    /** Throws IllegalStateException. For access by selected subclasses. */
    protected AbstractServiceProviderProcessor() {
        if (getClass().getName().equals("org.netbeans.modules.openide.util.ServiceProviderProcessor")) { // NOI18N
            // OK subclass
            return;
        }
        if (getClass().getName().equals("org.netbeans.modules.openide.util.URLStreamHandlerRegistrationProcessor")) { // NOI18N
            // OK subclass
            return;
        }
        if (getClass().getName().equals("org.netbeans.modules.openide.util.NamedServiceProcessor")) { // NOI18N
            // OK subclass
            return;
        }
        throw new IllegalStateException();
    }
    
    public @Override final boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.errorRaised()) {
            return false;
        }
        if (roundEnv.processingOver()) {
            writeServices();
            outputFilesByProcessor.clear();
            originatingElementsByProcessor.clear();
            return true;
        } else {
            return handleProcess(annotations, roundEnv);
        }
    }

    /**
     * The regular body of {@link #process}.
     * Called during regular rounds if there are no outstanding errors.
     * In the last round, one of the processors will write out generated registrations.
     * @param annotations as in {@link #process}
     * @param roundEnv as in {@link #process}
     * @return as in {@link #process}
     */
    protected abstract boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv);

    /**
     * Register a service.
     * If the class does not have an appropriate signature, an error will be printed and the registration skipped.
     * @param clazz the service implementation type (an error will be reported if not a {@link TypeElement})
     * @param annotation the (top-level) annotation registering the service, for diagnostic purposes
     * @param type the type to which the implementation must be assignable
     * @param path a path under which to register, or "" if inapplicable
     * @param position a position at which to register, or {@link Integer#MAX_VALUE} to skip
     * @param supersedes possibly empty list of implementation to supersede
     * @since 8.8
     */
    protected final void register(
        Element el, Class<? extends Annotation> annotation,
        TypeMirror type, String path, int position, String... supersedes
    ) {
        if (el.getKind() != ElementKind.CLASS) {
            processingEnv.getMessager().printMessage(Kind.ERROR, annotation.getName() + " is not applicable to a " + el.getKind(), el);
            return;
        }
        if (el.getEnclosingElement().getKind() == ElementKind.CLASS && !el.getModifiers().contains(Modifier.STATIC)) {
            processingEnv.getMessager().printMessage(Kind.ERROR, "Inner class needs to be static to be annotated with @ServiceProvider", el);
            return;
        }
        
        TypeElement clazz = (TypeElement) el;
        String impl = processingEnv.getElementUtils().getBinaryName(clazz).toString();
        String xface = processingEnv.getElementUtils().getBinaryName((TypeElement) processingEnv.getTypeUtils().asElement(type)).toString();
        if (!processingEnv.getTypeUtils().isAssignable(clazz.asType(), type)) {
            AnnotationMirror ann = findAnnotationMirror(clazz, annotation);
            processingEnv.getMessager().printMessage(Kind.ERROR, impl + " is not assignable to " + xface,
                    clazz, ann, findAnnotationValue(ann, "service"));
            return;
        }
        String rsrc = (path.length() > 0 ? "META-INF/namedservices/" + path + "/" : "META-INF/services/") + xface;
        Boolean verify = verifiedClasses.get(clazz);
        if (verify == null) {
            verify = verifyServiceProviderSignature(clazz, annotation);
            verifiedClasses.put(clazz, verify);
        }
        if (!verify) {
            return;
        }
        registerImpl(clazz, impl, rsrc, position, supersedes);
    }
    
    protected final void register(Element el, String path) {
        TypeElement clazz = (TypeElement)el;
        String impl = processingEnv.getElementUtils().getBinaryName(clazz).toString();
        registerImpl(clazz, impl, path, Integer.MAX_VALUE);
    }
    
    private void registerImpl(
        TypeElement clazz, String impl, String rsrc, int position, String... supersedes
    ) {
        /*
        processingEnv.getMessager().printMessage(Kind.NOTE,
                impl + " to be registered as a " + xface + (path.length() > 0 ? " under " + path : ""));
        */
        Filer filer = processingEnv.getFiler();
        {
            Map<String,List<Element>> originatingElements = originatingElementsByProcessor.get(filer);
            if (originatingElements == null) {
                originatingElements = new HashMap<String,List<Element>>();
                originatingElementsByProcessor.put(filer, originatingElements);
            }
            List<Element> origEls = originatingElements.get(rsrc);
            if (origEls == null) {
                origEls = new ArrayList<Element>();
                originatingElements.put(rsrc, origEls);
            }
            origEls.add(clazz);
        }
        Map<String,SortedSet<ServiceLoaderLine>> outputFiles = outputFilesByProcessor.get(filer);
        if (outputFiles == null) {
            outputFiles = new HashMap<String,SortedSet<ServiceLoaderLine>>();
            outputFilesByProcessor.put(filer, outputFiles);
        }
        SortedSet<ServiceLoaderLine> lines = outputFiles.get(rsrc);
        if (lines == null) {
            lines = new TreeSet<ServiceLoaderLine>();
            try {
                try {
                    FileObject in = filer.getResource(StandardLocation.SOURCE_PATH, "", rsrc);
                    in.openInputStream().close();
                    processingEnv.getMessager().printMessage(Kind.ERROR,
                            "Cannot generate " + rsrc + " because it already exists in sources: " + in.toUri());
                    return;
                } catch (NullPointerException ex) {
                    // trying to prevent java.lang.NullPointerException
                    // at com.sun.tools.javac.util.DefaultFileManager.getFileForOutput(DefaultFileManager.java:1078)
                    // at com.sun.tools.javac.util.DefaultFileManager.getFileForOutput(DefaultFileManager.java:1054)
                    // at com.sun.tools.javac.processing.JavacFiler.getResource(JavacFiler.java:434)
                    // at org.netbeans.modules.openide.util.AbstractServiceProviderProcessor.register(AbstractServiceProviderProcessor.java:163)
                    // at org.netbeans.modules.openide.util.ServiceProviderProcessor.register(ServiceProviderProcessor.java:99)
                } catch (FileNotFoundException | NoSuchFileException x) {
                    // Good.
                }
                try {
                    FileObject in = filer.getResource(StandardLocation.CLASS_OUTPUT, "", rsrc);
                    InputStream is = in.openInputStream();
                    try {
                        ServiceLoaderLine.parse(new InputStreamReader(is, "UTF-8"), lines); // NOI18N
                    } finally {
                        is.close();
                    }
                } catch (FileNotFoundException | NoSuchFileException x) {
                    // OK, created for the first time
                }
            } catch (IOException x) {
                processingEnv.getMessager().printMessage(Kind.ERROR, x.toString());
                return;
            }
            outputFiles.put(rsrc, lines);
        }
        lines.add(new ServiceLoaderLine(impl, position, supersedes));
    }

    /**
     * @param element a source element
     * @param annotation a type of annotation
     * @return the instance of that annotation on the element, or null if not found
     */
    private AnnotationMirror findAnnotationMirror(Element element, Class<? extends Annotation> annotation) {
        for (AnnotationMirror ann : element.getAnnotationMirrors()) {
            if (processingEnv.getElementUtils().getBinaryName((TypeElement) ann.getAnnotationType().asElement()).
                    contentEquals(annotation.getName())) {
                return ann;
            }
        }
        return null;
    }

    /**
     * @param annotation an annotation instance (null permitted)
     * @param name the name of an attribute of that annotation
     * @return the corresponding value if found
     */
    private AnnotationValue findAnnotationValue(AnnotationMirror annotation, String name) {
        if (annotation != null) {
            for (Map.Entry<? extends ExecutableElement,? extends AnnotationValue> entry : annotation.getElementValues().entrySet()) {
                if (entry.getKey().getSimpleName().contentEquals(name)) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    private boolean verifyServiceProviderSignature(TypeElement clazz, Class<? extends Annotation> annotation) {
        AnnotationMirror ann = findAnnotationMirror(clazz, annotation);
        if (!clazz.getModifiers().contains(Modifier.PUBLIC)) {
            processingEnv.getMessager().printMessage(Kind.ERROR, clazz + " must be public", clazz, ann);
            return false;
        }
        if (clazz.getModifiers().contains(Modifier.ABSTRACT)) {
            processingEnv.getMessager().printMessage(Kind.ERROR, clazz + " must not be abstract", clazz, ann);
            return false;
        }
        if (clazz.getEnclosingElement().getKind() != ElementKind.PACKAGE && !clazz.getModifiers().contains(Modifier.STATIC)) {
            processingEnv.getMessager().printMessage(Kind.ERROR, clazz + " must be static", clazz, ann);
            return false;
        }
        {
            boolean hasDefaultCtor = false;
            for (ExecutableElement constructor : ElementFilter.constructorsIn(clazz.getEnclosedElements())) {
                if (constructor.getModifiers().contains(Modifier.PUBLIC) && constructor.getParameters().isEmpty()) {
                    hasDefaultCtor = true;
                    break;
                }
            }
            if (!hasDefaultCtor) {
                processingEnv.getMessager().printMessage(Kind.ERROR, clazz + " must have a public no-argument constructor", clazz, ann);
                return false;
            }
        }
        return true;
    }

    private void writeServices() {
        for (Map.Entry<Filer,Map<String,SortedSet<ServiceLoaderLine>>> outputFiles : outputFilesByProcessor.entrySet()) {
            Filer filer = outputFiles.getKey();
            for (Map.Entry<String,SortedSet<ServiceLoaderLine>> entry : outputFiles.getValue().entrySet()) {
                try {
                    FileObject out = filer.createResource(StandardLocation.CLASS_OUTPUT, "", entry.getKey(),
                            originatingElementsByProcessor.get(filer).get(entry.getKey()).toArray(new Element[0]));
                    OutputStream os = out.openOutputStream();
                    try {
                        PrintWriter w = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));
                        for (ServiceLoaderLine line : entry.getValue()) {
                            line.write(w);
                        }
                        w.flush();
                        w.close();
                    } finally {
                        os.close();
                    }
                } catch (IOException x) {
                    processingEnv.getMessager().printMessage(Kind.ERROR, "Failed to write to " + entry.getKey() + ": " + x.toString());
                }
            }
        }
    }

    @Deprecated
    protected final void register(TypeElement el, Class<? extends Annotation> annotation,
            TypeMirror type, String path, int position, String[] supersedes) {
        register((Element) el, annotation, type, path, position, supersedes);
    }

    /**
     * If the subclass itself does not define SupportedSourceVersion, assume latest(). If it does
     * (was recommended prior to 8.40), returns the subclass' value.
     * @return max supported source version.
     * @since 8.40
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        SupportedSourceVersion ssv = this.getClass().getAnnotation(SupportedSourceVersion.class);
        SourceVersion sv;
        if (ssv == null) {
            sv = SourceVersion.latest();
        } else
            sv = ssv.value();
        return sv;
    }
}
