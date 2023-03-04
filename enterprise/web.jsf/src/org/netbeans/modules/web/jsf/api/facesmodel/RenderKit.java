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
package org.netbeans.modules.web.jsf.api.facesmodel;

import java.util.List;

import org.netbeans.modules.web.jsf.api.metamodel.ClientBehaviorRenderer;
import org.netbeans.modules.web.jsf.api.metamodel.Renderer;
import org.netbeans.modules.web.jsf.impl.facesmodel.JSFConfigQNames;


/**
 * @author ads
 *
 */
public interface RenderKit extends FacesConfigElement, DescriptionGroup, 
    IdentifiableElement 
{

    String RENDER_KIT_ID = JSFConfigQNames.RENDER_KIT_ID.getLocalName();
    String RENDER_KIT_CLASS = JSFConfigQNames.RENDER_KIT_CLASS.getLocalName();
    String RENDERER = JSFConfigQNames.RENDERER.getLocalName();
    String CLIENT_BEHAVIOR_RENDERER = JSFConfigQNames.CLIENT_BEHAVIOR_RENDERER.getLocalName();
    String RENDER_KIT_EXTENSION = JSFConfigQNames.RENDER_KIT_EXTENSION.getLocalName();
    
    String getRenderKitId();
    void setRenderKitId( String id );
    
    String getRenderKitClass();
    void setRenderKitClass( String clazz);
    
    List<Renderer> getRenderers();
    void addRenderer( FacesRenderer renderer );
    void addRenderer( int index , FacesRenderer renderer );
    void removeRenderer(FacesRenderer renderer );
    
    List<ClientBehaviorRenderer> getClientBehaviorRenderers();
    void addClientBehaviorRenderer( FacesClientBehaviorRenderer renderer);
    void addClientBehaviorRenderer( int index,  FacesClientBehaviorRenderer renderer);
    void removeaddClientBehaviorRenderer( FacesClientBehaviorRenderer renderer);
    
    List<RenderKitExtension> getRenderKitExtensions();
    void addRenderKitExtension( RenderKitExtension extension );
    void addRenderKitExtension( int index,  RenderKitExtension extension );
    void removeRenderKitExtension( RenderKitExtension extension );
}
