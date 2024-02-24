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
package org.netbeans.modules.micronaut.symbol;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.stream.Collectors;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexer;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Pair;
import org.openide.util.WeakListeners;

/**
 *
 * @author Dusan Balek
 */
public final class MicronautSymbolFinder extends EmbeddingIndexer implements PropertyChangeListener {

    public static final String NAME = "mn"; // NOI18N
    public static final int VERSION = 1;
    public static final MicronautSymbolFinder INSTANCE = new MicronautSymbolFinder();
    public static final String[] META_ANNOTATIONS = new String[] {
        "io.micronaut.http.annotation.HttpMethodMapping",
        "io.micronaut.context.annotation.Bean",
        "jakarta.inject.Qualifier",
        "jakarta.inject.Scope"
    };

    private final Map<Project, Boolean> map = new WeakHashMap<>();

    @Override
    protected void index(Indexable indexable, Parser.Result parserResult, Context context) {
        CompilationController cc = CompilationController.get(parserResult);
        if (initialize(cc)) {
            try {
                if (cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED).compareTo(JavaSource.Phase.ELEMENTS_RESOLVED) >= 0) {
                    store(context.getIndexFolder(), indexable.getURL(), indexable.getRelativePath(), scan(cc, false));
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        synchronized (this) {
            map.clear();
        }
    }

    private synchronized boolean initialize(CompilationController cc) {
        Project p = FileOwnerQuery.getOwner(cc.getFileObject());
        if (p == null) {
            return false;
        }
        Boolean ret = map.get(p);
        if (ret == null) {
            ClassPath cp = ClassPath.getClassPath(p.getProjectDirectory(), ClassPath.COMPILE);
            cp.addPropertyChangeListener(WeakListeners.propertyChange(this, cp));
            ret = cp.findResource("io/micronaut/http/annotation/HttpMethodMapping.class") != null;
            map.put(p, ret);
        }
        return ret;
    }

    public static List<SymbolLocation> scan(CompilationController cc, boolean selectEndpointAnnotation) {
        final List<SymbolLocation> ret = new ArrayList<>();
        SourcePositions sp = cc.getTrees().getSourcePositions();
        TreePathScanner<Void, String> scanner = new TreePathScanner<Void, String>() {
            @Override
            public Void visitClass(ClassTree node, String path) {
                TreePath treePath = this.getCurrentPath();
                Element cls = cc.getTrees().getElement(treePath);
                if (cls != null) {
                    Pair<AnnotationMirror, AnnotationMirror> metaAnnotated = isMetaAnnotated(cls);
                    if (metaAnnotated != null) {
                        Element annEl = metaAnnotated.first().getAnnotationType().asElement();
                        if ("io.micronaut.http.annotation.Controller".contentEquals(((TypeElement) annEl).getQualifiedName())) {
                            path = "";
                            for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : metaAnnotated.first().getElementValues().entrySet()) {
                                if ("value".contentEquals(entry.getKey().getSimpleName())) {
                                    path = (String) entry.getValue().getValue();
                                }
                            }
                        }
                        String name = "@+ '" + getBeanName(node.getSimpleName().toString()) + "' (@" + annEl.getSimpleName()
                                + (metaAnnotated.second() != null ? " <: @" + metaAnnotated.second().getAnnotationType().asElement().getSimpleName() : "")
                                + ") " + node.getSimpleName();
                        int[] span = cc.getTreeUtilities().findNameSpan(node);
                        ret.add(new SymbolLocation(name, (int) sp.getStartPosition(treePath.getCompilationUnit(), node), (int) sp.getEndPosition(treePath.getCompilationUnit(), node), span[0], span[1]));
                    }
                }
                return super.visitClass(node, path);
            }

            @Override
            public Void visitMethod(MethodTree node, String path) {
                if (path != null) {
                    TreePath treePath = this.getCurrentPath();
                    MthIterator it = new MthIterator(cc.getTrees().getElement(treePath), cc.getElements(), cc.getTypes());
                    while (it.hasNext()) {
                        ExecutableElement ee = it.next();
                        for (AnnotationMirror ann : ee.getAnnotationMirrors()) {
                            String method = getEndpointMethod((TypeElement) ann.getAnnotationType().asElement());
                            if (method != null) {
                                List<String> ids = new ArrayList<>();
                                Map<? extends ExecutableElement, ? extends AnnotationValue> values = ann.getElementValues();
                                if (values.isEmpty()) {
                                    ids.add("/");
                                } else {
                                    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : values.entrySet()) {
                                        if ("value".contentEquals(entry.getKey().getSimpleName()) || "uri".contentEquals(entry.getKey().getSimpleName())) {
                                            ids.add((String) entry.getValue().getValue());
                                        } else if ("uris".contentEquals(entry.getKey().getSimpleName())) {
                                            for (AnnotationValue av : (List<AnnotationValue>) entry.getValue().getValue()) {
                                                ids.add((String) av.getValue());
                                            }
                                        }
                                    }
                                }
                                for (Object id : ids) {
                                    String name = '@' + path + id + " -- " + method;
                                    int[] span = cc.getTreeUtilities().findNameSpan(node);
                                    if (selectEndpointAnnotation) {
                                        Tree tree = cc.getTrees().getTree(ee, ann);
                                        if (tree != null) {
                                            span = new int[] {(int) sp.getStartPosition(treePath.getCompilationUnit(), tree), (int) sp.getEndPosition(treePath.getCompilationUnit(), tree)};
                                        }
                                    }
                                    ret.add(new SymbolLocation(name, (int) sp.getStartPosition(treePath.getCompilationUnit(), node), (int) sp.getEndPosition(treePath.getCompilationUnit(), node), span[0], span[1]));
                                }
                                return null;
                            }
                        }
                    }
                }
                return null;
            }
        };
        scanner.scan(cc.getCompilationUnit(), null);
        return ret;
    }

    private void store(FileObject indexFolder, URL url, String resourceName, List<SymbolLocation> symbols) {
        File cacheRoot = FileUtil.toFile(indexFolder);
        File output = new File(cacheRoot, resourceName + ".mn"); //NOI18N
        if (symbols.isEmpty()) {
            if (output.exists()) {
                output.delete();
            }
        } else {
            output.getParentFile().mkdirs();
            try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(output), StandardCharsets.UTF_8))) {
                pw.print("url: "); //NOI18N
                pw.println(url.toString());
                for (SymbolLocation symbol : symbols) {
                    pw.print("symbol: ");
                    pw.print(symbol.name);
                    pw.print(':'); //NOI18N
                    pw.print(symbol.selectionStart);
                    pw.print('-'); //NOI18N
                    pw.println(symbol.selectionEnd);
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private static Pair<AnnotationMirror, AnnotationMirror> isMetaAnnotated(Element el) {
        for (AnnotationMirror ann : el.getAnnotationMirrors()) {
            Element annEl = ann.getAnnotationType().asElement();
            Name name = ((TypeElement) annEl).getQualifiedName();
            String annotation = check(name);
            if (annotation != null) {
                return Pair.of(ann, null);
            }
            for (AnnotationMirror metaAnn : annEl.getAnnotationMirrors()) {
                Element metaAnnEl = metaAnn.getAnnotationType().asElement();
                String metaAnnotation = check(((TypeElement) metaAnnEl).getQualifiedName());
                if (metaAnnotation != null) {
                    return Pair.of(ann, metaAnn);
                }
            }
        }
        return null;
    }

    private static String check(Name name) {
        for (String ann : META_ANNOTATIONS) {
            if (ann.contentEquals(name)) {
                return ann;
            }
        }
        return null;
    }

    private static String getEndpointMethod(TypeElement te) {
        for (AnnotationMirror ann : te.getAnnotationMirrors()) {
            Element el = ann.getAnnotationType().asElement();
            if ("io.micronaut.http.annotation.HttpMethodMapping".contentEquals(((TypeElement) el).getQualifiedName())) {
                return te.getSimpleName().toString().toUpperCase();
            }
        }
        return null;
    }

    public static String getBeanName(String typeName) {
        if (typeName.length() > 0 && Character.isUpperCase(typeName.charAt(0))) {
            if (typeName.length() == 1 || !Character.isUpperCase(typeName.charAt(1))) {
                typeName = Character.toLowerCase(typeName.charAt(0)) + typeName.substring(1);
            }
        }
        return typeName;
    }

    @MimeRegistration(mimeType="text/x-java", service=EmbeddingIndexerFactory.class) //NOI18N
    public static class Factory extends EmbeddingIndexerFactory {

        @Override
        public EmbeddingIndexer createIndexer(Indexable indexable, Snapshot snapshot) {
            return INSTANCE;
        }

        @Override
        public void filesDeleted(Iterable<? extends Indexable> deleted, Context context) {
            File cacheRoot = FileUtil.toFile(context.getIndexFolder());
            for (Indexable indexable : deleted) {
                File output = new File(cacheRoot, indexable.getRelativePath() + ".mn"); //NOI18N
                if (output.exists()) {
                    output.delete();
                }
            }
        }

        @Override
        public void filesDirty(Iterable<? extends Indexable> dirty, Context context) {
        }

        @Override
        public String getIndexerName() {
            return NAME;
        }

        @Override
        public int getIndexVersion() {
            return VERSION;
        }
    }

    public static class SymbolLocation {
        private final String name;
        private final int start;
        private final int end;
        private final int selectionStart;
        private final int selectionEnd;

        private SymbolLocation(String name, int start, int end, int selectionStart, int selectionEnd) {
            this.name = name;
            this.start = start;
            this.end = end;
            this.selectionStart = selectionStart;
            this.selectionEnd = selectionEnd;
        }

        public String getName() {
            return name;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        public int getSelectionStart() {
            return selectionStart;
        }

        public int getSelectionEnd() {
            return selectionEnd;
        }
    }

    private static class MthIterator implements Iterator<ExecutableElement> {

        private final ExecutableElement ee;
        private final Elements elements;
        private final Types types;
        private boolean createIt = false;
        private Iterator<ExecutableElement> it = null;

        private MthIterator(Element e, Elements elements, Types types) {
            this.ee = e != null && e.getKind() == ElementKind.METHOD ? (ExecutableElement) e : null;
            this.elements = elements;
            this.types = types;
        }

        @Override
        public boolean hasNext() {
            if (ee == null) {
                return false;
            }
            if (it == null) {
                if (!createIt) {
                    return true;
                }
                List<ExecutableElement> overriden = new ArrayList<>();
                collectOverriden(ee, ee.getEnclosingElement(), overriden);
                it = overriden.iterator();
            }
            return it.hasNext();
        }

        @Override
        public ExecutableElement next() {
            if (it == null) {
                createIt = true;
                return ee;
            }
            return it.next();
        }

        private void collectOverriden(ExecutableElement orig, Element el, List<ExecutableElement> overriden) {
            for (TypeMirror superType : types.directSupertypes(el.asType())) {
                if (superType.getKind() == TypeKind.DECLARED) {
                    Element se = ((DeclaredType) superType).asElement();
                    overriden.addAll(ElementFilter.methodsIn(se.getEnclosedElements()).stream()
                            .filter(me -> {
                                return orig.getSimpleName().contentEquals(me.getSimpleName()) && elements.overrides(orig, me, (TypeElement) el);
                            })
                            .collect(Collectors.toList()));
                    collectOverriden(orig, se, overriden);
                }
            }
        }
    }
}
