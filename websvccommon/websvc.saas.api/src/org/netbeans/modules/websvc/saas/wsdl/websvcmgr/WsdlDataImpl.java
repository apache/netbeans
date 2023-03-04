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
import org.netbeans.modules.websvc.jaxwsmodelapi.WSService;
import org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlData;
import org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlServiceProxyDescriptor;

/**
 *
 * @author rico
 */
public class WsdlDataImpl implements WsdlData{
    private String wsdlUrl;
    private String wsdlFile;
    private WSService wsService;
    private Status status;
    private String id;
    private String name;
    private boolean resolved;
    private List<PropertyChangeListener> propertyListeners = new ArrayList<PropertyChangeListener>();
    public static final String PROP_RESOLVED = "resolved";

    public WsdlDataImpl(String wsdlUrl){
        this.wsdlUrl = wsdlUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id){
        this.id = id;
    }

    public String getOriginalWsdlUrl() {
        return wsdlUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getWsdlFile() {
        return wsdlFile;
    }

    public void setWsdlFile(String wsdlFile){
        this.wsdlFile = wsdlFile;
    }

    public WSService getWsdlService() {
        return wsService;
    }

    public void setWsdlService(WSService wsService){
        this.wsService = wsService;
    }

    public boolean isReady() {
        return status == Status.WSDL_RETRIEVED;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status){
        Status old = this.status;
        this.status = status;

        PropertyChangeEvent evt =
                new PropertyChangeEvent(this, PROP_STATE, old, status); // NOI18N

        for (PropertyChangeListener listener : propertyListeners) {
            listener.propertyChange(evt);
        }
    }

    public void setResolved(boolean resolved){
        Boolean old = this.resolved;
        this.resolved = resolved;
         PropertyChangeEvent evt =
                new PropertyChangeEvent(this, PROP_RESOLVED, old, this.resolved); // NOI18N

        for (PropertyChangeListener listener : propertyListeners) {
            listener.propertyChange(evt);
        }
    }

    public boolean isResolved(){
        return resolved;
    }
    public WsdlServiceProxyDescriptor getJaxWsDescriptor() {
        return null;
    }

    public WsdlServiceProxyDescriptor getJaxRpcDescriptor() {
        return null;
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertyListeners.add(l);
        
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertyListeners.remove(l);
    }

}
