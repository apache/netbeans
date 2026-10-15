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
import java.util.regex.Pattern;

import org.netbeans.modules.javaee.specs.support.api.JaxRpc;
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
public class Hk2JaxRpcStack implements WSStackImplementation<JaxRpc> {
    private static final String[] METRO_LIBRARIES =
            new String[] {"webservices(|-osgi).jar"}; //NOI18N
    private static final String GFV3_MODULES_DIR_NAME = "modules"; // NOI18N
    
    private String gfRootStr;
    private JaxRpc jaxRpc;
    
    public Hk2JaxRpcStack(String gfRootStr) {
        this.gfRootStr = gfRootStr;
        jaxRpc = new JaxRpc();
    }

    @Override
    public JaxRpc get() {
        return jaxRpc;
    }
    
    @Override
    public WSStackVersion getVersion() {
        return WSStackVersion.valueOf(1, 1, 3, 0);
    }

    @Override
    public WSTool getWSTool(Tool toolId) {
        if (toolId == JaxRpc.Tool.WCOMPILE && isMetroInstalled()) {
            return WSStackFactory.createWSTool(new JaxRpcTool(JaxRpc.Tool.WCOMPILE));
        } else {
            return null;
        }
    }
    
    @Override
    public boolean isFeatureSupported(Feature feature) {
        if (feature == JaxRpc.Feature.JSR109 && isMetroInstalled()) {
            return true;
        }
        return false;   
    }
    
    protected class JaxRpcTool implements WSToolImplementation {
        JaxRpc.Tool tool;
        JaxRpcTool(JaxRpc.Tool tool) {
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
                    File f = getJarName(gfRootStr, entry);
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
        File f = getJarName(gfRootStr, METRO_LIBRARIES[0]);
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
    
    protected File getJarName(String glassfishInstallRoot, String jarNamePattern) {

        File modulesDir = new File(glassfishInstallRoot + File.separatorChar + 
                GFV3_MODULES_DIR_NAME);
        int subindex = jarNamePattern.lastIndexOf("/");
        if(subindex != -1) {
            String subdir = jarNamePattern.substring(0, subindex);
            jarNamePattern = jarNamePattern.substring(subindex+1);
            modulesDir = new File(modulesDir, subdir);
        }
        File candidates[] = modulesDir.listFiles(new VersionFilter(jarNamePattern));

        if (candidates != null && candidates.length > 0) {
            return candidates[0]; // the first one
        }
        return null;
    }

}
