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
