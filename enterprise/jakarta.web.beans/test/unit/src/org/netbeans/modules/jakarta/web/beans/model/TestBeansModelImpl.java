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
package org.netbeans.modules.jakarta.web.beans.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.modules.jakarta.web.beans.api.model.BeanArchiveType;

import org.netbeans.modules.jakarta.web.beans.api.model.BeansModel;
import org.netbeans.modules.jakarta.web.beans.xml.AlternativeElement;
import org.netbeans.modules.jakarta.web.beans.xml.Alternatives;
import org.netbeans.modules.jakarta.web.beans.xml.BeanClassContainer;
import org.netbeans.modules.jakarta.web.beans.xml.Decorators;
import org.netbeans.modules.jakarta.web.beans.xml.Interceptors;
import org.netbeans.modules.jakarta.web.beans.xml.BeanClass;
import org.netbeans.modules.jakarta.web.beans.xml.Stereotype;
import org.netbeans.modules.jakarta.web.beans.xml.WebBeansModel;
import org.netbeans.modules.jakarta.web.beans.xml.WebBeansModelFactory;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.omg.PortableInterceptor.Interceptor;
import org.openide.filesystems.FileObject;


/**
 * @author ads
 *
 */
class TestBeansModelImpl implements BeansModel {
    
    TestBeansModelImpl(FileObject sourceRoot ) 
    {
        FileObject fileObject = sourceRoot.getFileObject("beans.xml");
        if ( fileObject != null ) {
            myModel = WebBeansModelFactory.getInstance().getModel(
                    getModelSource(fileObject));
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.api.model.BeansModel#getAlternativeClasses()
     */
    @Override
    public Set<String> getAlternativeClasses() {
        Set<String> result = new HashSet<String>();
        for( BeanClass clazz : getAlternativeElement(BeanClass.class)){
            result.add( clazz.getBeanClass() );
        }
        return result;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.api.model.BeansModel#getAlternativeStereotypes()
     */
    @Override
    public Set<String> getAlternativeStereotypes() {
        Set<String> result = new HashSet<String>();
        for( Stereotype stereotype : getAlternativeElement(Stereotype.class)){
            result.add( stereotype.getStereotype() );
        }
        return result;
    }
    
    private <T extends AlternativeElement> List<T> getAlternativeElement( 
            Class<T> clazz)
    {
        if ( myModel == null ){
            return Collections.emptyList();
        }
        List<Alternatives> children = 
            myModel.getBeans().getChildren( Alternatives.class);
        List<T> result = new LinkedList<T>();
        for (Alternatives alternative : children) {
            List<T> elements = alternative.getChildren( clazz );
            result.addAll( elements );
        }
        return result;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.api.model.BeansModel#getDecoratorClasses()
     */
    @Override
    public LinkedHashSet<String> getDecoratorClasses() {
        LinkedHashSet<String> result = new LinkedHashSet<String>();
        if ( myModel == null ){
            return result;
        }
        List<Decorators> children = 
            myModel.getBeans().getChildren( Decorators.class);
        collectBeanClasses(result, children);
        return result;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.api.model.BeansModel#getIntercetorClasses()
     */
    @Override
    public LinkedHashSet<String> getInterceptorClasses() {
        LinkedHashSet<String> result = new LinkedHashSet<String>();
        if ( myModel == null ){
            return result;
        }
        List<Interceptors> children = 
            myModel.getBeans().getChildren( Interceptors.class);
        collectBeanClasses(result, children);
        return result;
    }

    private <T extends BeanClassContainer> void collectBeanClasses( 
            LinkedHashSet<String> resultCollection, List<T> containers )
    {
        for (T container : containers) {
            List<BeanClass> beansClasses = container.getBeansClasses();
            for (BeanClass beanClass : beansClasses) {
                resultCollection.add(beanClass.getBeanClass());
            }
        }
    }
    
    private ModelSource getModelSource( FileObject fileObject )
    {
        try {
            return Utilities.createModelSource( fileObject,false);
        } catch (CatalogModelException ex) {
            Logger.getLogger("global").log(java.util.logging.Level.SEVERE,
                ex.getMessage(), ex);   // NOI18N
        }
        return null;
    }

    private WebBeansModel myModel;

    @Override
    public BeanArchiveType getBeanArchiveType() {
        return BeanArchiveType.EXPLICIT;
    }

    @Override
    public boolean isCdi11OrLater() {
        return true;
    }
}
