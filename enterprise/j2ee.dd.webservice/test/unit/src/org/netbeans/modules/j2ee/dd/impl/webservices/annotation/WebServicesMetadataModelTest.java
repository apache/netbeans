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
                
                
                return metadata.getRoot().sizeWebserviceDescription();
            }
        });
        
        assertSame(expectedResult, result);
    }
    
}
