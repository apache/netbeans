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

package org.netbeans.modules.editor.java;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.SimpleTypeVisitor6;

import com.sun.source.util.TreePath;

import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.SourceUtils;

/**
 *
 * @author Dusan Balek
 */
public class AutoImport extends SimpleTypeVisitor6<Void, Void> {

    private static final String CAPTURED_WILDCARD = "<captured wildcard>"; //NOI18N

    private CompilationInfo info;
    private StringBuilder builder;
    private TreePath path;
    private Set<String> importedTypes = new HashSet<String>();

    private AutoImport(CompilationInfo info) {
        this.info = info;
    }

    public static AutoImport get(CompilationInfo info) {
        return new AutoImport(info);
    }
    
    public static CharSequence resolveImport(CompilationInfo info, TreePath treePath, TypeMirror type) {
        AutoImport imp = new AutoImport(info);
        return imp.resolveImport(treePath, type);
    }
    
    public CharSequence resolveImport(TreePath treePath, TypeMirror type) {
        this.builder = new StringBuilder();
        this.path = treePath;
        visit(type, null);
        return builder;
    }

    public Set<String> getAutoImportedTypes() {
        return importedTypes;
    }
    
    @Override
    public Void defaultAction(TypeMirror type, Void p) {
        builder.append(type);
        return null;
    }
        
    @Override
    public Void visitArray(ArrayType type, Void p) {
        visit(type.getComponentType());
        builder.append("[]"); //NOI18N
        return null;
    }
    
    @Override
    public Void visitDeclared(DeclaredType type, Void p) {
        TypeElement element = (TypeElement)type.asElement();
        String name = element.getQualifiedName().toString();
        ElementKind kind = element.getEnclosingElement().getKind();
        if (kind.isClass() || kind.isInterface() || kind == ElementKind.PACKAGE) {
            try {
                String s = SourceUtils.resolveImport(info, path, name);
                int idx = s.indexOf('.');
                if (idx < 0) {
                    importedTypes.add(name);
                } else {
                    importedTypes.add(name.substring(0, name.length() - s.length() + idx));
                }
                name = s;
            } catch (Exception e) {
                Logger.getLogger("global").log(Level.INFO, null, e); //NOI18N
            }
        }
        builder.append(name);
        Iterator<? extends TypeMirror> it = type.getTypeArguments().iterator();
        if (it.hasNext()) {
            builder.append('<'); //NOI18N
            while(it.hasNext()) {
                visit(it.next());
                if (it.hasNext())
                    builder.append(", "); //NOI18N
            }
            builder.append('>'); //NOI18N
        }
        return null;
    }
    
    @Override
    public Void visitTypeVariable(TypeVariable type, Void p) {
        Element e = type.asElement();
        if (e != null) {
            CharSequence name = e.getSimpleName();
            if (!CAPTURED_WILDCARD.contentEquals(name)) {
                builder.append(name);
                return null;
            }
        }
        builder.append("?"); //NOI18N
        TypeMirror bound = type.getLowerBound();
        if (bound != null && bound.getKind() != TypeKind.NULL) {
            builder.append(" super "); //NOI18N
            visit(bound);
        } else {
            bound = type.getUpperBound();
            if (bound != null && bound.getKind() != TypeKind.NULL) {
                builder.append(" extends "); //NOI18N
                visit(bound);
            }
        }
        return null;
    }

    @Override
    public Void visitWildcard(WildcardType type, Void p) {
        builder.append("?"); //NOI18N
        TypeMirror bound = type.getSuperBound();
        if (bound == null) {
            bound = type.getExtendsBound();
            if (bound != null) {
                builder.append(" extends "); //NOI18N
                if (bound.getKind() == TypeKind.WILDCARD)
                    bound = ((WildcardType)bound).getSuperBound();
                visit(bound);
            }
        } else {
            builder.append(" super "); //NOI18N
            visit(bound);
        }
        return null;
    }

    @Override
    public Void visitError(ErrorType type, Void p) {
        Element e = type.asElement();
        if (e instanceof TypeElement) {
            TypeElement te = (TypeElement)e;
            builder.append(te.getSimpleName());            
        }
        return null;
    }
}
