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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
/*
 * Endpoints.java
 *
 * Created on March 19, 2006, 8:54 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.api.jaxws.project.config;

import java.beans.PropertyChangeListener;
import java.io.OutputStream;
import org.netbeans.modules.schema2beans.BaseBean;
/**
 *
 * @author Roderico Cruz
 */
public class Endpoints {
     private org.netbeans.modules.websvc.jaxwsmodel.endpoints_config1_0.Endpoints endpoints;
    /** Creates a new instance of HandlerChains */
    public Endpoints(org.netbeans.modules.websvc.jaxwsmodel.endpoints_config1_0.Endpoints endpoints) {
        this.endpoints = endpoints;
    }
    
    public Endpoint[] getEndpoints() {
        org.netbeans.modules.websvc.jaxwsmodel.endpoints_config1_0.Endpoint[] endpointArray = 
                endpoints.getEndpoint();
        Endpoint[] newEndpoints = new Endpoint[endpointArray.length];
        for (int i=0;i<endpointArray.length;i++) {
            newEndpoints[i]=new Endpoint(endpointArray[i]);
        }
        return newEndpoints;
    }
    
    public Endpoint newEndpoint() {
        org.netbeans.modules.websvc.jaxwsmodel.endpoints_config1_0.Endpoint endpoint = 
                endpoints.newEndpoint();
        return new Endpoint(endpoint);
    }
    
    public void addEnpoint(Endpoint endpoint) {
        endpoints.addEndpoint((org.netbeans.modules.websvc.jaxwsmodel.endpoints_config1_0.Endpoint)endpoint.getOriginal());
    }
    
    public void removeEndpoint(Endpoint endpoint) {
        endpoints.removeEndpoint((org.netbeans.modules.websvc.jaxwsmodel.endpoints_config1_0.Endpoint)endpoint.getOriginal());
    }
    
    public Endpoint findEndpointByName(String endpointName) {
        Endpoint[]endpnts = getEndpoints();
        for (int i=0;i<endpnts.length;i++) {
            Endpoint endpoint = endpnts[i];
            if(endpointName.equals(endpoint.getEndpointName())){
                return endpoint;
            }
        }
        return null;
    }
    
    public Endpoint findEndpointByImplementation(String className) {
        Endpoint[] endpnts = getEndpoints();
        for (int i=0;i<endpnts.length;i++) {
            Endpoint endpoint = endpnts[i];
            if(className.equals(endpoint.getImplementation())) {
                return endpoint;
            }
        }
        return null;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        endpoints.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        endpoints.removePropertyChangeListener(l);
    }
    
    public void merge(Endpoints newEndpoints) {
        if (newEndpoints.endpoints!=null)
            endpoints.merge(newEndpoints.endpoints,BaseBean.MERGE_UPDATE);
    }
    
    public void write(OutputStream os) throws java.io.IOException {
        endpoints.write(os);
    }
    
}
