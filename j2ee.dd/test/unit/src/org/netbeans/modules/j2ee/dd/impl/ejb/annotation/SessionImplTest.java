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
import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.j2ee.dd.api.common.PortComponentRef;
import org.netbeans.modules.j2ee.dd.api.common.ServiceRef;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.dd.impl.common.annotation.CommonTestCase;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;

/**
 *
 * @author Martin Adamek
 */
public class SessionImplTest extends CommonTestCase {

    public SessionImplTest(String testName) {
        super(testName);
    }

    public void testSession() throws InterruptedException, IOException {
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
                "@Stateful(name=\"SatisfiedEmployee\")" +
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
        TestUtilities.copyStringToFileObject(srcFO, "foo/Employer.java",
                "package foo;" +
                "import javax.ejb.*;" +
                "@Stateful()" +
                "@Local(EmployerLocal.class)" +
                "@Remote({EmployerRemote.class, EmployerRemoteAdvanced.class})" +
                "public class Employer implements EmployerLocal, EmployerRemote {" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/EmployerLocal.java",
                "package foo;" +
                "public interface EmployerLocal {" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/EmployerRemote.java",
                "package foo;" +
                "public interface EmployerRemote {" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/EmployerRemoteAdvanced.java",
                "package foo;" +
                "public interface EmployerRemoteAdvanced {" +
                "}");

        createEjbJarModel().runReadAction(new MetadataModelAction<EjbJarMetadata, Void>() {
            public Void run(EjbJarMetadata metadata) throws VersionNotSupportedException {
                // test Customer
                Session session = (Session) getEjbByEjbName(metadata.getRoot().getEnterpriseBeans().getSession(), "Customer");
                assertEquals("foo.Customer", session.getEjbClass());
                assertEquals("Customer", session.getEjbName());
                assertEquals(Session.SESSION_TYPE_STATELESS, session.getSessionType());
                assertEquals(1, session.getBusinessLocal().length);
                assertEquals("foo.CustomerLocal", session.getBusinessLocal()[0]);
                // test Employee
                session = (Session) getEjbByEjbName(metadata.getRoot().getEnterpriseBeans().getSession(), "SatisfiedEmployee");
                assertEquals("foo.Employee", session.getEjbClass());
                assertEquals("SatisfiedEmployee", session.getEjbName());
                assertEquals(Session.SESSION_TYPE_STATEFUL, session.getSessionType());
                assertEquals(1, session.getBusinessLocal().length);
                assertEquals("foo.EmployeeLocal", session.getBusinessLocal()[0]);
                assertEquals(1, session.getBusinessRemote().length);
                assertEquals("foo.EmployeeRemote", session.getBusinessRemote()[0]);
                // test Employer
                session = (Session) getEjbByEjbName(metadata.getRoot().getEnterpriseBeans().getSession(), "Employer");
                assertEquals("foo.Employer", session.getEjbClass());
                assertEquals("Employer", session.getEjbName());
                assertEquals(Session.SESSION_TYPE_STATEFUL, session.getSessionType());
                assertEquals(1, session.getBusinessLocal().length);
                assertEquals("foo.EmployerLocal", session.getBusinessLocal()[0]);
                List<String> remoteInterfaces = Arrays.asList(session.getBusinessRemote());
                assertEquals(2, remoteInterfaces.size());
                assertTrue(remoteInterfaces.contains("foo.EmployerRemote"));
                assertTrue(remoteInterfaces.contains("foo.EmployerRemoteAdvanced"));
                return null;
            }
        });
    }

    public void testSession2() throws InterruptedException, IOException {
        TestUtilities.copyStringToFileObject(srcFO, "foo/Customer.java",
                "package foo;\n" +
                "import javax.ejb.*;\n" +
                "@Stateless()\n" +
                "public class Customer implements CustomerLocalA, CustomerLocalB {\n" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/CustomerLocalA.java",
                "package foo;\n" +
                "public interface CustomerLocalA {\n" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/CustomerLocalB.java",
                "package foo;\n" +
                "public interface CustomerLocalB {\n" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/Employee.java",
                "package foo;\n" +
                "import javax.ejb.*;\n" +
                "@Remote\n" +
                "@Stateful(name=\"SatisfiedEmployee\")\n" +
                "public class Employee implements EmployeeR1, EmployeeR2 {\n" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/EmployeeR1.java",
                "package foo;\n" +
                "import javax.ejb.*;\n" +
                "public interface EmployeeR1 {\n" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/EmployeeR2.java",
                "package foo;\n" +
                "import javax.ejb.*;\n" +
                "public interface EmployeeR2 {\n" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/Employer.java",
                "package foo;\n" +
                "import javax.ejb.*;\n" +
                "@Stateful()\n" +
                "public class Employer implements EmployerLocal, EmployerRemote {\n" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/EmployerLocal.java",
                "package foo;\n" +
                "public interface EmployerLocal {\n" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/EmployerRemote.java",
                "package foo;\n" +
                "import javax.ejb.*;\n" +
                "@Remote()\n" +
                "public interface EmployerRemote {\n" +
                "}");

        createEjbJarModel().runReadAction(new MetadataModelAction<EjbJarMetadata, Void>() {
            public Void run(EjbJarMetadata metadata) throws VersionNotSupportedException {
                // test Customer
                Session session = (Session) getEjbByEjbName(metadata.getRoot().getEnterpriseBeans().getSession(), "Customer");
                assertEquals(Session.SESSION_TYPE_STATELESS, session.getSessionType());
                assertEquals(2, session.getBusinessLocal().length);
                assertEquals("foo.CustomerLocalA", session.getBusinessLocal()[0]);
                assertEquals("foo.CustomerLocalB", session.getBusinessLocal()[1]);
                // test Employee
                session = (Session) getEjbByEjbName(metadata.getRoot().getEnterpriseBeans().getSession(), "SatisfiedEmployee");
                assertEquals(Session.SESSION_TYPE_STATEFUL, session.getSessionType());
                assertEquals(2, session.getBusinessRemote().length);
                // test Employer
                session = (Session) getEjbByEjbName(metadata.getRoot().getEnterpriseBeans().getSession(), "Employer");
                assertEquals(Session.SESSION_TYPE_STATEFUL, session.getSessionType());
                assertEquals(0, session.getBusinessLocal().length);
                List<String> remoteInterfaces = Arrays.asList(session.getBusinessRemote());
                assertEquals(1, remoteInterfaces.size());
                assertTrue(remoteInterfaces.contains("foo.EmployerRemote"));
                return null;
            }
        });
    }

    public void testSession3() throws InterruptedException, IOException {
        TestUtilities.copyStringToFileObject(srcFO, "javax/ejb/LocalBean.java",
                "package javax.ejb;\n" +
                "@Target(value = {ElementType.TYPE})\n" +
                "@Retention(value = RetentionPolicy.RUNTIME)\n" +
                "public @interface LocalBean {\n" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/Customer.java",
                "package foo;\n" +
                "import javax.ejb.*;\n" +
                "@LocalBean\n" +
                "@Stateless()\n" +
                "public class Customer implements CustomerLocalA, CustomerLocalB {\n" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/CustomerLocalA.java",
                "package foo;\n" +
                "public interface CustomerLocalA {\n" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/CustomerLocalB.java",
                "package foo;\n" +
                "public interface CustomerLocalB {\n" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/Employee.java",
                "package foo;\n" +
                "import javax.ejb.*;\n" +
                "@LocalBean\n" +
                "@Stateful(name=\"SatisfiedEmployee\")\n" +
                "public class Employee implements EmployeeR1, EmployeeR2 {\n" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/EmployeeR1.java",
                "package foo;\n" +
                "import javax.ejb.*;\n" +
                "public interface EmployeeR1 {\n" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/EmployeeR2.java",
                "package foo;\n" +
                "import javax.ejb.*;\n" +
                "@Remote()\n" +
                "public interface EmployeeR2 {\n" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/Employer.java",
                "package foo;\n" +
                "import javax.ejb.*;\n" +
                "@Stateful()\n" +
                "@Local(foo.EmployerLocal.class)\n" +
                "public class Employer implements EmployerLocal, EmployerRemote {\n" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/EmployerLocal.java",
                "package foo;\n" +
                "public interface EmployerLocal {\n" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/EmployerRemote.java",
                "package foo;\n" +
                "import javax.ejb.*;\n" +
                "public interface EmployerRemote {\n" +
                "}");

        createEjbJarModel().runReadAction(new MetadataModelAction<EjbJarMetadata, Void>() {
            public Void run(EjbJarMetadata metadata) throws VersionNotSupportedException {
                // test Customer
                Session session = (Session) getEjbByEjbName(metadata.getRoot().getEnterpriseBeans().getSession(), "Customer");
                assertEquals(Session.SESSION_TYPE_STATELESS, session.getSessionType());
                assertEquals(0, session.getBusinessLocal().length);
                assertEquals(0, session.getBusinessRemote().length);
                // test Employee
                session = (Session) getEjbByEjbName(metadata.getRoot().getEnterpriseBeans().getSession(), "SatisfiedEmployee");
                assertEquals(Session.SESSION_TYPE_STATEFUL, session.getSessionType());
                assertEquals(1, session.getBusinessRemote().length);
                assertEquals("foo.EmployeeR2", session.getBusinessRemote()[0]);
                // test Employer
                session = (Session) getEjbByEjbName(metadata.getRoot().getEnterpriseBeans().getSession(), "Employer");
                assertEquals(Session.SESSION_TYPE_STATEFUL, session.getSessionType());
                List<String> localInterfaces = Arrays.asList(session.getBusinessLocal());
                assertEquals(1, localInterfaces.size());
                assertTrue(localInterfaces.contains("foo.EmployerLocal"));
                List<String> remoteInterfaces = Arrays.asList(session.getBusinessRemote());
                assertEquals(0, remoteInterfaces.size());
                return null;
            }
        });
    }

    public void testSession4() throws InterruptedException, IOException {
        TestUtilities.copyStringToFileObject(srcFO, "foo/Customer.java",
                "package foo;\n" +
                "import javax.ejb.*;\n" +
                "@Stateless()\n" +
                "public class Customer implements CustomerLocalA {\n" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/CustomerLocalA.java",
                "package foo;\n" +
                "import javax.ejb.*;\n" +
                "@Remote\n" +
                "public interface CustomerLocalA {\n" +
                "}");

        createEjbJarModel().runReadAction(new MetadataModelAction<EjbJarMetadata, Void>() {
            public Void run(EjbJarMetadata metadata) throws VersionNotSupportedException {
                // test Customer
                Session session = (Session) getEjbByEjbName(metadata.getRoot().getEnterpriseBeans().getSession(), "Customer");
                assertEquals(Session.SESSION_TYPE_STATELESS, session.getSessionType());
                assertEquals(0, session.getBusinessLocal().length);
                assertEquals(1, session.getBusinessRemote().length);
                assertEquals("foo.CustomerLocalA", session.getBusinessRemote()[0]);
                return null;
            }
        });
    }
    
    public void testGetServiceRef() throws IOException, InterruptedException {
        TestUtilities.copyStringToFileObject(srcFO, "foo/FooService.java",
                "package foo;" +
                "@javax.jws.WebService()" +
                "public class FooService extends javax.xml.ws.Service {" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/FooServicePort.java",
                "package foo;" +
                "public interface FooServicePort {" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/FooServicePort1.java",
                "package foo;" +
                "public interface FooServicePort1 {" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/FooLocal.java",
                "package foo;" +
                "public interface FooLocal {" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/Foo1.java",
                "package foo;" +
                "import javax.ejb.*;" +
                "@Stateless()" +
                "public class Foo1 implements FooLocal {" +
                "@javax.xml.ws.WebServiceRef()" +
                "private FooService service;" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/Foo2.java",
                "package foo;" +
                "import javax.ejb.*;" +
                "@Stateless()" +
                "public class Foo2 implements FooLocal {" +
                "@javax.xml.ws.WebServiceRef(value=foo.FooService.class, name=\"service/Foo\", wsdlLocation=\"http://www.netbeans.org/FooService?wsdl\")" +
                "private FooServicePort port;" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/Foo3.java",
                "package foo;" +
                "import javax.ejb.*;" +
                "@Stateless()" +
                "public class Foo3 implements FooLocal {" +
                "@javax.xml.ws.WebServiceRef(foo.FooService.class)" +
                "private FooServicePort port;" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/Foo4.java",
                "package foo;" +
                "import javax.ejb.*;" +
                "@Stateless()" +
                "public class Foo4 implements FooLocal {" +
                "@javax.xml.ws.WebServiceRef(name=\"service/Foo\",value=foo.FooService.class)" +
                "private FooServicePort port;" +
                "@javax.xml.ws.WebServiceRef(name=\"service/Foo\",value=foo.FooService.class)" +
                "private FooServicePort1 port1;" +
                "}");
        
        createEjbJarModel().runReadAction(new MetadataModelAction<EjbJarMetadata, Void>() {
            public Void run(EjbJarMetadata metadata) throws VersionNotSupportedException {
                // test Foo1
                Session session1 = (Session) getEjbByEjbName(metadata.getRoot().getEnterpriseBeans().getSession(), "Foo1");
                assertEquals(1, session1.getServiceRef().length);
                assertEquals(1, session1.sizeServiceRef());
                ServiceRef serviceRef1 = session1.getServiceRef(0);
                assertEquals("foo.Foo1/service", serviceRef1.getServiceRefName());
                assertEquals("foo.FooService", serviceRef1.getServiceInterface());
                assertNull(serviceRef1.getWsdlFile());
                assertEquals(0,serviceRef1.sizePortComponentRef());
 
                Session session2 = (Session) getEjbByEjbName(metadata.getRoot().getEnterpriseBeans().getSession(), "Foo2");
                assertEquals(1, session2.getServiceRef().length);
                assertEquals(1, session2.sizeServiceRef());
                ServiceRef serviceRef2 = session2.getServiceRef(0);
                assertEquals("service/Foo", serviceRef2.getServiceRefName());
                assertEquals("foo.FooService", serviceRef2.getServiceInterface());
                assertEquals("http://www.netbeans.org/FooService?wsdl",serviceRef2.getWsdlFile().toASCIIString());
                assertEquals(1,serviceRef2.sizePortComponentRef());
                PortComponentRef portComponentRef2 = serviceRef2.getPortComponentRef(0);
                assertEquals("foo.FooServicePort",portComponentRef2.getServiceEndpointInterface());

                Session session3 = (Session) getEjbByEjbName(metadata.getRoot().getEnterpriseBeans().getSession(), "Foo3");
                assertEquals(1, session3.getServiceRef().length);
                assertEquals(1, session3.sizeServiceRef());
                ServiceRef serviceRef3 = session3.getServiceRef(0);
                assertEquals("foo.Foo3/port", serviceRef3.getServiceRefName());
                assertEquals("foo.FooService", serviceRef3.getServiceInterface());
                assertEquals(1,serviceRef3.sizePortComponentRef());
                PortComponentRef portComponentRef3 = serviceRef3.getPortComponentRef(0);
                assertEquals("foo.FooServicePort",portComponentRef3.getServiceEndpointInterface());
                
                Session session4 = (Session) getEjbByEjbName(metadata.getRoot().getEnterpriseBeans().getSession(), "Foo4");
                assertEquals(1, session4.getServiceRef().length);
                assertEquals(1, session4.sizeServiceRef());
                ServiceRef serviceRef4 = session4.getServiceRef(0);
                assertEquals("service/Foo", serviceRef4.getServiceRefName());
                assertEquals("foo.FooService", serviceRef4.getServiceInterface());
                assertEquals(2,serviceRef4.sizePortComponentRef());
                PortComponentRef portComponentRef4 = serviceRef4.getPortComponentRef(0);
                assertEquals("foo.FooServicePort",portComponentRef4.getServiceEndpointInterface());
                PortComponentRef portComponentRef41 = serviceRef4.getPortComponentRef(1);
                assertEquals("foo.FooServicePort1",portComponentRef41.getServiceEndpointInterface());
                return null;
            }
        });

    }

}
