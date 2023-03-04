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

package org.netbeans.installer.utils.system.launchers.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.FileProxy;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.exceptions.DownloadException;
import org.netbeans.installer.utils.helper.JavaCompatibleProperties;
import org.netbeans.installer.utils.helper.Version;
import org.netbeans.installer.utils.system.launchers.LauncherProperties;
import org.netbeans.installer.utils.system.launchers.LauncherResource;

/**
 *
 * @author Dmitry Lipin
 */
public class CommandLauncher extends ShLauncher {
    private static final String COMMAND_EXT = ".command"; //NOI18N
    
    public static final String JAVA_APPLICATION_ICON_PROPERTY =
            "nbi.java.application.icon"; //NOI18N
    
    public static final String JAVA_APPLICATION_NAME_LAUNCHER_PROPERTY =
            "nlu.java.application.name.macosx"; //NOI18N

    public static final String JAVA_APPLICATION_ICON_DEFAULT_URI =
            FileProxy.RESOURCE_SCHEME_PREFIX + 
            "org/netbeans/installer/utils/system/launchers/impl/dockicon.icns";
    
    public static final String NOTSET_DOCK_ICON_PROPERTY = 
            "nbi.not.set.dock.icon";
    public static final String NOTSET_DOCK_NAME_PROPERTY = 
            "nbi.not.set.dock.name";
            
    public static final String XDOCK_ICON_PROPERTY_NAME = 
            "-Xdock:icon";//NOI18N
    public static final String XDOCK_NAME_PROPERTY_NAME = 
            "-Xdock:name";//NOI18N
            
    
    private static final String [] JAVA_MACOSX_LOCATION = {};
    // the 1.5.0_02 is the first fcs release of J2SE5 for MacOSX according to
    // http://developer.apple.com/releasenotes/Java/Java50RN/index.html
    public static final String MIN_JAVA_VERSION_MACOSX = "1.5.0_02";
    
    public CommandLauncher(LauncherProperties props) {
        super(props);
    }
    @Override
    protected String [] getCommonSystemJavaLocations() {
        return JAVA_MACOSX_LOCATION;
    }
    @Override
    public String getExtension() {
        return COMMAND_EXT;
    }
    
    @Override
    public List <JavaCompatibleProperties> getDefaultCompatibleJava(Version version) {        
        if (version.equals(Version.getVersion("1.5"))) {
            List <JavaCompatibleProperties> list = new ArrayList <JavaCompatibleProperties>();
            list.add(new JavaCompatibleProperties(
                MIN_JAVA_VERSION_MACOSX, null, null, null, null));
            return list;
        } else {
            return super.getDefaultCompatibleJava(version);            
        }        
        
    }    
    @Override
    public void initialize() throws IOException {
        super.initialize();
        boolean setDockIcon = true;
        boolean setDockName = true;
        for(String s : jvmArguments) {
            if(s.contains(XDOCK_NAME_PROPERTY_NAME)) {
                setDockName = false;                
            }
            if(s.contains(XDOCK_ICON_PROPERTY_NAME)) {
                setDockIcon = false;                
            }
        }
        
        if(setDockIcon && !Boolean.getBoolean(NOTSET_DOCK_ICON_PROPERTY)) {
            File iconFile = null;            
            String uri = System.getProperty(JAVA_APPLICATION_ICON_PROPERTY);            
            if(uri == null) {
                uri = JAVA_APPLICATION_ICON_DEFAULT_URI;
            }
            
            try {
                iconFile = FileProxy.getInstance().getFile(uri,true);
                LauncherResource iconResource = new LauncherResource (iconFile);
                jvmArguments.add(XDOCK_ICON_PROPERTY_NAME + StringUtils.EQUAL + iconResource.getAbsolutePath());
                otherResources.add(iconResource);
            } catch (DownloadException e) {
                ErrorManager.notify(ResourceUtils.getString(
                        CommandLauncher.class, ERROR_CANNOT_GET_ICON_KEY, uri), e);
            }            
        }
        if(setDockName && !Boolean.getBoolean(NOTSET_DOCK_NAME_PROPERTY)) {
            jvmArguments.add(XDOCK_NAME_PROPERTY_NAME + StringUtils.EQUAL + 
                    "$P{" + JAVA_APPLICATION_NAME_LAUNCHER_PROPERTY + "}");
        }        
    }
    
    private static final String ERROR_CANNOT_GET_ICON_KEY = 
            "CdL.error.cannot.get.icon";//NOI18N
}
