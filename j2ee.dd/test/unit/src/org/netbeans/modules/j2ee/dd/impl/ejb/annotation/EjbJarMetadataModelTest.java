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

package org.netbeans.modules.j2ee.dd.impl.ejb.annotation;

import java.io.IOException;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.ejb.AssemblyDescriptor;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.dd.impl.common.annotation.CommonTestCase;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;

/**
 *
 * @author Martin Adamek
 */
public class EjbJarMetadataModelTest extends CommonTestCase {
    
    public EjbJarMetadataModelTest(String testName) {
        super(testName);
    }

    public void testModel() throws IOException, InterruptedException {
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Customer.java",
                "package foo;" +
                "import javax.ejb.*;" +
                "@Stateless()" +
                "public class Customer implements CustomerLocal {" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/CustomerLocal.java",
                "package foo;" +
                "public interface CustomerLocal {" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/Employee.java",
                "package foo;" +
                "import javax.ejb.*;" +
                "@Stateful()" +
                "public class Employee implements EmployeeLocal, EmployeeRemote {" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/EmployeeLocal.java",
                "package foo;" +
                "import javax.ejb.*;" +
                "@Local()" +
                "public interface EmployeeLocal {" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/EmployeeRemote.java",
                "package foo;" +
                "import javax.ejb.*;" +
                "@Remote()" +
                "public interface EmployeeRemote {" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/Dispatcher.java",
                "package foo;" +
                "import javax.ejb.*;" +
                "import javax.jms.*;" +
                "@MessageDriven()" +
                "public class Dispatcher implements MessageListener {" +
                "public void onMessage(Message message) {}" +
                "}");
        
        final String expectedResult = "foo";
        
        String result = createEjbJarModel().runReadAction(new MetadataModelAction<EjbJarMetadata, String>() {
            public String run(EjbJarMetadata metadata) throws VersionNotSupportedException {
                
                EjbJar ejbJar = metadata.getRoot();
                EnterpriseBeans enterpriseBeans = ejbJar.getEnterpriseBeans();
                assertNotNull(enterpriseBeans);
                Ejb[] ejbs = enterpriseBeans.getEjbs();
                assertEquals(3, ejbs.length);
                Session[] sessions = enterpriseBeans.getSession();
                assertEquals(2, sessions.length);
                MessageDriven[] messageDrivens = enterpriseBeans.getMessageDriven();
                assertEquals(1, messageDrivens.length);
                AssemblyDescriptor assemblyDescriptor = ejbJar.getSingleAssemblyDescriptor();
                assertNotNull(assemblyDescriptor);

                return expectedResult;
            }
        });
        
        assertSame(expectedResult, result);
    }
    
}
