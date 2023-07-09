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
package org.netbeans.modules.jakarta.web.beans.impl.model;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.PersistentObjectManager;
import org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelImplementation;
import org.netbeans.modules.jakarta.web.beans.api.model.AbstractModelImplementation;
import org.netbeans.modules.jakarta.web.beans.api.model.BeansModel;
import org.netbeans.modules.jakarta.web.beans.api.model.ModelUnit;
import org.netbeans.modules.jakarta.web.beans.api.model.WebBeansModel;


/**
 * @author ads
 *
 */
public class WebBeansModelImplementation extends AbstractModelImplementation 
    implements MetadataModelImplementation<WebBeansModel>
{

    protected WebBeansModelImplementation( ModelUnit unit ){
        super( unit );
        myManagers = new HashMap<String, PersistentObjectManager<BindingQualifier>>();
        myStereotypedManagers = new HashMap<String, PersistentObjectManager<StereotypedObject>>();
        myHelper = AnnotationModelHelper.create( getModelUnit().getClassPathInfo() );
    }
    
    public static MetadataModelImplementation<WebBeansModel> createMetaModel( 
            ModelUnit unit )
    {
        return new WebBeansModelImplementation( unit );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.api.model.AbstractModelImplementation#getBeansModel()
     */
    @Override
    public BeansModel getBeansModel() {
        return super.getBeansModel();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelImplementation#isReady()
     */
    @Override
    public boolean isReady() {
        return !getHelper().isJavaScanInProgress();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelImplementation#runReadAction(org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction)
     */
    @Override
    public <R> R runReadAction( final MetadataModelAction<WebBeansModel, R> action )
            throws MetadataModelException, IOException
    {
        return getHelper().runJavaSourceTask(new Callable<R>() {
            @Override
            public R call() throws Exception {
                return action.run(getModel());
            }
        });
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelImplementation#runReadActionWhenReady(org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction)
     */
    @Override
    public <R> Future<R> runReadActionWhenReady(
            final MetadataModelAction<WebBeansModel, R> action )
            throws MetadataModelException, IOException
    {
        return getHelper().runJavaSourceTaskWhenScanFinished(new Callable<R>() {
            @Override
            public R call() throws Exception {
                return action.run(getModel());
            }
        });
    }
    
    protected AnnotationModelHelper getHelper() {
        return myHelper;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.api.model.AbstractModelImplementation#getModel()
     */
    @Override
    protected WebBeansModel getModel() {
        return super.getModel();
    }
    
    Map<String,PersistentObjectManager<BindingQualifier>> getManagers(){
        return myManagers;
    }
    
    PersistentObjectManager<BindingQualifier> getManager( String annotationFQN ){
        PersistentObjectManager<BindingQualifier> result = getManagers().get(
                annotationFQN);
        if ( result == null ) {
            result  = getHelper().createPersistentObjectManager( 
                    new AnnotationObjectProvider( getHelper(), annotationFQN));
            getManagers().put(  annotationFQN , result);
        }
        return result;
    }
    
    PersistentObjectManager<BindingQualifier> getNamedManager(){
        return getManager( FieldInjectionPointLogic.NAMED_QUALIFIER_ANNOTATION );
    }
    
    PersistentObjectManager<NamedStereotype> getNamedStereotypesManager(){
        if ( myStereotypesManager == null ){
            myStereotypesManager = getHelper().createPersistentObjectManager(
                    new NamedStereotypeObjectProvider( getHelper()));
        }
        return myStereotypesManager;
    }
    
    PersistentObjectManager<StereotypedObject> getStereotypedManager( 
            String stereotype )
    {
        PersistentObjectManager<StereotypedObject> result = 
            getStereotypedManagers().get(stereotype);
        if ( result == null ) {
            result  = getHelper().createPersistentObjectManager( 
                    new StereotypedObjectProvider( stereotype, getHelper()));
            getStereotypedManagers().put(  stereotype , result);
        }
        return result;
    }
    
    Map<String,PersistentObjectManager<StereotypedObject>> getStereotypedManagers(){
        return myStereotypedManagers;
    }
    
    PersistentObjectManager<DecoratorObject> getDecoratorsManager(){
        if ( myDecoratorsManager == null ){
            myDecoratorsManager = getHelper().createPersistentObjectManager( 
                    new DecoratorObjectProvider( getHelper()));
        }
        return myDecoratorsManager;
    }
    
    PersistentObjectManager<InterceptorObject> getInterceptorsManager(){
        if ( myInterceptorsManager == null ){
            myInterceptorsManager = getHelper().createPersistentObjectManager( 
                    new InterceptorObjectProvider( getHelper()));
        }
        return myInterceptorsManager;
    }
    
    Set<String> adjustStereotypesManagers(){
        Set<String> stereotypes = getStereotypedManagers().keySet();
        Collection<NamedStereotype> namedStereotypes = getNamedStereotypesManager().
            getObjects();
        Set<String> existingStereotypes = new HashSet<String>(namedStereotypes.size());
        for (NamedStereotype namedStereotype : namedStereotypes) {
            if( namedStereotype!=null && namedStereotype.getTypeElement()!=null) {
                String name = namedStereotype.getTypeElement().getQualifiedName().
                    toString();
                if ( !stereotypes.contains( name)){
                    getStereotypedManager(name);
                }
                existingStereotypes.add( name );
            }
        }
        if ( existingStereotypes.size() == getStereotypedManagers().keySet().size()){
            return existingStereotypes;
        }
        for (Iterator<String> iterator = getStereotypedManagers().keySet().iterator();
            iterator.hasNext(); ) 
        {
            String stereotype = iterator.next();
            if ( !existingStereotypes.contains( stereotype)){
                iterator.remove();
            }
        }
        return existingStereotypes;
    }
    
    private Map<String,PersistentObjectManager<BindingQualifier>> myManagers;
    private PersistentObjectManager<NamedStereotype> myStereotypesManager;
    private PersistentObjectManager<DecoratorObject> myDecoratorsManager;
    private PersistentObjectManager<InterceptorObject> myInterceptorsManager;
    private Map<String,PersistentObjectManager<StereotypedObject>> myStereotypedManagers; 
    private AnnotationModelHelper myHelper;
}
