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

/**
 * @author Radek Matous
 */
class HostPropertyProcessor extends PropertyProcessor {    
    HostPropertyProcessor() {
        super("org.netbeans.modules.httpserver.HttpServerSettings.HostProperty");//NOI18N
    }
    
    @Override
    void processPropertyImpl(String propertyName, Object value) {
        if ("hostProperty".equals(propertyName)) {//NOI18N
            for (Object elem : ((SerParser.ObjectWrapper)value).data) {
                if (elem instanceof SerParser.NameValue) {
                    SerParser.NameValue nv = (SerParser.NameValue)elem;
                    if (nv.value != null && nv.name != null) {
                        if (nv.name.name.equals("grantedAddresses")) {//NOI18N
                            addProperty(nv.name.name,nv.value.toString());//NOI18N
                        } else if (nv.name.name.equals("host")) {//NOI18N
                            addProperty(nv.name.name,nv.value.toString());
                        }
                    }
                }
            }            
        }  else {
            throw new IllegalStateException();
        }
    }
}
