/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
