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

import java.util.List;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.AnnotationParser;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.ArrayValueHandler;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.DefaultProvider;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.ParseResult;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.*;

public class OneToOneImpl implements OneToOne {

    private final String name;
    private final ParseResult parseResult;
    private final JoinTable joinTable;
    private final List<JoinColumn> joinColumnList;
    private final List<PrimaryKeyJoinColumn> pkJoinColumnList;

    public OneToOneImpl(final AnnotationModelHelper helper, final Element element, AnnotationMirror oneToOneAnnotation, String name, Map<String, ? extends AnnotationMirror> annByType) {
        this.name = name;
        AnnotationParser parser = AnnotationParser.create(helper);
        parser.expectClass("targetEntity", new DefaultProvider() { // NOI18N
            public Object getDefaultValue() {
                return EntityMappingsUtilities.getElementTypeName(element);
            }
        });
        parser.expectEnumConstantArray("cascade", helper.resolveType("javax.persistence.CascadeType"), new ArrayValueHandler() { // NOI18N
            public Object handleArray(List<AnnotationValue> arrayMembers) {
                return new CascadeTypeImpl(arrayMembers);
            }
        }, parser.defaultValue(new CascadeTypeImpl()));
        parser.expectEnumConstant("fetch", helper.resolveType("javax.persistence.FetchType"), parser.defaultValue("EAGER")); // NOI18N
        parser.expectPrimitive("optional", Boolean.class, parser.defaultValue(true)); // NOI18N
        parser.expectString("mappedBy", parser.defaultValue("")); // NOI18N
        parseResult = parser.parse(oneToOneAnnotation);

        joinTable = new JoinTableImpl(helper, annByType.get("javax.persistence.JoinTable")); // NOI18N
        joinColumnList = EntityMappingsUtilities.getJoinColumns(helper, annByType);
        pkJoinColumnList = EntityMappingsUtilities.getPrimaryKeyJoinColumns(helper, annByType);
    }

    public void setName(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String getName() {
        return name;
    }

    public void setTargetEntity(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String getTargetEntity() {
        return parseResult.get("targetEntity", String.class); // NOI18N
    }

    public void setFetch(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String getFetch() {
        return parseResult.get("fetch", String.class); // NOI18N
    }

    public void setOptional(boolean value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public boolean isOptional() {
        return parseResult.get("optional", Boolean.class); // NOI18N
    }

    public void setMappedBy(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String getMappedBy() {
        return parseResult.get("mappedBy", String.class); // NOI18N
    }

    public void setPrimaryKeyJoinColumn(int index, PrimaryKeyJoinColumn value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public PrimaryKeyJoinColumn getPrimaryKeyJoinColumn(int index) {
        return pkJoinColumnList.get(index);
    }

    public int sizePrimaryKeyJoinColumn() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setPrimaryKeyJoinColumn(PrimaryKeyJoinColumn[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public PrimaryKeyJoinColumn[] getPrimaryKeyJoinColumn() {
        return pkJoinColumnList.toArray(new PrimaryKeyJoinColumn[pkJoinColumnList.size()]);
    }

    public int addPrimaryKeyJoinColumn(PrimaryKeyJoinColumn value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int removePrimaryKeyJoinColumn(PrimaryKeyJoinColumn value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public PrimaryKeyJoinColumn newPrimaryKeyJoinColumn() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setJoinColumn(int index, JoinColumn value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public JoinColumn getJoinColumn(int index) {
        return joinColumnList.get(index);
    }

    public int sizeJoinColumn() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setJoinColumn(JoinColumn[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public JoinColumn[] getJoinColumn() {
        return joinColumnList.toArray(new JoinColumn[joinColumnList.size()]);
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

    public void setJoinTable(JoinTable value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public JoinTable getJoinTable() {
        return joinTable;
    }

    public JoinTable newJoinTable() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setCascade(CascadeType value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public CascadeType getCascade() {
        return parseResult.get("cascade", CascadeType.class); // NOI18N
    }

    public CascadeType newCascadeType() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }
}
