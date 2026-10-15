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
package org.netbeans.modules.javafx2.editor.completion.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.modules.javafx2.editor.JavaFXEditorUtils;
import org.netbeans.modules.javafx2.editor.completion.beans.FxDefinitionKind;
import org.netbeans.modules.javafx2.editor.completion.beans.FxProperty;
import org.netbeans.modules.javafx2.editor.completion.model.FxClassUtils;
import org.netbeans.modules.javafx2.editor.completion.model.FxNode;
import org.netbeans.modules.javafx2.editor.completion.model.ImportDecl;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionProvider;

/**
 * Completer for class names. Activates at places, where a class name may be inserted:
 * - in tag names
 * - in attribute names, either blank or starting with capital letter
 * 
 * @author sdedic
 */
@MimeRegistration(mimeType=JavaFXEditorUtils.FXML_MIME_TYPE, service=Completer.Factory.class)
public final class ClassCompleter implements Completer, Completer.Factory {
    private static final Logger LOG = Logger.getLogger(ClassCompleter.class.getName());
    
    private static final int IMPORTED_PRIORITY = 50;
    private static final int NODE_PRIORITY = 100;
    private static final int OTHER_PRIORITY = 200;
    private static final int PACKAGE_PRIORITY = 150;
    
    /**
     * If the class prefix is >= than this treshold, the hierarchy + prefix filter
     * will first get the classes that match the filter, then checks their inheritance
     * hierarchy.
     */
    private static final int PREFIX_TRESHOLD = 3;
   
    private boolean moreItems;

    private String packagePrefix;
    
    private String namePrefix;
    
    
    private final CompletionContext ctx;
    
    public ClassCompleter() {
        this.ctx = null;
    }

    private ClassCompleter(CompletionContext ctx) {
        this.ctx = ctx;
    }
    
    public boolean hasMoreItems() {
        return moreItems;
    }
    
    @Override
    public Completer createCompleter(CompletionContext ctx) {
        FxNode parent = ctx.getElementParent();
        FxProperty pi = ctx.getEnclosingProperty();
        if (pi == null && parent.getKind() != FxNode.Kind.Source) {
            // can complete only in root and in properties
            return null;
        }
        if (ctx.getType() == CompletionContext.Type.BEAN ||
            ctx.getType() == CompletionContext.Type.ROOT ||
            ctx.getType() == CompletionContext.Type.CHILD_ELEMENT || 
            ctx.getType() == CompletionContext.Type.PROPERTY_ELEMENT) {
            
            if (pi == null || pi.getKind() == FxDefinitionKind.LIST) {
                return new ClassCompleter(ctx);
            } 
            if (ctx.getPrefix().startsWith("<") ||
                ctx.getCompletionType() == CompletionProvider.COMPLETION_ALL_QUERY_TYPE) {
                return new ClassCompleter(ctx);
            }
        }
        return null;
    }
    
    private Set<ElementHandle<TypeElement>> namedTypes;
    
    private boolean acceptsQName(CharSequence fullName, CharSequence name) {
        if (packagePrefix != null && fullName.length() > name.length()) {
            if (!CompletionUtils.startsWith(fullName.subSequence(0, fullName.length() - name.length() - 1), packagePrefix)) {
                return false;
            }
        }
        return acceptsName(name);
    }   
    
    private boolean acceptsName(CharSequence name) {
        return namePrefix.isEmpty() ||
            CompletionUtils.startsWith(name, namePrefix);
    }
    
    private TypeMirror  propertyType;
    private boolean propertyTypeResolved;
    
    private TypeMirror getPropertyType() {
        if (propertyTypeResolved) {
            return propertyType;
        }
        FxProperty prop = ctx.getEnclosingProperty();
        // if we start root tag with prefix longer than "<", it already appears in the parent list;
        // so minimal depth that does not fall back to j.n.Node is 2 in that case.
        int minDepth = (ctx.getPrefix().length() > 1) ? 2 : 1;
        if (prop != null) {
            TypeMirrorHandle propTypeH = prop.getType();
            if (propTypeH != null) {
                propertyType = propTypeH.resolve(ctx.getCompilationInfo());
            }
        } else if (ctx.getParents().size() <= minDepth) {
            // root element should be constrainted to Node subclass
            TypeElement e = ctx.getCompilationInfo().getElements().getTypeElement(JavaFXEditorUtils.FXML_NODE_CLASS);
            if (e != null) {
                propertyType = e.asType();
            }
        }
        propertyTypeResolved = true;
        return propertyType;
    }
    
    private boolean acceptsType(TypeElement t) {
        if (t.getModifiers().contains(Modifier.ABSTRACT) ||
            !FxClassUtils.isFxmlAccessible(t)) {
            return false;
        }
        TypeMirror pt = getPropertyType();
        if (pt == null) {
            return true;
        }
        return ctx.getCompilationInfo().getTypes().isAssignable(t.asType(), 
                pt);
    }
    
    /**
     * Loads classes imported by explicit or star import.
     */
    private Set<ElementHandle<TypeElement>> loadImportedClasses() {
        Set<ElementHandle<TypeElement>> handles = new HashSet<ElementHandle<TypeElement>>();
        Collection<ImportDecl> imports = ctx.getModel().getImports();
        for (ImportDecl decl : imports) {
            if (decl.isWildcard()) {
                if (packagePrefix != null && 
                    !CompletionUtils.startsWith(decl.getImportedName(), namePrefix)) {
                    continue;
                }
                // import all relevant classes from the package
                PackageElement pel = ctx.getCompilationInfo().getElements().getPackageElement(decl.getImportedName());
                for (Element e : pel.getEnclosedElements()) {
                    TypeElement tel = (TypeElement)e;
                    if (acceptsName(tel.getSimpleName()) && acceptsType(tel)) {
                        handles.add(ElementHandle.create((TypeElement)e));
                    }
                }
            } else if (CompletionUtils.startsWithCamelCase(decl.getImportedName(), namePrefix)) {
                TypeElement el = ctx.getCompilationInfo().getElements().getTypeElement(decl.getImportedName());
                if (el != null && acceptsType(el)) {
                    handles.add(ElementHandle.create(el));
                }
            }
        }
        return handles;
    }
    
    private TypeElement getBaseClass() {
        TypeElement baseClass = null;
        if (getPropertyType() != null) {
            baseClass = (TypeElement)ctx.getCompilationInfo().getTypes().asElement(getPropertyType());
        }
        if (baseClass == null) {
            baseClass = ctx.getCompilationInfo().getElements().getTypeElement(JavaFXEditorUtils.FXML_NODE_CLASS);
        }
        return baseClass;
    }
    
    Set<ElementHandle<TypeElement>> loadDescenantsOfNode() {
        if (namePrefix.length() < PREFIX_TRESHOLD) {
            return loadDescenantsOfNode2();
        }
        TypeElement baseClass = getBaseClass();
        if (baseClass == null) {
            return Collections.emptySet();
        }
        Set<ElementHandle<TypeElement>> handles = new HashSet<ElementHandle<TypeElement>>();

        Set<ElementHandle<TypeElement>> els = ctx.getClasspathInfo().getClassIndex().
                getDeclaredTypes(namePrefix, ClassIndex.NameKind.CASE_INSENSITIVE_PREFIX, 
                EnumSet.of(ClassIndex.SearchScope.DEPENDENCIES, ClassIndex.SearchScope.SOURCE));
        TypeMirror nodeType = baseClass.asType();
        for (Iterator<ElementHandle<TypeElement>> it = els.iterator(); it.hasNext(); ) {
            ElementHandle<TypeElement> h = it.next();
            TypeElement e = h.resolve(ctx.getCompilationInfo());
            if (e == null ||
                !acceptsQName(e.getQualifiedName(), e.getSimpleName()) ||
                e.getModifiers().contains(Modifier.ABSTRACT) ||
                !FxClassUtils.isFxmlAccessible(e) ||
                !ctx.getCompilationInfo().getTypes().isAssignable(e.asType(), nodeType)) {
                    continue;
            }
            handles.add(h);
        }
        return handles;
    }
    
    Set<ElementHandle<TypeElement>> loadDescenantsOfNode2() {
        // get javafx.scene.Node descendants
        TypeElement baseClass = getBaseClass();
        if (baseClass == null) {
            // something wrong, fxml rt class does not exist
            LOG.warning("javafx.scene.Node class not fond");
            return Collections.emptySet();
        }
        
        ClasspathInfo info = ctx.getClasspathInfo();
        
        ElementHandle<TypeElement> nodeHandle = ElementHandle.create (baseClass);
        
        Set<ElementHandle<TypeElement>> allTypesSeen = new HashSet<ElementHandle<TypeElement>>();
        Deque<ElementHandle<TypeElement>> handles = new LinkedList<ElementHandle<TypeElement>>();
        handles.add(nodeHandle);
        
        long time = System.currentTimeMillis();

        allTypesSeen.add(nodeHandle);
        while (!handles.isEmpty()) {
            ElementHandle<TypeElement> baseHandle = handles.poll();
            LOG.log(Level.FINE, "Loading descendants of {0}", baseHandle);
            Set<ElementHandle<TypeElement>> descendants = new HashSet<ElementHandle<TypeElement>>(
                    info.getClassIndex().getElements(baseHandle,
                        EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS), 
                        EnumSet.of(ClassIndex.SearchScope.DEPENDENCIES, ClassIndex.SearchScope.SOURCE)
                    )
            );
            // eliminate duplicates
            descendants.removeAll(allTypesSeen);
            allTypesSeen.addAll(descendants);
            handles.addAll(descendants);
            LOG.log(Level.FINE, "Unique descendants: {0}", descendants);
        }
        long diff = System.currentTimeMillis();
        LOG.log(Level.FINE, "Loading Node descendants took: {0}ms", diff);

        Set<ElementHandle<TypeElement>> result = new HashSet<ElementHandle<TypeElement>>();
        // add descendants not yet seen to the next processing round
        for (ElementHandle<TypeElement> htype : allTypesSeen) {
            if (!(
                htype.getKind() == ElementKind.CLASS || htype.getKind() == ElementKind.INTERFACE)) {
                continue;
            }
            String n = htype.getQualifiedName();
            if (n.length() < namePrefix.length()) {
                // shorter name, does not match prefix
                continue;
            }
            
            int lastDot = n.lastIndexOf('.');
            if (lastDot != -1 && packagePrefix != null && (
                lastDot < packagePrefix.length() || !n.startsWith(packagePrefix))) {
                    continue;
            }
            
            if (CompletionUtils.startsWith(n.substring(lastDot + 1), namePrefix)) {
                result.add(htype);
            }
        }
        
        return result;
    }
    
    private Set<ElementHandle<TypeElement>> loadFromAllTypes() {
        ClasspathInfo info = ctx.getClasspathInfo();
        Set<ElementHandle<TypeElement>> els = 
                new HashSet<ElementHandle<TypeElement>>(
                    info.getClassIndex().getDeclaredTypes(namePrefix, ClassIndex.NameKind.CASE_INSENSITIVE_PREFIX, 
                    EnumSet.of(ClassIndex.SearchScope.DEPENDENCIES, ClassIndex.SearchScope.SOURCE)
                ));

        TypeMirror pt = getPropertyType();
        if (pt == null) {
            return els;
        }
        for (Iterator<ElementHandle<TypeElement>> it = els.iterator(); it.hasNext(); ) {
            ElementHandle<TypeElement> teh = it.next();
            String qn = teh.getQualifiedName();
            int lastDot = qn.lastIndexOf('.');
            String sn = lastDot == -1 ? qn : qn.substring(lastDot + 1);
            if (!acceptsQName(qn, sn)) {
                continue;
            }
            TypeElement t = teh.resolve(ctx.getCompilationInfo());
            if (t == null || 
                !acceptsType(t)) {
                it.remove();
            }
        }
        return els;
    }
    
    private CompletionItem createItem(ElementHandle<TypeElement> handle, int priority) {
        TypeElement el = handle.resolve(ctx.getCompilationInfo());
        if (el == null) {
            // element does not exist etc
            return null;
        }
        if (el.getKind() != ElementKind.CLASS && el.getKind() != ElementKind.ENUM) {
            // do not honour interfaces
            return null;
        }
        if (!el.getModifiers().contains(Modifier.PUBLIC)) {
            return null;
        }
        CompletionItem item = null;
        
        Collection<? extends ClassItemFactory> converters = MimeLookup.getLookup(JavaFXEditorUtils.FXML_MIME_TYPE).lookupAll(ClassItemFactory.class);
        for (ClassItemFactory converter : converters) {
            item = converter.convert(el, ctx, priority);
            if (item != null) {
                break;
            }
        }
        return item;
    }
    
    private List<CompletionItem> createItems(Collection<? extends ElementHandle<TypeElement>> elems, int priority) {
        List<ElementHandle<TypeElement>> sorted = new ArrayList<ElementHandle<TypeElement>>(elems);
        sorted.sort(CLASS_SORTER);
        List<CompletionItem> items = new ArrayList<CompletionItem>();
        for (ElementHandle<TypeElement> tel : sorted) {
            CompletionItem item = createItem(tel, priority);
            if (item != null) {
                items.add(item);
            }
        }
        return items;
    }
    
    private boolean isPrefixEmpty() {
        return namePrefix.isEmpty() && packagePrefix == null;
    }
    
    @Override
    public List<CompletionItem> complete() {
        namePrefix = ctx.getPrefix();
        if (namePrefix.startsWith("<")) {
            namePrefix = namePrefix.substring(1);
        }
        
        int dot = namePrefix.indexOf('.');
        if (dot != -1) {
            packagePrefix = namePrefix.substring(0, dot);
            namePrefix = namePrefix.substring(dot + 1);
        }
        
        TypeMirror tm = getPropertyType();
        if (tm != null && tm.getKind() != TypeKind.DECLARED) {
            return null;
        }
        
        Set<ElementHandle<TypeElement>> handles;
        
        if (ctx.getCompletionType() == CompletionProvider.COMPLETION_QUERY_TYPE) {
            handles = loadImportedClasses();
            
            List<CompletionItem> items = createItems(handles, IMPORTED_PRIORITY);
            if (!items.isEmpty()) {
                moreItems = true;
                return items;
            }
        } else if (ctx.getCompletionType() != CompletionProvider.COMPLETION_ALL_QUERY_TYPE) {
            return null;
        }
        
        Set<ElementHandle<TypeElement>> nodeCandidates = loadDescenantsOfNode();
        
        List<CompletionItem> items = new ArrayList<CompletionItem>();
        items.addAll(createItems(nodeCandidates, NODE_PRIORITY));

        // offer all classes for some prefixes
        if (!namePrefix.isEmpty()) {
            Set<ElementHandle<TypeElement>> allCandidates = new HashSet<ElementHandle<TypeElement>>(loadFromAllTypes());
            allCandidates.removeAll(nodeCandidates);
            items.addAll(createItems(allCandidates, OTHER_PRIORITY));
        }
        
        return items;
    }
    
    private static final Comparator<ElementHandle<TypeElement>> CLASS_SORTER = 
            new Comparator<ElementHandle<TypeElement>>() {
        @Override
        public int compare(ElementHandle<TypeElement> o1, ElementHandle<TypeElement> o2) {
            String fn1 = o1.getQualifiedName();
            String fn2 = o2.getQualifiedName();
            
            int dot1 = fn1.lastIndexOf('.');
            int dot2 = fn2.lastIndexOf('.');
            
            String sn1 = dot1 == -1 ? fn1 : fn1.substring(dot1 + 1);
            String sn2 = dot2 == -1 ? fn2 : fn2.substring(dot2 + 1);
            
            int diff = sn1.compareToIgnoreCase(sn2);
            if (diff != 0) {
                return diff;
            }
            return fn1.compareToIgnoreCase(fn2);
        }
    };

    private static final Comparator<ElementHandle<TypeElement>> FQN_SORTER = 
            new Comparator<ElementHandle<TypeElement>>() {
        @Override
        public int compare(ElementHandle<TypeElement> o1, ElementHandle<TypeElement> o2) {
            String fn1 = o1.getQualifiedName();
            String fn2 = o2.getQualifiedName();
            
            return fn1.compareToIgnoreCase(fn2);
        }
    };
}
