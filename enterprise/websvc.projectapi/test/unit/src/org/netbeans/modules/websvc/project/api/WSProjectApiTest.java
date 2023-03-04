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
package org.netbeans.modules.websvc.project.api;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.websvc.project.spi.*;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.LookupMerger;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.test.MockLookup;

/**
 *
 * @author mkuchtiak
 */
public class WSProjectApiTest extends NbTestCase {
       
    public WSProjectApiTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
    }

    @Override
    protected void tearDown() throws Exception {
    }

    /** Test service model for AddNumbers service
     */
    public void testServiceModel() throws IOException {
        // TODO code application logic here
        final SOAP soap = new SOAP("http://localhost:8080/HelloService/HelloService.wsdl",
                                   "xml-resources/web-service-references/HelloService/wsdl/HelloService.wsdl");

        final ServiceDescriptorImplementation desc = new ServiceDescriptorImplementation() {


            public URL getRuntimeLocation() {
                try {
                    return new URL(soap.getRuntime());
                } catch (MalformedURLException ex) {
                    return null;
                }
            }

            public URI getRelativeURI() {
                try {
                    return new URI(soap.getRelative());
                } catch (URISyntaxException ex) {
                    return null;
                }
            }
        };

        WebServiceImplementation serviceImpl = new WebServiceImplementation() {

            public boolean isServiceProvider() {
                return true;
            }

            public WebService.Type getServiceType() {
                return WebService.Type.SOAP;
            }

            public ServiceDescriptor getServiceDescriptor() {
                return WebServiceFactory.createWebServiceDescriptor(desc);
            }

            public Node createNode() {
                return null;
            }

            public String getIdentifier() {
                return "TestService";
            }

        };
        final WebService service = WebServiceFactory.createWebService(serviceImpl);
      
        WebServiceImplementation clientImpl = new WebServiceImplementation() {

            public boolean isServiceProvider() {
                return false;
            }

            public WebService.Type getServiceType() {
                return WebService.Type.REST;
            }

            public ServiceDescriptor getServiceDescriptor() {
                return null;
            }

            public Node createNode() {
                return null;
            }

            public String getIdentifier() {
                return "TestClient";
            }

        };
        final WebService client = WebServiceFactory.createWebService(clientImpl);
       
        WebServiceDataProvider soapDataProvider = new WebServiceDataProvider() {

            public List<WebService> getServiceProviders() {
                return Collections.<WebService>singletonList(service);
            }

            public List<WebService> getServiceConsumers() {
                return Collections.<WebService>emptyList();
            }

            public void addPropertyChangeListener(PropertyChangeListener pcl) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void removePropertyChangeListener(PropertyChangeListener pcl) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        
        WebServiceDataProvider restDataProvider = new WebServiceDataProvider() {

            public List<WebService> getServiceProviders() {
                return Collections.<WebService>emptyList();
            }

            public List<WebService> getServiceConsumers() {
                return Collections.<WebService>singletonList(client);
            }

            public void addPropertyChangeListener(PropertyChangeListener pcl) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void removePropertyChangeListener(PropertyChangeListener pcl) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        
        MockLookup.setInstances(soapDataProvider, restDataProvider, LookupMergerSupport.createWebServiceDataProviderMerger() );
        LookupMerger<WebServiceDataProvider> o = Lookup.getDefault().lookup(LookupMerger.class);
        WebServiceDataProvider wsData = o.merge(Lookup.getDefault());
        
        assertNotNull(wsData);
        assertEquals(1, wsData.getServiceProviders().size());
        assertEquals(1, wsData.getServiceConsumers().size());

        WebService s = wsData.getServiceProviders().get(0);
        assertEquals(WebService.Type.SOAP, s.getServiceType());
        assertEquals("TestService", s.getIdentifier());
        assertTrue(s.isServiceProvider());
        assertEquals("http://localhost:8080/HelloService/HelloService.wsdl",s.getServiceDescriptor().getRuntimeLocation().toString());
 
        WebService c = wsData.getServiceConsumers().get(0);
        assertEquals(WebService.Type.REST, c.getServiceType());
        assertEquals("TestClient", c.getIdentifier());
        assertFalse(c.isServiceProvider());

    }
        
    class SOAP {
        String runtime, relative;

        public SOAP(String runtime, String relative) {
            this.runtime = runtime;
            this.relative = relative;
        }

        public String getRelative() {
            return relative;
        }

        public String getRuntime() {
            return runtime;
        }
    }
}
    
