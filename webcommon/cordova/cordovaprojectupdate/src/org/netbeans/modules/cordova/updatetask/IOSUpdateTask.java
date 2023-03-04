/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.cordova.updatetask;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.util.FileUtils;

/**
 *
 * @author Jan Becicka
 */
public class IOSUpdateTask extends CordovaTask {

    @Override
    public void execute() throws BuildException {
        try {
            File root = new File(
                    getProject().getBaseDir().getAbsolutePath() 
                    + "/" + getProperty("cordova.platforms") + "/ios");
            File configFile = getConfigFile();

            String[] list = root.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    if (name.endsWith("xcodeproj")) { // NOI18N
                        return true;
                    }
                    return false;
                }
            });

            String name = list[0].substring(0, list[0].indexOf(".")); // NOI18N
            File iosConfigFile = new File(
                    getProject().getBaseDir().getAbsolutePath() + 
                    "/" + getProperty("cordova.platforms") + "/ios/" 
                    + name + "/" + "config.xml"); // NOI18N
            getProject().setProperty("xcode.project.name", name); // NOI18N
            
            File infoPlist = new File(root.getPath() + "/" + name + "/" + name + "-Info.plist"); // NOI18N
            InfoPlist plist = new InfoPlist(infoPlist);
            final String bundleID = getProperty("android.project.package") + "." +getProperty("android.project.activity");
            plist.setBundleIdentifier(bundleID);
            plist.save();
            
            DeviceConfig iosConfig = new DeviceConfig(iosConfigFile);
            SourceConfig config = new SourceConfig(configFile);

            updateIOSConfig(config, iosConfig);
            iosConfig.save();
            updateResources(config);
        } catch (IOException ex) {
            throw new BuildException(ex);
        } 
    }
    
    private void updateIOSConfig(SourceConfig config, DeviceConfig iosConfig) {
        iosConfig.setAccess(config.getAccess());
        remap(config, iosConfig, "webviewbounce", "UIWebViewBounce"); // NOI18N
        remap(config, iosConfig, "DisallowOverscroll", "DisallowOverscroll"); // NOI18N
        remap(config, iosConfig, "auto-hide-splash-screen", "AutoHideSplashScreen"); // NOI18N
    }
    
    private void remap(SourceConfig config, DeviceConfig iosConfig, String orig, String newOne) {
        String pref = config.getPreference(orig);
        if (pref != null) {
            iosConfig.setPreference(newOne, pref);
        }
    }
    
    private void updateResources(SourceConfig config) throws IOException {
        String icon = config.getIcon("ios", 57, 57); // NOI18N
        copy(icon, "icons/icon"); // NOI18N

        icon = config.getIcon("ios", 114, 114); // NOI18N
        copy(icon, "icons/icon@2x"); // NOI18N
        
        icon = config.getIcon("ios", 72, 72); // NOI18N
        copy(icon, "icons/icon-72"); // NOI18N

        icon = config.getIcon("ios", 144, 144); // NOI18N
        copy(icon, "icons/icon-72@2x"); // NOI18N

        String splash = config.getSplash("ios", 320, 480); // NOI18N
        copy(splash, "splash/Default~iphone"); // NOI18N

        splash = config.getSplash("ios", 640, 960); // NOI18N
        copy(splash, "splash/Default@2x~iphone"); // NOI18N
        
        splash = config.getSplash("ios", 768, 1024); // NOI18N
        copy(splash, "splash/Default-Portrait~ipad"); // NOI18N

        splash = config.getSplash("ios", 1536, 2048); // NOI18N
        copy(splash, "splash/Default-Portrait@2x~ipad"); // NOI18N
        
        splash = config.getSplash("ios", 1024, 768); // NOI18N
        copy(splash, "splash/Default-Landscape~ipad"); // NOI18N

        splash = config.getSplash("ios", 2048, 1536); // NOI18N
        copy(splash, "splash/Default-Landscape@2x~ipad"); // NOI18N
        
        splash = config.getSplash("ios", 640, 1136); // NOI18N
        copy(splash, "splash/Default-568h@2x~iphone.png"); // NOI18N

    }
    
    private void copy(String source, String dest) throws IOException {
        if (source==null) {
            return;
        }
        String name = getProject().getProperty("xcode.project.name");
        final int i = source.indexOf("."); // NOI18N
        String ext = i<0?"":source.substring(i);
        final String prjPath = getProject().getBaseDir().getPath();
        FileUtils.getFileUtils().copyFile(
                prjPath + "/" + getProperty("site.root") + "/" + source, 
                prjPath + "/" + getProject().getProperty("cordova.platforms") + "/ios/" + name + "/Resources/" + dest + ext);
    }    
    
}

