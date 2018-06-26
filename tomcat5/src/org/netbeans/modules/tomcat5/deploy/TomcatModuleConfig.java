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
import org.netbeans.modules.tomcat5.config.gen.Context;
import org.netbeans.modules.tomcat5.config.gen.Engine;
import org.netbeans.modules.tomcat5.config.gen.Host;
import org.netbeans.modules.tomcat5.config.gen.SContext;
import org.netbeans.modules.tomcat5.config.gen.Server;
import org.netbeans.modules.tomcat5.config.gen.Service;


/**
 * <code>TomcatModuleConfig</code> offers easy access to some context.xml and
 * server.xml settings.
 *
 * @author Stepan Herold
 */
public class TomcatModuleConfig {
    private static final String CONTEXT_XML_PATH = "/META-INF/context.xml"; // NOI18N

    private File contextXml;
    private File serverXml;

    private long timestampContextXML;
    private long timestampServerXML;
    private String path;
    
    
    // context logger settings
    private boolean hasLogger;
    private String loggerClassName;
    private String loggerDir;
    private String loggerPrefix;
    private String loggerSuffix;
    private boolean loggerTimestamp;
    
    /** 
     * Creates a new instance of TomcatModuleConfig.
     *
     * @param docBase document base class.
     * @param path context path.
     * @param serverXmlPath path to server.xml file.
     */
    public TomcatModuleConfig(String docBase, String path, String serverXmlPath) {
        if (path.equals("/")) {
            this.path = ""; // NOI18N
        } else {
            this.path = path;
        }
        contextXml = new File(docBase + CONTEXT_XML_PATH);
        serverXml = new File(serverXmlPath);
        refresh();
    }
    
    /**
     * Returns context from META-INF/context.xml if exists, <code>null</code> otherwise
     * @return context from META-INF/context.xml if exists, <code>null</code> otherwise
     */
    private Context getContext() {
        try {
            timestampContextXML = contextXml.lastModified();
            Context ctx = Context.createGraph(contextXml);
            return ctx;
        } catch (IOException ioe) {
            Logger.getLogger(TomcatModuleConfig.class.getName()).log(Level.INFO, null, ioe);
            return null;
        } catch (RuntimeException e) {
            Logger.getLogger(TomcatModuleConfig.class.getName()).log(Level.INFO, null, e);
            return null;
        }
    }
    
    /**
     * Returns context element from server.xml if defined, <code>null</code> otherwise
     * @return context element from server.xml if defined, <code>null</code> otherwise
     */
    private SContext getSContext() {        
        try {
            timestampServerXML = serverXml.lastModified();
            Server server = Server.createGraph(serverXml);
            
            // Looks for the first appearance of the service and host element.
            // (ide currently does not support multiple service and host elements).
            Service[] service = server.getService();
            if (service.length > 0) {
                Engine engine = service[0].getEngine();
                if (engine != null) {
                    Host[] host = engine.getHost();
                    if (host.length > 0) {
                        SContext[] sContext = host[0].getSContext();
                        for (int i = 0; i < sContext.length; i++) {
                            if (sContext[i].getAttributeValue("path").equals(path)) { // NOI18N
                                return sContext[i];
                            }
                        }
                    }
                }
            }
            
        } catch (IOException ioe) {
            Logger.getLogger(TomcatModuleConfig.class.getName()).log(Level.INFO, null, ioe);
        }
        return null;
    }
    
    /**
     * Returns <code>true</code> if there is a logger defined for this module, 
     * <code>false</code> otherwise.
     *
     * @return <code>true</code> if there is a logger defined for this module, 
     *         <code>false</code> otherwise.
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
    
    /**
     * Refresh cached values if the context.xml or server.xml file changed.
     */
    public void refresh() {
        if (contextXml.exists()) {
            long newTimestamp = contextXml.lastModified();
            if (newTimestamp > timestampContextXML) {
                timestampContextXML = newTimestamp;
                Context ctx = getContext();
                if (ctx != null) {
                    hasLogger = ctx.isLogger();
                    if (hasLogger) {
                        loggerClassName = ctx.getLoggerClassName();
                        loggerDir = ctx.getLoggerDirectory();
                        loggerPrefix = ctx.getLoggerPrefix();
                        loggerSuffix = ctx.getLoggerSuffix();
                        loggerTimestamp = Boolean.valueOf(ctx.getLoggerTimestamp()).booleanValue();
                        return;
                    }
                }
            }
        } else if (serverXml.exists()) {
            long newTimestamp = serverXml.lastModified();
            if (newTimestamp > timestampServerXML) {
                timestampServerXML = newTimestamp;
                SContext sCtx = getSContext();
                if (sCtx != null) {
                    hasLogger = sCtx.isLogger();
                    if (hasLogger) {
                        loggerClassName = sCtx.getAttributeValue(SContext.LOGGER, "className"); // NOI18N
                        loggerDir = sCtx.getAttributeValue(SContext.LOGGER, "directory"); // NOI18N
                        loggerPrefix = sCtx.getAttributeValue(SContext.LOGGER, "prefix"); // NOI18N
                        loggerSuffix = sCtx.getAttributeValue(SContext.LOGGER, "suffix"); // NOI18N
                        String timestamp = sCtx.getAttributeValue(SContext.LOGGER, "timestamp"); // NOI18N
                        loggerTimestamp = Boolean.valueOf(timestamp).booleanValue();
                    }
                }
            }
        } else {
            hasLogger = false; // this shouldn't happen
        }
    }
}
