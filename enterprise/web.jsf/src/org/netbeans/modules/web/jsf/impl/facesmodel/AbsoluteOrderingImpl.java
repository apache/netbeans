/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.web.jsf.impl.facesmodel;

import java.util.List;

import org.netbeans.modules.web.jsf.api.facesmodel.AbsoluteOrdering;
import org.netbeans.modules.web.jsf.api.facesmodel.AbsoluteOrderingElement;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor;
import org.netbeans.modules.web.jsf.api.facesmodel.Name;
import org.netbeans.modules.web.jsf.api.facesmodel.Others;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
class AbsoluteOrderingImpl extends JSFConfigComponentImpl implements
        AbsoluteOrdering
{

    AbsoluteOrderingImpl( JSFConfigModelImpl model, Element element ) {
        super(model, element);
    }
    
    AbsoluteOrderingImpl( JSFConfigModelImpl model ) {
        super(model, createElementNS(model, JSFConfigQNames.ABSOLUTE_ORDERING));
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.AbsoluteOrdering#addElement(int, org.netbeans.modules.web.jsf.api.facesmodel.AbsoluteOrderingElement)
     */
    public void addElement( int index, AbsoluteOrderingElement element ) {
        String propName = null;
        if( element instanceof Name ){
            propName = NAME;
        }
        else if ( element instanceof Others ){
            propName = OTHERS;
        }
        assert propName != null: element.getClass()  + "  is not recognized child of " +
        	    AbsoluteOrdering.class +" element.";       // NOI18N
        insertAtIndex(propName, element, index, AbsoluteOrderingElement.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.AbsoluteOrdering#addName(org.netbeans.modules.web.jsf.api.facesmodel.Name)
     */
    public void addName( Name name ) {
        appendChild( NAME, name);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.AbsoluteOrdering#addOther(org.netbeans.modules.web.jsf.api.facesmodel.Others)
     */
    public void addOther( Others others ) {
        appendChild( OTHERS, others);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.AbsoluteOrdering#getElements()
     */
    public List<AbsoluteOrderingElement> getElements() {
        return getChildren( AbsoluteOrderingElement.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.AbsoluteOrdering#getNames()
     */
    public List<Name> getNames() {
        return getChildren(Name.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.AbsoluteOrdering#getOthers()
     */
    public List<Others> getOthers() {
        return getChildren(Others.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.AbsoluteOrdering#removeName(org.netbeans.modules.web.jsf.api.facesmodel.Name)
     */
    public void removeName( Name name ) {
        removeChild(NAME, name);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.AbsoluteOrdering#removeOthers(org.netbeans.modules.web.jsf.api.facesmodel.Others)
     */
    public void removeOthers( Others others ) {
        removeChild(OTHERS, others);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent#accept(org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor)
     */
    public void accept( JSFConfigVisitor visitor ) {
        visitor.visit( this );
    }

}
