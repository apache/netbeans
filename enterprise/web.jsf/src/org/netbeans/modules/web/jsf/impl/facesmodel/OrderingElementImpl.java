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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.netbeans.modules.web.jsf.api.facesmodel.Name;
import org.netbeans.modules.web.jsf.api.facesmodel.OrderingElement;
import org.netbeans.modules.web.jsf.api.facesmodel.Others;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
abstract class OrderingElementImpl extends JSFConfigComponentImpl implements
        OrderingElement
{

    OrderingElementImpl( JSFConfigModelImpl model, Element element ) {
        super(model, element);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.OrderingElement#addName(org.netbeans.modules.web.jsf.api.facesmodel.Name)
     */
    public void addName( Name name ) {
        appendChild( NAME , name );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.OrderingElement#getNames()
     */
    public List<Name> getNames() {
        return getChildren( Name.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.OrderingElement#getOthers()
     */
    public Others getOthers() {
        return getChild( Others.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.OrderingElement#removeName(org.netbeans.modules.web.jsf.api.facesmodel.Name)
     */
    public void removeName( Name name ) {
        removeChild( NAME , name);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.OrderingElement#setOther(org.netbeans.modules.web.jsf.api.facesmodel.Others)
     */
    public void setOther( Others others ) {
        setChild( Others.class, OTHERS, others, Collections.EMPTY_LIST);
        reorderChildren();
    }
    
    protected List<String> getSortedListOfLocalNames(){
        return SORTED_ELEMENTS;
    }
    
    protected static final List<String> SORTED_ELEMENTS = new ArrayList<String>(2);
    static {
        SORTED_ELEMENTS.add( NAME );
        SORTED_ELEMENTS.add( OTHERS);
    }

}
