
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
 * HandlerChain.java
 *
 * Created on March 19, 2006, 9:02 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.api.jaxws.project.config;

/**
 *
 * @author rico
 */
public class HandlerChain {
    private org.netbeans.modules.websvc.jaxwsmodel.handler_config1_0.HandlerChain chain;
    /** Creates a new instance of HandlerChain */
    public HandlerChain(org.netbeans.modules.websvc.jaxwsmodel.handler_config1_0.HandlerChain chain) {
        this.chain=chain;
    }
    
    Object getOriginal() {
        return chain;
    }
    
    public String getHandlerChainName() {
        return chain.getHandlerChainName();
    }
    
    public Handler[] getHandlers() {
        org.netbeans.modules.websvc.jaxwsmodel.handler_config1_0.Handler[] handlers = chain.getHandler();
        Handler[] newHandlers = new Handler[handlers.length];
        for (int i=0;i<handlers.length;i++) {
            newHandlers[i]=new Handler(handlers[i]);
        }
        return newHandlers;
    }
    
    public void setHandlerChainName(String value) {
        chain.setHandlerChainName(value);
    }
    
    public Handler newHandler() {
        org.netbeans.modules.websvc.jaxwsmodel.handler_config1_0.Handler handler = chain.newHandler();
        return new Handler(handler);
    }
    
    public void addHandler(String handlerName, String handlerClass) {
        org.netbeans.modules.websvc.jaxwsmodel.handler_config1_0.Handler handler = chain.newHandler();
        handler.setHandlerName(handlerName);
        handler.setHandlerClass(handlerClass);
        chain.addHandler(handler);
    }
    
    public boolean removeHandler(String handlerNameOrClass) {
        org.netbeans.modules.websvc.jaxwsmodel.handler_config1_0.Handler[] handlers = chain.getHandler();
        for (int i=0;i<handlers.length;i++) {
            if (handlerNameOrClass.equals(handlers[i].getHandlerName()) || 
                    handlerNameOrClass.equals(handlers[i].getHandlerClass())) {
                chain.removeHandler(handlers[i]);
                return true;
            }
        }
        return false;
    }
    
    public Handler findHandlerByName(String handlerName) {
        Handler[] handlers = getHandlers();
        for (int i=0;i<handlers.length;i++) {
            if (handlerName.equals(handlers[i].getHandlerName())) return handlers[i];
        }
        return null;
    }
}
