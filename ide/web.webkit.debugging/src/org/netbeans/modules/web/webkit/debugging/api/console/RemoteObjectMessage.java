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
package org.netbeans.modules.web.webkit.debugging.api.console;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging;
import org.netbeans.modules.web.webkit.debugging.api.debugger.PropertyDescriptor;
import org.netbeans.modules.web.webkit.debugging.api.debugger.RemoteObject;

/**
 *
 * @author Martin
 */
class RemoteObjectMessage extends ConsoleMessage {
    
    private final RemoteObject ro;
    private final WebKitDebugging webKit;
    
    RemoteObjectMessage(WebKitDebugging webKit, RemoteObject ro) {
        super(ro.getOwningProperty());
        this.webKit = webKit;
        this.ro = ro;
    }

    @Override
    public String getType() {
        return ro.getType().getName();
    }

    @Override
    public String getText() {
        if (ro.getType() == RemoteObject.Type.OBJECT) {
            String className = ro.getClassName();
            if (className != null &&
                className.startsWith("HTML") && className.endsWith("Element")) {
                
                List<PropertyDescriptor> properties = webKit.getRuntime().getRemoteObjectProperties(ro, true);
                for (PropertyDescriptor pd : properties) {
                    if ("outerHTML".equals(pd.getName())) {
                        return pd.getValue().getValueAsString();
                    }
                }
            }
            return ro.getDescription();
        }
        return ro.getValueAsString();
    }

    @Override
    public List<ConsoleMessage> getSubMessages() {
        if (ro.getType() == RemoteObject.Type.OBJECT) {
            List<PropertyDescriptor> properties = ro.getProperties();
            List<ConsoleMessage> propMessages = new ArrayList<ConsoleMessage>(properties.size());
            for (PropertyDescriptor pd : properties) {
                // #229457 - prevent NPE
                if (pd.getValue() != null) {
                    propMessages.add(new PropertyMessage(webKit, pd));
                }
            }
            return propMessages;
        } else {
            return super.getSubMessages();
        }
    }
    
}
