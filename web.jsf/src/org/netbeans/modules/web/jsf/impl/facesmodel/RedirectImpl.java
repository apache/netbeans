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

import java.util.List;

import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor;
import org.netbeans.modules.web.jsf.api.facesmodel.Redirect;
import org.netbeans.modules.web.jsf.api.facesmodel.ViewParam;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
class RedirectImpl extends IdentifiableComponentImpl implements Redirect
{

    RedirectImpl( JSFConfigModelImpl model, Element element ) {
        super(model, element);
    }
    
    RedirectImpl( JSFConfigModelImpl model) {
        this (model, createElementNS(model, JSFConfigQNames.REDIRECT));
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Redirect#addViewParam(org.netbeans.modules.web.jsf.api.facesmodel.ViewParam)
     */
    public void addViewParam( ViewParam param ) {
        appendChild( VIEW_PARAM, param);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Redirect#addViewParam(int, org.netbeans.modules.web.jsf.api.facesmodel.ViewParam)
     */
    public void addViewParam( int index, ViewParam param ) {
        insertAtIndex( VIEW_PARAM, param, index, ViewParam.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Redirect#getIncludeViewParams()
     */
    public Boolean getIncludeViewParams() {
        String value = getAttribute(FacesAttributes.INCLUDE_VIEW_PARAMS);
        return value == null ? null : Boolean.valueOf( value );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Redirect#getViewParams()
     */
    public List<ViewParam> getViewParams() {
        return getChildren( ViewParam.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Redirect#removeViewParam(org.netbeans.modules.web.jsf.api.facesmodel.ViewParam)
     */
    public void removeViewParam( ViewParam param ) {
        removeChild( VIEW_PARAM, param);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Redirect#setIncludeViewParams(java.lang.Boolean)
     */
    public void setIncludeViewParams( Boolean value ) {
        setAttribute( INCLUDE_VIEW_PARAMS, FacesAttributes.INCLUDE_VIEW_PARAMS, 
                value==null? null : value);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent#accept(org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor)
     */
    public void accept( JSFConfigVisitor visitor ) {
        visitor.visit( this );
    }

}
