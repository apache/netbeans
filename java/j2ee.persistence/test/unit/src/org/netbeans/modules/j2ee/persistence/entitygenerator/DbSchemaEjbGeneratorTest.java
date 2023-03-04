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

package org.netbeans.modules.j2ee.persistence.entitygenerator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.dbschema.SchemaElement;
import org.netbeans.modules.dbschema.SchemaElementUtil;
import org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation.CollectionType;
import org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation.FetchType;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.UpdateType;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;

/**
 *
 * @author Andrei Badea
 */
public class DbSchemaEjbGeneratorTest extends NbTestCase {

    public DbSchemaEjbGeneratorTest(String testName) {
        super(testName);
    }

    public void testIssue92031() throws Exception {
        /*
        create table part1 (id int not null primary key);
        create table part2 (part1_id int not null references part1(id) primary key);
         */
        SchemaElement schema = SchemaElementUtil.forName(URLMapper.findFileObject(getClass().getResource("Issue92031.dbschema")));
        DbSchemaEjbGenerator generator = new DbSchemaEjbGenerator(
                new GeneratedTablesImpl(new HashSet(Arrays.asList("PART1", "PART2"))),
                schema);

        EntityClass[] beans = generator.getBeans();
        EntityClass bean = getBeanByTableName(beans, "PART2");
        assertNotNull(getFieldByName(bean, "part1"));
        RelationshipRole role = (RelationshipRole)bean.getRoles().iterator().next();
        assertEquals("part11", role.getFieldName());
        assertNotNull("Should have CMR mapping for field part11", bean.getCMPMapping().getCmrFieldMapping().get("part11"));
    }

    private static EntityMember getFieldByName(EntityClass bean, String fieldName) {
        for (Iterator i = bean.getFields().iterator(); i.hasNext();) {
            EntityMember member = ((EntityMember)i.next());
            if (fieldName.equals(member.getMemberName())) {
                return member;
            }
        }
        return null;
    }

    private static EntityClass getBeanByTableName(EntityClass[] beans, String tableName) {
        for (int i = 0; i < beans.length; i++) {
            if (tableName.equals(beans[i].getTableName())) {
                return beans[i];
            }
        }
        return null;
    }

    private static final class GeneratedTablesImpl implements GeneratedTables {

        private final Set<String> tableNames;

        public GeneratedTablesImpl(Set<String> tableNames) {
            this.tableNames = tableNames;
        }

        @Override
        public Set<String> getTableNames() {
            return tableNames;
        }

        @Override
        public FileObject getRootFolder(String tableName) {
            return null;
        }

        @Override
        public String getPackageName(String tableName) {
            return null;
        }

        @Override
        public String getClassName(String tableName) {
            return tableName;
        }

        @Override
        public String getSchema() {
            return null;
        }

        @Override
        public String getCatalog() {
            return null;
        }

        public boolean isFullyQualifiedTableNames() {
            return false;
        }

        public FetchType getFetchType() {
             return FetchType.DEFAULT;
        }

        public boolean isRegenSchemaAttrs() {
             return false;
        }

        @Override
        public UpdateType getUpdateType(String tableName) {
            return UpdateType.NEW;
        }

        @Override
        public Set<List<String>> getUniqueConstraints(String tableName) {
            return null;
        }

        public CollectionType getCollectionType() {
            return CollectionType.COLLECTION;
        }
    }
}
