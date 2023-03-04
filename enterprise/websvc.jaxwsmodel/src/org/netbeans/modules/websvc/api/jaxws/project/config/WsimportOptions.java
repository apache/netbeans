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

/**
 *
 * @author rico
 */
public class WsimportOptions {

    private org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.WsimportOptions wsimportOptions;

    public WsimportOptions(org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.WsimportOptions wsimportOptions) {
        this.wsimportOptions = wsimportOptions;
    }

    public org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.WsimportOptions getOriginal() {
        return wsimportOptions;
    }

    public WsimportOption newWsimportOption() {
        org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.WsimportOption wsimportOption = wsimportOptions.newWsimportOption();
        return new WsimportOption(wsimportOption);
    }

    public void addWsimportOption(WsimportOption wsimportOption) {
        wsimportOptions.addWsimportOption(wsimportOption.getOriginal());
    }

    public void removeWsimportOption(WsimportOption wsimportOption) {
        wsimportOptions.removeWsimportOption(wsimportOption.getOriginal());
    }
    
    public void clearWsimportOptions(){
        org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.WsimportOption[] options = wsimportOptions.getWsimportOption();
        for(int i = 0; i < options.length; i++){
            wsimportOptions.removeWsimportOption(options[i]);
        }
    }

    public WsimportOption[] getWsimportOptions() {
        org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.WsimportOption[] options = wsimportOptions.getWsimportOption();
        WsimportOption[] wsimportOptions = new WsimportOption[options.length];
        for (int i = 0; i < options.length; i++) {
            wsimportOptions[i] = new WsimportOption(options[i]);
        }
        return wsimportOptions;
    }
}
