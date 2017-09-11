/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
