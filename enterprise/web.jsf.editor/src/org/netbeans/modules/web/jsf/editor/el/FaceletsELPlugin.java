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

package org.netbeans.modules.web.jsf.editor.el;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.web.el.spi.ELPlugin;
import org.netbeans.modules.web.el.spi.Function;
import org.netbeans.modules.web.el.spi.ImplicitObject;
import org.netbeans.modules.web.el.spi.ImplicitObjectType;

import static org.netbeans.modules.web.el.spi.ImplicitObjectType.*;

import org.netbeans.modules.web.el.spi.ResolverContext;
import org.netbeans.modules.web.el.spi.ResourceBundle;
import org.netbeans.modules.web.jsf.api.editor.JSFResourceBundlesProvider;
import org.netbeans.modules.web.jsf.editor.JsfUtils;
import org.netbeans.modules.web.jsf.editor.facelets.DefaultFaceletLibraries;
import org.netbeans.modules.web.jsf.editor.facelets.FaceletsLibraryDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author marekfukala
 */
@ServiceProvider(service=ELPlugin.class)
public class FaceletsELPlugin extends ELPlugin {

    private static final String PLUGIN_NAME = "JSF Facelets EL Plugin"; //NOI18N

    private Collection<ImplicitObject> implObjects;
    private Collection<ImplicitObject> implObjectsJakarta;

    @Override
    public String getName() {
        return PLUGIN_NAME;
    }

    @Override
    public Collection<String> getMimeTypes() {
        return Collections.singletonList(JsfUtils.XHTML_MIMETYPE);
    }

    @Override
    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public synchronized Collection<ImplicitObject> getImplicitObjects(FileObject file) {
        if(!getMimeTypes().contains(file.getMIMEType())) {
            return Collections.emptyList();
        }

        if(implObjects == null) {
            List<ImplicitObject> implObjectsBuilder = new ArrayList<>(9);

            implObjectsBuilder.addAll(getScopeObjects());

            implObjectsBuilder.add(new JsfImplicitObject("facesContext", "javax.faces.context.FacesContext", OBJECT_TYPE)); //NOI18N
            implObjectsBuilder.add(new JsfImplicitObject("application",  "javax.servlet.ServletContext", OBJECT_TYPE)); //NOI18N
            implObjectsBuilder.add(new JsfImplicitObject("component", "javax.faces.component.UIComponent", OBJECT_TYPE)); //NOI18N
            implObjectsBuilder.add(new JsfImplicitObject("flash", "javax.faces.context.Flash", OBJECT_TYPE)); //NOI18N
            implObjectsBuilder.add(new JsfImplicitObject("resource", "javax.faces.application.ResourceHandler", OBJECT_TYPE)); //NOI18N
            implObjectsBuilder.add(new JsfImplicitObject("session", "javax.servlet.http.HttpSession", OBJECT_TYPE)); //NOI18N
            implObjectsBuilder.add(new JsfImplicitObject("view", "javax.faces.component.UIViewRoot", OBJECT_TYPE)); //NOI18N
            implObjectsBuilder.add(new JsfImplicitObject("cookie", "java.util.Map", MAP_TYPE)); //NOI18N
            implObjectsBuilder.add(new JsfImplicitObject("cc", "javax.faces.component.UIComponent", RAW)); //NOI18N
            implObjectsBuilder.add(new JsfImplicitObject("request", "javax.servlet.http.HttpServletRequest", OBJECT_TYPE)); //NOI18N
            implObjectsBuilder.add(new JsfImplicitObject("header", "java.util.Map", MAP_TYPE)); //NOI18N
            implObjectsBuilder.add(new JsfImplicitObject("headerValues", "java.util.Map", MAP_TYPE)); //NOI18N
            implObjectsBuilder.add(new JsfImplicitObject("initParam", "java.util.Map", MAP_TYPE)); //NOI18N
            implObjectsBuilder.add(new JsfImplicitObject("param", "java.util.Map", MAP_TYPE)); //NOI18N
            implObjectsBuilder.add(new JsfImplicitObject("paramValues", "java.util.Map", MAP_TYPE)); //NOI18N

            implObjects = Collections.unmodifiableCollection(implObjectsBuilder);


            List<ImplicitObject> implObjectsJakartaBuilder = new ArrayList<>(9);

            implObjectsJakartaBuilder.addAll(getScopeObjects());

            implObjectsJakartaBuilder.add(new JsfImplicitObject("facesContext", "jakarta.faces.context.FacesContext", OBJECT_TYPE)); //NOI18N
            implObjectsJakartaBuilder.add(new JsfImplicitObject("application",  "jakarta.servlet.ServletContext", OBJECT_TYPE)); //NOI18N
            implObjectsJakartaBuilder.add(new JsfImplicitObject("component", "jakarta.faces.component.UIComponent", OBJECT_TYPE)); //NOI18N
            implObjectsJakartaBuilder.add(new JsfImplicitObject("flash", "jakarta.faces.context.Flash", OBJECT_TYPE)); //NOI18N
            implObjectsJakartaBuilder.add(new JsfImplicitObject("resource", "jakarta.faces.application.ResourceHandler", OBJECT_TYPE)); //NOI18N
            implObjectsJakartaBuilder.add(new JsfImplicitObject("session", "jakarta.servlet.http.HttpSession", OBJECT_TYPE)); //NOI18N
            implObjectsJakartaBuilder.add(new JsfImplicitObject("view", "jakarta.faces.component.UIViewRoot", OBJECT_TYPE)); //NOI18N
            implObjectsJakartaBuilder.add(new JsfImplicitObject("cookie", "java.util.Map", MAP_TYPE)); //NOI18N
            implObjectsJakartaBuilder.add(new JsfImplicitObject("cc", "jakarta.faces.component.UIComponent", RAW)); //NOI18N
            implObjectsJakartaBuilder.add(new JsfImplicitObject("request", "jakarta.servlet.http.HttpServletRequest", OBJECT_TYPE)); //NOI18N
            implObjectsJakartaBuilder.add(new JsfImplicitObject("header", "java.util.Map", MAP_TYPE)); //NOI18N
            implObjectsJakartaBuilder.add(new JsfImplicitObject("headerValues", "java.util.Map", MAP_TYPE)); //NOI18N
            implObjectsJakartaBuilder.add(new JsfImplicitObject("initParam", "java.util.Map", MAP_TYPE)); //NOI18N
            implObjectsJakartaBuilder.add(new JsfImplicitObject("param", "java.util.Map", MAP_TYPE)); //NOI18N
            implObjectsJakartaBuilder.add(new JsfImplicitObject("paramValues", "java.util.Map", MAP_TYPE)); //NOI18N

            implObjectsJakarta = Collections.unmodifiableCollection(implObjectsJakartaBuilder);
        }

        ClassPath cp = ClassPath.getClassPath(file, ClassPath.COMPILE);
        boolean jakartaVariant = cp != null && cp.findResource("jakarta/servlet/http/HttpServlet.class") != null;
        if(jakartaVariant) {
            return implObjectsJakarta;
        } else {
            return implObjects;
        }
    }

    @Override
    public List<ResourceBundle> getResourceBundles(FileObject file, ResolverContext context) {
        Project project = FileOwnerQuery.getOwner(file);
        if (project == null) {
            return Collections.emptyList();
        }

        // caches bundles if not loaded yet
        if (context.getContent(FaceletsELPlugin.class.getName()) == null) {
            context.setContent(FaceletsELPlugin.class.getName(), JSFResourceBundlesProvider.getResourceBundles(project));
        }

        return (List<ResourceBundle>) context.getContent(FaceletsELPlugin.class.getName());
    }

    /**
     * @return the implicit scope objects, i.e. {@code requestScope, sessionScope} etc.
     */
    private static Collection<ImplicitObject> getScopeObjects() {
        Collection<ImplicitObject> result = new ArrayList<>(5);
        result.add(new JsfImplicitObject("sessionScope", "java.util.Map", SCOPE_TYPE)); // NOI18N
        result.add(new JsfImplicitObject("applicationScope", "java.util.Map", SCOPE_TYPE)); // NOI18N
        result.add(new JsfImplicitObject("requestScope", "java.util.Map", SCOPE_TYPE)); // NOI18N
        result.add(new JsfImplicitObject("viewScope", "java.util.Map", SCOPE_TYPE)); // NOI18N
        result.add(new JsfImplicitObject("flowScope", "java.util.Map", SCOPE_TYPE)); // NOI18N
        return result;
    }

    @Override
    public List<Function> getFunctions(FileObject file) {
        List<Function> functions =  new ArrayList<>();
        final Map<String, String> namespaces = new HashMap<>();

        try {
            Source source = Source.create(file);
            ParserManager.parse(Collections.singletonList(source), new UserTask() {

                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    Result parseResult = JsfUtils.getEmbeddedParserResult(resultIterator, "text/html"); //NOI18N
                    if (parseResult instanceof HtmlParserResult) {
                        namespaces.putAll(((HtmlParserResult) parseResult).getNamespaces());
                    }
                }
            });
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }

        Map<String, FaceletsLibraryDescriptor> librariesDescriptors = DefaultFaceletLibraries.getInstance().getLibrariesDescriptors();
        for (Map.Entry<String, FaceletsLibraryDescriptor> entry : librariesDescriptors.entrySet()) {
            String currentPrefix = namespaces.get(entry.getKey());
            if (currentPrefix != null) {
                functions.addAll(getFunctionsFromDescriptor(entry.getValue(), currentPrefix));
            }
        }

        return functions;
    }

     private static List<Function> getFunctionsFromDescriptor(FaceletsLibraryDescriptor descriptor, String prefix) {
        List<Function> functions = new ArrayList<>();
        for (Map.Entry<String, org.netbeans.modules.web.jsfapi.api.Function> entry : descriptor.getFunctions().entrySet()) {
            org.netbeans.modules.web.jsfapi.api.Function function = entry.getValue();
            functions.add(new Function(
                    prefix + ":" + function.getName(),
                    getReturnTypeForSignature(function.getSignature()),
                    getParametersForSignature(function.getSignature()),
                    function.getDescription()));
        }

        return functions;
    }

    private static String getReturnTypeForSignature(String signature) {
        String returnType = signature.substring(0, signature.indexOf(" ")); //NOI18N
        return getSimpleNameForType(returnType.trim());
    }

    private static List<String> getParametersForSignature(String signature) {
        List<String> params = new ArrayList<>();
        String paramString = signature.substring(signature.indexOf("(") + 1, signature.indexOf(")")); //NOI18N
        for (String param : paramString.split(",")) { //NOI18N
            params.add(getSimpleNameForType(param.trim()));
        }
        return params;
    }

    private static String getSimpleNameForType(String fqn) {
        return fqn.substring(fqn.lastIndexOf(".") + 1); //NOI18N
    }

    private static class JsfImplicitObject implements ImplicitObject {

        private final String name;
        private final String clazz;
        private final ImplicitObjectType type;

        public JsfImplicitObject(String name, String clazz, ImplicitObjectType type) {
            this.name = name;
            this.clazz = clazz;
            this.type = type;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public ImplicitObjectType getType() {
            return type;
        }

        @Override
        public String getClazz() {
            return clazz;
        }

    }


}
