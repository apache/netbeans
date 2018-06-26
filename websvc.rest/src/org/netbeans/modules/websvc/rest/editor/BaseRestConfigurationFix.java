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
package org.netbeans.modules.websvc.rest.editor;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;

import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.websvc.rest.model.api.RestServiceDescription;
import org.netbeans.modules.websvc.rest.model.api.RestServices;
import org.netbeans.modules.websvc.rest.model.api.RestServicesMetadata;
import org.netbeans.modules.websvc.rest.model.api.RestServicesModel;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;


/**
 * @author ads
 *
 */
abstract class BaseRestConfigurationFix implements Fix {
    
    BaseRestConfigurationFix(Project project, FileObject fileObject, 
            RestConfigurationEditorAwareTaskFactory factory, ClasspathInfo cpInfo)
    {
        this.project = project;
        this.fileObject = fileObject;
        this.factory = factory;
        this.cpInfo = cpInfo;
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.editor.hints.Fix#implement()
     */
    @Override
    public ChangeInfo implement() throws Exception {
        RestServicesModel servicesModel = getSupport().getRestServicesModel();
        final Set<String> fqns = servicesModel
                .runReadAction(
                        new MetadataModelAction<RestServicesMetadata, Set<String>>()
                {

                    @Override
                    public Set<String> run(
                            RestServicesMetadata metadata )
                            throws Exception
                    {
                        Set<String> restFqns = new HashSet<String>();
                        RestServices services = metadata.getRoot();
                        RestServiceDescription[] descriptions = services
                                .getRestServiceDescription();
                        for (RestServiceDescription description : descriptions)
                        {
                            restFqns.add(description.getClassName());
                        }
                        return restFqns;
                    }

                });
        JavaSource javaSource = JavaSource.create(cpInfo);
        final Set<String> packs = new HashSet<String>();
        javaSource.runUserActionTask( new Task<CompilationController>() {
            
            @Override
            public void run( CompilationController controller ) throws Exception {
                packs.addAll( getPackages(fqns, controller));
            }
        }, true);
        getSupport().configure(packs.toArray( new String[packs.size()]));
        factory.restart(fileObject);
        return null;
    }
    
    protected RestSupport getSupport(){
        return project.getLookup().lookup(RestSupport.class);
    }
    
    protected Project getProject(){
        return project;
    }
    
    protected RestConfigurationEditorAwareTaskFactory getFactory(){
        return factory;
    }
    
    protected FileObject getFileObject(){
        return fileObject;
    }
    
    private Set<String> getPackages(Set<String> fqns , CompilationController controller){
        Set<String> set = new HashSet<String>();
        Set<TypeElement> restElements = getRestElements(fqns, controller );
        for (TypeElement typeElement : restElements) {
            PackageElement pack = controller.getElements().
                    getPackageOf(typeElement);
            set.add(pack.getQualifiedName().toString());
        }
        return set;
    }
    
    private Set<TypeElement> getRestElements(Set<String> fqns, 
            CompilationController controller)
    {
        Set<TypeElement> result = new HashSet<TypeElement>();
        for(String fqn : fqns ){
            TypeElement typeElement = controller.getElements().getTypeElement(fqn);
            if ( typeElement != null){
                result.add( typeElement );
            }
        }
        return result;
    }
    
    private Project project;
    private RestConfigurationEditorAwareTaskFactory factory;
    private FileObject fileObject;
    private ClasspathInfo cpInfo;
}
