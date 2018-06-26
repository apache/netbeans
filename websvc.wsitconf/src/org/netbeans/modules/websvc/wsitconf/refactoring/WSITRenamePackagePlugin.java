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
