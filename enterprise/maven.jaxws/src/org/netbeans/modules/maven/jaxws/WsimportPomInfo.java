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

package org.netbeans.modules.maven.jaxws;

import java.util.List;

/** Used to model wsimport configuration in pom file for one client or service from wsdl.
 *
 * @author mkuchtiak
 */
public class WsimportPomInfo {
    private String wsdlPath;
    private String handlerFile;
    private List<String> bindingFiles;
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public WsimportPomInfo(String wsdlPath) {
        this.wsdlPath = wsdlPath;
    }

    public List<String> getBindingFiles() {
        return bindingFiles;
    }

    public void setBindingFiles(List<String> bindingFiles) {
        this.bindingFiles = bindingFiles;
    }

    public String getHandlerFile() {
        return handlerFile;
    }

    public void setHandlerFile(String handlerFile) {
        this.handlerFile = handlerFile;
    }

    public String getWsdlPath() {
        return wsdlPath;
    }

    public void setWsdlPath(String wsdlPath) {
        this.wsdlPath = wsdlPath;
    }
}
