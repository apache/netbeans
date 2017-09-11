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

        public Set<String> getTableNames() {
            return tableNames;
        }

        public FileObject getRootFolder(String tableName) {
            return null;
        }

        public String getPackageName(String tableName) {
            return null;
        }

        public String getClassName(String tableName) {
            return tableName;
        }

        public String getSchema() {
            return null;
        }

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

        public UpdateType getUpdateType(String tableName) {
            return UpdateType.NEW;
        }

        public Set<List<String>> getUniqueConstraints(String tableName) {
            return null;
        }

        public CollectionType getCollectionType() {
            return CollectionType.COLLECTION;
        }
    }
}
