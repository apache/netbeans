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
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.web.beans.xml.impl;

import javax.xml.namespace.QName;

import org.netbeans.modules.web.beans.xml.Alternatives;
import org.netbeans.modules.web.beans.xml.BeanClass;
import org.netbeans.modules.web.beans.xml.BeanClassContainer;
import org.netbeans.modules.web.beans.xml.Beans;
import org.netbeans.modules.web.beans.xml.Decorators;
import org.netbeans.modules.web.beans.xml.Interceptors;
import org.netbeans.modules.web.beans.xml.Stereotype;
import org.netbeans.modules.web.beans.xml.WebBeansComponent;
import org.netbeans.modules.web.beans.xml.WebBeansVisitor;
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
     * @see org.netbeans.modules.web.beans.xml.WebBeansVisitor#visit(org.netbeans.modules.web.beans.xml.Beans)
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
     * @see org.netbeans.modules.web.beans.xml.WebBeansVisitor#visit(org.netbeans.modules.web.beans.xml.Interceptors)
     */
    public void visit( Interceptors interceptors ) {
        visitClassContainer( interceptors );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.xml.WebBeansVisitor#visit(org.netbeans.modules.web.beans.xml.Decorators)
     */
    public void visit( Decorators decorators ) {
        visitClassContainer( decorators );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.xml.WebBeansVisitor#visit(org.netbeans.modules.web.beans.xml.Alternatives)
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
     * @see org.netbeans.modules.web.beans.xml.WebBeansVisitor#visit(org.netbeans.modules.web.beans.xml.BeanClass)
     */
    public void visit( BeanClass clazz ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.xml.WebBeansVisitor#visit(org.netbeans.modules.web.beans.xml.Stereotype)
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
