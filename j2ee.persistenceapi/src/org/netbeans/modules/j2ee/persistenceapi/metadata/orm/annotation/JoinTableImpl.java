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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.j2ee.persistenceapi.metadata.orm.annotation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.type.TypeMirror;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.AnnotationParser;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.ArrayValueHandler;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.ParseResult;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.*;

public class JoinTableImpl implements JoinTable {

    private final ParseResult parseResult;

    public JoinTableImpl(final AnnotationModelHelper helper, AnnotationMirror annotation) {
        AnnotationParser parser = AnnotationParser.create(helper);
        ArrayValueHandler joinColumnHandler = new ArrayValueHandler() {
            public Object handleArray(List<AnnotationValue> arrayMembers) {
                List<JoinColumn> result = new ArrayList<JoinColumn>();
                for (AnnotationValue arrayMember : arrayMembers) {
                    AnnotationMirror joinColumnAnnotation = (AnnotationMirror)arrayMember.getValue();
                    result.add(new JoinColumnImpl(helper, joinColumnAnnotation));
                }
                return result;
            }
        };
        TypeMirror joinColumnType = helper.resolveType("javax.persistence.JoinColumn"); // NOI18N
        parser.expectAnnotationArray("joinColumn", joinColumnType, joinColumnHandler, parser.defaultValue(Collections.emptyList())); // NOI18N
        parser.expectAnnotationArray("inverseJoinColumn", joinColumnType, joinColumnHandler, parser.defaultValue(Collections.emptyList())); // NOI18N
        parseResult = parser.parse(annotation);
    }

    public void setName(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String getName() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setCatalog(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String getCatalog() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setSchema(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String getSchema() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setJoinColumn(int index, JoinColumn value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public JoinColumn getJoinColumn(int index) {
        @SuppressWarnings("unchecked") // can this be avoided?
        List<JoinColumn> result = parseResult.get("joinColumn", List.class); // NOI18N
        return result.get(index);
    }

    public int sizeJoinColumn() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setJoinColumn(JoinColumn[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public JoinColumn[] getJoinColumn() {
        @SuppressWarnings("unchecked") // can this be avoided?
        List<JoinColumn> result = parseResult.get("joinColumn", List.class); // NOI18N
        return result.toArray(new JoinColumn[result.size()]);
    }

    public int addJoinColumn(JoinColumn value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int removeJoinColumn(JoinColumn value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public JoinColumn newJoinColumn() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setInverseJoinColumn(int index, JoinColumn value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public JoinColumn getInverseJoinColumn(int index) {
        @SuppressWarnings("unchecked") // can this be avoided?
        List<JoinColumn> result = parseResult.get("inverseJoinColumn", List.class); // NOI18N
        return result.get(index);
    }

    public int sizeInverseJoinColumn() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setInverseJoinColumn(JoinColumn[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public JoinColumn[] getInverseJoinColumn() {
        @SuppressWarnings("unchecked") // can this be avoided?
        List<JoinColumn> result = parseResult.get("inverseJoinColumn", List.class); // NOI18N
        return result.toArray(new JoinColumn[result.size()]);
    }

    public int addInverseJoinColumn(JoinColumn value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int removeInverseJoinColumn(JoinColumn value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setUniqueConstraint(int index, UniqueConstraint value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public UniqueConstraint getUniqueConstraint(int index) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int sizeUniqueConstraint() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setUniqueConstraint(UniqueConstraint[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public UniqueConstraint[] getUniqueConstraint() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int addUniqueConstraint(UniqueConstraint value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int removeUniqueConstraint(UniqueConstraint value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public UniqueConstraint newUniqueConstraint() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }
}
