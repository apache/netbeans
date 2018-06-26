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

import java.io.IOException;
import java.util.List;
import org.netbeans.modules.j2ee.dd.api.client.AppClient;
import org.netbeans.modules.j2ee.dd.api.client.AppClientMetadata;
import org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef;
import org.netbeans.modules.j2ee.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;

/**
 *
 * @author Martin Adamek
 */
public class EjbRefHelperTest extends CommonTestCase {
    
    public EjbRefHelperTest(String testName) {
        super(testName);
    }

    public void testEjbRefsInClass() throws IOException, InterruptedException {
        TestUtilities.copyStringToFileObject(srcFO, "foo/FooLocal.java",
                "package foo;" +
                "public interface FooLocal {" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/FooLocal2.java",
                "package foo;" +
                "public interface FooLocal2 {" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/FooRemote.java",
                "package foo;" +
                "import javax.ejb.*;" +
                "@Remote()" +
                "public interface FooRemote {" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/Foo1.java",
                "package foo;" +
                "import javax.ejb.*;" +
                "@Stateless()" +
                "public class Foo1 implements FooLocal {" +
                "@EJB()" +
                "private FooLocal run;" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/Foo2.java",
                "package foo;" +
                "import javax.ejb.*;" +
                "@Stateless()" +
                "public class Foo2 implements FooLocal {" +
                "@EJB()" +
                "private void setIter(FooRemote it) {};" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/Foo3.java",
                "package foo;" +
                "import javax.ejb.*;" +
                "@Stateless()" +
                "public class Foo3 implements FooLocal {" +
                "@EJB(beanInterface=FooLocal.class, description=\"dsc\", mappedName=\"mFoo\", beanName=\"bFoo\", name=\"Foo\")" +
                "private FooRemote prop;" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/Foo4.java",
                "package foo;" +
                "import javax.ejb.*;" +
                "@Stateless()" +
                "public class Foo4 implements FooLocal {" +
                "@EJB(beanInterface=FooLocal.class, description=\"dsc\", mappedName=\"mFoo\", beanName=\"bFoo\", name=\"Foo\")" +
                "private void setIter(FooRemote it) {};" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/Foo5.java",
                "package foo;" +
                "import javax.ejb.*;" +
                "@EJB(beanInterface=FooLocal.class, description=\"dsc1\", mappedName=\"mFoo\", beanName=\"bFoo\", name=\"Foo\")" +
                "@Stateless()" +
                "public class Foo5 implements FooLocal {" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/Foo6.java",
                "package foo;" +
                "import javax.ejb.*;" +
                "@EJBs({" +
                "@EJB(beanInterface=FooLocal.class, description=\"dsc1\", mappedName=\"mFoo1\", beanName=\"bFoo1\", name=\"Foo1\")," +
                "@EJB(beanInterface=FooRemote.class, description=\"dsc2\", mappedName=\"mFoo2\", beanName=\"bFoo2\", name=\"Foo2\")," +
                "@EJB(beanInterface=FooLocal2.class, description=\"dsc3\", mappedName=\"mFoo3\", beanName=\"bFoo3\", name=\"Foo3\")" +
                "})" +
                "@Stateless()" +
                "public class Foo6 implements FooLocal {" +
                "}");
        
        createEjbJarModel().runReadAction(new MetadataModelAction<EjbJarMetadata, Void>() {
            public Void run(EjbJarMetadata metadata) throws VersionNotSupportedException {
                // test Foo1
                Session session = (Session) getEjbByEjbName(metadata.getRoot().getEnterpriseBeans().getSession(), "Foo1");
                assertEquals(1, session.getEjbLocalRef().length);
                // test Foo2
                session = (Session) getEjbByEjbName(metadata.getRoot().getEnterpriseBeans().getSession(), "Foo2");
                assertEquals(1, session.getEjbRef().length);
                // test Foo3
                session = (Session) getEjbByEjbName(metadata.getRoot().getEnterpriseBeans().getSession(), "Foo3");
                assertEquals(1, session.getEjbRef().length);
                // test Foo4
                session = (Session) getEjbByEjbName(metadata.getRoot().getEnterpriseBeans().getSession(), "Foo4");
                assertEquals(1, session.getEjbRef().length);
                // test Foo5
                session = (Session) getEjbByEjbName(metadata.getRoot().getEnterpriseBeans().getSession(), "Foo5");
                assertEquals(1, session.getEjbLocalRef().length);
                // test Foo6
                session = (Session) getEjbByEjbName(metadata.getRoot().getEnterpriseBeans().getSession(), "Foo6");
                assertEquals(2, session.getEjbLocalRef().length);
                assertEquals(1, session.getEjbRef().length);
                
                return null;
            }
        });

    }

    public void testEjbRefsOnClasspath() throws IOException, InterruptedException {
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/FooLocal.java",
                "package foo;" +
                "public interface FooLocal {" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/FooLocal2.java",
                "package foo;" +
                "public interface FooLocal2 {" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/FooRemote.java",
                "package foo;" +
                "import javax.ejb.*;" +
                "@Remote()" +
                "public interface FooRemote {" +
                "}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Foo1.java",
                "package foo;" +
                "import javax.ejb.*;" +
                "public class Foo1 {" +
                "@EJB()" +
                "private FooLocal ref1;" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/Foo2.java",
                "package foo;" +
                "import javax.ejb.*;" +
                "public class Foo2 {" +
                "@EJB()" +
                "private void setRef2(FooRemote whatever) {};" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/Foo3.java",
                "package foo;" +
                "import javax.ejb.*;" +
                "public class Foo3 {" +
                "@EJB(beanInterface=FooLocal.class, description=\"dRef3\", mappedName=\"mRef3\", beanName=\"bRef3\", name=\"ref3\")" +
                "private FooRemote prop;" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/Foo4.java",
                "package foo;" +
                "import javax.ejb.*;" +
                "public class Foo4 {" +
                "@EJB(beanInterface=FooLocal.class, description=\"dRef4\", mappedName=\"mRef4\", beanName=\"bRef4\", name=\"ref4\")" +
                "private void setIter(FooRemote it) {};" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/Foo5.java",
                "package foo;" +
                "import javax.ejb.*;" +
                "@EJB(beanInterface=FooLocal.class, description=\"dRef5\", mappedName=\"mef5\", beanName=\"bRef5\", name=\"ref5\")" +
                "public class Foo5 {" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/Foo6.java",
                "package foo;" +
                "import javax.ejb.*;" +
                "@EJBs({" +
                "@EJB(beanInterface=FooLocal.class, description=\"dRef6_1\", mappedName=\"mRef6_1\", beanName=\"bRef6_1\", name=\"ref6_1\")," +
                "@EJB(beanInterface=FooRemote.class, description=\"dRef6_2\", mappedName=\"mRef6_2\", beanName=\"bRef6_2\", name=\"ref6_2\")," +
                "@EJB(beanInterface=FooLocal2.class, description=\"dRef6_3\", mappedName=\"mRef6_3\", beanName=\"bRef6_3\", name=\"ref6_3\")" +
                "})" +
                "public class Foo6 {" +
                "}");
        
        createWebAppModel(false).runReadAction(new MetadataModelAction<WebAppMetadata, Void>() {
            public Void run(WebAppMetadata metadata) throws VersionNotSupportedException {
                List<EjbLocalRef> ejbLocalRefs = metadata.getEjbLocalRefs();
                List<EjbRef> ejbRefs = metadata.getEjbRefs();
                
                // local refs
                
                EjbLocalRef ref1 = findEjbLocalRef(ejbLocalRefs, "java:comp/env/foo.Foo1/ref1");
                assertNotNull(ref1);
                assertEquals("foo.FooLocal", ref1.getLocal());
                
                EjbLocalRef ref5 = findEjbLocalRef(ejbLocalRefs, "ref5");
                assertNotNull(ref5);
                assertEquals("foo.FooLocal", ref5.getLocal());
                
                EjbLocalRef ref6_1 = findEjbLocalRef(ejbLocalRefs, "ref6_1");
                assertNotNull(ref6_1);
                assertEquals("foo.FooLocal", ref6_1.getLocal());
                
                EjbLocalRef ref6_3 = findEjbLocalRef(ejbLocalRefs, "ref6_3");
                assertNotNull(ref6_3);
                assertEquals("foo.FooLocal2", ref6_3.getLocal());
                
                // remote refs
                
                EjbRef ref2 = findEjbRef(ejbRefs, "java:comp/env/foo.Foo2/ref2");
                assertNotNull(ref2);
                assertEquals("foo.FooRemote", ref2.getRemote());
                
                EjbRef ref3 = findEjbRef(ejbRefs, "ref3");
                assertNotNull(ref3);
                assertEquals("foo.FooRemote", ref3.getRemote());
                
                EjbRef ref4 = findEjbRef(ejbRefs, "ref4");
                assertNotNull(ref4);
                assertEquals("foo.FooRemote", ref4.getRemote());
                
                EjbRef ref6_2 = findEjbRef(ejbRefs, "ref6_2");
                assertNotNull(ref6_2);
                assertEquals("foo.FooRemote", ref6_2.getRemote());
                
                assertEquals(4, ejbLocalRefs.size());
                assertEquals(4, ejbRefs.size());
                
                return null;
            }
        });

        createAppClientModel().runReadAction(new MetadataModelAction<AppClientMetadata, Void>() {
            public Void run(AppClientMetadata metadata) throws VersionNotSupportedException {
                AppClient appClient = metadata.getRoot();
                
                EjbRef[] ejbRefs = appClient.getEjbRef();
                
                // remote refs
                
                EjbRef ref2 = findEjbRef(ejbRefs, "java:comp/env/foo.Foo2/ref2");
                assertNotNull(ref2);
                assertEquals("foo.FooRemote", ref2.getRemote());
                
                EjbRef ref3 = findEjbRef(ejbRefs, "ref3");
                assertNotNull(ref3);
                assertEquals("foo.FooRemote", ref3.getRemote());
                
                EjbRef ref4 = findEjbRef(ejbRefs, "ref4");
                assertNotNull(ref4);
                assertEquals("foo.FooRemote", ref4.getRemote());
                
                EjbRef ref6_2 = findEjbRef(ejbRefs, "ref6_2");
                assertNotNull(ref6_2);
                assertEquals("foo.FooRemote", ref6_2.getRemote());
                
                assertEquals(4, ejbRefs.length);
                
                return null;
            }
        });

    }
    
    private static EjbLocalRef findEjbLocalRef(List<EjbLocalRef> ejbLocalRefs, String ejbRefName) {
        for (EjbLocalRef ejbLocalRef : ejbLocalRefs) {
            if (ejbRefName.equals(ejbLocalRef.getEjbRefName())) {
                return ejbLocalRef;
            }
        }
        return null;
    }
    
    private static EjbRef findEjbRef(List<EjbRef> ejbRefs, String ejbRefName) {
        for (EjbRef ejbRef : ejbRefs) {
            if (ejbRefName.equals(ejbRef.getEjbRefName())) {
                return ejbRef;
            }
        }
        return null;
    }

    private static EjbRef findEjbRef(EjbRef[] ejbRefs, String ejbRefName) {
        for (EjbRef ejbRef : ejbRefs) {
            if (ejbRefName.equals(ejbRef.getEjbRefName())) {
                return ejbRef;
            }
        }
        return null;
    }
    
}
