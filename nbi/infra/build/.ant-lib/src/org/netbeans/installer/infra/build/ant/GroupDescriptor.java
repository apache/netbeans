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

package org.netbeans.installer.infra.build.ant;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.netbeans.installer.infra.build.ant.utils.Utils;

/**
 * This class is an ant task which creates a group package descriptor based on the
 * existing project properties and writes it to the specified file.
 *
 * @author Kirill Sorokin
 */
public class GroupDescriptor extends Task {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    /**
     * File to which the group descriptor should be written.
     */
    private File file;
    
    // setters //////////////////////////////////////////////////////////////////////
    /**
     * Setter for the 'file' property.
     * 
     * @param path The new value of the 'file' property.
     */
    public void setFile(final String path) {
        file = new File(path);
        if (!file.equals(file.getAbsoluteFile())) {
            file = new File(getProject().getBaseDir(), path);
        }
    }
    
    // execution ////////////////////////////////////////////////////////////////////
    /**
     * Executes the task. This method writes the group package descriptor xml code
     * to the specified file.
     * 
     * @throws org.apache.tools.ant.BuildException if a I/O error occurs.
     */
    public void execute() throws BuildException {        
        Utils.setProject(getProject());
        
        final StringBuilder xml = new StringBuilder();
        
        // header ///////////////////////////////////////////////////////////////////
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); // NOI18N
        xml.append("<registry " +
                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +// NOI18N 
                "xsi:noNamespaceSchemaLocation=\"registry.xsd\">\n"); // NOI18N
        xml.append("    <components>\n"); // NOI18N
        
        // core data ////////////////////////////////////////////////////////////////
        final String uid = get("group.uid"); // NOI18N
        final String offset = get("group.offset"); // NOI18N
        final String expand = get("group.expand"); // NOI18N
        final String visible = get("group.visible"); // NOI18N
        
        xml.append("        <group uid=\"" + uid + "\" " + // NOI18N
                "offset=\"" + offset + "\" " + // NOI18N
                "expand=\"" + expand + "\" " + // NOI18N
                "built=\"" + new Date().getTime() + "\" " + // NOI18N
                "visible=\"" + visible + "\">\n"); // NOI18N
        
        // locales //////////////////////////////////////////////////////////////////
        final String locales = 
                get("group.locales.list").trim(); // NOI18N
        
        // display name /////////////////////////////////////////////////////////////
        String displayName = 
                get("group.display.name.default"); // NOI18N
        
        xml.append("            <display-name>\n"); // NOI18N
        xml.append("                <default><![CDATA[" + Utils.toAscii(displayName) + // NOI18N
                "]]></default>\n"); // NOI18N
        
        if (!locales.equals("")) { // NOI18N
            for (String locale: locales.split(" ")) { // NOI18N
                displayName = get(
                        "group.display.name." + locale); // NOI18N
                if (displayName != null) {
                    xml.append("                <localized locale=\"" + // NOI18N
                            locale + "\"><![CDATA[" + // NOI18N
                            Utils.toAscii(displayName) + "]]></localized>\n"); // NOI18N
                }
            }
        }
        xml.append("            </display-name>\n"); // NOI18N
        
        // description //////////////////////////////////////////////////////////////
        String description = 
                get("group.description.default"); // NOI18N
        
        xml.append("            <description>\n"); // NOI18N
        xml.append("                <default><![CDATA[" + Utils.toAscii(description) + // NOI18N
                "]]></default>\n"); // NOI18N
        
        if (!locales.equals("")) { // NOI18N
            for (String locale: locales.split(" ")) { // NOI18N
                description = get(
                        "group.description." + locale); // NOI18N
                if (description != null) {
                    xml.append("                <localized locale=\"" + // NOI18N
                            locale + "\"><![CDATA[" + // NOI18N
                            Utils.toAscii(description) + "]]></localized>\n"); // NOI18N
                }
            }
        }
        xml.append("            </description>\n"); // NOI18N
        
        // icon /////////////////////////////////////////////////////////////////////
        final String size = get("group.icon.size"); // NOI18N
        final String md5 = get("group.icon.md5"); // NOI18N
        final String uri = get("group.icon.correct.uri"); // NOI18N
        
        xml.append("            <icon " + // NOI18N
                "size=\"" + size + "\" " + // NOI18N
                "md5=\"" + md5 + "\">\n"); // NOI18N
        xml.append("                <default-uri>" + // NOI18N
                uri.replace(" ", "%20") + "</default-uri>\n"); // NOI18N
        xml.append("            </icon>\n"); // NOI18N
        
        // properties ///////////////////////////////////////////////////////////////
        final int length = Integer.parseInt(
                get("group.properties.length")); // NOI18N
        
        if (length > 0) {
            xml.append("            <properties>\n"); // NOI18N
            for (int i = 1; i <= length; i++) {
                final String name = get(
                        "group.properties." + i + ".name"); // NOI18N
                final String value = get(
                        "group.properties." + i + ".value"); // NOI18N
                xml.append("                <property name=\"" + // NOI18N
                        name + "\">" + value + "</property>\n"); // NOI18N
            }
            xml.append("            </properties>\n"); // NOI18N
        }
        
        xml.append("        </group>\n"); // NOI18N
        xml.append("    </components>\n"); // NOI18N
        xml.append("</registry>\n"); // NOI18N
        
        
        try {
            Utils.write(file, xml);
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }
    // private //////////////////////////////////////////////////////////////////////
    /**
     * Gets a group's property's string value.
     *
     * @param name Name of the property whose value is required.
     * @return The value of the property as a string.
     */
    private String get(String name) {
        return Utils.resolveProperty(getProject().getProperty(name));
    }
}
