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
