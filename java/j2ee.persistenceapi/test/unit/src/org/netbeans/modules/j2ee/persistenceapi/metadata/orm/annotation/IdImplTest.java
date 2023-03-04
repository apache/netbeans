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

import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Id;

/**
 *
 * @author Andrei Badea
 */
public class IdImplTest extends EntityMappingsTestCase {

    public IdImplTest(String testName) {
        super(testName);
    }

    public void testBasic() throws Exception {
        TestUtilities.copyStringToFileObject(srcFO, "Customer.java",
                "import javax.persistence.*;" +
                "@Entity()" +
                "public class Customer {" +
                "   @Id()" +
                "   @GeneratedValue()" +
                "   private int id;" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "Employee.java",
                "import javax.persistence.*;" +
                "@Entity()" +
                "public class Employee {" +
                "   @Id()" +
                "   @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = \"seq\")" +
                "   private int id;" +
                "}");
        createModel().runReadAction(new MetadataModelAction<EntityMappingsMetadata, Void>() {
            public Void run(EntityMappingsMetadata metadata) {
                Entity[] entityList = metadata.getRoot().getEntity();
                Entity entity = getEntityByName(entityList, "Customer");
                Id id = entity.getAttributes().getId()[0];
                assertEquals("id", id.getName());
                assertEquals("AUTO", id.getGeneratedValue().getStrategy());
                assertEquals("", id.getGeneratedValue().getGenerator());
                entity = getEntityByName(entityList, "Employee");
                id = entity.getAttributes().getId()[0];
                assertEquals("SEQUENCE", id.getGeneratedValue().getStrategy());
                assertEquals("seq", id.getGeneratedValue().getGenerator());
                return null;
            }
        });
    }
}
