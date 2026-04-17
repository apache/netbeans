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
package org.netbeans.api.java.source;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import com.sun.tools.javac.api.JavacScope;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Kinds;
import com.sun.tools.javac.code.Source;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.TypeSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.ClassType;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Enter;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.comp.Resolve;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.model.JavacTypes;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;
import java.util.ArrayDeque;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.ModuleElement.ExportsDirective;
import javax.lang.model.element.ModuleElement.RequiresDirective;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.SimpleElementVisitor9;
import javax.lang.model.util.Types;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.java.source.builder.ElementsService;
import org.netbeans.modules.java.source.base.SourceLevelUtils;
import org.openide.util.Pair;
import org.openide.util.Parameters;

/**
 *
 * @author Jan Lahoda, Dusan Balek, Tomas Zezula
 */
public final class ElementUtilities {

    private static final ElementAcceptor ALL_ACCEPTOR = (Element e, TypeMirror type) -> true;

    private final Context ctx;
    private final ElementsService delegate;
    private final CompilationInfo info;
    
    /** Creates a new instance of ElementUtilities */
    ElementUtilities(@NonNull final CompilationInfo info) {
        this((JavacTaskImpl)info.impl.getJavacTask(), info);
    }

    ElementUtilities(@NonNull final JavacTaskImpl jt) {
        this(jt, null);
    }

    private ElementUtilities(@NonNull final JavacTaskImpl jt, @NullAllowed final CompilationInfo info) {
        this.ctx = jt.getContext();
        this.delegate = ElementsService.instance(ctx);
        this.info = info;
    }
    /**
     * Returns the type element within which this member or constructor
     * is declared. Does not accept packages
     * If this is the declaration of a top-level type (a non-nested class
     * or interface), returns null.
     *
     * @return the type declaration within which this member or constructor
     * is declared, or null if there is none
     * @throws IllegalArgumentException if the provided element is a package element
     */
    public TypeElement enclosingTypeElement( Element element ) throws IllegalArgumentException {
        return enclosingTypeElementImpl(element);
    }
    
    static TypeElement enclosingTypeElementImpl( Element element ) throws IllegalArgumentException {
	
        if( element.getKind() == ElementKind.PACKAGE ) {
            throw new IllegalArgumentException();
        }

        element = element.getEnclosingElement();
	
        if (element.getKind() == ElementKind.PACKAGE) {
            //element is a top level class, returning null according to the contract:
            return null;
        }
        
        while(element != null && !(element.getKind().isClass() || element.getKind().isInterface())) {
            element = element.getEnclosingElement();
        }

        return (TypeElement)element;
    }
    
    /**
     * 
     * The outermost TypeElement which indirectly encloses this element.
     */
    public TypeElement outermostTypeElement(Element element) {
        return delegate.outermostTypeElement(element);
    }
    
    /**
     * Returns the implementation of a method in class origin; null if none exists.
     */
    public Element getImplementationOf(ExecutableElement method, TypeElement origin) {
        return delegate.getImplementationOf(method, origin);
    }
    
    /**Returns true if the given element is synthetic.
     * 
     *  @param element to check
     *  @return true if and only if the given element is synthetic, false otherwise
     */
    public boolean isSynthetic(Element element) {
        return (((Symbol) element).flags() & Flags.SYNTHETIC) != 0
            || (((Symbol) element).flags() & Flags.GENERATEDCONSTR) != 0 
            || (((Symbol) element).flags() & Flags.GENERATED_MEMBER) != 0;
    }
    
    /**Returns true if the given module is open.
     * 
     *  @param element to check
     *  @return true if and only if the given module is open, false otherwise
     */
    public boolean isOpen(ModuleElement element) {
        return ((Symbol.ModuleSymbol) element).flags.contains(Symbol.ModuleFlags.OPEN);
    }
    
    /**
     * Returns true if this element represents a method which overrides a
     * method in one of its superclasses.
     */
    public boolean overridesMethod(ExecutableElement element) {
        return delegate.overridesMethod(element);
    }
    
    /**
     * Returns a binary name of a type.
     * @param element for which the binary name should be returned
     * @return the binary name, see Java Language Specification 13.1
     * @throws IllegalArgumentException when the element is not a javac element
     */
    public static String getBinaryName (TypeElement element) throws IllegalArgumentException {
        if (element instanceof Symbol.TypeSymbol) {
            return ((Symbol.TypeSymbol)element).flatName().toString();
        }
        else {
            throw new IllegalArgumentException ();
        } 
    }
    
    /**
     * Returns all members of a type, whether inherited or
     * declared directly.  For a class the result also includes its
     * constructors, but not local or anonymous classes.
     * 
     * @param type  the type being examined
     * @param acceptor to filter the members
     * @return all members in the type
     * @see Elements#getAllMembers
     */
    public Iterable<? extends Element> getMembers(TypeMirror type, ElementAcceptor acceptor) {
        List<Element> membersList = new ArrayList<>();
        Map<String, List<Element>> membersMap = new HashMap<>();
        if (type != null) {
            if (acceptor == null) {
                acceptor = ALL_ACCEPTOR;
            }
            Elements elements = JavacElements.instance(ctx);
            Types types = JavacTypes.instance(ctx);
            switch (type.getKind()) {
                case DECLARED:
                case UNION:
                case INTERSECTION:
                    TypeElement te = (TypeElement)((DeclaredType)type).asElement();
                    if (te == null) break;
                    for (Element member : elements.getAllMembers(te)) {
                        if (acceptor.accept(member, type)) {
                            addIfNotHidden(member, membersList, membersMap, elements, types);
                        }
                    }
                    if (te.getKind().isClass() || te.getKind().isInterface() && SourceLevelUtils.allowDefaultMethods(Source.instance(ctx))) {
                        VarSymbol thisPseudoMember = new VarSymbol(Flags.FINAL | Flags.HASINIT, Names.instance(ctx)._this, (ClassType)te.asType(), (ClassSymbol)te);
                        if (acceptor.accept(thisPseudoMember, type)) {
                            addAlways(thisPseudoMember, membersList, membersMap);
                        }
                        if (te.getSuperclass().getKind() == TypeKind.DECLARED) {
                            VarSymbol superPseudoMember = new VarSymbol(Flags.FINAL | Flags.HASINIT, Names.instance(ctx)._super, (ClassType)te.getSuperclass(), (ClassSymbol)te);
                            if (acceptor.accept(superPseudoMember, type)) {
                                addAlways(superPseudoMember, membersList, membersMap);
                            }
                        }
                    }
                case BOOLEAN:
                case BYTE:
                case CHAR:
                case DOUBLE:
                case FLOAT:
                case INT:
                case LONG:
                case SHORT:
                case VOID:
                    Type t = Symtab.instance(ctx).classType;
                    com.sun.tools.javac.util.List<Type> typeargs = com.sun.tools.javac.util.List.of((Type)type);
                    t = new ClassType(t.getEnclosingType(), typeargs, t.tsym);
                    Element classPseudoMember = new VarSymbol(Flags.STATIC | Flags.PUBLIC | Flags.FINAL, Names.instance(ctx)._class, t, ((Type)type).tsym);
                    if (acceptor.accept(classPseudoMember, type)) {
                        addAlways(classPseudoMember, membersList, membersMap);
                    }
                    break;
                case ARRAY:
                    for (Element member : elements.getAllMembers((TypeElement)((Type)type).tsym)) {
                        if (acceptor.accept(member, type)) {
                            addAlways(member, membersList, membersMap);
                        }
                    }
                    t = Symtab.instance(ctx).classType;
                    typeargs = com.sun.tools.javac.util.List.of((Type)type);
                    t = new ClassType(t.getEnclosingType(), typeargs, t.tsym);
                    classPseudoMember = new VarSymbol(Flags.STATIC | Flags.PUBLIC | Flags.FINAL, Names.instance(ctx)._class, t, ((Type)type).tsym);
                    if (acceptor.accept(classPseudoMember, type)) {
                        addAlways(classPseudoMember, membersList, membersMap);
                    }
                    break;
            }
        }
        return membersList;
    }
    
    /**
     * Finds symbols which satisfy the acceptor visible in the passed scope. The method returns a Map keyed by the
     * found Elements. Each Element is mapped to the closest Scope which introduced the Element. For example, a field declared
     * by an outer class will map to that outer class' scope. An accessible field inherited from outer class' superclass
     * will <b>also</b> map to the outer class' scope. The caller can then determine, based on {@link Element#getEnclosingElement()} and
     * the mapped Scope whether the symbol is directly declared, or inherited. Non-member symbols (variables, parameters, try resources, ...) 
     * map to Scope of their defining Method.
     * <p>
     * If an Element from outer Scope is hidden by a similar Element
     * in inner scope, only the Element visible to the passed Scope is returned. For example, if both the starting (inner) class and its outer class
     * define method m(), only InnerClass.m() will be returned.
     * <p>
     * Note that {@link Scope#getEnclosingMethod()} returns non-null even for class scopes of local or anonymous classes; check both {@link Scope#getEnclosingClass()}
     * and {@link Scope#getEnclosingMethod()} and their relationship to get the appropriate Element associated with the Scope.
     * 
     * @param scope the initial search scope
     * @param acceptor the element filter.
     * @return Mapping of visible and accessible Elements to their defining {@link Scope}s (which introduced them).
     * @see Scope
     * @since 2.16
     */
    public @NonNull Map<? extends Element, Scope> findElementsAndOrigins(@NonNull Scope scope, ElementAcceptor acceptor) {
        Parameters.notNull("scope", scope); // NOI18N
        final Map<Element, Scope> result = new HashMap<>();
        if (acceptor == null) {
            acceptor = ALL_ACCEPTOR;
        }
        Map<String, List<Element>> members = null;
        Elements elements = JavacElements.instance(ctx);
        Types types = JavacTypes.instance(ctx);
        TypeElement cls;
        Deque<Scope>  outerScopes = new ArrayDeque<>();
        Deque<Map>  visibleEls = new ArrayDeque<>();
        Element current = null;
        
        while (scope != null) {
            cls = scope.getEnclosingClass();
            Element e = null;
            if (cls != null) {
                ExecutableElement ee = scope.getEnclosingMethod();
                if (ee != null && ee.getEnclosingElement() != cls) {
                    e = ee;
                } else {
                    e = cls;
                }
            }
            if (e != current) {
                // push at the scope entry
                members = new HashMap<>();
                outerScopes.push(scope);
                visibleEls.push(members);
                current = e;
            }
            if (cls != null) {
                for (Element local : scope.getLocalElements()) {
                    if (acceptor.accept(local, null)) {
                        addIfNotHidden(local, null, members, elements, types);
                    }
                }
                TypeMirror type = cls.asType();
                for (Element member : elements.getAllMembers(cls)) {
                    if (acceptor.accept(member, type)) {
                        addIfNotHidden(member, null, members, elements, types);
                    }
                }
            } else {
                for (Element local : scope.getLocalElements()) {
                    if (!local.getKind().isClass() && !local.getKind().isInterface() &&
                        (local.getEnclosingElement() != null && acceptor.accept(local, local.getEnclosingElement().asType()))) {
                        addIfNotHidden(local, null, members, elements, types);
                    }
                }
            }
            scope = scope.getEnclosingScope();
        }
        
        while (!outerScopes.isEmpty()) {
            Scope x = outerScopes.pop();
            Collection<List<Element>> vals = (Collection<List<Element>>)visibleEls.pop().values();
            for (List<Element> col : vals) {
                for (Element e : col) {
                    result.put(e, x);
                }
            }
        }
        
        return result;
    }

    /**
     * @param list is used to collect elements in encounter order
     * @param members is only used for fast isHidden() checks later
     */
    private static void addAlways(Element element, List<Element> list, Map<String, List<Element>> members) {
        String name = element.getSimpleName().toString();
        members.computeIfAbsent(name, (e) -> new ArrayList<>()).add(element);
        if (list != null) {
            list.add(element);
        }
    }

    /**
     * @param list is used to collect elements in encounter order
     * @param map is only used for fast isHidden() checks later
     */
    private static <E extends Element> void addIfNotHidden(E element, List<E> list, Map<String, List<E>> map, Elements elements, Types types) {
        String name = element.getSimpleName().toString();
        List<E> namedMembers = map.get(name);
        if (namedMembers != null) {
            // PENDING: isHidden will not report variables, which are effectively hidden by anonymous or local class' variables.
            // there is no way how to denote such hidden local variable/paremeter from the inner class, so such vars should
            // not be reported.
            if (isHidden(element, namedMembers, elements, types)) {
                return;
            }
        } else {
            namedMembers = new ArrayList<>();
            map.put(name, namedMembers);
        }
        if (list != null) {
            list.add(element);
        }
        namedMembers.add(element);
    }

    /**Return members declared in the given scope.
     */
    public Iterable<? extends Element> getLocalMembersAndVars(Scope scope, ElementAcceptor acceptor) {
        ArrayList<Element> membersList = new ArrayList<>();
        Map<String, List<Element>> membersMap = new HashMap<>();
        if (acceptor == null) {
            acceptor = ALL_ACCEPTOR;
        }
        Elements elements = JavacElements.instance(ctx);
        Types types = JavacTypes.instance(ctx);
        TypeElement cls;
        while(scope != null) {
            if ((cls = scope.getEnclosingClass()) != null) {
                for (Element local : scope.getLocalElements()) {
                    if (acceptor.accept(local, null)) {
                        addIfNotHidden(local, membersList, membersMap, elements, types);
                    }
                }
                TypeMirror type = cls.asType();
                for (Element member : elements.getAllMembers(cls)) {
                    if (acceptor.accept(member, type)) {
                        addIfNotHidden(member, membersList, membersMap, elements, types);
                    }
                }
            } else {
                for (Element local : scope.getLocalElements()) {
                    if (!local.getKind().isClass() && !local.getKind().isInterface() &&
                        (local.getEnclosingElement() != null && acceptor.accept(local, local.getEnclosingElement().asType()))) {
                        addIfNotHidden(local, membersList, membersMap, elements, types);
                    }
                }
            }
            scope = scope.getEnclosingScope();
        }

        return membersList;
    }

    /**Return variables declared in the given scope.
     */
    public Iterable<? extends Element> getLocalVars(Scope scope, ElementAcceptor acceptor) {
        ArrayList<Element> membersList = new ArrayList<>();
        Map<String, List<Element>> membersMap = new HashMap<>();
        if (acceptor == null) {
            acceptor = ALL_ACCEPTOR;
        }
        Elements elements = JavacElements.instance(ctx);
        Types types = JavacTypes.instance(ctx);
        while(scope != null && scope.getEnclosingClass() != null) {
            for (Element local : scope.getLocalElements()) {
                if (acceptor.accept(local, null)) {
                    addIfNotHidden(local, membersList, membersMap, elements, types);
                }
            }
            scope = scope.getEnclosingScope();
        }
        return membersList;
    }
    
    /**Return {@link TypeElement}s:
     * <ul>
     *    <li>which are imported</li>
     *    <li>which are in the same package as the current file</li>
     *    <li>which are in the java.lang package</li>
     * </ul>
     */
    public Iterable<? extends TypeElement> getGlobalTypes(ElementAcceptor acceptor) {
        ArrayList<TypeElement> membersList = new ArrayList<>();
        Map<String, List<TypeElement>> membersMap = new HashMap<>();
        if (acceptor == null) {
            acceptor = ALL_ACCEPTOR;
        }
        Trees trees = JavacTrees.instance(ctx);
        Elements elements = JavacElements.instance(ctx);
        Types types = JavacTypes.instance(ctx);
        for (CompilationUnitTree unit : Collections.singletonList(info.getCompilationUnit())) {
            TreePath path = new TreePath(unit);
            Scope scope = trees.getScope(path);
            while (scope instanceof JavacScope && ((JavacScope)scope).getScopeType() == ORDINARY_SCOPE_TYPE) {
                for (Element local : scope.getLocalElements()) {
                    if (local.getKind().isClass() || local.getKind().isInterface()) {
                        if (acceptor.accept(local, null)) {
                            addIfNotHidden((TypeElement)local, membersList, membersMap, elements, types);
                        }
                    }
                }
                scope = scope.getEnclosingScope();
            }
            Element element = trees.getElement(path);
            if (element != null && element.getKind() == ElementKind.PACKAGE) {
                for (Element member : element.getEnclosedElements()) {
                    if (acceptor.accept(member, null)) {
                        addIfNotHidden((TypeElement)member, membersList, membersMap, elements, types);
                    }
                }
            }
            while (scope != null) {
                for (Element local : scope.getLocalElements()) {
                    if (local.getKind().isClass() || local.getKind().isInterface()) {
                        if (acceptor.accept(local, null)) {
                            addIfNotHidden((TypeElement)local, membersList, membersMap, elements, types);
                        }
                    }
                }
                scope = scope.getEnclosingScope();
            }
        }
        return membersList;
    }

    private static final Object ORDINARY_SCOPE_TYPE;
    private static final Logger LOG = Logger.getLogger(ElementUtilities.class.getName());

    static {
        Object ordinary = null;

        try {
            ordinary = Enum.valueOf((Class<Enum>) Class.forName("com.sun.tools.javac.api.JavacScope$ScopeType"), "ORDINARY");
        } catch (ClassNotFoundException ex) {
            LOG.log(Level.FINE, null, ex);
        }

        ORDINARY_SCOPE_TYPE = ordinary;
    }

    /**Filter {@link Element}s
     */
    public static interface ElementAcceptor {
        /**Is the given element accepted.
         * 
         * @param e element to test
         * @param type the type for which to check if the member is accepted
         * @return true if and only if given element should be accepted
         */
        boolean accept(Element e, TypeMirror type);
    }

    private static boolean isHidden(Element member, List<? extends Element> members, Elements elements, Types types) {
        for (ListIterator<? extends Element> it = members.listIterator(); it.hasNext();) {
            Element hider = it.next();
            if (hider == member) {
                return true;
            }
            if (elements.hides(member, hider)) {
                it.remove();
            } else {
                if (member instanceof VariableElement && hider instanceof VariableElement
                        && (!member.getKind().isField() || hider.getKind().isField())) {
                    return true;
                }
                TypeMirror memberType = member.asType();
                TypeMirror hiderType = hider.asType();
                if (memberType.getKind() == TypeKind.EXECUTABLE && hiderType.getKind() == TypeKind.EXECUTABLE) {
                    if (types.isSubsignature((ExecutableType)hiderType, (ExecutableType)memberType)) {
                        return true;
                    }
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * Returns name of the given element.
     * @param el element
     * @param fqn true if fully qualified name should be returned
     * @return requested name
     *
     * @since 0.136
     */
    public CharSequence getElementName(Element el, boolean fqn) {
        if (el == null || el.asType().getKind() == TypeKind.NONE) {
            return ""; //NOI18N
        }
        return new ElementNameVisitor().visit(el, fqn);
    }

    private static class ElementNameVisitor extends SimpleElementVisitor9<StringBuilder, Boolean> {
        
        private ElementNameVisitor() {
            super(new StringBuilder());
        }

        @Override
        public StringBuilder visitExecutable(ExecutableElement e, Boolean p) {
            if (p != Boolean.TRUE || e.getEnclosingElement() == null) {
                return DEFAULT_VALUE.append(e.getSimpleName());
            } else {
                return e.getEnclosingElement().accept(this, p).
                    append(".").
                    append(e.getSimpleName());
            }
        }

        @Override
        public StringBuilder visitVariable(VariableElement e, Boolean p) {
            if (p != Boolean.TRUE || e.getEnclosingElement() == null) {
                return DEFAULT_VALUE.append(e.getSimpleName());
            } else {
                return e.getEnclosingElement().accept(this, p).
                    append(".").
                    append(e.getSimpleName());
            }
        }

        
        @Override
        public StringBuilder visitPackage(PackageElement e, Boolean p) {
            return DEFAULT_VALUE.append((p ? e.getQualifiedName() : e.getSimpleName()).toString());
        }

	    @Override
        public StringBuilder visitType(TypeElement e, Boolean p) {
            return DEFAULT_VALUE.append((p ? e.getQualifiedName() : e.getSimpleName()).toString());
        }        

        @Override
        public StringBuilder visitModule(ModuleElement e, Boolean p) {
            return DEFAULT_VALUE.append((p ? e.getQualifiedName() : e.getSimpleName()).toString());
        }

        @Override
        public StringBuilder visitRecordComponent(RecordComponentElement e, Boolean p) {
            return visitVariable((VariableElement) e, p);
        }

    }

    /**
     * Returns true if the element is declared (directly or indirectly) local
     * to a method or variable initializer.  Also true for fields of inner 
     * classes which are in turn local to a method or variable initializer.
     */
    public boolean isLocal(Element element) {
        return delegate.isLocal(element);
    }
    
    /**
     * Returns true if a method specified by name and type is defined in a
     * class type.
     */
    public boolean alreadyDefinedIn(CharSequence name, ExecutableType method, TypeElement enclClass) {
        return delegate.alreadyDefinedIn(name, method, enclClass);
    }
    
    /**
     * Returns true if a type element has the specified element as a member.
     */
    public boolean isMemberOf(Element e, TypeElement type) {
        return delegate.isMemberOf(e, type);
    }                
    
    /**
     * Returns the parent method which the specified method overrides, or null
     * if the method does not override a parent class method.
     */
    public ExecutableElement getOverriddenMethod(ExecutableElement method) {
        return delegate.getOverriddenMethod(method);
    }        
    /**
     * Returns true if this element represents a method which 
     * implements a method in an interface the parent class implements.
     */
    public boolean implementsMethod(ExecutableElement element) {
        return delegate.implementsMethod(element);
    }
    
    /**Find all methods in given type and its supertypes, which are not implemented.
     * Will not return default methods implemented directly in interfaces in impl type closure.
     * 
     * @param impl to inspect
     * @return list of all unimplemented methods
     * 
     * @since 0.20
     * @since 2.15 does not return default methods
     */
    public List<? extends ExecutableElement> findUnimplementedMethods(TypeElement impl) {
        return findUnimplementedMethods(impl, impl, false);
    }
    
    /**
     * Finds all unimplemented methods in the given type and supertypes, but possibly include
     * also interface default methods.
     * <p>
     * If the platform configured for the type is older than JDK8, the method is equivalent
     * to {@link #findUnimplementedMethods(javax.lang.model.element.TypeElement)}. If `includeDefaults'
     * is {@code true}, returns also default methods as if the methods were required to be
     * reimplemented by the final class.
     * 
     * @param impl the implementation type
     * @param includeDefaults if true, will also return interface default methods, which
     * are not overriden in supertypes 
     * @return unimplemented (and/or default) methods.
     * @since 2.15
     */
    public List<? extends ExecutableElement> findUnimplementedMethods(TypeElement impl, boolean includeDefaults) {
        return findUnimplementedMethods(impl, impl, includeDefaults);
    }

    /**Find all methods in given type and its supertypes, which are overridable.
     * 
     * @param type to inspect
     * @return list of all overridable methods
     *
     * @since 0.136
     */
    public List<? extends ExecutableElement> findOverridableMethods(TypeElement type) {
        List<ExecutableElement> overridable = new ArrayList<>();
        final Set<Modifier> notOverridable = EnumSet.copyOf(NOT_OVERRIDABLE);
        if (!type.getModifiers().contains(Modifier.ABSTRACT)) {
            notOverridable.add(Modifier.ABSTRACT);
        }
        DeclaredType dt = (DeclaredType)type.asType();
        Types types = JavacTypes.instance(ctx);
        Set<String> typeStrings = new HashSet<>();
        Tree.Kind kind = info.getTrees().getTree(type).getKind();

        for (ExecutableElement ee : ElementFilter.methodsIn(info.getElements().getAllMembers(type))) {
            
            TypeMirror methodType = types.erasure(types.asMemberOf(dt, ee));
            String methodTypeString = ee.getSimpleName().toString() + methodType.toString();
            if (typeStrings.contains(methodTypeString)) {
                continue;
            }
            // javac generated record members disappear if overridden
            boolean replaceable = kind == Tree.Kind.RECORD && isSynthetic(ee);

            Set<Modifier> set = EnumSet.copyOf(notOverridable);                
            set.removeAll(ee.getModifiers());
            if (set.size() == notOverridable.size()
                    && !overridesPackagePrivateOutsidePackage(ee, type) //do not offer package private methods in case they're from different package
                    && (replaceable || !isOverridden(ee, type))) {
                overridable.add(ee);
                if (ee.getModifiers().contains(Modifier.ABSTRACT)) {
                    typeStrings.add(methodTypeString);
                }
            }
        }
        Collections.reverse(overridable);
        return overridable;
    }

    /**
     * Check whether given type has a getter method for the given field.
     * @param type to inspect
     * @param field to search getter for
     * @param codeStyle
     * @return true if getter method exists
     *
     * @since 0.136
     */
    public boolean hasGetter(TypeElement type, VariableElement field, CodeStyle codeStyle) {
        boolean isBoolean = field.asType().getKind() == TypeKind.BOOLEAN;
        boolean isStatic = field.getModifiers().contains(Modifier.STATIC);
        String name = CodeStyleUtils.computeGetterName(field.getSimpleName(), isBoolean, isStatic, codeStyle);
        return delegate.alreadyDefinedIn(name, field.asType(), com.sun.tools.javac.util.List.<TypeMirror>nil(), type);
    }

    /**
     * Check whether given type has a setter method for the given field.
     * @param type to inspect
     * @param field to search setter for
     * @param codeStyle
     * @return true if setter method exists
     *
     * @since 0.136
     */
    public boolean hasSetter(TypeElement type, VariableElement field, CodeStyle codeStyle) {
        boolean isStatic = field.getModifiers().contains(Modifier.STATIC);
        String name = CodeStyleUtils.computeSetterName(field.getSimpleName(), isStatic, codeStyle);
        return delegate.alreadyDefinedIn(name, info.getTypes().getNoType(TypeKind.VOID), com.sun.tools.javac.util.List.<TypeMirror>of(field.asType()), type);
    }

    /**
     * Checks whether 'e' contains error or is missing. If the passed element is null
     * it's assumed the element could not be resolved and this method returns true. Otherwise,
     * the element's type kind is checked against error constants and finally the erroneous
     * state of the element is checked. 
     * 
     * @param e Element to check or {@code null}
     * @return true, if the element is missing (is {@code null}) or contains errors.
     */
    public boolean isErroneous(@NullAllowed Element e) {
        if (e == null) {
            return true;
        }
        if (e.getKind() == ElementKind.MODULE && ((Symbol)e).kind == Kinds.Kind.ERR) {
            return true;
        }
        final TypeMirror type = e.asType();
        if (type == null) {
            return false;
        }
        if (type.getKind() == TypeKind.ERROR || type.getKind() == TypeKind.OTHER) {
            return true;
        }
        if (type instanceof Type) {
            if (((Type)type).isErroneous()) {
                return true;
            }
        }
        return false;
    }
    
    /** Check whether the given variable is effectively final or final.
     * 
     * @param e variable to check for effectively final status
     * @return true if the given variable is effectively final or final
     * @since 0.112
     */
    public boolean isEffectivelyFinal(VariableElement e) {
        return (((Symbol) e).flags() & (Flags.EFFECTIVELY_FINAL | Flags.FINAL)) != 0;
    }
    
    /**Looks up the given Java element.
     * 
     * The <code>elementDescription</code> format is as follows:
     * <dl>
     *   <dt>for type (class, enum, interface or annotation type)</dt>
     *     <dd><em>the FQN of the type</em></dd>
     *   <dt>for field or enum constant</dt>
     *     <dd><em>the FQN of the enclosing type</em><code>.</code><em>field name</em></dd>
     *   <dt>for method</dt>
     *     <dd><em>the FQN of the enclosing type</em><code>.</code><em>method name</em><code>(</code><em>comma separated parameter types</em><code>)</code><br>
     *         The parameter types may include type parameters, but these are ignored. The last parameter type can use ellipsis (...) to denote vararg method.</dd>
     *   <dt>for constructor</dt>
     *     <dd><em>the FQN of the enclosing type</em><code>.</code><em>simple name of enclosing type</em><code>(</code><em>comma separated parameter types</em><code>)</code><br>
     *         See method format for more details on parameter types.</dd>
     * </dl>
     * 
     * @param description the description of the element that should be checked for existence
     * @return the found element, or null if not available
     * @since 0.115
     */
    public @CheckForNull Element findElement(@NonNull String description) {
        if (description.contains("(")) {
            //method:
            String methodFullName = description.substring(0, description.indexOf('('));
            String className = methodFullName.substring(0, methodFullName.lastIndexOf('.'));
            TypeElement clazz = info.getElements().getTypeElement(className);
            
            if (clazz == null) return null;
            
            String methodSimpleName = methodFullName.substring(methodFullName.lastIndexOf('.') + 1);
            boolean constructor = clazz.getSimpleName().contentEquals(methodSimpleName);
            String parameters = description.substring(description.indexOf('(') + 1, description.lastIndexOf(')') + 1);
            
            int paramIndex = 0;
            int lastParamStart = 0;
            int angleDepth = 0;
            //XXX:
            List<TypeMirror> types = new ArrayList<>();
            
            while (paramIndex < parameters.length()) {
                switch (parameters.charAt(paramIndex)) {
                    case '<': angleDepth++; break;
                    case '>': angleDepth--; break; //TODO: check underflow
                    case ',':
                        if (angleDepth > 0) break;
                    case ')':
                        if (paramIndex > lastParamStart) {
                            String type = parameters.substring(lastParamStart, paramIndex).replace("...", "[]");
                            //TODO: handle varargs
                            types.add(info.getTypes().erasure(info.getTreeUtilities().parseType(type, info.getTopLevelElements().get(0)/*XXX*/)));
                            lastParamStart = paramIndex + 1;
                        }
                        break;
                }
                
                paramIndex++;
            }
            
            OUTER: for (ExecutableElement ee : constructor ? ElementFilter.constructorsIn(clazz.getEnclosedElements()) : ElementFilter.methodsIn(clazz.getEnclosedElements())) {
                if ((constructor || ee.getSimpleName().contentEquals(methodSimpleName)) && ee.getParameters().size() == types.size()) {
                    Iterator<? extends TypeMirror> real = ((ExecutableType) info.getTypes().erasure(ee.asType())).getParameterTypes().iterator();
                    Iterator<TypeMirror> expected = types.iterator();
                    
                    while (real.hasNext() && expected.hasNext()) {
                        if (!info.getTypes().isSameType(real.next(), expected.next())) {
                            continue OUTER;
                        }
                    }
                    
                    assert real.hasNext() == expected.hasNext();
                    
                    return ee;
                }
            }
        }
        
        //field or class:
        TypeElement el = info.getElements().getTypeElement(description);
        
        if (el != null) return el;
        
        int dot = description.lastIndexOf('.');
        
        if (dot != (-1)) {
            String simpleName = description.substring(dot + 1);
            
            el = info.getElements().getTypeElement(description.substring(0, dot));
            
            if (el != null) {
                for (VariableElement var : ElementFilter.fieldsIn(el.getEnclosedElements())) {
                    if (var.getSimpleName().contentEquals(simpleName)) {
                        return var;
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * Find the element of the method descriptor associated to the functional interface.
     * 
     * @param origin functional interface element
     * @return associated method descriptor element or <code>null</code> if the <code>origin</code> is not a functional interface.
     * @since 2.14
     */
    public ExecutableElement getDescriptorElement(TypeElement origin) {
        com.sun.tools.javac.code.Types types = com.sun.tools.javac.code.Types.instance(info.impl.getJavacTask().getContext());
        if (types.isFunctionalInterface((TypeSymbol)origin)) {
            Symbol sym = types.findDescriptorSymbol((TypeSymbol)origin);
            if (sym != null && sym.getKind() == ElementKind.METHOD) {
                return (ExecutableElement)sym;
            }
        }
        return null;
    }

    /**
     * Find all elements that are linked record elements for the given input. Will
     * return the record component element, field, accessor method, and canonical constructor
     * parameters.
     *
     * This method can be called on any {@code Element}, and will return a collection
     * with a single entry if the provided element is not a record element.
     *
     * @param forElement for which the linked elements should be found
     * @return a collection containing the provided element, plus any additional elements
     *         that are linked to it by the Java record specification
     * @since 2.70
     */
    public Collection<? extends Element> getLinkedRecordElements(Element forElement) {
        Parameters.notNull("forElement", forElement);

        TypeElement record = null;
        Name componentName = null;

        switch (forElement.getKind()) {
            case FIELD -> {
                Element enclosing = forElement.getEnclosingElement();
                if (enclosing.getKind() == ElementKind.RECORD) {
                    record = (TypeElement) enclosing;
                    componentName = forElement.getSimpleName();
                }
            }
            case PARAMETER -> {
                Element enclosing = forElement.getEnclosingElement();
                if (enclosing.getKind() == ElementKind.CONSTRUCTOR) {
                    Element enclosingType = enclosing.getEnclosingElement();
                    if (enclosingType.getKind() == ElementKind.RECORD) {
                        TypeElement recordType = (TypeElement) enclosingType;
                        ExecutableElement constructor = recordCanonicalConstructor(recordType);
                        if (constructor != null && constructor.equals(enclosing)) {
                            int idx = constructor.getParameters().indexOf(forElement);
                            if (idx >= 0 && idx < recordType.getRecordComponents().size()) {
                                RecordComponentElement component = recordType.getRecordComponents().get(idx);
                                if (component.getSimpleName().equals(forElement.getSimpleName())) {
                                    record = recordType;
                                    componentName = component.getSimpleName();
                                }
                            }
                        }
                    }
                }
            }
            case METHOD -> {
                Element enclosing = forElement.getEnclosingElement();
                ExecutableElement method = (ExecutableElement) forElement;
                if (method.getParameters().isEmpty() && enclosing.getKind() == ElementKind.RECORD) {
                    TypeElement recordType = (TypeElement) enclosing;
                    for (RecordComponentElement component : recordType.getRecordComponents()) {
                        if (forElement.equals(component.getAccessor())) {
                            record = recordType;
                            componentName = component.getSimpleName();
                        }
                    }
                }
            }
            case RECORD_COMPONENT -> {
                record = (TypeElement) forElement.getEnclosingElement();
                componentName = forElement.getSimpleName();
            }
        }

        if (record == null) {
            return Collections.singleton(forElement);
        }

        RecordComponentElement component = null;
        int componentIdx = 0;

        for (RecordComponentElement c : record.getRecordComponents()) {
            if (c.getSimpleName().equals(componentName)) {
                component = c;
                break;
            }
            componentIdx++;
        }

        if (component == null) {
            //erroneous state(?), ignore:
            return Collections.singleton(forElement);
        }

        Set<Element> result = new HashSet<>();

        result.add(component);
        result.add(component.getAccessor());

        for (Element el : record.getEnclosedElements()) {
            if (el.getKind() == ElementKind.FIELD && el.getSimpleName().equals(componentName)) {
                result.add(el);
                break;
            }
        }

        ExecutableElement canonicalConstructor = recordCanonicalConstructor(record);
        if (canonicalConstructor != null && componentIdx < canonicalConstructor.getParameters().size()) {
            result.add(canonicalConstructor.getParameters().get(componentIdx));
        }

        return result;
    }

    private ExecutableElement recordCanonicalConstructor(TypeElement recordType) {
        Supplier<ExecutableElement> fallback =
                () -> {
                          List<? extends RecordComponentElement> recordComponents = recordType.getRecordComponents();
                          for (ExecutableElement c : ElementFilter.constructorsIn(recordType.getEnclosedElements())) {
                              if (recordComponents.size() == c.getParameters().size()) {
                                  Iterator<? extends RecordComponentElement> componentIt = recordComponents.iterator();
                                  Iterator<? extends VariableElement> parameterIt = c.getParameters().iterator();
                                  boolean componentMatches = true;

                                  while (componentIt.hasNext() && parameterIt.hasNext() && componentMatches) {
                                      TypeMirror componentType = componentIt.next().asType();
                                      TypeMirror parameterType = parameterIt.next().asType();

                                      componentMatches &= info.getTypes().isSameType(componentType, parameterType);
                                  }
                                  if (componentMatches) {
                                      return c;
                                  }
                             }
                          }
                         return null;
                    };
        return ElementFilter.constructorsIn(recordType.getEnclosedElements())
                            .stream()
                            .filter(info.getElements()::isCanonicalConstructor)
                            .findAny()
                            .orElseGet(fallback);
    }

    /**Returns a set of packages that are exported to the current module, including
     * those visible through transitive dependencies.
     *
     * @param module the module for which the transitively exported packages should be computed.
     * @return the set of packages from the given module and its transitive dependencies
     *         that are exported to the current module
     * @since 2.79
     */
    public @NonNull Set<PackageElement> transitivelyExportedPackages(@NonNull ModuleElement module) {
        Parameters.notNull("module", module);
        ModuleElement currentModule = info.getModule();
        List<ModuleElement> todo = new ArrayList<>();
        Set<ModuleElement> seen = new HashSet<>();
        Set<PackageElement> exported = new HashSet<>();

        todo.add(module);

        while (!todo.isEmpty()) {
            ModuleElement currentlyProcessing = todo.remove(todo.size() - 1);

            if (!seen.add(currentlyProcessing)) {
                continue;
            }

            for (ExportsDirective exports : ElementFilter.exportsIn(currentlyProcessing.getDirectives())) {
                if (exports.getTargetModules() != null &&
                    !exports.getTargetModules().contains(currentModule)) {
                    continue;
                }

                exported.add(exports.getPackage());
            }

            for (RequiresDirective requires : ElementFilter.requiresIn(currentlyProcessing.getDirectives())) {
                if (!requires.isTransitive()) {
                    continue;
                }

                todo.add(requires.getDependency());
            }
        }

        return exported;
    }

    // private implementation --------------------------------------------------

    private static final Set<Modifier> NOT_OVERRIDABLE = EnumSet.of(Modifier.STATIC, Modifier.FINAL, Modifier.PRIVATE);
    
    private List<? extends ExecutableElement> findUnimplementedMethods(TypeElement impl, TypeElement element, boolean includeDefaults) {
        List<ExecutableElement> undef = new ArrayList<>();
        Types types = JavacTypes.instance(ctx);
        com.sun.tools.javac.code.Types implTypes = com.sun.tools.javac.code.Types.instance(ctx);
        DeclaredType implType = (DeclaredType)impl.asType();
        if (element.getKind().isInterface() || element.getModifiers().contains(Modifier.ABSTRACT)) {
            for (Element e : element.getEnclosedElements()) {
                if (e.getKind() != ElementKind.METHOD) {
                    continue;
                }
                if (element.getKind().isInterface()) {
                    // as of JDK9 interafce can contain static methods.
                    if (e.getModifiers().contains(Modifier.STATIC)) {
                        continue;
                        /*
                    } else if (e.getModifiers().contains(Modifier.DEFAULT) && !includeDefaults) {
                        continue;
                        */
                    } 
                } else if (!e.getModifiers().contains(Modifier.ABSTRACT)) {
                    continue;
                }
                ExecutableElement ee = (ExecutableElement)e;
                ExecutableElement eeImpl = (ExecutableElement)getImplementationOf(ee, impl);
                
                if (eeImpl == null) {
                    if (implTypes.asSuper((Type)implType, (Symbol)ee.getEnclosingElement()) != null) {
                        undef.add(ee);
                    }
                } else if (impl != element && implTypes.asSuper((Type)implType, (Symbol)ee.getEnclosingElement()) != null) {
                    if (eeImpl == ee) {
                        undef.add(ee);
                    } else if (includeDefaults && eeImpl.getModifiers().contains(Modifier.DEFAULT)) {
                        undef.add((ExecutableElement)eeImpl);
                    }
                }
            }
        }
        for (TypeMirror t : types.directSupertypes(element.asType())) {
            for (ExecutableElement ee : findUnimplementedMethods(impl, (TypeElement) ((DeclaredType) t).asElement(), includeDefaults)) {
                //check if "the same" method has already been added:
                boolean exists = false;
                ExecutableType eeType = (ExecutableType)types.asMemberOf(implType, ee);
                for (ExecutableElement existing : undef) {
                    if (existing.getSimpleName().contentEquals(ee.getSimpleName())) {
                        ExecutableType existingType = (ExecutableType)types.asMemberOf(implType, existing);
                        if (types.isSubsignature(existingType, eeType)) {
                            TypeMirror existingReturnType = existingType.getReturnType();
                            TypeMirror eeReturnType = eeType.getReturnType();
                            MethodSymbol msExisting = ((MethodSymbol)existing);
                            if (!types.isSubtype(existingReturnType, eeReturnType)) {
                                if (types.isSubtype(eeReturnType, existingReturnType)) {
                                    undef.remove(existing);
                                    undef.add(ee);
                                } else if (existingReturnType.getKind() == TypeKind.DECLARED && eeReturnType.getKind() == TypeKind.DECLARED) {
                                    Env<AttrContext> env = Enter.instance(ctx).getClassEnv((TypeSymbol)impl);
                                    DeclaredType subType = env != null ? findCommonSubtype((DeclaredType)existingReturnType, (DeclaredType)eeReturnType, env) : null;
                                    if (subType != null) {
                                        undef.remove(existing);
                                        MethodSymbol ms = msExisting.clone((Symbol)impl);
                                        Type mt = implTypes.createMethodTypeWithReturn(ms.type, (Type)subType);
                                        ms.type = mt;
                                        undef.add(ms);
                                    }
                                }
                            } else if (!msExisting.overrides((MethodSymbol)ee, (TypeSymbol)impl, implTypes, true)) {
                                // newly added does not override the old one, BUT 
                                // 1/ the old one might have been defined by an abstract class. In that case, only the abstract class' member should prevail
                                // 2/ we are in an abstract class AND 
                                //      both existing and ee are interface methods AND
                                //      a/ neither of them is default -> select one of them, discard the other
                                if (existing.getEnclosingElement().getKind().isClass()) {
                                    exists = true;
                                } else if (element.getKind().isClass()) {
                                    // existing is now known to be an interface
                                    if (ee.getEnclosingElement().getKind().isInterface()) {
                                        if (!existing.getModifiers().contains(Modifier.DEFAULT) &&
                                            !ee.getModifiers().contains(Modifier.DEFAULT)) {
                                            exists = true;
                                        }
                                    }
                                }
                                break;
                            }
                            exists = true;
                            break;
                        }
                    }
                }
                if (!exists) {
                    undef.add(ee);
                }
            }
        }
        // if not defaults, prune the defaults out:
        if (!includeDefaults && element.getKind() == ElementKind.INTERFACE) {
            for (Iterator<ExecutableElement> it = undef.iterator(); it.hasNext();) {
                ExecutableElement ee = it.next();
                if (ee.getModifiers().contains(Modifier.DEFAULT)) {
                    it.remove();
                }
            }
        }
        return undef;
    }
    
    private DeclaredType findCommonSubtype(DeclaredType type1, DeclaredType type2, Env<AttrContext> env) {
        List<DeclaredType> subtypes1 = getSubtypes(type1, env);
        List<DeclaredType> subtypes2 = getSubtypes(type2, env);
        if (subtypes1 == null || subtypes2 == null) return null;
        Types types = info.getTypes();
        for (DeclaredType subtype1 : subtypes1) {
            for (DeclaredType subtype2 : subtypes2) {
                if (types.isSubtype(subtype1, subtype2))
                    return subtype1;
                if (types.isSubtype(subtype2, subtype1))
                    return subtype2;
            }
        }
        return null;
    }
    
    private List<DeclaredType> getSubtypes(DeclaredType baseType, Env<AttrContext> env) {
        LinkedList<DeclaredType> subtypes = new LinkedList<>();
        HashSet<TypeElement> elems = new HashSet<>();
        LinkedList<DeclaredType> bases = new LinkedList<>();
        bases.add(baseType);
        ClassIndex index = info.getClasspathInfo().getClassIndex();
        Types types = info.getTypes();
        Resolve resolve = Resolve.instance(ctx);
        while(!bases.isEmpty()) {
            DeclaredType head = bases.remove();
            TypeElement elem = (TypeElement)head.asElement();
            if (!elems.add(elem))
                continue;
            subtypes.add(head);
            List<? extends TypeMirror> tas = head.getTypeArguments();
            boolean isRaw = !tas.iterator().hasNext();
            Set<ElementHandle<TypeElement>> implementors = index.getElements(ElementHandle.create(elem), EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS), EnumSet.allOf(ClassIndex.SearchScope.class));
            if (implementors == null) return null; //cancelled
            subtypes:
            for (ElementHandle<TypeElement> eh : implementors) {
                TypeElement e = eh.resolve(info);
                if (e != null) {
                    if (resolve.isAccessible(env, (TypeSymbol)e)) {
                        if (isRaw) {
                            DeclaredType dt = types.getDeclaredType(e);
                            bases.add(dt);
                        } else {
                            HashMap<Element, TypeMirror> map = new HashMap<>();
                            TypeMirror sup = e.getSuperclass();
                            if (sup.getKind() == TypeKind.DECLARED && ((DeclaredType)sup).asElement() == elem) {
                                DeclaredType dt = (DeclaredType)sup;
                                Iterator<? extends TypeMirror> ittas = tas.iterator();
                                Iterator<? extends TypeMirror> it = dt.getTypeArguments().iterator();
                                while(it.hasNext() && ittas.hasNext()) {
                                    TypeMirror basetm = ittas.next();
                                    TypeMirror stm = it.next();
                                    if (basetm != stm) {
                                        if (stm.getKind() == TypeKind.TYPEVAR) {
                                            map.put(((TypeVariable)stm).asElement(), basetm);
                                        } else {
                                            continue subtypes;
                                        }
                                    }
                                }
                                if (it.hasNext() != ittas.hasNext()) {
                                    continue subtypes;
                                }
                            } else {
                                for (TypeMirror tm : e.getInterfaces()) {
                                    if (((DeclaredType)tm).asElement() == elem) {
                                        DeclaredType dt = (DeclaredType)tm;
                                        Iterator<? extends TypeMirror> ittas = tas.iterator();
                                        Iterator<? extends TypeMirror> it = dt.getTypeArguments().iterator();
                                        while(it.hasNext() && ittas.hasNext()) {
                                            TypeMirror basetm = ittas.next();
                                            TypeMirror stm = it.next();
                                            if (basetm != stm) {
                                                if (stm.getKind() == TypeKind.TYPEVAR) {
                                                    map.put(((TypeVariable)stm).asElement(), basetm);
                                                } else {
                                                    continue subtypes;
                                                }
                                            }
                                        }
                                        if (it.hasNext() != ittas.hasNext()) {
                                            continue subtypes;
                                        }
                                        break;
                                    }
                                }
                            }
                            bases.add(getDeclaredType(e, map, types));
                        }
                    }
                }
            }
        }
        return subtypes;
    }

    private DeclaredType getDeclaredType(TypeElement e, HashMap<? extends Element, ? extends TypeMirror> map, Types types) {
        List<? extends TypeParameterElement> tpes = e.getTypeParameters();
        TypeMirror[] targs = new TypeMirror[tpes.size()];
        int i = 0;
        for (Iterator<? extends TypeParameterElement> it = tpes.iterator(); it.hasNext();) {
            TypeParameterElement tpe = it.next();
            TypeMirror t = map.get(tpe);
            targs[i++] = t != null ? t : tpe.asType();
        }
        Element encl = e.getEnclosingElement();
        if ((encl.getKind().isClass() || encl.getKind().isInterface()) && !((TypeElement)encl).getTypeParameters().isEmpty())
                return types.getDeclaredType(getDeclaredType((TypeElement)encl, map, types), e, targs);
        return types.getDeclaredType(e, targs);
    }

    private boolean isOverridden(ExecutableElement methodBase, TypeElement origin) {
        Element impl = getImplementationOf(methodBase, origin);
        if (impl == null || impl == methodBase && origin != methodBase.getEnclosingElement()) {
            return false;
        }
        return true;
    }

    private boolean overridesPackagePrivateOutsidePackage(ExecutableElement ee, TypeElement impl) {
        Name elemPackageName = getPackageName(ee);
        Name currentPackageName = getPackageName(impl);
        return !ee.getModifiers().contains(Modifier.PRIVATE)
                && !ee.getModifiers().contains(Modifier.PUBLIC)
                && !ee.getModifiers().contains(Modifier.PROTECTED)
                && !currentPackageName.contentEquals(elemPackageName);
    }

    private Name getPackageName(Element e) {
        while (e.getEnclosingElement().getKind() != ElementKind.PACKAGE) {
            e = e.getEnclosingElement();
        }
        return ((PackageElement) e.getEnclosingElement()).getQualifiedName();
    }
}
