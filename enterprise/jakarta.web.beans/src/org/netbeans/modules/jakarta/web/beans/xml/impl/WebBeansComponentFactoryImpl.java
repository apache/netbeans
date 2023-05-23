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
package org.netbeans.modules.jakarta.web.beans.xml.impl;

import org.netbeans.modules.jakarta.web.beans.xml.Alternatives;
import org.netbeans.modules.jakarta.web.beans.xml.BeanClass;
import org.netbeans.modules.jakarta.web.beans.xml.Beans;
import org.netbeans.modules.jakarta.web.beans.xml.Decorators;
import org.netbeans.modules.jakarta.web.beans.xml.Interceptors;
import org.netbeans.modules.jakarta.web.beans.xml.Stereotype;
import org.netbeans.modules.jakarta.web.beans.xml.WebBeansComponent;
import org.netbeans.modules.jakarta.web.beans.xml.WebBeansComponentFactory;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
class WebBeansComponentFactoryImpl implements WebBeansComponentFactory {
    
    WebBeansComponentFactoryImpl( WebBeansModelImpl model ){
        myModel = model;
        myBuilder = new ThreadLocal<WebBeansComponentBuildVisitor>();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.xml.WebBeansComponentFactory#createBeans()
     */
    public Beans createBeans() {
        return new BeansImpl( getModel());
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.xml.WebBeansComponentFactory#createComponent(org.w3c.dom.Element, org.netbeans.modules.jakarta.web.beans.xml.WebBeansComponent)
     */
    public WebBeansComponent createComponent( Element element,
            WebBeansComponent context )
    {
        WebBeansComponentBuildVisitor visitor = getBuilder();
        return visitor.create( context , element );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.xml.WebBeansComponentFactory#createAlternatives()
     */
    public Alternatives createAlternatives() {
        return new AlternativesImpl(getModel());
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.xml.WebBeansComponentFactory#createBeanClass()
     */
    public BeanClass createBeanClass() {
        return new BeanClassImpl( getModel());
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.xml.WebBeansComponentFactory#createDecorators()
     */
    public Decorators createDecorators() {
        return new DecoratorsImpl( getModel());
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.xml.WebBeansComponentFactory#createInterceptors()
     */
    public Interceptors createInterceptors() {
        return new InterceptorsImpl(getModel());
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.xml.WebBeansComponentFactory#createStereotype()
     */
    public Stereotype createStereotype() {
        return new StereotypeImpl(getModel());
    }
    
    private WebBeansModelImpl getModel(){
        return myModel;
    }
    
    private WebBeansComponentBuildVisitor getBuilder(){
        WebBeansComponentBuildVisitor visitor = myBuilder.get();
        if ( visitor == null ){
            visitor = new WebBeansComponentBuildVisitor( getModel() );
            myBuilder.set( visitor );
        }
        visitor.init();
        return visitor;
    }
    
    private WebBeansModelImpl myModel;
    
    private ThreadLocal<WebBeansComponentBuildVisitor> myBuilder;

}
