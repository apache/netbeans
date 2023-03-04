
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
 *//*
 * HandlerChains.java
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
public class HandlerChains {
     private org.netbeans.modules.websvc.jaxwsmodel.handler_config1_0.HandlerChains handlerChains;
    /** Creates a new instance of HandlerChains */
    public HandlerChains(org.netbeans.modules.websvc.jaxwsmodel.handler_config1_0.HandlerChains handlerChains) {
        this.handlerChains = handlerChains;
    }
    
    public HandlerChain[] getHandlerChains() {
        org.netbeans.modules.websvc.jaxwsmodel.handler_config1_0.HandlerChain[] chains = handlerChains.getHandlerChain();
        HandlerChain[] newChains = new HandlerChain[chains.length];
        for (int i=0;i<chains.length;i++) {
            newChains[i]=new HandlerChain(chains[i]);
        }
        return newChains;
    }
    
    public HandlerChain newChain() {
        org.netbeans.modules.websvc.jaxwsmodel.handler_config1_0.HandlerChain chain = handlerChains.newHandlerChain();
        return new HandlerChain(chain);
    }
    
    public void addHandlerChain(String handlerName, HandlerChain chain) {
        handlerChains.addHandlerChain((org.netbeans.modules.websvc.jaxwsmodel.handler_config1_0.HandlerChain)chain.getOriginal());
    }
    
    public void removeHandlerChain(HandlerChain chain) {
        handlerChains.removeHandlerChain((org.netbeans.modules.websvc.jaxwsmodel.handler_config1_0.HandlerChain)chain.getOriginal());
    }
    
    public HandlerChain findHandlerChainByName(String handlerChainName) {
        HandlerChain[] chains = getHandlerChains();
        for (int i=0;i<chains.length;i++) {
            if (handlerChainName.equals(chains[i].getHandlerChainName())) return chains[i];
        }
        return null;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        handlerChains.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        handlerChains.removePropertyChangeListener(l);
    }
    
    public void merge(HandlerChains newChains) {
        if (newChains.handlerChains!=null)
            handlerChains.merge(newChains.handlerChains,BaseBean.MERGE_UPDATE);
    }
    
    public void write(OutputStream os) throws java.io.IOException {
        handlerChains.write(os);
    }
    
}
