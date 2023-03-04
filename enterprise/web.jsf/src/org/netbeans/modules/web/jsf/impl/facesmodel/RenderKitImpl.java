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
import java.util.LinkedList;
import java.util.List;

import org.netbeans.modules.web.jsf.api.facesmodel.FacesClientBehaviorRenderer;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesRenderer;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor;
import org.netbeans.modules.web.jsf.api.facesmodel.RenderKit;
import org.netbeans.modules.web.jsf.api.facesmodel.RenderKitExtension;
import org.netbeans.modules.web.jsf.api.metamodel.ClientBehaviorRenderer;
import org.netbeans.modules.web.jsf.api.metamodel.Renderer;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
class RenderKitImpl extends IdentifiableDescriptionGroupImpl implements
        RenderKit
{

    RenderKitImpl( JSFConfigModelImpl model, Element element ) {
        super(model, element);
    }
    
    RenderKitImpl( JSFConfigModelImpl model ) {
        super(model, createElementNS(model, JSFConfigQNames.RENDER_KIT));
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.RenderKit#addRenderKitExtension(org.netbeans.modules.web.jsf.api.facesmodel.RenderKitExtension)
     */
    public void addRenderKitExtension( RenderKitExtension extension ) {
        appendChild( RENDER_KIT_EXTENSION, extension);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.RenderKit#addRenderKitExtension(int, org.netbeans.modules.web.jsf.api.facesmodel.RenderKitExtension)
     */
    public void addRenderKitExtension( int index, RenderKitExtension extension )
    {
        insertAtIndex( RENDER_KIT_EXTENSION, extension, index, RenderKitExtension.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.RenderKit#addRenderer(org.netbeans.modules.web.jsf.api.facesmodel.FacesRenderer)
     */
    public void addRenderer( FacesRenderer renderer ) {
        appendChild( RENDERER, renderer);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.RenderKit#addRenderer(int, org.netbeans.modules.web.jsf.api.facesmodel.FacesRenderer)
     */
    public void addRenderer( int index, FacesRenderer renderer ) {
        insertAtIndex( RENDERER, renderer, index, FacesRenderer.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.RenderKit#getClientBehaviorRenderers()
     */
    public List<ClientBehaviorRenderer> getClientBehaviorRenderers() {
        List<FacesClientBehaviorRenderer> list = getChildren( FacesClientBehaviorRenderer.class );
        List<ClientBehaviorRenderer> result = new LinkedList<ClientBehaviorRenderer>();
        result.addAll( list );
        AbstractJsfModel model = getModel().getModelSource().getLookup().lookup( 
                AbstractJsfModel.class );
        if ( model != null && getRenderKitId()!= null ){
            result.addAll(model.getClientBehaviorRenderers( getRenderKitId() ));
        }
        return result;
    }

    /**
     * Gets render-kit-class of the faces-config-render-kitType.
     * @return trimmed render-kit-class if any, {@code null} otherwise
     */
    public String getRenderKitClass() {
        String kitClass = getChildElementText(JSFConfigQNames.RENDER_KIT_CLASS.getQName(getNamespaceURI()));
        return ElementTypeHelper.pickFullyQualifiedClassType(kitClass);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.RenderKit#getRenderKitExtensions()
     */
    public List<RenderKitExtension> getRenderKitExtensions() {
        return getChildren(RenderKitExtension.class) ;
    }

    /**
     * Gets render-kit-id of the faces-config-render-kitType.
     * @return trimmed render-kit-id if any, {@code null} otherwise
     */
    public String getRenderKitId() {
        String kitId = getChildElementText(JSFConfigQNames.RENDER_KIT_ID.getQName(getNamespaceURI()));
        return ElementTypeHelper.pickString(kitId);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.RenderKit#getRenderers()
     */
    public List<Renderer> getRenderers() {
        List<FacesRenderer> list = getChildren( FacesRenderer.class);
        List<Renderer> result = new LinkedList<Renderer>();
        result.addAll( list );
        AbstractJsfModel model = getModel().getModelSource().getLookup().lookup( 
                AbstractJsfModel.class );
        if ( model != null && getRenderKitId()!= null ){
            result.addAll(model.getRenderers( getRenderKitId() ));
        }
        return result;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.RenderKit#removeRenderKitExtension(org.netbeans.modules.web.jsf.api.facesmodel.RenderKitExtension)
     */
    public void removeRenderKitExtension( RenderKitExtension extension ) {
        removeChild( RENDER_KIT_EXTENSION, extension);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.RenderKit#removeRenderer(org.netbeans.modules.web.jsf.api.facesmodel.FacesRenderer)
     */
    public void removeRenderer( FacesRenderer renderer ) {
        removeChild(RENDERER, renderer);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.RenderKit#setRenderKitClass(java.lang.String)
     */
    public void setRenderKitClass( String clazz ) {
        setChildElementText(RENDER_KIT_CLASS, clazz, 
                JSFConfigQNames.RENDER_KIT_CLASS.getQName(getNamespaceURI()));
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.RenderKit#setRenderKitId(java.lang.String)
     */
    public void setRenderKitId( String id ) {
        setChildElementText(RENDER_KIT_ID, id, 
                JSFConfigQNames.RENDER_KIT_ID.getQName(getNamespaceURI()));
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent#accept(org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor)
     */
    public void accept( JSFConfigVisitor visitor ) {
        visitor.visit( this );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.RenderKit#addClientBehaviorRenderer(org.netbeans.modules.web.jsf.api.facesmodel.FacesClientBehaviorRenderer)
     */
    public void addClientBehaviorRenderer( FacesClientBehaviorRenderer renderer )
    {
        appendChild( RENDERER, renderer);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.RenderKit#addClientBehaviorRenderer(int, org.netbeans.modules.web.jsf.api.facesmodel.FacesClientBehaviorRenderer)
     */
    public void addClientBehaviorRenderer( int index,
            FacesClientBehaviorRenderer renderer )
    {
        insertAtIndex( CLIENT_BEHAVIOR_RENDERER, renderer, index,
                FacesClientBehaviorRenderer.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.RenderKit#removeaddClientBehaviorRenderer(org.netbeans.modules.web.jsf.api.facesmodel.FacesClientBehaviorRenderer)
     */
    public void removeaddClientBehaviorRenderer(
            FacesClientBehaviorRenderer renderer )
    {
        removeChild( CLIENT_BEHAVIOR_RENDERER, renderer);
    }
    
    protected List<String> getSortedListOfLocalNames(){
        return SORTED_ELEMENTS;
    }
    
    protected static final List<String> SORTED_ELEMENTS = new ArrayList<String>(
            DESCRIPTION_GROUP_SORTED_ELEMENTS.size() + 5 );
    
    static {
        SORTED_ELEMENTS.addAll( DESCRIPTION_GROUP_SORTED_ELEMENTS );
        SORTED_ELEMENTS.add( RENDER_KIT_ID);
        SORTED_ELEMENTS.add( RENDER_KIT_CLASS );
        SORTED_ELEMENTS.add( RENDERER );  
        SORTED_ELEMENTS.add( CLIENT_BEHAVIOR_RENDERER );
        SORTED_ELEMENTS.add( RENDER_KIT_EXTENSION );
    }

}
