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
