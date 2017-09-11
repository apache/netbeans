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
import java.util.List;
import java.util.Map;
import java.util.Stack;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.*;
import org.netbeans.modules.j2ee.persistenceapi.metadata.orm.annotation.AttributesHelper.PropertyHandler;

public class AttributesImpl implements Attributes, PropertyHandler {

    private final AnnotationModelHelper helper;
    private  AttributesHelper attrHelper;
    private EmbeddedId embeddedId;
    private final List<Id> idList = new ArrayList<Id>();
    private final List<Id> derivedIdList = new ArrayList<Id>();
    private final List<Version> versionList = new ArrayList<Version>();
    private final List<Basic> basicList = new ArrayList<Basic>();
    private final List<OneToOne> oneToOneList = new ArrayList<OneToOne>();
    private final List<OneToMany> oneToManyList = new ArrayList<OneToMany>();
    private final List<ManyToOne> manyToOneList = new ArrayList<ManyToOne>();
    private final List<ManyToMany> manyToManyList = new ArrayList<ManyToMany>();

    public AttributesImpl(EntityImpl entity) {
        this(entity.getRoot(), entity.getTypeElement());
    }

    public AttributesImpl(MappedSuperclassImpl mappedSuperclass) {
        this(mappedSuperclass.getRoot(), mappedSuperclass.getTypeElement());
    }

    private AttributesImpl(EntityMappingsImpl mappings, TypeElement typeElement) {
        this.helper = mappings.getHelper();
        Stack<TypeElement> hierarchy = new Stack<TypeElement>();

        hierarchy.add(typeElement);
        for (; typeElement != null;) {
            TypeMirror supMirror = typeElement.getSuperclass();
            if (supMirror != null && supMirror.getKind() == TypeKind.DECLARED) {
                DeclaredType superclassDeclaredType = (DeclaredType) supMirror;
                typeElement = (TypeElement) superclassDeclaredType.asElement();
            } else {
                break;
            }
            Map<String, ? extends AnnotationMirror> annByType = helper.getAnnotationsByType(typeElement.getAnnotationMirrors());
            if (annByType.get("javax.persistence.Entity") != null || annByType.get("javax.persistence.MappedSuperclass") != null) { // NOI18N
                hierarchy.add(typeElement);
            }
        }
        while (!hierarchy.empty()) {
            typeElement = hierarchy.pop();
            attrHelper = new AttributesHelper(helper, typeElement, this);
            attrHelper.parse();
        }
    }

    public boolean hasFieldAccess() {
        return attrHelper.hasFieldAccess();
    }

    @Override
    public void handleProperty(Element element, String propertyName) {
        Map<String, ? extends AnnotationMirror> annByType = helper.getAnnotationsByType(element.getAnnotationMirrors());
        if (EntityMappingsUtilities.isTransient(annByType, element.getModifiers())) {
            return;
        }

        AnnotationMirror oneToOneAnnotation = annByType.get("javax.persistence.OneToOne"); // NOI18N
        AnnotationMirror oneToManyAnnotation = annByType.get("javax.persistence.OneToMany"); // NOI18N
        AnnotationMirror manyToOneAnnotation = annByType.get("javax.persistence.ManyToOne"); // NOI18N
        AnnotationMirror manyToManyAnnotation = annByType.get("javax.persistence.ManyToMany"); // NOI18N
        AnnotationMirror embeddedIdAnnotation = annByType.get("javax.persistence.EmbeddedId"); // NOI18N

        AnnotationMirror derivedIdAnnotation = null;

        if (oneToOneAnnotation != null) {
            oneToOneList.add(new OneToOneImpl(helper, element, oneToOneAnnotation, propertyName, annByType));
            //it may also be part of derived id
            derivedIdAnnotation = annByType.get("javax.persistence.Id"); // NOI18N
        } else if (oneToManyAnnotation != null) {
            oneToManyList.add(new OneToManyImpl(helper, element, oneToManyAnnotation, propertyName, annByType));
        } else if (manyToOneAnnotation != null) {
            manyToOneList.add(new ManyToOneImpl(helper, element, manyToOneAnnotation, propertyName, annByType));
            //it may also be part of derived id
            derivedIdAnnotation = annByType.get("javax.persistence.Id"); // NOI18N
        } else if (manyToManyAnnotation != null) {
            manyToManyList.add(new ManyToManyImpl(helper, element, manyToManyAnnotation, propertyName, annByType));
        } else {
            // not a relationship field
            if (embeddedIdAnnotation != null) {
                embeddedId = new EmbeddedIdImpl(propertyName);
            } else {
                AnnotationMirror versionAnnotation = annByType.get("javax.persistence.Version"); // NOI18N
                AnnotationMirror idAnnotation = annByType.get("javax.persistence.Id"); // NOI18N
                AnnotationMirror columnAnnotation = annByType.get("javax.persistence.Column"); // NOI18N
                AnnotationMirror temporalAnnotation = annByType.get("javax.persistence.Temporal"); // NOI18N
                String temporal = temporalAnnotation != null ? EntityMappingsUtilities.getTemporalType(helper, temporalAnnotation) : null;
                Column column = new ColumnImpl(helper, columnAnnotation, propertyName.toUpperCase()); // NOI18N
                if (idAnnotation != null) {
                    AnnotationMirror generatedValueAnnotation = annByType.get("javax.persistence.GeneratedValue"); // NOI18N
                    GeneratedValue genValue = generatedValueAnnotation != null ? new GeneratedValueImpl(helper, generatedValueAnnotation) : null;
                    idList.add(new IdImpl(propertyName, genValue, column, temporal));
                } else if (versionAnnotation != null) {
                    versionList.add(new VersionImpl(propertyName, column, temporal));
                } else {
                    AnnotationMirror basicAnnotation = annByType.get("javax.persistence.Basic"); // NOI18N
                    basicList.add(new BasicImpl(helper, basicAnnotation, propertyName, column, temporal));
                }
            }
        }
        //
        if (derivedIdAnnotation != null) {
            AnnotationMirror columnAnnotation = annByType.get("javax.persistence.Column"); // NOI18N, TODO some annotations may not be valid for derived id and it may  not be requires, also need to separate from usual id later(just like embedded ids) to find if exist
            AnnotationMirror temporalAnnotation = annByType.get("javax.persistence.Temporal"); // NOI18N
            String temporal = temporalAnnotation != null ? EntityMappingsUtilities.getTemporalType(helper, temporalAnnotation) : null;
            Column column = new ColumnImpl(helper, columnAnnotation, propertyName.toUpperCase()); // NOI18N
            AnnotationMirror generatedValueAnnotation = annByType.get("javax.persistence.GeneratedValue"); // NOI18N
            GeneratedValue genValue = generatedValueAnnotation != null ? new GeneratedValueImpl(helper, generatedValueAnnotation) : null;
            IdImpl id = new IdImpl(propertyName, genValue, column, temporal);
            idList.add(id);
            derivedIdList.add(id);
        }
    }

    @Override
    public void setId(int index, Id value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public Id getId(int index) {
        return idList.get(index);
    }

    @Override
    public int sizeId() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public void setId(Id[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public Id[] getId() {
        return idList.toArray(new Id[idList.size()]);
    }

    @Override
    public int addId(Id value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public int removeId(Id value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public Id newId() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public Id getDerivedId(int index) {
        return derivedIdList.get(index);
    }

    public int sizeDerivedId() {
        return derivedIdList.size(); // NOI18N
    }

    public Id[] getDerivedId() {
        return derivedIdList.toArray(new Id[derivedIdList.size()]);
    }

    @Override
    public void setEmbeddedId(EmbeddedId value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public EmbeddedId getEmbeddedId() {
        return embeddedId;
    }

    @Override
    public EmbeddedId newEmbeddedId() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public void setBasic(int index, Basic value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public Basic getBasic(int index) {
        return basicList.get(index);
    }

    @Override
    public int sizeBasic() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public void setBasic(Basic[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public Basic[] getBasic() {
        return basicList.toArray(new Basic[basicList.size()]);
    }

    @Override
    public int addBasic(Basic value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public int removeBasic(Basic value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public Basic newBasic() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public void setVersion(int index, Version value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public Version getVersion(int index) {
        return versionList.get(index);
    }

    @Override
    public int sizeVersion() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public void setVersion(Version[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public Version[] getVersion() {
        return versionList.toArray(new Version[versionList.size()]);
    }

    @Override
    public int addVersion(Version value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public int removeVersion(Version value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public Version newVersion() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public void setManyToOne(int index, ManyToOne value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public ManyToOne getManyToOne(int index) {
        return manyToOneList.get(index);
    }

    @Override
    public int sizeManyToOne() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public void setManyToOne(ManyToOne[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public ManyToOne[] getManyToOne() {
        return manyToOneList.toArray(new ManyToOne[manyToOneList.size()]);
    }

    @Override
    public int addManyToOne(ManyToOne value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public int removeManyToOne(ManyToOne value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public ManyToOne newManyToOne() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public void setOneToMany(int index, OneToMany value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public OneToMany getOneToMany(int index) {
        return oneToManyList.get(index);
    }

    @Override
    public int sizeOneToMany() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public void setOneToMany(OneToMany[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public OneToMany[] getOneToMany() {
        return oneToManyList.toArray(new OneToMany[oneToManyList.size()]);
    }

    @Override
    public int addOneToMany(OneToMany value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public int removeOneToMany(OneToMany value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public OneToMany newOneToMany() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public void setOneToOne(int index, OneToOne value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public OneToOne getOneToOne(int index) {
        return oneToOneList.get(index);
    }

    @Override
    public int sizeOneToOne() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public void setOneToOne(OneToOne[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public OneToOne[] getOneToOne() {
        return oneToOneList.toArray(new OneToOne[oneToOneList.size()]);
    }

    @Override
    public int addOneToOne(OneToOne value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public int removeOneToOne(OneToOne value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public OneToOne newOneToOne() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public void setManyToMany(int index, ManyToMany value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public ManyToMany getManyToMany(int index) {
        return manyToManyList.get(index);
    }

    @Override
    public int sizeManyToMany() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public void setManyToMany(ManyToMany[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public ManyToMany[] getManyToMany() {
        return manyToManyList.toArray(new ManyToMany[manyToManyList.size()]);
    }

    @Override
    public int addManyToMany(ManyToMany value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public int removeManyToMany(ManyToMany value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public ManyToMany newManyToMany() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public void setEmbedded(int index, Embedded value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public Embedded getEmbedded(int index) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public int sizeEmbedded() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public void setEmbedded(Embedded[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public Embedded[] getEmbedded() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public int addEmbedded(Embedded value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public int removeEmbedded(Embedded value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public Embedded newEmbedded() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public void setTransient(int index, Transient value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public Transient getTransient(int index) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public int sizeTransient() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public void setTransient(Transient[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public Transient[] getTransient() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public int addTransient(Transient value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public int removeTransient(Transient value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public Transient newTransient() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }
}
