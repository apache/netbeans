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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Jan Becicka
 */
public class InfoPlist extends XMLFile {

    public InfoPlist(File f) throws IOException {
        super(f);
    }

    InfoPlist(InputStream resourceAsStream) throws IOException {
        super(resourceAsStream);
    }
    
    public String getBundleIdentifier() {
        Node n = getPackageNode();
        if (n==null) {
            return null;
        }
        return n.getTextContent();
    }
    
    private Node getPackageNode() {
        Node node = getNode("/plist/dict"); // NOI18N
        NodeList childNodes = node.getChildNodes();
        for (int i=0;i<childNodes.getLength(); i++) {
            final Node key = childNodes.item(i);
            if ("key".equals(key.getNodeName()) && "CFBundleIdentifier".equals(key.getTextContent())) { // NOI18N
                return childNodes.item(i+2);
            }
        }
        return null;
    }
    
    
    public void setBundleIdentifier(String pkg) {
        Node n = getPackageNode();
        if (n!=null) {
             n.setTextContent(pkg);
        }
    }
}
