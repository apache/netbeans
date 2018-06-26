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
