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

package org.netbeans.modules.websvc.api.jaxws.wsdlmodel;

import com.sun.tools.ws.processor.model.Model;
import com.sun.tools.ws.processor.model.Service;
import java.util.*;

/**
 *
 * @author mkuchtiak
 */
public class WsdlModel  implements org.netbeans.modules.websvc.jaxwsmodelapi.wsdlmodel.WsdlModel{

    private Model model;

    /** Creates a new instance of WsdlModel */
    WsdlModel(Model model) {
        this.model=model;
    }

    public Object /*com.sun.tools.ws.processor.model.Model*/ getInternalJAXWSModel() {
        return model;
    }

    public List<WsdlService> getServices() {
        List<WsdlService> wsdlServices = new ArrayList<WsdlService> ();
        if (model==null) return wsdlServices;
        List<Service> services = model.getServices();
        for (Service s:services)
            wsdlServices.add(new WsdlService(s));
        return wsdlServices;
    }

    public WsdlService getServiceByName(String serviceName) {
        List<Service> services = model.getServices();
        for (Service s:services)
            if (serviceName.equals(s.getName().getLocalPart())) return new WsdlService(s);
        return null;
    }
}
