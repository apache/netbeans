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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2ee.persistence.util;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.j2ee.core.api.support.java.GenerationUtils;

/**
 * A helper class for generating equals, hashCode and toString methods for entity classes.
 */
//XXX all methods are extracted from JavaPersistenceGenerator, consider generalizing 
// them and putting to GenerationUtils.
public class EntityMethodGenerator {

    private final WorkingCopy copy;
    private final GenerationUtils genUtils;
    private final TypeElement scope;
    
    public EntityMethodGenerator(WorkingCopy copy, GenerationUtils genUtils, TypeElement scope) {
        this.copy = copy;
        this.genUtils = genUtils;
        this.scope = scope;
    }

    public MethodTree createHashCodeMethod(List<VariableTree> fields) {
        StringBuilder body = new StringBuilder(20 + fields.size() * 30);
        body.append("{"); // NOI18N
        body.append("int hash = 0;"); // NOI18N
        for (VariableTree field : fields) {
            body.append(createHashCodeLineForField(field));
        }
        body.append("return hash;"); // NOI18N
        body.append("}"); // NOI18N
        TreeMaker make = copy.getTreeMaker();
        // XXX Javadoc
        return make.Method(make.Modifiers(EnumSet.of(Modifier.PUBLIC),
                Collections.singletonList(genUtils.createAnnotation("java.lang.Override"))), "hashCode",
                make.PrimitiveType(TypeKind.INT), Collections.<TypeParameterTree>emptyList(),
                Collections.<VariableTree>emptyList(), Collections.<ExpressionTree>emptyList(), body.toString(), null);
    }

    private String createHashCodeLineForField(VariableTree field) {
        Name fieldName = field.getName();
        Tree fieldType = field.getType();
        if (fieldType.getKind() == Tree.Kind.PRIMITIVE_TYPE) {
            if (((PrimitiveTypeTree) fieldType).getPrimitiveTypeKind() == TypeKind.BOOLEAN) {
                return "hash += (" + fieldName + " ? 1 : 0"; // NOI18N
            }
            return "hash += (int)" + fieldName + ";"; // NOI18N
        }
        return "hash += (" + fieldName + " != null ? " + fieldName + ".hashCode() : 0);"; // NOI18N
    }

    public  MethodTree createEqualsMethod(String simpleClassName, List<VariableTree> fields) {
        StringBuilder body = new StringBuilder(50 + fields.size() * 30);
        body.append("{"); // NOI18N
        body.append("// TODO: Warning - this method won't work in the case the id fields are not set\n"); // NOI18N
        body.append("if (!(object instanceof "); // NOI18N
        body.append(simpleClassName + ")) {return false;}"); // NOI18N
        body.append(simpleClassName + " other = (" + simpleClassName + ")object;"); // NOI18N
        for (VariableTree field : fields) {
            body.append(createEqualsLineForField(field));
        }
        body.append("return true;"); // NOI18N
        body.append("}"); // NOI18N
        TreeMaker make = copy.getTreeMaker();
        // XXX Javadoc
        return make.Method(make.Modifiers(EnumSet.of(Modifier.PUBLIC),
                Collections.singletonList(genUtils.createAnnotation("java.lang.Override"))), "equals",
                make.PrimitiveType(TypeKind.BOOLEAN), Collections.<TypeParameterTree>emptyList(),
                Collections.singletonList(genUtils.createVariable(scope, "object", "java.lang.Object")),
                Collections.<ExpressionTree>emptyList(), body.toString(), null);
    }

    private String createEqualsLineForField(VariableTree field) {
        Name fieldName = field.getName();
        Tree fieldType = field.getType();
        if (fieldType.getKind() == Tree.Kind.PRIMITIVE_TYPE) {
            return "if (this." + fieldName + " != other." + fieldName + ") return false;"; // NOI18N
        }
        return "if ((this." + fieldName + " == null && other." + fieldName + " != null) || " + "(this." + fieldName +
                " != null && !this." + fieldName + ".equals(other." + fieldName + "))) return false;"; // NOI18N
    }

    public MethodTree createToStringMethod(String fqn, List<VariableTree> fields) {
        StringBuilder body = new StringBuilder(30 + fields.size() * 30);
        body.append("{"); // NOI18N
        body.append("return \"" + fqn + "[ "); // NOI18N
        for (Iterator<VariableTree> i = fields.iterator(); i.hasNext();) {
            String fieldName = i.next().getName().toString();
            body.append(fieldName + "=\" + " + fieldName + " + \""); //NOI18N
            body.append(i.hasNext() ? ", " : " ]\";"); //NOI18N
        }
        body.append("}"); // NOI18N
        TreeMaker make = copy.getTreeMaker();
        // XXX Javadoc
        return make.Method(make.Modifiers(EnumSet.of(Modifier.PUBLIC),
                Collections.singletonList(genUtils.createAnnotation("java.lang.Override"))), "toString",
                genUtils.createType("java.lang.String", scope), Collections.<TypeParameterTree>emptyList(),
                Collections.<VariableTree>emptyList(), Collections.<ExpressionTree>emptyList(), body.toString(), null);
    }
}
