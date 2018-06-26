/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.javaee.specs.support.bridge;


import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.api.j2ee.core.Profile;
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
                ProjectClassPathModifier.removeLibraries(libraries.toArray( 
                        new Library[ libraries.size()]), sourceRoot, type);
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
