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

package org.netbeans.modules.j2ee.ejbcore.action;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel;
import org.netbeans.modules.j2ee.dd.api.ejb.CmpField;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.ejbcore.test.TestBase;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Adamek
 */
public class CmFieldGeneratorTest extends TestBase {
    
    public CmFieldGeneratorTest(String testName) {
        super(testName);
    }

    public void testAddCmpField() throws Exception {
        
        TestModule testModule = createEjb21Module();
        
        MethodModel.Variable field = MethodModel.Variable.create("java.lang.String", "firstName");
        final FileObject ejbClassFO = testModule.getSources()[0].getFileObject("cmplr/CmpLRBean.java");
        CmFieldGenerator generator = CmFieldGenerator.create("cmplr.CmpLRBean", ejbClassFO);
        generator.addCmpField(field, true, true, false, false, "");
        
        JavaSource javaSource = JavaSource.forFileObject(ejbClassFO);
        javaSource.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = controller.getElements().getTypeElement("cmplr.CmpLRBean");

                MethodModel methodModel = MethodModel.create(
                        "getFirstName",
                        "java.lang.String",
                        "",
                        Collections.<MethodModel.Variable>emptyList(),
                        Collections.<String>emptyList(),
                        CmFieldGenerator.PUBLIC_ABSTRACT
                        );
                assertTrue(containsMethod(controller, methodModel, typeElement));

                methodModel = MethodModel.create(
                        "setFirstName",
                        "void",
                        "",
                        Collections.singletonList(MethodModel.Variable.create("java.lang.String", "firstName")),
                        Collections.<String>emptyList(),
                        CmFieldGenerator.PUBLIC_ABSTRACT
                        );
                assertTrue(containsMethod(controller, methodModel, typeElement));
            }
        }, true);

        final FileObject localInterfaceFO = testModule.getSources()[0].getFileObject("cmplr/CmpLRLocal.java");
        javaSource = JavaSource.forFileObject(localInterfaceFO);
        javaSource.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = controller.getElements().getTypeElement("cmplr.CmpLRLocal");

                MethodModel methodModel = MethodModel.create(
                        "getFirstName",
                        "java.lang.String",
                        "",
                        Collections.<MethodModel.Variable>emptyList(),
                        Collections.<String>emptyList(),
                        Collections.<Modifier>emptySet()
                        );
                assertTrue(containsMethod(controller, methodModel, typeElement));

                methodModel = MethodModel.create(
                        "setFirstName",
                        "void",
                        "",
                        Collections.singletonList(MethodModel.Variable.create("java.lang.String", "firstName")),
                        Collections.<String>emptyList(),
                        Collections.<Modifier>emptySet()
                        );
                assertTrue(containsMethod(controller, methodModel, typeElement));
                
            }
        }, true);
        
        MetadataModel<EjbJarMetadata> metadataModel = EjbJar.getEjbJar(testModule.getDeploymentDescriptor()).getMetadataModel();
        metadataModel.runReadAction(new MetadataModelAction<EjbJarMetadata, Void>() {
            public Void run(EjbJarMetadata metadata) {
                Entity entity = (Entity) metadata.findByEjbClass("cmplr.CmpLRBean");
                CmpField[] cmpFields = entity.getCmpField();
                assertEquals(2, cmpFields.length);
                Set<String> cmpFieldsNames = new HashSet<String>();
                for (CmpField cmpField : cmpFields) {
                    cmpFieldsNames.add(cmpField.getFieldName());
                }
                assertTrue(cmpFieldsNames.contains("key"));
                assertTrue(cmpFieldsNames.contains("firstName"));
                return null;
            }
        });
        
    }
    
}
