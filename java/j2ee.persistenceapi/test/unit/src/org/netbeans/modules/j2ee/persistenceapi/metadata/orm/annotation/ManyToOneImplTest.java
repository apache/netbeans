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
public class ManyToOneImplTest extends EntityMappingsTestCase {

    public ManyToOneImplTest(String testName) {
        super(testName);
    }

    public void testManyToOne() throws Exception {
        TestUtilities.copyStringToFileObject(srcFO, "foo/Customer.java",
                "package foo;" +
                "import javax.persistence.*;" +
                "@Entity()" +
                "public class Customer {" +
                "   @Id()" +
                "   private int id;" +
                "   @ManyToOne(cascade = CascadeType.ALL, optional = false)" +
                "   private bar.DiscountCode discountCode;" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "bar/DiscountCode.java",
                "package bar;" +
                "import java.util.*;" +
                "import javax.persistence.*;" +
                "@Entity()" +
                "public class DiscountCode {" +
                "   @Id()" +
                "   private int id;" +
                "   @OneToMany(cascade = CascadeType.ALL, mappedBy = \"discountCode\")" +
                "   private List<foo.Customer> customerList;" +
                "}");
        createModel().runReadAction(new MetadataModelAction<EntityMappingsMetadata, Void>() {
            public Void run(EntityMappingsMetadata metadata) {
                Entity customer = getEntityByName(metadata.getRoot().getEntity(), "Customer");
                ManyToOne manyToOne = customer.getAttributes().getManyToOne(0);
                assertEquals("discountCode", manyToOne.getName());
                assertEquals("bar.DiscountCode", manyToOne.getTargetEntity());
                assertNotNull(manyToOne.getCascade().getCascadeAll());
                assertFalse(manyToOne.isOptional());
                assertEquals("EAGER", manyToOne.getFetch());
                return null;
            }
        });
    }
}
