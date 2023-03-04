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

package org.netbeans.modules.j2ee.dd.impl.client.annotation;

import org.netbeans.modules.j2ee.dd.api.client.AppClient;
import org.netbeans.modules.j2ee.dd.api.client.AppClientMetadata;
import java.io.IOException;
import org.netbeans.modules.j2ee.dd.api.common.PortComponentRef;
import org.netbeans.modules.j2ee.dd.api.common.ServiceRef;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.impl.common.annotation.CommonTestCase;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;

/**
 *
 * @author Milan Kuchtiak
 */
public class AppClientImplTest extends CommonTestCase {

    public AppClientImplTest(String testName) {
        super(testName);
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
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Main.java",
                "package foo;" +
                "public class Main {" +
                "@javax.xml.ws.WebServiceRef(wsdlLocation=\"http://www.netbeans.org/FooService?wsdl\")" +
                "private FooService service;" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/Main1.java",
                "package foo;" +
                "public class Main1 {" +
                "@javax.xml.ws.WebServiceRef(value=foo.FooService.class, name=\"service/Foo1\", wsdlLocation=\"http://www.netbeans.org/FooService?wsdl\")" +
                "private FooServicePort port;" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/Main2.java",
                "package foo;" +
                "public class Main2 {" +
                "@javax.xml.ws.WebServiceRef(name=\"service/Foo2\",value=foo.FooService.class)" +
                "private FooServicePort port;" +
                "@javax.xml.ws.WebServiceRef(name=\"service/Foo2\",value=foo.FooService.class)" +
                "private FooServicePort1 port1;" +
                "}");
        
        createAppClientModel().runReadAction(new MetadataModelAction<AppClientMetadata, Void>() {
            public Void run(AppClientMetadata metadata) throws VersionNotSupportedException {
                AppClient appClient = metadata.getRoot();
                
                assertEquals(3, appClient.getServiceRef().length);
                assertEquals(3, appClient.sizeServiceRef());
                ServiceRef serviceRef = findServiceRef(appClient.getServiceRef(),"foo.Main/service");
                assertNotNull(serviceRef);
                assertEquals("foo.FooService", serviceRef.getServiceInterface());
                assertEquals("http://www.netbeans.org/FooService?wsdl",serviceRef.getWsdlFile().toASCIIString());
                assertEquals(0,serviceRef.sizePortComponentRef());
                
                ServiceRef serviceRef1 = findServiceRef(appClient.getServiceRef(),"service/Foo1");
                assertNotNull(serviceRef1);
                assertEquals("foo.FooService", serviceRef1.getServiceInterface());
                assertEquals("http://www.netbeans.org/FooService?wsdl",serviceRef1.getWsdlFile().toASCIIString());
                assertEquals(1,serviceRef1.sizePortComponentRef());
                PortComponentRef portComponentRef = serviceRef1.getPortComponentRef(0);
                assertEquals("foo.FooServicePort",portComponentRef.getServiceEndpointInterface());
                
                ServiceRef serviceRef2 = findServiceRef(appClient.getServiceRef(),"service/Foo2");
                assertNotNull(serviceRef2);
                assertEquals("foo.FooService", serviceRef2.getServiceInterface());
                assertEquals(2,serviceRef2.sizePortComponentRef());
                PortComponentRef portComponentRef1 = serviceRef2.getPortComponentRef(0);
                assertEquals("foo.FooServicePort",portComponentRef1.getServiceEndpointInterface());
                PortComponentRef portComponentRef2 = serviceRef2.getPortComponentRef(1);
                assertEquals("foo.FooServicePort1",portComponentRef2.getServiceEndpointInterface());
                return null;
            }
        });
        

    }
    
    private ServiceRef findServiceRef(ServiceRef[] refs, String name) {
        for (ServiceRef ref:refs) {
            if (name.equals(ref.getServiceRefName())) {
                return ref;
            }
        }
        return null;
    }

}
