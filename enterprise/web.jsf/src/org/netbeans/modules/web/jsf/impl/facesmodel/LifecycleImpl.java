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
import java.util.List;

import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor;
import org.netbeans.modules.web.jsf.api.facesmodel.Lifecycle;
import org.netbeans.modules.web.jsf.api.facesmodel.LifecycleExtension;
import org.netbeans.modules.web.jsf.api.facesmodel.PhaseListener;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
class LifecycleImpl extends IdentifiableComponentImpl implements Lifecycle {

    LifecycleImpl( JSFConfigModelImpl model, Element element ) {
        super(model, element);
    }
    
    LifecycleImpl( JSFConfigModelImpl model ) {
        super(model, createElementNS(model, JSFConfigQNames.LIFECYCLE ));
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Lifecycle#addLifecycleExtension(org.netbeans.modules.web.jsf.api.facesmodel.LifecycleExtension)
     */
    public void addLifecycleExtension( LifecycleExtension extension ) {
        appendChild( LIFECYCLE_EXTENSION, extension);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Lifecycle#addLifecycleExtension(int, org.netbeans.modules.web.jsf.api.facesmodel.LifecycleExtension)
     */
    public void addLifecycleExtension( int index, LifecycleExtension extension )
    {
        insertAtIndex( LIFECYCLE_EXTENSION, extension, index, LifecycleExtension.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Lifecycle#addPhaseListener(org.netbeans.modules.web.jsf.api.facesmodel.PhaseListener)
     */
    public void addPhaseListener( PhaseListener listener ) {
        appendChild( PHASE_LISTENER,  listener);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Lifecycle#addPhaseListener(int, org.netbeans.modules.web.jsf.api.facesmodel.PhaseListener)
     */
    public void addPhaseListener( int index, PhaseListener listener ) {
        insertAtIndex( PHASE_LISTENER, listener, index, PhaseListener.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Lifecycle#getLifecycleExtensions()
     */
    public List<LifecycleExtension> getLifecycleExtensions() {
        return getChildren( LifecycleExtension.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Lifecycle#getPhaseListeners()
     */
    public List<PhaseListener> getPhaseListeners() {
        return getChildren( PhaseListener.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Lifecycle#removeLifecycleExtension(org.netbeans.modules.web.jsf.api.facesmodel.LifecycleExtension)
     */
    public void removeLifecycleExtension( LifecycleExtension extension ) {
        removeChild( LIFECYCLE_EXTENSION, extension);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Lifecycle#removePhaseListener(org.netbeans.modules.web.jsf.api.facesmodel.PhaseListener)
     */
    public void removePhaseListener( PhaseListener listener ) {
        removeChild( PHASE_LISTENER , listener);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent#accept(org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor)
     */
    public void accept( JSFConfigVisitor visitor ) {
        visitor.visit( this );
    }
    
    protected List<String> getSortedListOfLocalNames(){
        return SORTED_ELEMENTS;
    }
    
    protected static final List<String> SORTED_ELEMENTS = new ArrayList<String>(2);
    
    static {
        SORTED_ELEMENTS.add( PHASE_LISTENER);
        SORTED_ELEMENTS.add( LIFECYCLE_EXTENSION );
    }

}
