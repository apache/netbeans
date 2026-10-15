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
package org.netbeans.modules.javaee.specs.support.bridge;


import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.javaee.specs.support.api.JaxRsStackSupport;
import org.netbeans.modules.javaee.specs.support.spi.JaxRsStackSupportImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;


/**
 * @author ads
 *
 */
public class IdeJaxRsSupportImpl implements JaxRsStackSupportImplementation {
    
    private static final String RESTAPI_LIBRARY = "restapi";        //NOI18N
    private static final String SWDP_LIBRARY = "restlib";           //NOI18N
    

    /* (non-Javadoc)
     * @see org.netbeans.modules.javaee.specs.support.spi.JaxRsStackSupportImplementation#addJsr311Api(org.netbeans.api.project.Project)
     */
    @Override
    public boolean addJsr311Api( Project project ) {
        Library restapiLibrary = LibraryManager.getDefault().getLibrary(
                RESTAPI_LIBRARY);
        if (restapiLibrary == null) {
            return false;
        }
        return addLibrary( project , restapiLibrary);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.javaee.specs.support.spi.JaxRsStackSupportImplementation#extendsJerseyProjectClasspath(org.netbeans.api.project.Project)
     */
    @Override
    public boolean extendsJerseyProjectClasspath( Project project ) {
        Library swdpLibrary = LibraryManager.getDefault().getLibrary(SWDP_LIBRARY);
        if (swdpLibrary == null) {
            return false;
        }
        JaxRsStackSupport support = JaxRsStackSupport.getInstance(project);
        if ( support != null ){
            support.configureCustomJersey(project);
        }
        return addLibrary( project , swdpLibrary);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.javaee.specs.support.spi.JaxRsStackSupportImplementation#removeJaxRsLibraries(org.netbeans.api.project.Project)
     */
    @Override
    public void removeJaxRsLibraries( Project project ) {
        List<Library> libraries = new ArrayList<Library>(2);
        Library swdpLibrary = LibraryManager.getDefault().getLibrary(SWDP_LIBRARY);
        if (swdpLibrary != null) {
            libraries.add( swdpLibrary );
        }

        Library restapiLibrary = LibraryManager.getDefault().getLibrary(RESTAPI_LIBRARY);
        if (restapiLibrary != null) {
            libraries.add( restapiLibrary );
        }
        SourceGroup[] sgs = ProjectUtils.getSources(project).getSourceGroups(
                JavaProjectConstants.SOURCES_TYPE_JAVA);
        FileObject sourceRoot = sgs[0].getRootFolder();
        String[] classPathTypes = new String[]{ ClassPath.COMPILE , ClassPath.EXECUTE };
        for (String type : classPathTypes) {
            try {
                ProjectClassPathModifier.removeLibraries(libraries.toArray(new Library[0]), sourceRoot, type);
            } 
            catch(UnsupportedOperationException ex) {
                Logger.getLogger( IdeJaxRsSupportImpl.class.getName() ).log( 
                        Level.INFO, null , ex );
            }
            catch( IOException e ){
                Logger.getLogger( IdeJaxRsSupportImpl.class.getName() ).log( 
                        Level.INFO, null , e );
            }
        }        
    }
    
    @Override
    public void configureCustomJersey(Project project) {
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.javaee.specs.support.spi.JaxRsStackSupportImplementation#isBundled(java.lang.String)
     */
    @Override
    public boolean isBundled( String classFqn ) {
        Library restapiLibrary = LibraryManager.getDefault().getLibrary(RESTAPI_LIBRARY);
        List<URL> urls = restapiLibrary.getContent("classpath");            // NOI18N
        for( URL url : urls ){
            FileObject root = URLMapper.findFileObject(url);
            if ( FileUtil.isArchiveFile(root)){
                root = FileUtil.getArchiveRoot(root);
            }
            String classFileObject = classFqn.replace('.', '/')+".class";   // NOI18N
            if ( root.getFileObject(classFileObject) != null ){
                return true;
            }
        }
        return false;
    }
    
    private boolean addLibrary( Project project , Library lib)  {
        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).
            getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (sourceGroups == null || sourceGroups.length < 1) {
            return false;
        }
        FileObject sourceRoot = sourceGroups[0].getRootFolder();
        try {
            ProjectClassPathModifier.addLibraries(new Library[] { lib },
                    sourceRoot, ClassPath.COMPILE);
        }
        catch (UnsupportedOperationException ex) {
            return false;
        }
        catch (IOException ex) {
            return false;
        }
        return true;
    }
    
}
