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
package org.netbeans.modules.lsp.client.options;

import com.google.gson.Gson;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.event.ChangeEvent;
import org.eclipse.tm4e.core.registry.IRegistryOptions;
import org.eclipse.tm4e.core.registry.Registry;
import org.netbeans.modules.textmate.lexer.TextmateTokenId;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataLoaderPool;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

/**
 *
 * @author lahvac
 */
public class LanguageStorage {

    private static final String KEY = "language.descriptions";

    static List<LanguageDescription> load() {
        String descriptions = NbPreferences.forModule(LanguageServersPanel.class).get(KEY, "[]");
        return Arrays.stream(new Gson().fromJson(descriptions, LanguageDescription[].class)).collect(Collectors.toList());
    }

    static void store(List<LanguageDescription> languages) {
        Set<String> originalMimeTypes = load().stream().map(ld -> ld.mimeType).collect(Collectors.toSet());
        Set<String> mimeTypesToClear = new HashSet<>(originalMimeTypes);

        FileUtil.runAtomicAction((Runnable) () -> {
            FileObject mimeResolver = FileUtil.getConfigFile("Services/MIMEResolver");

            if (mimeResolver == null) {
                try {
                    mimeResolver = FileUtil.createFolder(FileUtil.getConfigRoot(), "Services/MIMEResolver");
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            
            for (FileObject children : mimeResolver.getChildren()) {
                if ("synthetic".equals(children.getAttribute(LanguageServersPanel.class.getName()))) {
                    try {
                        children.delete();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }

            for (LanguageDescription description : languages) {
                try {
                    FileObject resolver = mimeResolver.getFileObject(description.id + ".xml");
                    if (resolver != null) {
                        //TODO: should happen?
                        resolver.delete();
                    }
                    resolver = mimeResolver.createData(description.id + ".xml");
                    Class<?> mimeResolverClass = Class.forName("org.openide.filesystems.MIMEResolver");
                    Method mimeResolverCreate = mimeResolverClass.getDeclaredMethod("create", FileObject.class);
                    resolver.setAttribute("methodvalue:instanceCreate", mimeResolverCreate);
                    resolver.setAttribute("instanceClass", "org.openide.filesystems.MIMEResolver");
                    resolver.setAttribute("mimeType", description.mimeType);
                    int c = 0;
                    for (String ext : description.extensions.split(" +")) {
                        resolver.setAttribute("ext." + c++, ext);
                    }
                    resolver.setAttribute(LanguageServersPanel.class.getName(), "synthetic");
                    FileObject syntax = FileUtil.getConfigFile("Editors/" + description.mimeType + "/syntax.json");
                    if (syntax != null) {
                        syntax.delete();
                    }
                    syntax = FileUtil.getConfigFile("Editors/" + description.mimeType + "/syntax.xml");
                    if (syntax != null) {
                        syntax.delete();
                    }
                    String ext = description.syntaxGrammar.substring(Math.max(0, description.syntaxGrammar.length() - ".json".length())).equalsIgnoreCase(".json") ? "json" : "xml";
                    syntax = FileUtil.createData(FileUtil.getConfigRoot(), "Editors/" + description.mimeType + "/syntax." + ext);
                    File grammar = new File(description.syntaxGrammar);
                    syntax.setAttribute("textmate-grammar", findScope(grammar));
                    try (InputStream in = new FileInputStream(grammar);
                         OutputStream out = syntax.getOutputStream()) {
                        FileUtil.copy(in, out);
                    }
                    FileObject loader = FileUtil.getConfigFile("Loaders/" + description.mimeType + "/Factories/data-object.instance");
                    if (loader != null) {
                        loader.delete();
                    }
                    loader = FileUtil.createData(FileUtil.getConfigRoot(), "Loaders/" + description.mimeType + "/Factories/data-object.instance");
                    loader.setAttribute("position", 300);
                    Class<?> dataLoaderPoolClass = GenericDataObject.class;
                    Method dataLoaderPoolFactory = dataLoaderPoolClass.getDeclaredMethod("factory");
                    //TODO: display name
                    loader.setAttribute("methodvalue:instanceCreate", dataLoaderPoolFactory);
                    loader.setAttribute("instanceOf", DataObject.Factory.class.getName());
                    loader.setAttribute("dataObjectClass", GenericDataObject.class.getName());
                    loader.setAttribute("mimeType", description.mimeType);

                    FileObject icon = FileUtil.getConfigFile("Loaders/" + description.mimeType + "/Factories/icon.png");
                    if (icon != null) {
                        icon.delete();
                    }
                    File iconFile = description.icon != null ? new File(description.icon) : null;
                    if (iconFile != null && iconFile.isFile()) {
                        icon = FileUtil.createData(FileUtil.getConfigRoot(), "Loaders/" + description.mimeType + "/Factories/icon.png");
                        try (InputStream in = new FileInputStream(iconFile);
                             OutputStream out = icon.getOutputStream()) {
                            FileUtil.copy(in, out);
                        }

                        loader.setAttribute("iconBase", icon.getNameExt());
                    }
                    
                    if (description.languageServer != null && !description.languageServer.isEmpty()) {
                        FileObject langServer = FileUtil.createData(FileUtil.getConfigRoot(), "Editors/" + description.mimeType + "/org-netbeans-modules-lsp-client-options-GenericLanguageServer.instance");
                        langServer.setAttribute("command", description.languageServer.split(" "));
                        if (description.name != null) {
                            langServer.setAttribute("name", description.name);
                        }
                    }
                    
                    mimeTypesToClear.remove(description.mimeType);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

            for (String mimeType : mimeTypesToClear) {
                try {
                    FileObject syntax = FileUtil.getConfigFile("Editors/" + mimeType + "/syntax.json");
                    if (syntax != null) {
                        syntax.delete();
                    }
                    FileObject langServer = FileUtil.getConfigFile("Editors/" + mimeType + "/org-netbeans-modules-lsp-client-options-GenericLanguageServer.instance");
                    if (langServer != null) {
                        langServer.delete();
                    }
                    FileObject loader = FileUtil.getConfigFile("Loaders/" + mimeType + "/Factories/data-object.instance");
                    if (loader != null) {
                        loader.delete();
                    }
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });

        try {
            Method resetCache = Class.forName("org.openide.filesystems.MIMESupport").getDeclaredMethod("resetCache");
            resetCache.setAccessible(true);
            resetCache.invoke(null);

            GenericDataObject.invalidate();

            Method fireChangeEvent = DataLoaderPool.class.getDeclaredMethod("fireChangeEvent", ChangeEvent.class);
            fireChangeEvent.setAccessible(true);
            fireChangeEvent.invoke(DataLoaderPool.getDefault(), new ChangeEvent(DataLoaderPool.getDefault()));

            TextmateTokenId.LanguageHierarchyImpl.refreshGrammars();

            Class<?> providerRegistry = Class.forName("org.netbeans.modules.navigator.ProviderRegistry", false, NavigatorPanel.class.getClassLoader());
            Method getInstance = providerRegistry.getDeclaredMethod("getInstance");
            getInstance.setAccessible(true);
            Object providerRegistryInstance = getInstance.invoke(null);

            if (providerRegistryInstance != null) {
                Field file2Providers = providerRegistry.getDeclaredField("file2Providers");
                file2Providers.setAccessible(true);
                Map<?,?> file2ProvidersInstance = (Map<?,?>) file2Providers.get(providerRegistryInstance);
                if (file2ProvidersInstance != null) {
                    file2ProvidersInstance.clear();
                }
            }
        } catch (ReflectiveOperationException ex) {
            Exceptions.printStackTrace(ex);
        }

        NbPreferences.forModule(LanguageServersPanel.class).put(KEY, new Gson().toJson(languages));
    }

    private static String findScope(File grammar) throws Exception {
        IRegistryOptions opts = new IRegistryOptions() {
            @Override
            public String getFilePath(String scopeName) {
                return null;
            }
            @Override
            public InputStream getInputStream(String scopeName) throws IOException {
                return null;
            }
            @Override
            public Collection<String> getInjections(String scopeName) {
                return null;
            }
        };
        return new Registry(opts).loadGrammarFromPathSync(grammar).getScopeName();
    }
    
    public static class LanguageDescription {

        public String id;
        public String extensions;
        public String syntaxGrammar;
        public String languageServer;
        public String name;
        public String icon;
        public String mimeType;

        public LanguageDescription() {
            this.id = null;
            this.extensions = null;
            this.syntaxGrammar = null;
            this.languageServer = null;
            this.name = null;
            this.icon = null;
            this.mimeType = null;
        }

        public LanguageDescription(String id, String extensions, String syntaxGrammar, String languageServer, String name, String icon) {
            this.id = id;
            this.extensions = extensions;
            this.syntaxGrammar = syntaxGrammar;
            this.languageServer = languageServer;
            this.name = name;
            this.icon = icon;
            this.mimeType = "text/x-ext-" + id;
        }

    }
}
