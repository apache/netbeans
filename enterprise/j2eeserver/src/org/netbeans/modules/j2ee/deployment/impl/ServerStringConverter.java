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

package org.netbeans.modules.j2ee.deployment.impl;

import org.w3c.dom.Element;
import org.w3c.dom.DOMException;
import org.w3c.dom.NodeList;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.spi.settings.DOMConvertor;

import org.openide.util.NbBundle;
import org.openide.filesystems.*;

/**
 * @author  nn136682
 */
public class ServerStringConverter extends org.netbeans.spi.settings.DOMConvertor {

    private static final String E_SERVER_STRING = "server-string";
    private static final String E_TARGET = "target";
    private static final String A_PLUGIN = "plugin";
    private static final String A_URL = "url";
    private static final String A_NAME = "name";
    private static final String PUBLIC_ID = "-//org_netbeans_modules_j2ee//DTD ServerString 1.0//EN"; // NOI18N
    private static final String SYSTEM_ID = "nbres:/org/netbeans/modules/j2ee/deployment/impl/server-string.dtd"; // NOI18N
    
    public static boolean writeServerInstance(ServerString instance, String destDir, String destFile) {
        try {
            FileObject dir = FileUtil.getConfigFile(destDir);
            FileObject fo = FileUtil.createData(dir, destFile);
            try (FileLock lock = fo.lock();
                    Writer writer = new OutputStreamWriter(fo.getOutputStream(lock), StandardCharsets.UTF_8)) {
                create().write(writer, instance);
                return true;
            }
        } catch(Exception ioe) {
            Logger.getLogger("global").log(Level.WARNING, null, ioe);
            return false;
        }
    }

    public static ServerString readServerInstance(String fromDir, String fromFile) {
        try {
            FileObject dir = FileUtil.getConfigFile(fromDir);
            if (dir == null) {
                return null;
            }
            FileObject fo = dir.getFileObject (fromFile);
            if (fo == null)
                return null;
            
            try (Reader reader = new InputStreamReader(fo.getInputStream(), StandardCharsets.UTF_8)) {
                ServerString serverString = (ServerString) create().read(reader);
                return serverString;
            }
        } catch(Exception ioe) {
            Logger.getLogger("global").log(Level.WARNING, null, ioe);
            return null;
        }
    }
    
    public static DOMConvertor create() {
        return new ServerStringConverter();
    }
    
    /** Creates a new instance of ServerStringConverter */
    protected ServerStringConverter() {
        super(PUBLIC_ID, SYSTEM_ID, E_SERVER_STRING);
    }
    
    protected Object readElement(org.w3c.dom.Element element) throws java.io.IOException, ClassNotFoundException {
        NodeList targetElements =  element.getElementsByTagName(E_TARGET);
        String[] targets = new String[targetElements.getLength()];
        for (int i=0; i<targets.length; i++) {
            Element te = (Element) targetElements.item(i);
            targets[i] = te.getAttribute(A_NAME);
            if (targets[i] == null)
                throw new IOException(NbBundle.getMessage(ServerStringConverter.class, "MSG_ServerStringParseError", E_TARGET));
        }
        String plugin = element.getAttribute(A_PLUGIN);
        if (plugin == null)
            throw new IOException(NbBundle.getMessage(ServerStringConverter.class, "MSG_ServerStringParseError", A_PLUGIN));
        
        String url = element.getAttribute(A_URL);
        //if (plugin == null)
        //    throw new IOException(NbBundle.getMessage(ServerStringConverter.class, "MSG_ServerStringParseError", A_URL));

        return new ServerString(plugin, url, targets);
    }
    
    public void registerSaver(Object inst, org.netbeans.spi.settings.Saver s) {
        // Not needed:  there is not editing of ServerName
    }
    public void unregisterSaver(Object inst, org.netbeans.spi.settings.Saver s) {
        // Not needed:  there is not editing of ServerName
    }
    protected void writeElement(org.w3c.dom.Document doc, org.w3c.dom.Element element, Object obj) throws IOException, DOMException {
        if (obj == null)
            return;
        
        if (! (obj instanceof ServerString))
            throw new DOMException(
            DOMException.NOT_SUPPORTED_ERR, 
            NbBundle.getMessage(ServerStringConverter.class, "MSG_NotSupportedObject", obj.getClass()));
        
        ServerString ss = (ServerString) obj;
        if (ss.getPlugin() == null)
            throw new IOException(NbBundle.getMessage(ServerStringConverter.class, "MSG_BadServerString", ss));

        String[] targets = ss.getTargets();
        if (targets == null)
            targets = new String[0];
        
        for (int i=0; i<targets.length; i++) {
            Element targetElement = doc.createElement (E_TARGET);
            targetElement.setAttribute(A_NAME, targets[i]);
            element.appendChild (targetElement);
        }
        String url = ss.getUrl();
        if (url == null)
            url = "";
        element.setAttribute(A_URL, url);
        element.setAttribute(A_PLUGIN, ss.getPlugin());
    }
}
