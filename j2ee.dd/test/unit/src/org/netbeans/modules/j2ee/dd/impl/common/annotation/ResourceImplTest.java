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

package org.netbeans.modules.j2ee.dd.impl.common.annotation;

import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;

/**
 * Test for {@link ResourceImpl}.
 * @author Tomas Mysik
 */
public class ResourceImplTest extends CommonTestCase {
    
    public ResourceImplTest(String testName) {
        super(testName);
    }

    public void testSimplyAnnotatedField() throws Exception {
        TestUtilities.copyStringToFileObject(srcFO, "MyClass.java",
                "public class MyClass {" +
                "   @javax.annotation.Resource" +
                "   private javax.sql.DataSource myResource;" +
                "}");
        IndexingManager.getDefault().refreshIndexAndWait(srcFO.getURL(), null);
        ClasspathInfo cpi = ClasspathInfo.create(srcFO);
        final AnnotationModelHelper annotationModelHelper = AnnotationModelHelper.create(cpi);
        annotationModelHelper.runJavaSourceTask(new Runnable() {
            public void run() {
                TypeElement myClass = annotationModelHelper.getCompilationController().getElements().getTypeElement("MyClass");
                List<VariableElement> fields = ElementFilter.fieldsIn(myClass.getEnclosedElements());
                VariableElement annotatedField = fields.get(0);
                
                ResourceImpl resource = new ResourceImpl(annotatedField, myClass, annotationModelHelper);
                assertEquals("myResource", resource.getName());
                assertEquals("javax.sql.DataSource", resource.getType());
                assertEquals(ResourceImpl.DEFAULT_AUTHENTICATION_TYPE, resource.getAuthenticationType());
                assertEquals(ResourceImpl.DEFAULT_SHAREABLE, resource.getShareable());
                assertEquals(ResourceImpl.DEFAULT_MAPPED_NAME, resource.getMappedName());
                assertEquals(ResourceImpl.DEFAULT_DESCRIPTION, resource.getDescription());
            }
        });
    }
    
    public void testFullyAnnotatedField() throws Exception {
        TestUtilities.copyStringToFileObject(srcFO, "MyClass.java",
                "public class MyClass {" +
                "   @javax.annotation.Resource(" +
                "       name=\"myName\", type=Object.class, shareable=false," +
                "       authenticationType=javax.annotation.Resource.AuthenticationType.APPLICATION," +
                "       mappedName=\"myMappedName\", description=\"myDescription\")" +
                "   private javax.sql.DataSource myResource;" +
                "}");
        IndexingManager.getDefault().refreshIndexAndWait(srcFO.getURL(), null);
        ClasspathInfo cpi = ClasspathInfo.create(srcFO);
        final AnnotationModelHelper annotationModelHelper = AnnotationModelHelper.create(cpi);
        annotationModelHelper.runJavaSourceTask(new Runnable() {
            public void run() {
                TypeElement myClass = annotationModelHelper.getCompilationController().getElements().getTypeElement("MyClass");
                List<VariableElement> fields = ElementFilter.fieldsIn(myClass.getEnclosedElements());
                VariableElement annotatedField = fields.get(0);
                
                ResourceImpl resource = new ResourceImpl(annotatedField, myClass, annotationModelHelper);
                assertEquals("myName", resource.getName());
                assertEquals("java.lang.Object", resource.getType());
                assertEquals("APPLICATION", resource.getAuthenticationType());
                assertEquals("false", resource.getShareable());
                assertEquals("myMappedName", resource.getMappedName());
                assertEquals("myDescription", resource.getDescription());
            }
        });
    }
    
    public void testSimplyAnnotatedMethod() throws Exception {
        TestUtilities.copyStringToFileObject(srcFO, "MyClass.java",
                "public class MyClass {" +
                "   @javax.annotation.Resource" +
                "   private void setMyResource(javax.sql.DataSource dataSource) {" +
                "   }" +
                "}");
        IndexingManager.getDefault().refreshIndexAndWait(srcFO.getURL(), null);
        ClasspathInfo cpi = ClasspathInfo.create(srcFO);
        final AnnotationModelHelper annotationModelHelper = AnnotationModelHelper.create(cpi);
        annotationModelHelper.runJavaSourceTask(new Runnable() {
            public void run() {
                TypeElement myClass = annotationModelHelper.getCompilationController().getElements().getTypeElement("MyClass");
                List<ExecutableElement> methods = ElementFilter.methodsIn(myClass.getEnclosedElements());
                ExecutableElement annotatedMethod = methods.get(0);
                
                ResourceImpl resource = new ResourceImpl(annotatedMethod, myClass, annotationModelHelper);
                assertEquals("myResource", resource.getName());
                assertEquals("javax.sql.DataSource", resource.getType());
                assertEquals(ResourceImpl.DEFAULT_AUTHENTICATION_TYPE, resource.getAuthenticationType());
                assertEquals(ResourceImpl.DEFAULT_SHAREABLE, resource.getShareable());
                assertEquals(ResourceImpl.DEFAULT_MAPPED_NAME, resource.getMappedName());
                assertEquals(ResourceImpl.DEFAULT_DESCRIPTION, resource.getDescription());
            }
        });
    }
    
    public void testFullyAnnotatedMethod() throws Exception {
        TestUtilities.copyStringToFileObject(srcFO, "MyClass.java",
                "public class MyClass {" +
                "   @javax.annotation.Resource(" +
                "       name=\"myName\", type=Object.class, shareable=false," +
                "       authenticationType=javax.annotation.Resource.AuthenticationType.APPLICATION," +
                "       mappedName=\"myMappedName\", description=\"myDescription\")" +
                "   private void setMyResource(javax.sql.DataSource dataSource) {" +
                "   }" +
                "}");
        IndexingManager.getDefault().refreshIndexAndWait(srcFO.getURL(), null);
        ClasspathInfo cpi = ClasspathInfo.create(srcFO);
        final AnnotationModelHelper annotationModelHelper = AnnotationModelHelper.create(cpi);
        annotationModelHelper.runJavaSourceTask(new Runnable() {
            public void run() {
                TypeElement myClass = annotationModelHelper.getCompilationController().getElements().getTypeElement("MyClass");
                List<ExecutableElement> methods = ElementFilter.methodsIn(myClass.getEnclosedElements());
                ExecutableElement annotatedMethod = methods.get(0);
                
                ResourceImpl resource = new ResourceImpl(annotatedMethod, myClass, annotationModelHelper);
                assertEquals("myName", resource.getName());
                assertEquals("java.lang.Object", resource.getType());
                assertEquals("APPLICATION", resource.getAuthenticationType());
                assertEquals("false", resource.getShareable());
                assertEquals("myMappedName", resource.getMappedName());
                assertEquals("myDescription", resource.getDescription());
            }
        });
    }
    
    public void testSimplyAnnotatedClass() throws Exception {
        TestUtilities.copyStringToFileObject(srcFO, "MyClass.java",
                "@javax.annotation.Resource(name=\"myAnnotatedClass\")" +
                "public class MyClass {" +
                "}");
        IndexingManager.getDefault().refreshIndexAndWait(srcFO.getURL(), null);
        ClasspathInfo cpi = ClasspathInfo.create(srcFO);
        final AnnotationModelHelper annotationModelHelper = AnnotationModelHelper.create(cpi);
        annotationModelHelper.runJavaSourceTask(new Runnable() {
            public void run() {
                TypeElement myClass = annotationModelHelper.getCompilationController().getElements().getTypeElement("MyClass");
                
                ResourceImpl resource = new ResourceImpl(myClass, myClass, annotationModelHelper);
                assertEquals("myAnnotatedClass", resource.getName());
                assertEquals("MyClass", resource.getType());
                assertEquals(ResourceImpl.DEFAULT_AUTHENTICATION_TYPE, resource.getAuthenticationType());
                assertEquals(ResourceImpl.DEFAULT_SHAREABLE, resource.getShareable());
                assertEquals(ResourceImpl.DEFAULT_MAPPED_NAME, resource.getMappedName());
                assertEquals(ResourceImpl.DEFAULT_DESCRIPTION, resource.getDescription());
            }
        });
    }
    
    public void testFullyAnnotatedClass() throws Exception {
        TestUtilities.copyStringToFileObject(srcFO, "MyClass.java",
                "@javax.annotation.Resource(name=\"myAnnotatedClass\", type=Object.class, shareable=false," +
                "   authenticationType=javax.annotation.Resource.AuthenticationType.APPLICATION," +
                "   mappedName=\"myMappedName\", description=\"myDescription\")" +
                "public class MyClass {" +
                "}");
        IndexingManager.getDefault().refreshIndexAndWait(srcFO.getURL(), null);
        ClasspathInfo cpi = ClasspathInfo.create(srcFO);
        final AnnotationModelHelper annotationModelHelper = AnnotationModelHelper.create(cpi);
        annotationModelHelper.runJavaSourceTask(new Runnable() {
            public void run() {
                TypeElement myClass = annotationModelHelper.getCompilationController().getElements().getTypeElement("MyClass");
                
                ResourceImpl resource = new ResourceImpl(myClass, myClass, annotationModelHelper);
                assertEquals("myAnnotatedClass", resource.getName());
                assertEquals("java.lang.Object", resource.getType());
                assertEquals("APPLICATION", resource.getAuthenticationType());
                assertEquals("false", resource.getShareable());
                assertEquals("myMappedName", resource.getMappedName());
                assertEquals("myDescription", resource.getDescription());
            }
        });
    }
}
