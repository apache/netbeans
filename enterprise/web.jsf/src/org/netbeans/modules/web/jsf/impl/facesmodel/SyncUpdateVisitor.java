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

import org.netbeans.modules.web.jsf.api.facesmodel.AbsoluteOrdering;
import org.netbeans.modules.web.jsf.api.facesmodel.ActionListener;
import org.netbeans.modules.web.jsf.api.facesmodel.After;
import org.netbeans.modules.web.jsf.api.facesmodel.Application;
import org.netbeans.modules.web.jsf.api.facesmodel.ApplicationFactory;
import org.netbeans.modules.web.jsf.api.facesmodel.AttributeContainer;
import org.netbeans.modules.web.jsf.api.facesmodel.Before;
import org.netbeans.modules.web.jsf.api.facesmodel.ConfigAttribute;
import org.netbeans.modules.web.jsf.api.facesmodel.ContractMapping;
import org.netbeans.modules.web.jsf.api.facesmodel.Converter;
import org.netbeans.modules.web.jsf.api.facesmodel.DefaultLocale;
import org.netbeans.modules.web.jsf.api.facesmodel.DefaultRenderKitId;
import org.netbeans.modules.web.jsf.api.facesmodel.DefaultValidators;
import org.netbeans.modules.web.jsf.api.facesmodel.Description;
import org.netbeans.modules.web.jsf.api.facesmodel.DescriptionGroup;
import org.netbeans.modules.web.jsf.api.facesmodel.DisplayName;
import org.netbeans.modules.web.jsf.api.facesmodel.ElResolver;
import org.netbeans.modules.web.jsf.api.facesmodel.ExceptionHandlerFactory;
import org.netbeans.modules.web.jsf.api.facesmodel.ExternalContextFactory;
import org.netbeans.modules.web.jsf.api.facesmodel.FaceletCacheFactory;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesBehavior;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesClientBehaviorRenderer;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesComponent;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesContextFactory;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesManagedProperty;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesRenderer;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesSystemEventListener;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesValidator;
import org.netbeans.modules.web.jsf.api.facesmodel.Facet;
import org.netbeans.modules.web.jsf.api.facesmodel.Factory;
import org.netbeans.modules.web.jsf.api.facesmodel.Icon;
import org.netbeans.modules.web.jsf.api.facesmodel.If;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor;
import org.netbeans.modules.web.jsf.api.facesmodel.Lifecycle;
import org.netbeans.modules.web.jsf.api.facesmodel.LifecycleFactory;
import org.netbeans.modules.web.jsf.api.facesmodel.ListEntries;
import org.netbeans.modules.web.jsf.api.facesmodel.LocaleConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean;
import org.netbeans.modules.web.jsf.api.facesmodel.MapEntries;
import org.netbeans.modules.web.jsf.api.facesmodel.MessageBundle;
import org.netbeans.modules.web.jsf.api.facesmodel.Name;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationCase;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationHandler;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationRule;
import org.netbeans.modules.web.jsf.api.facesmodel.Ordering;
import org.netbeans.modules.web.jsf.api.facesmodel.OrderingElement;
import org.netbeans.modules.web.jsf.api.facesmodel.Others;
import org.netbeans.modules.web.jsf.api.facesmodel.PartialTraversal;
import org.netbeans.modules.web.jsf.api.facesmodel.PartialViewContextFactory;
import org.netbeans.modules.web.jsf.api.facesmodel.PhaseListener;
import org.netbeans.modules.web.jsf.api.facesmodel.Property;
import org.netbeans.modules.web.jsf.api.facesmodel.PropertyContainer;
import org.netbeans.modules.web.jsf.api.facesmodel.PropertyResolver;
import org.netbeans.modules.web.jsf.api.facesmodel.Redirect;
import org.netbeans.modules.web.jsf.api.facesmodel.ReferencedBean;
import org.netbeans.modules.web.jsf.api.facesmodel.RenderKit;
import org.netbeans.modules.web.jsf.api.facesmodel.RenderKitFactory;
import org.netbeans.modules.web.jsf.api.facesmodel.ResourceBundle;
import org.netbeans.modules.web.jsf.api.facesmodel.ResourceHandler;
import org.netbeans.modules.web.jsf.api.facesmodel.StateManager;
import org.netbeans.modules.web.jsf.api.facesmodel.SupportedLocale;
import org.netbeans.modules.web.jsf.api.facesmodel.TagHandlerDelegateFactory;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesValidatorId;
import org.netbeans.modules.web.jsf.api.facesmodel.FlowFinalizer;
import org.netbeans.modules.web.jsf.api.facesmodel.FlashFactory;
import org.netbeans.modules.web.jsf.api.facesmodel.FlowDefinition;
import org.netbeans.modules.web.jsf.api.facesmodel.FlowCall;
import org.netbeans.modules.web.jsf.api.facesmodel.FlowCallFacesFlowReference;
import org.netbeans.modules.web.jsf.api.facesmodel.FlowCallInOutParameter;
import org.netbeans.modules.web.jsf.api.facesmodel.FlowCallOutboundParameter;
import org.netbeans.modules.web.jsf.api.facesmodel.FlowReturn;
import org.netbeans.modules.web.jsf.api.facesmodel.FlowView;
import org.netbeans.modules.web.jsf.api.facesmodel.FlowDocumentId;
import org.netbeans.modules.web.jsf.api.facesmodel.FlowId;
import org.netbeans.modules.web.jsf.api.facesmodel.FlowInitializer;
import org.netbeans.modules.web.jsf.api.facesmodel.ProtectedViews;
import org.netbeans.modules.web.jsf.api.facesmodel.ResourceLibraryContracts;
import org.netbeans.modules.web.jsf.api.facesmodel.FlowStartNode;
import org.netbeans.modules.web.jsf.api.facesmodel.UrlPattern;
import org.netbeans.modules.web.jsf.api.facesmodel.Value;
import org.netbeans.modules.web.jsf.api.facesmodel.VariableResolver;
import org.netbeans.modules.web.jsf.api.facesmodel.ViewDeclarationLanguageFactory;
import org.netbeans.modules.web.jsf.api.facesmodel.ViewHandler;
import org.netbeans.modules.web.jsf.api.facesmodel.ViewParam;
import org.netbeans.modules.web.jsf.api.facesmodel.VisitContextFactory;
import org.netbeans.modules.xml.xam.ComponentUpdater;

/**
 *
 * @author Petr Pisl, ads
 */
class SyncUpdateVisitor extends JSFConfigVisitor.Default
    implements ComponentUpdater<JSFConfigComponent>
{

    private JSFConfigComponent target;
    private Operation operation;
    private int index;

    /** Creates a new instance of SyncUpdateVisitor */
    SyncUpdateVisitor() {
    }


    public void update(JSFConfigComponent target, JSFConfigComponent child, Operation operation) {
        update(target, child, -1 , operation);
    }

    public void update(JSFConfigComponent target, JSFConfigComponent child,
            int index, Operation operation)
    {
        assert target != null;
        assert child != null;
        this.target = target;
        this.index = index;
        this.operation = operation;
        child.accept(this);
    }

    private void insert(String propertyName, JSFConfigComponent component) {
        ((JSFConfigComponentImpl)target).insertAtIndex(propertyName, component, index);
    }

    private void remove(String propertyName, JSFConfigComponent component) {
        ((JSFConfigComponentImpl)target).removeChild(propertyName, component);
    }

    @Override
    public void visit(ManagedBean component){
        if (target instanceof FacesConfig) {
            if (operation == Operation.ADD) {
                insert(FacesConfig.MANAGED_BEAN, component);
            } else {
                remove(FacesConfig.MANAGED_BEAN, component);
            }
        }
    }

    @Override
    public void visit(NavigationRule component){
        if (target instanceof FacesConfig) {
            if (operation == Operation.ADD) {
                insert(FacesConfig.NAVIGATION_RULE, component);
            } else {
                remove(FacesConfig.NAVIGATION_RULE, component);
            }
        }
    }

    @Override
    public void visit(NavigationCase component){
        if (target instanceof NavigationRule) {
            if (operation == Operation.ADD) {
                insert(NavigationRule.NAVIGATION_CASE, component);
            } else {
                remove(NavigationRule.NAVIGATION_CASE, component);
            }
        }
    }

    @Override
    public void visit(Converter component){
        if (target instanceof FacesConfig) {
            if (operation == Operation.ADD) {
                insert(FacesConfig.CONVERTER, component);
            } else {
                remove(FacesConfig.CONVERTER, component);
            }
        }
    }

    @Override
    public void visit(Application component) {
        if (target instanceof FacesConfig) {
            if (operation == Operation.ADD) {
                insert(FacesConfig.APPLICATION, component);
            } else {
                remove(FacesConfig.APPLICATION, component);
            }
        }
    }

    @Override
    public void visit(ViewHandler component) {
        if (target instanceof Application) {
            if (operation == Operation.ADD) {
                insert(Application.VIEW_HANDLER, component);
            } else {
                remove(Application.VIEW_HANDLER, component);
            }
        }
    }

    @Override
    public void visit(LocaleConfig component) {
        if (target instanceof Application) {
            if (operation == Operation.ADD) {
                insert(Application.LOCALE_CONFIG, component);
            } else {
                remove(Application.LOCALE_CONFIG, component);
            }
        }
    }

    @Override
    public void visit(DefaultLocale component) {
        if (target instanceof LocaleConfig) {
            if (operation == Operation.ADD) {
                insert(LocaleConfig.DEFAULT_LOCALE, component);
            } else {
                remove(LocaleConfig.DEFAULT_LOCALE, component);
            }
        }
    }

    @Override
    public void visit(SupportedLocale component) {
        if (target instanceof LocaleConfig) {
            if (operation == Operation.ADD) {
                insert(LocaleConfig.SUPPORTED_LOCALE, component);
            } else {
                remove(LocaleConfig.SUPPORTED_LOCALE, component);
            }
        }
    }

    @Override
    public void visit(ResourceBundle component) {
        if (target instanceof Application) {
            if (operation == Operation.ADD) {
                insert(Application.RESOURCE_BUNDLE, component);
            } else {
                remove(Application.RESOURCE_BUNDLE, component);
            }
        }
    }

    @Override
    public void visit(ActionListener component) {
        if (target instanceof Application) {
            if (operation == Operation.ADD) {
                insert(Application.ACTION_LISTENER, component);
            } else {
                remove(Application.ACTION_LISTENER, component);
            }
        }
    }

    @Override
    public void visit( DefaultRenderKitId id ) {
        if (target instanceof Application) {
            if (operation == Operation.ADD) {
                insert(Application.DEFAULT_RENDER_KIT_ID, id);
            } else {
                remove(Application.DEFAULT_RENDER_KIT_ID, id);
            }
        }
    }

    @Override
    public void visit( MessageBundle bundle ) {
        if (target instanceof Application) {
            if (operation == Operation.ADD) {
                insert(Application.MESSAGE_BUNDLE, bundle);
            } else {
                remove(Application.MESSAGE_BUNDLE, bundle);
            }
        }
    }

    @Override
    public void visit( NavigationHandler handler ) {
        if (target instanceof Application) {
            if (operation == Operation.ADD) {
                insert(Application.NAVIGATION_HANDLER, handler);
            } else {
                remove(Application.NAVIGATION_HANDLER, handler);
            }
        }
    }

    @Override
    public void visit( PartialTraversal traversal ) {
        if (target instanceof Application) {
            if (operation == Operation.ADD) {
                insert(Application.PARTIAL_TRAVERSAL, traversal);
            } else {
                remove(Application.PARTIAL_TRAVERSAL, traversal);
            }
        }
    }

    @Override
    public void visit( StateManager manager ) {
        if (target instanceof Application) {
            if (operation == Operation.ADD) {
                insert(Application.STATE_MANAGER, manager);
            } else {
                remove(Application.STATE_MANAGER, manager);
            }
        }
    }

    @Override
    public void visit( ElResolver resolver ) {
        if (target instanceof Application) {
            if (operation == Operation.ADD) {
                insert(Application.EL_RESOLVER, resolver);
            } else {
                remove(Application.EL_RESOLVER , resolver );
            }
        }
    }

    @Override
    public void visit( PropertyResolver resolver ) {
        if (target instanceof Application) {
            if (operation == Operation.ADD) {
                insert(Application.PROPERTY_RESOLVER, resolver);
            } else {
                remove(Application.PROPERTY_RESOLVER , resolver );
            }
        }
    }

    @Override
    public void visit( VariableResolver resolver ) {
        if (target instanceof Application) {
            if (operation == Operation.ADD) {
                insert(Application.VARIABLE_RESOLVER, resolver);
            } else {
                remove(Application.VARIABLE_RESOLVER , resolver );
            }
        }
    }

    @Override
    public void visit( ResourceHandler handler ) {
        if (target instanceof Application) {
            if (operation == Operation.ADD) {
                insert(Application.RESOURCE_HANDLER, handler);
            } else {
                remove(Application.RESOURCE_HANDLER , handler );
            }
        }
    }

    @Override
    public void visit( FacesSystemEventListener listener ) {
        if (target instanceof Application) {
            if (operation == Operation.ADD) {
                insert(Application.SYSTEM_EVENT_LISTENER, listener);
            } else {
                remove(Application.SYSTEM_EVENT_LISTENER , listener );
            }
        }
    }

    @Override
    public void visit( DefaultValidators validators ) {
        if (target instanceof Application) {
            if (operation == Operation.ADD) {
                insert(Application.DEFAULT_VALIDATORS, validators);
            } else {
                remove(Application.DEFAULT_VALIDATORS , validators );
            }
        }
    }

    @Override
    public void visit( Ordering ordering ) {
        if (target instanceof FacesConfig) {
            if (operation == Operation.ADD) {
                insert(FacesConfig.ORDERING, ordering);
            } else {
                remove(FacesConfig.ORDERING , ordering );
            }
        }
    }

    @Override
    public void visit( After after ) {
        if (target instanceof Ordering) {
            if (operation == Operation.ADD) {
                insert(Ordering.AFTER, after);
            } else {
                remove(Ordering.AFTER , after );
            }
        }
    }

    @Override
    public void visit( Before before ) {
        if (target instanceof Ordering) {
            if (operation == Operation.ADD) {
                insert(Ordering.BEFORE, before);
            } else {
                remove(Ordering.BEFORE , before );
            }
        }
    }

    @Override
    public void visit( Name name ) {
        if (target instanceof OrderingElement || target instanceof FacesConfig
                || target instanceof AbsoluteOrdering ) {
            if (operation == Operation.ADD) {
                insert( OrderingElement.NAME, name);
            } else {
                remove(OrderingElement.NAME, name );
            }
        } else if (target instanceof FlowCallOutboundParameter) {
            if (operation == Operation.ADD) {
                insert(FlowCallOutboundParameter.NAME, name);
            } else {
                remove(FlowCallOutboundParameter.NAME, name);
            }
        }
    }

    @Override
    public void visit( Others others ) {
        if (target instanceof OrderingElement || target instanceof AbsoluteOrdering ) {
            if (operation == Operation.ADD) {
                insert( OrderingElement.OTHERS, others);
            } else {
                remove( OrderingElement.OTHERS, others );
            }
        }
    }

    @Override
    public void visit( AbsoluteOrdering ordering ) {
        if (target instanceof FacesConfig  ) {
            if (operation == Operation.ADD) {
                insert( FacesConfig.ABSOLUTE_ORDERING, ordering);
            } else {
                remove( FacesConfig.ABSOLUTE_ORDERING, ordering );
            }
        }
    }

    @Override
    public void visit( FacesValidatorId id ) {
        if (target instanceof DefaultValidators  ) {
            if (operation == Operation.ADD) {
                insert( DefaultValidators.VALIDATOR_ID, id);
            } else {
                remove( DefaultValidators.VALIDATOR_ID, id );
            }
        }
    }

    @Override
    public void visit( Factory factory ) {
        if (target instanceof FacesConfig  ) {
            if (operation == Operation.ADD) {
                insert( FacesConfig.FACTORY, factory);
            } else {
                remove( FacesConfig.FACTORY, factory );
            }
        }
    }

    @Override
    public void visit( ApplicationFactory factory ) {
        if (target instanceof Factory  ) {
            if (operation == Operation.ADD) {
                insert( Factory.APPLICATION_FACTORY, factory);
            } else {
                remove( Factory.APPLICATION_FACTORY, factory );
            }
        }
    }

    @Override
    public void visit( ExceptionHandlerFactory factory ) {
        if (target instanceof Factory  ) {
            if (operation == Operation.ADD) {
                insert( Factory.EXCEPTION_HANDLER_FACTORY, factory);
            } else {
                remove( Factory.EXCEPTION_HANDLER_FACTORY, factory );
            }
        }
    }

    @Override
    public void visit( ExternalContextFactory factory ) {
        if (target instanceof Factory  ) {
            if (operation == Operation.ADD) {
                insert( Factory.EXTERNAL_CONTEXT_FACTORY, factory);
            } else {
                remove( Factory.EXTERNAL_CONTEXT_FACTORY, factory );
            }
        }
    }

    @Override
    public void visit( FacesContextFactory factory ) {
        if (target instanceof Factory  ) {
            if (operation == Operation.ADD) {
                insert( Factory.FACES_CONTEXT_FACTORY, factory);
            } else {
                remove( Factory.FACES_CONTEXT_FACTORY, factory );
            }
        }
    }

    @Override
    public void visit(FaceletCacheFactory factory) {
        if (target instanceof Factory) {
            if (operation == Operation.ADD) {
                insert(Factory.FACELET_CACHE_FACTORY, factory);
            } else {
                remove(Factory.FACELET_CACHE_FACTORY, factory);
            }
        }
    }

    @Override
    public void visit( PartialViewContextFactory factory ) {
        if (target instanceof Factory  ) {
            if (operation == Operation.ADD) {
                insert( Factory.PARTIAL_VIEW_CONTEXT_FACTORY, factory);
            } else {
                remove( Factory.PARTIAL_VIEW_CONTEXT_FACTORY, factory );
            }
        }
    }

    @Override
    public void visit( LifecycleFactory factory ) {
        if (target instanceof Factory  ) {
            if (operation == Operation.ADD) {
                insert( Factory.LIFECYCLE_FACTORY, factory);
            } else {
                remove( Factory.LIFECYCLE_FACTORY, factory );
            }
        }
    }

    @Override
    public void visit( ViewDeclarationLanguageFactory factory ) {
        if (target instanceof Factory  ) {
            if (operation == Operation.ADD) {
                insert( Factory.VIEW_DECLARATION_LANGUAGE_FACTORY, factory);
            } else {
                remove( Factory.VIEW_DECLARATION_LANGUAGE_FACTORY, factory );
            }
        }
    }

    @Override
    public void visit( TagHandlerDelegateFactory factory ) {
        if (target instanceof Factory  ) {
            if (operation == Operation.ADD) {
                insert( Factory.TAG_HANDLER_DELEGATE_FACTORY, factory);
            } else {
                remove( Factory.TAG_HANDLER_DELEGATE_FACTORY, factory );
            }
        }
    }

    @Override
    public void visit( RenderKitFactory factory ) {
        if (target instanceof Factory  ) {
            if (operation == Operation.ADD) {
                insert( Factory.RENDER_KIT_FACTORY, factory);
            } else {
                remove( Factory.RENDER_KIT_FACTORY, factory );
            }
        }
    }

    @Override
    public void visit( VisitContextFactory factory ) {
        if (target instanceof Factory  ) {
            if (operation == Operation.ADD) {
                insert( Factory.VISIT_CONTEXT_FACTORY, factory);
            } else {
                remove( Factory.VISIT_CONTEXT_FACTORY, factory );
            }
        }
    }

    @Override
    public void visit( FacesComponent component ) {
        if (target instanceof FacesConfig  ) {
            if (operation == Operation.ADD) {
                insert( FacesConfig.COMPONENT, component);
            } else {
                remove( FacesConfig.COMPONENT, component );
            }
        }
    }

    @Override
    public void visit( Facet facet ) {
        if (target instanceof FacesComponent  || target instanceof FacesRenderer) {
            if (operation == Operation.ADD) {
                insert( FacesComponent.FACET, facet);
            } else {
                remove( FacesComponent.FACET, facet );
            }
        }
    }

    @Override
    public void visit( Property property ) {
        if (target instanceof PropertyContainer  ) {
            if (operation == Operation.ADD) {
                insert( PropertyContainer.PROPERTY, property);
            } else {
                remove( PropertyContainer.PROPERTY, property );
            }
        }
    }

    @Override
    public void visit( ConfigAttribute attribute ) {
        if (target instanceof AttributeContainer  ) {
            if (operation == Operation.ADD) {
                insert( AttributeContainer.ATTRIBUTE, attribute);
            } else {
                remove( AttributeContainer.ATTRIBUTE, attribute );
            }
        }
    }

    @Override
    public void visit( Description description ) {
        if (target instanceof DescriptionGroup  ) {
            if (operation == Operation.ADD) {
                insert( DescriptionGroup.DESCRIPTION, description);
            } else {
                remove( DescriptionGroup.DESCRIPTION, description );
            }
        }
    }

    @Override
    public void visit( Icon icon ) {
        if (target instanceof DescriptionGroup  ) {
            if (operation == Operation.ADD) {
                insert( DescriptionGroup.ICON, icon);
            } else {
                remove( DescriptionGroup.ICON, icon );
            }
        }
    }

    @Override
    public void visit(  DisplayName name ) {
        if (target instanceof DescriptionGroup  ) {
            if (operation == Operation.ADD) {
                insert( DescriptionGroup.DISPLAY_NAME, name);
            } else {
                remove( DescriptionGroup.DISPLAY_NAME, name );
            }
        }
    }

    @Override
    public void visit(  FacesManagedProperty property ) {
        if (target instanceof ManagedBean  ) {
            if (operation == Operation.ADD) {
                insert( ManagedBean.MANAGED_PROPERTY, property);
            } else {
                remove( ManagedBean.MANAGED_PROPERTY, property );
            }
        }
    }

    @Override
    public void visit(  ListEntries entries ) {
        if (target instanceof ManagedBean  ) {
            if (operation == Operation.ADD) {
                insert( ManagedBean.LIST_ENTRIES, entries);
            } else {
                remove( ManagedBean.LIST_ENTRIES, entries );
            }
        }
    }

    @Override
    public void visit(  MapEntries entries ) {
        if (target instanceof ManagedBean  ) {
            if (operation == Operation.ADD) {
                insert( ManagedBean.MAP_ENTRIES, entries);
            } else {
                remove( ManagedBean.MAP_ENTRIES, entries );
            }
        }
    }

    @Override
    public void visit(  If iff ) {
        if (target instanceof NavigationCase  ) {
            if (operation == Operation.ADD) {
                insert( NavigationCase.IF, iff);
            } else {
                remove( NavigationCase.IF, iff );
            }
        }
    }

    @Override
    public void visit(  Redirect redirect ) {
        if (target instanceof NavigationCase  ) {
            if (operation == Operation.ADD) {
                insert( NavigationCase.REDIRECT, redirect);
            } else {
                remove( NavigationCase.REDIRECT, redirect );
            }
        }
    }

    @Override
    public void visit(  ViewParam viewParam ) {
        if (target instanceof Redirect  ) {
            if (operation == Operation.ADD) {
                insert( Redirect.VIEW_PARAM, viewParam);
            } else {
                remove( Redirect.VIEW_PARAM, viewParam );
            }
        }
    }

    @Override
    public void visit(  ReferencedBean referencedBean ) {
        if (target instanceof FacesConfig  ) {
            if (operation == Operation.ADD) {
                insert( FacesConfig.REFERENCED_BEAN, referencedBean);
            } else {
                remove( FacesConfig.REFERENCED_BEAN, referencedBean );
            }
        }
    }

    @Override
    public void visit(  RenderKit renderKit ) {
        if (target instanceof FacesConfig  ) {
            if (operation == Operation.ADD) {
                insert( FacesConfig.RENDER_KIT, renderKit);
            } else {
                remove( FacesConfig.RENDER_KIT, renderKit );
            }
        }
    }

    @Override
    public void visit(  FacesRenderer renderer ) {
        if (target instanceof RenderKit  ) {
            if (operation == Operation.ADD) {
                insert( RenderKit.RENDERER, renderer);
            } else {
                remove( RenderKit.RENDERER, renderer );
            }
        }
    }

    @Override
    public void visit(  FacesClientBehaviorRenderer renderer ) {
        if (target instanceof RenderKit  ) {
            if (operation == Operation.ADD) {
                insert( RenderKit.CLIENT_BEHAVIOR_RENDERER, renderer);
            } else {
                remove( RenderKit.CLIENT_BEHAVIOR_RENDERER, renderer );
            }
        }
    }

    @Override
    public void visit(  Lifecycle lifecycle ) {
        if (target instanceof FacesConfig  ) {
            if (operation == Operation.ADD) {
                insert( FacesConfig.LIFECYCLE, lifecycle);
            } else {
                remove( FacesConfig.LIFECYCLE, lifecycle );
            }
        }
    }

    @Override
    public void visit(  PhaseListener listener ) {
        if (target instanceof Lifecycle  ) {
            if (operation == Operation.ADD) {
                insert( Lifecycle.PHASE_LISTENER, listener);
            } else {
                remove( Lifecycle.PHASE_LISTENER, listener );
            }
        }
    }

    @Override
    public void visit(  FacesValidator validator ) {
        if (target instanceof FacesConfig  ) {
            if (operation == Operation.ADD) {
                insert( FacesConfig.VALIDATOR, validator);
            } else {
                remove( FacesConfig.VALIDATOR, validator );
            }
        }
    }

    @Override
    public void visit(  FacesBehavior behavior ) {
        if (target instanceof FacesConfig  ) {
            if (operation == Operation.ADD) {
                insert( FacesConfig.BEHAVIOR, behavior);
            } else {
                remove( FacesConfig.BEHAVIOR, behavior );
            }
        }
    }

    @Override
    public void visit(ProtectedViews views) {
        if (target instanceof FacesConfig) {
            if (operation == Operation.ADD) {
                insert(FacesConfig.PROTECTED_VIEWS, views);
            } else {
                remove(FacesConfig.PROTECTED_VIEWS, views);
            }
        }
    }

    @Override
    public void visit(UrlPattern pattern) {
        if (target instanceof ProtectedViews) {
            if (operation == Operation.ADD) {
                insert(ProtectedViews.PROTECTED_VIEWS, pattern);
            } else {
                remove(ProtectedViews.PROTECTED_VIEWS, pattern);
            }
        } else if (target instanceof ContractMapping) {
            if (operation == Operation.ADD) {
                insert(ContractMapping.URL_PATTERN, pattern);
            } else {
                remove(ContractMapping.URL_PATTERN, pattern);
            }
        }
    }

    @Override
    public void visit(ResourceLibraryContracts contracts) {
        if (target instanceof Application) {
            if (operation == Operation.ADD) {
                insert(Application.RESOURCE_LIBRARY_CONTRACTS, contracts);
            } else {
                remove(Application.RESOURCE_LIBRARY_CONTRACTS, contracts);
            }
        }
    }

    @Override
    public void visit(ContractMapping mapping) {
        if (target instanceof ResourceLibraryContracts) {
            if (operation == Operation.ADD) {
                insert(ResourceLibraryContracts.CONTRACT_MAPPING, mapping);
            } else {
                remove(ResourceLibraryContracts.CONTRACT_MAPPING, mapping);
            }
        }
    }

    @Override
    public void visit(FlashFactory factory) {
        if (target instanceof Factory) {
            if (operation == Operation.ADD) {
                insert(Factory.FLASH_FACTORY, factory);
            } else {
                remove(Factory.FLASH_FACTORY, factory);
            }
        }
    }

    @Override
    public void visit(FlowDefinition definition) {
        if (target instanceof FacesConfig) {
            if (operation == Operation.ADD) {
                insert(FacesConfig.FLOW_DEFINITION, definition);
            } else {
                remove(FacesConfig.FLOW_DEFINITION, definition);
            }
        }
    }

    @Override
    public void visit(FlowStartNode definition) {
        if (target instanceof FlowDefinition) {
            if (operation == Operation.ADD) {
                insert(FlowDefinition.START_NODE, definition);
            } else {
                remove(FlowDefinition.START_NODE, definition);
            }
        }
    }

    @Override
    public void visit(FlowInitializer definition) {
        if (target instanceof FlowDefinition) {
            if (operation == Operation.ADD) {
                insert(FlowDefinition.INITIALIZER, definition);
            } else {
                remove(FlowDefinition.INITIALIZER, definition);
            }
        }
    }

    @Override
    public void visit(FlowFinalizer definition) {
        if (target instanceof FlowDefinition) {
            if (operation == Operation.ADD) {
                insert(FlowDefinition.FINALIZER, definition);
            } else {
                remove(FlowDefinition.FINALIZER, definition);
            }
        }
    }

    @Override
    public void visit(FlowView view) {
        if (target instanceof FlowDefinition) {
            if (operation == Operation.ADD) {
                insert(FlowDefinition.VIEW, view);
            } else {
                remove(FlowDefinition.VIEW, view);
            }
        }
    }

    @Override
    public void visit(FlowReturn flowReturn) {
        if (target instanceof FlowDefinition) {
            if (operation == Operation.ADD) {
                insert(FlowDefinition.FLOW_RETURN, flowReturn);
            } else {
                remove(FlowDefinition.FLOW_RETURN, flowReturn);
            }
        }
    }

    @Override
    public void visit(FlowCall flowCall) {
        if (target instanceof FlowDefinition) {
            if (operation == Operation.ADD) {
                insert(FlowDefinition.FLOW_CALL, flowCall);
            } else {
                remove(FlowDefinition.FLOW_CALL, flowCall);
            }
        }
    }

    @Override
    public void visit(FlowCallFacesFlowReference reference) {
        if (target instanceof FlowCall) {
            if (operation == Operation.ADD) {
                insert(FlowCall.FLOW_REFERENCE, reference);
            } else {
                remove(FlowCall.FLOW_REFERENCE, reference);
            }
        }
    }

    @Override
    public void visit(FlowId flowId) {
        if (target instanceof FlowCallFacesFlowReference) {
            if (operation == Operation.ADD) {
                insert(FlowCallFacesFlowReference.FLOW_ID, flowId);
            } else {
                remove(FlowCallFacesFlowReference.FLOW_ID, flowId);
            }
        }
    }

    @Override
    public void visit(FlowDocumentId flowDocumentId) {
        if (target instanceof FlowCallFacesFlowReference) {
            if (operation == Operation.ADD) {
                insert(FlowCallFacesFlowReference.FLOW_DOCUMENT_ID, flowDocumentId);
            } else {
                remove(FlowCallFacesFlowReference.FLOW_DOCUMENT_ID, flowDocumentId);
            }
        }
    }

    @Override
    public void visit(FlowCallInOutParameter outboundParameter) {
        if (target instanceof FlowCall) {
            if (operation == Operation.ADD) {
                insert(FlowCall.OUTBOUND_PARAMETER, outboundParameter);
            } else {
                remove(FlowCall.OUTBOUND_PARAMETER, outboundParameter);
            }
        } else if (target instanceof FlowDefinition) {
            if (operation == Operation.ADD) {
                insert(FlowDefinition.INBOUND_PARAMETER, outboundParameter);
            } else {
                remove(FlowDefinition.INBOUND_PARAMETER, outboundParameter);
            }
        }
    }

    @Override
    public void visit(Value value) {
        if (target instanceof FlowCallOutboundParameter) {
            if (operation == Operation.ADD) {
                insert(FlowCallOutboundParameter.VALUE, value);
            } else {
                remove(FlowCallOutboundParameter.VALUE, value);
            }
        }
    }

}
