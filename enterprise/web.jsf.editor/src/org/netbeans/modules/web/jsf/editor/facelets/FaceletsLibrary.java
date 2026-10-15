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
package org.netbeans.modules.web.jsf.editor.facelets;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.web.jsfapi.api.LibraryType;
import org.netbeans.modules.web.jsfapi.spi.LibraryUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;

/**
 * Represents a facelets library defined by the facelets library VDL descriptor (.taglib.xml) file.
 * The descriptor must declare the library namespace.
 * 
 * The library may contain both class or composite components
 * 
 * @author marekfukala
 */
public class FaceletsLibrary extends AbstractFaceletsLibrary {

    /** 
     * The namespace declared in the facelets library descriptor
     */
    private final Set<String> validNamespaces;
    
    private final Map<String, NamedComponent> components = new HashMap<>();
    private LibraryDescriptor libraryDescriptor, faceletsLibraryDescriptor;
    private String defaultPrefix;
    private final URL libraryDescriptorSource;

    public FaceletsLibrary(FaceletsLibrarySupport support, Set<String> allValidNamespaces, URL libraryDescriptorSourceURL) {
        super(support, allValidNamespaces.iterator().next());

        this.validNamespaces = allValidNamespaces;
        this.libraryDescriptorSource = libraryDescriptorSourceURL;
    }
    
    protected synchronized LibraryDescriptor getFaceletsLibraryDescriptor() throws LibraryDescriptorException {
        if(faceletsLibraryDescriptor == null) {
            FileObject libraryDescriptorSourceFile = URLMapper.findFileObject(libraryDescriptorSource);
            faceletsLibraryDescriptor = FaceletsLibraryDescriptor.create(libraryDescriptorSourceFile);
        }
        return faceletsLibraryDescriptor;
    }
    
    @Override
    public Map<String, ? extends NamedComponent> getComponentsMap() {
        return components;
    }

    @Override
    public String getNamespace() {
        return validNamespaces.iterator().next();
    }

    @Override
    public URL getLibraryDescriptorSource() {
        return libraryDescriptorSource;
    }

    @Override
    public LibraryType getType() {
        return LibraryType.CLASS;
    }

    @Override
    public String getDefaultNamespace() {
        return null;
    }

    @Override
    public synchronized String getDefaultPrefix() {
        if(defaultPrefix == null) {
            try {
                //first try to get the prefix from the facelets library descriptor
                defaultPrefix = getFaceletsLibraryDescriptor().getPrefix();
            } catch (LibraryDescriptorException ex) {
                Exceptions.printStackTrace(ex);
            }
            
            if(defaultPrefix == null) {
                //no prefix defined in the library descriptor
                //if standard library, we have hardcododed prefixes
                defaultPrefix = super.getDefaultPrefix();
            }
            
            if(defaultPrefix == null) {
                //non standard library will use a prefix generated from the library namespace
                defaultPrefix = LibraryUtils.generateDefaultPrefix(getNamespace());
                
            }
        }
        return defaultPrefix;
    }
    

    @Override
    public synchronized LibraryDescriptor getLibraryDescriptor() {
        if(libraryDescriptor == null) {
            try {
                //create a merged library descriptor from facelets VDL descriptor and the JSP taglib descriptor
                //the reason for this is that often the facelets descriptor doesn't declare the components metadata but the
                //jsp tag library descriptor does
                libraryDescriptor = new TldProxyLibraryDescriptor(getFaceletsLibraryDescriptor(), support.getJsfSupport().getIndex());
            } catch (LibraryDescriptorException ex) {
                //error in parsing the descriptors
                Exceptions.printStackTrace(ex);
            }
        }
        return libraryDescriptor;
    }

    @Override
    public void putConverter(String name, String id) {
        components.put(name, new Converter(name, id, null));
    }

    @Override
    public void putConverter(String name, String id, Class handlerClass) {
        components.put(name, new Converter(name, id, handlerClass));
    }

    @Override
    public void putValidator(String name, String id) {
        components.put(name, new Validator(name, id, null));
    }

    @Override
    public void putValidator(String name, String id, Class handlerClass) {
        components.put(name, new Validator(name, id, handlerClass));
    }

    @Override
    public void putBehavior(String name, String id) {
        components.put(name, new Behavior(name, id, null));
    }

    @Override
    public void putBehavior(String name, String id, Class handlerClass) {
        components.put(name, new Behavior(name, id, handlerClass));
    }

    @Override
    public void putTagHandler(String name, Class type) {
        components.put(name, new TagHandler(name, type));
    }

    @Override
    public void putComponent(String name, String componentType,
            String rendererType) {
        components.put(name, new Component(name, componentType, rendererType, null));
    }

    @Override
    public void putComponent(String name, String componentType,
            String rendererType, Class handlerClass) {
        components.put(name, new Component(name, componentType, rendererType, handlerClass));
    }

    @Override
    public void putUserTag(String name, URL source) {
        components.put(name, new UserTag(name, source));
    }

    @Override
    public void putCompositeComponentTag(String name, String resourceId) {
        components.put(name, new CompositeComponentTag(name, resourceId));
    }

    @Override
    public void putFunction(String name, Method method) {
        components.put(name, new Function(name, method));
    }

    public NamedComponent createNamedComponent(String name) {
        return new NamedComponent(name);
    }

    public Function createFunction(String name, Method method) {
        return new Function(name, method);
    }

    @Override
    public Set<String> getValidNamespaces() {
        return validNamespaces;
    }
}
