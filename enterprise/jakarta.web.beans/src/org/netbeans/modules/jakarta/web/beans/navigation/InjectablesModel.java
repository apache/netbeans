/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.jakarta.web.beans.navigation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.jakarta.web.beans.api.model.DependencyInjectionResult;
import org.netbeans.modules.jakarta.web.beans.api.model.WebBeansModel;
import org.openide.filesystems.FileObject;

/**
 * @author ads
 */
public final class InjectablesModel extends DefaultTreeModel 
    implements JavaHierarchyModel
{
    
    private static final long serialVersionUID = -6845959436250662000L;

    private static final Logger LOG = Logger.getLogger(
            InjectablesModel.class.getName());
    
    static Element[] EMPTY_ELEMENTS_ARRAY = new Element[0];

    public InjectablesModel(DependencyInjectionResult result, 
            CompilationController controller ,MetadataModel<WebBeansModel> model ) 
    {
        super(null);
        
        myModel = model;
        if ( result.getKind() == DependencyInjectionResult.ResultKind.DEFINITION_ERROR || 
                !( result instanceof DependencyInjectionResult.ApplicableResult))
        {
            myTypeHandles= Collections.emptyList();
            myProductionHandles = Collections.emptyMap();
            return;
        }
        
        DependencyInjectionResult.ApplicableResult applicableResult = 
            (DependencyInjectionResult.ApplicableResult) result;
        Set<TypeElement> typeElements = applicableResult.getTypeElements();
        
        myProductionHandles = new HashMap<ElementHandle<?>, 
            ElementHandle<TypeElement>>();
        
        myDisabledBeans = new HashSet<ElementHandle<?>>();
        Set<Element> disabled = new HashSet<Element>();

        myTypeHandles = new ArrayList<ElementHandle<TypeElement>>(typeElements.size());
        for (TypeElement el : typeElements) {
            ElementHandle<TypeElement> handle = ElementHandle.create(el);
            myTypeHandles.add(handle);
            if ( applicableResult.isDisabled(el)){
                myDisabledBeans.add( handle );
                disabled.add( el );
            }
        }
        
        Set<Element> productions = applicableResult.getProductions();
        Map<Element,TypeElement> productionMap = new HashMap<Element, TypeElement>();
        for (Element production : productions) {
            ElementHandle<Element> handleKey = ElementHandle.create( production );
            TypeElement clazz = controller.getElementUtilities().
                enclosingTypeElement(production);
            myProductionHandles.put(handleKey,  ElementHandle.create( clazz ) );
            productionMap.put( production,  clazz );
            if ( applicableResult.isDisabled(production)){
                myDisabledBeans.add( handleKey );
                disabled.add( production );
            }
        }

        update( typeElements, productionMap, disabled , controller);
    }
    
    @Override
    public void update() {
        update( myTypeHandles , myProductionHandles );
    }

    @Override
    public void fireTreeNodesChanged() {
        super.fireTreeNodesChanged(this, getPathToRoot((TreeNode)getRoot()), 
                null, null);
    }
    
    private void update( final List<ElementHandle<TypeElement>> typeHandles ,
            final Map<ElementHandle<?>,ElementHandle<TypeElement>> 
            productions ) 
    {
        try {
            getModel().runReadAction(
                    new MetadataModelAction<WebBeansModel, Void>() {

                        @Override
                        public Void run( WebBeansModel model ) {
                            Set<Element> disabled = new HashSet<Element>();
                            List<TypeElement> typesList = fillTypes(typeHandles, 
                                    model, disabled);
                            
                            Map<Element, TypeElement> productionsMap = 
                                fillProductions( productions, model , disabled);

                            update(typesList, productionsMap , disabled , model
                                    .getCompilationController());
                            return null;
                        }
                    });

            return;
        }
        catch (MetadataModelException e) {
            LOG.log(Level.WARNING, e.getMessage(), e);
        }
        catch (IOException e) {
            LOG.log(Level.WARNING, e.getMessage(), e);
        }
    }
    
    private Map<Element, TypeElement> fillProductions(
            Map<ElementHandle<?>, ElementHandle<TypeElement>> productions,
            WebBeansModel model, Set<Element> disabled )
    {
        Map<Element, TypeElement> result;
        if ( productions == null || productions.size() == 0){
            result = Collections.emptyMap();
        }
        else {
            result = new HashMap<Element, TypeElement>();
            for(Entry<ElementHandle<?>,ElementHandle<TypeElement>>
                entry : productions.entrySet() )
            {
                ElementHandle<?> handle = entry.getKey();
                Element element = handle.resolve(model.getCompilationController());
                if (element != null) {
                    if (myDisabledBeans.contains(handle)) {
                        disabled.add(element);
                    }
                    result.put(element, entry.getValue().resolve( 
                            model.getCompilationController()));
                }
                else {
                    LOG.warning(handle.toString()
                            + " cannot be resolved using: " // NOI18N
                            + model.getCompilationController()
                                    .getClasspathInfo());
                }
            }
        }
        return result;
    }

    private  List<TypeElement> fillTypes(final List<ElementHandle<TypeElement>> 
            typeHandles, WebBeansModel model, Set<Element> disabled )
    {
        List<TypeElement> typesList;
        if (typeHandles != null && typeHandles.size() != 0)
        {
            typesList = new ArrayList<TypeElement>(
                    typeHandles.size());

            for (ElementHandle<TypeElement> 
                typeHandle : typeHandles)
            {
                TypeElement element = typeHandle
                        .resolve(model
                                .getCompilationController());
                if (element != null) {
                    typesList.add(element);
                    if ( myDisabledBeans.contains( typeHandle)){
                        disabled.add( element);
                    }
                }
                else {
                    LOG.warning(typeHandle.toString()
                        + " cannot be resolved using: "     // NOI18N
                         + model.getCompilationController()
                                .getClasspathInfo());
                }
            }
        }
        else {
            typesList = Collections.emptyList();
        }
        return typesList;
    }

    private void update(final Collection<TypeElement> typeElements, 
            final Map<Element, TypeElement> productions, final 
            Set<Element> disabledBeans,
            CompilationController controller) 
    {
        if (typeElements.size()==0 && productions.size() == 0 ) {
            return;
        }

        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        Map<Element, InjectableTreeNode<? extends Element>> elementMap= 
            new LinkedHashMap<Element, InjectableTreeNode<? extends Element>>();

        for (TypeElement element : typeElements) {
            FileObject fileObject = SourceUtils.getFile(ElementHandle
                    .create(element), controller.getClasspathInfo());
            // Type declaration
            TypeTreeNode node = new TypeTreeNode(fileObject,
                    element, disabledBeans.contains( element),
                    controller);
            insertTreeNode(elementMap, element, node, root, 
                    disabledBeans.contains(element), controller);
        }
        
        for (Entry<Element,TypeElement> entry : productions.entrySet()){
            Element element = entry.getKey();
            FileObject fileObject = SourceUtils.getFile(ElementHandle
                    .create(element), controller.getClasspathInfo());
            if ( element instanceof ExecutableElement ){
                // Method definition
                MethodTreeNode node = new MethodTreeNode(fileObject, 
                        (ExecutableElement)element, 
                        (DeclaredType)entry.getValue().asType(),
                        disabledBeans.contains( element), controller);
                insertTreeNode( elementMap , (ExecutableElement)element , 
                        node , root ,  controller);
            }
            else  {
                // Should be produces field.
                InjectableTreeNode<Element> node = 
                    new InjectableTreeNode<Element>(fileObject, element,  
                            (DeclaredType)entry.getValue().asType(), 
                            disabledBeans.contains(element),controller);
                insertTreeNode( elementMap , node , root );
            }
        }

        setRoot(root);
    }
    
    private void insertTreeNode( Map<Element, 
            InjectableTreeNode<? extends Element>> elementMap,TypeElement element , 
            TypeTreeNode node, DefaultMutableTreeNode root , boolean isDisabled,
            CompilationController controller)
    {
        TypeTreeNode parent = null;
        
        for( Entry<Element, InjectableTreeNode<? extends Element>> entry : 
                elementMap.entrySet())
        {
            Element key = entry.getKey();
            if ( !( key instanceof TypeElement )){
                continue;
            }
            TypeTreeNode injectableNode = (TypeTreeNode)entry.getValue();
            TypeElement typeElement = (TypeElement)key;
            if ( controller.getTypes().isAssignable( element.asType(), 
                    typeElement.asType()))
            {
                if ( parent == null ){
                    parent = injectableNode;
                }
                else if ( parent.isAssignableFrom(typeElement, controller)){
                    parent = injectableNode;
                }
            }
        }
        
        DefaultMutableTreeNode parentNode = parent;
        
        if ( parentNode == null ){
            parentNode = root;
        }
        Enumeration<?> children = parentNode.children();
        List<TypeTreeNode> movedChildren = new LinkedList<TypeTreeNode>();
        while (children.hasMoreElements()) {
            TypeTreeNode childNode = (TypeTreeNode) children.nextElement();
            if (childNode.isAssignable(element, controller))
            {
                movedChildren.add(childNode);
            }
        }

        for (TypeTreeNode typeTreeNode : movedChildren) {
            parentNode.remove(typeTreeNode);
            node.add(typeTreeNode);
        }
        parentNode.add(node);
        elementMap.put(element, node);
    }
    
    static void insertTreeNode( Map<Element, 
            InjectableTreeNode<? extends Element>> elementMap,
            ExecutableElement element , MethodTreeNode node, 
            DefaultMutableTreeNode root , CompilationController controller)
    {
        MethodTreeNode parent = null;
        
        List<ExecutableElement> overriddenMethods = new ArrayList<ExecutableElement>();
        ExecutableElement overriddenMethod = element;
        while ( true ){
            overriddenMethod = 
                controller.getElementUtilities().getOverriddenMethod(overriddenMethod);
            if ( overriddenMethod == null ){
                break;
            }
            overriddenMethods.add( overriddenMethod );
        }
        if ( overriddenMethods.size() > 0  )
        {
            for (Entry<Element, InjectableTreeNode<? extends Element>> entry : 
                elementMap.entrySet())
            {
                Element key = entry.getKey();
                if (!(key instanceof ExecutableElement)) {
                    continue;
                }
                MethodTreeNode injectableNode = (MethodTreeNode) entry
                        .getValue();
                ExecutableElement method = (ExecutableElement) key;

                int index = overriddenMethods.indexOf( method);
                if ( index != -1 ) {
                    if (parent == null) {
                        parent = injectableNode;
                    }
                    else if (parent.isOverridden( index, overriddenMethods, 
                            controller)) 
                    {
                        parent = injectableNode;
                    }
                }
            }
        }
        
        DefaultMutableTreeNode parentNode = parent;
        
        if ( parentNode == null ){
            parentNode = root;
        }
        Enumeration<?> children = parentNode.children();
        List<MethodTreeNode> movedChildren = new LinkedList<MethodTreeNode>();
        while (children.hasMoreElements()) {
            Object child = children.nextElement();
            if (child instanceof MethodTreeNode) {
                MethodTreeNode childNode = (MethodTreeNode)child;
                if (childNode.overridesMethod(element, controller)) {
                    movedChildren.add(childNode);
                }
            }
        }

        for (MethodTreeNode methodNode : movedChildren) {
            parentNode.remove(methodNode);
            node.add(methodNode);
        }
        parentNode.add(node);
        elementMap.put(element, node);
    }
    
    private void insertTreeNode( Map<Element, 
            InjectableTreeNode<? extends Element>> elementMap,
            InjectableTreeNode<Element> node, DefaultMutableTreeNode root )
    {
        root.add( node );
    }

    private MetadataModel<WebBeansModel> getModel(){
        return myModel;
    }
    
    private List<ElementHandle<TypeElement>> myTypeHandles;
    private Map<ElementHandle<?>,ElementHandle<TypeElement>> myProductionHandles;
    private Set<ElementHandle<?>> myDisabledBeans;
    private MetadataModel<WebBeansModel> myModel;
}
