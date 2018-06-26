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

package org.netbeans.modules.j2ee.ejbcore.api.codegeneration;

import org.netbeans.modules.j2ee.ejbcore.api.codegeneration.CallEjbGenerator;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import java.io.IOException;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.j2ee.api.ejbjar.EjbReference;
import org.netbeans.modules.j2ee.core.api.support.java.SourceUtils;
import org.netbeans.modules.j2ee.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.ejbcore.test.EnterpriseReferenceContainerImpl;
import org.netbeans.modules.j2ee.ejbcore.test.TestBase;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Adamek
 */
public class CallEjbGeneratorTest extends TestBase {
    
    private TestModule referencedEjb21Module;
    private EjbReference ejbReference;
        
    public CallEjbGeneratorTest(String testName) throws IOException {
        super(testName);
    }
    
    @Override
    protected void setUp() throws IOException {
        super.setUp();
        this.referencedEjb21Module = createTestModule("EJBModule2_1_4", EJB_2_1);
        this.ejbReference = EjbReference.create(
            "statelesslr.StatelessLRBean2",
            EjbRef.EJB_REF_TYPE_SESSION,
            "statelesslr.StatelessLRLocal2",
            "statelesslr.StatelessLRLocalHome2",
            "statelesslr.StatelessLRRemote2",
            "statelesslr.StatelessLRRemoteHome2",
            referencedEjb21Module.getEjbModule()
            );
    }
    
    public void testAddReference_LocalEE14FromEjbEE14() throws IOException {
        TestModule referencingModule = createEjb21Module(referencedEjb21Module);
        
        FileObject referencingFO = referencingModule.getSources()[0].getFileObject("statelesslr/StatelessLRBean.java");
        
        CallEjbGenerator generator = CallEjbGenerator.create(ejbReference, "StatelessLRBean2", true);
        generator.addReference(
                referencingFO,
                "statelesslr.StatelessLRBean",
                referencedEjb21Module.getSources()[0].getFileObject("statelesslr/StatelessLRBean2.java"),
                "statelesslr.StatelessLRBean2",
                null,
                EjbReference.EjbRefIType.LOCAL,
                false,
                referencedEjb21Module.getProject()
                );
        
        EnterpriseReferenceContainerImpl erc = referencingModule.getEnterpriseReferenceContainerImpl();
        assertNotNull(erc.getLocalEjbReference());
        assertEquals("StatelessLRBean2", erc.getLocalEjbRefName());
        assertEquals("statelesslr.StatelessLRBean", erc.getLocalReferencingClass());

        final String generatedMethodBody =
        "{" + newline +
        "    try {" + newline +
        "        Context c = new InitialContext();" + newline +
        "        StatelessLRLocalHome2 rv = (StatelessLRLocalHome2)c.lookup(\"java:comp/env/StatelessLRBean2\");" + newline +
        "        return rv.create();" + newline +
        "    } catch (NamingException ne) {" + newline +
        "        Logger.getLogger(getClass().getName()).log(Level.SEVERE, \"exception caught\", ne);" + newline +
        "        throw new RuntimeException(ne);" + newline +
        "    } catch (CreateException ce) {" + newline +
        "        Logger.getLogger(getClass().getName()).log(Level.SEVERE, \"exception caught\", ce);" + newline +
        "        throw new RuntimeException(ce);" + newline +
        "    }" + newline +
        "}";
        
        JavaSource javaSource = JavaSource.forFileObject(referencingFO);
        javaSource.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController controller) throws Exception {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = SourceUtils.getPublicTopLevelElement(controller);
                ExecutableElement method = (ExecutableElement) getMember(typeElement, "lookupStatelessLRBean2Local");
                assertNotNull(method);
                MethodTree methodTree = controller.getTrees().getTree(method);
                assertEquals(generatedMethodBody, methodTree.getBody().toString());
            }
        }, true);
        
    }
    
    public void testAddReference_LocalEE14FromEjbEE5() throws IOException {
        TestModule referencingModule = createEjb30Module(referencedEjb21Module);
        
        FileObject referencingFO = referencingModule.getSources()[0].getFileObject("statelesslr/StatelessLRBean.java");
        
        CallEjbGenerator generator = CallEjbGenerator.create(ejbReference, "StatelessLRBean2", true);
        generator.addReference(
                referencingFO,
                "statelesslr.StatelessLRBean",
                referencedEjb21Module.getSources()[0].getFileObject("statelesslr/StatelessLRBean2.java"),
                "statelesslr.StatelessLRBean2",
                null,
                EjbReference.EjbRefIType.LOCAL,
                false,
                referencedEjb21Module.getProject()
                );
        
        EnterpriseReferenceContainerImpl erc = referencingModule.getEnterpriseReferenceContainerImpl();
        assertNull(erc.getLocalEjbReference());
        assertNull(erc.getLocalEjbRefName());
        assertNull(erc.getLocalReferencingClass());

        final String generatedHome =
                "@EJB()" + newline +
                "private StatelessLRLocalHome2 statelessLRLocalHome2";
        
        final String generatedComponent =
                "private StatelessLRLocal2 statelessLRBean2";
        
        
        
        final String generatedMethod =
                newline +
                "@PostConstruct()" + newline +
                "private void initialize() {" + newline +
                "    try {" + newline +
                "        statelessLRBean2 = statelessLRLocalHome2.create();" + newline +
                "    } catch (Exception e) {" + newline +
                "        throw new EJBException(e);" + newline +
                "    }" + newline +
                "}";
        
        JavaSource javaSource = JavaSource.forFileObject(referencingFO);
        javaSource.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController controller) throws Exception {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = SourceUtils.getPublicTopLevelElement(controller);

                Element memberElement = getMember(typeElement, "statelessLRLocalHome2");
                assertNotNull(memberElement);
                Tree memberTree = controller.getTrees().getTree(memberElement);
                assertEquals(generatedHome, memberTree.toString());
                
                memberElement = getMember(typeElement, "statelessLRBean2");
                assertNotNull(memberElement);
                memberTree = controller.getTrees().getTree(memberElement);
                assertEquals(generatedComponent, memberTree.toString());
                
                memberElement = getMember(typeElement, "initialize");
                assertNotNull(memberElement);
                memberTree = controller.getTrees().getTree(memberElement);
                assertEquals(generatedMethod, memberTree.toString());
                
            }
        }, true);
        
    }
    
}
