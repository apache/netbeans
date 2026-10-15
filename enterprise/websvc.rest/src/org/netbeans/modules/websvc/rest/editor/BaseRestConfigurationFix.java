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
        getSupport().configure(packs.toArray(new String[0]));
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
