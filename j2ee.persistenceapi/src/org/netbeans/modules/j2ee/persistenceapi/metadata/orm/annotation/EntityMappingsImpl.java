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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationHandler;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationScanner;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.ObjectProvider;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.PersistentObjectManager;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.*;

public class EntityMappingsImpl implements EntityMappings {

    private final AnnotationModelHelper helper;
    private final PersistentObjectManager<EntityImpl> entityManager;
    private final PersistentObjectManager<EmbeddableImpl> embeddableManager;
    private final PersistentObjectManager<MappedSuperclassImpl> mappedSuperclassManager;

    public EntityMappingsImpl(AnnotationModelHelper helper) {
        this.helper = helper;
        entityManager = helper.createPersistentObjectManager(new EntityProvider());
        embeddableManager = helper.createPersistentObjectManager(new EmbeddableProvider());
        mappedSuperclassManager = helper.createPersistentObjectManager(new MappedSuperclassProvider());
    }

    AnnotationModelHelper getHelper() {
        return helper;
    }

    public void setVersion(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String getVersion() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setDescription(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String getDescription() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setPersistenceUnitMetadata(PersistenceUnitMetadata value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public PersistenceUnitMetadata getPersistenceUnitMetadata() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public PersistenceUnitMetadata newPersistenceUnitMetadata() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setPackage(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String getPackage() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setSchema(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String getSchema() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setCatalog(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String getCatalog() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setAccess(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String getAccess() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setSequenceGenerator(int index, SequenceGenerator value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public SequenceGenerator getSequenceGenerator(int index) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int sizeSequenceGenerator() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setSequenceGenerator(SequenceGenerator[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public SequenceGenerator[] getSequenceGenerator() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int addSequenceGenerator(SequenceGenerator value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int removeSequenceGenerator(SequenceGenerator value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public SequenceGenerator newSequenceGenerator() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setTableGenerator(int index, TableGenerator value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public TableGenerator getTableGenerator(int index) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int sizeTableGenerator() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setTableGenerator(TableGenerator[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public TableGenerator[] getTableGenerator() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int addTableGenerator(TableGenerator value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int removeTableGenerator(TableGenerator value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public TableGenerator newTableGenerator() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setNamedQuery(int index, NamedQuery value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public NamedQuery getNamedQuery(int index) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int sizeNamedQuery() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setNamedQuery(NamedQuery[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public NamedQuery[] getNamedQuery() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int addNamedQuery(NamedQuery value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int removeNamedQuery(NamedQuery value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public NamedQuery newNamedQuery() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setNamedNativeQuery(int index, NamedNativeQuery value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public NamedNativeQuery getNamedNativeQuery(int index) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int sizeNamedNativeQuery() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setNamedNativeQuery(NamedNativeQuery[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public NamedNativeQuery[] getNamedNativeQuery() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int addNamedNativeQuery(NamedNativeQuery value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int removeNamedNativeQuery(NamedNativeQuery value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public NamedNativeQuery newNamedNativeQuery() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setSqlResultSetMapping(int index, SqlResultSetMapping value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public SqlResultSetMapping getSqlResultSetMapping(int index) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int sizeSqlResultSetMapping() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setSqlResultSetMapping(SqlResultSetMapping[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public SqlResultSetMapping[] getSqlResultSetMapping() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int addSqlResultSetMapping(SqlResultSetMapping value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int removeSqlResultSetMapping(SqlResultSetMapping value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public SqlResultSetMapping newSqlResultSetMapping() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setMappedSuperclass(int index, MappedSuperclass value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public MappedSuperclass getMappedSuperclass(int index) {
        return getMappedSuperclass()[index];
    }

    public int sizeMappedSuperclass() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setMappedSuperclass(MappedSuperclass[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public MappedSuperclass[] getMappedSuperclass() {
        Collection<MappedSuperclassImpl> mappedSuperclasses = mappedSuperclassManager.getObjects();
        return mappedSuperclasses.toArray(new MappedSuperclass[mappedSuperclasses.size()]);
    }

    public int addMappedSuperclass(MappedSuperclass value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int removeMappedSuperclass(MappedSuperclass value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public MappedSuperclass newMappedSuperclass() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setEntity(int index, Entity value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public Entity getEntity(int index) {
        return getEntity()[index];
    }

    public int sizeEntity() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setEntity(Entity[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public Entity[] getEntity() {
        Collection<EntityImpl> entities = entityManager.getObjects();
        return entities.toArray(new Entity[entities.size()]);
    }

    public int addEntity(Entity value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int removeEntity(Entity value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public Entity newEntity() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setEmbeddable(int index, Embeddable value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public Embeddable getEmbeddable(int index) {
        return getEmbeddable()[index];
    }

    public int sizeEmbeddable() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setEmbeddable(Embeddable[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public Embeddable[] getEmbeddable() {
        Collection<EmbeddableImpl> embeddables = embeddableManager.getObjects();
        return embeddables.toArray(new Embeddable[embeddables.size()]);
    }

    public int addEmbeddable(Embeddable value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int removeEmbeddable(Embeddable value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public Embeddable newEmbeddable() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    private final class EntityProvider implements ObjectProvider<EntityImpl> {

        public List<EntityImpl> createInitialObjects() throws InterruptedException {
            final List<EntityImpl> result = new ArrayList<EntityImpl>();
            helper.getAnnotationScanner().findAnnotations("javax.persistence.Entity", AnnotationScanner.TYPE_KINDS, new AnnotationHandler() { // NOI18N
                public void handleAnnotation(TypeElement type, Element element, AnnotationMirror annotation) {
                    result.add(new EntityImpl(helper, EntityMappingsImpl.this, type));
                }
            });
            return result;
        }

        public List<EntityImpl> createObjects(TypeElement type) {
            if (helper.hasAnnotation(type.getAnnotationMirrors(), "javax.persistence.Entity")) { // NOI18N
                return Collections.singletonList(new EntityImpl(helper, EntityMappingsImpl.this, type));
            }
            return Collections.emptyList();
        }

        public boolean modifyObjects(TypeElement type, List<EntityImpl> objects) {
            assert objects.size() == 1;
            EntityImpl entity = objects.get(0);
            if (!entity.refresh(type)) {
                objects.remove(0);
                return true;
            }
            return false;
        }
    }

    private final class EmbeddableProvider  implements ObjectProvider<EmbeddableImpl> {

        public List<EmbeddableImpl> createInitialObjects() throws InterruptedException {
            final List<EmbeddableImpl> result = new ArrayList<EmbeddableImpl>();
            helper.getAnnotationScanner().findAnnotations("javax.persistence.Embeddable", AnnotationScanner.TYPE_KINDS, new AnnotationHandler() { // NOI18N
                public void handleAnnotation(TypeElement type, Element element, AnnotationMirror annotation) {
                    result.add(new EmbeddableImpl(helper, EntityMappingsImpl.this, type));
                }
            });
            return result;
        }

        public List<EmbeddableImpl> createObjects(TypeElement type) {
            if (helper.hasAnnotation(type.getAnnotationMirrors(), "javax.persistence.Embeddable")) { // NOI18N
                return Collections.singletonList(new EmbeddableImpl(helper, EntityMappingsImpl.this, type));
            }
            return Collections.emptyList();
        }

        public boolean modifyObjects(TypeElement type, List<EmbeddableImpl> objects) {
            assert objects.size() == 1;
            EmbeddableImpl embeddable = objects.get(0);
            if (!embeddable.refresh(type)) {
                objects.remove(0);
                return true;
            }
            return false;
        }
    }

    private final class MappedSuperclassProvider  implements ObjectProvider<MappedSuperclassImpl> {

        public List<MappedSuperclassImpl> createInitialObjects() throws InterruptedException {
            final List<MappedSuperclassImpl> result = new ArrayList<MappedSuperclassImpl>();
            helper.getAnnotationScanner().findAnnotations("javax.persistence.MappedSuperclass", AnnotationScanner.TYPE_KINDS, new AnnotationHandler() { // NOI18N
                public void handleAnnotation(TypeElement type, Element element, AnnotationMirror annotation) {
                    result.add(new MappedSuperclassImpl(helper, EntityMappingsImpl.this, type));
                }
            });
            return result;
        }

        public List<MappedSuperclassImpl> createObjects(TypeElement type) {
            if (helper.hasAnnotation(type.getAnnotationMirrors(), "javax.persistence.MappedSuperclass")) { // NOI18N
                return Collections.singletonList(new MappedSuperclassImpl(helper, EntityMappingsImpl.this, type));
            }
            return Collections.emptyList();
        }

        public boolean modifyObjects(TypeElement type, List<MappedSuperclassImpl> objects) {
            assert objects.size() == 1;
            MappedSuperclassImpl mappedSuperclass = objects.get(0);
            if (!mappedSuperclass.refresh(type)) {
                objects.remove(0);
                return true;
            }
            return false;
        }
    }
}
