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
    private Hk2JavaEEPlatformImpl platform;
    
    public Hk2JaxWsStack(String gfRootStr, Hk2JavaEEPlatformImpl platform ) {
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
        if ( supportedProfiles.contains( Profile.JAVA_EE_6_FULL) || 
                supportedProfiles.contains(Profile.JAVA_EE_6_WEB))
        {
            // gfv3ee6 GF id 
            if (isMetroInstalled()) {
                return WSStackVersion.valueOf(2, 2, 0, 0);
            }
            return WSStackVersion.valueOf(2, 1, 4, 1);
        }
        else {
            // gfv3 GF id
            if (isMetroInstalled()) {
                return WSStackVersion.valueOf(2, 1, 4, 1);
            }
            return WSStackVersion.valueOf(2, 1, 3, 0);
        }
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
            return cPath.toArray(new URL[cPath.size()]);
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
