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

package org.netbeans.modules.j2ee.dd.impl.webservices.annotation;

import java.io.IOException;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.webservices.PortComponent;
import org.netbeans.modules.j2ee.dd.api.webservices.WebservicesMetadata;
import org.netbeans.modules.j2ee.dd.api.webservices.WebserviceDescription;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;

/**
 *
 * @author Milan Kuchtiak
 */
public class WebServicesMetadataModelTest extends WebServicesTestCase {
    
    public WebServicesMetadataModelTest(String testName) {
        super(testName);
    }

    public void testModel() throws IOException, InterruptedException {
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Hello.java",
                "package foo;" +
                "@javax.jws.WebService()" +
                "public class Hello {" +
                "   public String hello() {" +
                "       return \"hello\"" +
                "   }" +               
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/Hello1.java",
                "package foo;" +
                "@javax.jws.WebService(serviceName=\"helloS\", portName = \"helloP\", name=\"hi\", targetNamespace=\"http://www.netbeans.org/hello\")" +
                "public class Hello1 {" +
                "   public String hello() {" +
                "       return \"hello\"" +
                "   }" +               
                "}");
        
        final int expectedResult = 2;
        
        Integer result = createModel().runReadAction(new MetadataModelAction<WebservicesMetadata, Integer>() {
            public Integer run(WebservicesMetadata metadata) {
                
                WebserviceDescription[] wsDesc = metadata.getRoot().getWebserviceDescription();
                assertNotNull(wsDesc);
                
                WebserviceDescription ws1 = metadata.findWebserviceByName("HelloService");
                assertNotNull(ws1);
                assertEquals(1,ws1.sizePortComponent());
                assertEquals("HelloService", ws1.getWebserviceDescriptionName());
                assertEquals("HelloService", ws1.getDisplayName());
                PortComponent port1 = ws1.getPortComponent(0);
                assertEquals("Hello",port1.getPortComponentName());
                try {
                    assertEquals("http://foo/",port1.getWsdlService().getNamespaceURI());
                    assertEquals("HelloService",port1.getWsdlService().getLocalPart());
                } catch (VersionNotSupportedException ex) {
                    throw new AssertionError(ex);
                }
                assertEquals("http://foo/",port1.getWsdlPort().getNamespaceURI());
                assertEquals("HelloPort",port1.getWsdlPort().getLocalPart());
                assertEquals("foo.Hello",port1.getServiceEndpointInterface());  
                assertEquals("foo.Hello",port1.getServiceEndpointInterface());
                assertEquals("Hello",port1.getServiceImplBean().getServletLink());
                
                
                WebserviceDescription ws2 = metadata.findWebserviceByName("helloS");
                assertNotNull(ws2);
                assertEquals(1,ws2.sizePortComponent());
                assertEquals("helloS", ws2.getWebserviceDescriptionName());
                PortComponent port2 = ws2.getPortComponent(0);
                assertEquals("hi",port2.getPortComponentName());
                 try {
                    assertEquals("http://www.netbeans.org/hello",port2.getWsdlService().getNamespaceURI());
                    assertEquals("helloS",port2.getWsdlService().getLocalPart());
                } catch (VersionNotSupportedException ex) {
                    throw new AssertionError(ex);
                }
                assertEquals("http://www.netbeans.org/hello",port2.getWsdlPort().getNamespaceURI());
                assertEquals("helloP",port2.getWsdlPort().getLocalPart());
                assertEquals("foo.Hello1",port2.getServiceEndpointInterface());
                assertEquals("hi",port2.getServiceImplBean().getServletLink());
                
                
                return Integer.valueOf(metadata.getRoot().sizeWebserviceDescription());
            }
        });
        
        assertSame(expectedResult, result);
    }
    
}
