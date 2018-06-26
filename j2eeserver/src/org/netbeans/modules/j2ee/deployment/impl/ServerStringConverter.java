/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.j2ee.deployment.impl;

import org.w3c.dom.Element;
import org.w3c.dom.DOMException;
import org.w3c.dom.NodeList;
import java.io.*;
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
        FileLock lock = null;
        Writer writer = null;
        try {
            FileObject dir = FileUtil.getConfigFile(destDir);
            FileObject fo = FileUtil.createData(dir, destFile);
            lock = fo.lock();
            writer = new OutputStreamWriter(fo.getOutputStream(lock), "UTF-8"); // NOI18N
            create().write(writer, instance);
            return true;
            
        } catch(Exception ioe) {
            Logger.getLogger("global").log(Level.WARNING, null, ioe);
            return false;
        }
        finally {
            try {
            if (lock != null) lock.releaseLock();
            if (writer != null) writer.close();
            } catch (Exception e) {
                Logger.getLogger("global").log(Level.WARNING, null, e);
            }
        }
    }

    public static ServerString readServerInstance(String fromDir, String fromFile) {
        Reader reader = null;
        try {
            FileObject dir = FileUtil.getConfigFile(fromDir);
            if (dir == null) {
                return null;
            }
            FileObject fo = dir.getFileObject (fromFile);
            if (fo == null)
                return null;
            
            reader = new InputStreamReader(fo.getInputStream(), "UTF-8"); // NOI18N
            return (ServerString) create().read(reader);
        } catch(Exception ioe) {
            Logger.getLogger("global").log(Level.WARNING, null, ioe);
            return null;
        } finally {
            try {  if (reader != null) reader.close(); } catch(Exception e) {
                Logger.getLogger("global").log(Level.WARNING, null, e);
            }
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
