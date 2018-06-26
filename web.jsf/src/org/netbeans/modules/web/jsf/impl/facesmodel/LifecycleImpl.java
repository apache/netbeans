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
