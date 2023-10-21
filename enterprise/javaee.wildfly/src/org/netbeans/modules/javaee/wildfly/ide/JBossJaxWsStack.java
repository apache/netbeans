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

package org.netbeans.modules.javaee.wildfly.ide;

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
import org.netbeans.modules.websvc.wsstack.spi.WSStackImplementation;
import org.netbeans.modules.websvc.wsstack.spi.WSStackFactory;
import org.netbeans.modules.websvc.wsstack.spi.WSToolImplementation;


/**
 *
 * @author mkuchtiak
 * @author ads
 */
public class JBossJaxWsStack implements WSStackImplementation<JaxWs> {
    private static final String JAXWS_TOOLS_JAR = "client/jaxws-tools.jar"; //NOI18N

    private File root;
    private String version;
    private JaxWs jaxWs;

    public JBossJaxWsStack(File root) {
        this.root = root;
        try {
            version = resolveImplementationVersion();
            if (version == null) {
                // Default Version
                version = "2.1.3"; // NOI18N
            }
        } catch (IOException ex) {
            // Default Version
            version = "2.1.3"; // NOI18N
        }
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

    @Override
    public WSTool getWSTool(Tool toolId) {
        if (toolId == JaxWs.Tool.WSIMPORT) {
            return WSStackFactory.createWSTool(new JaxWsTool(JaxWs.Tool.WSIMPORT));
        } else if (toolId == JaxWs.Tool.WSGEN) {
            return WSStackFactory.createWSTool(new JaxWsTool(JaxWs.Tool.WSGEN));
        } else {
            return null;
        }
    }

    private JaxWs.UriDescriptor getUriDescriptor() {
        return new JaxWs.UriDescriptor() {

            @Override
            public String getServiceUri(String applicationRoot, String serviceName,
                    String portName, boolean isEjb)
            {
                return applicationRoot+"/"+serviceName; //NOI18N
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
                return "";
            }

        };
    }

    @Override
    public boolean isFeatureSupported(Feature feature) {
        if (feature == JaxWs.Feature.JSR109) {
            return true;
        } else if (feature == JaxWs.Feature.WSIT && new File(root,
                "client/jbossws-metro-wsit-tools.jar").exists())  //NOI18N
        {
            return true;
        } else {
            return false;
        }
    }

    private String resolveImplementationVersion() throws IOException {
        // take webservices-tools.jar file
        File wsToolsJar = new File(root, JAXWS_TOOLS_JAR);

        if (wsToolsJar.exists()) {
            try (JarFile jarFile = new JarFile(wsToolsJar)) {
                JarEntry entry = jarFile.getJarEntry("com/sun/tools/ws/version.properties"); //NOI18N
                if (entry != null) {
                    String ver = null;
                    try (InputStream is = jarFile.getInputStream(entry);
                            BufferedReader r = new BufferedReader(new InputStreamReader(is))) {
                        String ln = null;
                        while ((ln=r.readLine()) != null) {
                            String line = ln.trim();
                            if (line.startsWith("major-version=")) { //NOI18N
                                ver = line.substring(14);
                                break;
                            }
                        }
                    }
                    return ver;
                }
            }
        }
        return null;
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

            File clientRoot = new File(root, "client"); // NOI18N
            try {
                File loggingJar = new File(clientRoot, "jboss-common-client.jar"); // NOI18N
                if (!loggingJar.exists()) {
                    loggingJar = new File(clientRoot, "jboss-logging-spi.jar");  // NOI18N
                }

                File jaxWsAPILib = new File(clientRoot, "jboss-jaxws.jar"); // NOI18N
                // JBoss without jbossws
                if (jaxWsAPILib.exists()) {
                    return new URL[] {
                        new File(clientRoot, "wstx.jar").toURI().toURL(),   // NOI18N
                        new File(clientRoot, "jaxws-tools.jar").toURI().toURL(),  // NOI18N
                        loggingJar.toURI().toURL(),
                        new File(clientRoot, "stax-api.jar").toURI().toURL(),    // NOI18N
                        jaxWsAPILib.toURI().toURL(),
                        new File(clientRoot, "jbossws-client.jar").toURI().toURL(),  // NOI18N
                        new File(clientRoot, "jboss-jaxws-ext.jar").toURI().toURL(),    // NOI18N
                        new File(clientRoot, "jboss-saaj.jar").toURI().toURL()    // NOI18N
                    };
                }
                jaxWsAPILib = new File(clientRoot, "jbossws-native-jaxws.jar"); // NOI18N
                // JBoss+jbossws-native
                if (jaxWsAPILib.exists()) {
                    return new URL[] {
                        new File(clientRoot, "wstx.jar").toURI().toURL(),   // NOI18N
                        new File(clientRoot, "jaxws-tools.jar").toURI().toURL(),  // NOI18N
                        loggingJar.toURI().toURL(),
                        new File(clientRoot, "stax-api.jar").toURI().toURL(),    // NOI18N
                        jaxWsAPILib.toURI().toURL(),    // NOI18N
                        new File(clientRoot, "jbossws-native-client.jar").toURI().toURL(),  // NOI18N
                        new File(clientRoot, "jbossws-native-jaxws-ext.jar").toURI().toURL(),    // NOI18N
                        new File(clientRoot, "jbossws-native-saaj.jar").toURI().toURL()    // NOI18N
                    };
                }
                jaxWsAPILib = new File(clientRoot, "jaxws-api.jar"); // NOI18N
                // JBoss+jbossws-metro
                if (jaxWsAPILib.exists()) {
                    return new URL[] {
                        new File(clientRoot, "wstx.jar").toURI().toURL(),   // NOI18N
                        new File(clientRoot, "jaxws-tools.jar").toURI().toURL(),  // NOI18N
                        loggingJar.toURI().toURL(),
                        new File(clientRoot, "stax-api.jar").toURI().toURL(),    // NOI18N
                        jaxWsAPILib.toURI().toURL(),
                        new File(clientRoot, "jbossws-metro-client.jar").toURI().toURL(),  // NOI18N
                        new File(clientRoot, "saaj-api.jar").toURI().toURL()    // NOI18N
                    };
                }
            } catch (MalformedURLException ex) {
                return new URL[0];
            }
            return new URL[0];
        }

    }

}
