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
public class ManyToManyImplTest extends EntityMappingsTestCase {

    public ManyToManyImplTest(String testName) {
        super(testName);
    }

    public void testManyToOne() throws Exception {
        TestUtilities.copyStringToFileObject(srcFO, "foo/Employee.java",
                "package foo;" +
                "import java.util.*;" +
                "import javax.persistence.*;" +
                "@Entity()" +
                "public class Employee {" +
                "   @Id()" +
                "   private int id;" +
                "   @ManyToMany(cascade = CascadeType.ALL)" +
                "   private List<bar.Project> projectList;" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "bar/Project.java",
                "package bar;" +
                "import java.util.*;" +
                "import javax.persistence.*;" +
                "@Entity()" +
                "public class Project {" +
                "   @Id()" +
                "   private int id;" +
                "   @ManyToMany(cascade = CascadeType.ALL, mappedBy = \"projectList\")" +
                "   private List<foo.Employee> employeeList;" +
                "}");
        createModel().runReadAction(new MetadataModelAction<EntityMappingsMetadata, Void>() {
            public Void run(EntityMappingsMetadata metadata) {
                Entity customer = getEntityByName(metadata.getRoot().getEntity(), "Employee");
                ManyToMany manyToMany = customer.getAttributes().getManyToMany(0);
                assertEquals("projectList", manyToMany.getName());
                assertEquals("bar.Project", manyToMany.getTargetEntity());
                assertNotNull(manyToMany.getCascade().getCascadeAll());
                assertEquals("LAZY", manyToMany.getFetch());
                Entity project = getEntityByName(metadata.getRoot().getEntity(), "Project");
                manyToMany = project.getAttributes().getManyToMany(0);
                assertEquals("employeeList", manyToMany.getName());
                assertEquals("foo.Employee", manyToMany.getTargetEntity());
                assertNotNull(manyToMany.getCascade().getCascadeAll());
                assertEquals("LAZY", manyToMany.getFetch());
                return null;
            }
        });
    }
}
