/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.websvc.manager.model;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A model to keep track of web service group
 * Group Nodes are created using this model.
 * Default group does not a corresponding node
 * Its webservices are displayed directly under
 * Web Service root Node.
 * Group node has only the webservice Id List
 * @author Winston Prakash
 */

public class WebServiceGroup {
    
    Set<WebServiceGroupListener> listeners = new HashSet<>();
    String groupId = null;
    String groupName = null;
    boolean userDefined = true;
    
    Set<String> webserviceIds = new HashSet<String>();
    
    public WebServiceGroup() {
        this(WebServiceListModel.getInstance().getUniqueWebServiceGroupId());
    }
    
    public WebServiceGroup(String id) {
        setId(id);
    }
    
    public void addWebServiceGroupListener(WebServiceGroupListener listener){
        listeners.add(listener);
    }
    
    public void removeWebServiceGroupListener(WebServiceGroupListener listener){
        listeners.remove(listener);
    }
    
    public void setId(String id){
        groupId = id;
    }
    
    public String getId(){
        return groupId;
    }
    
    public String getName() {
        return groupName;
    }
    
    public void setName(String name) {
        modelDirty();
        groupName = name;
    }
    
    public boolean isUserDefined() {
        return userDefined;
    }
    
    public void setUserDefined(boolean v) {
        userDefined = v;
    }
    
    public void add(String webServiceId) {
        add(webServiceId, false);
    }
    
    public void remove(String webServiceId) {
        remove(webServiceId, false);
    }
    
    public void add(String webServiceId, boolean quietly) {
        if (!webserviceIds.contains(webServiceId)) {
            WebServiceData wsData = WebServiceListModel.getInstance().getWebService(webServiceId);
            wsData.setGroupId(getId());
            webserviceIds.add(webServiceId);
            
            if (quietly) return;
            Iterator<WebServiceGroupListener> iter = listeners.iterator();
            while(iter.hasNext()) {
                WebServiceGroupEvent evt = new  WebServiceGroupEvent(webServiceId, getId());
                ((WebServiceGroupListener)iter.next()).webServiceAdded(evt);
            }
        }else if (!quietly) {
            // This is a hack to make the nodes to appear while restoring 
            // the W/S meta data at IDE start (lag due to WSDL parsing)
            Iterator<WebServiceGroupListener> iter = listeners.iterator();
            while(iter.hasNext()) {
                WebServiceGroupEvent evt = new  WebServiceGroupEvent(webServiceId, getId());
                ((WebServiceGroupListener)iter.next()).webServiceAdded(evt);
            }
        }
    }
    
    public void remove(String webServiceId, boolean quietly){
        //System.out.println("WebServiceGroup remove called - " + webServiceId);
        if (webserviceIds.contains(webServiceId)) {
            webserviceIds.remove(webServiceId);
            if (quietly) return;
            
            Iterator<WebServiceGroupListener> iter = listeners.iterator();
            while(iter.hasNext()) {
                WebServiceGroupEvent evt = new  WebServiceGroupEvent(webServiceId, getId());
                ((WebServiceGroupListener)iter.next()).webServiceRemoved(evt);
            }
        }
    }
    
    public void modify(String webServiceId) {
        // It is here solely to notify the listners
        Iterator<WebServiceGroupListener> iter = listeners.iterator();
        while(iter.hasNext()) {
            WebServiceGroupEvent evt = new  WebServiceGroupEvent(webServiceId, getId());
            ((WebServiceGroupListener)iter.next()).webServiceRemoved(evt);
        }
    }
    
    public void setWebServiceIds(Set ids){
        webserviceIds = ids;
        Iterator iter = webserviceIds.iterator();
        while(iter.hasNext()) {
            WebServiceData wsData = WebServiceListModel.getInstance().getWebService((String)iter.next());
            wsData.setGroupId(getId());
        }
    }
    
    public Set<String> getWebServiceIds(){
        return webserviceIds;
    }
    /**
     * Partial Fix for Bug: 5107518
     * Changed so the web services will only be persisted if there is a change.
     * - David Botterill 9/30/2004
     */
    private void modelDirty() {
        WebServiceListModel.getInstance().setDirty(true);
    }
    
    @Override
    public boolean equals(Object o) {
        try {
            if ( !(o instanceof WebServiceGroup)){
                return false;
            }
            WebServiceGroup g2 = (WebServiceGroup)o;
            return g2.getId().equals(getId());
        }catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, ex.getLocalizedMessage(), ex);
            return false;
        }
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
