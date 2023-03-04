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
import java.util.StringTokenizer;
import javax.lang.model.SourceVersion;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * API for config.xml
 * @author Jan Becicka
 */
public class SourceConfig extends XMLFile {
    
    public static final String ANDROID_PLATFORM = "android"; // NOI18N
    public static final String IOS_PLATFORM = "ios"; // NOI18N

    public SourceConfig(InputStream resource) throws IOException {
        super(resource);
    }

    public SourceConfig(File f) throws IOException {
        super(f);
    }

    public String getName() {
        return getTextContent("/widget/name"); // NOI18N
    }
    
    public void setName(String name) {
        setTextContent("/widget/name", name); // NOI18N
    }

    public String getDescription() {
        return getTextContent("/widget/description"); // NOI18N
    }

    public void setDescription(String description) {
        setTextContent("/widget/description", description); // NOI18N
    }

    public String getAuthor() {
        return getTextContent("/widget/author"); // NOI18N
    }

    public void setAuthor(String author) {
        setTextContent("/widget/author", author); // NOI18N
    }
    
    public String getAuthorHref() {
        return getAttributeText("/widget/author", "href"); // NOI18N
    }

    public void setAuthorHref(String href) {
        setAttributeText("/widget/author", "href", href); // NOI18N
    }

    public String getAuthorEmail() {
        return getAttributeText("/widget/author", "email"); // NOI18N
    }

    public void setAuthorEmail(String email) {
        setAttributeText("/widget/author", "email", email); // NOI18N
    }
    
    public String getId() {
        return getAttributeText("/widget", "id"); // NOI18N
    }
    
    public String getVersion() {
        return getAttributeText("/widget", "version"); // NOI18N
    }
    
    public String getAccess() {
        return getAttributeText("/widget/access", "origin"); // NOI18N
    }
    
    public void setAccess(String access) {
        setAttributeText("/widget/access", "origin", access); // NOI18N
    }

    public void setId(String id) {
        if (!isValidId(id)) {
            throw new IllegalArgumentException("\"" + id + "\" is not a valid Application ID");
        }
       setAttributeText("/widget", "id", id); // NOI18N
    }
    
    public void setVersion(String version) {
        setAttributeText("/widget", "version", version); // NOI18N
    }

    public String getIcon(String platform, int width, int height) {
        return getSplashOrIcon("icon", platform, width, height); // NOI18N
    }

    public void setIcon(String platform, int width, int height, String value) {
        setSplashOrIcon("icon", platform, width, height, value); // NOI18N
    }
    
    
    private String getSplashOrIcon(String name, String platform, int width, int height) {
        Node node = getSplashOrIconNode(name, platform, width, height);
        if (node == null) {
            return null;
        }
        return getAttributeText(node, "src"); // NOI18N
    }
    
    private Node getSplashOrIconNode(String name, String platform, int width, int height) {
        final NodeList icons = doc.getElementsByTagName(name);
        for (int i=0; i < icons.getLength();i++) {
            Node n = icons.item(i);
            if (platform.equals(getAttributeText(n, "gap:platform")) && // NOI18N
                    Integer.toString(width).equals(getAttributeText(n, "width")) && // NOI18N
                    Integer.toString(height).equals(getAttributeText(n, "height"))) { // NOI18N
                return n;
            }
        }
        return null;
    }
    
    private void setSplashOrIcon(String name, String platform, int width, int height, String value) {
        Node n = getSplashOrIconNode(name, platform, width, height);
        if (n!=null) {
            final Attr src = doc.createAttribute("src"); // NOI18N
            src.setValue(value);
            n.getAttributes().setNamedItem(src);
        } else {
            Element element = doc.createElement(name);
            element.setAttribute("src", value); // NOI18N
            element.setAttribute("width", Integer.toString(width)); // NOI18N
            element.setAttribute("height", Integer.toString(height)); // NOI18N
            element.setAttribute("gap:platform", platform); // NOI18N
            NodeList elementsByTagName = doc.getElementsByTagName(name);
            Node widget = getNode("/widget"); // NOI18N
            if (elementsByTagName!=null && elementsByTagName.getLength()>0) {
                widget.insertBefore(element, elementsByTagName.item(0));
            } else {
                widget.appendChild(element);
            }
        }
    }

    
    public String getSplash(String platform, int width, int height) {
        return getSplashOrIcon("gap:splash", platform, width, height); // NOI18N
    }
    
    public void setSplash(String platform, int width, int height, String value) {
        setSplashOrIcon("gap:splash", platform, width, height, value); // NOI18N
    }
    
    public String getPreference(String name) {
        final NodeList pref = doc.getElementsByTagName("preference"); // NOI18N
        for (int i=0; i < pref.getLength();i++) {
            Node n = pref.item(i);
            if (name.equals(getAttributeText(n, "name"))) { // NOI18N
                return getAttributeText(n, "value"); // NOI18N
            }
        }
        return null;
    }
    
        
    public String getIcon(String platform) {
        if (platform.equals("ios")) { // NOI18N
            return getIcon(platform, 144,144);
        } else {
            return getIcon(platform, 96,96);
        }
    }    
    
    public void setIcon(String platform, String value) {
        if (platform.equals("ios")) { // NOI18N
            setIcon(platform, 144, 144, value);
        } else {
            setIcon(platform, 96, 96, value);
        }
    }

    public static boolean isValidId(String id) {
        if (id.endsWith(".")) { //NOI18N
            return false;
        }
        if (id.startsWith(".")) { //NOI18N
            return false;
        }
        if (id.contains("..")) { //NOI18N
            return false;
        }
        StringTokenizer tokenizer = new StringTokenizer(id, "."); //NOI18N
        while (tokenizer.hasMoreTokens()) {
            String part = tokenizer.nextElement().toString();
            // Cordova replaces all occurences of "-" with "_" during the build,
            // so it is valid to have a dash in app. id (issue #249337).
            part = part.replaceAll("-", "_"); //NOI18N
            if (part.isEmpty() || !SourceVersion.isIdentifier(part) || SourceVersion.isKeyword(part)) {
                return false;
            }
        }
        return true;
    }
}
