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
package org.netbeans.modules.websvc.wsitconf.refactoring;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.core.api.support.java.JavaIdentifiers;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.modules.websvc.wsitconf.refactoring.WSITRefactoringPlugin.AbstractRefactoringElement;
import org.netbeans.modules.websvc.wsitconf.refactoring.WSITRefactoringPlugin.AbstractRenameConfigElement;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.WSITModelSupport;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
class WSITRenamePackagePlugin implements RefactoringPlugin {
    
    private static final Logger LOG = Logger.getLogger( WSITRenamePackagePlugin.class.getName()); 

    WSITRenamePackagePlugin( RenameRefactoring refactoring ) {
        this.refactoring = refactoring;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.refactoring.spi.RefactoringPlugin#cancelRequest()
     */
    @Override
    public void cancelRequest() {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.refactoring.spi.RefactoringPlugin#checkParameters()
     */
    @Override
    public Problem checkParameters() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.refactoring.spi.RefactoringPlugin#fastCheckParameters()
     */
    @Override
    public Problem fastCheckParameters() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.refactoring.spi.RefactoringPlugin#preCheck()
     */
    @Override
    public Problem preCheck() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.refactoring.spi.RefactoringPlugin#prepare(org.netbeans.modules.refactoring.spi.RefactoringElementsBag)
     */
    @Override
    public Problem prepare( RefactoringElementsBag refactoringElements ) {
        FileObject pkg = refactoring.getRefactoringSource().lookup(
                NonRecursiveFolder.class).getFolder();
        String oldPackageName = JavaIdentifiers.getQualifiedName(pkg);
        
        JAXWSSupport support = JAXWSSupport.getJAXWSSupport( pkg );
        if ( support == null ){
            LOG.log( Level.FINE , "No JAX-WS support for project found");   // NOI18N
            return null;
        }
        List</*Service*/?> services = support.getServices();
        for (Object object : services) {
            Service service = (Service) object;
            String implementationClass = service.getImplementationClass();
            if ( implementationClass!= null 
                    && implementationClass.startsWith(oldPackageName))
            {
                doPrepare( pkg, implementationClass , refactoringElements);
            }
        }
        return null;
    }

    private void doPrepare( FileObject pkg , String classFqn , 
            RefactoringElementsBag refactoringElements) 
    {
        Project project = FileOwnerQuery.getOwner(pkg);
        WSDLModel model = null;
        if (project == null) {
            return;
        }
        FileObject file = getJavaFile( classFqn , project);
        if ( file == null ){
            return;
        }
        try {
            model = WSITModelSupport.getModelForServiceFromJava(file, 
                    project, false, null);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        if ( model == null ){
            return;
        }
        refactoringElements.addFileChange(refactoring, new PackageRenameElement(
                JavaIdentifiers.getQualifiedName(pkg) , refactoring.getNewName(),
                model )); 
    }

    private FileObject getJavaFile( String fqn, Project project ) {
        SourceGroup[] sourceGroups = ProjectUtils.getSources(project)
                .getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (sourceGroups.length > 0) {
            ClassPath classPath = null;
            for (int i = 0; i < sourceGroups.length; i++) {
                classPath = ClassPath.getClassPath(
                        sourceGroups[i].getRootFolder(), ClassPath.SOURCE);
                if (classPath != null) {
                    FileObject javaClass = classPath.findResource(fqn.replace(
                            '.', '/') + ".java"); // NOI18N
                    if (javaClass != null) {
                        return javaClass;
                    }
                }
            }
        }
        return null;
    }
    
    private static class PackageRenameElement extends AbstractRenameConfigElement{

        PackageRenameElement( String oldPackageName, String newPackageName,
                WSDLModel model )
        {
            super(model);
            String oldConfName = getParentFile().getName();
            setOldConfigName( oldConfName );
            setNewConfigName(oldConfName.replace( oldPackageName, newPackageName));
        }

    }

    private RenameRefactoring refactoring;

}
