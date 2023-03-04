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
package org.netbeans.modules.cordova.updatetask;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * API for android res/xml/config.xml
 * @author Jan Becicka
 */
public class DeviceConfig extends XMLFile {

    public DeviceConfig(InputStream resource) throws IOException {
        super(resource);
        init();
    }

    private String root;
    public DeviceConfig(File androidConfigFile) throws IOException {
        super(androidConfigFile);
        init();
    }

    public String getAccess() {
        return getAttributeText(root + "/access", "origin"); // NOI18N
    }
    
    public void setAccess(String access) {
        setAttributeText(root + "/access", "origin", access); // NOI18N
    }
    
    public String getContent() {
        return getAttributeText(root + "/content", "src"); // NOI18N
    }
    
    public void setContent(String src) {
        setAttributeText(root + "/content", "src", src); // NOI18N
    }
    
    public void setPreference(String name, String value) {
        NodeList nodes = getXpathNodes(root + "/preference"); // NOI18N
        for (int i = 0; i<nodes.getLength();i++) {
            Node n = nodes.item(i);
            String nameAttr = getAttributeText(n, "name"); // NOI18N
            if (name.equals(nameAttr)) {
                ((Element) n).setAttribute("name", name); // NOI18N
                ((Element) n).setAttribute("value", value); // NOI18N
                return;
            }
        }
        Element createElement = doc.createElement("preference"); // NOI18N
        createElement.setAttribute("name", name); // NOI18N
        createElement.setAttribute("value", value); // NOI18N
        getXpathNode(root).appendChild(createElement);
    }

    public String getPreference(String name) {
        return getAttributeText(root + "/preference", name); // NOI18N
    }

    private void init() {
        if (getNode("/cordova") != null) { // NOI18N
            root = "/cordova"; // NOI18N
            //version 2.4
        } else {
            root = "/widget"; // NOI18N
            //version 2.5
        }
    }
    
}
