/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
