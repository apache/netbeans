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

package org.netbeans.modules.tomcat5.util;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import org.netbeans.modules.tomcat5.deploy.TomcatManager.TomcatVersion;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.XMLDataObject;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Helper class for working with tomcat-users.xml file.
 * 
 * Please note that syntax of tomcat-users.xml is not strictly given. Accoring 
 * to Tomcat documentation the "user" element has a "name" attribute, also the
 * default tomcat-users.xml file that comes with Tomcat 5.x uses the "name" 
 * attribute. During the server startup, however, the "name" attribute is 
 * automatically changed to a "username" attribute. This class needs to handle
 * both cases.
 * 
 * @author sherold
 */
public class TomcatUsers {

    // do not create instances
    private TomcatUsers() {
    }
    
    /**
     * Creates an user with the specified username and password and gives him
     * the "manager" and the "admin" roles, if the user already exists the two
     * roles are added to him and the password is changed to the new one.
     * 
     * @param tomcatUsersFile tomcat-users.xml file.
     * @param username username.
     * @param password password.
     * 
     * @throws IOException if the file does not exist or an error occurs during 
     *         parsing it.
     */
    public static void createUser(File tomcatUsersFile, String username, String password,
            TomcatVersion version) throws IOException {
        Document doc = getDocument(tomcatUsersFile);
        Element root = doc.getDocumentElement();
        Element userElement = findUserByName(root, username);
        if (userElement == null) {
            userElement = doc.createElement("user"); // NOI18N
            userElement.setAttribute("username", username); // NOI18N
        }
        // add roles
        String roles = userElement.getAttribute("roles"); // NOI18N
        if (roles == null) {
            roles = ""; // NOI18N
        }
        StringBuilder newRoles = new StringBuilder(roles.trim());
        if (version.isAtLeast(TomcatVersion.TOMCAT_70)) {
            if (!hasRole(roles, "manager-script")) { // NOI18N
                if (newRoles.length() > 0 && !newRoles.toString().endsWith(",")) { // NOI18N
                    newRoles.append(',');
                }
                newRoles.append("manager-script"); // NOI18N
            }
        } else {
            if (!hasRole(roles, "manager")) { // NOI18N
                if (newRoles.length() > 0 && !newRoles.toString().endsWith(",")) { // NOI18N
                    newRoles.append(',');
                }
                newRoles.append("manager"); // NOI18N
            }
        }
        if (!hasRole(roles, "admin")) { // NOI18N
            if (!newRoles.toString().endsWith(",")) { // NOI18N
                newRoles.append(',');
            }
            newRoles.append("admin"); // NOI18N
        }
        userElement.setAttribute("roles", newRoles.toString()); // NOI18N
        userElement.setAttribute("password", password); // NOI18N
        root.appendChild(userElement);
        FileObject fo = FileUtil.toFileObject(tomcatUsersFile);
        if (fo == null) {
            throw new IOException(NbBundle.getMessage(TomcatUsers.class, "MSG_FileNotFound", tomcatUsersFile.getPath()));
        }
        try (OutputStream os = fo.getOutputStream()){
            XMLUtil.write(doc, os, "UTF-8"); // NOI18N
        }
    }
    
    /**
     * Returns true if the user exists and has the "manager" role.
     * 
     * @param tomcatUsersFile tomcat-users.xml file.
     * 
     * @return true if the user exists and has the "manager" role, false otherwise.
     * 
     * @throws IOException if the file does not exist or an error occurs during 
     *         parsing it.
     */
    public static boolean hasManagerRole(TomcatVersion version,
            File tomcatUsersFile, String username) throws IOException {

        Document doc = getDocument(tomcatUsersFile);
        Element root = doc.getDocumentElement();
        NodeList users = root.getElementsByTagName("user"); // NOI18N
        int length = users.getLength();
        for (int i = 0; i < length; i++) {
            Element user = (Element) users.item(i);
            String name = user.getAttribute("name"); // NOI18N
            if (name.length() == 0) {
                name = user.getAttribute("username"); // NOI18N
            }
            if (username.equals(name)) { // NOI18N
                String roles = user.getAttribute("roles"); // NOI18N
                if (version.isAtLeast(TomcatVersion.TOMCAT_70)) {
                    if (hasRole(roles, "manager-script")) { // NOI18N
                        return true;
                    }                    
                } else {
                    if (hasRole(roles, "manager")) { // NOI18N
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * Returns true if the user exists.
     * 
     * @param tomcatUsersFile tomcat-users.xml file.
     * 
     * @return true if the user exists, false otherwise.
     * 
     * @throws IOException if the file does not exist or an error occurs during 
     *         parsing it.
     */
    public static boolean userExists(File tomcatUsersFile, String username)  throws IOException {
        Document doc = getDocument(tomcatUsersFile);
        return findUserByName(doc.getDocumentElement(), username) != null;
    }
    
    private static Document getDocument(File tomcatUsersFile) throws IOException {
        FileObject fo = FileUtil.toFileObject(tomcatUsersFile);
        if (fo == null) {
            throw new IOException(NbBundle.getMessage(TomcatUsers.class, "MSG_FileNotFound", tomcatUsersFile.getPath()));
        }
        XMLDataObject dobj = (XMLDataObject) DataObject.find(fo);
        try {
            return dobj.getDocument();
        } catch (SAXException ex) {
            throw (IOException) (new IOException()).initCause(ex);
        }
    }
    
    private static Element findUserByName(Element root, String username) {
        NodeList users = root.getElementsByTagName("user"); // NOI18N
        int length = users.getLength();
        for (int i = 0; i < length; i++) {
            Element user = (Element) users.item(i);
            String name = user.getAttribute("name"); // NOI18N
            if (name.length() == 0) {
                name = user.getAttribute("username"); // NOI18N
            }
            if (username.equals(name)) {
                return user;
            }
        }
        return null;
    }
    
    private static boolean hasRole(String roles, String rolename) {
        for (String role : roles.split(",")) { // NOI18N
            if (rolename.equals(role.trim())) {
                return true;
            }
        }
        return false;
    }
}
