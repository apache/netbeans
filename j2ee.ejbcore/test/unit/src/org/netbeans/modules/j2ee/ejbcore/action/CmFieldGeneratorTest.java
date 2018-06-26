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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
