/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.editor.java;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.SimpleTypeVisitor6;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.modules.csl.api.Modifier;


/**
 * This was copied from org.netbeans.modules.editor.java.Utilities.
 * see http://www.netbeans.org/issues/show_bug.cgi?id=149168
 *
 * @author Dusan Balek, Petr Hejl
 */
public final class Utilities {

    private static final String CAPTURED_WILDCARD = "<captured wildcard>"; // NOI18N
    
    private static final String UNKNOWN = "<unknown>"; // NOI18N
    
    private static final Map<Character, String> NATIVE_TYPES = new HashMap<Character, String>();
    
    static {
        NATIVE_TYPES.put(Character.valueOf('B'), "byte"); // NOI18N
        NATIVE_TYPES.put(Character.valueOf('C'), "char"); // NOI18N
        NATIVE_TYPES.put(Character.valueOf('D'), "double"); // NOI18N
        NATIVE_TYPES.put(Character.valueOf('F'), "float"); // NOI18N
        NATIVE_TYPES.put(Character.valueOf('I'), "int"); // NOI18N
        NATIVE_TYPES.put(Character.valueOf('J'), "long"); // NOI18N
        NATIVE_TYPES.put(Character.valueOf('S'), "short"); // NOI18N
        NATIVE_TYPES.put(Character.valueOf('Z'), "boolean"); // NOI18N
    }

    public static String translateClassLoaderTypeName(String type) {
        if (type.length() < 1) {
            return type;
        }
        char start = type.charAt(0);
        if ('L' == start && type.charAt(type.length() - 1) == ';') { // NOI18N
            return type.substring(1, type.length() - 1);
        } else if ('[' == start) { // NOI18N
            if (type.length() < 2) {
                throw new IllegalArgumentException("Not a correct type: " + type);
            }
            return translateClassLoaderTypeName(type.substring(1)) + "[]"; // NOI18N
        } else if (type.length() == 1) {
            String result = NATIVE_TYPES.get(Character.valueOf(start));
            if (result != null) {
                return result;
            }
            return type;
        }

        return type;
    }

    public static Set<javax.lang.model.element.Modifier> reflectionModifiersToModel(int modifiers) {
        Set<javax.lang.model.element.Modifier> ret = new HashSet<javax.lang.model.element.Modifier>();

        if (java.lang.reflect.Modifier.isAbstract(modifiers)) {
            ret.add(javax.lang.model.element.Modifier.ABSTRACT);
        }
        if (java.lang.reflect.Modifier.isFinal(modifiers)) {
            ret.add(javax.lang.model.element.Modifier.FINAL);
        }
        if (java.lang.reflect.Modifier.isNative(modifiers)) {
            ret.add(javax.lang.model.element.Modifier.NATIVE);
        }
        if (java.lang.reflect.Modifier.isStatic(modifiers)) {
            ret.add(javax.lang.model.element.Modifier.STATIC);
        }
        if (java.lang.reflect.Modifier.isStrict(modifiers)) {
            ret.add(javax.lang.model.element.Modifier.STRICTFP);
        }
        if (java.lang.reflect.Modifier.isSynchronized(modifiers)) {
            ret.add(javax.lang.model.element.Modifier.SYNCHRONIZED);
        }
        if (java.lang.reflect.Modifier.isTransient(modifiers)) {
            ret.add(javax.lang.model.element.Modifier.TRANSIENT);
        }
        if (java.lang.reflect.Modifier.isVolatile(modifiers)) {
            ret.add(javax.lang.model.element.Modifier.VOLATILE);
        }
        if (java.lang.reflect.Modifier.isPrivate(modifiers)) {
            ret.add(javax.lang.model.element.Modifier.PRIVATE);
        } else if (java.lang.reflect.Modifier.isProtected(modifiers)) {
            ret.add(javax.lang.model.element.Modifier.PROTECTED);
        } else if (java.lang.reflect.Modifier.isPublic(modifiers)) {
            ret.add(javax.lang.model.element.Modifier.PUBLIC);
        }

        return ret;
    }

    public static Set<javax.lang.model.element.Modifier> gsfModifiersToModel(Set<Modifier> modifiers,
            javax.lang.model.element.Modifier defaultModifier) {

        Set<javax.lang.model.element.Modifier> ret = new HashSet<javax.lang.model.element.Modifier>();

        if (modifiers.contains(Modifier.STATIC)) {
            ret.add(javax.lang.model.element.Modifier.STATIC);
        }
        if (modifiers.contains(Modifier.PRIVATE)) {
            ret.add(javax.lang.model.element.Modifier.PRIVATE);
        } else if (modifiers.contains(Modifier.PROTECTED)) {
            ret.add(javax.lang.model.element.Modifier.PROTECTED);
        } else if (modifiers.contains(Modifier.PUBLIC)) {
            ret.add(javax.lang.model.element.Modifier.PUBLIC);
        } else if (defaultModifier != null) {
            ret.add(defaultModifier);
        }

        return ret;
    }

    public static Set<Modifier> modelModifiersToGsf(Set<javax.lang.model.element.Modifier> modifiers) {

        Set<Modifier> ret = new LinkedHashSet<Modifier>();

        if (modifiers.contains(javax.lang.model.element.Modifier.STATIC)) {
            ret.add(Modifier.STATIC);
        }
        if (modifiers.contains(javax.lang.model.element.Modifier.PRIVATE)) {
            ret.add(Modifier.PRIVATE);
        } else if (modifiers.contains(javax.lang.model.element.Modifier.PROTECTED)) {
            ret.add(Modifier.PROTECTED);
        } else if (modifiers.contains(javax.lang.model.element.Modifier.PUBLIC)) {
            ret.add(Modifier.PUBLIC);
        }

        return ret;
    }

    public static CharSequence getTypeName(TypeMirror type, boolean fqn) {
        return getTypeName(type, fqn, false);
    }

    public static CharSequence getClassName(TypeMirror type) {
        assert type != null;
        return new ClassNameVisitor().visit(type, null);
    }

    public static CharSequence getTypeName(TypeMirror type, boolean fqn, boolean varArg) {
        if (type == null) {
            return ""; //NOI18N
        }
        return new TypeNameVisitor(varArg).visit(type, fqn);
    }

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
        public StringBuilder visitError(ErrorType t, Boolean p) {
            Element e = t.asElement();
            if (e instanceof TypeElement) {
                TypeElement te = (TypeElement)e;
                return DEFAULT_VALUE.append((p ? te.getQualifiedName() : te.getSimpleName()).toString());
            }
            return DEFAULT_VALUE;
        }
    }

    private static class ClassNameVisitor extends SimpleTypeVisitor6<StringBuilder,Void> {

        private ClassNameVisitor() {
            super(new StringBuilder());
        }


        @Override
        public StringBuilder defaultAction(TypeMirror t, Void p) {
            return DEFAULT_VALUE.append(t);
        }

        @Override
        public StringBuilder visitDeclared(DeclaredType t, Void p) {
            Element e = t.asElement();
            if (e instanceof TypeElement) {
                TypeElement te = (TypeElement) e;
                return DEFAULT_VALUE.append(te.getQualifiedName().toString());
            } else {
                return DEFAULT_VALUE.append(UNKNOWN); //NOI18N
            }
        }

        @Override
        public StringBuilder visitError(ErrorType t, Void p) {
            Element e = t.asElement();
            if (e instanceof TypeElement) {
                TypeElement te = (TypeElement) e;
                return DEFAULT_VALUE.append(te.getQualifiedName().toString());
            }
            return DEFAULT_VALUE;
        }
    }
}
