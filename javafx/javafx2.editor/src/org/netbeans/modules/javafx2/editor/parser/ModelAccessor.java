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
package org.netbeans.modules.javafx2.editor.parser;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.modules.javafx2.editor.completion.beans.FxDefinition;
import org.netbeans.modules.javafx2.editor.completion.model.EventHandler;
import org.netbeans.modules.javafx2.editor.completion.model.FxInclude;
import org.netbeans.modules.javafx2.editor.completion.model.FxInstance;
import org.netbeans.modules.javafx2.editor.completion.model.FxNewInstance;
import org.netbeans.modules.javafx2.editor.completion.model.FxModel;
import org.netbeans.modules.javafx2.editor.completion.model.FxNode;
import org.netbeans.modules.javafx2.editor.completion.model.FxObjectBase;
import org.netbeans.modules.javafx2.editor.completion.model.FxScriptFragment;
import org.netbeans.modules.javafx2.editor.completion.model.HasContent;
import org.netbeans.modules.javafx2.editor.completion.model.HasResource;
import org.netbeans.modules.javafx2.editor.completion.model.ImportDecl;
import org.netbeans.modules.javafx2.editor.completion.model.LanguageDecl;
import org.netbeans.modules.javafx2.editor.completion.model.MapProperty;
import org.netbeans.modules.javafx2.editor.completion.model.PropertySetter;
import org.netbeans.modules.javafx2.editor.completion.model.PropertyValue;
import org.netbeans.modules.javafx2.editor.completion.model.StaticProperty;

/**
 * Provides extra access to model properties, so partial instances
 * can be built and attributed.
 *
 * @author sdedic
 */
public abstract class ModelAccessor {
    static ModelAccessor INSTANCE;
    
    static {
        try {
            Class.forName(FxModel.class.getName());
        } catch (ClassNotFoundException ex) {
             throw new IllegalStateException(ex);
        }
    }
    
    public static void setInstance(ModelAccessor i) {
        if (INSTANCE != null) {
            throw new IllegalStateException();
        }
        INSTANCE = i;
    }
    
    /**
     * Creates new model. Note that the parameters are live and
     * <b>may be changed</b> by the caller.
     * 
     * @param imports
     * @param defs
     * @return 
     */
    public abstract FxModel    newModel(URL baseURL, List<ImportDecl> imports, List<FxNewInstance> defs);
    public abstract ImportDecl createImport(String imported, boolean wildcard);
    public abstract LanguageDecl createLanguage(String lang);
    public abstract FxInclude createInclude(String included, String id);
    public abstract FxNewInstance createInstance(String sourceName, CharSequence value, boolean constant, String factory, String id);
    public abstract FxNewInstance createCustomRoot(String sourceName, String id);
    public abstract FxObjectBase createCopyReference(boolean copy, String targetName);
    public abstract PropertySetter createProperty(String name, boolean implicit);
    public abstract StaticProperty createStaticProperty(String name, String sourceName);
    public abstract MapProperty createMapProperty(String name, Map<String, CharSequence> values);
    
    public abstract EventHandler createEventHandler(String eventName);
    
    public abstract FxNode createElement(String localName);
    public abstract FxNode createErrorElement(String localName);
    public abstract FxScriptFragment createScript(String sourceRef);
    public abstract EventHandler asMethodRef(EventHandler h);
    
    public abstract void initModel(FxModel model, String controller, FxInstance rootInstance, LanguageDecl language);
    /**
     * Resolves class name for instance nodes, accessor name for property and event nodes
     * @param n
     * @param handle 
     */
    @SuppressWarnings("rawtypes")
    public abstract void resolve(FxNode n, ElementHandle nameHandle, TypeMirrorHandle typeHandle, 
            ElementHandle<TypeElement> sourceTypeHandle, FxDefinition info);
    
    public abstract void addContent(HasContent content, CharSequence additionalContent);
    
    /**
     * Adds child to the parent, based on the parent/child type. May throw
     * {@link IllegalArgumentException} if the child is not appropriate for the parent.
     * 
     * @param parent
     * @param child 
     */
    public abstract void addChild(FxNode parent, FxNode child) throws IllegalArgumentException;
    
    public abstract void resolveResource(HasResource decl, URL resolved);
    
    public abstract NodeInfo i(FxNode n);
    public abstract <T extends FxNode> T makeBroken(T n);
    
    public abstract void addDefinitions(FxModel model, Collection<FxNewInstance> definitions);
    
    public abstract void setNamedInstances(FxModel model, Map<String, FxInstance> instances);
    
    public abstract void resolveReference(FxObjectBase copyOrReference, FxInstance original);
    
    public abstract void attach(FxNode node, FxModel model);

    public abstract void rename(FxInstance instance, PropertyValue pv, String newName);
}
