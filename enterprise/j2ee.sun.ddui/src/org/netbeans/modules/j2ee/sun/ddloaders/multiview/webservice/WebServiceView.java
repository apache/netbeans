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
package org.netbeans.modules.j2ee.sun.ddloaders.multiview.webservice;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceDescription;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.SunEjbJar;
import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;
import org.netbeans.modules.j2ee.sun.dd.api.client.SunApplicationClient;
import org.netbeans.modules.j2ee.sun.ddloaders.SunDescriptorDataObject;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.DDSectionNodeView;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.ServiceRefGroupNode;
import org.netbeans.modules.xml.multiview.SectionNode;


/**
 * @author Peter Williams
 */
public class WebServiceView extends DDSectionNodeView {

    private final Set<WebserviceDescription> webServiceCache = new HashSet<WebserviceDescription>();

    public WebServiceView(SunDescriptorDataObject dataObject) {
        super(dataObject);
        
        if(!(rootDD instanceof SunWebApp || rootDD instanceof SunEjbJar || rootDD instanceof SunApplicationClient)) {
            throw new IllegalArgumentException("Data object is not a root that contains webservice elements (" + rootDD + ")");
        }
        
        // web apps and ejb jars support web services.
        boolean hasWebServices = (rootDD instanceof SunWebApp || rootDD instanceof SunEjbJar);
        // ejb jars show the clients under the ejb node so hide them here.
        boolean hasGlobalClients = (rootDD instanceof SunWebApp || rootDD instanceof SunApplicationClient);
        
        LinkedList<SectionNode> children = new LinkedList<SectionNode>();
        if(hasWebServices) {
            children.add(new WebServiceGroupNode(this, rootDD, version));
        }
        if(hasGlobalClients) {
            children.add(new ServiceRefGroupNode(this, rootDD, version));
        }
        
        setChildren(children);
    }
    
}
