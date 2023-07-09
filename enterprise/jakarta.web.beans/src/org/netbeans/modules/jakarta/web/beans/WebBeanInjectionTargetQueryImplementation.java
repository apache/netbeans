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
package org.netbeans.modules.jakarta.web.beans;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;

import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javaee.injection.spi.InjectionTargetQueryImplementation;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.jakarta.web.beans.api.model.WebBeansModel;


import org.openide.util.Parameters;


/**
 * @author ads
 *
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.javaee.injection.spi.InjectionTargetQueryImplementation.class)
public class WebBeanInjectionTargetQueryImplementation implements 
    InjectionTargetQueryImplementation
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.j2ee.common.queries.spi.InjectionTargetQueryImplementation#isInjectionTarget(org.netbeans.modules.j2ee.common.queries.spi.CompilationController, javax.lang.model.element.TypeElement)
     */
    @Override
    public boolean isInjectionTarget( CompilationController controller,
            TypeElement typeElement )
    {
        try {
            Parameters.notNull("controller", controller);
            Parameters.notNull("typeElement", typeElement);
            
            Project project = FileOwnerQuery.getOwner( controller.getFileObject() );
            if ( project == null ){
                return false;
            }
            MetaModelSupport support = new MetaModelSupport(project);
            MetadataModel<WebBeansModel> metaModel = support.getMetaModel();
            final ElementHandle<TypeElement> handle = ElementHandle.create(typeElement);
            return metaModel.runReadAction(new MetadataModelAction<WebBeansModel, Boolean>() {

                    @Override
                    public Boolean run( WebBeansModel model ) throws Exception {
                        TypeElement element = handle.resolve(model.getCompilationController());
                        if ( element == null ){
                            return false;
                        }
                        List<AnnotationMirror> qualifiers = model.getQualifiers(
                                element, true);
                        if ( qualifiers.size() == 0 ){
                            /* 
                             * @Named is special case.
                             * It could be present implicitly : there are 
                             * stereotype declared for the element which 
                             * is annotated by @Named.  
                             */
                            if ( model.getName( element ) != null ){
                                return true;
                            }
                            return false;
                        }
                        else {
                            /*
                             *  There are some qualifiers. 
                             *  So this bean is eligible for injection. But it
                             *  doesn't mean it is really managed by J2EE container.
                             */
                            return true;
                        }
                    }
                });
        } catch (MetadataModelException ex) {
            Logger.getLogger( WebBeanInjectionTargetQueryImplementation.class.getName()).
                log( Level.WARNING, ex.getMessage(), ex);
        } catch (IOException ex) {
            Logger.getLogger( WebBeanInjectionTargetQueryImplementation.class.getName()).
                log( Level.WARNING, ex.getMessage(), ex);
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.j2ee.common.queries.spi.InjectionTargetQueryImplementation#isStaticReferenceRequired(org.netbeans.modules.j2ee.common.queries.spi.CompilationController, javax.lang.model.element.TypeElement)
     */
    @Override
    public boolean isStaticReferenceRequired( CompilationController controller,
            TypeElement typeElement )
    {
        return false;
    }

}
