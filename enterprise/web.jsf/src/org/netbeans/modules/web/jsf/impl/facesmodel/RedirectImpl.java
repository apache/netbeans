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
