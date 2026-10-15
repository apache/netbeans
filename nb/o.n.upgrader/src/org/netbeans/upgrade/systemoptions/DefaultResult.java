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

package org.netbeans.upgrade.systemoptions;

import java.util.Map;


class DefaultResult implements Result {
    private final Map<String, String> m;
    private final String instanceName;
    private String moduleName;    
    DefaultResult(String instanceName, Map<String, String> m) {
        this.instanceName = instanceName;
        this.m = m;
    }
    @Override
    public String getProperty(final String propName) {
        return m.get(propName);
    }
    
    @Override
    public String[] getPropertyNames() {
        return m.keySet().toArray(new String[m.size()]);
    }
    
    @Override
    public String getInstanceName() {
        return instanceName;
    }
    public String getModuleName() {
        return moduleName;
    }    
    public void setModuleName(String aModuleName) {
        moduleName = aModuleName;
    }        
}    
