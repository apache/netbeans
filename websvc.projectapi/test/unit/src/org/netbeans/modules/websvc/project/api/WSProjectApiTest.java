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
    
