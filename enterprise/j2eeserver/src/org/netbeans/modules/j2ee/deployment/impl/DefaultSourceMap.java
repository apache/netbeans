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

package org.netbeans.modules.j2ee.deployment.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import org.netbeans.modules.j2ee.deployment.common.api.SourceFileMap;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeApplicationProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author nn136682
 */
public class DefaultSourceMap extends SourceFileMap {

    /**
     * Straight file mapping service.
     * Map a distribution path to a file using distribution path as relative path to a mapping root.
     */
    private J2eeModuleProvider provider;
    private HashSet rootFiles = new HashSet();
    
    /** Creates a new instance of DefaultFileMapping */
    public DefaultSourceMap(J2eeModuleProvider provider) {
        this.provider = provider;
        FileObject[] roots = provider.getSourceRoots();
        for (int i=0; i<roots.length; i++) {
            if (roots[i] != null) {
                rootFiles.add(FileUtil.toFile(roots[i]));
            }
        }
    }
    
    public String getContextName() {
        return provider.getDeploymentName();
    }

    public FileObject[] getSourceRoots() {
        return provider.getSourceRoots();
    }
    
    public File getEnterpriseResourceDir() {
        return provider.getJ2eeModule().getResourceDirectory();
    }
    
    public File[] getEnterpriseResourceDirs() {
        ArrayList result = new ArrayList();
        result.add(provider.getJ2eeModule().getResourceDirectory());
        if (provider instanceof J2eeApplicationProvider) {
            J2eeApplicationProvider jap = (J2eeApplicationProvider) provider;
            J2eeModuleProvider[] children = jap.getChildModuleProviders();
            for (int i=0; i<children.length; i++) {
                result.add(children[i].getJ2eeModule().getResourceDirectory());
            }
        }
        return (File[]) result.toArray(new File[0]);
    }
   
    public boolean add(String distributionPath, FileObject sourceFile) {
        return false;
    }
    
    public FileObject remove(String distributionPath) {
        return null;
    }
    
    public FileObject[] findSourceFile(String distributionPath) {
        ArrayList ret = new ArrayList();
        FileObject[] roots = getSourceRoots();
        String path = distributionPath.startsWith("/") ? distributionPath.substring(1) : distributionPath; //NOI18N
        for (int i=0; i<roots.length; i++) {
            FileObject fo = roots[i].getFileObject(path);
            if (fo != null)
                ret.add(fo);
        }
        return (FileObject[]) ret.toArray(new FileObject[0]);
    }
    
    public File getDistributionPath(FileObject sourceFile) {
        for (Iterator i=rootFiles.iterator(); i.hasNext();) {
            File rootFile = (File) i.next();
            FileObject root = FileUtil.toFileObject(rootFile);
            String relative = FileUtil.getRelativePath(root, sourceFile);
            if (relative != null && ! relative.trim().equals("")) { //NOI18N
                return new File(relative);
            }
        }
        return null;
    }
}


