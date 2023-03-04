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
/*
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
 * @author mkuchtiak
 */
public class Binding {
    private org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Binding binding;
    /** Creates a new instance of HandlerChain */
    public Binding(org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Binding binding) {
        this.binding=binding;
    }
    
    Object getOriginal() {
        return binding;
    }
    
    public String getFileName() {
        return binding.getFileName().trim();
    }
    
    public String getOriginalFileUrl(){
        return binding.getOriginalFileUrl().trim();
    }
   
    public void setFileName(String value) {
        binding.setFileName(value.trim());
    }
    
    public void setOriginalFileUrl(String value){
        binding.setOriginalFileUrl(value.trim());
    }
}
