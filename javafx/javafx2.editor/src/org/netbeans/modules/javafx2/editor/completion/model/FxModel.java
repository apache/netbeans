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
package org.netbeans.modules.javafx2.editor.completion.model;

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.modules.javafx2.editor.completion.beans.FxDefinition;
import org.netbeans.modules.javafx2.editor.parser.ModelAccessor;
import org.netbeans.modules.javafx2.editor.parser.NodeInfo;

/**
 * Represents a single FXML source file.
 * 
 * @author sdedic
 */
public final class FxModel extends FxNode {
    private URL                        baseURL;
    /**
     * Import declarations
     */
    private List<ImportDecl>           imports = Collections.emptyList();
    
    /**
     * Definitions, keyed by ID
     */
    private Map<String, FxNewInstance>    definitions = Collections.emptyMap();
    
    /**
     * The declared language for scripting
     */
    private LanguageDecl               language;
    
    /**
     * Root component instance
     */
    @NullAllowed
    private FxObjectBase                 rootComponent;
    
    /**
     * Value of the 'controller' attribute of the root element
     */
    private String                      controller;
    
    private ElementHandle<TypeElement>  controllerType;
    
    /**
     * Instance with IDs; both definitions and ordinary instances with fx:id
     */
    private Map<String, ? extends FxInstance>  namedInstances = Collections.emptyMap();
    
    public List<ImportDecl> getImports() {
        return imports;
    }

    public Collection<FxNewInstance> getDefinitions() {
        return definitions.values();
    }
    
    @CheckForNull
    public LanguageDecl getLanguage() {
        return language;
    }

    /**
     * Provides the root component of the FXML. May be null, if root
     * element is missing or does not represent a Component instance
     * 
     * @return root component
     */
    @CheckForNull
    public FxObjectBase getRootComponent() {
        return rootComponent;
    }
    
    FxModel(URL baseURL) {
        this.baseURL = baseURL;
    }

    public URL getBaseURL() {
        return baseURL;
    }
    
    void setLanguage(LanguageDecl lang) {
        this.language = lang;
    }
    
    void setImports(List<ImportDecl> decls) {
        this.imports = Collections.unmodifiableList(decls);
    }
    
    void addDefinitions(Collection<FxNewInstance> defs) {
        if (defs.isEmpty()) {
            return;
        }
        Map<String, FxNewInstance> newInstances = new LinkedHashMap<String, FxNewInstance>(this.definitions.size() + defs.size());
        newInstances.putAll(definitions);
        addDefinitions(defs, newInstances);
        this.definitions = Collections.unmodifiableMap(newInstances);
    }
    
    private void addDefinitions(Collection<FxNewInstance> defs, Map<String, FxNewInstance> instances) {
        for (FxNewInstance i : defs) {
            if (i.getId() != null) {
                instances.put(i.getId(), i);
            }
        }
    }
    
    void setDefinitions(List<FxNewInstance> defs) {
        Map<String, FxNewInstance> newInstances = new LinkedHashMap<String, FxNewInstance>(defs.size());
        addDefinitions(defs, newInstances);
        this.definitions = Collections.unmodifiableMap(newInstances);
    }
    
    void setRootComponent(FxObjectBase root) {
        this.rootComponent = root;
    }
    
    public String getSourceName() {
        return "<source>"; // NOI18N
    }

    @Override
    public Kind getKind() {
        return Kind.Source;
    }

    @Override
    public void accept(FxNodeVisitor v) {
        v.visitSource(this);
    }
    
    
    void detachChild(FxNode child) {
        if (child instanceof ImportDecl) {
            imports.add((ImportDecl)child);
        } else if (child instanceof LanguageDecl) {
            language = null;
        }
        super.detachChild(child);
    }
    
    void setNamedInstances(Map<String, ? extends FxInstance> instances) {
        this.namedInstances = instances;
    }
    
    @NonNull
    public Set<String> getInstanceNames() {
        return Collections.unmodifiableSet(this.namedInstances.keySet());
    }
    
    @CheckForNull
    public FxInstance getInstance(String id ) {
        return namedInstances.get(id);
    }

    public String getController() {
        return controller;
    }

    void setController(String controller) {
        this.controller = controller;
    }

    public ElementHandle<TypeElement> getControllerType() {
        return controllerType;
    }

    void setControllerType(ElementHandle<TypeElement> controllerType) {
        this.controllerType = controllerType;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    void resolve(ElementHandle nameHandle, TypeMirrorHandle typeHandle, ElementHandle<TypeElement> sourceTypeHandle, FxDefinition info) {
        this.controllerType = nameHandle;
    }

    static {
        ModelAccessor.setInstance(new AccessorImpl());
    }
    
    private static final class AccessorImpl extends ModelAccessor {

        @Override
        public FxModel newModel(URL baseURL, List<ImportDecl> imports, List<FxNewInstance> defs) {
            FxModel m = new FxModel(baseURL);
            m.setImports(imports);
            m.setDefinitions(defs);
            
            return m;
        }

        @Override
        public ImportDecl createImport(String imported, boolean wildcard) {
            return new ImportDecl(imported, wildcard);
        }

        @Override
        public LanguageDecl createLanguage(String lang) {
            return new LanguageDecl(lang);
        }

        @Override
        public FxInclude createInclude(String included, String id) {
            return (FxInclude)new FxInclude(included).withId(id);
        }

        @Override
        public FxScriptFragment createScript(String sourceRef) {
            return new FxScriptFragment(sourceRef);
        }
        
        @Override
        public FxNewInstance createCustomRoot(String sourceName, String id) {
            FxNewInstance n = new FxNewInstance(sourceName, true);
            n.withId(id);
            return n;
        }

        @Override
        public FxNewInstance createInstance(String sourceName, CharSequence value, boolean constant, String factory, String id) {
            FxNewInstance n = new FxNewInstance(sourceName);
            n.fromValue(value).usingFactory(factory).withId(id);
            n.setConstant(constant);
            
            return n;
        }

        @Override
        public FxObjectBase createCopyReference(boolean copy, String targetName) {
            return copy ?
                    new FxInstanceCopy(targetName) :
                    new FxReference(targetName);
        }

        @Override
        public PropertySetter createProperty(String name, boolean implicit) {
            PropertySetter s = new PropertySetter(name);
            if (implicit) {
                s = s.asImplicitDefault();
            }
            return s;
        }

        @Override
        public StaticProperty createStaticProperty(String name, String sourceClassName) {
            return new StaticProperty(sourceClassName, name);
        }

        @Override
        public MapProperty createMapProperty(String name, Map<String, CharSequence> values) {
            MapProperty m = new MapProperty(name);
            m.setValues(values);
            return m;
        }

        @Override
        public EventHandler createEventHandler(String eventName) {
            return new EventHandler(eventName);
        }

        @Override
        public FxNode createErrorElement(String localName) {
            return new Dummy(localName);
        }
        
        

        @Override
        public EventHandler asMethodRef(EventHandler h) {
            return h.asMethodRef();
        }

        @Override
        public void initModel(FxModel model, String controller, FxInstance rootInstance, LanguageDecl language) {
            model.setController(controller);
            model.setRootComponent(rootInstance);
            model.setLanguage(language);
        }

        @Override
        @SuppressWarnings("rawtypes")
        public void resolve(FxNode n, ElementHandle nameHandle, TypeMirrorHandle typeHandle, ElementHandle<TypeElement> sourceTypeHandle, FxDefinition info) {
            n.resolve(nameHandle, typeHandle, sourceTypeHandle, info);
        }

        @Override
        public void addContent(HasContent content, CharSequence additionalContent) {
            if (content instanceof EventHandler) {
                ((EventHandler)content).addContent(additionalContent);
            } else if (content instanceof PropertySetter) {
                ((PropertySetter)content).addContent(additionalContent);
            } else if (content instanceof FxScriptFragment) {
                ((FxScriptFragment)content).addContent(additionalContent);
            } else {
                throw new IllegalArgumentException();
            }
        }

        @Override
        public void addChild(FxNode parent, FxNode child) throws IllegalArgumentException {
            parent.addChild(child);
        }

        @Override
        public void resolveResource(HasResource decl, URL resolved) {
            // TODO: build a method into an abstract predecessor
            if (decl instanceof FxInclude) {
                ((FxInclude)decl).resolveFile(resolved);
            } else if (decl instanceof FxScriptFragment) {
                ((FxScriptFragment)decl).resolveSource(resolved);
            }
        }

        @Override
        public NodeInfo i(FxNode n) {
            return n.i();
        }

        @Override
        public <T extends FxNode> T makeBroken(T n) {
            n.markError();
            return n;
        }

        @Override
        public void setNamedInstances(FxModel model, Map<String, FxInstance> instances) {
            model.setNamedInstances(instances);
        }
        
        @Override
        public void addDefinitions(FxModel model, Collection<FxNewInstance> definitions) {
            model.addDefinitions(definitions);
        }

        @Override
        public void resolveReference(FxObjectBase copyOrReference, FxInstance original) {
            if (copyOrReference instanceof FxInstanceCopy) {
                ((FxInstanceCopy)copyOrReference).resolveBlueprint(original);
            } else if (copyOrReference instanceof FxReference) {
                ((FxReference)copyOrReference).resolveTarget((FxNewInstance)original);
            } else if (copyOrReference instanceof FxInclude) {
                ((FxInclude)copyOrReference).resolveTarget((FxNewInstance)original);
            } else {
                throw new IllegalArgumentException();
            }
        }

        @Override
        public FxNode createElement(String localName) {
            return new XmlNode(localName);
        }

        @Override
        public void attach(FxNode node, FxModel model) {
            node.setModel(model);
        }

        @Override
        public void rename(FxInstance instance, PropertyValue value, String newName) {
            instance.propertyRenamed(value);
        }
    }
}
