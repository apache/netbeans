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
package org.netbeans.modules.java.source.annotations;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.Trees;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.util.Elements;
import javax.tools.JavaFileObject;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.java.source.ClasspathInfo.PathKind;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.CompilationInfo.CacheClearPolicy;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.modules.Places;
import org.openide.util.*;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class AugmentedAnnotations {

    private static final Logger LOG = Logger.getLogger(AugmentedAnnotations.class.getName()); //-J-Dorg.netbeans.modules.java.source.annotations.AugmentedAnnotations.level=0
    public static final String NS = "http://www.netbeans.org/ns/external-annotations-java/1";

    private static byte[] augmentedAnnotationsOverride = null;

    //package->encoded element+DOM
    private static final Map<FileObject, Reference<ParsedAnnotationsXML>> hardCodedAnnotations2DOMCache = new WeakHashMap<>();
    //root->encoded element+DOM
    private static final Map<FileObject, Reference<ParsedAnnotationsXML>> userAnnotations2DOMCache = new WeakHashMap<>();

    public static List<? extends AnnotationMirror> getAugmentedAnnotationMirrors(CompilationInfo info, Element e) {
        ParsedAnnotationsXML parsedAnnotations = findParsedAnnotations(info, e);

        if (parsedAnnotations == null || parsedAnnotations.encodedElement2DOMElement.isEmpty()) {
            return e.getAnnotationMirrors();
        }

        String serializedElement = serialize(e);
        org.w3c.dom.Element externalAnnotation = parsedAnnotations.encodedElement2DOMElement.get(serializedElement);

        if (externalAnnotation == null) return e.getAnnotationMirrors();

        List<AnnotationMirror> augmented = new ArrayList<AnnotationMirror>(e.getAnnotationMirrors());

        augmented.addAll(mirror(info, externalAnnotation));

        return augmented;
    }

    public static boolean attachAnnotation(CompilationInfo info, Element e, String annotation) {
        FileObject root = binaryRootFor(info, e);

        if (root == null) return false;

        ParsedAnnotationsXML parsedAnnotations = findAugmentElementAnnotationsRoot(info, root);
        org.w3c.dom.Element annotations = parsedAnnotations.root;
        String serialized = serialize(e);
        org.w3c.dom.Element elementsElement;
        Document document;

        document = parsedAnnotations.root.getOwnerDocument();
        elementsElement = parsedAnnotations.encodedElement2DOMElement.computeIfAbsent(serialized, s -> {
            org.w3c.dom.Element nue = document.createElementNS(NS, "item");
            nue.setAttribute("name", serialized);
            annotations.appendChild(nue);
            return nue;
        });

        StatementTree st = info.getTreeUtilities().parseStatement("{ " + annotation + " int i; }", new SourcePositions[1]);

        if (st.getKind() != Kind.BLOCK) {
            //error
            return false;
        }

        BlockTree bt = (BlockTree) st;

        if (bt.getStatements().size() != 1) {
            //error
            return false;
        }

        st = bt.getStatements().get(0);

        if (st.getKind() != Kind.VARIABLE) {
            //error
            return false;
        }

        for (AnnotationTree at : ((VariableTree) st).getModifiers().getAnnotations()) {
            org.w3c.dom.Element annot = document.createElement("annotation");

            annot.setAttribute("name", at.getAnnotationType().toString());
            for (ExpressionTree attribute : at.getArguments()) {
                String attributeName;
                ExpressionTree attributeValue;
                if (attribute.getKind() == Kind.ASSIGNMENT) {
                    AssignmentTree assign = (AssignmentTree) attribute;
                    attributeName = assign.getVariable().toString();
                    attributeValue = assign.getExpression();
                } else {
                    attributeName = "value";
                    attributeValue = attribute;
                }
                AttributeDescription desc = AttributeDescription.create(attributeValue);

                org.w3c.dom.Element val = document.createElement("val");

                val.setAttribute("name", attributeName);
                val.setAttribute("val", desc.toString());

                annot.appendChild(val);
            }

            elementsElement.appendChild(annot);
        }

        writeAugmentElementAnnotationsRoot(info, root, annotations);

        return true;
    }

    private static ParsedAnnotationsXML findAugmentElementAnnotationsRoot(CompilationInfo info, FileObject elementRoot) {
        Reference<ParsedAnnotationsXML> cachedRef = userAnnotations2DOMCache.get(elementRoot);
        ParsedAnnotationsXML cached = cachedRef != null ? cachedRef.get() : null;
        if (cached != null) {
            return cached;
        }
        Project prj = FileOwnerQuery.getOwner(elementRoot);
        org.w3c.dom.Element parsed = null;
        if (prj != null) {
            parsed = ProjectUtils.getAuxiliaryConfiguration(prj).getConfigurationFragment("annotations", AugmentedAnnotations.NS, true);
        }
        if (parsed == null) {
            try {
                byte[] annotations = (byte[]) getAugmentedAnnotations(elementRoot);
                if (annotations != null) {
                    InputSource input = new InputSource(new ByteArrayInputStream(annotations));
                    parsed = XMLUtil.parse(input, false, true, /*XXX*/null, null).getDocumentElement();
                }
            } catch (IOException | SAXException ex) {
                LOG.log(Level.WARNING, null, ex);
            }
        }
        if (parsed == null) {
            parsed = XMLUtil.createDocument("annotations", NS, null, null).getDocumentElement();
        }
        ParsedAnnotationsXML result = new ParsedAnnotationsXML(parsed);
        userAnnotations2DOMCache.put(elementRoot, new SoftReference<>(result));
        return result;
    }

    private static void writeAugmentElementAnnotationsRoot(CompilationInfo info, FileObject elementRoot, org.w3c.dom.Element annotations) {
        Project prj = FileOwnerQuery.getOwner(elementRoot);
        if (prj != null) {
            ProjectUtils.getAuxiliaryConfiguration(prj).putConfigurationFragment(annotations, true);
            return ;
        }
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XMLUtil.write(annotations.getOwnerDocument(), out, "UTF-8");
            setAugmentedAnnotations(elementRoot, out.toByteArray());
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    static byte[] getAugmentedAnnotations(FileObject elementRoot) throws IOException {
        if (augmentedAnnotationsOverride != null) {
            return augmentedAnnotationsOverride;
        }
        return (byte[]) augmentedDataHolder().getAttribute(AugmentedAnnotations.class.getName() + "-" + elementRoot.toURI());
    }

    static void setAugmentedAnnotations(FileObject elementRoot, byte[] data) throws IOException {
        augmentedDataHolder().setAttribute(AugmentedAnnotations.class.getName() + "-" + elementRoot.toURI(), data);
        userAnnotations2DOMCache.remove(elementRoot);
    }

    public static void setAugmentedAnnotationsForTests(String annotations) {
        augmentedAnnotationsOverride = annotations.getBytes(StandardCharsets.UTF_8);
    }

    private static FileObject augmentedDataHolder() {
        File c = Places.getCacheSubfile("var/cache/javaAugmentedAnnotations");
        if (!c.exists()) {
            try {
                return FileUtil.createData(c);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return FileUtil.toFileObject(c);
    }

    private static class AttributeDescription {
        enum Kind {
            STRING;
        }
        final Kind kind;
        final Object value;

        public AttributeDescription(Kind kind, Object value) {
            this.kind = kind;
            this.value = value;
        }

        public static AttributeDescription create(ExpressionTree from) {
            switch (from.getKind()) {
                case STRING_LITERAL: return new AttributeDescription(Kind.STRING, ((LiteralTree) from).getValue());
                default: throw new IllegalArgumentException("Unknown expression: " + from.getKind());
            }
        }

        @Override
        public String toString() {
            switch (kind) {
                case STRING: return String.valueOf(value);
                default: throw new IllegalStateException();
            }
        }
    }

    private static @CheckForNull ParsedAnnotationsXML findParsedAnnotations(CompilationInfo info, Element e) {
        String packageName = info.getElements().getPackageOf(e).getQualifiedName().toString();

        FileObject data = FileUtil.getConfigFile("java/annotations/external/" + packageName.replace('.', '/') + "/annotations.xml");

        if (data != null) {
            Reference<ParsedAnnotationsXML> cachedDOMRef = hardCodedAnnotations2DOMCache.get(data);
            ParsedAnnotationsXML cachedDOM = cachedDOMRef != null ? cachedDOMRef.get() : null;

            if (cachedDOM == null) {
                try (InputStream in = data.getInputStream()) {
                    cachedDOM = new ParsedAnnotationsXML(XMLUtil.parse(new InputSource(in), false, false, null, null).getDocumentElement());
                    hardCodedAnnotations2DOMCache.put(data, new SoftReference<ParsedAnnotationsXML>(cachedDOM)); //TODO: clearing reference?
                } catch (IOException | SAXException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

            return cachedDOM;
        }

        FileObject binaryRoot = binaryRootFor(info, e);

        if (binaryRoot == null) {
            return null;
        }

        return findAugmentElementAnnotationsRoot(info, binaryRoot);
    }


    private static final class ParsedAnnotationsXML {
        private static final ParsedAnnotationsXML EMPTY = new ParsedAnnotationsXML();
        private final org.w3c.dom.Element root;
        private final Map<String, org.w3c.dom.Element> encodedElement2DOMElement;
        public ParsedAnnotationsXML() {
            this.root = null;
            this.encodedElement2DOMElement = new HashMap<String, org.w3c.dom.Element>();
        }
        public ParsedAnnotationsXML(org.w3c.dom.Element root) {
            this.root = root;
            this.encodedElement2DOMElement = new HashMap<String, org.w3c.dom.Element>();
            NodeList itemsList = root.getChildNodes();

            for (int i = 0; i < itemsList.getLength(); i++) {
                Node current = itemsList.item(i);

                if (!(current instanceof org.w3c.dom.Element)) continue;

                org.w3c.dom.Element currentElement = (org.w3c.dom.Element) current;

                this.encodedElement2DOMElement.put(currentElement.getAttribute("name"), currentElement);
            }
        }
    }

    private static String serialize(Element e) {
        switch (e.getKind()) {
            case PARAMETER: {
                ExecutableElement method = (ExecutableElement) e.getEnclosingElement();

                return serialize(method) + " " + method.getParameters().indexOf(e);
            }
            case CONSTRUCTOR:
            case METHOD:
                ExecutableElement method = (ExecutableElement) e;
                StringBuilder result = new StringBuilder();

                result.append(serialize(e.getEnclosingElement()));
                result.append(" ");
                if (e.getKind() == ElementKind.METHOD) {
                    result.append(method.getReturnType().toString());
                    result.append(" ");
                    result.append(method.getSimpleName().toString());
                } else {
                    result.append(method.getEnclosingElement().getSimpleName().toString());
                }
                result.append("(");
                for (Iterator<? extends VariableElement> it = method.getParameters().iterator(); it.hasNext();) {
                    VariableElement param = it.next();
                    if (it.hasNext() || !method.isVarArgs()) {
                        result.append(param.asType().toString());
                    } else {
                        result.append(((ArrayType) param.asType()).getComponentType().toString());
                        result.append("...");
                    }
                    if (it.hasNext()) {
                        result.append(", ");
                    }
                }
                result.append(")");
                return result.toString();
            case ANNOTATION_TYPE: case CLASS:
            case ENUM: case INTERFACE:
                return ((TypeElement) e).getQualifiedName().toString();
            default:
                return "";
        }
    }

    private static List<AnnotationMirror> mirror(CompilationInfo info, org.w3c.dom.Element el) {
        NodeList annotationDOM = el.getChildNodes();
        List<AnnotationMirror> result = new ArrayList<AnnotationMirror>();

        for (int i = 0; i < annotationDOM.getLength(); i++) {
            Node ann = annotationDOM.item(i);

            if (ann instanceof org.w3c.dom.Element && "annotation".equals(((org.w3c.dom.Element) ann).getNodeName())) {
                result.add(singleAnnotationMirror(info, (org.w3c.dom.Element) ann));
            }
        }

        return result;
    }

    private static AnnotationMirror singleAnnotationMirror(CompilationInfo info, org.w3c.dom.Element annotation) {
        String typeName = annotation.getAttribute("name");

        NodeList valueDOM = annotation.getChildNodes();
        Map<ExecutableElement, AnnotationValue> values  = new IdentityHashMap<ExecutableElement, AnnotationValue>();

        for (int i = 0; i < valueDOM.getLength(); i++) {
            Node ann = valueDOM.item(i);

            if (ann instanceof org.w3c.dom.Element && "val".equals(((org.w3c.dom.Element) ann).getNodeName())) {
                Pair<? extends ExecutableElement, ? extends AnnotationValue> value = singleValueMirror(info, (org.w3c.dom.Element) ann);
                values.put(value.first(), value.second());
            }
        }

        return new FakeAnnotationMirror(declaredTypeForName(info, typeName), values);
    }

    private static Pair<? extends ExecutableElement, ? extends AnnotationValue> singleValueMirror(CompilationInfo info, org.w3c.dom.Element val) {
        String methodName = val.getAttribute("name");
        String value = val.getAttribute("val");

        return Pair.of(new FakeAttributeElement(info.getElements().getName(methodName)), mirrorValueFromString(info, value));
    }

    private static AnnotationValue mirrorValueFromString(CompilationInfo info, String value) {
        Object realValue;

        //TODO: .class and { escaped? Good idea anyway? Need to actually know the types?
        if (value.endsWith(".class")) {
            realValue = declaredTypeForName(info, value.substring(0, value.length() - ".class".length()));
        } else if (value.startsWith("{") && value.endsWith("}")) {
            value = value.substring(1, value.length() - 1);

            List<Object> result = new ArrayList<Object>();

            for (String element : value.split(", ")) {
                result.add(mirrorValueFromString(info, element));
            }

            realValue = result;
        } else {
            realValue = value;
        }

        return new FakeAnnotationValue(realValue);
    }

    private static FileObject binaryRootFor(CompilationInfo info, Element forElement) {
        TypeElement topLevel = info.getElementUtilities().outermostTypeElement(forElement);

        if (topLevel == null || topLevel.asType().getKind() == TypeKind.ERROR) {
            return null;
        }

        record RootHolder(FileObject root) {}

        Map<TypeElement, RootHolder> binaryRootCache = (Map<TypeElement, RootHolder>) info.getCachedValue(AugmentedAnnotations.class);

        if (binaryRootCache == null) {
            info.putCachedValue(AugmentedAnnotations.class, binaryRootCache = new HashMap<>(), CacheClearPolicy.ON_SIGNATURE_CHANGE);
        }

        return binaryRootCache.computeIfAbsent(topLevel, te -> {
            long s = System.nanoTime();
            try {
                JavaFileObject origin = info.getElements().getFileObjectOf(topLevel);

                if (origin == null) {
                    return null;
                }

                FileObject originFO;

                try {
                    originFO = URLMapper.findFileObject(origin.toUri().toURL());
                } catch (MalformedURLException ex) {
                    LOG.log(Level.FINE, null, ex);
                    return null;
                }

                if (originFO == null) {
                    return null;
                }

                originFO = originFO.getParent();

                String name = topLevel.getQualifiedName().toString();

                for (int i = 0; i < name.length() && originFO != null; i++) {
                    if (name.charAt(i) == '.') {
                        originFO = originFO.getParent();
                    }
                }

                return new RootHolder(originFO);
            } finally {
                if (LOG.isLoggable(Level.FINE)) {
                    Trees trees = info.getTrees();
                    long time = System.nanoTime() - s;
                    synchronized (rootComputationCummulativePerJavac) {
                        WeakJavacReference found = null;

                        for (WeakJavacReference ref : rootComputationCummulativePerJavac) {
                            if (ref.get() == trees) {
                                found = ref;
                            }
                        }
                        if (found == null) {
                            rootComputationCummulativePerJavac.add(found = new WeakJavacReference(trees));
                        }

                        found.cummulativeTime += time;
                    }
                }
            }
        }).root();
    }

    private static DeclaredType declaredTypeForName(CompilationInfo info, String fqn) {
        TypeElement element = info.getElements().getTypeElement(fqn);

        return element != null ? (DeclaredType) element.asType() : new FakeDeclaredType(new FakeTypeElement(info.getElements(), fqn));
    }

    private static final class FakeTypeElement implements TypeElement {

        private final Name fqn;
        private final Name simpleName;

        public FakeTypeElement(Elements elements, String fqn) {
            this.fqn = elements.getName(fqn);

            int lastDot = fqn.lastIndexOf('.');

            if (lastDot > (-1)) this.simpleName = elements.getName(fqn.substring(lastDot + 1));
            else this.simpleName = this.fqn;
        }

        @Override public List<? extends Element> getEnclosedElements() {
            return Collections.emptyList();//TODO: can be made better?
        }

        @Override public NestingKind getNestingKind() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override public Name getQualifiedName() {
            return fqn;
        }

        @Override public Name getSimpleName() {
            return simpleName;
        }

        @Override
        public TypeMirror getSuperclass() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public List<? extends TypeMirror> getInterfaces() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public List<? extends TypeParameterElement> getTypeParameters() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Element getEnclosingElement() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public TypeMirror asType() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.ANNOTATION_TYPE;
        }

        @Override
        public List<? extends AnnotationMirror> getAnnotationMirrors() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Set<Modifier> getModifiers() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public <R, P> R accept(ElementVisitor<R, P> v, P p) {
            return v.visitType(this, p);
        }

        @Override
        public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

    private static final class FakeDeclaredType implements DeclaredType {

        private final TypeElement forType;

        public FakeDeclaredType(TypeElement forType) {
            this.forType = forType;
        }

        @Override
        public Element asElement() {
            return forType;
        }

        @Override
        public TypeMirror getEnclosingType() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public List<? extends TypeMirror> getTypeArguments() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public TypeKind getKind() {
            return TypeKind.DECLARED;
        }

        @Override
        public <R, P> R accept(TypeVisitor<R, P> v, P p) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String toString() {
            return forType.getQualifiedName().toString();
        }

        @Override
        public List<? extends AnnotationMirror> getAnnotationMirrors() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

    private static final class FakeAttributeElement implements ExecutableElement {

        private final Name simpleName;

        public FakeAttributeElement(Name simpleName) {
            this.simpleName = simpleName;
        }

        @Override
        public List<? extends TypeParameterElement> getTypeParameters() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public TypeMirror getReturnType() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public List<? extends VariableElement> getParameters() {
            return Collections.emptyList();
        }

        @Override
        public boolean isVarArgs() {
            return false;
        }

        @Override
        public List<? extends TypeMirror> getThrownTypes() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public AnnotationValue getDefaultValue() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Name getSimpleName() {
            return simpleName;
        }

        @Override
        public TypeMirror asType() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.METHOD;
        }

        @Override
        public List<? extends AnnotationMirror> getAnnotationMirrors() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Set<Modifier> getModifiers() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Element getEnclosingElement() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public List<? extends Element> getEnclosedElements() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public <R, P> R accept(ElementVisitor<R, P> v, P p) {
            return v.visitExecutable(this, p);
        }

        @Override
        public TypeMirror getReceiverType() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isDefault() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

    private static final class FakeAnnotationMirror implements AnnotationMirror {

        private final DeclaredType annotationType;
        private final Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues;

        public FakeAnnotationMirror(DeclaredType annotationType, Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues) {
            this.annotationType = annotationType;
            this.elementValues = elementValues;
        }

        @Override
        public DeclaredType getAnnotationType() {
            return annotationType;
        }

        @Override
        public Map<? extends ExecutableElement, ? extends AnnotationValue> getElementValues() {
            return elementValues;
        }

    }

    private static final class FakeAnnotationValue implements AnnotationValue {

        private final Object value;

        public FakeAnnotationValue(Object value) {
            this.value = value;
        }

        @Override
        public Object getValue() {
            return value;
        }

        @Override
        public <R, P> R accept(AnnotationValueVisitor<R, P> v, P p) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

    //statistics:
    private static final Set<WeakJavacReference> rootComputationCummulativePerJavac = Collections.newSetFromMap(new IdentityHashMap<>());

    private static final class WeakJavacReference extends WeakReference<Trees> implements Runnable {

        private long cummulativeTime;

        public WeakJavacReference(Trees trees) {
            super(trees, Utilities.activeReferenceQueue());
        }

        @Override
        public void run() {
            long timeToReport;

            synchronized (rootComputationCummulativePerJavac) {
                timeToReport = cummulativeTime;
                rootComputationCummulativePerJavac.remove(this);
            }

            LOG.log(Level.FINE, "Cummulative time spend searching for binary roots for javac instance: {0}ms", timeToReport / 1000 / 1000.);
        }

    }

}
