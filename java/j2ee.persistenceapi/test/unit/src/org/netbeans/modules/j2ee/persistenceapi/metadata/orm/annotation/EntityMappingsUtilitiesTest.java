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

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;

/**
 *
 * @author Andrei Badea
 */
public class EntityMappingsUtilitiesTest extends EntityMappingsTestCase {

    public EntityMappingsUtilitiesTest(String testName) {
        super(testName);
    }

    public void testIsCollectionType() throws Exception {
        ClasspathInfo cpi = ClasspathInfo.create(srcFO);
        final AnnotationModelHelper helper = AnnotationModelHelper.create(cpi);
        helper.runJavaSourceTask(new Runnable() {
            public void run() {
                Elements elements = helper.getCompilationController().getElements();
                Types types = helper.getCompilationController().getTypes();
                TypeElement typeElement = elements.getTypeElement("java.util.Collection");
                // Collection<E>
                assertTrue(EntityMappingsUtilities.isCollectionType(helper, typeElement.asType()));
                // Collection
                assertTrue(EntityMappingsUtilities.isCollectionType(helper, types.erasure(typeElement.asType())));
                // Collection<String>
                TypeMirror stringType = elements.getTypeElement("java.lang.String").asType();
                assertTrue(EntityMappingsUtilities.isCollectionType(helper, types.getDeclaredType(typeElement, stringType)));
                typeElement = elements.getTypeElement("java.util.ArrayList");
                // ArrayList<E>
                assertFalse(EntityMappingsUtilities.isCollectionType(helper, typeElement.asType()));
            }
        });
    }

    public void testGetFirstTypeArgument() throws Exception {
        ClasspathInfo cpi = ClasspathInfo.create(srcFO);
        final AnnotationModelHelper helper = AnnotationModelHelper.create(cpi);
        helper.runJavaSourceTask(new Runnable() {
            public void run() {
                Elements elements = helper.getCompilationController().getElements();
                Types types = helper.getCompilationController().getTypes();
                TypeElement typeElement = elements.getTypeElement("java.util.Collection");
                // Collection<E>
                assertNull(EntityMappingsUtilities.getFirstTypeArgument(typeElement.asType()));
                // Collection
                assertNull(EntityMappingsUtilities.getFirstTypeArgument(types.erasure(typeElement.asType())));
                // Collection<String>
                TypeMirror stringType = elements.getTypeElement("java.lang.String").asType();
                TypeElement argTypeElement = EntityMappingsUtilities.getFirstTypeArgument(types.getDeclaredType(typeElement, stringType));
                assertTrue(argTypeElement.getQualifiedName().contentEquals("java.lang.String"));
            }
        });
    }

    public void testHasFieldAccess() throws Exception {
        TestUtilities.copyStringToFileObject(srcFO, "Customer.java",
                "import javax.persistence.*;" +
                "@Entity()" +
                "public class Customer {" +
                "   @Id()" +
                "   private int id;" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "Customer2.java",
                "import javax.persistence.*;" +
                "@Entity()" +
                "public class Customer2 {" +
                "   @Id()" +
                "   private int id;" +
                "   @PrePersist()" +
                "   public int prePersist() {" +
                "   }" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "Customer3.java",
                "import javax.persistence.*;" +
                "@Entity()" +
                "public class Customer3 {" +
                "   private int id;" +
                "   @Id()" +
                "   public int getId() {" +
                "       return id;" +
                "   }" +
                "}");
        IndexingManager.getDefault().refreshIndexAndWait(srcFO.getURL(), null);
        ClasspathInfo cpi = ClasspathInfo.create(srcFO);
        final AnnotationModelHelper helper = AnnotationModelHelper.create(cpi);
        helper.runJavaSourceTask(new Runnable() {
            public void run() {
                TypeElement customer = helper.getCompilationController().getElements().getTypeElement("Customer");
                assertTrue(EntityMappingsUtilities.hasFieldAccess(helper, customer.getEnclosedElements()));
                TypeElement customer2 = helper.getCompilationController().getElements().getTypeElement("Customer2");
                assertTrue(EntityMappingsUtilities.hasFieldAccess(helper, customer2.getEnclosedElements()));
                TypeElement customer3 = helper.getCompilationController().getElements().getTypeElement("Customer3");
                assertFalse(EntityMappingsUtilities.hasFieldAccess(helper, customer3.getEnclosedElements()));
            }
        });
    }
}
