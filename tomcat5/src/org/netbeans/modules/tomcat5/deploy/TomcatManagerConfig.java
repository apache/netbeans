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

package org.netbeans.modules.tomcat5.deploy;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.tomcat5.config.gen.Engine;
import org.netbeans.modules.tomcat5.config.gen.Host;
import org.netbeans.modules.tomcat5.config.gen.SContext;
import org.netbeans.modules.tomcat5.config.gen.Server;
import org.netbeans.modules.tomcat5.config.gen.Service;

/**
 * <code>TomcatManagerConfig</code> offers easy access to some server.xml settings.
 *
 * @author Stepan Herold
 */
public class TomcatManagerConfig {
    private File serverXml;
    private long timestamp;

    // shared context logger settings
    private boolean hasLogger;
    private String loggerClassName;
    private String loggerDir;
    private String loggerPrefix;
    private String loggerSuffix;
    private boolean loggerTimestamp;
    
    /** 
     * Creates a new instance of TomcatManagerConfig.
     * 
     * @param serverXmlPath path to server.xml file.
     */
    public TomcatManagerConfig(File serverXml) {
        this.serverXml = serverXml;
        refresh();
    }
    
    /**
     * Refresh cached values if the server.xml file changed.
     */
    public void refresh() {
        long newTimestamp = serverXml.lastModified();
        if (newTimestamp > timestamp) {
            timestamp = newTimestamp;            
            Host host = getHostElement();
            if (host != null && host.isLogger()) {
                hasLogger = true;
                loggerClassName = host.getAttributeValue(SContext.LOGGER, "className"); // NOI18N
                loggerDir = host.getAttributeValue(SContext.LOGGER, "directory"); // NOI18N
                loggerPrefix = host.getAttributeValue(SContext.LOGGER, "prefix"); // NOI18N
                loggerSuffix = host.getAttributeValue(SContext.LOGGER, "suffix"); // NOI18N
                String timestamp = host.getAttributeValue(SContext.LOGGER, "timestamp"); // NOI18N
                loggerTimestamp = Boolean.valueOf(timestamp).booleanValue();
            } else {
                Engine engine = getEngineElement();
                if  (engine != null && engine.isLogger()) {
                    hasLogger = true;
                    loggerClassName = engine.getAttributeValue(SContext.LOGGER, "className"); // NOI18N
                    loggerDir = engine.getAttributeValue(SContext.LOGGER, "directory"); // NOI18N
                    loggerPrefix = engine.getAttributeValue(SContext.LOGGER, "prefix"); // NOI18N
                    loggerSuffix = engine.getAttributeValue(SContext.LOGGER, "suffix"); // NOI18N
                    String timestamp = engine.getAttributeValue(SContext.LOGGER, "timestamp"); // NOI18N
                    loggerTimestamp = Boolean.valueOf(timestamp).booleanValue();
                } else {
                    hasLogger = false;
                }
            }
        }
    }
    
    /**
     * Return path to server.xml file.
     *
     * @return path to server.xml file.
     */
    public String serverXmlPath() {
        return serverXml.getAbsolutePath();
    }
    
    /**
     * Return bean representation of the Server element of the server.xml file.
     *
     * @return bean representation of the Server element of the server.xml file, 
     *         <code>null</code> if error occurs.
     */
    public Server getServerElement() {
        try {
            return Server.createGraph(serverXml);
        } catch (IOException ioe) {
            Logger.getLogger(TomcatManagerConfig.class.getName()).log(Level.INFO, null, ioe);
        } catch (RuntimeException e) {
            Logger.getLogger(TomcatManagerConfig.class.getName()).log(Level.INFO, null, e);
        }
        return null;
    }
    
    /**
     * Return engine element from server.xml if defined.
     * Looks only for the first appearance of the service element.
     * (ide currently does not support multiple service and host elements).
     */
    public Engine getEngineElement() {
        Server server = getServerElement();
        if (server == null) return null;
        Service[] service = server.getService();
        if (service.length > 0) {
            return service[0].getEngine();
        }
        return null;
    }
    
    /**
     * Return host element from server.xml if defined.
     * Looks only for the first appearance of the service and host element.
     * (ide currently does not support multiple service and host elements).
     */
    public Host getHostElement() {
        Engine engine = getEngineElement();
        if (engine != null) {
            Host[] host = engine.getHost();
            if (host.length > 0) {
                return host[0];
            }
        }
        return null;
    }
    
    /**
     * Return <code>true</code> if there is a logger defined in the first host 
     * or engine element in server.xml, <code>false</code> otherwise.
     *
     * @return <code>true</code> if there is a logger defined in the first host 
     *         or engine element in server.xml, <code>false</code> otherwise.
     */
    public boolean hasLogger() {
        return hasLogger;
    }
    
    /**
     * Return logger class name.
     *
     * @return logger class name.
     */
    public String loggerClassName() {
        return loggerClassName;
    }
    
    /**
     * Return logger directory.
     *
     * @return logger directory.
     */
    public String loggerDir() {
        return loggerDir;
    }

    /**
     * Return logger prefix.
     *
     * @return logger prefix.
     */
    public String loggerPrefix() {
        return loggerPrefix;
    }
    
    /**
     * Return logger suffix.
     *
     * @return logger suffix.
     */
    public String loggerSuffix() {
        return loggerSuffix;
    }

    /**
     * Return <code>true</code> whether logger timestamps messages, <code>false</code>
     * otherwise.
     *
     * @return <code>true</code> whether logger timestamps messages, <code>false</code>
     *         otherwise.
     */
    public boolean loggerTimestamp() {
        return loggerTimestamp;
    }    
}
