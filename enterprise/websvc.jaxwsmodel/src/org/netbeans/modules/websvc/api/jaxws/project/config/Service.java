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

package org.netbeans.modules.websvc.api.jaxws.project.config;

/** Service information for wsimport utility
 */
public final class Service {
    org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Service  service;
    
    Service(org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Service  service) {
        this.service=service;
    }
    
    public void setName(java.lang.String value) {
        service.setName(value);
    }

    public java.lang.String getName() {
        return service.getName();
    }

    public void setImplementationClass(String value) {
        service.setImplementationClass(value);
    }

    public String getImplementationClass() {
        return service.getImplementationClass();
    }

    public void setWsdlUrl(String value) {
        service.setWsdlUrl(value);
    }

    public String getWsdlUrl() {
        return service.getWsdlUrl();
    }
    
    public void setPortName(String value) {
        service.setPortName(value);
    }

    public String getPortName() {
        return service.getPortName();
    }
    
    public void setServiceName(String value) {
        service.setServiceName(value);
    }

    public String getServiceName() {
        return service.getServiceName();
    }
    
    public void setLocalWsdlFile(String value) {
        service.setLocalWsdlFile(value);
    }

    public String getLocalWsdlFile() {
        return service.getLocalWsdlFile();
    }
    
    public void setPackageName(String value) {
        service.setPackageName(value);
    }

    public String getPackageName() {
        return service.getPackageName();
    }
    
    public void setJvmArgs(String[] jvmArgs) {
        service.setJvmArg(jvmArgs);
    }

    public String[] getJvmArgs() {
        return service.getJvmArg();
    }
    
    public int sizeJvmArgs() {
        return service.sizeJvmArg();
    }
    
     public Binding newBinding(){
        return new Binding(service.newBinding());
    }

    public void setBindings(Binding[] bindings) {
        org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Binding[] origBindings =
                new org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Binding[bindings.length];
        for(int i = 0; i < bindings.length; i++){
            origBindings[i] = (org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Binding)bindings[i].getOriginal();
        }
        service.setBinding(origBindings);
    }
    
    public Binding getBindingByFileName(String fileName){
        Binding[] bindings = getBindings();
        for (int i = 0 ; i < bindings.length; i++){
            Binding binding = bindings[i];
            if(binding.getFileName().equals(fileName)){
                return binding;
            }
        }
        return null;
    }

    public Binding[] getBindings() {
        org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Binding[] bindings = service.getBinding();
        Binding[] newBindings = new Binding[bindings.length];
        for(int i = 0; i < bindings.length; i++){
            newBindings[i] = new Binding(bindings[i]);
        }
        return newBindings;
    }
    
    
    public void addBinding(Binding binding){
        service.addBinding((org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Binding)binding.getOriginal());
    }
    
    public void removeBinding(Binding binding){
        service.removeBinding((org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Binding)binding.getOriginal());
    }

    public void setCatalogFile(String value) {
        service.setCatalogFile(value);
    }

    public String getCatalogFile() {
        return service.getCatalogFile();
    }
    
    public String getHandlerBindingFile(){
        return service.getHandlerBindingFile();
    }
    
    public void setHandlerBindingFile(String file){
        service.setHandlerBindingFile(file);
    }
    
    public void setPackageNameForceReplace(boolean value) {
        if (value) service.setPackageNameForceReplace("true");
        else service.setPackageNameForceReplace(null);
    }
    
    public boolean isPackageNameForceReplace() {
        String value = service.getPackageNameForceReplace();
        return "true".equals(value);
    }
   
    public void setUseProvider(Boolean useProvider){
        service.setUseProvider(useProvider);
    }
    
    public boolean isUseProvider(){
        Boolean bool = service.getUseProvider();
        if(bool == null){
            bool = false;
        }    
        return bool;
    }
    
    public WsimportOptions newWsimportOptions(){
        return new WsimportOptions(service.newWsimportOptions());
    }
  
    public void setWsimportOptions(WsimportOptions options){
        service.setWsimportOptions(options.getOriginal());
    }
    
    public WsimportOptions getWsImportOptions(){
        org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.WsimportOptions options = service.getWsimportOptions();
        if(options == null){
            options = service.newWsimportOptions();
            service.setWsimportOptions(options);
        }
        return new WsimportOptions(options);
    }
 
}
