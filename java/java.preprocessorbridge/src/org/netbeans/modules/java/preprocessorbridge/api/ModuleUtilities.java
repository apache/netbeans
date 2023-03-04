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
package org.netbeans.modules.java.preprocessorbridge.api;

import com.sun.source.tree.ModuleTree;
import java.io.IOException;
import java.util.Collection;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.java.preprocessorbridge.JavaSourceUtilImplAccessor;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaSourceUtilImpl;
import org.openide.util.Lookup;

/**
 * Contains utility methods to create private copy of javac, used to work with module-infos.
 * 
 * @author Dusan Balek
 * @since 1.45
 */
public final class ModuleUtilities {

    private static final Lookup.Result<JavaSourceUtilImpl> result = Lookup.getDefault().lookupResult(JavaSourceUtilImpl.class);
    private final Object javaSource;
    private JavaSourceUtilImpl.ModuleInfoHandle handle = null;

    private ModuleUtilities(@NonNull final Object javaSource) {
        this.javaSource = javaSource;
    }

    /**
     * Returns the instance of this class.
     * 
     * @param javaSource JavaSource representing module-info.java file to be parsed,
     * class file to be read, or ClasspathInfo to be scanned
     * @return {@link ModuleUtilities} instance
     */
    public static ModuleUtilities get(@NonNull final Object javaSource) {
        return new ModuleUtilities(javaSource);
    }
    
    /**
     * Parses module name from the module-info.java file represented by the JavaSource.
     * 
     * @param javaSource JavaSource representing module-info.java file to be parsed
     * @return module name
     * @throws IOException if the module-info.java does not exist or cannot be parsed. 
     */
    @CheckForNull
    public String parseModuleName() throws IOException {
        return init() ? handle.parseModuleName() : null;
    }
    
    /**
     * Parses ModuleElement from the module-info.java file represented by the JavaSource.
     * 
     * @param javaSource JavaSource representing module-info.java file to be parsed
     * @return {@link ModuleElement} of the given module
     * @throws IOException if the module-info.java does not exist or cannot be parsed. 
     */
    @CheckForNull
    public ModuleTree parseModule() throws IOException {
        return init() ? handle.parseModule() : null;
    }

    /**
     * Resolves a {@link ModuleTree} into the {@link ModuleElement}.
     * @param moduleTree the {@link ModuleTree} to resolve.
     * @return the resolved {@link ModuleElement} or null
     * @throws IOException in case of IO error.
     * @since 1.49
     */
    @CheckForNull
    public ModuleElement resolveModule(@NonNull final ModuleTree moduleTree) throws IOException {
        return init() ? handle.resolveModule(moduleTree) : null;
    }

    /**
     * Resolves module within the ClasspathInfo represented by the JavaSource.
     * 
     * @param moduleName name of the module to be resolved
     * @return module name
     * @throws IOException in case of IO problem
     */
    @CheckForNull
    public ModuleElement resolveModule(String moduleName) throws IOException {
        return init() ? handle.resolveModule(moduleName) : null;
    }
    
    /**
     * Reads a class file represented by the JavaSource.
     * @return the {@link TypeElement} of given class file
     * @throws IOException in case of IO problem
     */
    @CheckForNull
    public TypeElement readClassFile() throws IOException {
        return init() ? handle.readClassFile() : null;
    }

    private boolean init() throws IOException {
        if (handle == null) {
            final Collection<? extends JavaSourceUtilImpl> instances = result.allInstances();
            int size = instances.size();
            if (size == 1) {
                handle = JavaSourceUtilImplAccessor.getInstance().getModuleInfoHandle(instances.iterator().next(), javaSource);
            }
        }
        return handle != null;
    }
}
