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

import javax.xml.namespace.QName;

import org.netbeans.modules.jakarta.web.beans.xml.Alternatives;
import org.netbeans.modules.jakarta.web.beans.xml.BeanClass;
import org.netbeans.modules.jakarta.web.beans.xml.BeanClassContainer;
import org.netbeans.modules.jakarta.web.beans.xml.Beans;
import org.netbeans.modules.jakarta.web.beans.xml.Decorators;
import org.netbeans.modules.jakarta.web.beans.xml.Interceptors;
import org.netbeans.modules.jakarta.web.beans.xml.Stereotype;
import org.netbeans.modules.jakarta.web.beans.xml.WebBeansComponent;
import org.netbeans.modules.jakarta.web.beans.xml.WebBeansVisitor;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
class WebBeansComponentBuildVisitor implements WebBeansVisitor {

    WebBeansComponentBuildVisitor( WebBeansModelImpl model ) {
        myModel = model;
    }
    
    public void init() {
        myResult = null;
        myElement = null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.xml.WebBeansVisitor#visit(org.netbeans.modules.jakarta.web.beans.xml.Beans)
     */
    public void visit( Beans beans ) {
        if ( isAcceptable( WebBeansElements.INTERCEPTORS)){
            setResult( new InterceptorsImpl(getModel() , getElement()));
        }
        else if (isAcceptable( WebBeansElements.DECORATORS )){
            setResult( new DecoratorsImpl(getModel(), getElement()));
        }
        else if (isAcceptable( WebBeansElements.ALTERNATIVES)){
            setResult( new AlternativesImpl(getModel(), getElement()));
        }
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.xml.WebBeansVisitor#visit(org.netbeans.modules.jakarta.web.beans.xml.Interceptors)
     */
    public void visit( Interceptors interceptors ) {
        visitClassContainer( interceptors );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.xml.WebBeansVisitor#visit(org.netbeans.modules.jakarta.web.beans.xml.Decorators)
     */
    public void visit( Decorators decorators ) {
        visitClassContainer( decorators );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.xml.WebBeansVisitor#visit(org.netbeans.modules.jakarta.web.beans.xml.Alternatives)
     */
    public void visit( Alternatives alternatives ) {
        if ( isAcceptable( WebBeansElements.CLASS)){
            setResult( new BeanClassImpl(getModel(), getElement()));
        }
        else if ( isAcceptable( WebBeansElements.STEREOTYPE )){
            setResult( new StereotypeImpl( getModel(), getElement()));
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.xml.WebBeansVisitor#visit(org.netbeans.modules.jakarta.web.beans.xml.BeanClass)
     */
    public void visit( BeanClass clazz ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.xml.WebBeansVisitor#visit(org.netbeans.modules.jakarta.web.beans.xml.Stereotype)
     */
    public void visit( Stereotype stereotype ) {
    }

    WebBeansComponent create( WebBeansComponent context, Element element )
    {
        QName qName = AbstractDocumentComponent.getQName(element);
        if ( !(WebBeansComponent.WEB_BEANS_NAMESPACE.equals( 
                qName.getNamespaceURI() ) || 
                WebBeansComponent.WEB_BEANS_NAMESPACE_OLD.equals( 
                qName.getNamespaceURI() )))
        {
            return null;
        }
        if ( context == null ){
            return new BeansImpl( getModel() , element );
        }
        else {
            myElement = element;
            context.accept( this );
        }
        return myResult;
    }
    
    
    private void visitClassContainer( BeanClassContainer container ) {
        if ( isAcceptable( WebBeansElements.CLASS)){
            setResult( new BeanClassImpl(getModel(), getElement()));
        }        
    }
    
    private WebBeansModelImpl getModel(){
        return myModel;
    }
    
    private void setResult( WebBeansComponent component ) {
        myResult = component;
    }

    private boolean isAcceptable( WebBeansElements elements ) {
        return elements.getName().equals( getLocalName() );
    }

    private String getLocalName() {
        return getElement().getLocalName();
    }
    
    private Element getElement(){
        return myElement;
    }
    
    private WebBeansComponent myResult;
    
    private Element myElement;
    
    private WebBeansModelImpl myModel;

}
