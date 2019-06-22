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
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.CapturedType;
import com.sun.tools.javac.code.Type.ClassType;
import com.sun.tools.javac.code.Type.TypeVar;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.model.JavacTypes;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.SimpleTypeVisitor6;
import org.netbeans.api.annotations.common.CheckReturnValue;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;

/**Various utilities related to the {@link TypeMirror}s.
 *
 * @see javax.lang.model.util.Types
 *
 * @since 0.6
 *
 * @author Jan Lahoda
 */
public final class TypeUtilities {

    private final CompilationInfo info;

    /** Creates a new instance of CommentUtilities */
    TypeUtilities(final CompilationInfo info) {
        assert info != null;
        this.info = info;
    }

    /**Check if type t1 can be cast to t2.
     * 
     * @param t1 cast from type
     * @param t2 cast to type
     * @return true if and only if type t1 can be cast to t2 without a compile time error
     * @throws IllegalArgumentException if the 't1' is of {@link TypeKind#EXECUTABLE EXACUTABLE},
     *         {@link TypeKind#PACKAGE PACKAGE}, {@link TypeKind#NONE NONE}, or {@link TypeKind#OTHER OTHER} kind
     * 
     * @since 0.6
     */
    public boolean isCastable(TypeMirror t1, TypeMirror t2) {
        switch(t1.getKind()) {
            case EXECUTABLE:
            case PACKAGE:
            case NONE:
            case OTHER:
                throw new IllegalArgumentException();
            default:
                return Types.instance(info.impl.getJavacTask().getContext()).isCastable((Type) t1, (Type) t2);
        }
    }
    
    /**
     * Substitute all occurrences of a type in 'from' with the corresponding type
     * in 'to' in 'type'. 'from' and 'to' lists have to be of the same length.
     * 
     * @param type in which the types should be substituted
     * @param from types to substitute
     * @param to   substitute to types
     * @return type corresponding to input 'type' with all references to any type from 'from'
     *         replaced with a corresponding type from 'to'
     * @throws IllegalArgumentException if the 'from' and 'to' lists are not of the same length
     * @since 0.36
     */
    public TypeMirror substitute(TypeMirror type, List<? extends TypeMirror> from, List<? extends TypeMirror> to) {
        if (from.size() != to.size()) {
            throw new IllegalArgumentException();
        }
        com.sun.tools.javac.util.List<Type> l1 = com.sun.tools.javac.util.List.nil();
        for (TypeMirror typeMirror : from)
            l1 = l1.prepend((Type)typeMirror);
        com.sun.tools.javac.util.List<Type> l2 = com.sun.tools.javac.util.List.nil();
        for (TypeMirror typeMirror : to)
            l2 = l2.prepend((Type)typeMirror);
        return Types.instance(info.impl.getJavacTask().getContext()).subst((Type)type, l1, l2);
    }

    /**
     * Find the type of the method descriptor associated to the functional interface.
     * 
     * @param origin functional interface type
     * @return associated method descriptor type or <code>null</code> if the <code>origin</code> is not a functional interface.
     * @since 0.112
     */
    public ExecutableType getDescriptorType(DeclaredType origin) {
        Types types = Types.instance(info.impl.getJavacTask().getContext());
        if (types.isFunctionalInterface(((Type)origin).tsym)) {
            Type dt = types.findDescriptorType((Type)origin);
            if (dt != null && dt.getKind() == TypeKind.EXECUTABLE) {
                return (ExecutableType)dt;
            }
        }
        return null;
    }
    
   /**Get textual representation of the given type.
     *
     * @param type to print
     * @param options allows to specify various adjustments to the output text
     * @return textual representation of the given type
     * @since 0.62
     */
    public @NonNull @CheckReturnValue CharSequence getTypeName(@NullAllowed TypeMirror type, @NonNull TypeNameOptions... options) {
	if (type == null)
            return ""; //NOI18N
        Set<TypeNameOptions> opt = EnumSet.noneOf(TypeNameOptions.class);
        opt.addAll(Arrays.asList(options));
        return new TypeNameVisitor(opt.contains(TypeNameOptions.PRINT_AS_VARARG)).visit(type, opt.contains(TypeNameOptions.PRINT_FQN)).toString();
    }

    /**
     * Returns a TypeMirror which can be represented in a source as a type declarator. The returned TypeMirror,
     * if not erroneous, can be used as type of a variable or a method's return type. The method will attempt to
     * infer proper wildcards or bounds.
     * <p/>
     * If the type could be represented in source, the method returns a type of {@link TypeKind#ERROR}.
     * 
     * @param type the type to be polished
     * @return the representable type or an error type
     * @since 2.17
     */
    public TypeMirror getDenotableType(TypeMirror type) {
        Types types = Types.instance(info.impl.getJavacTask().getContext());
        if (type == null) {
            return types.createErrorType(
                    (Type)JavacTypes.instance(info.impl.getJavacTask().getContext()).getNoType(TypeKind.NONE)
            );
        }
        Type inType = (Type)type;
        TypeKind tk = type.getKind();
        if (tk == TypeKind.ERROR) {
            inType = (Type)info.getTrees().getOriginalType((ErrorType)type);
        } else if (tk == TypeKind.NONE || tk == TypeKind.OTHER) {
            return types.createErrorType(inType);
        }
        Type t = (Type) resolveCapturedType(info, type);
        if (t == null) {
            return types.createErrorType(inType);
        }
        if (!t.isErroneous()) {
            if (!checkDenotable(t)) {
                return types.createErrorType(t);
            }
        }
        if (t.hasTag(TypeTag.BOT)) {
            return types.createErrorType(t);
        } else {
            return t;
        }
    }
    
    boolean checkDenotable(Type t) {
        return denotableChecker.visit(t, null);
    }
        // where

    /** diamondTypeChecker: A type visitor that descends down the given type looking for non-denotable
     *  types. The visit methods return false as soon as a non-denotable type is encountered and true
     *  otherwise.
     */
    private static final Types.SimpleVisitor<Boolean, Void> denotableChecker = new Types.SimpleVisitor<Boolean, Void>() {
        @Override
        public Boolean visitType(Type t, Void s) {
            return true;
        }
        @Override
        public Boolean visitClassType(ClassType t, Void s) {
            if (t.isUnion() || t.isIntersection()) {
                return false;
            }
            for (Type targ : t.allparams()) {
                if (!visit(targ, s)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public Boolean visitTypeVar(TypeVar t, Void s) {
            /* Any type variable mentioned in the inferred type must have been declared as a type parameter
              (i.e cannot have been produced by inference (18.4))
            */
            return (t.tsym.flags() & Flags.SYNTHETIC) == 0;
        }

        @Override
        public Boolean visitCapturedType(CapturedType t, Void s) {
            /* Any type variable mentioned in the inferred type must have been declared as a type parameter
              (i.e cannot have been produced by capture conversion (5.1.10))
            */
            return false;
        }


        @Override
        public Boolean visitArrayType(Type.ArrayType t, Void s) {
            return visit(t.elemtype, s);
        }

        @Override
        public Boolean visitWildcardType(Type.WildcardType t, Void s) {
            return visit(t.type, s);
        }
    };

    //from java.hints/src/org/netbeans/modules/java/hints/errors/Utilities.java:
    private static TypeMirror resolveCapturedType(CompilationInfo info, TypeMirror tm) {
        if (tm == null) {
            return tm;
        }
        if (tm.getKind() == TypeKind.ERROR) {
            tm = info.getTrees().getOriginalType((ErrorType) tm);
        }
        TypeMirror type = resolveCapturedTypeInt(info, tm);
        if (type == null) {
            return tm;
        }
        if (type.getKind() == TypeKind.WILDCARD) {
            TypeMirror tmirr = ((WildcardType) type).getExtendsBound();
            if (tmirr != null)
                return tmirr;
            else { //no extends, just '?'
                TypeElement te = info.getElements().getTypeElement("java.lang.Object"); // NOI18N
                return te == null ? null : te.asType();
            }
                
        }
        
        return type;
    }
    
    /**
     * Note: may return {@code null}, if an intersection type is encountered, to indicate a 
     * real type cannot be created.
     */
    private static TypeMirror resolveCapturedTypeInt(CompilationInfo info, TypeMirror tm) {
        if (tm == null) return tm;
        
        TypeMirror orig = SourceUtils.resolveCapturedType(tm);

        if (orig != null) {
            tm = orig;
        }
        
        if (tm.getKind() == TypeKind.WILDCARD) {
            TypeMirror extendsBound = ((WildcardType) tm).getExtendsBound();
            TypeMirror superBound = ((WildcardType) tm).getSuperBound();
            if (extendsBound != null || superBound != null) {
                TypeMirror rct = resolveCapturedTypeInt(info, extendsBound != null ? extendsBound : superBound);
                if (rct != null) {
                    switch (rct.getKind()) {
                        case WILDCARD:
                            return rct;
                        case ARRAY:
                        case DECLARED:
                        case ERROR:
                        case TYPEVAR:
                        case OTHER:
                            return info.getTypes().getWildcardType(
                                    extendsBound != null ? rct : null, superBound != null ? rct : null);
                    }
                } else {
                    // propagate failure out of all wildcards
                    return null;
                }
            }
        } else if (tm.getKind() == TypeKind.INTERSECTION) {
            return null;
        }
        
        if (tm.getKind() == TypeKind.DECLARED) {
            DeclaredType dt = (DeclaredType) tm;
            List<TypeMirror> typeArguments = new LinkedList<TypeMirror>();
            
            for (TypeMirror t : dt.getTypeArguments()) {
                TypeMirror targ = resolveCapturedTypeInt(info, t);
                if (targ == null) {
                    // bail out, if the type parameter is a wildcard, it's probably not possible
                    // to create a proper parametrized type from it
                    if (t.getKind() == TypeKind.WILDCARD || t.getKind() == TypeKind.INTERSECTION) {
                        return null;
                    }
                    // use rawtype
                    typeArguments.clear();
                    break;
                }
                typeArguments.add(targ);
            }
            
            final TypeMirror enclosingType = dt.getEnclosingType();
            if (enclosingType.getKind() == TypeKind.DECLARED) {
                return info.getTypes().getDeclaredType((DeclaredType) enclosingType, (TypeElement) dt.asElement(), typeArguments.toArray(new TypeMirror[0]));
            } else {
                if (dt.asElement() == null) return dt;
                return info.getTypes().getDeclaredType((TypeElement) dt.asElement(), typeArguments.toArray(new TypeMirror[0]));
            }
        }

        if (tm.getKind() == TypeKind.ARRAY) {
            ArrayType at = (ArrayType) tm;
            TypeMirror tm2 = resolveCapturedTypeInt(info, at.getComponentType());
            return info.getTypes().getArrayType(tm2 != null ? tm2 : tm);
        }
        
        return tm;
    }

    /**Options for the {@link #getTypeName(javax.lang.model.type.TypeMirror, org.netbeans.api.java.source.TypeUtilities.TypeNameOptions[]) } method.
     * @since 0.62
     */
    public enum TypeNameOptions {
        /**
         * Print declared types as fully qualified names.
         */
        PRINT_FQN,
        /**
         * Print "..." instead of "[]".
         */
        PRINT_AS_VARARG;
    }

    private static final String UNKNOWN = "<unknown>"; //NOI18N
    private static final String CAPTURED_WILDCARD = "<captured wildcard>"; //NOI18N
    private static class TypeNameVisitor extends SimpleTypeVisitor6<StringBuilder,Boolean> {

        private boolean varArg;
        private boolean insideCapturedWildcard = false;

        private TypeNameVisitor(boolean varArg) {
            super(new StringBuilder());
            this.varArg = varArg;
        }

        @Override
        public StringBuilder defaultAction(TypeMirror t, Boolean p) {
            return DEFAULT_VALUE.append(t);
        }

        @Override
        public StringBuilder visitDeclared(DeclaredType t, Boolean p) {
            Element e = t.asElement();
            if (e instanceof TypeElement) {
                TypeElement te = (TypeElement)e;
                DEFAULT_VALUE.append((p ? te.getQualifiedName() : te.getSimpleName()).toString());
                Iterator<? extends TypeMirror> it = t.getTypeArguments().iterator();
                if (it.hasNext()) {
                    DEFAULT_VALUE.append("<"); //NOI18N
                    while(it.hasNext()) {
                        visit(it.next(), p);
                        if (it.hasNext())
                            DEFAULT_VALUE.append(", "); //NOI18N
                    }
                    DEFAULT_VALUE.append(">"); //NOI18N
                }
                return DEFAULT_VALUE;
            } else {
                return DEFAULT_VALUE.append(UNKNOWN); //NOI18N
            }
        }

        @Override
        public StringBuilder visitArray(ArrayType t, Boolean p) {
            boolean isVarArg = varArg;
            varArg = false;
            visit(t.getComponentType(), p);
            return DEFAULT_VALUE.append(isVarArg ? "..." : "[]"); //NOI18N
        }

        @Override
        public StringBuilder visitTypeVariable(TypeVariable t, Boolean p) {
            Element e = t.asElement();
            if (e != null) {
                String name = e.getSimpleName().toString();
                if (!CAPTURED_WILDCARD.equals(name))
                    return DEFAULT_VALUE.append(name);
            }
            DEFAULT_VALUE.append("?"); //NOI18N
            if (!insideCapturedWildcard) {
                insideCapturedWildcard = true;
                TypeMirror bound = t.getLowerBound();
                if (bound != null && bound.getKind() != TypeKind.NULL) {
                    DEFAULT_VALUE.append(" super "); //NOI18N
                    visit(bound, p);
                } else {
                    bound = t.getUpperBound();
                    if (bound != null && bound.getKind() != TypeKind.NULL) {
                        DEFAULT_VALUE.append(" extends "); //NOI18N
                        if (bound.getKind() == TypeKind.TYPEVAR)
                            bound = ((TypeVariable)bound).getLowerBound();
                        visit(bound, p);
                    }
                }
                insideCapturedWildcard = false;
            }
            return DEFAULT_VALUE;
        }

        @Override
        public StringBuilder visitWildcard(WildcardType t, Boolean p) {
            int len = DEFAULT_VALUE.length();
            DEFAULT_VALUE.append("?"); //NOI18N
            TypeMirror bound = t.getSuperBound();
            if (bound == null) {
                bound = t.getExtendsBound();
                if (bound != null) {
                    DEFAULT_VALUE.append(" extends "); //NOI18N
                    if (bound.getKind() == TypeKind.WILDCARD)
                        bound = ((WildcardType)bound).getSuperBound();
                    visit(bound, p);
                } else if (len == 0) {
                    bound = SourceUtils.getBound(t);
                    if (bound != null && (bound.getKind() != TypeKind.DECLARED || !((TypeElement)((DeclaredType)bound).asElement()).getQualifiedName().contentEquals("java.lang.Object"))) { //NOI18N
                        DEFAULT_VALUE.append(" extends "); //NOI18N
                        visit(bound, p);
                    }
                }
            } else {
                DEFAULT_VALUE.append(" super "); //NOI18N
                visit(bound, p);
            }
            return DEFAULT_VALUE;
        }

        @Override
        public StringBuilder visitIntersection(IntersectionType t, Boolean p) {
            Iterator<? extends TypeMirror> it = t.getBounds().iterator();
            while (it.hasNext()) {
                visit(it.next(), p);
                if (it.hasNext()) {
                    DEFAULT_VALUE.append(" & ");
                }
            }
            return DEFAULT_VALUE;
        }

        @Override
        public StringBuilder visitError(ErrorType t, Boolean p) {
            Element e = t.asElement();
            if (e instanceof TypeElement) {
                TypeElement te = (TypeElement)e;
                return DEFAULT_VALUE.append((p ? te.getQualifiedName() : te.getSimpleName()).toString());
            }
            return DEFAULT_VALUE;
        }

        @Override
        public StringBuilder visitUnknown(TypeMirror t, Boolean p) {
            return DEFAULT_VALUE.append("<unknown>");
        }

    }
}
