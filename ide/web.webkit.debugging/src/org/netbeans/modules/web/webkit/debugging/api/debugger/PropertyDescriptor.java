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
package org.netbeans.modules.web.webkit.debugging.api.debugger;

import org.json.simple.JSONObject;
import org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging;

/**
 * Runtime.PropertyDescriptor
 */
public class PropertyDescriptor extends AbstractObject {
    
    private RemoteObject value;

    PropertyDescriptor(JSONObject property, WebKitDebugging webkit) {
        super(property, webkit);
        JSONObject ro = (JSONObject)property.get("value");
        if (ro != null) {
            value = new RemoteObject(ro, webkit, property);
        }
    }
    
    public String getName() {
        return (String)getObject().get("name");
    }

    public RemoteObject getValue() {
        return value;
    }
    
    public boolean isMutable() {
        return (Boolean)getObject().get("writable");
    }
    
}
