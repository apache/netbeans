/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.web.el.completion;

import com.sun.el.parser.AstDotSuffix;
import com.sun.el.parser.AstIdentifier;
import com.sun.el.parser.AstMethodArguments;
import com.sun.el.parser.Node;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementScanner6;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.modules.csl.api.CodeCompletionContext;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.spi.DefaultCompletionProposal;
import org.netbeans.modules.web.el.AstPath;
import org.netbeans.modules.web.el.CompilationContext;
import org.netbeans.modules.web.el.ELElement;
import org.netbeans.modules.web.el.completion.ELCodeCompletionHandler.PrefixMatcher;

/**
 * Provides java completion inside the EL expressions - for calls of static field, methods and constructors.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class ELJavaCompletion {

    private ELJavaCompletion() {
    }

    private static final Set<Modifier> PUBLIC_STATIC = EnumSet.<Modifier>of(Modifier.PUBLIC, Modifier.STATIC);
    private static final Set<ElementKind> FIELD_METHOD = EnumSet.<ElementKind>of(ElementKind.FIELD, ElementKind.METHOD);
    private static final String JAVA_LANG_PACKAGE = "java.lang"; //NOI18N
    private static final String JAVA_LANG_PREFIX = JAVA_LANG_PACKAGE + "."; //NOI18N
    private static final PackageEntry DEFAULT_PACKAGE = new PackageEntry(JAVA_LANG_PACKAGE, true);

    protected static void propose(
            CompilationContext ccontext,
            CodeCompletionContext context,
            ELElement element,
            Node targetNode,
            List<CompletionProposal> proposals) throws IOException {
        String prefix = getPrefixForNode(context, element, targetNode);
        PrefixMatcher prefixMatcher = PrefixMatcher.create(prefix, context);
        doJavaCompletion(ccontext, prefixMatcher, context.getCaretOffset() - prefix.length(), proposals);
    }

    private static String getPrefixForNode(CodeCompletionContext context, ELElement element, Node targetNode) {
        if (targetNode instanceof AstDotSuffix) {
            AstPath path = new AstPath(element.getNode());
            return extractPrefixFromPath(path.rootToNode(targetNode)) + "."; //NOI18N
        } else if (targetNode instanceof AstMethodArguments) {
            AstPath path = new AstPath(targetNode);
            return extractPrefixFromPath(path.rootToLeaf()); //NOI18N
        }
        return context.getPrefix() != null ? context.getPrefix() : ""; //NOI18N
    }

    private static void doJavaCompletion(CompilationContext ccontext, PrefixMatcher pm, final int offset, final List<CompletionProposal> proposals) throws IOException {
        CompilationController cc = (CompilationController) ccontext.info();
        String packName = pm.getPrefix();
        int dotIndex = pm.getPrefix().lastIndexOf('.'); // NOI18N
        if (dotIndex != -1) {
            packName = pm.getPrefix().substring(0, dotIndex);
        }

        // adds packages to the CC
        addPackages(cc, pm.getPrefix(), offset, proposals);

        Set<PackageEntry> packages = new HashSet<>();
        if (dotIndex == -1) {
            // java.lang package is imported by default
            packages.add(DEFAULT_PACKAGE);
        }
        if (!packName.isEmpty()) {
            packages.add(new PackageEntry(packName, false));
        }

        // adds types to the CC
        addTypesFromPackages(cc, pm, packages, offset + dotIndex + 1, proposals);

        // adds element type fields and methods
        TypeElement typeElement = cc.getElements().getTypeElement(packName);
        if (typeElement == null && dotIndex != -1) {
            typeElement = cc.getElements().getTypeElement(JAVA_LANG_PREFIX + pm.getPrefix().substring(0, dotIndex));
        }
        if (typeElement != null) {
            proposals.addAll(getFieldsAndMethods(typeElement, offset + dotIndex + 1));
        }
    }

//    private static void addAllTypes(final CompilationContext ccontext, CompilationController controler, final List<CompletionProposal> proposals) {
//        String EMPTY = ""; //NOI18N
//        String prefix = ""; //NOI18N
//        ClassIndex.NameKind kind = ClassIndex.NameKind.CASE_INSENSITIVE_PREFIX;
//        Set<ElementHandle<TypeElement>> declaredTypes = controler.getClasspathInfo().getClassIndex().getDeclaredTypes(prefix != null ? prefix : EMPTY, kind, EnumSet.allOf(ClassIndex.SearchScope.class));
//        for (ElementHandle<TypeElement> name : declaredTypes) {
//            proposals.add(new JavaTypeCompletionItem(ccontext, name));
//        }
//    }
    private static void addTypesFromPackages(CompilationController cc, PrefixMatcher pm, Set<PackageEntry> packages, int offset, List<CompletionProposal> proposals) {
        for (PackageEntry packageEntry : packages) {
            PackageElement pkgElem = cc.getElements().getPackageElement(packageEntry.packageName);
            if (pkgElem == null) {
                continue;
            }
            List<TypeElement> tes = new TypeScanner().scan(pkgElem);
            for (TypeElement te : tes) {
                if (packageEntry.imported) {
                    if (pm.matches(te.getSimpleName().toString())) {
                        JavaTypeCompletionItem item = new JavaTypeCompletionItem(te, offset);
                        proposals.add(item);
                    }
                } else {
                    if (pm.matches(te.getQualifiedName().toString())) {
                        JavaTypeCompletionItem item = new JavaTypeCompletionItem(te, offset);
                        proposals.add(item);
                    }
                }
            }
        }
    }

    private static Collection<DefaultCompletionProposal> getFieldsAndMethods(TypeElement typeElement, int i) {
        Map<String, DefaultCompletionProposal> classProposals = new HashMap<>();
        for (Element element : typeElement.getEnclosedElements()) {
            if (element.getModifiers().containsAll(PUBLIC_STATIC) && FIELD_METHOD.contains(element.getKind())) {
                classProposals.put(element.getSimpleName().toString(), new JavaFieldCompletionItem(element, typeElement.getSimpleName().toString(), i));
            }
        }
        return classProposals.values();
    }

    private static void addPackages(CompilationController controler, String fqnPrefix, int offset, final List<CompletionProposal> proposals) {
        if (fqnPrefix == null || fqnPrefix.isEmpty()) {
            fqnPrefix = "java"; //NOI18N
        }
        for (String pkgName : controler.getClasspathInfo().getClassIndex().getPackageNames(
                fqnPrefix, true, EnumSet.allOf(ClassIndex.SearchScope.class))) {
            if (isImportedPackage(pkgName)) {
                proposals.add(new JavaPackageCompletionItem(pkgName, offset));
            }
        }
    }

    private static String extractPrefixFromPath(List<Node> pathToNode) {
        StringBuilder sb = new StringBuilder();
        for (Node node : pathToNode) {
            if (node instanceof AstIdentifier) {
                sb.append(node.getImage());
            } else if (node instanceof AstDotSuffix) {
                sb.append(".").append(node.getImage()); //NOI18N
            }
        }
        return sb.toString(); //NOI18N
    }

    private static boolean isImportedPackage(String pkgName) {
        // TODO - get imported classes and allow them to be completed too. For now we show only classes
        // enabled by default
        return pkgName.startsWith("java.") || pkgName.startsWith("javax.")
                || pkgName.equals("java") || pkgName.equals("javax");
    }

    private static final class JavaTypeCompletionItem extends DefaultCompletionProposal {

        private final String simpleName;
        private final String fqn;
        private final int offset;

        public JavaTypeCompletionItem(TypeElement te, int offset) {
            this.simpleName = te.getSimpleName().toString();
            this.fqn = te.getQualifiedName().toString();
            this.offset = offset;
        }

        @Override
        public org.netbeans.modules.csl.api.ElementHandle getElement() {
            return null;
        }

        @Override
        public org.netbeans.modules.csl.api.ElementKind getKind() {
            return org.netbeans.modules.csl.api.ElementKind.CLASS;
        }

        @Override
        public String getName() {
            return simpleName;
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            return fqn;
        }

        @Override
        public int getAnchorOffset() {
            return offset;
        }
    }

    private static final class JavaFieldCompletionItem extends DefaultCompletionProposal {

        private final String name;
        private final String enclosingName;
        private final org.netbeans.modules.csl.api.ElementKind kind;
        private final int offset;

        public JavaFieldCompletionItem(Element e, String enclosingName, int offset) {
            this.name = e.getSimpleName().toString();
            this.enclosingName = enclosingName;
            this.kind = e.getKind() == ElementKind.METHOD
                    ? org.netbeans.modules.csl.api.ElementKind.METHOD
                    : org.netbeans.modules.csl.api.ElementKind.CONSTANT;
            this.offset = offset;
        }

        @Override
        public org.netbeans.modules.csl.api.ElementHandle getElement() {
            return null;
        }

        @Override
        public org.netbeans.modules.csl.api.ElementKind getKind() {
            return kind;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            return enclosingName;
        }

        @Override
        public int getAnchorOffset() {
            return offset;
        }

        @Override
        public String getCustomInsertTemplate() {
            if (isMethod()) {
                return super.getCustomInsertTemplate() + "(${cursor})"; //NOI18N
            }
            return super.getCustomInsertTemplate();
        }

        private boolean isMethod() {
            return kind == org.netbeans.modules.csl.api.ElementKind.METHOD;
        }
    }

    private static final class JavaPackageCompletionItem extends DefaultCompletionProposal {

        private final String name;
        private final int offset;

        public JavaPackageCompletionItem(String name, int offset) {
            this.name = name;
            this.offset = offset;
        }

        @Override
        public org.netbeans.modules.csl.api.ElementHandle getElement() {
            return null;
        }

        @Override
        public org.netbeans.modules.csl.api.ElementKind getKind() {
            return org.netbeans.modules.csl.api.ElementKind.PACKAGE;
        }

        @Override
        public int getAnchorOffset() {
            return offset;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            return name;
        }
    }

    private static final class TypeScanner extends ElementScanner6<List<TypeElement>, Void> {

        public TypeScanner() {
            super(new ArrayList<TypeElement>());
        }

        private static boolean isAccessibleClass(TypeElement te) {
            NestingKind nestingKind = te.getNestingKind();
            return (nestingKind == NestingKind.TOP_LEVEL && te.getModifiers().contains(Modifier.PUBLIC));
        }

        @Override
        public List<TypeElement> visitType(TypeElement typeElement, Void arg) {
            if (typeElement.getKind() == ElementKind.CLASS && isAccessibleClass(typeElement)) {
                DEFAULT_VALUE.add(typeElement);
            }
            return super.visitType(typeElement, arg);
        }
    }

    private static final class PackageEntry {
        private final String packageName;
        private final boolean imported;

        public PackageEntry(String packageName, boolean imported) {
            this.packageName = packageName;
            this.imported = imported;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 23 * hash + packageName.hashCode();
            hash = 23 * hash + Boolean.valueOf(imported).hashCode();
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (this.getClass() != obj.getClass()) {
                return false;
            }
            PackageEntry other = (PackageEntry) obj;
            if (!this.packageName.equals(other.packageName)) {
                return false;
            }
            if (this.imported != other.imported) {
                return false;
            }
            return true;
        }
    }
}
