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

package org.netbeans.modules.j2ee.persistenceapi.metadata.orm.annotation;

import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Basic;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Embeddable;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappings;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Id;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Version;

/**
 *
 * @author Andrei Badea
 */
public class EntityMappingsMetadataModelTest extends EntityMappingsTestCase {

    public EntityMappingsMetadataModelTest(String testName) {
        super(testName);
    }

    public void testModel() throws Exception {
        TestUtilities.copyStringToFileObject(srcFO, "foo/CustomerImpl.java",
                "package foo;" +
                "import java.util.*;" +
                "import javax.persistence.*;" +
                "@Entity(name = \"Customer\")" +
                "public class CustomerImpl {" +
                "   @Id()" +
                "   @Column(name = \"CUST_ID\")" +
                "   private int id;" +
                "   @Basic(optional = false)" +
                "   private int age;" +
                "   @Column(name = \"CUST_NAME\", nullable = false)" +
                "   private String name;" +
                "   @Temporal(TemporalType.DATE)" +
                "   private Date birthDate;" +
                "   @Temporal(TemporalType.TIME)" +
                "   private Date birthTime;" +
                "   @Version()" +
                "   @Column(name = \"VER\", nullable = false)" +
                "   private int version;" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/Employee.java",
                "package foo;" +
                "import javax.persistence.*;" +
                "import java.util.*;" +
                "@Entity()" +
                "public class Employee {" +
                "   @Id()" +
                "   @Column(name=\"EMP_ID\")" +
                "   @Temporal(TemporalType.TIMESTAMP)" +
                "   private Date id;" +
                "   @Version()" +
                "   @Temporal(TemporalType.DATE)" +
                "   private Date entryDate;" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/DateBasedId.java",
                "package foo;" +
                "import javax.persistence.*;" +
                "import java.util.*;" +
                "@Embeddable()" +
                "public class DateBasedId {" +
                "   @Temporal(TemporalType.DATE)" +
                "   private Date date;" +
                "   @Column(name=\"ORDER_NUM\")" +
                "   private int order;" +
                "}");
        final String expectedResult = "foo";
        String result = createModel().runReadAction(new MetadataModelAction<EntityMappingsMetadata, String>() {
            public String run(EntityMappingsMetadata metadata) {
                EntityMappings entityMappings = metadata.getRoot();
                // test entities
                Entity[] entityList = entityMappings.getEntity();
                assertEquals(2, entityList.length);
                // test Customer
                Entity entity = getEntityByName(entityList, "Customer");
                assertEquals("foo.CustomerImpl", entity.getClass2());
                assertEquals("CUSTOMER", entity.getTable().getName());
                assertEquals("", entity.getTable().getSchema());
                assertEquals("", entity.getTable().getCatalog());
                Id[] idList = entity.getAttributes().getId();
                assertEquals(1, idList.length);
                assertEquals("id", idList[0].getName());
                assertEquals("CUST_ID", idList[0].getColumn().getName());
                assertNull(idList[0].getTemporal());
                Basic[] basicList = entity.getAttributes().getBasic();
                assertEquals(4, basicList.length);
                assertEquals("age", basicList[0].getName());
                assertFalse(basicList[0].isOptional());
                assertEquals("AGE", basicList[0].getColumn().getName());
                assertNull(basicList[0].getTemporal());
                assertEquals("name", basicList[1].getName());
                assertTrue(basicList[1].isOptional());
                assertEquals("CUST_NAME", basicList[1].getColumn().getName());
                assertEquals(255, basicList[1].getColumn().getLength());
                assertFalse(basicList[1].getColumn().isNullable());
                assertNull(basicList[1].getTemporal());
                assertEquals("DATE", basicList[2].getTemporal());
                assertEquals("TIME", basicList[3].getTemporal());
                Version[] versionList = entity.getAttributes().getVersion();
                assertEquals(1, versionList.length);
                assertEquals("version", versionList[0].getName());
                assertEquals("VER", versionList[0].getColumn().getName());
                // test Employee
                entity = getEntityByName(entityList, "Employee");
                idList = entity.getAttributes().getId();
                assertEquals(1, idList.length);
                assertEquals("TIMESTAMP", idList[0].getTemporal());
                versionList = entity.getAttributes().getVersion();
                assertEquals(1, versionList.length);
                assertEquals("entryDate", versionList[0].getName());
                assertEquals("DATE", versionList[0].getTemporal());
                // test embeddables
                Embeddable[] embeddableList = entityMappings.getEmbeddable();
                assertEquals(1, embeddableList.length);
                // test Address
                Embeddable embeddable = getEmbeddableByClass(embeddableList, "foo.DateBasedId");
                basicList = embeddable.getAttributes().getBasic();
                assertEquals(2, basicList.length);
                assertEquals("date", basicList[0].getName());
                assertEquals("DATE", basicList[0].getTemporal());
                assertEquals("order", basicList[1].getName());
                assertEquals("ORDER_NUM", basicList[1].getColumn().getName());
                return expectedResult;
            }
        });
        assertSame(expectedResult, result);
    }

    public void testStaticFieldsIgnoredIssue108993() throws Exception {
        TestUtilities.copyStringToFileObject(srcFO, "foo/Employee.java",
                "package foo;" +
                "import javax.persistence.*;" +
                "@Entity()" +
                "public class Employee {" +
                "   public static String ID = \"id\";" +
                "   @Id()" +
                "   private int id;" +
                "}");
        createModel().runReadAction(new MetadataModelAction<EntityMappingsMetadata, Void>() {
            public Void run(EntityMappingsMetadata metadata) {
                assertEquals(0, metadata.getRoot().getEntity()[0].getAttributes().getBasic().length);
                return null;
            }
        });
    }
}
