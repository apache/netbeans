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
package org.netbeans.modules.htmlui;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.MirroredTypeException;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import org.netbeans.api.htmlui.HTMLComponent;
import org.netbeans.api.htmlui.HTMLDialog;
import org.openide.filesystems.annotations.LayerBuilder;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jaroslav Tulach
 */
@ServiceProvider(service = Processor.class)
public class HTMLDialogProcessor extends AbstractProcessor
implements Comparator<ExecutableElement> {
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> hash = new HashSet<>();
        hash.add(HTMLDialog.class.getCanonicalName());
        hash.add(HTMLComponent.class.getCanonicalName());
        return hash;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }
    
    private Set<Element> annotatedWith(RoundEnvironment re, Class<? extends Annotation> type) {
        Set<Element> collect = new HashSet<>();
        findAllElements(re.getElementsAnnotatedWith(type), collect, type);
        return collect;
    }
    
    
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment re)  {
        Map<String,Set<ExecutableElement>> names = new TreeMap<>();
        for (Element e : annotatedWith(re, HTMLDialog.class)) {
            HTMLDialog reg = e.getAnnotation(HTMLDialog.class);
            if (reg == null || e.getKind() != ElementKind.METHOD) {
                continue;
            }
            ExecutableElement ee = (ExecutableElement) e;
            if (!e.getModifiers().contains(Modifier.STATIC)) {
                error("Method annotated by @HTMLDialog needs to be static", e);
            }
            if (e.getModifiers().contains(Modifier.PRIVATE)) {
                error("Method annotated by @HTMLDialog cannot be private", e);
            }
            if (!ee.getThrownTypes().isEmpty()) {
                error("Method annotated by @HTMLDialog cannot throw exceptions", e);
            }
            
            PackageElement pkg = findPkg(ee);
            
            String fqn = pkg.getQualifiedName() + "." + reg.className();
            
            Set<ExecutableElement> elems = names.get(fqn);
            if (elems == null) {
                elems = new TreeSet<>(this);
                names.put(fqn, elems);
            }
            elems.add(ee);
        }
        for (Element e : annotatedWith(re, HTMLComponent.class)) {
            HTMLComponent reg = e.getAnnotation(HTMLComponent.class);
            if (reg == null || e.getKind() != ElementKind.METHOD) {
                continue;
            }
            ExecutableElement ee = (ExecutableElement) e;
            if (!e.getModifiers().contains(Modifier.STATIC)) {
                error("Method annotated by @HTMLComponent needs to be static", e);
            }
            if (e.getModifiers().contains(Modifier.PRIVATE)) {
                error("Method annotated by @HTMLComponent cannot be private", e);
            }
            if (!ee.getThrownTypes().isEmpty()) {
                error("Method annotated by @HTMLComponent cannot throw exceptions", e);
            }
            
            PackageElement pkg = findPkg(ee);
            
            String fqn = pkg.getQualifiedName() + "." + reg.className();
            
            Set<ExecutableElement> elems = names.get(fqn);
            if (elems == null) {
                elems = new TreeSet<>(this);
                names.put(fqn, elems);
            }
            elems.add(ee);
        }
        
        for (Map.Entry<String, Set<ExecutableElement>> entry : names.entrySet()) {
            String clazzName = entry.getKey();
            Set<ExecutableElement> elems = entry.getValue();
            Element first = elems.iterator().next();
            try {
                JavaFileObject f = processingEnv.getFiler().createSourceFile(
                    clazzName, elems.toArray(new Element[0])
                );
                Writer w = f.openWriter();

                final String[] arr = splitPkg(clazzName, first);
                w.append("package ").append(arr[0]).append(";\n");
                w.append("\n");
                w.append("import org.netbeans.api.htmlui.HTMLDialog.Builder;\n");
                w.append("class ").append(arr[1]).append(" {\n");
                w.append("  private ").append(arr[1]).append("() {\n  }\n");
                w.append("\n");
                
                for (ExecutableElement ee : elems) {
                    HTMLDialog reg = ee.getAnnotation(HTMLDialog.class);
                    HTMLComponent comp = ee.getAnnotation(HTMLComponent.class);
                    if (reg != null) {
                        String url = findURL(reg.url(), ee);
                        if (url == null) {
                            continue;
                        }
                        generateDialog(w, ee, url, reg.techIds());
                    }
                    if (comp != null) {
                        String url = findURL(comp.url(), ee);
                        if (url == null) {
                            continue;
                        }
                        String t;
                        try {
                            t = comp.type().getName();
                        } catch (MirroredTypeException ex) {
                            t = ex.getTypeMirror().toString();
                        }
                        if (
                            !t.equals("javafx.scene.Node") &&
                            !t.equals("javax.swing.JComponent")
                        ) {
                            error("type() can be either Node.class or JComponent.class", ee);
                        }
                        generateComponent(w, ee, t, url, comp.techIds());
                    }
                }
                
                w.append("}\n");
                w.close();
                
            } catch (IOException ex) {
                error("Cannot create " + clazzName, first);
            }
        }
        
        return true;
    }

    private String findURL(final String relativeURL, ExecutableElement ee) {
        String url;
        try {
            URL u = new URL(relativeURL);
            url = u.toExternalForm();
        } catch (MalformedURLException ex2) {
            try {
                final String res = LayerBuilder.absolutizeResource(ee, relativeURL);
                validateResource(res, ee, null, null, false);
                url = "nbresloc:/" + res;
            } catch (LayerGenerationException ex) {
                error("Cannot find resource " + relativeURL, ee);
                url = null;
            }
        }
        return url;
    }

    private void generateDialog(Writer w, ExecutableElement ee, String url, String[] techIds) throws IOException {
        w.append("  public static String ").append(ee.getSimpleName());
        w.append("(");
        String sep = "";
        for (VariableElement v : ee.getParameters()) {
            w.append(sep);
            w.append("final ").append(v.asType().toString()).append(" ").append(v.getSimpleName());
            sep = ", ";
        }
        
        w.append(") {\n");
        w.append("    return Builder.newDialog(\"").append(url).append("\").\n");
        generateTechIds(w, techIds);
        w.append("      loadFinished(new Runnable() {\n");
        w.append("        public void run() {\n");
        w.append("          ").append(ee.getEnclosingElement().getSimpleName())
                .append(".").append(ee.getSimpleName()).append("(");
        sep = "";
        for (VariableElement v : ee.getParameters()) {
            w.append(sep);
            w.append(v.getSimpleName());
            sep = ", ";
        }
        w.append(");\n");
        w.append("        }\n");
        w.append("      }).\n");
        w.append("      showAndWait();\n");
        w.append("  }\n");
    }
    
    private void generateComponent(
        Writer w, ExecutableElement ee, String type, String url, String[] techIds
    ) throws IOException {
        w.append("  public static ").append(type).append(" ").append(ee.getSimpleName());
        w.append("(");
        String sep = "";
        for (VariableElement v : ee.getParameters()) {
            w.append(sep);
            w.append("final ").append(v.asType().toString()).append(" ").append(v.getSimpleName());
            sep = ", ";
        }
        
        w.append(") {\n");
        w.append("    return Builder.newDialog(\"").append(url).append("\").\n");
        generateTechIds(w, techIds);
        w.append("      loadFinished(new Runnable() {\n");
        w.append("        public void run() {\n");
        w.append("          ").append(ee.getEnclosingElement().getSimpleName())
                .append(".").append(ee.getSimpleName()).append("(");
        sep = "";
        for (VariableElement v : ee.getParameters()) {
            w.append(sep);
            w.append(v.getSimpleName());
            sep = ", ";
        }
        w.append(");\n");
        w.append("        }\n");
        w.append("      }).\n");
        w.append("      component(").append(type).append(".class);\n");
        w.append("  }\n");
    }

    private void error(final String msg, Element e) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg, e);
    }
    
    private static PackageElement findPkg(Element e) {
        while (e.getKind() != ElementKind.PACKAGE) {
            e = e.getEnclosingElement();
        }
        return (PackageElement)e;
    }
    
    private String[] splitPkg(String s, Element e) {
        int last = s.lastIndexOf('.');
        if (last == -1) {
            error("Cannot generate " + s + " into default package!", e);
            return new String[] { "x", s };
        } else {
            return new String[] { s.substring(0, last), s.substring(last + 1) };
        }
    }

    @Override
    public int compare(ExecutableElement o1, ExecutableElement o2) {
        if (o1 == o2) {
            return 0;
        }
        int names = o1.getSimpleName().toString().compareTo(
            o2.getSimpleName().toString()
        );
        if (names != 0) {
            return names;
        }
        names = o1.getEnclosingElement().getSimpleName().toString().compareTo(
            o2.getEnclosingElement().getSimpleName().toString()
        );
        if (names != 0) {
            return names;
        }
        int id1 = System.identityHashCode(o1);
        int id2 = System.identityHashCode(o2);
        if (id1 == id2) {
            throw new IllegalStateException("Cannot order " + o1 + " and " + o2);
        }
        return id1 - id2;
    }
    
    FileObject validateResource(String resource, Element originatingElement, Annotation annotation, String annotationMethod, boolean searchClasspath) throws LayerGenerationException {
        if (resource.startsWith("/")) {
            throw new LayerGenerationException("do not use leading slashes on resource paths", originatingElement, processingEnv, annotation, annotationMethod);
        }
        if (searchClasspath) {
            for (JavaFileManager.Location loc : new JavaFileManager.Location[]{StandardLocation.SOURCE_PATH, /* #181355 */ StandardLocation.CLASS_OUTPUT, StandardLocation.CLASS_PATH, StandardLocation.PLATFORM_CLASS_PATH}) {
                try {
                    FileObject f = processingEnv.getFiler().getResource(loc, "", resource);
                    if (loc.isOutputLocation()) {
                        f.openInputStream().close();
                    }
                    return f;
                } catch (IOException ex) {
                    continue;
                }
            }
            throw new LayerGenerationException("Cannot find resource " + resource, originatingElement, processingEnv, annotation, annotationMethod);
        } else {
            try {
                try {
                    FileObject f = processingEnv.getFiler().getResource(StandardLocation.SOURCE_PATH, "", resource);
                    f.openInputStream().close();
                    return f;
                } catch (FileNotFoundException x) {
                    try {
                        FileObject f = processingEnv.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "", resource);
                        f.openInputStream().close();
                        return f;
                    } catch (IOException x2) {
                        throw x;
                    }
                }
            } catch (IOException x) {
                throw new LayerGenerationException("Cannot find resource " + resource, originatingElement, processingEnv, annotation, annotationMethod);
            }
        }
    }

    private static void findAllElements(
            Set<? extends Element> scan, Set<Element> found,
            Class<? extends Annotation> type
    ) {
        for (Element e : scan) {
            PackageElement pkg = findPkg(e);
            if (found.add(pkg)) {
                searchSubTree(pkg, found, type);
            }
        }
    }

    private static void searchSubTree(
            Element e,
            Set<Element> found,
            Class<? extends Annotation> type
    ) {
        if (e.getAnnotation(type) != null) {
            found.add(e);
        }
        for (Element ee : e.getEnclosedElements()) {
            searchSubTree(ee, found, type);
        }
    }

    private void generateTechIds(Writer w, String[] techIds) throws IOException {
        if (techIds.length == 0) {
            return;
        }
        w.append("addTechIds(");
        String sep = "";
        for (String id : techIds) {
            w.append(sep);
            w.append('"');
            w.append(id);
            w.append('"');
            sep = ", ";
        }
        w.append(").\n");
    }
}
