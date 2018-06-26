/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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

