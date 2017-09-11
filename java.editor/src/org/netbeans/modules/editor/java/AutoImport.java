/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
                if (bound.getKind() == TypeKind.TYPEVAR)
                    bound = ((TypeVariable)bound).getLowerBound();
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
