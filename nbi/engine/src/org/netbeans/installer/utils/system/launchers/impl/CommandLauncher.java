/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
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
