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
package org.netbeans.modules.websvc.saas.wsdl.websvcmgr;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.websvc.saas.model.Saas;
import org.netbeans.modules.websvc.saas.model.SaasGroup;
import org.netbeans.modules.websvc.saas.model.SaasServicesModel;
import org.netbeans.modules.websvc.saas.model.WsdlSaas;

/**
 *
 * @author rico
 */
public class WebServiceListManager implements PropertyChangeListener {

    private List<WsdlDataImpl> wsdlServices;
    private static WebServiceListManager mgr;
    private boolean initialized;

    public static WebServiceListManager getInstance() {
        if (mgr == null) {
            mgr = new WebServiceListManager();
        }
        return mgr;
    }

    private WebServiceListManager() {
    }

    public void init() {
        if (!initialized) {
            initialized = true;
            wsdlServices = new ArrayList<WsdlDataImpl>();
            SaasServicesModel saasServicesModel = SaasServicesModel.getInstance();
            saasServicesModel.addPropertyChangeListener(this);
            SaasGroup root = saasServicesModel.getRootGroup();
            List<SaasGroup> groups = root.getChildrenGroups();
            for (SaasGroup group : groups) {
                List<Saas> saasServices = group.getServices();
                for (Saas saasService : saasServices) {
                    if (saasService instanceof WsdlSaas) {
                        WsdlSaas wsdlSaas = (WsdlSaas) saasService;

                        wsdlServices.add(new WsdlDataImpl(wsdlSaas.getDelegate().getUrl()));
                    }
                }
            }
        }
    }

    public boolean wsdlDataExistsFor(String wsdlUrl) {
        init();
        for (WsdlDataImpl wsdlData : wsdlServices) {
            if (wsdlData.getOriginalWsdlUrl().equals(wsdlUrl)) {
                return true;
            }
        }
        return false;
    }

    public WsdlDataImpl findWsdlData(String wsdlUrl) {
        init();
        for (WsdlDataImpl wsdlData : wsdlServices) {
            if (wsdlData.getOriginalWsdlUrl().equals(wsdlUrl)) {
                return wsdlData;
            }
        }
        return null;
    }

    public WsdlDataImpl findReadyWsdlData(String wsdlUrl) {
        WsdlDataImpl wsdlData = findWsdlData(wsdlUrl);
        if(wsdlData != null && wsdlData.isReady()){
            return wsdlData;
        }
        return null;
    }

    public void addWsdlData(WsdlDataImpl wsdlData) {
        wsdlServices.add(wsdlData);
    }

    public void removeWsdlData(WsdlDataImpl wsdlData) {
        if (wsdlDataExistsFor(wsdlData.getOriginalWsdlUrl())) {
            wsdlServices.remove(wsdlData);
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
    }
}
