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
package org.netbeans.modules.web.beans;



import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.WeakHashMap;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.netbeans.modules.web.beans.api.model.ModelUnit;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;
import org.netbeans.modules.web.beans.api.model.WebBeansModelFactory;

import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;

/**
 * @author ads
 *
 */
public class MetaModelSupport {

    public MetaModelSupport( Project project ){
        myProject = project;
    }
    
    public MetadataModel<WebBeansModel> getMetaModel(){
        synchronized (MODELS) {
            MetadataModel<WebBeansModel> metadataModel = MODELS.get( myProject );
            if ( metadataModel != null ){
                return metadataModel;
            }
            ClassPath boot = getClassPath(  ClassPath.BOOT);
            ClassPath compile = getClassPath( ClassPath.COMPILE );
            ClassPath src = getClassPath( ClassPath.SOURCE);
            if ( boot == null || compile == null || src == null ){
                return null;
            }
            ModelUnit modelUnit = ModelUnit.create( boot, compile , src, myProject);
            metadataModel = WebBeansModelFactory.getMetaModel( modelUnit );
            MODELS.put( myProject, metadataModel );
            return metadataModel;
        }
    }
    
    public ClassPath getClassPath( String type ) {
        ClassPathProvider provider = getProject().getLookup().lookup( 
                ClassPathProvider.class);
        if ( provider == null ){
            return null;
        }
        Sources sources = getProject().getLookup().lookup(Sources.class);
        if ( sources == null ){
            return null;
        }
        SourceGroup[] sourceGroups = sources.getSourceGroups( 
                JavaProjectConstants.SOURCES_TYPE_JAVA );
        SourceGroup[] webGroup = sources.getSourceGroups(
                WebProjectConstants.TYPE_WEB_INF);
        ClassPath[] paths = new ClassPath[ sourceGroups.length+webGroup.length];
        int i=0;
        for (SourceGroup sourceGroup : sourceGroups) {
            FileObject rootFolder = sourceGroup.getRootFolder();
            paths[ i ] = provider.findClassPath( rootFolder, type);
            i++;
        }
        for (SourceGroup sourceGroup : webGroup) {
            FileObject rootFolder = sourceGroup.getRootFolder();
            paths[ i ] = provider.findClassPath( rootFolder, type);
            i++;
        }
        return ClassPathSupport.createProxyClassPath( paths );
    }
    
    private Project getProject(){
        return myProject;
    }
    
    private Project myProject; 
    
    private static WeakHashMap<Project, MetadataModel<WebBeansModel>>
        MODELS = new WeakHashMap<Project, MetadataModel<WebBeansModel>>();
}
