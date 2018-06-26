/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.websvc.project.api;

import org.netbeans.modules.websvc.project.WebServiceAccessor;
import org.netbeans.modules.websvc.project.spi.WebServiceImplementation;
import org.openide.nodes.Node;

/**
 * Encapsulation of a web service. 
 * 
 * Clients make calls to instances of this class which are then delegated to the
 * (@link WebServiceImplementation).
 * 
 * @author  mkuchtiak
 */
public final class WebService {

    private WebServiceImplementation serviceImpl;
    

    static {
        WebServiceAccessor.DEFAULT = new WebServiceAccessor() {

            @Override
            public WebService createWebService(WebServiceImplementation serviceImpl) {
                return new WebService(serviceImpl);
            }
        };
    }

    private WebService(WebServiceImplementation serviceImpl) {
        this.serviceImpl = serviceImpl;
    }

    /**
     * Returns an identifier for the service provider or consumer
     */
    public String getIdentifier(){
        return serviceImpl.getIdentifier();
    }
    
    /**
     * Says whether this is a service provider or service consumer
     * @return true if it is a service provider, false if consumer
     */
    public boolean isServiceProvider() {
        return serviceImpl.isServiceProvider();
    }

    /**
     * Returns the type of service (e.g., SOAP, REST, etc)
     * @return Type of service
     */
    public Type getServiceType() {
        return serviceImpl.getServiceType();
    }

    /**
     * Creates Netbeans node representing the web service
     * @return Node representing the web service.
     */
    public Node createNode() {
        return serviceImpl.createNode();
    }

    /**
     * Returns the service descriptor for this web service implementation
     * @return ServiceDescriptor
     */
    public ServiceDescriptor getServiceDescriptor() {
        return serviceImpl.getServiceDescriptor();
    }

    /**
     * Enum that enumerates the web service types (SOAP, REST, etc) supported in Netbeans.
     */
    public static enum Type {
        /**
         * WSDL-based services
         */
        SOAP,
        /**
         * RESTful services
         */
        REST;
    }
}
