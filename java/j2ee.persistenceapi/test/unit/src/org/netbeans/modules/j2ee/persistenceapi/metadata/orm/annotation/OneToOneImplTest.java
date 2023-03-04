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
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.*;

/**
 *
 * @author Andrei Badea
 */
public class OneToOneImplTest extends EntityMappingsTestCase {

    public OneToOneImplTest(String testName) {
        super(testName);
    }

    public void testOneToOne() throws Exception {
        TestUtilities.copyStringToFileObject(srcFO, "foo/Customer.java",
                "package foo;" +
                "import javax.persistence.*;" +
                "@Entity()" +
                "public class Customer {" +
                "   @Id()" +
                "   private int id;" +
                "   @OneToOne()" +
                "   private bar.Address address;" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "bar/Address.java",
                "package bar;" +
                "import javax.persistence.*;" +
                "@Entity()" +
                "public class Address {" +
                "   @Id()" +
                "   private int id;" +
                "   @OneToOne(cascade = CascadeType.ALL, mappedBy = \"address\", optional = false)" +
                "   private foo.Customer customer;" +
                "}");
        createModel().runReadAction(new MetadataModelAction<EntityMappingsMetadata, Void>() {
            public Void run(EntityMappingsMetadata metadata) {
                Entity address = getEntityByName(metadata.getRoot().getEntity(), "Address");
                OneToOne oneToOne = address.getAttributes().getOneToOne(0);
                assertEquals("customer", oneToOne.getName());
                assertEquals("address", oneToOne.getMappedBy());
                assertEquals("foo.Customer", oneToOne.getTargetEntity());
                assertNotNull(oneToOne.getCascade().getCascadeAll());
                assertFalse(oneToOne.isOptional());
                assertEquals("EAGER", oneToOne.getFetch());
                return null;
            }
        });
    }
}
