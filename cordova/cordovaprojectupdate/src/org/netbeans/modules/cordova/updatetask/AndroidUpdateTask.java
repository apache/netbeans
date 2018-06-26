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
import java.io.IOException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.util.FileUtils;

/**
 *
 * @author Jan Becicka
 */
public class AndroidUpdateTask extends CordovaTask {

    @Override
    public void execute() throws BuildException {
        File manifestFile = new File(
                getProject().getBaseDir().getAbsolutePath() + 
                "/" + getProperty("cordova.platforms") + 
                "/android/AndroidManifest.xml"); // NOI18N
        File configFile = getConfigFile();
        File androidConfigFile = new File(
                getProject().getBaseDir().getAbsolutePath() + 
                "/" + getProperty("cordova.platforms") + 
                "/android/res/xml/config.xml"); // NOI18N
        try {
            AndroidManifest androidManifest = new AndroidManifest(manifestFile);
            updateAndroidManifest(androidManifest);
            androidManifest.save();
            
            DeviceConfig androidConfig = new DeviceConfig(androidConfigFile);
            SourceConfig config = new SourceConfig(configFile);
            
            updateAndroidConfig(config, androidConfig);
            androidConfig.save();
            updateResources(config);
            
        } catch (IOException ex) {
            throw new BuildException(ex);
        }
    }
    
    private void updateAndroidManifest(AndroidManifest manifest) {
        manifest.setName(getProperty("android.project.activity"));
        manifest.setPackage(getProperty("android.project.package"));
    }

    private void updateAndroidConfig(SourceConfig config, DeviceConfig androidConfig) {
        androidConfig.setAccess(config.getAccess());
    }
    
    private void updateResources(SourceConfig config) throws IOException {
        String icon = config.getIcon("android", 36, 36); // NOI18N
        copy(icon, "drawable-ldpi/icon"); // NOI18N

        icon = config.getIcon("android", 48, 48); // NOI18N
        copy(icon, "drawable-mdpi/icon"); // NOI18N
        
        icon = config.getIcon("android", 72, 72); // NOI18N
        copy(icon, "drawable-hdpi/icon"); // NOI18N

        icon = config.getIcon("android", 96, 96); // NOI18N
        copy(icon, "drawable-xhdpi/icon"); // NOI18N

        copy(icon, "drawable/icon"); // NOI18N
        
        String splash = config.getSplash("android", 320, 200); // NOI18N
        copy(splash, "drawable-ldpi/splash_landscape"); // NOI18N

        splash = config.getSplash("android", 200, 320); // NOI18N
        copy(splash, "drawable-ldpi/splash_portrait"); // NOI18N
        
        splash = config.getSplash("android", 480, 320); // NOI18N
        copy(splash, "drawable-mdpi/splash_landscape"); // NOI18N

        splash = config.getSplash("android", 320, 480); // NOI18N
        copy(splash, "drawable-mdpi/splash_portrait"); // NOI18N
        
        
        splash = config.getSplash("android", 800, 480); // NOI18N
        copy(splash, "drawable-hdpi/splash_landscape"); // NOI18N

        splash = config.getSplash("android", 480, 800); // NOI18N
        copy(splash, "drawable-hdpi/splash_portrait"); // NOI18N
        
        splash = config.getSplash("android", 1280, 720); // NOI18N
        copy(splash, "drawable-xhdpi/splash_landscape"); // NOI18N

        splash = config.getSplash("android", 720, 1280); // NOI18N
        copy(splash, "drawable-xhdpi/splash_portrait"); // NOI18N

        splash = config.getSplash("android", 1280, 720); // NOI18N
        copy(splash, "drawable/splash_landscape"); // NOI18N

        splash = config.getSplash("android", 720, 1280); // NOI18N
        copy(splash, "drawable/splash_portrait"); // NOI18N
        
    }
    
    private void copy(String source, String dest) throws IOException {
        if (source==null) {
            return;
        }
        String ext = source.substring(source.indexOf(".")); // NOI18N
        final String prjPath = getProject().getBaseDir().getPath();
        FileUtils.getFileUtils().copyFile(
                prjPath + "/" + getProperty("site.root") + "/" + source, 
                prjPath + "/" + getProject().getProperty("cordova.platforms") + "/android/res/" + dest + ext);
    }
            
}
