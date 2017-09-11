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

import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.TypeElement;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.JavaContextListener;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.PersistentObject;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.AnnotationParser;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.ParseResult;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.ValueProvider;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.*;

public class MappedSuperclassImpl extends PersistentObject implements MappedSuperclass, JavaContextListener {

    private final EntityMappingsImpl root;

    // persistent
    private String class2;

    // transient: set to null in javaContextLeft()
    private IdClassImpl idClass;
    private AttributesImpl attributes;
    private String accessType;

    public MappedSuperclassImpl(AnnotationModelHelper helper, EntityMappingsImpl root, TypeElement typeElement) {
        super(helper, typeElement);
        this.root = root;
        helper.addJavaContextListener(this);
        boolean valid = refresh(typeElement);
        assert valid;
    }

    boolean refresh(TypeElement typeElement) {
        class2 = typeElement.getQualifiedName().toString();
        AnnotationModelHelper helper = getHelper();
        Map<String, ? extends AnnotationMirror> annByType = helper.getAnnotationsByType(typeElement.getAnnotationMirrors());
        AnnotationMirror embeddableAnn = annByType.get("javax.persistence.MappedSuperclass"); // NOI18N
        AnnotationMirror entityAcc = annByType.get("javax.persistence.Access"); // NOI18N
        if (entityAcc != null) {
            entityAcc.getElementValues();
            AnnotationParser parser = AnnotationParser.create(helper);
            parser.expect("value", new ValueProvider() {
                @Override
                public Object getValue(AnnotationValue elementValue) {
                    return elementValue.toString();
                }

                @Override
                public Object getDefaultValue() {
                    return null;
                }
            });//NOI18N
            ParseResult parseResult = parser.parse(entityAcc);
            accessType = parseResult.get("value", String.class);
        }
        return embeddableAnn != null;
    }

    EntityMappingsImpl getRoot() {
        return root;
    }

    public void javaContextLeft() {
        attributes = null;
        idClass = null;
    }

    public void setClass2(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String getClass2() {
        return class2;
    }

    public void setAccess(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String getAccess() {
        if (accessType != null && accessType.length()>0) {
            //use access type specified by annotation by default, regardless of later fields/properties annitatons
            return accessType.equals("javax.persistence.AccessType.PROPERTY") ? PROPERTY_ACCESS : FIELD_ACCESS;
        } else {
            return getAttributes().hasFieldAccess() ? FIELD_ACCESS : PROPERTY_ACCESS;
        }
    }

    public void setMetadataComplete(boolean value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public boolean isMetadataComplete() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setDescription(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String getDescription() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setIdClass(IdClass value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public IdClass getIdClass() {
        if (idClass == null) {
            TypeElement typeElement = getTypeElement();
            if (typeElement != null) {
                idClass = EntityMappingsUtilities.getIdClass(getRoot().getHelper(), typeElement);
            }
        }
        return idClass;
    }

    public IdClass newIdClass() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setExcludeDefaultListeners(EmptyType value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public EmptyType getExcludeDefaultListeners() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public EmptyType newEmptyType() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setExcludeSuperclassListeners(EmptyType value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public EmptyType getExcludeSuperclassListeners() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setEntityListeners(EntityListeners value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public EntityListeners getEntityListeners() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public EntityListeners newEntityListeners() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setPrePersist(PrePersist value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public PrePersist getPrePersist() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public PrePersist newPrePersist() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setPostPersist(PostPersist value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public PostPersist getPostPersist() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public PostPersist newPostPersist() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setPreRemove(PreRemove value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public PreRemove getPreRemove() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public PreRemove newPreRemove() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setPostRemove(PostRemove value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public PostRemove getPostRemove() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public PostRemove newPostRemove() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setPreUpdate(PreUpdate value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public PreUpdate getPreUpdate() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public PreUpdate newPreUpdate() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setPostUpdate(PostUpdate value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public PostUpdate getPostUpdate() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public PostUpdate newPostUpdate() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setPostLoad(PostLoad value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public PostLoad getPostLoad() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public PostLoad newPostLoad() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setAttributes(Attributes value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public AttributesImpl getAttributes() {
        if (attributes == null) {
            attributes = new AttributesImpl(this);
        }
        return attributes;
    }

    public Attributes newAttributes() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }
}
