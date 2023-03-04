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
package org.netbeans.modules.web.el.spi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.ExecutableElement;
import org.netbeans.modules.csl.api.CodeCompletionContext;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.web.el.CompilationContext;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author marekfukala
 */
public abstract class ELPlugin {

    /** Name - id of the ELPlugin */
    public abstract String getName();

    /** A list of file mimetypes which this plugin is registered for. */
    public abstract Collection<String> getMimeTypes();

    /** A list of EL implicit objects for given file */
    public abstract Collection<ImplicitObject> getImplicitObjects(FileObject file);

    /** A list of resource bundles for given file */
    public abstract List<ResourceBundle> getResourceBundles(FileObject file, ResolverContext context);

    /** A list of functions for given file */
    public abstract List<Function> getFunctions(FileObject file);
    
    /**
     * Check {@link ExecutableElement} can be used as valid property in context for code on caret.
     * @param executableElement element to be checked. Can not be null.
     * @param source content of this Source will be checked. Can not be null.
     * @param completionContext code completion context to get additional informations.
     * @param compilationContext compilation context to get additional informations.
     * @return <b>True</b> when {@link ExecutableElement} can be used as valid property in context for code on caret.
     * Returns <b>false</b> when not. Default implementation returns <b>false</b>. <br /> <b>False</b> is returned on any error. 
     * @since 1.20
     */
    public boolean isValidProperty(ExecutableElement executableElement, Source source, CodeCompletionContext completionContext, CompilationContext compilationContext) {
        return false;
    }

    public static class Query {

        public static Collection<? extends ELPlugin> getELPlugins() {
            Collection<? extends ELPlugin> plugins =
                    Lookup.getDefault().lookupAll(ELPlugin.class);
            return plugins;
        }

        public static Collection<ImplicitObject> getImplicitObjects(FileObject file) {
            Set<ImplicitObject> result = new HashSet<ImplicitObject>();
            for (ELPlugin plugin : getELPlugins()) {
                result.addAll(plugin.getImplicitObjects(file));
            }
            return result;
        }

        public static List<ResourceBundle> getResourceBundles(FileObject file, ResolverContext context) {
            List<ResourceBundle> result = new ArrayList<ResourceBundle>();
             for (ELPlugin plugin : getELPlugins()) {
                result.addAll(plugin.getResourceBundles(file, context));
            }
            return result;
        }

        public static List<Function> getFunctions(FileObject file) {
            List<Function> result = new ArrayList<Function>();
             for (ELPlugin plugin : getELPlugins()) {
                result.addAll(plugin.getFunctions(file));
            }
            return result;
        }
        
        /**
         * Check {@link ExecutableElement} can be used as valid property in context for code on caret.
         * @param executableElement element to be checked. Can not be null.
         * @param source content of this Source will be checked. Can not be null.
         * @param completionContext code completion context to get additional informations.
         * @param compilationContext compilation context to get additional informations.
         * @return <b>True</b> when {@link ExecutableElement} can be used as valid property in context for code on caret.
         * Returns <b>false</b> when not. Default implementation returns <b>false</b>. <br /> <b>False</b> is returned on any error. 
         * @since 1.20
         */
        public static boolean isValidProperty(ExecutableElement executableElement, Source source, CompilationContext compilationContext, CodeCompletionContext completionContext) {
            for (ELPlugin plugin : getELPlugins()) {
                boolean correspondsToSignature = plugin.isValidProperty(executableElement, source, completionContext, compilationContext);
                if (correspondsToSignature) {
                    return true;
                }
            }
            
            return false;
        }

    }
}
