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
package org.netbeans.modules.web.jsf.api.facesmodel;

import java.util.List;

import org.netbeans.modules.web.jsf.impl.facesmodel.JSFConfigQNames;


/**
 * @author ads
 *
 */
public interface Factory extends FacesConfigElement, IdentifiableElement {

    /**
     * Property name of &lt;application-factory&gt; element.
     */
    String APPLICATION_FACTORY = JSFConfigQNames.APPLICATION_FACTORY.getLocalName();

    /**
     * Property name of &lt;exception-handler-factory&gt; element.
     */
    String EXCEPTION_HANDLER_FACTORY = JSFConfigQNames.EXCEPTION_HANDLER_FACTORY.getLocalName();

    /**
     * Property name of &lt;external-context-factory&gt; element.
     */
    String EXTERNAL_CONTEXT_FACTORY = JSFConfigQNames.EXTERNAL_CONTEXT_FACTORY.getLocalName();

    /**
     * Property name of &lt;faces-context-factory&gt; element.
     */
    String FACES_CONTEXT_FACTORY = JSFConfigQNames.FACES_CONTEXT_FACTORY.getLocalName();

    /**
     * Property name of &lt;facelet-cache-factory&gt; element.
     */
    String FACELET_CACHE_FACTORY = JSFConfigQNames.FACELET_CACHE_FACTORY.getLocalName();

    /**
     * Property name of &lt;partial-view-context-factory&gt; element.
     */
    String PARTIAL_VIEW_CONTEXT_FACTORY = JSFConfigQNames.PARTIAL_VIEW_CONTEXT_FACTORY.getLocalName();

    /**
     * Property name of &lt;lifecycle-factory&gt; element.
     */
    String LIFECYCLE_FACTORY = JSFConfigQNames.LIFECYCLE_FACTORY.getLocalName();

    /**
     * Property name of &lt;view-declaration-language-factory&gt; element.
     */
    String VIEW_DECLARATION_LANGUAGE_FACTORY = JSFConfigQNames.VIEW_DECLARATION_LANGUAGE_FACTORY.getLocalName();

    /**
     * Property name of &lt;tag-handler-delegate-factory&gt; element.
     */
    String TAG_HANDLER_DELEGATE_FACTORY = JSFConfigQNames.TAG_HANDLER_DELEGATE_FACTORY.getLocalName();

    /**
     * Property name of &lt;render-kit-factory&gt; element.
     */
    String RENDER_KIT_FACTORY = JSFConfigQNames.RENDER_KIT_FACTORY.getLocalName();

    /**
     * Property name of &lt;visit-context-factory&gt; element.
     */
    String VISIT_CONTEXT_FACTORY = JSFConfigQNames.VISIT_CONTEXT_FACTORY.getLocalName();

    /**
     * Property name of &lt;factory-extension&gt; element.
     */
    String FACTORY_EXTENSION = JSFConfigQNames.FACTORY_EXTENSION.getLocalName();

    /**
     * Property name of &lt;flash-factory&gt; element.
     */
    String FLASH_FACTORY = JSFConfigQNames.FLASH_FACTORY.getLocalName();

    /**
     * Property name of &lt;flow-handler-factory&gt; element.
     */
    String FLOW_HANDLER_FACTORY = JSFConfigQNames.FLOW_HANDLER_FACTORY.getLocalName();

    List<FactoryElement> getElements();
    void addElement( int index , FactoryElement element );

    List<ApplicationFactory> getApplicationFactories();
    void addApplicationFactory( ApplicationFactory factory );
    void removeApplicationFactory( ApplicationFactory factory );

    List<ExceptionHandlerFactory> getExceptionHandlerFactories();
    void addExceptionHandlerFactory( ExceptionHandlerFactory factory );
    void removeExceptionHandlerFactory( ExceptionHandlerFactory factory );

    List<ExternalContextFactory> getExternalContextFactories();
    void addExternalContextFactory( ExternalContextFactory factory  );
    void removeExternalContextFactory( ExternalContextFactory factory );

    List<FacesContextFactory> getFacesContextFactories();
    void addFacesContextFactory( FacesContextFactory factory );
    void removeFacesContextFactory( FacesContextFactory factory );

    List<FaceletCacheFactory> getFaceletCacheFactories();
    void addFaceletCacheFactory( FaceletCacheFactory factory );
    void removeFaceletCacheFactory( FaceletCacheFactory factory );

    List<PartialViewContextFactory> getPartialViewContextFactories();
    void addPartialViewContextFactory( PartialViewContextFactory factory );
    void removePartialViewContextFactory( PartialViewContextFactory factory );

    List<LifecycleFactory> getLifecycleFactories();
    void addLifecycleFactory( LifecycleFactory factory );
    void removeLifecycleFactory(LifecycleFactory factory );

    List<ViewDeclarationLanguageFactory> getViewDeclarationLanguageFactories();
    void addViewDeclarationLanguageFactory( ViewDeclarationLanguageFactory factory );
    void removeViewDeclarationLanguageFactory( ViewDeclarationLanguageFactory factory );

    List<TagHandlerDelegateFactory> getTagHandlerDelegateFactories();
    void addTagHandlerDelegateFactory(TagHandlerDelegateFactory factory );
    void removeTagHandlerDelegateFactory( TagHandlerDelegateFactory factory );

    List<RenderKitFactory> getRenderKitFactories();
    void addRenderKitFactory( RenderKitFactory factory );
    void removeRenderKitFactory( RenderKitFactory factory );

    List<VisitContextFactory> getVisitContextFactories();
    void addVisitContextFactory( VisitContextFactory factory );
    void removeVisitContextFactory( VisitContextFactory factory );

    List<FactoryExtension> getFactoryExtensions();
    void addFactoryExtension(FactoryExtension extension);
    void removeFactoryExtension( FactoryExtension extension );

    List<FlashFactory> getFlashFactory();
    void addFlashFactory(FlashFactory flashFactory);
    void removeFlashFactory(FlashFactory flashFactory);

    List<FlowHandlerFactory> getFlowHandlerFactory();
    void addFlowHandlerFactory(FlowHandlerFactory flowHandlerFactory);
    void removeFlowHandlerFactory(FlowHandlerFactory flowHandlerFactory);
}
