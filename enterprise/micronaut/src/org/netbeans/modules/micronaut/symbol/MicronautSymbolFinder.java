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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Collectors;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexer;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
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

    private static final MicronautSymbolFinder INSTANCE = new MicronautSymbolFinder();
    private static final String[] META_ANNOTATIONS = new String[] {
        "io.micronaut.http.annotation.HttpMethodMapping",
        "io.micronaut.context.annotation.Bean",
        "jakarta.inject.Qualifier",
        "jakarta.inject.Scope"
    };
    private static final String CONTROLLER_ANNOTATION = "io.micronaut.http.annotation.Controller";
    private static final String HTTP_METHOD_MAPPING_ANNOTATION = "io.micronaut.http.annotation.HttpMethodMapping";
    private static final String MANAGEMENT_ENDPOINT_ANNOTATION = "io.micronaut.management.endpoint.annotation.Endpoint";
    private static final String MANAGEMENT_READ_ANNOTATION = "io.micronaut.management.endpoint.annotation.Read";
    private static final String MANAGEMENT_WRITE_ANNOTATION = "io.micronaut.management.endpoint.annotation.Write";
    private static final String MANAGEMENT_DELETE_ANNOTATION = "io.micronaut.management.endpoint.annotation.Delete";
    private static final String MANAGEMENT_SELECTOR_ANNOTATION = "io.micronaut.management.endpoint.annotation.Selector";

    private final Map<FileObject, Boolean> map = new WeakHashMap<>();
    private final Map<ClasspathInfo, List<ClassSymbolLocation>> cache = new WeakHashMap<>();

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
        for (SourceGroup sg : ProjectUtils.getSources(p).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
            if (sg.contains(cc.getFileObject())) {
                FileObject root = sg.getRootFolder();
                Boolean ret = map.get(root);
                if (ret == null) {
                    ClassPath cp = ClassPath.getClassPath(root, ClassPath.COMPILE);
                    cp.addPropertyChangeListener(WeakListeners.propertyChange(this, cp));
                    ret = cp.findResource("io/micronaut/http/annotation/HttpMethodMapping.class") != null
                            || cp.findResource("io/micronaut/management/endpoint/annotation/Endpoint.class") != null;
                    map.put(root, ret);
                }
                return ret;
            }
        }
        return false;
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
                        path = getPath(metaAnnotated.first());
                        Element annEl = metaAnnotated.first().getAnnotationType().asElement();
                        String name = "@+ '" + getBeanName(node.getSimpleName().toString()) + "' (@" + annEl.getSimpleName()
                                + (metaAnnotated.second() != null ? " <: @" + metaAnnotated.second().getAnnotationType().asElement().getSimpleName() : "")
                                + ") " + node.getSimpleName();
                        int[] span = cc.getTreeUtilities().findNameSpan(node);
                        ret.add(new SymbolLocation(name, (int) sp.getStartPosition(treePath.getCompilationUnit(), node), (int) sp.getEndPosition(treePath.getCompilationUnit(), node), span[0], span[1]));
                    } else {
                        path = getPath((TypeElement) cls);
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
                                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : ann.getElementValues().entrySet()) {
                                    if ("value".contentEquals(entry.getKey().getSimpleName()) || "uri".contentEquals(entry.getKey().getSimpleName())) {
                                        ids.add((String) entry.getValue().getValue());
                                    } else if ("uris".contentEquals(entry.getKey().getSimpleName())) {
                                        for (AnnotationValue av : (List<AnnotationValue>) entry.getValue().getValue()) {
                                            ids.add((String) av.getValue());
                                        }
                                    }
                                }
                                if (ids.isEmpty()) {
                                    ids.add(getId(ee));
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

    public static String getEndpointMethod(TypeElement te) {
        switch (te.getQualifiedName().toString()) {
            case MANAGEMENT_READ_ANNOTATION:
                return "GET";
            case MANAGEMENT_WRITE_ANNOTATION:
                return "POST";
            case MANAGEMENT_DELETE_ANNOTATION:
                return "DELETE";
            default:
                for (AnnotationMirror ann : te.getAnnotationMirrors()) {
                    Element el = ann.getAnnotationType().asElement();
                    if (HTTP_METHOD_MAPPING_ANNOTATION.contentEquals(((TypeElement) el).getQualifiedName())) {
                        return te.getSimpleName().toString().toUpperCase();
                    }
                }
        }
        return null;
    }

    private static String getBeanName(String typeName) {
        if (typeName.length() > 0 && Character.isUpperCase(typeName.charAt(0))) {
            if (typeName.length() == 1 || !Character.isUpperCase(typeName.charAt(1))) {
                typeName = Character.toLowerCase(typeName.charAt(0)) + typeName.substring(1);
            }
        }
        return typeName;
    }

    private static String getPath(AnnotationMirror ann) {
        String path = null;
        Element annEl = ann.getAnnotationType().asElement();
        switch (((TypeElement) annEl).getQualifiedName().toString()) {
            case CONTROLLER_ANNOTATION:
                path = "/";
                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : ann.getElementValues().entrySet()) {
                    if ("value".contentEquals(entry.getKey().getSimpleName())) {
                        path = (String) entry.getValue().getValue();
                    }
                }
                break;
            case MANAGEMENT_ENDPOINT_ANNOTATION:
                path = "/";
                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : ann.getElementValues().entrySet()) {
                    if ("value".contentEquals(entry.getKey().getSimpleName()) || "id".contentEquals(entry.getKey().getSimpleName())) {
                        path = (String) entry.getValue().getValue();
                    }
                }
                break;
        }
        if (path != null && !path.startsWith("/")) {
            path = "/" + path;
        }
        return path;
    }

    private static String getPath(TypeElement te) {
        for (AnnotationMirror ann : te.getAnnotationMirrors()) {
            String path = getPath(ann);
            if (path != null) {
                return path;
            }
        }
        return null;
    }

    private static String getId(ExecutableElement ee) {
        StringBuilder sb = new StringBuilder();
        for (VariableElement ve : ee.getParameters()) {
            for (AnnotationMirror ann : ve.getAnnotationMirrors()) {
                Element el = ann.getAnnotationType().asElement();
                if (MANAGEMENT_SELECTOR_ANNOTATION.contentEquals(((TypeElement) el).getQualifiedName())) {
                    sb.append("/{").append(ve.getSimpleName()).append('}');
                }
            }
        }
        return sb.toString();
    }

    private static String[] getSignatures(Element e) {
        switch (e.getKind()) {
            case ANNOTATION_TYPE:
            case CLASS:
            case ENUM:
            case INTERFACE:
            case RECORD:
                return new String[] {encodeClassNameOrArray((TypeElement) e)};
            case METHOD:
                return createMethodDescriptor((ExecutableElement) e);
        }
        return new String[0];
    }

    private static String[] createMethodDescriptor(ExecutableElement ee) {
        String[] result = new String[3];
        Element enclosingType = ee.getEnclosingElement();
        if (enclosingType != null && enclosingType.asType().getKind() == TypeKind.NONE) {
            result[0] = "";
        } else {
	    assert enclosingType instanceof TypeElement : enclosingType == null ? "null" : enclosingType.toString() + "(" + enclosingType.getKind()+")"; //NOI18N
            result[0] = encodeClassNameOrArray ((TypeElement)enclosingType);
        }
        StringBuilder retType = new StringBuilder ();
        result[1] = ee.getSimpleName().toString();
        if (ee.asType().getKind() == TypeKind.EXECUTABLE) {
            encodeType(ee.getReturnType(), retType);
        }
        StringBuilder sb = new StringBuilder ();
        sb.append('(');
        for (VariableElement pd : ee.getParameters()) {
            encodeType(pd.asType(),sb);
        }
        sb.append(')');
        sb.append(retType);
        result[2] = sb.toString();
        return result;
    }

    private static String encodeClassNameOrArray(TypeElement td) {
        CharSequence qname = td.getQualifiedName();
        TypeMirror enclosingType = td.getEnclosingElement().asType();
        if (qname != null && enclosingType != null && enclosingType.getKind() == TypeKind.NONE && "Array".equals(qname.toString())) {
            return "[";
        } else {
            return encodeClassName(td);
        }
    }

    private static String encodeClassName(TypeElement td) {
        StringBuilder sb = new StringBuilder ();
        encodeClassName(td, sb, '.');
        return sb.toString();
    }

    private static void encodeType(TypeMirror type, StringBuilder sb) {
	switch (type.getKind()) {
	    case VOID:
		sb.append('V');
		break;
	    case BOOLEAN:
		sb.append('Z');
		break;
	    case BYTE:
		sb.append('B');
		break;
	    case SHORT:
		sb.append('S');
		break;
	    case INT:
		sb.append('I');
		break;
	    case LONG:
		sb.append('J');
		break;
	    case CHAR:
		sb.append('C');
		break;
	    case FLOAT:
		sb.append('F');
		break;
	    case DOUBLE:
		sb.append('D');
		break;
	    case ARRAY:
		sb.append('[');
		encodeType(((ArrayType)type).getComponentType(), sb);
		break;
	    case DECLARED:
            {
                sb.append('L');
                TypeElement te = (TypeElement) ((DeclaredType)type).asElement();
                encodeClassName(te, sb, '/');
                sb.append(';');
                break;
            }
	    case TYPEVAR:
            {
		TypeVariable tr = (TypeVariable) type;
		TypeMirror upperBound = tr.getUpperBound();
		if (upperBound.getKind() == TypeKind.NULL) {
		    sb.append("Ljava/lang/Object;");
		}
		else {
		    encodeType(upperBound, sb);
		}
		break;
            }
            case INTERSECTION:
            {
                encodeType(((IntersectionType) type).getBounds().get(0), sb);
                break;
            }
            case ERROR:
            {
                TypeElement te = (TypeElement) ((DeclaredType)type).asElement();
                if (te != null) {
                    sb.append('L');
                    encodeClassName(te, sb,'/');
                    sb.append(';');
                    break;
                }
            }
	    default:
		throw new IllegalArgumentException (String.format("Unsupported type: %s, kind: %s", type, type.getKind()));
	}
    }

    private static void encodeClassName (TypeElement te, final StringBuilder sb, final char separator) {
        final char[] nameChars = flatName(te).toCharArray();
        int charLength = nameChars.length;
        if (separator != '.') {
            for (int i = 0; i < charLength; i++) {
                if (nameChars[i] == '.') {
                    nameChars[i] = separator;
                }
            }
        }
        sb.append(nameChars, 0, charLength);
    }

    private static String flatName(TypeElement te) {
        Element owner = te.getEnclosingElement();
        if (owner.getKind().isClass() || owner.getKind().isInterface()) {
            return flatName((TypeElement) owner) + '$' + te.getSimpleName();
        }
        return te.getQualifiedName().toString();
    }

    public static List<ClassSymbolLocation> getSymbolsFromDependencies(ClasspathInfo info, String textForQuery) {
        List<ClassSymbolLocation> cached = INSTANCE.cache.get(info);
        if (cached == null) {
            List<ClassSymbolLocation> ret = new ArrayList<>();
            ClassIndex ci = info.getClassIndex();
            Set<ElementHandle<TypeElement>> beanHandles = new HashSet<>();
            Set<ElementHandle<TypeElement>> checked = new HashSet<>();
            LinkedList<ElementHandle<TypeElement>> queue = new LinkedList<>();
            for (String ann : META_ANNOTATIONS) {
                queue.add(ElementHandle.createTypeElementHandle(ElementKind.ANNOTATION_TYPE, ann));
            }
            while (!queue.isEmpty()) {
                ElementHandle<TypeElement> eh = queue.removeFirst();
                if (checked.add(eh)) {
                    Set<ElementHandle<TypeElement>> elems = ci.getElements(eh, Set.of(ClassIndex.SearchKind.TYPE_REFERENCES), Set.of(ClassIndex.SearchScope.DEPENDENCIES));
                    for (ElementHandle<TypeElement> elem : elems) {
                        if (elem.getKind() == ElementKind.ANNOTATION_TYPE) {
                            queue.add(elem);
                        } else {
                            beanHandles.add(elem);
                        }
                    }
                }
            }
            ElementHandle<TypeElement> eh = ElementHandle.createTypeElementHandle(ElementKind.ANNOTATION_TYPE, MANAGEMENT_ENDPOINT_ANNOTATION);
            Set<ElementHandle<TypeElement>> endpointHandles = ci.getElements(eh, Set.of(ClassIndex.SearchKind.TYPE_REFERENCES), Set.of(ClassIndex.SearchScope.DEPENDENCIES));
            if (!beanHandles.isEmpty() || !endpointHandles.isEmpty()) {
                JavaSource js = JavaSource.create(info);
                if (js != null) {
                    try {
                        js.runUserActionTask(cc -> {
                            cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                            Elements elements = cc.getElements();
                            for (ElementHandle<TypeElement> beanHandle : beanHandles) {
                                TypeElement symbol = beanHandle.resolve(cc);
                                if (symbol != null) {
                                    Pair<AnnotationMirror, AnnotationMirror> metaAnnotated = isMetaAnnotated(symbol);
                                    if (metaAnnotated != null) {
                                        JavaFileObject jfo = elements.getFileObjectOf(symbol);
                                        FileObject fo = jfo != null && jfo.getName().endsWith(".class") ? URLMapper.findFileObject(jfo.toUri().toURL()) : null;
                                        if (fo != null) {
                                            Element annEl = metaAnnotated.first().getAnnotationType().asElement();
                                            String name = "@+ '" + getBeanName(symbol.getSimpleName().toString()) + "' (@" + annEl.getSimpleName()
                                                + (metaAnnotated.second() != null ? " <: @" + metaAnnotated.second().getAnnotationType().asElement().getSimpleName() : "")
                                                + ") " + symbol.getSimpleName();
                                            ret.add(new ClassSymbolLocation(name, fo, symbol.getKind().name(), getSignatures(symbol)));
                                            if (CONTROLLER_ANNOTATION.contentEquals(((TypeElement) annEl).getQualifiedName())) {
                                                String path = getPath(metaAnnotated.first());
                                                if (path != null) {
                                                    addEndpointSymbols(cc, fo, symbol, path, ret);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            for (ElementHandle<TypeElement> endpointHandle : endpointHandles) {
                                TypeElement symbol = endpointHandle.resolve(cc);
                                String path = symbol != null ? getPath(symbol) : null;
                                if (path != null) {
                                    JavaFileObject jfo = elements.getFileObjectOf(symbol);
                                    FileObject fo = jfo != null && jfo.getName().endsWith(".class") ? URLMapper.findFileObject(jfo.toUri().toURL()) : null;
                                    if (fo != null) {
                                        addEndpointSymbols(cc, fo, symbol, path, ret);
                                    }
                                }
                            }
                        }, true);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
            cached = ret;
            synchronized (INSTANCE.cache) {
                INSTANCE.cache.clear();
                INSTANCE.cache.put(info, ret);
            }
        }
        return cached.stream().filter(s -> s.getName().startsWith(textForQuery)).collect(Collectors.toList());
    }

    private static void addEndpointSymbols(CompilationController cc, FileObject fo, TypeElement symbol, String path, List<ClassSymbolLocation> ret) {
        for (ExecutableElement mth : ElementFilter.methodsIn(symbol.getEnclosedElements())) {
            MthIterator it = new MthIterator(mth, cc.getElements(), cc.getTypes());
            while (it.hasNext()) {
                ExecutableElement ee = it.next();
                for (AnnotationMirror ann : ee.getAnnotationMirrors()) {
                    String method = getEndpointMethod((TypeElement) ann.getAnnotationType().asElement());
                    if (method != null) {
                        List<String> ids = new ArrayList<>();
                        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : ann.getElementValues().entrySet()) {
                            if ("value".contentEquals(entry.getKey().getSimpleName()) || "uri".contentEquals(entry.getKey().getSimpleName())) {
                                ids.add((String) entry.getValue().getValue());
                            } else if ("uris".contentEquals(entry.getKey().getSimpleName())) {
                                for (AnnotationValue av : (List<AnnotationValue>) entry.getValue().getValue()) {
                                    ids.add((String) av.getValue());
                                }
                            }
                        }
                        if (ids.isEmpty()) {
                            ids.add(getId(ee));
                        }
                        for (String id : ids) {
                            String name = '@' + path + id + " -- " + method;
                            ret.add(new ClassSymbolLocation(name, fo, mth.getKind().name(), getSignatures(mth)));
                        }
                    }
                }
            }
        }
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

    public static class ClassSymbolLocation {
        private final String name;
        private final FileObject file;
        private final String kind;
        private final String[] signatures;

        private ClassSymbolLocation(String name, FileObject file, String kind, String[] signatures) {
            this.name = name;
            this.file = file;
            this.kind = kind;
            this.signatures = signatures;
        }

        public FileObject getFile() {
            return file;
        }

        public String getName() {
            return name;
        }

        public String getKind() {
            return kind;
        }

        public String[] getSignatures() {
            return signatures;
        }
    }

    public static class MthIterator implements Iterator<ExecutableElement> {

        private final ExecutableElement ee;
        private final Elements elements;
        private final Types types;
        private boolean createIt = false;
        private Iterator<ExecutableElement> it = null;

        public MthIterator(Element e, Elements elements, Types types) {
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
