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

package org.netbeans.modules.glassfish.javaee;

import java.io.File;
import java.io.FileFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformImpl;
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
public class Hk2JaxWsStack implements WSStackImplementation<JaxWs> {
    private static final String[] METRO_LIBRARIES =
            new String[] {"webservices(|-osgi).jar", //NOI18N
                          "webservices-api(|-osgi).jar", //NOI18N
                          "jaxb(|-osgi).jar", //NOI18N
                          "jaxb-api(|-osgi).jar", //NOI18N
                          "javax.ejb.jar", //NOI18N
                          "javax.activation.jar"}; //NOI18N
    private static final String GFV3_MODULES_DIR_NAME = "modules"; // NOI18N

    private String gfRootStr;
    private JaxWs jaxWs;
    private J2eePlatformImpl platform;

    public Hk2JaxWsStack(String gfRootStr, J2eePlatformImpl platform ) {
        this.gfRootStr = gfRootStr;
        jaxWs = new JaxWs(getUriDescriptor());
        this.platform = platform;
    }


    @Override
    public JaxWs get() {
        return jaxWs;
    }

    @Override
    public WSStackVersion getVersion() {
        Set<Profile> supportedProfiles = platform.getSupportedProfiles();
        
        for (Profile profile : supportedProfiles) {
            if (profile.isAtLeast(Profile.JAVA_EE_6_WEB)) {
                // gfv3ee6 GF id
                if (isMetroInstalled()) {
                    return WSStackVersion.valueOf(2, 2, 0, 0);
                }
                return WSStackVersion.valueOf(2, 1, 4, 1);
            }
        }
        // gfv3 GF id
        if (isMetroInstalled()) {
            return WSStackVersion.valueOf(2, 1, 4, 1);
        }
        return WSStackVersion.valueOf(2, 1, 3, 0);
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

    @Override
    public boolean isFeatureSupported(Feature feature) {
        if (feature == JaxWs.Feature.WSIT && isMetroInstalled()) {
            return true;
        }
        if (feature == JaxWs.Feature.JSR109 && isMetroInstalled()) {
            return true;
        }
        if (feature == JaxWs.Feature.TESTER_PAGE) return true;
        return false;
    }

    private JaxWs.UriDescriptor getUriDescriptor() {
        return new JaxWs.UriDescriptor() {

            @Override
            public String getServiceUri(String applicationRoot, String serviceName,
                    String portName, boolean isEjb)
            {
                if (isEjb) {
                    return serviceName+"/"+portName; //NOI18N
                } else {
                    if ( applicationRoot == null || applicationRoot.length() ==0 ){
                        return serviceName;
                    }
                    else {
                        StringBuilder builder = new StringBuilder( applicationRoot);
                        builder.append('/');
                        builder.append(serviceName);
                        return builder.toString();
                    }
                }
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
                return "http://"+host+":"+port+"/"+getServiceUri(applicationRoot,
                        serviceName, portName, isEjb)+"?Tester"; //NOI18N
            }

        };
    }

    protected class JaxWsTool implements WSToolImplementation {
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
            List<URL> cPath = new ArrayList<URL>();
            if (isMetroInstalled()) {
                for (String entry : METRO_LIBRARIES) {
                    File f = getWsJarName(gfRootStr, entry);
                    if ((f != null) && (f.exists())) {
                        try {
                            cPath.add(f.toURI().toURL());
                        } catch (MalformedURLException ex) {

                        }
                    }
                }
            }
            return cPath.toArray(URL[]::new);
        }

    }

    protected boolean isMetroInstalled() {
        File f = getWsJarName(gfRootStr, METRO_LIBRARIES[0]);
        return f!=null && f.exists();
    }

    private static class VersionFilter implements FileFilter {

        private final Pattern pattern;

        public VersionFilter(String namePattern) {
            pattern = Pattern.compile(namePattern);
        }

        @Override
        public boolean accept(File file) {
            return pattern.matcher(file.getName()).matches();
        }

    }

    public static File getWsJarName(String glassfishInstallRoot,
            String jarNamePattern)
    {
        File modulesDir = new File(glassfishInstallRoot + File.separatorChar +
                GFV3_MODULES_DIR_NAME);
        int subindex = jarNamePattern.lastIndexOf("/");
        if(subindex != -1) {
            String subdir = jarNamePattern.substring(0, subindex);
            jarNamePattern = jarNamePattern.substring(subindex+1);
            modulesDir = new File(modulesDir, subdir);
        }
        File candidates[] = modulesDir.listFiles(
                new VersionFilter(jarNamePattern));

        if(candidates != null && candidates.length > 0) {
            return candidates[0]; // the first one
        } else {
            File endorsed = new File(modulesDir,"endorsed"); //NOI18N
            if (endorsed!= null && endorsed.isDirectory()) {
                File candidates1[] = endorsed.listFiles(
                        new VersionFilter(jarNamePattern));
                if (candidates1 != null && candidates1.length > 0) {
                    return candidates1[0]; // the first one
                }
            }
        }
        return null;
    }

}
