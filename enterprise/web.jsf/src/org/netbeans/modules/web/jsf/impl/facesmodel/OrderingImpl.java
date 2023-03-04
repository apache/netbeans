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

import org.netbeans.modules.web.jsf.api.facesmodel.After;
import org.netbeans.modules.web.jsf.api.facesmodel.Before;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor;
import org.netbeans.modules.web.jsf.api.facesmodel.Ordering;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
class OrderingImpl extends JSFConfigComponentImpl implements Ordering {

    OrderingImpl( JSFConfigModelImpl model, Element element ) {
        super(model, element);
    }
    
    OrderingImpl( JSFConfigModelImpl model ) {
        super(model, createElementNS(model, JSFConfigQNames.ORDERING));
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Ordering#setAfter(org.netbeans.modules.web.jsf.api.facesmodel.After)
     */
    public void setAfter( After after ) {
        setChild(After.class, AFTER, after, Collections.EMPTY_LIST );
        reorderChildren();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Ordering#setBefore(org.netbeans.modules.web.jsf.api.facesmodel.Before)
     */
    public void setBefore( Before before ) {
        setChild( Before.class , BEFORE, before , Collections.EMPTY_LIST );
        reorderChildren();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Ordering#getAfter()
     */
    public After getAfter() {
        return getChild( After.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Ordering#getBefore()
     */
    public Before getBefore() {
        return getChild( Before.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent#accept(org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor)
     */
    public void accept( JSFConfigVisitor visitor ) {
        visitor.visit( this );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.impl.facesmodel.JSFConfigComponentImpl#getSortedListOfLocalNames()
     */
    @Override
    protected List<String> getSortedListOfLocalNames() {
        return SORTED_ELEMENTS;
    }

    protected static final List<String> SORTED_ELEMENTS = new ArrayList<String>(2);
    static {
        SORTED_ELEMENTS.add( AFTER);
        SORTED_ELEMENTS.add( BEFORE);
    }
}
