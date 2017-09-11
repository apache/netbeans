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
