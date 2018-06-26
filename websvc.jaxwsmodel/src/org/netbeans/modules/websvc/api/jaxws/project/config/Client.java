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
