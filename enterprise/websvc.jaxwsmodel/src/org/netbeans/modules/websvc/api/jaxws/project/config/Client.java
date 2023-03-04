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

/** Client information for wsimport utility
 */
public final class Client {
    org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Client  client;
    
    Client(org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Client client) {
        this.client=client;
    }
    
    public void setName(java.lang.String value) {
        client.setName(value);
    }

    public java.lang.String getName() {
        return client.getName();
    }

    public void setWsdlUrl(String value) {
        client.setWsdlUrl(value);
    }
    
    public String getWsdlUrl() {
        return client.getWsdlUrl();
    }
    
    public void setLocalWsdlFile(String value) {
        client.setLocalWsdlFile(value);
    }
    
    public String getLocalWsdlFile() {
        return client.getLocalWsdlFile();
    }

    public void setCatalogFile(String value) {
        client.setCatalogFile(value);
    }

    public String getCatalogFile() {
        return client.getCatalogFile();
    }

    public void setPackageName(String value) {
        client.setPackageName(value);
    }

    public String getPackageName() {
        return client.getPackageName();
    }
    
    public void setJvmArgs(String[] jvmArgs) {
        client.setJvmArg(jvmArgs);
    }

    public String[] getJvmArgs() {
        return client.getJvmArg();
    }
    
    public int sizeJvmArgs() {
        return client.sizeJvmArg();
    }

    public Binding newBinding(){
        return new Binding(client.newBinding());
    }

    public void setBindings(Binding[] bindings) {
        org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Binding[] origBindings =
                new org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Binding[bindings.length];
        for(int i = 0; i < bindings.length; i++){
            origBindings[i] = (org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Binding)bindings[i].getOriginal();
        }
        client.setBinding(origBindings);
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
        org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Binding[] bindings = client.getBinding();
        Binding[] newBindings = new Binding[bindings.length];
        for(int i = 0; i < bindings.length; i++){
            newBindings[i] = new Binding(bindings[i]);
        }
        return newBindings;
    }
    
    
    public void addBinding(Binding binding){
        client.addBinding((org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Binding)binding.getOriginal());
    }
    
    public void removeBinding(Binding binding){
        client.removeBinding((org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Binding)binding.getOriginal());
    }
    
    public String getHandlerBindingFile(){
        return client.getHandlerBindingFile();
    }
    
    public void setHandlerBindingFile(String file){
        client.setHandlerBindingFile(file);
    }
    
    public void setPackageNameForceReplace(boolean value) {
        if (value) client.setPackageNameForceReplace("true");
        else client.setPackageNameForceReplace(null);
    }
    
    public boolean isPackageNameForceReplace() {
        String value = client.getPackageNameForceReplace();
        return "true".equals(value);
    }
    
    public void setUseDispatch(Boolean useDispatch) {
        client.setUseDispatch(useDispatch);
    }

    public Boolean getUseDispatch() {
        Boolean use = client.getUseDispatch();
        if(use == null){
            use = false;
        }
        return use;
    }
     public WsimportOptions newWsimportOptions(){
        return new WsimportOptions(client.newWsimportOptions());
    }
  
    public void setWsimportOptions(WsimportOptions options){
        client.setWsimportOptions(options.getOriginal());
    }
    
    public WsimportOptions getWsImportOptions(){
        org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.WsimportOptions options = client.getWsimportOptions();
        if(options == null){
            options = client.newWsimportOptions();
            client.setWsimportOptions(options);
        }
        return new WsimportOptions(options);
    }
 
}
