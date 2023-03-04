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
package org.netbeans.modules.websvc.rest.projects;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntBuildExtender;
import org.netbeans.api.project.ant.AntBuildExtender.Extension;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.filesystems.FileObject;
import org.openide.util.Mutex;
import org.openide.util.MutexException;

/**
 *
 * @author nam
 */
public class AntFilesHelper {
    /**
     * IMPORTANT: bump up version when you change the dependencies list
     */
    public static final int CURRENT_DEPENDECIES_VERSION = 5;
    
    public static final String REST_ANT_EXT_NAME_BASE = "rest";
    public static final String REST_ANT_EXT_NAME = getExtensionVersionString(CURRENT_DEPENDECIES_VERSION);

    public static final String REST_BUILD_XSL = "org/netbeans/modules/websvc/rest/resources/rest-build.xsl";
    public static final String REST_BUILD_XML_PATH = "nbproject/rest-build.xml";

    private AntProjectHelper projectHelper;
    private Project project;
    private AntBuildExtender extender;
    
    public AntFilesHelper(RestSupport restSupport) {
        this(restSupport.getProject(), restSupport.getAntProjectHelper());
    }
    
    public AntFilesHelper(Project project, AntProjectHelper helper) {
        projectHelper = helper;
        this.project = project;
        extender = project.getLookup().lookup(AntBuildExtender.class);
        if (extender == null) {
            throw new IllegalArgumentException("Given project does not allow extension");
        }
    }
    
    public void initRestBuildExtension() throws IOException {
        boolean restBuildScriptRefreshed = refreshRestBuildXml();
        boolean saveProjectXml = false;
        boolean changed = false;
        FileObject restBuildScript = project.getProjectDirectory().getFileObject(REST_BUILD_XML_PATH);
        if (restBuildScript != null) {
            Extension extension =  extender.getExtension(REST_ANT_EXT_NAME);
            if (extension == null) {
                extension = extender.addExtension(REST_ANT_EXT_NAME, restBuildScript);
                changed = true;
                saveProjectXml = true;
            }
        }

        // check for cleanup of last version
        if (cleanupLastExtensionVersions()) {
            saveProjectXml = true;
        }
        if (saveProjectXml) {
            ProjectManager.getDefault().saveProject(project);
        }

        if (changed && !restBuildScriptRefreshed) {
            // generate build script
            try {
                final GeneratedFilesHelper helper = new GeneratedFilesHelper(projectHelper);
                ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Boolean>() {
                    public Boolean run() throws IOException {
                        URL xslURL = this.getClass().getClassLoader().getResource(REST_BUILD_XSL);
                        helper.generateBuildScriptFromStylesheet(REST_BUILD_XML_PATH,  xslURL);
                        return true;
                    }
                });
            } catch (MutexException e) {
                throw (IOException)e.getException();
            }
        }
    }
    
    public boolean refreshRestBuildXml() throws IOException {
        URL xslURL = this.getClass().getClassLoader().getResource(REST_BUILD_XSL);
        GeneratedFilesHelper helper = new GeneratedFilesHelper(projectHelper);
        return helper.refreshBuildScript(REST_BUILD_XML_PATH, xslURL, true);
    }
            
    private static String getExtensionVersionString(int version) {
        return REST_ANT_EXT_NAME_BASE + "." + version;
    }
    
    public boolean cleanupLastExtensionVersions() {
        List<String> extensionNames = new ArrayList<String>();
        String lastVersion = REST_ANT_EXT_NAME_BASE;
        if (extender.getExtension(lastVersion) != null) {
            extensionNames.add(lastVersion);
        }
        
        for (int version = 0 ; version < CURRENT_DEPENDECIES_VERSION; version++) {
            lastVersion = getExtensionVersionString(version);
            if (extender.getExtension(lastVersion) != null) {
                extensionNames.add(lastVersion);
            }
        }

        for (String name : extensionNames) {
            extender.removeExtension(name);
        }
        return extensionNames.size() > 0;
    }
}
