/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.tomcat5.j2ee;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.netbeans.modules.javaee.specs.support.api.JaxWs;
import org.netbeans.modules.websvc.wsstack.api.WSStack.Feature;
import org.netbeans.modules.websvc.wsstack.api.WSStack.Tool;
import org.netbeans.modules.websvc.wsstack.api.WSStackVersion;
import org.netbeans.modules.websvc.wsstack.api.WSTool;
import org.netbeans.modules.websvc.wsstack.spi.WSStackFactory;
import org.netbeans.modules.websvc.wsstack.spi.WSStackImplementation;
import org.netbeans.modules.websvc.wsstack.spi.WSToolImplementation;

/**
 *
 * @author mkuchtiak
 * @author ads
 */
public class JaxWsStack implements WSStackImplementation<JaxWs> {
    
    private static final String SHARED_METRO_LIBS[] = new String[] {
        "shared/lib/webservices-rt.jar",   // NOI18N
        "shared/lib/webservices-tools.jar" // NOI18N
    };

    private static final String GLOBAL_METRO_LIBS[] = new String[] {
        "lib/webservices-rt.jar",   // NOI18N
        "lib/webservices-tools.jar" // NOI18N
    };

    private int TOOLS_JAR_INDEX = 1;
    
    private static final String KEYSTORE_LOCATION = "certs/server-keystore.jks";  //NOI18N
    private static final String TRUSTSTORE_LOCATION = "certs/server-truststore.jks";  //NOI18N
    private static final String KEYSTORE_CLIENT_LOCATION = "certs/client-keystore.jks";  //NOI18N
    private static final String TRUSTSTORE_CLIENT_LOCATION = "certs/client-truststore.jks";  //NOI18N
    
    private File catalinaHome;
    private String version;
    private JaxWs jaxWs;
    
    public JaxWsStack(File catalinaHome) {
        this.catalinaHome = catalinaHome;
        try {
            version = resolveImplementationVersion();
            if (version == null) {
                // Default Version
                version = "2.1.4"; // NOI18N
            }
        } catch (IOException ex) {
            // Default Version
            version = "2.1.4"; // NOI18N
        };
        jaxWs = new JaxWs(getUriDescriptor());
    }

    @Override
    public JaxWs get() {
        return jaxWs;
    }

    @Override
    public WSStackVersion getVersion() {
        return WSStackFactory.createWSStackVersion(version);
    }

//    public Set<String> getSupportedTools() {
//        Set<String> supportedTools = new HashSet<String>();
//        if (isWsit()) {
//            supportedTools.add(WSStack.TOOL_WSGEN);
//            supportedTools.add(WSStack.TOOL_WSIMPORT);
//        }
//        if (isKeystore()) supportedTools.add(WSStack.TOOL_KEYSTORE);
//        if (isTruststore()) supportedTools.add(WSStack.TOOL_TRUSTSTORE);
//        if (isKeystoreClient()) supportedTools.add(WSStack.TOOL_KEYSTORE_CLIENT);
//        if (isTruststoreClient()) supportedTools.add(WSStack.TOOL_TRUSTSTORE_CLIENT);
//        return supportedTools;
//    }
//
//    public File[] getToolClassPathEntries(String toolName) {
//        if (WSStack.TOOL_WSGEN.equals(toolName) || WSStack.TOOL_WSIMPORT.equals(toolName)) {
//            if (isWsit()) {
//                File[] retValue = new File[WSIT_LIBS.length];
//                for (int i = 0; i < WSIT_LIBS.length; i++) {
//                    retValue[i] = new File(catalinaHome, WSIT_LIBS[i]);
//                }
//                return retValue; 
//            }                     
//        } else if (WSStack.TOOL_KEYSTORE.equals(toolName) && isKeystore()) {
//            return new File[]{new File(catalinaHome, KEYSTORE_LOCATION)};
//        } else if (WSStack.TOOL_TRUSTSTORE.equals(toolName) && isTruststore()) {
//            return new File[]{new File(catalinaHome, TRUSTSTORE_LOCATION)};
//        } else if (WSStack.TOOL_KEYSTORE_CLIENT.equals(toolName) && isKeystoreClient()) {
//            return new File[]{new File(catalinaHome, KEYSTORE_CLIENT_LOCATION)};
//        } else if (WSStack.TOOL_TRUSTSTORE_CLIENT.equals(toolName) && isTruststoreClient()) {
//            return new File[]{new File(catalinaHome, TRUSTSTORE_CLIENT_LOCATION)};
//        }
//        
//        return new File[]{};
//    }

    @Override
    public WSTool getWSTool(Tool toolId) {
        if (toolId == JaxWs.Tool.WSIMPORT && isWsit()) {
            return WSStackFactory.createWSTool(new JaxWsTool(JaxWs.Tool.WSIMPORT));
        } else if (toolId == JaxWs.Tool.WSGEN && isWsit()) {
            return WSStackFactory.createWSTool(new JaxWsTool(JaxWs.Tool.WSGEN));
        } else {
            return null;
        }
    }

    @Override
    public boolean isFeatureSupported(Feature feature) {
        if (feature == JaxWs.Feature.TESTER_PAGE) {
            return true;
        } else if (feature == JaxWs.Feature.WSIT) {
            return isWsit();
        } else {
            return false;
        }
    }
    
    private boolean isWsit() {
        return isWsit(true/*shared*/) || isWsit(false/*global*/);
    }

    private boolean isWsit(boolean shared) {
        String[] libList = shared ? SHARED_METRO_LIBS : GLOBAL_METRO_LIBS;
        boolean wsit = true;
        for (int i = 0; i < libList.length; i++) {
            if (!new File(catalinaHome, libList[i]).exists()) {
                wsit = false;
            }
        }
        return wsit;
    }

    private String[] getDetectedMetroLibs() {
        return isWsit(false) ? GLOBAL_METRO_LIBS : SHARED_METRO_LIBS;
    }

    
    private boolean isKeystore() {
        if (new File(catalinaHome, KEYSTORE_LOCATION).exists()) return true;
        else return false;
    }
    private boolean isKeystoreClient() {
        if (new File(catalinaHome, KEYSTORE_CLIENT_LOCATION).exists()) return true;
        else return false;
    }
    
    private boolean isTruststore() {
        if (new File(catalinaHome, TRUSTSTORE_LOCATION).exists()) return true;
        else return false;
    }
    private boolean isTruststoreClient() {
        if (new File(catalinaHome, TRUSTSTORE_CLIENT_LOCATION).exists()) return true;
        else return false;
    }
    
    private String resolveImplementationVersion() throws IOException {
        String [] metroLibs = getDetectedMetroLibs();
        File wsToolsJar = new File(catalinaHome, metroLibs[TOOLS_JAR_INDEX]);
        if (wsToolsJar.exists()) {
            JarFile jarFile = new JarFile(wsToolsJar);
            JarEntry entry = jarFile.getJarEntry("com/sun/tools/ws/version.properties"); //NOI18N
            if (entry != null) {
                InputStream is = jarFile.getInputStream(entry);
                BufferedReader r = new BufferedReader(new InputStreamReader(is));
                String ln = null;
                String ver = null;
                while ((ln=r.readLine()) != null) {
                    String line = ln.trim();
                    if (line.startsWith("major-version=")) { //NOI18N
                        ver = line.substring(14);
                    }
                }
                r.close();
                return ver;
            }           
        }
        return null;
    }
    
    private JaxWs.UriDescriptor getUriDescriptor() {
        return new JaxWs.UriDescriptor() {

            @Override
            public String getServiceUri(String applicationRoot, String serviceName, 
                    String portName, boolean isEjb) 
            {
                return (applicationRoot.length()>0 ? applicationRoot+"/" : 
                    "")+serviceName; //NOI18N
            }

            @Override
            public String getDescriptorUri(String applicationRoot, 
                    String serviceName, String portName, boolean isEjb) 
            {
                return getServiceUri(applicationRoot, serviceName, portName, 
                        isEjb)+"?wsdl"; //NOI18N
            }
            
            @Override
            public String getTesterPageUri(String host, String port, 
                    String applicationRoot, String serviceName, String portName, 
                        boolean isEjb) 
            {
                return "http://"+host+":"+port+"/"+getServiceUri(applicationRoot, //NOI18N
                        serviceName, portName, isEjb); 
            }
            
        };
    }
    
    private class JaxWsTool implements WSToolImplementation {
        JaxWs.Tool tool;
        JaxWsTool(JaxWs.Tool tool) {
            this.tool = tool;
        }

        @Override
        public String getName() {
            return tool.getName();
        }

        @Override
        public URL[] getLibraries() {
            String[] metroLibs = getDetectedMetroLibs();
            URL[] retValue = new URL[metroLibs.length];
            try {
                for (int i = 0; i < metroLibs.length; i++) {
                    retValue[i] = new File(catalinaHome, 
                            metroLibs[i]).toURI().toURL();
                }
                return retValue;
            } catch (MalformedURLException ex) {
                return new URL[0];
            }
        }
        
    }
    
}
