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

import org.netbeans.modules.jakarta.web.beans.xml.AlternativeElement;
import org.netbeans.modules.jakarta.web.beans.xml.Alternatives;
import org.netbeans.modules.jakarta.web.beans.xml.BeanClass;
import org.netbeans.modules.jakarta.web.beans.xml.BeanClassContainer;
import org.netbeans.modules.jakarta.web.beans.xml.Beans;
import org.netbeans.modules.jakarta.web.beans.xml.BeansElement;
import org.netbeans.modules.jakarta.web.beans.xml.Decorators;
import org.netbeans.modules.jakarta.web.beans.xml.Interceptors;
import org.netbeans.modules.jakarta.web.beans.xml.Stereotype;
import org.netbeans.modules.jakarta.web.beans.xml.WebBeansComponent;
import org.netbeans.modules.jakarta.web.beans.xml.WebBeansVisitor;
import org.netbeans.modules.xml.xam.ComponentUpdater;


/**
 * @author ads
 *
 */
class SyncUpdateVisitor implements ComponentUpdater<WebBeansComponent>, WebBeansVisitor {

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.xml.WebBeansVisitor#visit(org.netbeans.modules.jakarta.web.beans.xml.Beans)
     */
    public void visit( Beans beans ) {
        assert false : "Should never add or remove beans root"; // NOI18N
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xam.ComponentUpdater#update(org.netbeans.modules.xml.xam.Component, org.netbeans.modules.xml.xam.Component, org.netbeans.modules.xml.xam.ComponentUpdater.Operation)
     */
    public void update( WebBeansComponent target, WebBeansComponent child,
            Operation operation )
    {
        update(target, child, -1 , operation);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xam.ComponentUpdater#update(org.netbeans.modules.xml.xam.Component, org.netbeans.modules.xml.xam.Component, int, org.netbeans.modules.xml.xam.ComponentUpdater.Operation)
     */
    public void update( WebBeansComponent target, WebBeansComponent child,
            int index, Operation operation )
    {
        assert target != null;
        assert child != null;
        assert operation == null || operation == Operation.ADD ||
            operation == Operation.REMOVE;

        myParent = target;
        myIndex = index;
        myOperation = operation;
        child.accept(this);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.xml.WebBeansVisitor#visit(org.netbeans.modules.jakarta.web.beans.xml.Interceptors)
     */
    public void visit( Interceptors interceptors ) {
        visitBeanElement(interceptors);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.xml.WebBeansVisitor#visit(org.netbeans.modules.jakarta.web.beans.xml.Decorators)
     */
    public void visit( Decorators decorators ) {
        visitBeanElement(decorators);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.xml.WebBeansVisitor#visit(org.netbeans.modules.jakarta.web.beans.xml.Alternatives)
     */
    public void visit( Alternatives alternatives ) {
        visitBeanElement(alternatives);        
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.xml.WebBeansVisitor#visit(org.netbeans.modules.jakarta.web.beans.xml.BeanClass)
     */
    public void visit( BeanClass clazz ) {
        if ( getParent() instanceof Alternatives ){
            visitAlternativesChild(clazz);
        }
        else if ( getParent() instanceof BeanClassContainer ){
            BeanClassContainer parent = (BeanClassContainer)getParent();
            if ( isAdd() ){
                parent.addBeanClass( getIndex() , clazz );
            }
            else if ( isRemove() ){
                parent.removeBeanClass( clazz );
            }
        }
        else {
            assert false;
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.xml.WebBeansVisitor#visit(org.netbeans.modules.jakarta.web.beans.xml.Stereotype)
     */
    public void visit( Stereotype stereotype ) {
        visitAlternativesChild(stereotype);
    }

    private void visitAlternativesChild( AlternativeElement element ) {
        assert getParent() instanceof Alternatives;
        Alternatives alternatives = (Alternatives)getParent();
        if ( isAdd() ){
            alternatives.addElement( getIndex() , element );
        }
        else if ( isRemove() ){
            alternatives.removeElement( element );
        }
    }
    
    private void visitBeanElement( BeansElement element ) {
        assert getParent() instanceof Beans;
        Beans beans = (Beans)getParent();
        if ( isAdd() ){
            beans.addElement( getIndex() , element );
        }
        else if ( isRemove() ){
            beans.removeElement( element );
        }
    }

    private boolean isAdd() {
        return getOperation() == Operation.ADD;
    }

    private boolean isRemove() {
        return getOperation() == Operation.REMOVE;
    }

    private WebBeansComponent getParent() {
        return myParent;
    }

    private int getIndex() {
        return myIndex;
    }

    private Operation getOperation() {
        return myOperation;
    }

    private WebBeansComponent myParent;

    private int myIndex;

    private Operation myOperation;

}
