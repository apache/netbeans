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

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.TypeSymbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.ClassType;
import com.sun.tools.javac.code.Type.TypeVar;
import com.sun.tools.javac.code.TypeMetadata;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.code.Types.DefaultTypeVisitor;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.ReferenceType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.UnionType;
import javax.lang.model.type.WildcardType;
import org.netbeans.api.annotations.common.NonNull;

/**
 * Represents a handle for {@link TypeMirror} which can be kept and later resolved
 * by another javac. The Javac {@link TypeMirror}s are valid only in the single
 * {@link javax.tools.JavaCompiler.CompilationTask} or single run of the
 * {@link org.netbeans.api.java.source.CancellableTask}. If the client needs to
 * keep a reference to the {@link TypeMirror} and use it in the other CancellableTask
 * he has to serialize it into the {@link TypeMirrorHandle}.
 * <div class="nonnormative"> 
 * <p>
 ** Typical usage of TypeMirrorHandle is:
 * </p>
 * <pre>{@code
 * final TypeMirrorHandle[] typeMirrorHandle = new TypeMirrorHandle[1];
 * javaSource.runCompileControlTask(new CancellableTask<CompilationController>() {
 *     public void run(CompilationController compilationController) {
 *         parameter.toPhase(Phase.RESOLVED);
 *         CompilationUnitTree cu = compilationController.getTree ();
 *         TypeMirror type = getInterestingType(cu);
 *         typeMirrorHandle[0] = TypeMirrorHandle.create (type);
 *    }
 * },priority);
 *
 * otherJavaSource.runCompileControlTask(new CancellableTask<CompilationController>() {
 *     public void run(CompilationController compilationController) {
 *         parameter.toPhase(Phase.RESOLVED);
 *         TypeMirror type = typeMirrorHandle[0].resolve (compilationController);
 *         ....
 *    }
 * },priority);
 * }</pre>
 * </div>
 * Currently, not all the {@link TypeMirror} {@link TypeKind kinds} are supported by handle.
 * The unsupported {@link TypeKind kinds} are: {@link TypeKind#EXECUTABLE}, {@link TypeKind#OTHER},
 * and {@link TypeKind#PACKAGE}.
 *
 * @author Jan Lahoda, Dusan Balek
 */
public final class TypeMirrorHandle<T extends TypeMirror> {

    private final TypeKind kind;
    private final ElementHandle<? extends Element> element;
    private List<TypeMirrorHandle<? extends TypeMirror>> typeMirrors;
    
    private TypeMirrorHandle(TypeKind kind, ElementHandle<? extends Element> element, List<TypeMirrorHandle<? extends TypeMirror>> typeArguments) {
        this.kind = kind;
        this.element = element;
        this.typeMirrors = typeArguments;
    }
    
    /**
     * Factory method for creating {@link TypeMirrorHandle}.
     * @param tm for which the {@link TypeMirrorHandle} should be created.
     * Not all the {@link TypeMirror} {@link TypeKind kinds} are currently supported.
     * The unsupported {@link TypeKind kinds} are: {@link TypeKind#EXECUTABLE}, {@link TypeKind#OTHER},
     * and {@link TypeKind#PACKAGE}.
     * @return a new {@link TypeMirrorHandle}
     * @throws IllegalArgumentException if the {@link TypeMirror} is of an unsupported
     * {@link TypeKind}.
     */
    public static @NonNull <T extends TypeMirror> TypeMirrorHandle<T> create(@NonNull T tm) {
        return create(tm, new HashMap<TypeMirror, TypeMirrorHandle>());
    }
    
    private static <T extends TypeMirror> TypeMirrorHandle<T> create(T tm, Map<TypeMirror, TypeMirrorHandle> map) {
        TypeMirrorHandle<T> handle = map.get(tm);
        if (handle != null)
            return handle.typeMirrors == null ? handle : (TypeMirrorHandle<T>)handle.typeMirrors.get(0);
        map.put(tm, new TypeMirrorHandle(null, null, null));
        TypeKind kind = tm.getKind();
        ElementHandle<? extends Element> element = null;
        List<TypeMirrorHandle<? extends TypeMirror>> typeMirrors = null;
        switch (kind) {
            case BOOLEAN:
            case BYTE:
            case CHAR:
            case DOUBLE:
            case FLOAT:
            case INT:
            case LONG:
            case NONE:
            case NULL:
            case SHORT:
            case VOID:
                break;
            case DECLARED:
                DeclaredType dt = (DeclaredType)tm;
                boolean compoundType = ((Type) tm).tsym != null && (((Type) tm).tsym.flags_field & Flags.COMPOUND) != 0;
                boolean genericOuter;
                List<? extends TypeMirror> targs;
                if (!compoundType) {
                    TypeElement te = (TypeElement)dt.asElement();
                    element = ElementHandle.create(te);
                    Element encl = te.getEnclosingElement();
                    genericOuter = (encl.getKind().isClass() || encl.getKind().isInterface()) && !((TypeElement)encl).getTypeParameters().isEmpty() && !te.getModifiers().contains(Modifier.STATIC);
                    if (te.getTypeParameters().isEmpty() && !genericOuter)
                        break;
                    targs = dt.getTypeArguments();
                } else {
                    genericOuter = false;
                    TypeElement fakeClass = (TypeElement) dt.asElement();
                    List<TypeMirror> temp = new ArrayList<TypeMirror>(fakeClass.getInterfaces().size() + 1);
                    temp.add(fakeClass.getSuperclass());
                    temp.addAll(fakeClass.getInterfaces());
                    targs = temp;
                }
                if (!targs.isEmpty()) {
                    TypeMirror targ = targs.get(0);
                    if (targ.getKind() == TypeKind.TYPEVAR) {
                        TypeParameterElement tpe = (TypeParameterElement)((TypeVariable)targ).asElement();
                        if (tpe.getGenericElement() == dt.asElement())
                            break;
                    }
                }
                if (genericOuter) {
                    typeMirrors = new ArrayList<TypeMirrorHandle<? extends TypeMirror>>(targs.size() + 1);
                    typeMirrors.add(create(dt.getEnclosingType(), map));
                } else {
                    typeMirrors = new ArrayList<TypeMirrorHandle<? extends TypeMirror>>(targs.size());
                }
                for (TypeMirror ta : targs)
                    typeMirrors.add(create(ta, map));
                break;
            case ARRAY:
                typeMirrors = Collections.<TypeMirrorHandle<? extends TypeMirror>>singletonList(create(((ArrayType)tm).getComponentType(), map));
                break;
            case TYPEVAR:
                TypeVariable tv = (TypeVariable)tm;
                element = ElementHandle.create(tv.asElement());
                typeMirrors = new ArrayList<TypeMirrorHandle<? extends TypeMirror>>();
                typeMirrors.add(tv.getLowerBound() != null ? create(tv.getLowerBound(), map) : null);
                typeMirrors.add(tv.getUpperBound() != null ? create(tv.getUpperBound(), map) : null);
                break;
            case WILDCARD:
                WildcardType wt = (WildcardType)tm;
                typeMirrors = new ArrayList<TypeMirrorHandle<? extends TypeMirror>>();
                typeMirrors.add(wt.getExtendsBound() != null ? create(wt.getExtendsBound(), map) : null);
                typeMirrors.add(wt.getSuperBound() != null ? create(wt.getSuperBound(), map) : null);
                break;
            case ERROR:
                ErrorType et = (ErrorType)tm;
                element = ElementHandle.create(et.asElement());
                break;
            case UNION:
                typeMirrors = new ArrayList<TypeMirrorHandle<? extends TypeMirror>>();
                for (TypeMirror alternative : ((UnionType) tm).getAlternatives()) {
                    typeMirrors.add(create(alternative, map));
                }
                break;
            case INTERSECTION:
                typeMirrors = new ArrayList<TypeMirrorHandle<? extends TypeMirror>>();
                for (TypeMirror alternative : ((IntersectionType) tm).getBounds()) {
                    typeMirrors.add(create(alternative, map));
                }
                break;
            default:
                throw new IllegalArgumentException("Currently unsupported TypeKind: " + tm.getKind());
        }
        handle = new TypeMirrorHandle(kind, element, typeMirrors);
        map.get(tm).typeMirrors = Collections.singletonList(handle);
        return handle;
    }
    
    /**
     * Resolves an {@link TypeMirror} from the {@link TypeMirrorHandle}.
     * @param info representing the {@link javax.tools.JavaCompiler.CompilationTask}
     * in which the {@link TypeMirror} should be resolved.
     * @return resolved subclass of {@link TypeMirror} or null if the type cannot be
     * resolved in this {@link javax.tools.JavaCompiler.CompilationTask}.
     */
    public T resolve(@NonNull CompilationInfo info) {
        return resolve(info, new HashMap<TypeMirrorHandle, PlaceholderType>());
    }
    
    private T resolve(CompilationInfo info, Map<TypeMirrorHandle, PlaceholderType> map) {
        if (kind == null) {
            TypeMirrorHandle handle = typeMirrors.get(0);
            PlaceholderType pt = map.get(handle);
            if (pt == null) {
                pt = new PlaceholderType();
                map.put(handle, pt);
            }
            return pt.delegate != null ? (T)pt.delegate : (T)pt;
        }
        switch (kind) {
            case BOOLEAN:
            case BYTE:
            case CHAR:
            case DOUBLE:
            case FLOAT:
            case INT:
            case LONG:
            case SHORT:
                return (T)info.getTypes().getPrimitiveType(kind);
            case NONE:
            case VOID:
                return (T)info.getTypes().getNoType(kind);
            case NULL:
                return (T)info.getTypes().getNullType();
            case DECLARED:
                Types t = Types.instance(info.impl.getJavacTask().getContext());
                if (element == null) {
                    //compound type:
                    com.sun.tools.javac.util.List<Type> resolvedBounds = com.sun.tools.javac.util.List.nil();
                    for (TypeMirrorHandle bound : typeMirrors) {
                        TypeMirror resolved = bound.resolve(info, map);
                        if (resolved == null) {
                            return null;
                        }
                        resolvedBounds = resolvedBounds.prepend((Type) resolved);
                    }

                    Type ct = t.makeIntersectionType(resolvedBounds.reverse());
                    ct.getTypeArguments(); //initialize typarams_field
                    return (T) ct;
                }
                TypeElement te = (TypeElement)element.resolve(info);
                if (te == null)
                    return null;
                if (typeMirrors == null)
                    return (T)te.asType();
                Iterator<TypeMirrorHandle<? extends TypeMirror>> it = typeMirrors.iterator();
                Element encl = te.getEnclosingElement();
                boolean genericOuter = (encl.getKind().isClass() || encl.getKind().isInterface()) && !((TypeElement)encl).getTypeParameters().isEmpty() && !te.getModifiers().contains(Modifier.STATIC);
                TypeMirror outer = null;
                if (genericOuter) {
                    outer = it.hasNext() ? it.next().resolve(info, map) : null;
                    if (outer == null || outer.getKind() != TypeKind.DECLARED)
                        return null;
                }
                List<TypeMirror> resolvedTypeArguments = new ArrayList<TypeMirror>();
                while(it.hasNext()) {
                    TypeMirror resolved = it.next().resolve(info, map);
                    if (resolved == null)
                        return null;
                    resolvedTypeArguments.add(resolved);
                }
                DeclaredType dt = outer != null ? info.getTypes().getDeclaredType((DeclaredType)outer, te, resolvedTypeArguments.toArray(new TypeMirror[0]))
                        : info.getTypes().getDeclaredType(te, resolvedTypeArguments.toArray(new TypeMirror[0]));
                t.supertype((Type)dt); //initialize supertype_field
                t.interfaces((Type)dt); //initialize interfaces_field
                PlaceholderType pt = map.get(this);
                if (pt != null) {
                    pt.delegate = (Type)dt;
                    new Visitor().visitClassType((ClassType)dt, null);
                }
                return (T)dt;
            case ARRAY:
                TypeMirror resolved = typeMirrors.get(0).resolve(info, map);
                if (resolved == null)
                    return null;
                ArrayType at = info.getTypes().getArrayType(resolved);
                pt = map.get(this);
                if (pt != null) {
                    pt.delegate = (Type)at;
                    new Visitor().visitArrayType((Type.ArrayType)at, null);
                }
                return (T)at;
            case TYPEVAR:
                Element e = element.resolve(info);
                if (!(e instanceof TypeSymbol))
                    return null;
                TypeMirrorHandle<? extends TypeMirror> lBound = typeMirrors.get(0);
                TypeMirror lowerBound = lBound != null ? lBound.resolve(info, map) : null;
                TypeMirrorHandle<? extends TypeMirror> uBound = typeMirrors.get(1);
                TypeMirror upperBound = uBound != null ? uBound.resolve(info, map) : null;
                TypeVar tv = new TypeVar((TypeSymbol)e, (Type)upperBound, (Type)lowerBound);
                pt = map.get(this);
                if (pt != null) {
                    pt.delegate = tv;
                    new Visitor().visitTypeVar(tv, null);
                }
                return (T)tv;
            case WILDCARD:
                TypeMirrorHandle<? extends TypeMirror> eBound = typeMirrors.get(0);
                TypeMirror extendsBound = eBound != null ? eBound.resolve(info, map) : null;
                TypeMirrorHandle<? extends TypeMirror> sBound = typeMirrors.get(1);
                TypeMirror superBound = sBound != null ? sBound.resolve(info, map) : null;
                WildcardType wt = info.getTypes().getWildcardType(extendsBound, superBound);
                pt = map.get(this);
                if (pt != null) {
                    pt.delegate = (Type)wt;
                    new Visitor().visitWildcardType((Type.WildcardType)wt, null);
                }
                return (T)wt;
            case ERROR:
                e = element.resolve(info);
                if (e == null) {
                    String[] signatures = element.getSignature();
                    assert signatures.length == 1;
                    Context context = info.impl.getJavacTask().getContext();
                    try {
                        return (T) new Type.ErrorType(Names.instance(context).table.fromString(signatures[0]), Symtab.instance(context).rootPackage, Type.noType);
                    } catch (NoSuchMethodError err) {
                        // ErrorType constructor signature in vanilla javac differ from the corresponding signature in nb-javac.
                        // TODO: Remove reflection once the nb-javac is fixed.
                        try {
                            Constructor<Type.ErrorType> constructor = Type.ErrorType.class.getConstructor(Name.class, Symbol.class, Type.class);
                            return (T) constructor.newInstance(Names.instance(context).table.fromString(signatures[0]), Symtab.instance(context).rootPackage, Type.noType);
                        } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException ex) {
                            return null;
                        }
                    }
                }
                if (!(e instanceof ClassSymbol))
                    return null;
                return (T)new Type.ErrorType((ClassSymbol)e, Type.noType);
            case UNION:
                com.sun.tools.javac.util.List<Type> resolvedAlternatives = com.sun.tools.javac.util.List.nil();
                for (TypeMirrorHandle alternative : typeMirrors) {
                    TypeMirror resolvedAlternative = alternative.resolve(info, map);
                    if (resolvedAlternative == null) {
                        return null;
                    }
                    resolvedAlternatives = resolvedAlternatives.prepend((Type) resolvedAlternative);
                }

                t = Types.instance(info.impl.getJavacTask().getContext());
                Type lub = t.lub(resolvedAlternatives);

                if (!lub.hasTag(TypeTag.CLASS)) return null;

                return (T) new Type.UnionClassType((ClassType) lub, resolvedAlternatives);
            case INTERSECTION:
                ListBuffer<Type> resolvedBounds = new ListBuffer<Type>();
                for (TypeMirrorHandle alternative : typeMirrors) {
                    TypeMirror resolvedAlternative = alternative.resolve(info, map);
                    if (resolvedAlternative == null) {
                        return null;
                    }
                    resolvedBounds = resolvedBounds.append((Type) resolvedAlternative);
                }
                
                t = Types.instance(info.impl.getJavacTask().getContext());
                
                return (T) t.makeIntersectionType(resolvedBounds.toList());
            default:
                throw new IllegalStateException("Internal error: unknown TypeHandle kind: " + kind);
        }
    }
    
    /**
     * Returns the {@link TypeKind} of this handle,
     * it is the kind of the {@link TypeMirror} from which the handle
     * was created.
     * @return {@link TypeKind}
     */
    public @NonNull TypeKind getKind () {
        return this.kind;
    }
    
    ElementHandle<? extends Element> getElementHandle() {
        return element;
    }
    
    private static class PlaceholderType extends Type implements ReferenceType {

        private Type delegate = null;
        
        public PlaceholderType() {
            this(com.sun.tools.javac.util.List.nil());
        }       

        public PlaceholderType(com.sun.tools.javac.util.List<TypeMetadata> md) {
            super(null, md);
        }       

        @Override
        public TypeTag getTag() {
            return TypeTag.UNKNOWN;
        }

        @Override
        public Type cloneWithMetadata(com.sun.tools.javac.util.List<TypeMetadata> md) {
            PlaceholderType out = new PlaceholderType(md);
            out.delegate = delegate;
            return out;
        }
    }
    
    private static class Visitor extends DefaultTypeVisitor<Void, Void> {

        public Void visitType(Type t, Void s) {
            return null;
        }

        @Override
        public Void visitArrayType(Type.ArrayType t, Void s) {
            if (t.elemtype != null)
                t.elemtype.accept(this, s);
            return null;
        }

        @Override
        public Void visitClassType(ClassType t, Void s) {
            if (t.supertype_field != null)
                t.supertype_field.accept(this, s);
            if (t.interfaces_field != null)
                for (com.sun.tools.javac.util.List<Type> l = t.interfaces_field; l.nonEmpty(); l = l.tail)
                    l.head.accept(this, s);
            if (t.typarams_field != null)
                for (com.sun.tools.javac.util.List<Type> l = t.typarams_field; l.nonEmpty(); l = l.tail)
                    l.head.accept(this, s);
            return null;
        }

        @Override
        public Void visitTypeVar(TypeVar t, Void s) {
            if (t.getUpperBound() instanceof PlaceholderType)
                t.setUpperBound(((PlaceholderType)t.getUpperBound()).delegate);
            else if (t.getUpperBound() != null)
                t.getUpperBound().accept(this, s);
            if (t.lower instanceof PlaceholderType)
                t.lower = ((PlaceholderType)t.lower).delegate;
            else if (t.lower != null)
                t.lower.accept(this, s);
            return null;
        }

        @Override
        public Void visitWildcardType(Type.WildcardType t, Void s) {
            if (t.type instanceof PlaceholderType)
                t.type = ((PlaceholderType)t.type).delegate;
            else if (t.type != null)
                t.type.accept(this, s);
            if (t.getUpperBound() != null)
                t.getUpperBound().accept(this, s);
            return null;
        }
    }
}
