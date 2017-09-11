/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.metrics.hints;

import java.util.Collection;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.type.UnionType;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.SimpleTypeVisitor7;

/**
 * Collects all DeclaredTypes from the type tree into the Collection passed as a parameter.
 * The Collector does no efforts to minimize the number of collection operations.
 * 
 * @author sdedic
 */
class DeclaredTypeCollector extends SimpleTypeVisitor7<Void, Collection<DeclaredType>> {
    static final TypeVisitor INSTANCE = new DeclaredTypeCollector();
    
    @Override
    public Void visitDeclared(DeclaredType t, Collection<DeclaredType> p) {
        p.add(t);
        return DEFAULT_VALUE;
    }

    @Override
    public Void visitArray(ArrayType t, Collection<DeclaredType> p) {
        visit(t.getComponentType(), p);
        return DEFAULT_VALUE;
    }

    /*
    @Override
    public Void visitIntersection(IntersectionType t, Collection<DeclaredType> p) {
        for (TypeMirror tm : t.getBounds()) {
            visit(tm, p);
        }
        return DEFAULT_VALUE;
    }

    @Override
    public Void visitAnnotated(AnnotatedType t, Collection<DeclaredType> p) {
        for (AnnotationMirror am : t.getAnnotations()) {
            visit(am.getAnnotationType(), p);
            // PENDING
        }
        return super.visitAnnotated(t, p); //To change body of generated methods, choose Tools | Templates.
    }
    */

    @Override
    public Void visitTypeVariable(TypeVariable t, Collection<DeclaredType> p) {
        if (t.getLowerBound() != null) {
            visit(t.getLowerBound(), p);
        }
        if (t.getUpperBound() != null) {
            visit(t.getUpperBound(), p);
        }
        return DEFAULT_VALUE;
    }

    @Override
    public Void visitWildcard(WildcardType t, Collection<DeclaredType> p) {
        if (t.getExtendsBound() != null) {
            visit(t.getExtendsBound(), p);
        }
        if (t.getSuperBound() != null) {
            visit(t.getSuperBound(), p);
        }
        return DEFAULT_VALUE;
    }

    @Override
    public Void visitUnion(UnionType t, Collection<DeclaredType> p) {
        for (TypeMirror tm : t.getAlternatives()) {
            visit(tm, p);
        }
        return DEFAULT_VALUE;
    }
    
}
